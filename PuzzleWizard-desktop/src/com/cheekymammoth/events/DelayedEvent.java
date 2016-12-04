package com.cheekymammoth.events;

import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.cheekymammoth.animations.IAnimatable;

public class DelayedEvent implements IAnimatable, Poolable {
	private int evType;
	private float delay;
	private Object evData;
	private IEventListener listener;
	
	public DelayedEvent() {
		delay = 0;
		evType = -1;
		evData = null;
		listener = null;
	}
	
	public DelayedEvent(float delay, int evType, Object evData, IEventListener listener) {
		this.delay = delay;
		this.evType = evType;
		this.evData = evData;
		this.listener = listener;
	}
	
	public static DelayedEvent getDelayedEvent(float delay, int evType, Object evData, IEventListener listener) {
		DelayedEvent delayedEvent = Pools.obtain(DelayedEvent.class);
		delayedEvent.delay = delay;
		delayedEvent.evType = evType;
		delayedEvent.evData = evData;
		delayedEvent.listener = listener;
		return delayedEvent;
	}
	
	public static void freeDelayedEvent(DelayedEvent delayedEvent) {
		if (delayedEvent != null)
			Pools.free(delayedEvent);
	}

	@Override
	public boolean isComplete() {
		return delay <= 0;
	}
	
	@Override
	public Object getTarget() {
		return listener;
	}

	@Override
	public void advanceTime(float dt) {
		if (delay > 0) {
			delay -= dt;
			if (delay <= 0)
			{
				if (listener != null)
					listener.onEvent(evType, evData);
				DelayedEvent.freeDelayedEvent(this);
			}
		}
	}

	@Override
	public void reset() {
		delay = 0;
		evType = -1;
		evData = null;
		listener = null;
	}
}
