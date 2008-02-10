package org.red5.io.m4a.impl;

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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Enumeration;
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
import org.red5.io.object.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Reader is used to read the contents of a M4A file.
 * NOTE: This class is not implemented as threading-safe. The caller
 * should make sure the threading-safety.
 * 
 * @author The Red5 Project (red5@osflash.org)
 * @author Paul Gregoire, (mondain@gmail.com)
 */
public class M4AReader implements IoConstants, ITagReader {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(M4AReader.class);

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
     * Input byte buffer
     */
    private ByteBuffer in;

	/** Current tag. */
	private int tagPosition;

	/** Mapping between file position and timestamp in ms. */
	private HashMap<Long, Long> posTimeMap;

	/** Mapping between file position and tag number. */
	private HashMap<Long, Integer> posTagMap;
	
	private HashMap<Integer, Long> samplePosMap;

	/** Buffer type / style to use **/
	private static BufferType bufferType = BufferType.AUTO;

	private static int bufferSize = 1024;

	/** Cache for keyframe informations. */
	private static IKeyFrameMetaCache keyframeCache;
		
	private String audioCodecId = "mp4a";
	
	/** Duration in milliseconds. */
	private long duration;	
	private int timeScale;
	private int audioSampleRate;
	private int audioChannels;
	private int audioSampleCount;
	private String formattedDuration;
	private long moovOffset;
	private long mdatOffset;
	
	//samples to chunk mappings
	private Vector audioSamplesToChunks;
	//samples 
	private Vector audioSamples;
	//chunk offsets
	private Vector audioChunkOffsets;
	//sample duration
	private int audioSampleDuration = 1024;
	
	//keep track of current sample
	private int currentSample = 0;
	
	private long firstAudioTag;
	
    /**
     * File metadata
     */
	private ITag fileMeta;
	
	/** Constructs a new M4AReader. */
	M4AReader() {
	}

    /**
     * Creates M4A reader from file input stream.
	 *
     * @param f         File
     */
    public M4AReader(File f) throws IOException {
		this(f, false);
	}

    /**
     * Creates M4A reader from file input stream, sets up metadata generation flag.
	 *
     * @param f                    File input stream
     * @param generateMetadata     <code>true</code> if metadata generation required, <code>false</code> otherwise
     */
    public M4AReader(File f, boolean generateMetadata) throws IOException {
    	if (null == f) {
    		log.warn("Reader was passed a null file");
        	log.debug("{}", ToStringBuilder.reflectionToString(this));
    	}
    	this.file = f;
		this.fis = new MP4DataStream(new FileInputStream(f));
		channel = fis.getChannel();

		decodeHeader();
		fillBuffer();
	}

    /**
	 * Accepts mapped file bytes to construct internal members.
	 *
	 * @param generateMetadata         <code>true</code> if metadata generation required, <code>false</code> otherwise
     * @param buffer                   Byte buffer
	 */
	public M4AReader(ByteBuffer buffer, boolean generateMetadata) throws IOException {
		in = buffer;
		decodeHeader();
	}
    
