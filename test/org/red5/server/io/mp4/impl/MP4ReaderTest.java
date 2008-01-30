package org.red5.server.io.mp4.impl;

import java.io.File;

import junit.framework.TestCase;

import org.junit.Test;
import org.red5.io.mp4.impl.MP4Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MP4ReaderTest extends TestCase {
	
    private static Logger log = LoggerFactory.getLogger(MP4ReaderTest.class);

	@Test
	public void testCtor() throws Exception {	
		File file = new File("C:/red5/webapps/oflaDemo/streams/backcountry_bombshells_4min_HD_H264.mp4");
		MP4Reader reader = new MP4Reader(file, false);
		
		log.info("----------------------------------------------------------------------------------");

		File file2 = new File("C:/red5/webapps/oflaDemo/streams/IronMan.mov");
		MP4Reader reader2 = new MP4Reader(file2, false);
		
	}
	
}
