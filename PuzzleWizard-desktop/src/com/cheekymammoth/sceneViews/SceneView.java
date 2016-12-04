package com.cheekymammoth.sceneViews;

import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.events.IEventDispatcher;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.sceneControllers.SceneController;

public abstract class SceneView implements IEventDispatcher, IEventListener {
	private EventDispatcher dispatcher;
	
	public SceneView() {
		
	}
	
	public void setupView() {
		
	}
	
	protected SceneController getController() {
		return null;
	}
	
	public void attachEventListeners() { }
	
	public void detachEventListeners() { }
	
	public void resize(float width, float height) { }
	
	public void advanceTime(float dt) { }
	
	protected EventDispatcher getEventDispatcher() {
		if (dispatcher == null)
			dispatcher = new EventDispatcher();
		return dispatcher;
	}
	
	protected void bubbleEvent(int evType, Object evData) {
		SceneController scene = getController();
		if (scene != null)
			scene.onEvent(evType, evData);
	}
	
	@Override
	public void onEvent(int evType, Object evData) { }

	@Override
	public void addEventListener(int evType, IEventListener listener) {
		EventDispatcher dispatcher = getEventDispatcher();
		if (dispatcher != null)
			dispatcher.addEventListener(evType, listener);
	}
	
	@Override
	public void removeEventListener(int evType, IEventListener listener) {
		EventDispatcher dispatcher = getEventDispatcher();
		if (dispatcher != null)
			dispatcher.removeEventListener(evType, listener);
	}
	
	@Override
	public void removeEventListeners(int evType) {
		EventDispatcher dispatcher = getEventDispatcher();
		if (dispatcher != null)
			dispatcher.removeEventListeners(evType);
	}
	
	@Override
	public boolean hasEventListenerForType(int evType) {
		EventDispatcher dispatcher = getEventDispatcher();
		return dispatcher != null && dispatcher.hasEventListenerForType(evType);
	}
	
	@Override
	public void dispatchEvent(int evType, Object evData) {
		EventDispatcher dispatcher = getEventDispatcher();
		if (dispatcher != null)
			dispatcher.dispatchEvent(evType, evData);
	}
	
	public void dispatchEvent(int evType) {
		dispatchEvent(evType, null);
	}
}
