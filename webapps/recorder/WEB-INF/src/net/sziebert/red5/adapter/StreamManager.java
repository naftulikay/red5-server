package net.sziebert.red5.adapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.red5.server.api.IConnection;
import org.red5.server.api.stream.IServerStream;
import org.red5.server.api.stream.support.SimplePlayItem;
import org.red5.server.api.stream.support.StreamUtils;

/**
 * <code>StreamManager</code> provides services for snapshotting and recording
 * the broadcast stream.
 */
public class StreamManager {
	private static final Log log = LogFactory.getLog(StreamManager.class);

	// Application components
	@SuppressWarnings("unused")
	private Application app;

	/**
	 * Start recording the publishing stream for the specified
	 * <code>IConnection</code>.
	 *
	 * @param conn
	 */
	public void recordShow(IConnection conn) {
		log.debug("Recording show for: " + conn.getScope().getContextPath());
		// Create the recorded stream name.
		String streamName = conn.getScope().getContextPath() + "-" + 1;
		// Create the server stream.
		IServerStream stream = StreamUtils.createServerStream(conn.getScope(),
				streamName);
		// Create the play list and play the publishing stream into it.
		SimplePlayItem item = new SimplePlayItem();
		//		item.setName("hostStream");
		//		stream.addItem(item);
		//		stream.start();
		//		// Set the name of the recorded stream.
		//		stream.setPublishedName(streamName);
		// TODO: Tell the server stream to record itself.
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
		// Create the recorded stream name.
		String streamName = conn.getScope().getContextPath() + "-" + 1;
		// Get the server stream.
		IServerStream stream = StreamUtils.getServerStream(conn.getScope(),
				streamName);
		// TODO: Get a reference to the previously created server stream
		// and tell it to stop recording.
	}

	/* ----- Spring injected dependencies ----- */

	public void setApplication(Application app) {
		this.app = app;
	}
}
