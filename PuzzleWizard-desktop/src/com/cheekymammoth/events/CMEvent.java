package com.cheekymammoth.events;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.utils.Pools;

public class CMEvent extends Event {
	private int evType;
	
	public CMEvent() {
		this(0, null);
	}
	
	public CMEvent(int evType, Actor target) {
		this.evType = evType;
		setTarget(target);
	}
	
	public static CMEvent getEvent(int evType, Actor target) {
		CMEvent event = Pools.obtain(CMEvent.class);
		event.evType = evType;
		event.setTarget(target);
		return event;
	}
	
	public static void freeEvent(CMEvent event) {
		if (event != null)
			Pools.free(event);
	}
	
	public int getEvType() { return evType; }
	
	@Override
	public void reset() {
		setTarget(null);
	}
}
