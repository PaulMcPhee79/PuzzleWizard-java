package com.cheekymammoth.events;

public interface IEventDispatcher {
	public void addEventListener(int evType, IEventListener listener);
	public void removeEventListener(int evType, IEventListener listener);
	public void removeEventListeners(int evType);
	public boolean hasEventListenerForType(int evType);
	public void dispatchEvent(int evType, Object evData);
}
