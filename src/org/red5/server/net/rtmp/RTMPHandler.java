package org.red5.server.net.rtmp;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.ByteBuffer;
import org.red5.server.api.ISharedObject;
import org.red5.server.api.Red5;
import org.red5.server.context.AppContext;
import org.red5.server.context.BaseApplication;
import org.red5.server.context.Scope;
import org.red5.server.net.BaseHandler;
import org.red5.server.net.HostNotFoundException;
import org.red5.server.net.ProtocolState;
import org.red5.server.net.rtmp.codec.RTMP;
import org.red5.server.net.rtmp.message.Constants;
import org.red5.server.net.rtmp.message.InPacket;
import org.red5.server.net.rtmp.message.Invoke;
import org.red5.server.net.rtmp.message.Message;
import org.red5.server.net.rtmp.message.OutPacket;
import org.red5.server.net.rtmp.message.PacketHeader;
import org.red5.server.net.rtmp.message.Ping;
import org.red5.server.net.rtmp.message.SharedObject;
import org.red5.server.net.rtmp.message.SharedObjectEvent;
import org.red5.server.net.rtmp.message.StreamBytesRead;
import org.red5.server.net.rtmp.message.Unknown;
import org.red5.server.net.rtmp.status.StatusObjectService;
import org.red5.server.service.Call;
import org.red5.server.stream.Stream;

/*
 * Network library independent RTMP handler.
 * 
 * Inherit from this class and overwrite the two "write" methods to support
 * any network library.
 */
public class RTMPHandler extends BaseHandler implements Constants {
	protected static Log log =
        LogFactory.getLog(RTMPHandler.class.getName());
	
	public StatusObjectService statusObjectService = null;

	public void setStatusObjectService(StatusObjectService statusObjectService) {
		this.statusObjectService = statusObjectService;
	}
	
	public void messageReceived(RTMPConnection conn, ProtocolState state, Object in) throws Exception {
		
		if(in instanceof ByteBuffer){
			rawBufferRecieved(conn, state, (ByteBuffer) in);
			return;
		}
		
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
				
			// Is this a bad for performance ?
			Red5.setConnectionLocal(conn);
			Scope.setChannel(channel);
			Scope.setStream(stream);
			Scope.setStatusObjectService(statusObjectService);
			
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
				onSharedObject(conn, channel, source, so);
				break;
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
		invokeCall(conn, new Call("disconnect"));
		if (log.isDebugEnabled())
			log.debug("Connection closed: " + conn);
	}
	
	protected void onSharedObject(RTMPConnection conn, Channel channel, PacketHeader  source, SharedObject request) {
		AppContext ctx = conn.getAppContext();
		BaseApplication app = (BaseApplication) ctx.getBean(AppContext.APP_SERVICE_NAME);
		String name = request.getName();
		
		log.debug("Received SO request from " + channel + "(" + request + ")");
		ISharedObject so = app.getSharedObject(name, request.isPersistent());
		
		so.beginUpdate();
		Iterator it = request.getEvents().iterator();
		while (it.hasNext()) {
			SharedObjectEvent event = (SharedObjectEvent) it.next();
			
			switch (event.getType())
			{
			case SO_CONNECT:
				// Register client for this shared object and send initial state
				so.register(conn);
				break;
			
			case SO_CLEAR:
				// Clear the shared object
				if (!request.isPersistent())
					so.unregister(conn);
				
				/* XXX: should we really clear the SO here?  I think this is rather
				 *      a "disconnect" - the same event is sent for the "clear" method
				 *      as well as the disconnect of a client.
				 */
				so.removeAttributes();
				break;
			
			case SO_SET_ATTRIBUTE:
				// The client wants to update an attribute
				so.setAttribute(event.getKey(), event.getValue());
				break;
			
			case SO_DELETE_ATTRIBUTE:
				// The client wants to remove an attribute
				so.removeAttribute(event.getKey());
				break;
				
			case SO_SEND_MESSAGE:
				// The client wants to send a message
				so.sendMessage(event.getKey(), (List) event.getValue());
				break;
				
			default:
				log.error("Unknown shared object update event " + event.getType());
			}
		}
		so.endUpdate();
	}
	
	private void rawBufferRecieved(RTMPConnection conn, ProtocolState state, ByteBuffer in) {
		
		final RTMP rtmp = (RTMP) state;
		
		if(rtmp.getState() != RTMP.STATE_HANDSHAKE){
			log.warn("Raw buffer after handshake, something odd going on");
		}
		
		ByteBuffer out = ByteBuffer.allocate((Constants.HANDSHAKE_SIZE*2)+1);
		
		if(log.isDebugEnabled()){
			log.debug("Writing handshake reply");
			log.debug("handskake size:"+in.remaining());
		}
		
		out.put((byte)0x03);
		out.fill((byte)0x00,Constants.HANDSHAKE_SIZE);
		out.put(in).flip();
		conn.dispatchEvent(out);
	}

	public void invokeCall(RTMPConnection conn, Call call){
		
		if(call.getServiceName()==null){
			call.setServiceName(AppContext.APP_SERVICE_NAME);
		} 
		
		serviceInvoker.invoke(call, conn.getAppContext());
		
	}
	
	// ------------------------------------------------------------------------------
	
	public void onInvoke(RTMPConnection conn, Channel channel, PacketHeader source, Invoke invoke){
		
		log.debug("Invoke");
		
		Map params = invoke.getConnectionParams();
		if (params != null) {
			log.debug("Setting connection params: "+params);
			conn.setParameters(params);
			log.debug("Setting application context");
			conn.setAppContext(lookupAppContext(conn));
			
			final String hostname = getHostname((String) params.get("tcUrl"));
			try {
				conn.setClient(newClient(hostname));
			} catch (HostNotFoundException e) {
				log.error("Could not create client for host " + hostname, e);
			}
		}
		
		final Call call = invoke.getCall();
		
		invokeCall(conn,call);
		
		if(invoke.isAndReturn()){
		
			if(call.getStatus() == Call.STATUS_SUCCESS_VOID ||
				call.getStatus() == Call.STATUS_SUCCESS_NULL ){
				log.debug("Method does not have return value, do not reply");
				return;
			}
			Invoke reply = new Invoke();
			reply.setCall(call);
			reply.setInvokeId(invoke.getInvokeId());
			channel.write(reply);
		}
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
		// TODO: pass this to streaming code
		// can be used to work out client bandwidth
	}
	
	//	 ---------------------------------------------------------------------------
}
