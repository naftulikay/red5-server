package org.red5.server.net.rtmp;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.ByteBuffer;
import org.red5.server.api.IContext;
import org.red5.server.api.IScope;
import org.red5.server.api.IServer;
import org.red5.server.api.Red5;
import org.red5.server.net.protocol.ProtocolState;
import org.red5.server.net.rtmp.codec.RTMP;
import org.red5.server.net.rtmp.message.Constants;
import org.red5.server.net.rtmp.message.InPacket;
import org.red5.server.net.rtmp.message.Invoke;
import org.red5.server.net.rtmp.message.Message;
import org.red5.server.net.rtmp.message.OutPacket;
import org.red5.server.net.rtmp.message.PacketHeader;
import org.red5.server.net.rtmp.message.Ping;
import org.red5.server.net.rtmp.message.StreamBytesRead;
import org.red5.server.net.rtmp.message.Unknown;
import org.red5.server.net.rtmp.status.StatusCodes;
import org.red5.server.net.rtmp.status.StatusObjectService;
import org.red5.server.service.Call;
import org.red5.server.stream.Stream;

public class RTMPHandler 
	implements Constants, StatusCodes {
	
	protected static Log log =
        LogFactory.getLog(RTMPHandler.class.getName());
	
	protected StatusObjectService statusObjectService;
	protected IServer server;
	
	public void setServer(IServer server) {
		this.server = server;
	}

	public void setStatusObjectService(StatusObjectService statusObjectService) {
		this.statusObjectService = statusObjectService;
	}

	public void messageReceived(RTMPConnection conn, ProtocolState state, Object in) throws Exception {
			
		try {
			
			final InPacket packet = (InPacket) in;
			final Message message = packet.getMessage();
			final PacketHeader source = packet.getSource();
			final Channel channel = conn.getChannel(packet.getSource().getChannelId());
			final Stream stream = conn.getStreamById(source.getStreamId());
			
			if(log.isDebugEnabled()){
				log.debug("Message recieved");
				log.debug("Stream Id: "+source);
				log.debug("Channel: "+channel);
			}
				
			// Thread local performance ? Should we benchmark
			Red5.setConnectionLocal(conn);
			
			switch(message.getDataType()){
			case TYPE_INVOKE:
			case TYPE_NOTIFY: // just like invoke, but does not return
				if (((Invoke) message).getCall() == null && stream != null)
					// Stream metadata
					stream.publish(message);
				else
					onInvoke(conn, channel, source, (Invoke) message);
				break;
			case TYPE_PING:
				onPing(conn, channel, source, (Ping) message);
				break;
			/*
			case TYPE_STREAM_BYTES_READ:
				onStreamBytesRead(conn, channel, source, (StreamBytesRead) message);
				break;
			case TYPE_AUDIO_DATA:
			case TYPE_VIDEO_DATA:
				log.info("in packet: "+source.getSize()+" ts:"+source.getTimer());
				stream.publish(message);
				break;
			case TYPE_SHARED_OBJECT:
				SharedObject so = (SharedObject) message;
				// DISABLED :: onSharedObject(conn, channel, source, so);
				break;
			*/
			}
			if(message instanceof Unknown){
				log.info(message);
			}
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			log.error("Exception",e);
		}
	}

	public void messageSent(RTMPConnection conn, Object message) {
		if(log.isDebugEnabled())
			log.debug("Message sent");
		
		if(message instanceof ByteBuffer){
			return;
		}
		
		OutPacket sent = (OutPacket) message;
		final byte channelId = sent.getDestination().getChannelId();
		final Stream stream = conn.getStreamByChannelId(channelId);
		if(stream!=null){
			stream.written(sent.getMessage());
		}
	}

	public void connectionClosed(RTMPConnection conn, RTMP state) {
		state.setState(RTMP.STATE_DISCONNECTED);
		conn.close();
	}
	
	
	public void invokeCall(RTMPConnection conn, Call call){
		conn.getScope().getHandler().serviceCall(conn,call);
	}
	
	// ------------------------------------------------------------------------------
	
	protected String getHostname(String url) {
		log.debug("url: "+url);
		return url.split("/")[2];
	}
	
	public void onInvoke(RTMPConnection conn, Channel channel, PacketHeader source, Invoke invoke){
		
		log.debug("Invoke");
		final Call call = invoke.getCall();
		
		if(call.getServiceName() == null){
			log.info("call: "+call);
			final String action = call.getServiceMethodName();
			log.info("--"+action);
			if(!conn.isConnected()){
				
				if(action.equals(ACTION_CONNECT)){
					log.debug("connect");
					final Map params = invoke.getConnectionParams();
					final String host = getHostname((String) params.get("tcUrl"));
					final String path = (String) params.get("app");
					final String sessionId = null;
					conn.setup(host, path, sessionId, params);
					try {
						final IContext context = server.lookupGlobal(host,path).getContext();
						final IScope scope = context.resolveScope(path);
						log.info("Connecting to: "+scope);
						if(conn.connect(scope)){
							log.debug("connected");
							log.debug("client: "+conn.getClient());
							call.setStatus(Call.STATUS_SUCCESS_RESULT);
							call.setResult(getStatus(NC_CONNECT_SUCCESS));
						} else {
							log.debug("connect failed");
							call.setStatus(Call.STATUS_ACCESS_DENIED);
							call.setResult(getStatus(NC_CONNECT_REJECTED));
						}
					} catch (RuntimeException e) {
						call.setStatus(Call.STATUS_GENERAL_EXCEPTION);
						call.setResult(getStatus(NC_CONNECT_FAILED));
						log.error("Error connecting",e);
					}
				}
			} else if(action.equals(ACTION_DISCONNECT)){
				conn.close();
			} else {
				log.debug(conn.getScope());
				log.debug(conn.getScope().getHandler());
				conn.getScope().getHandler().serviceCall(conn, call);
			}
		} else if(conn.isConnected()){
			// Service calls, must be connected.
			conn.getScope().getHandler().serviceCall(conn, call);
		} else {
			// Warn user attemps to call service without being connected
			log.warn("Not connected, closing connection");
			conn.close();
		}
		
		if(invoke.isAndReturn()){
		
			if(call.getStatus() == Call.STATUS_SUCCESS_VOID ||
				call.getStatus() == Call.STATUS_SUCCESS_NULL ){
				log.debug("Method does not have return value, do not reply");
				return;
			}
			Invoke reply = new Invoke();
			reply.setCall(call);
			reply.setInvokeId(invoke.getInvokeId());
			log.debug("sending reply");
			channel.write(reply);
		}
	}
	
	public Object getStatus(String code){
		return statusObjectService.getStatusObject(code);
	}
	
	public void onPing(RTMPConnection conn, Channel channel, PacketHeader source, Ping ping){
		final Ping pong = new Ping();
		pong.setValue1((short)(ping.getValue1()+1));
		pong.setValue2(ping.getValue2());
		channel.write(pong);
		log.info(ping);
		// No idea why this is needed, 
		// but it was the thing stopping the new rtmp code streaming
		final Ping pong2 = new Ping();
		pong2.setValue1((short)0);
		pong2.setValue2(1);
		channel.write(pong2);
	}
	
	public void onStreamBytesRead(RTMPConnection conn, Channel channel, PacketHeader source, StreamBytesRead streamBytesRead){
		log.info("Stream Bytes Read: "+streamBytesRead.getBytesRead());
		// pass to stream handler
	}
	
}