package org.red5.server.plugin.icy.parser;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.red5.logging.Red5LoggerFactory;
import org.red5.server.plugin.icy.IFlowControl;
import org.red5.server.plugin.icy.IICYHandler;
import org.red5.server.plugin.icy.StreamManager;
import org.slf4j.Logger;

/**
 * Handles the main parsing work.
 * 
 * @author Paul Gregoire (mondain@gmail.com)
 * @author Andy Shaules (bowljoman@hotmail.com)
 */
public class NSVThread implements Runnable, IFlowControl {

	private static Logger log = Red5LoggerFactory.getLogger(NSVThread.class, "plugins");
	
	public static final int SERVER_MODE = 0;

	public static final int CLIENT_MODE = 1;

	private int mode = 0;

	private InputStream input;

	private URL u;

	private boolean connected;

	public Map<String, Object> metaData = new HashMap<String, Object>();

	private URLConnection uc;

	public ServerSocket outSock;

	private IICYHandler handler;
	
	private boolean keepRunning = true;

	private boolean getFrame = true;

	private boolean gotFrame;

	private int oldBytes = 0;

	private int lastRead;

	private int[] prevBits;

	private long lastData = 0;

	private String host = "";

	private int port = 8001;

	public Socket client;

	private boolean verified;

	private boolean initiated;

	private String password = "changeme";

	private NSVSenderThread sender;

	public NSVStreamConfig config;

	//thread sleep period
	private int waitTime = 50;
	
	//data timeout in milliseconds
	private long dataTimeout = 10000;

	@SuppressWarnings("unused")
	private String audioType;

	//determines how to notify players that the video is upside down
	private boolean notifyFlipped;
	
	/**
	 * 
	 * @param mode
	 * @param val
	 * @param senderThread
	 */
	public NSVThread(int mode, IICYHandler handler, NSVSenderThread senderThread) {
		this.mode = mode;
		this.handler = handler;
		sender = senderThread;
	}
	
	/**
	 * 
	 * @param mode
	 * @param p_host
	 * @param val
	 * @param senderThread
	 */
	public NSVThread(int mode, String host, IICYHandler handler, NSVSenderThread senderThread) {
		this.mode = mode;
		this.host = host;
		this.handler = handler;
		sender = senderThread;
	}

	/**
	 * Returns the ServerTypes value of current running status.
	 * @return
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * Returns the number of frames created / written
	 * @return
	 */
	public int getFrames() {
		int frames = -1;
		if (config != null) {
			frames = Long.valueOf(config.totalFrames.get()).intValue();
		}
		return frames;
	}

