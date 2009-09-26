package org.red5.server.plugin.icy.codec;

/*
 * RED5 Open Source Flash Server - http://www.osflash.org/red5
 * 
 * Copyright (c) 2006-2008 by respective authors (see below). All rights reserved.
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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decoder for data coming from a source.
 * 
 * @author Paul Gregoire
 */
public class ICYDecoder extends CumulativeProtocolDecoder {

	/**
	 * State enumerator that indicates the reached state in the message
	 * decoding process.
	 */
	public enum ReadState {
		/** Unrecoverable error occurred */
		Failed,
		/** Trying to resync */
		Sync,
		/** Waiting for a command */
		Ready,
		/** Reading interleaved packet */
		Packet,
		/** Not validated (password not yet checked) */
		Notvalidated,
		/** Reading headers */
		Header
	}

	private static Logger log = LoggerFactory.getLogger(ICYDecoder.class);

	private static final byte[] OK_MESSAGE = "OK2\r\nicy-caps:11\r\n\r\n".getBytes();
	
	private static final byte[] BAD_PASSWD_MESSAGE = "invalid password\r\n".getBytes();
	
	private static final Pattern PATTERN_CRLF = Pattern.compile("[\\r\\n|\\n]",	Pattern.CANON_EQ);

	private static final Pattern PATTERN_HEADER = Pattern.compile("(icy-|content-).*");
	
	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		String hex = in.getHexDump();
		log.debug("Dump: {}", hex);
		
		boolean result = false;
		
		//check state
		ReadState state = (ReadState) session.getAttribute("state");
		if (state == null) {
			state = ReadState.Notvalidated;
		}
				
		switch (state) {
			case Notvalidated: 
				//need to check password
				if (in.indexOf((byte) 0x0a) != -1) {
					//get data as a string
					byte[] buf = new byte[in.limit()];
					in.get(buf);
					String msg = new String(buf);
					log.debug("Not validated, check password {}", msg);
					//pull password from session
					String password = (String) session.getAttribute("password");
					log.debug("Password from session: {}", password);
					String[] arr = null;
					try {
						arr = PATTERN_CRLF.split(msg);
						log.debug("Password data count: {}", arr.length);
					} catch (PatternSyntaxException ex) {
						log.warn("", ex);
					}
					if (password.equals(arr[0])) {
						log.debug("Passwords match!");
						state = ReadState.Header;
						out.write(OK_MESSAGE);
						//if password array is longer than one then parse it for headers
						if (arr.length > 1) {
    						parseHeaders(session, arr);
    						//look for the end of the header marker (blank line)
    						state = detectEndOfHeader(hex, state);
						}
					} else {
						log.info("Invalid password {}, reset and close", arr[0]);
						state = ReadState.Failed;
						out.write(BAD_PASSWD_MESSAGE);
					}					
					//
					result = true;
				}
				break;
			case Header:
				//we expect to get headers here
				if (in.indexOf((byte) 0x0a) != -1) {
					//get data as a string
					byte[] buf = new byte[in.limit()];
					in.get(buf);
					String msg = new String(buf);					
					log.debug("Message {}", msg);
					//get the headers
					String[] headers = null;
					try {
						headers = PATTERN_CRLF.split(msg);
						log.debug("Header count: {}", headers.length);
					} catch (PatternSyntaxException ex) {
						log.warn("", ex);
					}
					//pull out the headers and put into meta data
					parseHeaders(session, headers);

					//look for the end of the header marker (blank line)
					state = detectEndOfHeader(hex, state);
					
					//
					result = true;
				}
				break;
			case Ready:
				//indicate that we are handling media data now
				state = ReadState.Packet;
				
				break;
			case Failed:
				log.info("Stream error, closing");
        		session.close(true);
        		break;
			default:
				log.warn("Unhandled state: {}", state);
		}
		
		// save attributes in session
		session.setAttribute("state", state);

		return result;
	}

	/**
	 * Look for the end of the header block. This is different depending on the
	 * client.
	 * <br />
	 * Winamp dsp sends (0A 0A)
	 * <br />
	 * NSVCAP sends (0D 0D 0A 0D 0D 0A 0D 0A)
	 * <br />
	 * 
	 * @param hex
	 * @param state
	 * @return
	 */
	private ReadState detectEndOfHeader(String hex, ReadState state) {
		if (hex.indexOf("0A 0A") > 0 || hex.indexOf("0D 0D 0A 0D 0D 0A 0D 0A") > 0) {
			log.debug("End of header detected");
			state = ReadState.Ready;
		}
		return state;
	}

	@SuppressWarnings("unchecked")
	private void parseHeaders(IoSession session, String[] headers) {
		for (String header : headers) {
			log.trace("Header length: {}", header.length());
			if (header.length() == 0) {
				continue;
			}
			if (PATTERN_HEADER.matcher(header).matches()) {
				String key = header.substring(header.indexOf('-') + 1, header.indexOf(':'));
				String value = header.substring(header.indexOf(':') + 1);
				log.debug("Meta: {}={}", key, value);
				//lookup the metadata in the session
				Map<String, Object> metaData = (Map<String, Object>) session.getAttribute("meta");
				if (metaData == null) {
					metaData = new HashMap<String, Object>();
					session.setAttribute("meta", metaData);
				}
				metaData.put(key, value);
			} else {
				log.info("Unrecognized header: {}", header);
			}
		}
	}	
    
}