package net.sziebert.red5.adapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.red5.server.api.IConnection;
import org.red5.server.stream.ClientBroadcastStream;

/**
 * <code>StreamManager</code> provides services for snapshotting and recording
 * the broadcast stream.
 */
public class StreamManager {
	private static final Log log = LogFactory.getLog(StreamManager.class);

	// Application components
	@SuppressWarnings("unused")
	private Application app;

	private ClientBroadcastStream broadcastStream;

	/**
	 * Start recording the publishing stream for the specified
	 * <code>IConnection</code>.
	 *
	 * @param conn
	 */
	public void recordShow(IConnection conn) {
		log.debug("Recording show for: " + conn.getScope().getContextPath());
		// Create the recorded stream name.
		String streamName = conn.getScope().getContextPath() + "-"
				+ System.currentTimeMillis();
		// get a ref to the broadcasting stream
		broadcastStream = (ClientBroadcastStream) app.getBroadcastStream(conn
				.getScope(), "hostStream");
		try {
			broadcastStream.saveAs(streamName, false);
		} catch (Exception e) {
			log.error("Error saving stream", e);
		}
	}

	/**
	 * Stops recording the publishing stream for the specifed
	 * <code>IConnection</code>.
	 *
	 * @param conn
	 */
	public void stopRecordingShow(IConnection conn) {
		log.debug("Stop recording show for: "
				+ conn.getScope().getContextPath());
		// Get a reference to the previously created server stream
		// and tell it to stop recording.
		broadcastStream.stopRecording();
	}

	/* ----- Spring injected dependencies ----- */

	public void setApplication(Application app) {
		this.app = app;
	}
}
