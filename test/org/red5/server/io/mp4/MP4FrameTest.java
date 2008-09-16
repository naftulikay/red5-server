package org.red5.server.io.mp4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;
import org.red5.io.mp4.MP4Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MP4FrameTest extends TestCase {

	private static Logger log = LoggerFactory.getLogger(MP4FrameTest.class);

	@Test
	public void testSort() {
		
		List<MP4Frame> frames = new ArrayList<MP4Frame>(6);
		
		//create some frames
		MP4Frame frame1 = new MP4Frame();
		frame1.setTime(1);
		frames.add(frame1);
		
		MP4Frame frame2 = new MP4Frame();
		frame2.setTime(6);
		frames.add(frame2);
		
		MP4Frame frame3 = new MP4Frame();
		frame3.setTime(660);
		frames.add(frame3);
		
		MP4Frame frame4 = new MP4Frame();
		frame4.setTime(3);
		frames.add(frame4);
		
		MP4Frame frame5 = new MP4Frame();
		frame5.setTime(400);
		frames.add(frame5);
		
		System.out.printf("Frame 1 - time: %d (should be 660)\n", frames.get(2).getTime());

		Collections.sort(frames);

		System.out.println("After sorting");
		
		int f = 1;
		for (MP4Frame frame : frames) {
			System.out.printf("Frame %d - time: %d\n", f++, frame.getTime());
		}
		
	}

	
	
}
