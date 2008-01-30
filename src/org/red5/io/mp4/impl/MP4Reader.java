package org.red5.io.mp4.impl;

/*
 * RED5 Open Source Flash Server - http://www.osflash.org/red5
 * 
 * Copyright (c) 2006-2007 by respective authors (see below). All rights reserved.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.mina.common.ByteBuffer;
import org.red5.io.BufferType;
import org.red5.io.IKeyFrameMetaCache;
import org.red5.io.IStreamableFile;
import org.red5.io.ITag;
import org.red5.io.ITagReader;
import org.red5.io.IoConstants;
import org.red5.io.amf.Output;
import org.red5.io.flv.IKeyFrameDataAnalyzer;
import org.red5.io.flv.impl.Tag;
import org.red5.io.mp4.MP4Atom;
import org.red5.io.mp4.MP4DataStream;
import org.red5.io.mp4.MP4Descriptor;
import org.red5.io.mp4.MP4Header;
import org.red5.io.object.Serializer;
import org.red5.io.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Reader is used to read the contents of a MP4 file.
 * NOTE: This class is not implemented as threading-safe. The caller
 * should make sure the threading-safety.
 * <p>
 * New NetStream notifications
 * <br />
 * Two new notifications facilitate the implementation of the playback components:
 * <ul>
 * <li>NetStream.Play.FileStructureInvalid: This event is sent if the player detects an MP4 with an invalid file structure. Flash Player cannot play files that have invalid file structures.</li>
 * <li>NetStream.Play.NoSupportedTrackFound: This event is sent if the player does not detect any supported tracks. If there aren't any supported video, audio or data tracks found, Flash Player does not play the file.</li>
 * </ul>
 * </p>
 * 
 * @author The Red5 Project (red5@osflash.org)
 * @author Paul Gregoire, (mondain@gmail.com)
 */
