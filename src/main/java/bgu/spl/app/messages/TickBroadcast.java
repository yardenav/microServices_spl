package bgu.spl.app.messages;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast{

	Integer tick;

	public TickBroadcast(int tick) {
		super();
		this.tick = tick;
	}

	public int getTick() {
		return tick;
	}
	
	
}
