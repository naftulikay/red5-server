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

import org.apache.mina.core.buffer.IoBuffer;
import org.red5.server.api.scheduling.IScheduledJob;
import org.red5.server.api.scheduling.ISchedulingService;
import org.red5.server.plugin.icy.IFlowControl;
import org.red5.server.plugin.icy.IICYHandler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles the main parsing work.
 * 
 * @author Paul Gregoire (mondain@gmail.com)
 * @author Andy Shaules (bowljoman@hotmail.com)
 */
public class NSVThread implements Runnable {

	public static int SERVER_MODE = 0;

	public static int CLIENT_MODE = 1;

	private int _mode = 0;

	private InputStream m_input = null;

	private URL u;

	private boolean connected = false;

	private int frames = -1;

	public Map<String, Object> metaData = new HashMap<String, Object>();

	private URLConnection uc;

	public ServerSocket outSock;

	private IICYHandler handler;

	private IFlowControl idler;

	private boolean get_frame = true;

	private boolean got_frame = false;

	private int old_bytes = 0;

	private int last_read;

	private int[] prev_bits;

	private long lastData = 0;

	private String host = "";

	private int port = 8001;

	public Socket client;

	private boolean verified = false;

	private boolean initiated = false;

	private String password = "changeme";

	private NSVSenderThread sender;

	public NSVStreamConfig config;

	private long dataTimeout = 10000;//milliseconds

	@SuppressWarnings("unused")
	private String audioType;

	/**
	 * 
	 * @param mode
	 * @param p_host
	 * @param val
	 * @param senderThread
	 */
	public NSVThread(int mode, String p_host, IICYHandler val, IFlowControl pIdler, NSVSenderThread senderThread) {

		idler = pIdler;
		host = p_host;
		_mode = mode;
		handler = val;
		sender = senderThread;
	}

	/**
	 * 
	 * @param mode
	 * @param val
	 * @param senderThread
	 */
	public NSVThread(int mode, IICYHandler val, NSVSenderThread senderThread) {
		handler = val;
		sender = senderThread;
		_mode = mode;

	}

	/**
	 * Returns the ServerTypes value of current running status.
	 * @return
	 */
	public int getMode() {
		return _mode;
	}

	public int getFrames() {
		return frames;
	}

