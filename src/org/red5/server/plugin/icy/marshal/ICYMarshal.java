package org.red5.server.plugin.icy.marshal;

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

import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.red5.io.amf.Output;
import org.red5.io.object.Serializer;
import org.red5.server.api.IContext;
import org.red5.server.api.IScope;

import org.red5.server.net.rtmp.event.IRTMPEvent;
import org.red5.server.net.rtmp.event.Notify;
import org.red5.server.plugin.icy.IICYMarshal;
import org.red5.server.plugin.icy.marshal.transpose.AudioFramer;
import org.red5.server.plugin.icy.marshal.transpose.VideoFramer;
import org.red5.server.plugin.icy.stream.ICYStream;
import org.red5.server.stream.BroadcastScope;
import org.red5.server.stream.IBroadcastScope;
import org.red5.server.stream.IProviderService;
import org.red5.server.stream.codec.AACAudio;

/**
 * This class registers the stream name in the provided scope and packages the buffers into rtmp events.
 * 
 * @author Wittawas Nakkasem (vittee@hotmail.com)
 * @author Andy Shaules (bowljoman@hotmail.com)
 */
public class ICYMarshal implements IICYMarshal {

	private AudioFramer audioFramer;

	private VideoFramer videoFramer;

	private IScope _scope;

	private String _name;

	private ICYStream _stream;

	private String _content;

	private String _type;

	private String _fourCCAudio;

	private String _fourCCVideo;

	private Map<String, Object> _metaData;

	public ICYMarshal(IScope outputScope, String outputName) {
		_scope = outputScope;
		_name = outputName;
		_stream = new ICYStream(_name, true, true);
		_stream.setScope(outputScope);

		IContext context = outputScope.getContext();
		IProviderService providerService = (IProviderService) context.getBean(IProviderService.BEAN_NAME);
		if (providerService.registerBroadcastStream(outputScope, _stream.getPublishedName(), _stream)) {
			IBroadcastScope bsScope = (BroadcastScope) providerService.getLiveProviderInput(outputScope, _stream
					.getPublishedName(), true);
			bsScope.setAttribute(IBroadcastScope.STREAM_ATTRIBUTE, _stream);
		}
		audioFramer = new AudioFramer(_stream);

	}

	public AudioFramer getAudioFramer() {
		return audioFramer;
	}

	public VideoFramer getVideoFramer() {
		return videoFramer;
	}

	public IScope getScope() {
		return _scope;
	}

	public ICYStream getStream() {
		return _stream;
	}

	public String getContentType() {
		return _type;
	}

	public String getAudioType() {
		return _fourCCAudio;
	}

	public String getVideoType() {
		return _fourCCVideo;
	}

	public void reset(String content, String type) {

		_content = content;
		_type = type;
		_stream.reset();
		audioFramer.reset();

		if (content.equals("audio")) {
			if (type.startsWith("aac")) {
				AACAudio audioCodec = new AACAudio();
				_stream.setAudioReader(audioCodec);
				_stream.audioFramer = audioFramer;

			} else if (type.equals("mpeg")) {
				//MP3Audio
			}
		} else if (content.equals("video")) {
			videoFramer = new VideoFramer(_stream);

		}

	}

	public void onAuxData(String fourCC, IoBuffer buffer) {
		// TODO Auto-generated method stub

	}

	public void onConnected(String vidType, String audioType) {

		_fourCCAudio = audioType;
		_fourCCVideo = vidType;

		if (_content.equals("video")) {
			if (audioType.startsWith("AAC")) {
				AACAudio audioCodec = new AACAudio();
				_stream.setAudioReader(audioCodec);
				_stream.audioFramer = audioFramer;

			} else if (audioType.equals("MP3")) {
				//TODO
				//MP3Audio
			}

		}

	}

	public void onAudioData(int[] data) {
		if (_stream.getCodecReader().getName().equals("AAC")) {
			audioFramer.onAACData(data);
		} else if (_stream.getCodecReader().getName().equals("MP3")) {
			audioFramer.onMP3Data(data);
		}
	}

	public void onDisconnected() {
	}

	public void onMetaData(Map<String, Object> metaData) {
		_metaData = metaData;
		IRTMPEvent event = getMetaDataEvent();
		if (event != null) {
			_stream.setMetaDataEvent(event);
		}
		_stream.dispatchEvent(event);
	}

	private IRTMPEvent getMetaDataEvent() {
		if (_metaData == null) {
			return null;
		}

		IoBuffer buf = IoBuffer.allocate(1024);
		buf.setAutoExpand(true);
		Output out = new Output(buf);
		out.writeString("onMetaData");

		Map<Object, Object> props = new HashMap<Object, Object>();
		props.putAll(_metaData);
		props.put("canSeekToEnd", false);

		out.writeMap(props, new Serializer());
		buf.flip();

		return new Notify(buf);
	}

	public void onVideoData(int[] buffer) {
		if (_fourCCVideo.startsWith("VP6")) {
			videoFramer.pushVP6Frame(buffer, 0);
		} else if (_fourCCVideo.startsWith("H264")) {
			videoFramer.pushAVCFrame(buffer, 0);
		}
	}

	public void start() {
	}

	public void stop() {
	}

}
