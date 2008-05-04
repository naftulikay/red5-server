package org.red5.server.core.rtmp;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RTMPStatus implements Serializable {
	private static final long serialVersionUID = -1046670173218417241L;
	
	public static final String LEVEL_SUCCESS = "error";
	public static final String LEVEL_WARNING = "warning";
	public static final String LEVEL_STATUS  = "status";
	
    /**
     * The NetConnection.call method was not able to invoke the server-side method or
     * command.
     */
	public static final String NC_CALL_FAILED = "NetConnection.Call.Failed";
    /**
     * The URI specified in the NetConnection.connect method did not
     * specify 'rtmp' as the protocol. 'rtmp' must be specified when connecting to
     * FMS and Red5. Either not supported version of AMF was used (3 when only 0 is supported)
     */
	public static final String NC_CALL_BADVERSION = "NetConnection.Call.BadVersion";
    /**
     * The application has been shut down (for example, if the application is out of
     * memory resources and must shut down to prevent the server from crashing) or the server has shut down.
     */
	public static final String NC_CONNECT_APPSHUTDOWN = "NetConnection.Connect.AppShutdown";
    /**
     * The connection was closed successfully
     */
	public static final String NC_CONNECT_CLOSED = "NetConnection.Connect.Closed";
    /**
     * The connection attempt failed.
     */
	public static final String NC_CONNECT_FAILED = "NetConnection.Connect.Failed";
    /**
     * The client does not have permission to connect to the application, the
     * application expected different parameters from those that were passed,
     * or the application name specified during the connection attempt was not found on
     * the server.
     */
	public static final String NC_CONNECT_REJECTED = "NetConnection.Connect.Rejected";
    /**
     * The connection attempt succeeded.
     */
	public static final String NC_CONNECT_SUCCESS = "NetConnection.Connect.Success";
    /**
     * The application name specified during connect is invalid.
     */
	public static final String NC_CONNECT_INVALID_APPLICATION = "NetConnection.Connect.InvalidApp";
	/**
	 * Invalid arguments were passed to a NetStream method.
	 */
	public static final String NS_INVALID_ARGUMENT = "NetStream.InvalidArg";
    /**
     * A recorded stream was deleted successfully.
     */
	public static final String NS_CLEAR_SUCCESS = "NetStream.Clear.Success";
    /**
     * A recorded stream failed to delete.
     */
	public static final String NS_CLEAR_FAILED = "NetStream.Clear.Failed";
    /**
     * An attempt to publish was successful.
     */
	public static final String NS_PUBLISH_START = "NetStream.Publish.Start";
    /**
     * An attempt was made to publish a stream that is already being published by someone else.
     */
	public static final String NS_PUBLISH_BADNAME = "NetStream.Publish.BadName";
    /**
     * An attempt to use a Stream method (at client-side) failed
     */
	public static final String NS_FAILED = "NetStream.Failed";
    /**
     * An attempt to unpublish was successful
     */
	public static final String NS_UNPUBLISHED_SUCCESS = "NetStream.Unpublish.Success";
    /**
     * Recording was started
     */
	public static final String NS_RECORD_START = "NetStream.Record.Start";
    /**
     * An attempt was made to record a read-only stream
     */
	public static final String NS_RECORD_NOACCESS = "NetStream.Record.NoAccess";
    /**
     * Recording was stopped
     */
	public static final String NS_RECORD_STOP = "NetStream.Record.Stop";
    /**
     * An attempt to record a stream failed
     */
	public static final String NS_RECORD_FAILED = "NetStream.Record.Failed";
    /**
     * Data is playing behind the normal speed
     */
	public static final String NS_PLAY_INSUFFICIENT_BW = "NetStream.Play.InsufficientBW";
    /**
     * Play was started
     */
	public static final String NS_PLAY_START = "NetStream.Play.Start";
    /**
     * An attempt was made to play a stream that does not exist
     */
	public static final String NS_PLAY_STREAMNOTFOUND = "NetStream.Play.StreamNotFound";
    /**
     * Play was stopped
     */
	public static final String NS_PLAY_STOP = "NetStream.Play.Stop";
    /**
     * An attempt to play back a stream failed
     */
	public static final String NS_PLAY_FAILED = "NetStream.Play.Failed";
    /**
     * A playlist was reset
     */
	public static final String NS_PLAY_RESET = "NetStream.Play.Reset";
    /**
     * The initial publish to a stream was successful. This message is sent to all subscribers
     */
	public static final String NS_PLAY_PUBLISHNOTIFY = "NetStream.Play.PublishNotify";
    /**
     * An unpublish from a stream was successful. This message is sent to all subscribers
     */
	public static final String NS_PLAY_UNPUBLISHNOTIFY = "NetStream.Play.UnpublishNotify";
    /**
     * Playlist playback switched from one stream to another.
     */
	public static final String NS_PLAY_SWITCH = "NetStream.Play.Switch";
    /**
     * Playlist playback is complete.
     */
	public static final String NS_PLAY_COMPLETE = "NetStream.Play.Complete";
    /**
     * The subscriber has used the seek command to move to a particular location in the recorded stream.
     */
	public static final String NS_SEEK_NOTIFY = "NetStream.Seek.Notify";
	/**
	 * The stream doesn't support seeking.
	 */
	public static final String NS_SEEK_FAILED = "NetStream.Seek.Failed";
    /**
     * The subscriber has used the seek command to move to a particular location in the recorded stream.
     */
	public static final String NS_PAUSE_NOTIFY = "NetStream.Pause.Notify";
    /**
     * Publishing has stopped
     */
	public static final String NS_UNPAUSE_NOTIFY = "NetStream.Unpause.Notify";
    /**
     *
     */
	public static final String NS_DATA_START = "NetStream.Data.Start";
	/**
	 * This event is sent if the player detects an MP4 with an invalid file structure. 
	 * Flash Player cannot play files that have invalid file structures.
	 * 
	 * New for FMS3
	 */
	public static final String NS_PLAY_FILE_STRUCTURE_INVALID = "NetStream.Play.FileStructureInvalid";

	/**
	 * This event is sent if the player does not detect any supported tracks. If there aren't any supported
	 * video, audio or data tracks found, Flash Player does not play the file.
	 * 
	 * New for FMS3
	 */
	public static final String NS_PLAY_NO_SUPPORTED_TRACK_FOUND = "NetStream.Play.NoSupportedTrackFound";
	/**
	 * Read access to a shared object was denied.
	 */
	public static final String SO_NO_READ_ACCESS = "SharedObject.NoReadAccess";
	/**
	 * Write access to a shared object was denied.
	 */
	public static final String SO_NO_WRITE_ACCESS = "SharedObject.NoWriteAccess";
	/**
	 * The creation of a shared object was denied.
	 */
	public static final String SO_CREATION_FAILED = "SharedObject.ObjectCreationFailed";
	/**
	 * The persistence parameter passed to SharedObject.getRemote() is different from the one used
	 * when the shared object was created.
	 */
	public static final String SO_PERSISTENCE_MISMATCH = "SharedObject.BadPersistence";

	private String code;
	private String level;
	private String description;
	private int streamId;
	private Map<String,Object> additionals = new HashMap<String,Object>();
	
	public RTMPStatus() {
		
	}
	
	public RTMPStatus(String code, String level, String description) {
		this.code = code;
		this.level = level;
		this.description = description;
	}
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getLevel() {
		return level;
	}
	
	public void setLevel(String level) {
		this.level = level;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public int getStreamId() {
		return streamId;
	}

	public void setStreamId(int streamId) {
		this.streamId = streamId;
	}

	/**
	 * Get additional parameter map.
	 * Connection status has "application" to point to the return
	 * value of rejectConnection().
	 * Stream status has "details" to point extra stream info.
	 * @return
	 */
	public Map<String, Object> getAdditionals() {
		return additionals;
	}
	
	public static RTMPStatus generateErrorResult(String code, Throwable error) {
		// Construct error object to return
		String message = "";
		if (error != null && error.getMessage() != null) {
			message = error.getMessage();
		}
		RTMPStatus status = new RTMPStatus(code, "error", message);
		if (error != null) {
			status.getAdditionals().put("application", error.getClass().getCanonicalName());
		}
		return status;
	}
}
