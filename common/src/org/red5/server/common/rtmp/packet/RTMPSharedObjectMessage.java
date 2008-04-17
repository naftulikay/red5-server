package org.red5.server.common.rtmp.packet;

import java.util.List;

public class RTMPSharedObjectMessage extends RTMPPacket {
	private String name;
	private int version;
	private boolean persistent;
	private int unknown;
	private List<RTMPSharedObject> sharedObjects;

	public RTMPSharedObjectMessage() {
		super(TYPE_RTMP_SHARED_OBJECT);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public boolean isPersistent() {
		return persistent;
	}

	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	public int getUnknown() {
		return unknown;
	}

	public void setUnknown(int unknown) {
		this.unknown = unknown;
	}

	public List<RTMPSharedObject> getSharedObjects() {
		return sharedObjects;
	}

	public void setSharedObjects(List<RTMPSharedObject> sharedObjects) {
		this.sharedObjects = sharedObjects;
	}

}