	/**
	 * Currently this expects the moov atom at the beginning of the file, later we will
	 * have to handle the mdat at the beginning instead.
	 */
	public void decodeHeader() {
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
			moovOffset = fis.getOffset() - moov.getSize();

			MP4Atom mvhd = moov.lookup(MP4Atom.typeToInt("mvhd"), 0);
			if (mvhd != null) {
				log.debug("Movie header atom found");
				log.debug("Time scale {}", mvhd.getTimeScale());
				log.debug("Duration {}", mvhd.getDuration());
				timeScale = mvhd.getTimeScale();
				duration = mvhd.getDuration();
			}

			MP4Atom meta = moov.lookup(MP4Atom.typeToInt("meta"), 0);
			if (meta != null) {
				log.debug("Meta atom found");
				log.debug("{}", ToStringBuilder.reflectionToString(meta));
			}
			
			MP4Atom trak = moov.lookup(MP4Atom.typeToInt("trak"), 0);
			if (trak != null) {
				log.debug("Track atom found");
				log.debug("trak children: {}", trak.getChildren());	
				// trak: tkhd, edts, mdia
				MP4Atom tkhd = trak.lookup(MP4Atom.typeToInt("tkhd"), 0);
				if (tkhd != null) {
					log.debug("Track header atom found");
					log.debug("tkhd children: {}", tkhd.getChildren());	
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
									audioSamplesToChunks = stsc.getRecords();
									log.debug("Record count: {}", audioSamplesToChunks.size());
									MP4Atom.Record rec = (MP4Atom.Record) audioSamplesToChunks.firstElement();
									log.debug("Record data: Description index={} Samples per chunk={}", rec.getSampleDescriptionIndex(), rec.getSamplesPerChunk());
								}									
								//stsz - has Samples
								MP4Atom stsz = stbl.lookup(MP4Atom.typeToInt("stsz"), 0);
								if (stsz != null) {
									log.debug("Sample size atom found");
									audioSamples = stsz.getSamples();
									//vector full of integers										
									log.debug("Sample size: {}", stsz.getSampleSize());
									log.debug("Sample count: {}", audioSamples.size());
									audioSampleCount = audioSamples.size();
								}
								//stco - has Chunks
								MP4Atom stco = stbl.lookup(MP4Atom.typeToInt("stco"), 0);
								if (stco != null) {
									log.debug("Chunk offset atom found");
									//vector full of integers
									audioChunkOffsets = stco.getChunks();
									log.debug("Chunk count: {}", audioChunkOffsets.size());
									//set the first video offset
									firstAudioTag = (Long) audioChunkOffsets.get(0);
								}
								//stts - has TimeSampleRecords
								MP4Atom stts = stbl.lookup(MP4Atom.typeToInt("stts"), 0);
								if (stts != null) {
									log.debug("Time to sample atom found");
									Vector records = stts.getTimeToSamplesRecords();
									log.debug("Record count: {}", records.size());
									MP4Atom.TimeSampleRecord rec = (MP4Atom.TimeSampleRecord) records.firstElement();
									log.debug("Record data: Consecutive samples={} Duration={}", rec.getConsecutiveSamples(), rec.getSampleDuration());
									//if we have 1 record then all samples have the same duration
									if (records.size() > 1) {
										log.warn("Audio samples have differing durations, audio playback may fail");
									}
									audioSampleDuration = rec.getSampleDuration();
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
				
			//real duration
			StringBuilder sb = new StringBuilder();
			double videoTime = ((double) duration / (double) timeScale);
			int minutes = (int) (videoTime / 60);
			if (minutes > 0) {
    			sb.append(minutes);
    			sb.append('.');
			}
			//formatter for seconds / millis
			NumberFormat df = DecimalFormat.getInstance();
			df.setMaximumFractionDigits(2);
			sb.append(df.format((videoTime % 60)));
			formattedDuration = sb.toString();
			log.debug("Time: {}", formattedDuration);
			
			long dataSize = 0L;
			
			MP4Atom mdat = null;
			do {
				mdat = MP4Atom.createAtom(fis);
    			if (mdat != null && mdat.getType() == MP4Atom.typeToInt("mdat")) {
    				log.debug("Movie data atom found");
    				dataSize = mdat.getSize();
    				log.debug("{}", ToStringBuilder.reflectionToString(mdat));    
    				mdatOffset = fis.getOffset() - mdat.getSize();
    			} else {
    				log.debug("{} atom found", MP4Atom.intToType(mdat.getType()));
    			}
			} while (mdat.getType() != MP4Atom.typeToInt("mdat"));
			
			log.debug("File size: {} mdat size: {}", file.length(), dataSize);
			//the tag name to the offsets
			moovOffset += 8;
			mdatOffset += 8;
			//
			//mdatOffset = 135204;
			log.debug("Offsets moov: {} mdat: {}", moovOffset, mdatOffset);
						
		} catch (IOException e) {
			log.error("{}", e);
		}		
	}
	
    public void setKeyFrameCache(IKeyFrameMetaCache keyframeCache) {
    	M4AReader.keyframeCache = keyframeCache;
    }

    /**
	 * Get the remaining bytes that could be read from a file or ByteBuffer.
	 *
	 * @return          Number of remaining bytes
	 */
	private long getRemainingBytes() {
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
		try {
			//if we are at the end of the file drop back to mdat offset
			if (channel.position() == channel.size()) {
				log.debug("Reached end of file, going back to data offset");
				channel.position(mdatOffset);
			}		
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
    	log.debug("Set current position: {}", pos);
		if (pos == Long.MAX_VALUE) {
			pos = file.length();
		}
		//in.position((int) pos);
		try {
			if (pos >= (channel.position() - in.limit()) && pos < channel.position()) {
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
			log.debug("Fill buffer with {} bytes. Append or reload: {}", amount, (reload ? "reload" : "append"));
//			if (amount > bufferSize) {
//				amount = bufferSize;
//			}
			if (in != null) {
				if (!reload) {
					in.compact();
				} else {
					in.clear();
				}		
			} else {
				log.debug("ByteBuffer was null, creating a new one");
				switch (bufferType) {
					case HEAP:
						in = ByteBuffer.allocate(bufferSize, false);
						break;
					case DIRECT:
						in = ByteBuffer.allocate(bufferSize, true);
						break;
					case AUTO:
					default:
						in = ByteBuffer.allocate(bufferSize);
				}
			}	
			if (amount > bufferSize) {
				in = ByteBuffer.allocate((int) amount);
			}
			channel.read(in.buf());
			in.flip();	
		} catch (Exception e) {
			log.error("Error fillBuffer", e);
		}
	}

    /** {@inheritDoc} */
    public boolean hasVideo() {
    	return false;
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
				 M4AReader.bufferType = BufferType.HEAP;
				 break;
			 case -1331586071: //direct
				 //Get a direct buffer from buffer pool
				 M4AReader.bufferType = BufferType.DIRECT;
				 break;
			 case 3005871: //auto
				 //Let MINA choose
			 default:
				 M4AReader.bufferType = BufferType.AUTO;
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
		M4AReader.bufferSize = bufferSize;
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

	public String getAudioCodecId() {
		return audioCodecId;
	}

	/** {@inheritDoc}
	 */
	public boolean hasMoreTags() {
		return currentSample < audioSampleCount;
	}

    /**
     * Create tag for metadata event.
	 *
     * @return         Metadata event tag
     */
    private ITag createFileMeta() {
		// Create tag for onMetaData event
		ByteBuffer buf = ByteBuffer.allocate(320);
		buf.setAutoExpand(true);
		Output out = new Output(buf);

        // Duration property
		out.writeString("onMetaData");
		Map<Object, Object> props = new HashMap<Object, Object>();
		props.put("duration", ((double) duration / (double) timeScale));
		// Audio codec id - watch for mp3 instead of aac
        props.put("audiocodecid", audioCodecId);
        props.put("aacaot", "0");
        props.put("audiosamplerate", audioSampleRate);
        props.put("audiochannels", audioChannels);
        
        props.put("moovposition", moovOffset);
        //props.put("chapters", "");
        //props.put("seekpoints", "");
        //tags will only appear if there is an "ilst" atom in the file
        //props.put("tags", "");
        props.put("trackinfo", "");
   
		props.put("canSeekToEnd", false);
		out.writeMap(props, new Serializer());
		buf.flip();

		ITag result = new Tag(IoConstants.TYPE_METADATA, 0, buf.limit(), null, 0);
		result.setBody(buf);
		return result;
	}

    int prevFrameSize = 0;
    int currentTime = 0;
    
	/** {@inheritDoc}
	 */
    public synchronized ITag readTag() {
		log.debug("Read tag - tagPosition {}, prevFrameSize {}", new Object[]{tagPosition, prevFrameSize});
		if (tagPosition == 0) {
			tagPosition++;
			analyzeFrames();	
			fileMeta = createFileMeta();				
			//return onMetaData stuff
			prevFrameSize = fileMeta.getBodySize();
			return fileMeta;
		}
				
		int sampleSize = (Integer) audioSamples.get(currentSample);

		int ts = ((int) audioSampleDuration * (currentSample));
		
		long samplePos = samplePosMap.get(currentSample);
		setCurrentPosition(samplePos);
		
		ITag tag = new Tag(IoConstants.TYPE_AUDIO, ts, sampleSize, null, prevFrameSize);
		log.debug("Read tag - body size: {}", tag.getBodySize());
		ByteBuffer body = ByteBuffer.allocate(tag.getBodySize());
		log.debug("Read tag - current pos {} sample pos {}", getCurrentPosition(), samplePos);
		fillBuffer(sampleSize);
		//get current limit
		final int limit = in.limit();
		log.debug("Limit (current): {}", limit);
		//set to sample size
		in.limit(sampleSize);
		body.put(in);
		body.flip();
		//reset limit
		in.limit(limit);
		tag.setBody(body);
		tagPosition++;
		
		prevFrameSize = tag.getBodySize();

		currentSample++;
		
		return tag;
	}

    /**
     * Key frames analysis may be used as a utility method so
	 * synchronize it.
	 *
     * @return             Keyframe metadata
     */
    public void analyzeFrames() {				
        // Maps positions to tags
        posTagMap = new HashMap<Long, Integer>();
        samplePosMap = new HashMap<Integer, Long>();
		posTimeMap = new HashMap<Long, Long>();

        // tag == sample
		int sample = 0;
		Long pos = null;
		Enumeration records = audioSamplesToChunks.elements();
		while (records.hasMoreElements()) {
			MP4Atom.Record record = (MP4Atom.Record) records.nextElement();
			int firstChunk = record.getFirstChunk();
			int sampleCount = record.getSamplesPerChunk();
			//log.debug("First chunk: {} count:{}", firstChunk, sampleCount);
			pos = (Long) audioChunkOffsets.elementAt(firstChunk - 1);
			while (sampleCount > 0) {
				//log.debug("Position: {}", pos);
    			posTagMap.put(pos, sample);
    			samplePosMap.put(sample, pos);
    			posTimeMap.put(pos, (long)(audioSampleDuration * (sample)));
    			pos = pos + (Integer) audioSamples.get(sample);
    			sampleCount--;
    			sample++;
			}
		}

		log.debug("Position Tag Map size: {}", posTagMap.size());
	}

	/**
	 * Put the current position to pos.
	 * The caller must ensure the pos is a valid one
	 * (eg. not sit in the middle of a frame).
	 *
	 * @param pos         New position in file. Pass <code>Long.MAX_VALUE</code> to seek to end of file.
	 */
	public void position(long pos) {
		log.debug("position: {}", pos);
		setCurrentPosition(pos);
		if (pos == Long.MAX_VALUE) {
			tagPosition = posTagMap.size()+1;
			return;
		}
		// Update the current tag number
		Integer tagNumber = posTagMap.get(pos);
		log.debug("Got a tag number {} for position {}", tagNumber, pos);
		if (tagNumber == null) {
			return;
		}
		tagPosition = tagNumber;
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
				fis = null;
			} catch (IOException e) {
				log.error("Channel close {}", e);
			}
		}
	}

	public void setAudioCodecId(String audioCodecId) {
		this.audioCodecId = audioCodecId;
	}

	public ITag readTagHeader() {
		return null;
	}
	
}
