package org.red5.server.plugin;

/*
 * RED5 Open Source Flash Server - http://www.osflash.org/red5
 * 
 * Copyright (c) 2006-2009 by respective authors (see below). All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * Foundation; either version 2.1 of the License, or (at your option) any later 
 * version. 
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along 
 * with this library; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

import java.net.URI;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.red5.io.amf.AMF;
import org.red5.io.amf.Output;
import org.red5.io.object.Serializer;
import org.red5.logging.Red5LoggerFactory;
import org.red5.server.adapter.ApplicationLifecycle;
import org.red5.server.api.IConnection;
import org.red5.server.net.rtmp.BaseRTMPHandler;
import org.red5.server.net.rtmp.RTMPConnection;
import org.red5.server.net.rtmp.event.IRTMPEvent;
import org.red5.server.net.rtmp.event.Notify;
import org.red5.server.net.rtmp.message.Header;
import org.red5.server.net.rtmp.message.Packet;
import org.red5.server.net.rtmp.status.StatusCodes;
import org.red5.server.net.rtmp.status.StatusObject;
import org.slf4j.Logger;

import com.kennardconsulting.core.net.UrlEncodedQueryString;

/**
 * Provides FMS-style authentication using an application listener.
 * 
 * @author Paul Gregoire
 * @author Dan Rossi
 */
public class FMSAuthenticationHandler extends ApplicationLifecycle {

	private static Logger log = Red5LoggerFactory.getLogger(FMSAuthenticationHandler.class, "plugins");

	private static Serializer serializer = new Serializer();
	
	private static StatusObject rejectMissingAuth;

	static {
		rejectMissingAuth = new StatusObject(StatusCodes.NC_CONNECT_REJECTED, StatusObject.ERROR,
				"[ code=403 .need auth; authmod=adobe ]");
	}

	public boolean appConnect(IConnection conn, Object[] params) {

		boolean result = false;
		
		StatusObject status = null;
		
		Map<String, Object> connectionParams = conn.getConnectParams();
		
		if (!connectionParams.containsKey("queryString")) {
			//set as missing auth notification
			status = rejectMissingAuth;
		} else {
			//get the raw query string
    		String rawQueryString = (String) connectionParams.get("queryString");
    		try {
    			//convert to uri
    			URI uri = new URI(rawQueryString);
    			//parse into a usable query string
    			UrlEncodedQueryString queryString = UrlEncodedQueryString.parse(uri);
    			
    			//get the values we want
    			String user = queryString.get("user");
    			log.debug("User: {}", user);
    			
    			String authmod = queryString.get("authmod");    			
    			log.debug("Authmod: {}", authmod);
    			
    			String response = queryString.get("response");
    			log.debug("Response: {}", response);
    			/*
    			 * Base64 base64 = new Base64(); byte[] salt =
    			 * base64.decode("salt=0xkAAA==&challenge=sTQAAA==&opaque=sTQAAA="
    			 * );
    			 * 
    			 * System.out.println(salt.toString());
    			 * challenge=khcAAA==&response
    			 * =Qp0GSBumMwziL6I6y3iZaQ==&opaque=ExcAAA==.
    			 * 
    			 * danielr:thechallenge:theresponse
    			 */
    			if (authmod != null && user != null && response == null) {
    				//set as rejected
    				status = new StatusObject(StatusCodes.NC_CONNECT_REJECTED, StatusObject.ERROR, 
    						String.format("[ AccessManager.Reject ] : [ authmod=%s ] : ?reason=needauth&user=%s&salt=0xkAAA==&challenge=sTQAAA==&opaque=sTQAAA=", authmod, user));
    			} else {
    				//set to success
    				status = new StatusObject(StatusCodes.NC_CONNECT_SUCCESS, StatusObject.STATUS, "Connection succeeded.");
    				result = true;
    			}
    		} catch (Exception e) {
    			log.error("Error authenticating", e);
    		}
		}
		
		//status.setAdditional("secureToken", "testing secure token status property from RED5 !!!");
		
		//send the status object
		log.debug("Status: {}", status);
		FMSAuthenticationHandler.writeStatus(conn, status);
		
		return result;
	}
	
	/**
	 * Invokes the "onStatus" event on the client, passing our derived status.
	 * 
	 * @param conn
	 * @param status
	 */
	public static void writeStatus(IConnection conn, StatusObject status) {
		//make a buffer to put our data in
		IoBuffer buf = IoBuffer.allocate(128);
		buf.setAutoExpand(true);
		//create amf output
		Output out = new Output(buf);
		//mark it as an amf object
		buf.put(AMF.TYPE_OBJECT);
		//serialize our status
    	status.serialize(out, serializer);
    	//write trailer
		buf.put((byte) 0x00);
		buf.put((byte) 0x00);
		buf.put(AMF.TYPE_END_OF_OBJECT);
		//make the buffer read to be read
		buf.flip();
		
		//create an RTMP event of Notify type
		IRTMPEvent event = new Notify(buf);

		//construct a packet
		Header header = new Header();
		Packet packet = new Packet(header, event);

		//get our stream id
		int streamId = BaseRTMPHandler.getStreamId();
		//set channel to "data" which im pretty sure is 3
		header.setChannelId(3);
		header.setTimer(event.getTimestamp()); //0
		header.setStreamId(streamId);
		header.setDataType(event.getDataType());
		
		//write to the client
		((RTMPConnection) conn).write(packet);
	}
	
}
