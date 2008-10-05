package org.red5.server.stream;

import org.red5.server.net.rtmp.event.IRTMPEvent;
import org.red5.server.net.rtmp.message.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamTracker implements Constants {

    private static final Logger log = LoggerFactory.getLogger(StreamTracker.class);	
    
	/**
     * Last audio flag
     */
	private int lastAudio;
    /**
     * Last video flag
     */
	private int lastVideo;
    /**
     * Last notification flag
     */
	private int lastNotify;
    /**
     * Relative flag
     */
	private boolean relative;
    /**
     * First video flag
     */
	private boolean firstVideo;
    /**
     * First audio flag
     */
	private boolean firstAudio;
    /**
     * First notification flag
     */
	private boolean firstNotify;

	/** Constructs a new StreamTracker. */
    public StreamTracker() {
		reset();
	}

    /**
     * Reset state
     */
    public void reset() {
    	log.debug("reset");
		lastAudio = 0;
		lastVideo = 0;
		lastNotify = 0;
		firstVideo = true;
		firstAudio = true;
		firstNotify = true;
	}

    /**
     * RTMP event handler
     * @param event      RTMP event
     * @return           Timeframe since last notification (or audio or video packet sending)
     */
    public int add(IRTMPEvent event) {
    	log.debug("add - firstAudio: {} firstVideo: {} firstNotify: {}", new Object[]{firstAudio, firstVideo, firstNotify});
    	log.debug("timestamps - lastAudio: {} lastVideo: {} lastNotify: {}", new Object[]{lastAudio, lastVideo, lastNotify});
    	relative = true;
		int timestamp = event.getTimestamp();
    	log.debug("timestamp: {}", timestamp);
		int tsOut = 0;

		switch (event.getDataType()) {

			case TYPE_AUDIO_DATA:
			case TYPE_VIDEO_DATA:
			case TYPE_NOTIFY:
			case TYPE_INVOKE:
				if (firstAudio) {
					tsOut = timestamp;
					relative = false;
					firstAudio = false;
				} else {
					tsOut = timestamp - lastAudio;
				}
				if (tsOut < 0) {
					tsOut = lastAudio;
				} else {
					//dont update timestamp if ts was negative
					lastAudio = timestamp;
				}
				break;

			default:
				// ignore other types
				break;

		}
    	log.debug("timestamp out: {}", tsOut);
		return tsOut;
	}

	/**
     * Getter for property 'relative'.
     *
     * @return Value for property 'relative'.
     */
    public boolean isRelative() {
		return relative;
	}
}