public class MP4Reader implements IoConstants, ITagReader,
		IKeyFrameDataAnalyzer {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(MP4Reader.class);

    /**
     * File
     */
    private File file;
    
    /**
     * Input stream
     */
    private MP4DataStream fis;

    /**
     * File channel
     */
    private FileChannel channel;
    /**
     * Keyframe metadata
     */
	private KeyFrameMeta keyframeMeta;

    /**
     * Input byte buffer
     */
    private ByteBuffer in;

	/** Set to true to generate metadata automatically before the first tag. */
	private boolean generateMetadata;

	/** Position of first video tag. */
	private long firstVideoTag = -1;

	/** Position of first audio tag. */
	private long firstAudioTag = -1;

	/** Current tag. */
	private int tagPosition;

	/** Duration in milliseconds. */
	private long duration;

	/** Mapping between file position and timestamp in ms. */
	private HashMap<Long, Long> posTimeMap;

	/** Mapping between file position and tag number. */
	private HashMap<Long, Integer> posTagMap;

	/** Buffer type / style to use **/
	private static BufferType bufferType = BufferType.AUTO;

	private static int bufferSize = 1024;
	
	/** Use load buffer */
	private boolean useLoadBuf;

	/** Cache for keyframe informations. */
	private static IKeyFrameMetaCache keyframeCache;
	
	/** The header of this MP4 file. */
	private MP4Header header;
	
	/** Whether or not the clip contains a video track */
	private boolean hasVideo = false;
	
	//
	private String videoCodecId = "avc1";
	private String audioCodecId = "mp4a";
	
	private int width;
	private int height;
	private int audioSampleRate;
	private int audioChannels;
	
    /**
     * File metadata
     */
	private ITag fileMeta;
	
	/** Constructs a new MP4Reader. */
	MP4Reader() {
	}

    /**
     * Creates MP4 reader from file input stream.
	 *
     * @param f         File
     */
    public MP4Reader(File f) throws IOException {
		this(f, false);
	}

    /**
     * Creates MP4 reader from file input stream, sets up metadata generation flag.
	 *
     * @param f                    File input stream
     * @param generateMetadata     <code>true</code> if metadata generation required, <code>false</code> otherwise
     */
    public MP4Reader(File f, boolean generateMetadata) throws IOException {
    	if (null == f) {
    		log.warn("Reader was passed a null file");
        	log.debug("{}", ToStringBuilder.reflectionToString(this));
    	}
    	this.file = f;
		this.fis = new MP4DataStream(new FileInputStream(f));
		this.generateMetadata = generateMetadata;
		channel = fis.getChannel();
		
		try {
			// the first atom will/should be the type
			MP4Atom type = MP4Atom.createAtom(fis);
			// expect ftyp
			log.debug("Type {}", MP4Atom.intToType(type.getType()));
			log.debug("type children: {}", type.getChildren());
			log.debug("{}", ToStringBuilder.reflectionToString(type));

			MP4Atom moov = MP4Atom.createAtom(fis);
			// expect moov
			log.debug("Type {}", MP4Atom.intToType(moov.getType()));
			log.debug("moov children: {}", moov.getChildren());			

			MP4Atom mvhd = moov.lookup(MP4Atom.typeToInt("mvhd"), 0);
			if (mvhd != null) {
				log.debug("Movie header atom found");
				log.debug("Time scale {}", mvhd.getTimeScale());
				log.debug("Duration {}", mvhd.getDuration());
				duration = mvhd.getDuration() / mvhd.getTimeScale();
				
			}

			MP4Atom meta = moov.lookup(MP4Atom.typeToInt("meta"), 0);
			if (meta != null) {
				log.debug("Meta atom found");
				log.debug("{}", ToStringBuilder.reflectionToString(meta));
			}
			
			int i = 0;
			while (i < 2) {

				MP4Atom trak = moov.lookup(MP4Atom.typeToInt("trak"), i);
				if (trak != null) {
					log.debug("Track atom found");
					log.debug("trak children: {}", trak.getChildren());	
					// trak: tkhd, edts, mdia
					MP4Atom tkhd = trak.lookup(MP4Atom.typeToInt("tkhd"), 0);
					if (tkhd != null) {
						log.debug("Track header atom found");
						log.debug("tkhd children: {}", tkhd.getChildren());	
						if (tkhd.getWidth() > 0) {
							width = tkhd.getWidth();
							height = tkhd.getHeight();
							log.debug("Width {} x Height {}", width, height);
						}
					}

					MP4Atom edts = trak.lookup(MP4Atom.typeToInt("edts"), 0);
					if (edts != null) {
						log.debug("Edit atom found");
						log.debug("edts children: {}", edts.getChildren());	
						//log.debug("Width {} x Height {}", edts.getWidth(), edts.getHeight());
					}					
					
					MP4Atom mdia = trak.lookup(MP4Atom.typeToInt("mdia"), 0);
					if (mdia != null) {
						log.debug("Media atom found");
						// mdia: mdhd, hdlr, minf
						MP4Atom hdlr = mdia
								.lookup(MP4Atom.typeToInt("hdlr"), 0);
						if (hdlr != null) {
							log.debug("Handler ref atom found");
							// soun or vide
							log.debug("Handler type: {}", MP4Atom
									.intToType(hdlr.getHandlerType()));
							if (MP4Atom.intToType(hdlr.getHandlerType()) == "vide") {
								hasVideo = true;
							}
							i++;
						}

						MP4Atom minf = mdia
								.lookup(MP4Atom.typeToInt("minf"), 0);
						if (minf != null) {
							log.debug("Media info atom found");
							// minf: (audio) smhd, dinf, stbl / (video) vmhd,
							// dinf, stbl

							MP4Atom smhd = minf.lookup(MP4Atom
									.typeToInt("smhd"), 0);
							if (smhd != null) {
								log.debug("Sound header atom found");
								MP4Atom dinf = minf.lookup(MP4Atom
										.typeToInt("dinf"), 0);
								if (dinf != null) {
									log.debug("Data info atom found");
									// dinf: dref
									log.debug("Sound dinf children: {}", dinf
											.getChildren());
									MP4Atom dref = dinf.lookup(MP4Atom
											.typeToInt("dref"), 0);
									if (dref != null) {
										log.debug("Data reference atom found");
									}

								}
								MP4Atom stbl = minf.lookup(MP4Atom
										.typeToInt("stbl"), 0);
								if (stbl != null) {
									log.debug("Sample table atom found");
									// stbl: stsd, stts, stss, stsc, stsz, stco,
									// stsh
									log.debug("Sound stbl children: {}", stbl
											.getChildren());
									// stsd - sample description
									// stts - time to sample
									// stsc - sample to chunk
									// stsz - sample size
									// stco - chunk offset

									//stsd - has codec child
									MP4Atom stsd = stbl.lookup(MP4Atom.typeToInt("stsd"), 0);
									if (stsd != null) {
										//stsd: mp4a
										log.debug("Sample description atom found");
										MP4Atom mp4a = stsd.getChildren().get(0);
										//could set the audio codec here
										setAudioCodecId(MP4Atom.intToType(mp4a.getType()));
										//log.debug("{}", ToStringBuilder.reflectionToString(mp4a));
										log.debug("Sample size: {}", mp4a.getSampleSize());										
										audioSampleRate = mp4a.getTimeScale();
										audioChannels = mp4a.getChannelCount();
										log.debug("Sample rate: {}", audioSampleRate);			
										log.debug("Channels: {}", audioChannels);										
										//mp4a: esds
										if (mp4a.getChildren().size() > 0) {
											log.debug("Elementary stream descriptor atom found");
											MP4Atom esds = mp4a.getChildren().get(0);
											log.debug("{}", ToStringBuilder.reflectionToString(esds));
											MP4Descriptor descriptor = esds.getEsd_descriptor();
											//log.debug("{}", ToStringBuilder.reflectionToString(descriptor));
											if (descriptor != null) {
    											Vector children = descriptor.getChildren();
    											for (int e = 0; e < children.size(); e++) { 
    												MP4Descriptor descr = (MP4Descriptor) children.get(e);
    												log.debug("{}", ToStringBuilder.reflectionToString(descr));
    												if (descr.getChildren().size() > 0) {
    													Vector children2 = descr.getChildren();
    													for (int e2 = 0; e2 < children2.size(); e2++) { 
    														MP4Descriptor descr2 = (MP4Descriptor) children2.get(e2);
    														log.debug("{}", ToStringBuilder.reflectionToString(descr2));														
    													}													
    												}
    											}
											}
										}
									}
									//stsc - has Records
									MP4Atom stsc = stbl.lookup(MP4Atom.typeToInt("stsc"), 0);
									if (stsc != null) {
										log.debug("Sample to chunk atom found");
										Vector records = stsc.getRecords();
										log.debug("Record count: {}", records.size());
										MP4Atom.Record rec = (MP4Atom.Record) records.firstElement();
										log.debug("Record data: Description index={} Samples per chunk={}", rec.getSampleDescriptionIndex(), rec.getSamplesPerChunk());
									}									
									//stsz - has Samples
									MP4Atom stsz = stbl.lookup(MP4Atom.typeToInt("stsz"), 0);
									if (stsz != null) {
										log.debug("Sample size atom found");
										Vector samples = stsz.getSamples();
										//vector full of integers										
										log.debug("Sample size: {}", stsz.getSampleSize());
										log.debug("Sample count: {}", samples.size());
									}
									//stco - has Chunks
									MP4Atom stco = stbl.lookup(MP4Atom.typeToInt("stco"), 0);
									if (stco != null) {
										log.debug("Chunk offset atom found");
										//vector full of integers
										Vector chunks = stco.getChunks();
										log.debug("Chunk count: {}", chunks.size());
									}
									
									//for (MP4Atom child : stbl.getChildren()) {
									//	log.debug("{}", MP4Atom.intToType(child.getType()));
									//	log.debug("{}", ToStringBuilder.reflectionToString(child));
									//}

								}
							}
							MP4Atom vmhd = minf.lookup(MP4Atom
									.typeToInt("vmhd"), 0);
							if (vmhd != null) {
								log.debug("Video header atom found");
								MP4Atom dinf = minf.lookup(MP4Atom
										.typeToInt("dinf"), 0);
								if (dinf != null) {
									log.debug("Data info atom found");
									// dinf: dref
									log.debug("Video dinf children: {}", dinf
											.getChildren());
									MP4Atom dref = dinf.lookup(MP4Atom
											.typeToInt("dref"), 0);
									if (dref != null) {
										log.debug("Data reference atom found");
									}
								}
								MP4Atom stbl = minf.lookup(MP4Atom
										.typeToInt("stbl"), 0);
								if (stbl != null) {
									log.debug("Sample table atom found");
									// stbl: stsd, stts, stss, stsc, stsz, stco,
									// stsh
									log.debug("Video stbl children: {}", stbl
											.getChildren());
									// stsd - sample description
									// stts - (decoding) time to sample
									// stsc - sample to chunk
									// stsz - sample size
									// stco - chunk offset
									// ctts - (composition) time to sample
									// stss - sync sample
									// sdtp - independent and disposible samples

									//stsd - has codec child
									MP4Atom stsd = stbl.lookup(MP4Atom.typeToInt("stsd"), 0);
									if (stsd != null) {
										log.debug("Sample description atom found");
										
										MP4Atom avc1 = stsd.getChildren().get(0);
										//could set the video codec here
										setVideoCodecId(MP4Atom.intToType(avc1.getType()));
										log.debug("{}", ToStringBuilder.reflectionToString(avc1));
									}
									//stsc - has Records
									MP4Atom stsc = stbl.lookup(MP4Atom.typeToInt("stsc"), 0);
									if (stsc != null) {
										log.debug("Sample to chunk atom found");
										Vector records = stsc.getRecords();
										log.debug("Record count: {}", records.size());
										MP4Atom.Record rec = (MP4Atom.Record) records.firstElement();
										log.debug("Record data: Description index={} Samples per chunk={}", rec.getSampleDescriptionIndex(), rec.getSamplesPerChunk());
									}									
									//stsz - has Samples
									MP4Atom stsz = stbl.lookup(MP4Atom.typeToInt("stsz"), 0);
									if (stsz != null) {
										log.debug("Sample size atom found");
										Vector samples = stsz.getSamples();
										//vector full of integers										
										log.debug("Sample size: {}", stsz.getSampleSize());
										log.debug("Sample count: {}", samples.size());
									}
									//stco - has Chunks
									MP4Atom stco = stbl.lookup(MP4Atom.typeToInt("stco"), 0);
									if (stco != null) {
										log.debug("Chunk offset atom found");
										//vector full of integers
										Vector chunks = stco.getChunks();
										log.debug("Chunk count: {}", chunks.size());
									}									
									
									//for (MP4Atom child : stbl.getChildren()) {
									//	log.debug("{}", MP4Atom.intToType(child.getType()));
									//	log.debug("{}", ToStringBuilder.reflectionToString(child));
									//}

								}
							}

						}

					}
				}
			}
				
			MP4Atom mdat = null;
			do {
				mdat = MP4Atom.createAtom(fis);
    			if (mdat != null && mdat.getType() == MP4Atom.typeToInt("mdat")) {
    				log.debug("Movie data atom found");
    				log.debug("{}", ToStringBuilder.reflectionToString(mdat));    				
    			} else {
    				log.debug("{} atom found", MP4Atom.intToType(mdat.getType()));
    			}
			} while (mdat.getType() != MP4Atom.typeToInt("mdat"));
				
/*
				List<MP4Atom> children = moov.getChildren();
				for (int v = 0; v < children.size(); v++) {
					MP4Atom at = (MP4Atom) children.get(v);
					log.debug("Type {}", MP4Atom.intToType(at.getType()));
					if (at.getType() == MP4Atom.typeToInt("mvhd")) {
						log.debug("----------- Got movie header");
						log.debug("Children {}", at.getChildren());
					} else if (at.getType() == MP4Atom.typeToInt("trak")) {
						log.debug("----------- Got track");
						log.debug("Children {}", at.getChildren());

						List<MP4Atom> children2 = at.getChildren();
						for (int v2 = 0; v2 < children2.size(); v2++) {
							MP4Atom at2 = (MP4Atom) children2.get(v2);
							log.debug("Type {}", MP4Atom.intToType(at2.getType()));
							//tkhd, edts, mdia
							log.debug("Children 2 {}", at2.getChildren());
							
							if (at2.getType() == MP4Atom.typeToInt("mdia")) {
								log.debug("----------- Got Media");
								List<MP4Atom> children3 = at2.getChildren();
								for (int v3 = 0; v3 < children3.size(); v3++) {
									MP4Atom at3 = (MP4Atom) children3.get(v3);
									log.debug("Type {}", MP4Atom.intToType(at3.getType()));

									//mdhd, hdlr, minf
									log.debug("Children 3 {}", at3.getChildren());
									//smhd, dinf, stbl
									//vmhd, dinf, stbl
									
									if (at3.getType() == MP4Atom.typeToInt("minf")) {
										log.debug("----------- Got Media Information");									
									}
								}
							}
						}
					} 
				}
*/
				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        // Create file metadata object
        fileMeta = createFileMeta();		
				
		in = null;
		//fillBuffer();

		//postInitialize();
	}

    /**
	 * Accepts mapped file bytes to construct internal members.
	 *
	 * @param generateMetadata         <code>true</code> if metadata generation required, <code>false</code> otherwise
     * @param buffer                   Byte buffer
	 */
	public MP4Reader(ByteBuffer buffer, boolean generateMetadata) {
		this.generateMetadata = generateMetadata;
		in = buffer;

		postInitialize();
	}
    
    public void setKeyFrameCache(IKeyFrameMetaCache keyframeCache) {
    	MP4Reader.keyframeCache = keyframeCache;
    }

    /**
	 * Get the remaining bytes that could be read from a file or ByteBuffer.
	 *
	 * @return          Number of remaining bytes
	 */
	private long getRemainingBytes() {
		if (!useLoadBuf) {
			return in.remaining();
		}

		try {
			return channel.size() - channel.position() + in.remaining();
		} catch (Exception e) {
			log.error("Error getRemainingBytes", e);
			return 0;
		}
	}

	/**
	 * Get the total readable bytes in a file or ByteBuffer.
	 *
	 * @return          Total readable bytes
	 */
	private long getTotalBytes() {
		if (!useLoadBuf) {
			return in.capacity();
		}

		try {
			return channel.size();
		} catch (Exception e) {
			log.error("Error getTotalBytes", e);
			return 0;
		}
	}

	/**
	 * Get the current position in a file or ByteBuffer.
	 *
	 * @return           Current position in a file
	 */
	private long getCurrentPosition() {
		long pos;

		if (!useLoadBuf) {
			return in.position();
		}

		try {
			if (in != null) {
				pos = (channel.position() - in.remaining());
			} else {
				pos = channel.position();
			}
			return pos;
		} catch (Exception e) {
			log.error("Error getCurrentPosition", e);
			return 0;
		}
	}

	/**
     * Modifies current position.
     *
     * @param pos  Current position in file
     */
    private void setCurrentPosition(long pos) {
		if (pos == Long.MAX_VALUE) {
			pos = file.length();
		}
		if (!useLoadBuf) {
			in.position((int) pos);
			return;
		}

		try {
			if (pos >= (channel.position() - in.limit())
					&& pos < channel.position()) {
				in.position((int) (pos - (channel.position() - in.limit())));
			} else {
				channel.position(pos);
				fillBuffer(bufferSize, true);
			}
		} catch (Exception e) {
			log.error("Error setCurrentPosition", e);
		}

	}

    /**
     * Loads whole buffer from file channel, with no reloading (that is, appending).
     */
    private void fillBuffer() {
		fillBuffer(bufferSize, false);
	}

	/**
	 * Loads data from channel to buffer.
	 *
	 * @param amount         Amount of data to load with no reloading
	 */
	private void fillBuffer(long amount) {
		fillBuffer(amount, false);
	}

	/**
	 * Load enough bytes from channel to buffer.
	 * After the loading process, the caller can make sure the amount
	 * in buffer is of size 'amount' if we haven't reached the end of channel.
	 *
	 * @param amount The amount of bytes in buffer after returning,
	 * no larger than bufferSize
	 * @param reload Whether to reload or append
	 */
	private void fillBuffer(long amount, boolean reload) {
		try {
			if (amount > bufferSize) {
				amount = bufferSize;
			}
			// Read all remaining bytes if the requested amount reach the end
			// of channel.
			if (channel.size() - channel.position() < amount) {
				amount = channel.size() - channel.position();
			}

			if (in == null) {
				switch (bufferType) {
					case HEAP:
						in = ByteBuffer.allocate(bufferSize, false);
						break;
					case DIRECT:
						in = ByteBuffer.allocate(bufferSize, true);
						break;
					case AUTO:
						in = ByteBuffer.allocate(bufferSize);
						break;
					default:
						in = ByteBuffer.allocate(bufferSize);
				}
				channel.read(in.buf());
				in.flip();
				useLoadBuf = true;
			}

			if (!useLoadBuf) {
				return;
			}

			if (reload || in.remaining() < amount) {
				if (!reload) {
					in.compact();
				} else {
					in.clear();
				}
				channel.read(in.buf());
				in.flip();
			}

		} catch (Exception e) {
			log.error("Error fillBuffer", e);
		}
	}

    /**
     * Post-initialization hook, reads keyframe metadata and decodes header (if any).
     */
    private void postInitialize() {
		ITag tag = null;

		if (log.isDebugEnabled()) {
			log.debug("MP4Reader 1 - Buffer size: " + getTotalBytes()
					+ " position: " + getCurrentPosition() + " remaining: "
					+ getRemainingBytes());
		}
		if (getRemainingBytes() >= 9) {
			decodeHeader();
		}
		keyframeMeta = analyzeKeyFrames();
		long old = getCurrentPosition();
		
		

	}
	
    /** {@inheritDoc} */
    public boolean hasVideo() {
    	return hasVideo;
    }

	/**
     * Getter for buffer type (auto, direct or heap).
     *
     * @return Value for property 'bufferType'
     */
    public static String getBufferType() {
		switch (bufferType) {
			case DIRECT:
				return "direct";
			case HEAP:
				return "heap";
			case AUTO:
			default:
				return "auto";
		}
	}

	/**
     * Setter for buffer type.
     *
     * @param bufferType Value to set for property 'bufferType'
     */
    public static void setBufferType(String bufferType) {
		int bufferTypeHash = bufferType.hashCode();
		 switch (bufferTypeHash) {
			 case 3198444: //heap
				 //Get a heap buffer from buffer pool
				 MP4Reader.bufferType = BufferType.HEAP;
				 break;
			 case -1331586071: //direct
				 //Get a direct buffer from buffer pool
				 MP4Reader.bufferType = BufferType.DIRECT;
				 break;
			 case 3005871: //auto
				 //Let MINA choose
			 default:
				 MP4Reader.bufferType = BufferType.AUTO;
		}
	}

	/**
     * Getter for buffer size.
     *
     * @return Value for property 'bufferSize'
     */
    public static int getBufferSize() {
		return bufferSize;
	}

	/**
     * Setter for property 'bufferSize'.
     *
     * @param bufferSize Value to set for property 'bufferSize'
     */
    public static void setBufferSize(int bufferSize) {
		// make sure buffer size is no less than 1024 bytes.
		if (bufferSize < 1024) {
			bufferSize = 1024;
		}
		MP4Reader.bufferSize = bufferSize;
	}

	/**
	 * Returns the file buffer.
	 * 
	 * @return  File contents as byte buffer
	 */
	public ByteBuffer getFileData() {
		// TODO as of now, return null will disable cache
		// we need to redesign the cache architecture so that
		// the cache is layered underneath FLVReader not above it,
		// thus both tag cache and file cache are feasible.
		return null;
	}

	/** {@inheritDoc} */
    public void decodeHeader() {
		// XXX check signature?
		// SIGNATURE, lets just skip
		fillBuffer(9);
		header = new MP4Header();
		in.skip(3);
		header.setVersion(in.get());
		header.setTypeFlags(in.get());
		header.setDataOffset(in.getInt());
		if (log.isDebugEnabled()) {
			log.debug("Header: " + header.toString());
		}
	}

	/** {@inheritDoc}
	 */
	public IStreamableFile getFile() {
		// TODO wondering if we need to have a reference
		return null;
	}

	/** {@inheritDoc}
	 */
	public int getOffset() {
		// XXX what's the difference from getBytesRead
		return 0;
	}

	/** {@inheritDoc}
	 */
	public long getBytesRead() {
		// XXX should summarize the total bytes read or
		// just the current position?
		return getCurrentPosition();
	}

	/** {@inheritDoc} */
    public long getDuration() {
		return duration;
	}

	public String getVideoCodecId() {
		return videoCodecId;
	}

	public String getAudioCodecId() {
		return audioCodecId;
	}

	/** {@inheritDoc}
	 */
	public boolean hasMoreTags() {
		return getRemainingBytes() > 4;
	}

    /**
     * Create tag for metadata event.
	 *
	 * Info from http://www.kaourantin.net/2007/08/what-just-happened-to-video-on-web_20.html
	 * <pre>
		duration - Obvious. But unlike for FLV files this field will always be present.
		videocodecid - For H.264 we report 'avc1'.
        audiocodecid - For AAC we report 'mp4a', for MP3 we report '.mp3'.
        avcprofile - 66, 77, 88, 100, 110, 122 or 144 which corresponds to the H.264 profiles.
        avclevel - A number between 10 and 51. Consult this list to find out more.
        aottype - Either 0, 1 or 2. This corresponds to AAC Main, AAC LC and SBR audio types.
        moovposition - The offset in bytes of the moov atom in a file.
        trackinfo - An array of objects containing various infomation about all the tracks in a file.
        chapters - As mentioned above information about chapters in audiobooks.
        seekpoints - As mentioned above times you can directly feed into NetStream.seek();
        videoframerate - The frame rate of the video if a monotone frame rate is used. 
        		Most videos will have a monotone frame rate.
        audiosamplerate - The original sampling rate of the audio track.
        audiochannels - The original number of channels of the audio track.
        tags - As mentioned above ID3 like tag information.
	 * </pre>
	 * Info from 
	 * <pre>
		width: Display width in pixels.
		height: Display height in pixels.
		duration: Duration in seconds.
		avcprofile: AVC profile number such as 55, 77, 100 etc.
		avclevel: AVC IDC level number such as 10, 11, 20, 21 etc.
		aacaot: AAC audio object type; 0, 1 or 2 are supported.
		videoframerate: Frame rate of the video in this MP4.
		seekpoints: Array that lists the available keyframes in a file as time stamps in milliseconds. 
				This is optional as the MP4 file might not contain this information. Generally speaking, 
				most MP4 files will include this by default.
		videocodecid: Usually a string such as "avc1" or "VP6F."
		audiocodecid: Usually a string such as ".mp3" or "mp4a."
		progressivedownloadinfo: Object that provides information from the "pdin" atom. This is optional 
				and many files will not have this field.
 		trackinfo: Object that provides information on all the tracks in the MP4 file, including their 
 				sample description ID.
		tags: Array of key value pairs representing the information present in the "ilst" atom, which is 
				the equivalent of ID3 tags for MP4 files. These tags are mostly used by iTunes. 
	 * </pre>
	 *
     * @return         Metadata event tag
     */
    private ITag createFileMeta() {
		// Create tag for onMetaData event
		ByteBuffer buf = ByteBuffer.allocate(1024);
		buf.setAutoExpand(true);
		Output out = new Output(buf);

        // Duration property
		out.writeString("onMetaData");
		Map<Object, Object> props = new HashMap<Object, Object>();
		props.put("duration", duration);
		props.put("width", width);
		props.put("height", height);

		// Video codec id
		props.put("videocodecid", videoCodecId);
		props.put("avcprofile", "");
        props.put("avclevel", "");
        props.put("videoframerate", "");
		// Audio codec id - watch for mp3 instead of aac
        props.put("audiocodecid", audioCodecId);
        props.put("aottype", "");
        props.put("audiosamplerate", audioSampleRate);
        props.put("audiochannels", audioChannels);
        
        props.put("moovposition", "");
        props.put("trackinfo", "");
        //props.put("chapters", "");
        props.put("seekpoints", "");
        // tags will only appear if there is an "ilst" atom in the file
        //props.put("tags", "");
   
		props.put("canSeekToEnd", false);
		out.writeMap(props, new Serializer());
		buf.flip();

		ITag result = new Tag(IoConstants.TYPE_METADATA, 0, buf.limit(), null, 0);
		result.setBody(buf);
		return result;
	}

	/** {@inheritDoc}
	 */
    public synchronized ITag readTag() {
		long oldPos = getCurrentPosition();
		ITag tag = readTagHeader();

		if (tagPosition == 0 && tag.getDataType() != TYPE_METADATA
				&& generateMetadata) {
			// Generate initial metadata automatically
			setCurrentPosition(oldPos);
			KeyFrameMeta meta = analyzeKeyFrames();
			tagPosition++;
			if (meta != null) {
				return createFileMeta();
			}
		}

		ByteBuffer body = ByteBuffer.allocate(tag.getBodySize());

		// XXX Paul: this assists in 'properly' handling damaged FLV files		
		long newPosition = getCurrentPosition() + tag.getBodySize();
		if (newPosition <= getTotalBytes()) {
			int limit;
			while (getCurrentPosition() < newPosition) {
				fillBuffer(newPosition - getCurrentPosition());
				if (getCurrentPosition() + in.remaining() > newPosition) {
					limit = in.limit();
					in.limit((int) (newPosition - getCurrentPosition()) + in.position());
					body.put(in);
					in.limit(limit);
				} else {
					body.put(in);
				}
			}

			body.flip();
			tag.setBody(body);
			tagPosition++;
		}

		return tag;
	}

	/** {@inheritDoc}
	 */
	public void close() {
		log.debug("Close");
		if (in != null) {
			in.release();
			in = null;
		}
		if (channel != null) {
			try {
				channel.close();
				fis.close();
			} catch (IOException e) {
				log.error("Channel close {}", e);
			}
		}
	}

    /**
     * Key frames analysis may be used as a utility method so
	 * synchronize it.
	 *
     * @return             Keyframe metadata
     */
    public synchronized KeyFrameMeta analyzeKeyFrames() {
		if (keyframeMeta != null) {
			return keyframeMeta;
		}

		// check for cached keyframe informations
		if (keyframeCache != null) {
			keyframeMeta = keyframeCache.loadKeyFrameMeta(file);
			if (keyframeMeta != null) {
				// Keyframe data loaded, create other mappings
				duration = keyframeMeta.duration;
				posTimeMap = new HashMap<Long, Long>();
				for (int i=0; i<keyframeMeta.positions.length; i++) {
					posTimeMap.put(keyframeMeta.positions[i], (long) keyframeMeta.timestamps[i]);
				}
				// XXX: We currently lose pos -> tag mapping, but that isn't used anywhere, so that's okay for now... 
				posTagMap = new HashMap<Long, Integer>();
				posTagMap.put((long) 0, 0);
				return keyframeMeta;
			}
		}
		
        // Lists of video positions and timestamps
        List<Long> positionList = new ArrayList<Long>();
        List<Integer> timestampList = new ArrayList<Integer>();
        // Lists of audio positions and timestamps
        List<Long> audioPositionList = new ArrayList<Long>();
        List<Integer> audioTimestampList = new ArrayList<Integer>();
		long origPos = getCurrentPosition();
		// point to the first tag
		setCurrentPosition(9);

        // Maps positions to tags
        posTagMap = new HashMap<Long, Integer>();
		int idx = 0;
		boolean audioOnly = true;
		while (this.hasMoreTags()) {
			long pos = getCurrentPosition();
			posTagMap.put(pos, idx++);
            // Read tag header and duration
            ITag tmpTag = this.readTagHeader();
			duration = tmpTag.getTimestamp();
			if (tmpTag.getDataType() == IoConstants.TYPE_VIDEO) {
				if (audioOnly) {
					audioOnly = false;
					audioPositionList.clear();
					audioTimestampList.clear();
				}
				if (firstVideoTag == -1) {
					firstVideoTag = pos;
				}

				// Grab Frame type
				fillBuffer(1);
				byte frametype = in.get();
				if (((frametype & MASK_VIDEO_FRAMETYPE) >> 4) == FLAG_FRAMETYPE_KEYFRAME) {
					positionList.add(pos);
					timestampList.add(tmpTag.getTimestamp());
				}

			} else if (tmpTag.getDataType() == IoConstants.TYPE_AUDIO) {
				if (firstAudioTag == -1) {
					firstAudioTag = pos;
				}
				if (audioOnly) {
					audioPositionList.add(pos);
					audioTimestampList.add(tmpTag.getTimestamp());
				}
			}
			// XXX Paul: this 'properly' handles damaged FLV files - as far as
			// duration/size is concerned
			long newPosition = pos + tmpTag.getBodySize() + 15;
			// log.debug("---->" + in.remaining() + " limit=" + in.limit() + "
			// new pos=" + newPosition);
			if (newPosition >= getTotalBytes()) {
				log.info("New position exceeds limit");
				if (log.isDebugEnabled()) {
					log.debug("-----");
					log.debug("Keyframe analysis");
					log.debug(" data type=" + tmpTag.getDataType()
							+ " bodysize=" + tmpTag.getBodySize());
					log.debug(" remaining=" + getRemainingBytes() + " limit="
							+ getTotalBytes() + " new pos=" + newPosition);
					log.debug(" pos=" + pos);
					log.debug("-----");
				}
				break;
			} else {
				setCurrentPosition(newPosition);
			}
		}
		// restore the pos
		setCurrentPosition(origPos);

		keyframeMeta = new KeyFrameMeta();
		keyframeMeta.duration = duration;
		posTimeMap = new HashMap<Long, Long>();
		if (audioOnly) {
			// The flv only contains audio tags, use their lists
			// to support pause and seeking
			positionList = audioPositionList;
			timestampList = audioTimestampList;
		}
		keyframeMeta.audioOnly = audioOnly;
		keyframeMeta.positions = new long[positionList.size()];
		keyframeMeta.timestamps = new int[timestampList.size()];
		for (int i = 0; i < keyframeMeta.positions.length; i++) {
			keyframeMeta.positions[i] = positionList.get(i);
			keyframeMeta.timestamps[i] = timestampList.get(i);
			posTimeMap.put((long) positionList.get(i), (long) timestampList
					.get(i));
		}
		if (keyframeCache != null)
			keyframeCache.saveKeyFrameMeta(file, keyframeMeta);
		return keyframeMeta;
	}

	/**
	 * Put the current position to pos.
	 * The caller must ensure the pos is a valid one
	 * (eg. not sit in the middle of a frame).
	 *
	 * @param pos         New position in file. Pass <code>Long.MAX_VALUE</code> to seek to end of file.
	 */
	public void position(long pos) {
		setCurrentPosition(pos);
		if (pos == Long.MAX_VALUE) {
			tagPosition = posTagMap.size()+1;
			return;
		}
		// Make sure we have informations about the keyframes.
		analyzeKeyFrames();
		// Update the current tag number
		Integer tag = posTagMap.get(pos);
		if (tag == null) {
			return;
		}

		tagPosition = tag;
	}

	/**
	 * Read only header part of a tag.
	 *
	 * @return              Tag header
	 */
	private ITag readTagHeader() {
		// PREVIOUS TAG SIZE
		fillBuffer(15);
		int previousTagSize = in.getInt();

		// START OF FLV TAG
		byte dataType = in.get();

		// The next two lines use a utility method which reads in
		// three consecutive bytes but stores them in a 4 byte int.
		// We are able to write those three bytes back out by using
		// another utility method which strips off the last byte
		// However, we will have to check into this during optimization.
		int bodySize = IOUtils.readUnsignedMediumInt(in);
		int timestamp = IOUtils.readUnsignedMediumInt(in);
		// reserved
		in.getInt();

		return new Tag(dataType, timestamp, bodySize, null, previousTagSize);
	}

	public void setVideoCodecId(String videoCodecId) {
		this.videoCodecId = videoCodecId;
	}

	public void setAudioCodecId(String audioCodecId) {
		this.audioCodecId = audioCodecId;
	}

}
