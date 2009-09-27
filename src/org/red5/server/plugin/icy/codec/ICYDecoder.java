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

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.red5.server.plugin.icy.StreamManager;
import org.red5.server.plugin.icy.parser.NSVBitStream;
import org.red5.server.plugin.icy.parser.NSVFrame;
import org.red5.server.plugin.icy.parser.NSVStream;
import org.red5.server.plugin.icy.parser.NSVStreamConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decoder for data coming from a source.
 * 
 * @author Paul Gregoire (mondain@gmail.com)
 * @author Andy Shaules (bowljoman@hotmail.com)
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
	
	private static final Pattern PATTERN_CRLF = Pattern.compile("[\\r|\\n|\u0085|\u2028]{1,2}");

	private static final Pattern PATTERN_HEADER = Pattern.compile("(icy-|content-).{1,}[:]{1}.{1,}", Pattern.DOTALL);
	
	private ThreadLocal<NSVFrame> frameLocal = new ThreadLocal<NSVFrame>();
	
	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		String hex = in.getHexDump();
		log.debug("doDecode dump: {}", hex);
		
		boolean result = false;
		
		//check state
		ReadState state = (ReadState) session.getAttribute("state");
		if (state == null) {
			state = ReadState.Notvalidated;
		}

		//collect the current incoming bytes
		IoBuffer curBuffer = IoBuffer.wrap(in.buf());

		//index of the line-feed in the byte stream
		int lfIndex = -1;
		
		//get any previously read / unused bytes and put them in the front
		byte[] prevBuffer = (byte[]) session.removeAttribute("prev");
		if (prevBuffer != null) {
			//wrap previous bytes
			IoBuffer tmp = IoBuffer.wrap(prevBuffer);
			tmp.setAutoExpand(true);
			//jump to the end
			tmp.position(tmp.limit());
			//add current buffer to the end
			tmp.put(curBuffer);
			//flip
			tmp.flip();
			//free the current buffer we are replacing
			curBuffer.free();
			//replace with our new buffer
			curBuffer = tmp;
			log.debug("Current buffer dump (prep): {}", curBuffer.getHexDump());
		}
		
		//most common action should live at the top of the switch!
		switch (state) {
			case Packet:
				//do normal media packet handling here
				log.trace("Hex (pkt): {}", curBuffer.getHexDump());

				//debugging
				out.write(Boolean.FALSE);
				
				break;			
			case Ready:
				//look for NSV prefix
				log.trace("Hex (rdy): {}", curBuffer.getHexDump());
				
				//do nsv handling		
				//4E 53 56 73
				//consume bytes
				byte[] nsv = new byte[4];
				curBuffer.get(nsv);
				log.trace("NSV: {}", new String(nsv));
				int nsvSync = (nsv[0] | (nsv[1] << 8) | (nsv[2] << 16) | (nsv[3] << 24));
				log.trace("Sync: {} Dword: {}", nsvSync, NSVStream.NSV_SYNC_DWORD);
				if (nsvSync == NSVStream.NSV_SYNC_DWORD) {
					//read all the configuration info for the stream
					int synced = syncFrame(session, curBuffer);
					if (synced > 0) {

					}
					
					NSVFrame frame = frameLocal.get();
					if (frame != null) {
    					//write frame as decoder output
    					out.write(frame);
    					//clear thread local
    					frameLocal.remove();
					} else {
						//let the handler know it should handle the "ready" stuff
						out.write(Boolean.TRUE);
					}
					
				} else {
					//rewind it back 4 
					curBuffer.position(curBuffer.position() - 4);
					//let the handler know it should handle the "ready" stuff
					out.write(Boolean.FALSE);
				}
				
				//drop any remaining current buffer data into the session
				if (curBuffer.hasRemaining()) {
					log.debug("Had left over bytes after sync, adding to session");
					//get the buffer info
					int pos = curBuffer.position();
					int len = curBuffer.limit();
					log.trace("Current pos: {} len: {} size: {}", new Object[]{pos, len, (len - pos)});
					//consume bytes
					byte[] bf = new byte[(len - pos)];
					curBuffer.get(bf);
					//put bytes into the session
					session.setAttribute("prev", bf);
				}	
				
				break;
			case Notvalidated: 
				//need to check password
				lfIndex = curBuffer.indexOf((byte) 0x0a);
				if (lfIndex > 0) {
					//get data as a string
					byte[] buf = new byte[lfIndex + 1];
					curBuffer.get(buf);
					String msg = new String(buf, "US-ASCII");
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
						//check for remaining data
						if (curBuffer.hasRemaining()) {
							log.debug("Had left over bytes, adding to session");
							//get the buffer info
							int pos = curBuffer.position();
							int len = curBuffer.limit();
							log.trace("Current pos: {} len: {} size: {}", new Object[]{pos, len, (len - pos)});
							//consume bytes
							byte[] bf = new byte[(len - pos)];
							curBuffer.get(bf);
							//put bytes into the session
							session.setAttribute("prev", bf);			
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
				//max bytes to read when looking for LF
				int maxSearchBytes = 128;
				//search count
				int searched = 0;
				//buffer for storing bytes which may be headers
				IoBuffer headerBuf = IoBuffer.allocate(maxSearchBytes);
				//nio bytebuffer seems to work better for locating LF
				ByteBuffer byteBuffer = curBuffer.buf();
				while (true) {
					//read a byte
					byte b = byteBuffer.get();
					searched++;
					log.trace("Byte: {} searched: {}", b, searched);
					//make sure we dont search too far
					if (searched > maxSearchBytes) {
						log.debug("Searched past maximum range");						
						//add to prev bytes (outside this loop)
						break;
					}
					//store our byte for header checking
					headerBuf.put(b);
					//LF found
					if (b == 0x0a) {
						//flip so we can do some reading
    					headerBuf.flip();
    					//get the buffer info
    					log.trace("Current pos: {} len: {}", curBuffer.position(), curBuffer.limit());
    					log.trace("ByteBuffer pos: {} len: {}", byteBuffer.position(), byteBuffer.limit());
    					log.trace("Hex: {}", curBuffer.getHexDump());
    
    					//get data as a string
    					byte[] buf = null;
    					
    					//look for the end of the header marker
    					hex = headerBuf.getHexDump();
						log.debug("Buffer head1: {}", hex);
    					state = detectEndOfHeader(hex, state);
    					if (state != ReadState.Ready) {
    						//size our array
    						buf = new byte[headerBuf.limit()];
    						//consume bytes
    						headerBuf.get(buf);
    						log.debug("Buffer head2: {}", headerBuf.getHexDump());
    						String header = new String(buf, "US-ASCII");					
    						log.debug("Message {}", header);
    						//pull out the headers and put into meta data
    						parseHeader(session, header);			
    						//set result to true
    						result = true;
    						//check if theres remaining data to parse in current buffer
    						if (curBuffer.hasRemaining()) {
    							//clear already read data
    							headerBuf.clear();    							
    						} else {
    							//no more to read for now
    							break;
    						}
    					} else if (state == ReadState.Ready) {
    						log.debug("End of header found during header parse");
    						//consume the EOH bytes
    						buf = hex.startsWith("0D 0A") ? new byte[2] : new byte[1];
    						headerBuf.get(buf);
    						//set result to true just in-case no headers were read
    						result = true;
    						//drop any remaining current buffer data into the session
    						if (curBuffer.hasRemaining()) {
        						log.debug("Had left over bytes after EOH, adding to session");
        						//get the buffer info
        						int pos = curBuffer.position();
        						int len = curBuffer.limit();
        						log.trace("Current pos: {} len: {} size: {}", new Object[]{pos, len, (len - pos)});
        						//consume bytes
        						byte[] bf = new byte[(len - pos)];
        						curBuffer.get(bf);
        						//put bytes into the session
        						session.setAttribute("prev", bf);
    						}
    						//exit header read loop
    						break;
    					}
    					//reset searched counter
    					searched = 0;
    				}					
				}
				
				if (headerBuf.hasRemaining()) {
					log.debug("Had left over bytes, adding to session");
					//get the buffer info
					int pos = headerBuf.position();
					int len = headerBuf.limit();
					log.trace("Header buffer pos: {} len: {} size: {}", new Object[]{pos, len, (len - pos)});
					//consume bytes
					byte[] bf = new byte[(len - pos)];
					headerBuf.get(bf);
					//put bytes into the session
					session.setAttribute("prev", bf);
				}
				
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
		if (hex.indexOf("0A") == 0 || hex.indexOf("0D 0A") == 0) {
			log.debug("End of header detected");
			//set to ready state
			state = ReadState.Ready;
		} else if (hex.indexOf("4E 53 56") == 0) {
			//check also for NSV
			log.debug("End of header detected, found NSV");
			//set to ready state
			state = ReadState.Ready;
		}
		return state;
	}

	@SuppressWarnings("unchecked")
	private void parseHeader(IoSession session, String header) {
		//lookup the metadata in the session
		Map<String, Object> metaData = (Map<String, Object>) session.getAttribute("meta");
		if (metaData == null) {
			metaData = new HashMap<String, Object>();
			session.setAttribute("meta", metaData);
		}
		//log.trace("Header length: {}", header.length());
		if (header.length() > 0 && PATTERN_HEADER.matcher(header).matches()) {
			String key = header.substring(header.indexOf('-') + 1, header.indexOf(':'));
			String value = header.substring(header.indexOf(':') + 1);
			log.debug("Meta: {}={}", key, value);
			metaData.put(key, value.trim());
		} else {
			//ignore 0 length headers 
			if (header.length() > 0) {
				log.debug("Unrecognized header: {}", header);			
			}
		}
	}	
	
	@SuppressWarnings({ "unchecked", "unused" })
	private void parseHeaders(IoSession session, String[] headers) {
		//lookup the metadata in the session
		Map<String, Object> metaData = (Map<String, Object>) session.getAttribute("meta");
		if (metaData == null) {
			metaData = new HashMap<String, Object>();
			session.setAttribute("meta", metaData);
		}
		for (String header : headers) {
			parseHeader(session, header);
		}
	}	
    
	/**
	 * Called when sync frame header is found.
	 * 
	 * @param session
	 * @param ioBuffer contains nsv bitstream
	 * @return position in data array or < 0 on in invalid frame. Returns offset if valid frame but needs more data.
	 */
	private int syncFrame(IoSession session, IoBuffer ioBuffer) {
		int limit = ioBuffer.limit();
		//we need at least 20 bytes
		if (limit < 20) {
			return limit;
		}
		//first frame with full data
		byte[] fourBytes = new byte[4];
		ioBuffer.get(fourBytes);
		String videoType = new String(fourBytes);
		ioBuffer.get(fourBytes);
		String audioType = new String(fourBytes);
		log.debug("Types - video: {} audio: {}", videoType, audioType);
		
		byte[] twoBytes = new byte[2];
		ioBuffer.get(twoBytes);
		int width = (twoBytes[0] & 0xff) | ((twoBytes[1] & 0xff) << 8);
		ioBuffer.get(twoBytes);
		int height = (twoBytes[0] & 0xff) | ((twoBytes[1] & 0xff) << 8);
		int frameRateEncoded = ioBuffer.get();
		
		double frameRate = NSVStream.framerateToDouble(frameRateEncoded);
		log.debug("Width: {} Height: {} Framerate: {}", new Object[]{width, height, frameRate});
		
		NSVStreamConfig config = StreamManager.createStreamConfig(videoType, audioType, width, height, frameRate);
		config.frameRateEncoded = frameRateEncoded;

		//add stream config to the session
		session.setAttribute("nsvconfig", config);
		
		NSVFrame frame = new NSVFrame(config, NSVStream.NSV_SYNC_DWORD);
		ioBuffer.get(twoBytes);
		frame.offsetCurrent = (twoBytes[0] & 0xff) | ((twoBytes[1] & 0xff) << 8);
		log.trace("av sync {}", frame.offsetCurrent);

		NSVBitStream bs0 = new NSVBitStream();
		bs0.putBits(8, ioBuffer.get());
		bs0.putBits(8, ioBuffer.get());
		bs0.putBits(8, ioBuffer.get());
		
		int numAux = bs0.getbits(4);
		int vidLen = bs0.getbits(20);

		bs0.putBits(8, ioBuffer.get());
		bs0.putBits(8, ioBuffer.get());
		int audLen = bs0.getbits(16);

		if (vidLen > NSVStream.NSV_MAX_VIDEO_LEN / 8 || audLen > NSVStream.NSV_MAX_AUDIO_LEN / 8) {
			return -1;
		}

		int bytesNeeded = 24 + (vidLen + audLen);
		if (limit < bytesNeeded) {
			return limit;
		}
		
		int totalAuxUsed = 0;
		
		Map<String, IoBuffer> aux = null;
		
		if (numAux > 0) {
			log.debug("Number of aux: {}", numAux);
			aux = new HashMap<String, IoBuffer>(numAux);
			for (int a = 0; a < numAux; a++) {
				ioBuffer.get(twoBytes);
				int auxLen = (twoBytes[0] & 0xff) | ((twoBytes[1] & 0xff) << 8);
				totalAuxUsed += auxLen + 6;
				ioBuffer.get(fourBytes);
				String auxType = new String(fourBytes);
				log.debug("Aux type: {}", auxType);
				IoBuffer buffer = IoBuffer.allocate(auxLen);
				byte[] auxBytes = new byte[auxLen];
				ioBuffer.get(auxBytes);
				buffer.put(auxBytes);
				buffer.flip();
				buffer.position(0);
				//add to the vector
				aux.put(auxType, buffer);
			}
			session.setAttribute("aux", aux);
		}

		frame.videoLength = vidLen;
		frame.videoData = new byte[vidLen - totalAuxUsed];
		ioBuffer.get(frame.videoData);
		
		frame.audioLength = audLen;
		frame.audioData = new byte[audLen];		
		ioBuffer.get(frame.audioData);
		
		frameLocal.set(frame);

		return 0;
	}	
	
	/**
	 * Called when chunk frame header is found.
	 * 
	 * @param session
	 * @param ioBuffer contains nsv bitstream
	 * @return position in data array or < 0 on in invalid frame. Returns offset if valid frame but needs more data.
	 */
	private int chnkFrame(IoSession session, IoBuffer ioBuffer) {
		/*
		int limit = data.length - offset;
		if (limit < 7) {
			return offset;
		}
		
		offset++;//0xbeef;
		offset++;
		
		NSVFrame frame = new NSVFrame(config, NSVStream.NSV_NONSYNC_WORD);

		NSVBitStream bs0 = new NSVBitStream();

		bs0.putBits(8, data[offset++]);
		bs0.putBits(8, data[offset++]);
		bs0.putBits(8, data[offset++]);
		int num_aux = bs0.getbits(4);
		int vid_len = bs0.getbits(20);

		bs0.putBits(8, data[offset++]);
		bs0.putBits(8, data[offset++]);
		int aud_len = bs0.getbits(16);

		if (vid_len > NSVStream.NSV_MAX_VIDEO_LEN / 8) {
			return -1;
		}
		if (aud_len > NSVStream.NSV_MAX_AUDIO_LEN / 8) {
			return -1;
		}

		int bytesNeeded = 7 + vid_len + aud_len;
		if (limit < bytesNeeded) {
			return offset;
		}
		frame.vid_len = vid_len;
		frame.aud_len = aud_len;
		frame.vid_data = new int[vid_len];
		frame.aud_data = new int[aud_len];
		
		int total_aux_used = 0;
		
		if (num_aux > 0) {
			for (int a = 0; a < num_aux; a++) {
				int aux_len = (byte) data[offset++] | (byte) data[offset++] << 8;
				total_aux_used += aux_len + 6;
				String aux_type = String.valueOf((char) data[offset++]) + String.valueOf((char) data[offset++])
						+ String.valueOf((char) data[offset++]) + String.valueOf((char) data[offset++]);
				IoBuffer buffer = IoBuffer.allocate(aux_len);
				for (int b = 0; b < aux_len; b++) {
					buffer.put((byte) data[offset++]);
				}
				buffer.flip();
				buffer.position(0);
				handler.onAuxData(aux_type, buffer);
			}
		}
		for (int vids = 0; vids < vid_len - total_aux_used; vids++) {
			frame.vid_data[vids] = data[offset++];
		}

		for (int auds = 0; auds < aud_len; auds++) {
			frame.aud_data[auds] = data[offset++];
		}

		config.writeFrame(frame);
		
		*/
		return 0;
	}	
	
}