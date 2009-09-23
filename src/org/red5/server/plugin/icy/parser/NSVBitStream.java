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

import java.io.*;

/**
 * Provides a means to stream media via NSV and shoutcast.
 * For use with NSVCap, Winamp shoutcast dsp, and shoutcast dnas.
 * 
 * @author Paul Gregoire (mondain@gmail.com)
 * @author Andy Shaules (bowljoman@hotmail.com)
 */
public class NSVBitStream {

	private int m_allocated = 1;

	private long[] m_bits = new long[m_allocated];

	public InputStream m_input_source;

	private int m_used = 0;

	private int m_bitpos = 0;

	private int m_eof = 0;

	public NSVBitStream() {
	}

	public NSVBitStream(InputStream p_stream) {
		m_input_source = p_stream;
	}

	/**
	 * only tested 32 bits max per request.
	 * 
	 * @param nbits
	 * @param p_value
	 */
	public void putBits(int nbits, long p_value) {

		m_eof = 0;

		while (nbits-- > 0) {
			m_bits[m_used / 8] |= (p_value & 1) << (m_used & 7);
			if (((++m_used) & 7) == 0) {
				resize(1);
			}
			p_value >>= 1;
		}
	}

	/**
	 * only tested 32 bit Maximum per request.
	 * 
	 * @param nbits
	 * @return
	 */
	public int getbits(int nbits) {
		if (m_used - m_bitpos < nbits) {
			if (!m_input_source.equals(null)) {
				try {
					this.putBits(8, m_input_source.read());
					this.putBits(8, m_input_source.read());
					this.putBits(8, m_input_source.read());
					this.putBits(8, m_input_source.read());
				} catch (java.io.IOException er2) {
					m_eof = 1;
					return -1;
				}
			} else {
				m_eof = 1;
				return -1;
			}
		}
		int ret = 0;
		int sh = 0;
		int t = m_bitpos / 8;

		for (sh = 0; sh < nbits; sh++) {
			ret |= ((m_bits[t] >> (m_bitpos & 7)) & 1) << sh;

			if (((++m_bitpos) & 7) == 0) {
				t++;
			}
		}

		return ret;
	}

	public int eof() {
		m_eof = (m_used - m_bitpos == 0) ? 1 : 0;
		return m_eof;
	}

	public int rewind() {
		m_bitpos = 0;
		return m_used;
	}

	public int available() {
		return m_used - m_bitpos;
	}

	/**
	 * Number of Bytes to add.
	 * 
	 * @param p_size
	 */
	private void resize(int p_size) {

		long[] new_bits = new long[m_allocated + p_size];

		for (int i = 0; i < m_allocated; i++) {
			new_bits[i] = m_bits[i];
		}
		m_allocated += p_size;
		m_bits = new_bits;

	}

	public void seek(int pval) {
		m_bitpos += pval;
		m_bitpos = m_bitpos < 0 ? 0 : m_bitpos;
		m_bitpos = m_bitpos > m_used ? m_used : m_bitpos;

	}

	public void compact() {
		System.out.println("Compacting");
		int av = m_used - m_bitpos;
		long[] new_bits = new long[av / 8 + 1];

		for (int i = (m_bitpos / 8); i < av / 8 + 1; i++) {
			new_bits[i] = m_bits[i];
		}
		m_allocated = av / 8 + 1;
		m_bits = new_bits;

	}

}
