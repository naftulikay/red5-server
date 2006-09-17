package org.red5.server.cache;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.ByteBuffer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.red5.io.flv.impl.FLVReader;
import org.red5.server.api.cache.ICacheable;

public class CacheImplTest extends TestCase {

	private static Log log = LogFactory.getLog(CacheImplTest.class.getName());

	private FileInputStream fis = null;

	private FileChannel channel = null;

	private MappedByteBuffer mappedFile = null;

	private ByteBuffer in = null;

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetObjectNames() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetObjects() {
		fail("Not yet implemented");
	}

	@Test
	public void testOfferStringByteBuffer() {
		fail("Not yet implemented");
	}

	@Test
	public void testOfferStringICacheable() throws Exception {

		String dataHash1 = "";

		FLVReader reader = null;
		ByteBuffer fileData = null;
		File file = new File("test.flv");

		String fileName = file.getName();
		log.debug("Path: " + file.getCanonicalPath());
		ICacheable ic = CacheImpl.getInstance().get(fileName);
		// look in the cache before reading the file from the disk
		if (null == ic || (null == ic.getByteBuffer())) {
			log.debug("Cache miss");
			log.debug("File size: " + file.length());
			reader = new FLVReader(new FileInputStream(file), true);
			// get a ref to the mapped byte buffer
			fileData = reader.getFileData();
			dataHash1 = fileData.getHexDump().substring(0, 200);
			log.debug("In miss: " + dataHash1);
			// offer the uncached file to the cache
			if (CacheImpl.getInstance().offer(fileName, fileData)) {
				log.debug("Item accepted by the cache");
			} else {
				log.debug("Item rejected by the cache");
			}

			// if it is unwanted, start or update the request count
			// the file will be accepted once its request count exceeds
			// the count of any other entry in the cache
		} else {
			log.debug("Cache hit");
			fileData = ic.getByteBuffer();
			dataHash1 = fileData.getHexDump().substring(0, 200);
			log.debug("In hit: " + dataHash1);
			reader = new FLVReader(fileData, true);
		}

		ic = CacheImpl.getInstance().get(fileName);
		fileData = ic.getByteBuffer();
		String dataHash2 = fileData.getHexDump().substring(0, 200);
		log.debug("" + dataHash2);

		assertTrue(dataHash1.equals(dataHash2));

	}

	@Test
	public void testGet() {
		fail("Not yet implemented");
	}

	@Test
	public void testRemoveICacheable() {
		fail("Not yet implemented");
	}

	@Test
	public void testRemoveString() {
		fail("Not yet implemented");
	}

}
