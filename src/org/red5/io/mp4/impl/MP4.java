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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.mina.common.ByteBuffer;
import org.red5.io.ITag;
import org.red5.io.ITagReader;
import org.red5.io.ITagWriter;
import org.red5.io.IoConstants;
import org.red5.io.flv.meta.IMetaData;
import org.red5.io.flv.meta.IMetaService;
import org.red5.io.flv.meta.MetaService;
import org.red5.io.mp4.IMP4;
import org.red5.server.api.cache.ICacheStore;
import org.red5.server.api.cache.ICacheable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A MP4Impl implements the MP4 api
 * 
 * @author The Red5 Project (red5@osflash.org)
 * @author Paul Gregoire, (mondain@gmail.com)
 */
public class MP4 implements IMP4 {

	protected static Logger log = LoggerFactory.getLogger(MP4.class);

	private static ICacheStore cache;

	private File file;

	private boolean generateMetadata;

	private IMetaService metaService;

	private IMetaData metaData;

	/**
	 * Default constructor, used by Spring so that parameters may be injected.
	 */
	public MP4() {
	}

	/**
	 * Create MP4 from given file source
	 * 
	 * @param file
	 *            File source
	 */
	public MP4(File file) {
		this(file, false);
	}

	/**
	 * Create MP4 from given file source and with specified metadata generation
	 * option
	 * 
	 * @param file
	 *            File source
	 * @param generateMetadata
	 *            Metadata generation option
	 */
	public MP4(File file, boolean generateMetadata) {
		this.file = file;
		this.generateMetadata = generateMetadata;
		int count = 0;

		if (!generateMetadata) {
			try {
				MP4Reader reader = new MP4Reader(this.file);
				ITag tag = null;
				while (reader.hasMoreTags() && (++count < 5)) {
					tag = reader.readTag();
					if (tag.getDataType() == IoConstants.TYPE_METADATA) {
						if (metaService == null) {
							metaService = new MetaService(this.file);
						}
						metaData = metaService.readMetaData(tag.getBody());
					}
				}
				reader.close();
			} catch (Exception e) {
				log.error("An error occured looking for metadata:", e);
			}
		}

	}

	/**
	 * Sets the cache implementation to be used.
	 * 
	 * @param cache
	 *            Cache store
	 */
	public void setCache(ICacheStore cache) {
		MP4.cache = cache;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasMetaData() {
		return metaData != null;
	}

	/**
	 * {@inheritDoc}
	 */
	public IMetaData getMetaData() throws FileNotFoundException {
		metaService.setInStream(new FileInputStream(file));
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasKeyFrameData() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setKeyFrameData(Map keyframedata) {
	}

	/**
	 * {@inheritDoc}
	 */
	public Map getKeyFrameData() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void refreshHeaders() throws IOException {
	}

	/**
	 * {@inheritDoc}
	 */
	public void flushHeaders() throws IOException {
	}

	/**
	 * {@inheritDoc}
	 */
	public ITagReader getReader() throws IOException {
		MP4Reader reader = null;
		ByteBuffer fileData;
		String fileName = file.getName();

		// if no cache is set an NPE will be thrown
		if (cache == null) {
			System.out.println("No cache");
			log
					.warn("MP4 cache is null, an NPE may be thrown. To fix your code, ensure a cache is set via Spring or by the following: setCache(NoCacheImpl.getInstance())");
		}
		ICacheable ic = cache.get(fileName);

		// look in the cache before reading the file from the disk
		if (null == ic || (null == ic.getByteBuffer())) {
			if (file.exists()) {
				if (log.isDebugEnabled()) {
					log.debug("File size: " + file.length());
				}
				reader = new MP4Reader(file, generateMetadata);
				// get a ref to the mapped byte buffer
				fileData = reader.getFileData();
				// offer the uncached file to the cache
				if (fileData != null && cache.offer(fileName, fileData)) {
					if (log.isDebugEnabled()) {
						log.debug("Item accepted by the cache: " + fileName);
					}
				} else {
					if (log.isDebugEnabled()) {
						log.debug("Item will not be cached: " + fileName);
					}
				}
			} else {
				log.info("Creating new file: " + file);
				file.createNewFile();
			}
		} else {
			fileData = ByteBuffer.wrap(ic.getBytes());
			reader = new MP4Reader(fileData, generateMetadata);
		}
		return reader;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITagReader readerFromNearestKeyFrame(int seekPoint) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITagWriter getWriter() throws IOException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public ITagWriter writerFromNearestKeyFrame(int seekPoint) {
		return null;
	}

	/** {@inheritDoc} */
	public void setMetaData(IMetaData meta) throws IOException {
		File tmpFile = File.createTempFile("setMeta_", ".mp4");
		if (metaService == null) {
			metaService = new MetaService(file);
		}
		metaService.setInStream(new FileInputStream(file));
		metaService.setOutStream(new FileOutputStream(tmpFile));
		//if the file is not checked the write may produce an NPE
		if (((MetaService) metaService).getFile() == null) {
			((MetaService) metaService).setFile(file);
		}
		metaService.write(meta);
		metaData = meta;
		file.delete();
		if (!tmpFile.renameTo(file)) {
			// Probably renaming across filesystems? Move manually.
			FileInputStream fis = new FileInputStream(tmpFile);
			FileOutputStream fos = new FileOutputStream(file);
			byte[] buf = new byte[16384];
			int i = 0;
			while ((i = fis.read(buf)) != -1) {
				fos.write(buf, 0, i);
			}
			fis.close();
			fos.close();
			tmpFile.delete();
		}
	}

	/** {@inheritDoc} */
	public void setMetaService(IMetaService service) {
		metaService = service;
	}

	public ITagWriter getAppendWriter() throws IOException {
		return null;
	}
	
}
