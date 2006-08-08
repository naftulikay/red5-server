package org.red5.server.io.test;

/*
 * * RED5 Open Source Flash Server - http://www.osflash.org/red5
 * 
 * Copyright © 2006 by respective authors. All rights reserved.
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
 * 
 * @author The Red5 Project (red5@osflash.org)
 * @author Dominick Accattato (daccattato@gmail.com)
 */

import org.red5.io.flv.meta.MetaData;

import junit.framework.Assert;
import junit.framework.TestCase;


/**
 * MetaData TestCase
 * 
 * @author The Red5 Project (red5@osflash.org)
 * @author daccattato (daccattato@gmail.com)
 * @version 0.3
 */
public class MetaDataTest extends TestCase {
	MetaData data;
	
	public MetaDataTest() {
		data = new MetaData();
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		
		data.setCanSeekToEnd(true);
		data.setDuration(7.347);
		data.setframeRate(15);
		data.setHeight(333);
		data.setVideoCodecId(4);
		data.setVideoDataRate(400);
		data.setWidth(300);
	}
	
	public void tearDown() {
		data = null;
	}
	
	public void testCanSeekToEnd() {
		Assert.assertEquals(true, data.getCanSeekToEnd());
	}	
	
	public void testDuration() {
		Assert.assertEquals(7.347, data.getDuration(), 0);
	}
	
	public void testFrameRate() {
		Assert.assertEquals(15, data.getframeRate());
	}	
		
	public void testHeight() {		
		Assert.assertEquals(333, data.getHeight());
	}		
	
	public void testVideoCodecId() {
		Assert.assertEquals(4, data.getVideoCodecId());
	}	
	
	public void testVideoDataRate() {
		Assert.assertEquals(400, data.getVideoDataRate());
	}	
	
	public void testWidth() {
		Assert.assertEquals(400, data.getVideoDataRate());
	}	
	
	public void testBitShift () {
		int maxLoop = 10000;
		
		int i = 0;
		int j = 0;
////	org.red5.server.stream.StreamFlow
//		 << 3 == * 8
		long start = System.nanoTime();
		for (int x=0;x<maxLoop;x++) {
			i = 8 << 3;
		}
		long meth1 = (System.nanoTime() - start);
		System.out.println("Method 1 (bit shift): " + meth1 + " ns");		

		start = System.nanoTime();
		for (int x=0;x<maxLoop;x++) {
			j = 8 * 8;
		}
		long meth2 = (System.nanoTime() - start);
		System.out.println("Method 2 (* multiply): " + meth2 + " ns");		
		System.out.println("Increase in speed? " + (meth2 / meth1) + "%");		
		
		
		System.out.println("Results 1 = I: " + i + " J: " + j);
		assertTrue(i == j);
		
////		org.red5.server.net.rtmp.RTMPHandler
//		>> 2 == / 4	
		i = 8 >> 2;
		j = 8 / 4;
		
		start = System.nanoTime();
		for (int x=0;x<maxLoop;x++) {
			i = 8 >> 2;
		}
		meth1 = (System.nanoTime() - start);
		System.out.println("Method 1 (bit shift): " + meth1 + " ns");		

		start = System.nanoTime();
		for (int x=0;x<maxLoop;x++) {
			j = 8 / 4;
		}
		meth2 = (System.nanoTime() - start);
		System.out.println("Method 2 (/ divide): " + meth2 + " ns");	
		System.out.println("Increase in speed? " + (meth2 / meth1) + "%");			
		System.out.println("Results 2 = I: " + i + " J: " + j);
		assertTrue(i == j);		
	
	}	
	
}
