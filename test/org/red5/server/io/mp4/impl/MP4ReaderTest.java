package org.red5.server.io.mp4.impl;

import java.io.File;

import junit.framework.TestCase;

import org.junit.Test;
import org.red5.io.mp4.impl.MP4Reader;

public class MP4ReaderTest extends TestCase {

	@Test
	public void testCtor() throws Exception {	
		File file = new File("C:/red5/webapps/oflaDemo/streams/backcountry_bombshells_4min_HD_H264.mp4");
		//File file = new File("C:/red5/webapps/oflaDemo/streams/IronMan.mov");
		MP4Reader reader = new MP4Reader(file, false);
	}
	
}
