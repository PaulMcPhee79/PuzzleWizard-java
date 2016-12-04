package com.cheekymammoth.events;

import com.cheekymammoth.utils.SnapshotSet;
import com.badlogic.gdx.utils.IntMap;

public class EventDispatcher implements IEventDispatcher {
	public static int EV_TYPE_ALL = -1;
	private static int s_evType = 1;
	private IntMap<SnapshotSet<IEventListener>> mEventListeners;
	
	public static int nextEvType() {
		return s_evType++;
	}
	
	public EventDispatcher() {
		
	}
	
	public void addEventListener(int evType, IEventListener listener) {
		if (listener == null)
			return;
		
		if (mEventListeners == null)
			mEventListeners = new IntMap<SnapshotSet<IEventListener>>();
		
		SnapshotSet<IEventListener> listeners = mEventListeners.get(evType);
		if (listeners == null) {
			listeners = new SnapshotSet<IEventListener>(true, 2, IEventListener.class);
			mEventListeners.put(evType, listeners);
		}
		
		listeners.add(listener);
	}
	
	public void removeEventListener(int evType, IEventListener listener) {
		if (listener == null || mEventListeners == null)
			return;
		
		SnapshotSet<IEventListener> listeners = mEventListeners.get(evType);
		if (listeners != null)
			listeners.removeValue(listener, true);
	}
	
	public void removeEventListeners(int evType) {
		if (mEventListeners == null)
			return;
		
		SnapshotSet<IEventListener> listeners = mEventListeners.get(evType);
		if (listeners != null)
			listeners.clear();
	}
	
	public boolean hasEventListenerForType(int evType) {
		if (mEventListeners != null) {
			SnapshotSet<IEventListener> listeners = mEventListeners.get(evType);
			return listeners != null && listeners.size > 0;
		}
		else
			return false;
	}
	
	public void dispatchEvent(int evType) {
		dispatchEvent(evType, null);
	}
	
	public void dispatchEvent(int evType, Object evData) {
		if (mEventListeners == null)
			return;
		
		SnapshotSet<IEventListener> listeners = mEventListeners.get(evType);
		if (listeners != null) {
			IEventListener[] items = listeners.begin();
			for (int i = 0; i < listeners.size; i++)
				items[i].onEvent(evType, evData);
			listeners.end();
		}
		
		listeners = mEventListeners.get(EV_TYPE_ALL);
		if (listeners != null) {
			IEventListener[] items = listeners.begin();
			for (int i = 0; i < listeners.size; i++)
				items[i].onEvent(evType, evData);
			listeners.end();
		}
	}
}