	public void listen() {
		initiated = false;
		verified = false;
		connected = false;
		metaData.clear();
		switch (mode) {
			case 1:
				try {
					// client mode
					verified = true;
					//u = new URL("http://192.168.2.62:8000/;stream.nsv");
					u = new URL(host);
					uc = u.openConnection();
					uc.connect();
					input = uc.getInputStream();
					lastData = System.currentTimeMillis();
				} catch (IOException er0) {
				}
				break;
			
			case 0:
				try {
					//  server mode;	
					outSock = new ServerSocket(port);
					client = outSock.accept();
					input = client.getInputStream();
					lastData = System.currentTimeMillis();
					connected = true;
					outSock.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
				
			default:
				log.debug("Unhandled mode: {}", mode);
		}

	}

	public void setPort(int val) {
		port = val;
	}

	public void setPassword(String val) {
		password = val;
	}

	public int getWaitTime() {
		return waitTime;
	}

	public void setWaitTime(int waitTime) {
		this.waitTime = waitTime;
	}

	private String sample(int passWordLength, int[] buffer) {
		String password = "";
		for (int g = 0; g < passWordLength; g++) {
			password = password.concat(String.valueOf((char) buffer[g]));
		}
		return password;
	}

	@Override
	public void run() {
		//initialize the inputs
		listen();
		//if no input, no need to continue
		if (input == null) {
			log.warn("No input available");
			return;
		}
		//start the loop
		while (keepRunning) {
			try {
				if (input.available() > 0) {
					lastData = System.currentTimeMillis();
				} else {
					long delta = System.currentTimeMillis() - lastData;
					if (delta > dataTimeout) {
						log.debug("Data too late exit time: {} > timeout: {}", delta, dataTimeout);
						//disconnect if late?
						connected = false;
						reset();
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			lastRead = 0;

			try {
				int[] bits;
				int offset = 0;
				if (oldBytes > 0) {
					offset = prevBits.length;
					bits = new int[offset + input.available()];
					for (int j = 0; j < offset; j++) {
						bits[j] = prevBits[j];
					}

					oldBytes = 0;
				} else {
					if (input.available() > 0) {
						bits = new int[input.available()];						
					} else {
						continue;
					}
				}
				//Password
				if (!verified) {
					log.debug("Not verified, check password");
					if (input.available() < password.length()) {
						log.debug("Bytes not long enough to match against password");
					}
					for (int m = offset; m < bits.length; m++) {
						bits[m] = input.read();
					}
					OutputStream os = client.getOutputStream();
					if (sample(password.length(), bits).equals(password)) {
						verified = true;
						os.write("OK2\r\nicy-caps:11\r\n\r\n".getBytes());
						os.flush();
					} else {
						log.debug("Invalid password, reset and close");
						os.write("invalid password\r\n".getBytes());
						os.flush();
						reset();
						break;
					}
				} else {
					//store chunk
					for (int m = offset; m < bits.length; m++) {
						bits[m] = input.read();
					}
				}
				log.debug("Entering getFrame loop");
				while (getFrame) {
					for (int h = 0; h < bits.length; h++) {
						int limit = bits.length;
						if (h < bits.length - 4) {
							if (gotFrame && (mode == 0 || mode == 1)) {
								if ((char) bits[h] == 0xef) {
									if ((char) bits[h + 1] == 0xbe) {
										int enough = h;
										enough = chnkFrame(enough, bits);
										if (enough < 0) {
											continue;
										}
										if (enough == h) {
											save(bits.length - h, h, bits);
											break;
										} else {
											h = enough;
											lastRead = bits.length - h;
											//	System.out.println("bytes left "+last_read);
											if (lastRead == 0) {
												break;
											}
										}
										//Adjust for next parser.
										h--;
									}
								}
								if (limit < h + 4) {
									save(bits.length - h, h, bits);
									break;
								}
							}
							//*******************************************************************************
							if (initiated && (mode == 0 || mode == 1)) {
								if (((bits[h]) | ((bits[h + 1]) << 8) | ((bits[h + 2]) << 16) | ((bits[h + 3]) << 24)) == NSVStream.NSV_SYNC_DWORD) {
									int was_enough = syncFrame(h, bits);
									if (was_enough > 0) {
	    								if (was_enough == h) {
	    									save(bits.length - h, h, bits);    
	    									break;
	    								} else {
	    									h = was_enough;
	    									lastRead = bits.length - h;
	    									if (lastRead == 0) {
	    										break;
	    									}
	    								}
	    
	    								h--;
									}
								}
								if (limit < h + 2) {
									save(bits.length - h, h, bits);
									break;
								}
							}
							if ((char) bits[h] == 'i' && (char) bits[h + 1] == 'c' && (char) bits[h + 2] == 'y') {
								if (limit < h + 1024) {
									save(bits.length - h, h, bits);
									break;
								}

								char[] chars = new char[1024];
								for (int j = 0; j < 1024; j++) {
									if ((char) bits[h + j] == '\r' || (char) bits[h + j] == '\n') {
										break;
									}
									chars[j] = (char) bits[h + j];
								}

								String meta = new String(chars);
								String[] item = meta.split("cy-", 2);
								String[] value = item[1].split(":", 2);
								item = item[1].split(":", 2);
								metaData.put(item[0], value[1]);

								if (initiated) {
									handler.onMetaData(metaData);
								}
							}

							//*******************************************************************************
							if ((char) bits[h] == 'c' && (char) bits[h + 1] == 'o' && (char) bits[h + 2] == 'n'	&& (char) bits[h + 3] == 't') {

								char[] chars = new char[36];
								for (int j = 0; j < 36; j++) {
									if ((char) bits[h + j] == '\r' || (char) bits[h + j] == '\n') {
										break;
									}
									chars[j] = (char) bits[h + j];
								}

								String meta = new String(chars);
								String[] value = meta.split(":", 2);
								String[] type = value[1].split("/", 2);
								if (!initiated) {
									initiated = true;
								}
								//Switch mode if wrong content
								if (mode == 3 || mode == 2) {
									if (type[0].equals("video")) {
										mode = (mode == 3) ? 0 : 1;
									} else {
										audioType = type[1];
									}
								} else {
									if (type[0].equals("audio")) {
										if (mode == 0) {
											mode = 3;
										} else {
											mode = 2;
										}
										audioType = type[1];
									}
								}
								connected = true;
								//Notify handler of new content.
								handler.reset(type[0], type[1]);
							}

							//Audio only
							if (initiated && (mode == 2 || mode == 3)) {
								handler.onAudioData(bits);
							}
						} else {
							//Not enough to parse.
							save(lastRead, bits.length - lastRead, bits);
						}
						
						break;
					}
					
					break;
				}

			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
			//sleep for a few ticks
			try {
				log.trace("Sleep for {}ms", waitTime);
				Thread.sleep(waitTime);
			} catch (Exception e) {
			}			
		}
		log.debug("Exiting run block");
	}
	
	private void reset() {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		input = null;
		if (client != null) {
			try {
				client.close();
				connected = false;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		oldBytes = 0;
		prevBits = new int[0];
	}

	public void stop() {
		keepRunning = false;
		//reset everything
		reset();
		//flushing the config will also cause the sender thread
		//to stop, so its win-win
		config.flush();
	}
	
	/**
	 * Called when sync frame header is found.
	 * 
	 * @param offset current offset in data array.
	 * @param data contains nsv bitstream.
	 * @return position in data array or < 0 on in invalid frame. Returns offset if valid frame but needs more data.
	 */
	private int syncFrame(int offset, int[] data) {
		int mark = offset;
		int total_aux_used = 0;
		int limit = data.length - offset;
		if (limit < 24) {
			return offset;
		}
		offset += 4;//NSVs;
		if (!gotFrame) { 
			//First frame with full data.
			String vidtype = String.valueOf((char) data[offset++]) + String.valueOf((char) data[offset++])
					+ String.valueOf((char) data[offset++]) + String.valueOf((char) data[offset++]);
			String audtype = String.valueOf((char) data[offset++]) + String.valueOf((char) data[offset++])
					+ String.valueOf((char) data[offset++]) + String.valueOf((char) data[offset++]);

			int width = data[offset++] | data[offset++] << 8;
			int height = data[offset++] | data[offset++] << 8;
			int frameRateEncoded = data[offset++];
			
			double frameRate = NSVStream.framerateToDouble(frameRateEncoded);

			config = StreamManager.createStreamConfig(vidtype, audtype, width, height, frameRate);
			config.frameRateEncoded = frameRateEncoded;
			
			sender.config = config;
			
			//now that the sender has a config, submit it for execution
			StreamManager.submit(sender);
			
			handler.onConnected(vidtype, audtype);

			//TODO use standard codec meta tags.
			
			//upside down format. Send negative values?
			if (notifyFlipped) {
				metaData.put("width", config.videoWidth);
				metaData.put("height", config.videoHeight);
				metaData.put("flipped", "true");
			} else {
				metaData.put("width", config.videoWidth * -1);
				metaData.put("height", config.videoHeight * -1);			
			}
			metaData.put("frameRate", config.frameRate);
			metaData.put("videoCodec", config.videoFormat);
			metaData.put("audioCodec", config.audioFormat);

			handler.onMetaData(metaData);
			gotFrame = true;
		} else {
			offset += 4;//vid
			offset += 4;//aud
			offset += 2;//width ;
			offset += 2;//height ;
			offset += 1;//framerate ;
		}
		
		NSVFrame frame = new NSVFrame(config, NSVStream.NSV_SYNC_DWORD);
		frame.offsetCurrent = data[offset++] | data[offset++] << 8;
		//	System.out.println("av sync "+frame.offset_current);

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

		int bytesNeeded = (24) + (vid_len) + (aud_len);
		if (limit < bytesNeeded) {
			return mark;
		}

		frame.vid_len = vid_len;
		frame.aud_len = aud_len;
		frame.vid_data = new int[vid_len];
		frame.aud_data = new int[aud_len];

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
		
		return offset;
	}

	/**
	 * Called when chunk frame header is found.
	 * 
	 * @param offset current offset in data array.
	 * @param data contains nsv bitstream.
	 * @return position in data array or < 0 on in invalid frame. Returns offset if valid frame but needs more data.
	 */
	private int chnkFrame(int offset, int[] data) {
		int total_aux_used = 0;
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
		return offset;
	}

	private void save(int num, int offSet, int[] pbits) {
		oldBytes = num;
		this.prevBits = new int[oldBytes];
		for (int i = 0; i < num; i++) {
			prevBits[i] = pbits[i + offSet];
		}
	}
	
	@Override
	public void notifyIdler(int stat) {
		if (stat == 0) {
			waitTime = 100;
		} else {
			waitTime = 1;
		}
		waitTime = (waitTime < 1) ? 1 : waitTime;
		waitTime = (waitTime > 300) ? 300 : waitTime;
	}
	
	public boolean isConnected() {
		return connected;
	}

	public void setHost(String val) {
		host = val;
	}

	public boolean isNotifyFlipped() {
		return notifyFlipped;
	}

	public void setNotifyFlipped(boolean notifyFlipped) {
		this.notifyFlipped = notifyFlipped;
	}
	
}
