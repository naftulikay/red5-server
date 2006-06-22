package org.red5.server.net.rtmp.event;

public class ChunkSize extends BaseEvent {
		
	protected byte EVENT_DATATYPE = TYPE_CHUNK_SIZE;
	private int size = 0;
	
	public ChunkSize(int size){
		super(Type.SYSTEM);
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	protected void doRelease() {
		size = 0;
	}
	
	public String toString(){
		return "ChunkSize: "+size;
	}
	
	public boolean equals(Object obj){
		if(!(obj instanceof ChunkSize)) return false;
		final ChunkSize other = (ChunkSize) obj;
		return getSize() == other.getSize();
	}
	
}