	public void listen() {

		initiated = false;
		verified = false;
		connected = false;
		metaData.clear();
		switch (_mode) {

			case 1:
				try {
					// client mode;						
					verified = true;
					//u = new URL("http://192.168.2.62:8000/;stream.nsv");
					u = new URL(host);
					uc = u.openConnection();
					uc.connect();
					m_input = uc.getInputStream();
					lastData = System.currentTimeMillis();
					//connected=true;

				} catch (IOException er0) {
				}

				break;
			case 0:
				try {

					//  server mode;	
					outSock = new ServerSocket(port);
					client = outSock.accept();
					m_input = client.getInputStream();
					lastData = System.currentTimeMillis();
					connected = true;
					outSock.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
		}

	}

	public void setPort(int val) {
		port = val;
	}

	public void setPassword(String val) {
		password = val;
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
		if (m_input == null) {
			return;
		} else {
			try {
				idler.notifyIdler(m_input.available());
			} catch (IOException e) {
				m_input = null;
				old_bytes = 0;
				prev_bits = new int[0];
				listen();
			}
		}

		try {
			if (m_input.available() > 0) {
				lastData = System.currentTimeMillis();
			} else {
				if (System.currentTimeMillis() - lastData > dataTimeout) {
					connected = false;
					m_input.close();
					old_bytes = 0;
					prev_bits = new int[0];
					listen();
				}
			}
		} catch (IOException e) {
			old_bytes = 0;
			prev_bits = new int[0];
			listen();
		}

		//send frames
		sender.execute(service);

		last_read = 0;

		try {

			int[] bits;
			int offset = 0;//=prev_bits.length;
			if (old_bytes > 0) {
				offset = prev_bits.length;

				bits = new int[offset + m_input.available()];
				for (int j = 0; j < offset; j++) {
					bits[j] = prev_bits[j];
				}

				old_bytes = 0;
			} else {
				if (m_input.available() < 1)
					return;
				bits = new int[m_input.available()];
			}
			//Password
			if (!verified) {
				if (m_input.available() < password.length()) {
					return;
				}
				for (int m = offset; m < bits.length; m++) {
					bits[m] = (m_input.read());
				}
				if (sample(password.length(), bits).equals(password)) {
					verified = true;
					client.getOutputStream().write("OK2\r\nicy-caps:11\r\n\r\n".getBytes());
					client.getOutputStream().flush();

				} else {
					client.getOutputStream().write("invalid password\r\n".getBytes());
					client.getOutputStream().flush();
					client.close();
					m_input.close();
					this.connected = false;

					return;

				}
			} else
				//store chunk
				for (int m = offset; m < bits.length; m++) {
					bits[m] = (m_input.read());
				}

			while (get_frame) {
				for (int h = 0; h < bits.length; h++) {
					int limit = bits.length;
					if (h < bits.length - 4) {
						if (got_frame && (_mode == 0 || _mode == 1)) {
							if ((char) bits[h] == 0xef) {
								if ((char) bits[h + 1] == 0xbe) {

									int enough = h;
									enough = chnkFrame(enough, bits);
									if (enough < 0) {
										continue;
									}
									if (enough == h) {
										save(bits.length - h, h, bits);
										//sender.execute(service);
										return;
									} else {
										h = enough;
										last_read = bits.length - h;
										//	System.out.println("bytes left "+last_read);
										if (last_read == 0) {
											//sender.execute(service);
											return;
										}
									}
									//Adjust for next parser.
									h--;
								}
							}
							if (limit < h + 4) {
								save(bits.length - h, h, bits);
								return;
							}
						}
						//*******************************************************************************
						if (initiated && (_mode == 0 || _mode == 1)) {
							if (((bits[h]) | ((bits[h + 1]) << 8) | ((bits[h + 2]) << 16) | ((bits[h + 3]) << 24)) == NSVStream.NSV_SYNC_DWORD) {

								int was_enough = syncFrame(h, bits);
								if (was_enough < 0)//invalid
									continue;

								if (was_enough == h) {
									save(bits.length - h, h, bits);

									return;
								} else {
									h = was_enough;
									last_read = bits.length - h;

									if (last_read == 0) {
										return;
									}
								}

								h--;
							}
							if (limit < h + 2) {
								save(bits.length - h, h, bits);
								return;
							}
						}
						if ((char) bits[h] == 'i' && (char) bits[h + 1] == 'c' && (char) bits[h + 2] == 'y') {
							if (limit < h + 1024) {
								save(bits.length - h, h, bits);
								return;
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
						if ((char) bits[h] == 'c' && (char) bits[h + 1] == 'o' && (char) bits[h + 2] == 'n'
								&& (char) bits[h + 3] == 't') {

							char[] chars = new char[36];
							for (int j = 0; j < 36; j++) {
								if ((char) bits[h + j] == '\r' || (char) bits[h + j] == '\n') {
									//last_read=j;
									//h=j;
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
							if (_mode == 3 || _mode == 2) {
								if (type[0].equals("video")) {
									_mode = (_mode == 3) ? 0 : 1;
								} else {
									audioType = type[1];
								}
							} else {
								if (type[0].equals("audio")) {
									if (_mode == 0)
										_mode = 3;
									else
										_mode = 2;

									audioType = type[1];
								}
							}
							connected = true;
							//Notify handler of new content.
							handler.reset(type[0], type[1]);
						}

						//Audio only
						if (_mode == 3 && initiated) {
							handler.onAudioData(bits);
							return;
						}

						//Audio only
						if (_mode == 2 && initiated) {
							handler.onAudioData(bits);
							return;
						}

					} else {
						//Not enough to parse.
						save(last_read, bits.length - last_read, bits);
						break;
					}
				}

				break;
			}

		} catch (IOException er0) {
		}

	}

	/**
	 * Called when sync frame header is found.
	 * 
	 * @param poffset current offset in data array.
	 * @param data contains nsv bitstream.
	 * @return position in data array or < 0 on in invalid frame. Returns poffset if valid frame but needs more data.
	 */
	private int syncFrame(int poffset, int[] data) {
		int offset = poffset;
		int mark = offset;
		int total_aux_used = 0;
		int limit = data.length - offset;
		if (limit < 24) {
			return offset;
		}
		offset += 4;//NSVs;
		if (!got_frame) { //First frame with full data.
			String p_Vidtype = String.valueOf((char) data[offset++]) + String.valueOf((char) data[offset++])
					+ String.valueOf((char) data[offset++]) + String.valueOf((char) data[offset++]);
			String p_Audtype = String.valueOf((char) data[offset++]) + String.valueOf((char) data[offset++])
					+ String.valueOf((char) data[offset++]) + String.valueOf((char) data[offset++]);

			int p_width = data[offset++] | data[offset++] << 8;
			int p_height = data[offset++] | data[offset++] << 8;
			int frame_rate_encoded = data[offset++];
			double p_framerate = NSVStream.framerateToDouble(frame_rate_encoded);
			config = NSVStream.create(p_Vidtype, p_Audtype, p_width, p_height, p_framerate);
			config.frameRateEncoded = frame_rate_encoded;
			sender.config = config;
			handler.onConnected(p_Vidtype, p_Audtype);
			//Upside down format. Send negative values?
			//TODO use standard codec meta tags.
			metaData.put("width", config.videoWidth * -1);
			metaData.put("height", config.videoHeight * -1);
			metaData.put("frameRate", config.frameRate);
			metaData.put("videoCodec", config.videoFormat);
			metaData.put("audioCodec", config.audioFormat);

			handler.onMetaData(metaData);
			got_frame = true;
		} else {
			offset += 4;//vid
			offset += 4;//aud
			offset += 2;//width ;
			offset += 2;//height ;
			offset += 1;//framerate ;
		}
		
		NSVFrame frame = new NSVFrame(config, NSVStream.NSV_SYNC_DWORD);

		frame.frame_number = frames++;

		frame.offset_current = data[offset++] | data[offset++] << 8;
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
	 * @param poffset current offset in data array.
	 * @param data contains nsv bitstream.
	 * @return position in data array or < 0 on in invalid frame. Returns poffset if valid frame but needs more data.
	 */
	private int chnkFrame(int poffset, int[] data) {
		int offset = poffset;

		int total_aux_used = 0;
		int limit = data.length - poffset;
		if (limit < 7) {
			return poffset;
		}
		offset++;//0xbeef;
		offset++;
		NSVFrame frame = new NSVFrame(config, NSVStream.NSV_NONSYNC_WORD);//NSVStream.stream(config,NSVStream.NSV_NONSYNC_WORD);
		frame.frame_number = frames++;
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

		int bytesNeeded = (7) + (vid_len) + (aud_len);
		if (limit < bytesNeeded) {
			return poffset;
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
		old_bytes = num;
		this.prev_bits = new int[old_bytes];
		for (int i = 0; i < num; i++) {
			prev_bits[i] = pbits[i + offSet];
		}
	}

	public boolean isConnected() {
		return connected;
	}

	public void setHost(String val) {
		host = val;
	}
}
