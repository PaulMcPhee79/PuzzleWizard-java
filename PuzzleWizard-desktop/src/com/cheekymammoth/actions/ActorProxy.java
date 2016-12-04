package com.cheekymammoth.actions;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.events.IEventListener;

public class ActorProxy extends Actor {
	public static final int EV_TYPE_VALUE_CHANGED;
	
	static {
		EV_TYPE_VALUE_CHANGED = EventDispatcher.nextEvType();
	}
	
	private IEventListener listener;
	
	public ActorProxy() {
		this(null);
	}
	
	public ActorProxy(IEventListener listener) {
		this.listener = listener;
	}
	
	public IEventListener getListener() { return listener; }
	
	public void setListener(IEventListener value) { listener = value; }
	
	@Override
	public void setPosition (float x, float y) {
        super.setPosition(x, y);
        proxyNotify();
	}
	
	protected void proxyNotify() {
		if (listener != null)
			listener.onEvent(EV_TYPE_VALUE_CHANGED, this);
	}
}
