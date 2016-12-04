package com.cheekymammoth.animations;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.events.IEventDispatcher;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.graphics.CMAtlasSprite;

public class MovieClip extends CMAtlasSprite implements IEventDispatcher, IAnimatable {
	public static final int EV_TYPE_MOVIE_COMPLETED;
	
	static {
		EV_TYPE_MOVIE_COMPLETED = EventDispatcher.nextEvType();
	}
	
	private float[] frameDurations;
	private float defaultFrameDuration;
	private float totalDuration;
	private float currentTime;
	private boolean loop;
	private boolean isPlaying;
	private int currentFrame;
	private Array<AtlasRegion> mFrames;
	private EventDispatcher dispatcher;
	
	public MovieClip(Array<AtlasRegion> frames, float fps) {
		super(frames.get(0));
		
		defaultFrameDuration = fps == 0.0f ? Float.MAX_VALUE : 1f / fps;
		loop = true;
		isPlaying = true;
		currentTime = 0f;
		currentFrame = 0;
		mFrames = new Array<AtlasRegion>(true, frames.size, AtlasRegion.class);
		frameDurations = new float[frames.size];
		for (int i = 0, n = frames.size; i < n; ++i)
			addFrame(frames.get(i), defaultFrameDuration);
		setFps(fps);
	}
	
	@Override
	public Object getTarget() {
		return null;
	}
	
	private void addFrame(AtlasRegion frame, float duration) {
		totalDuration += duration;
		frameDurations[mFrames.size] = duration;
		mFrames.add(frame);
	}
	
	public AtlasRegion getFrameAtIndex(int index) {
		return isValidIndex(index) ? mFrames.get(index) : null;
	}
	
	public void play() {
		isPlaying = true;
	}
	
	public void pause() {
		isPlaying = false;
	}
	
	public boolean isComplete() {
		return false;
	}
	
	float getDuration() {
		return totalDuration;
	}
	
	public boolean getLoop() {
		return loop;
	}
	
	public void setLoop(boolean value) {
		loop = value;
	}
	
	boolean isPlaying() {
		return isPlaying;
	}
	
	public int getNumFrames() {
		return mFrames.size;
	}
	
	public int getCurrentFrame() {
		return currentFrame;
	}
	
	public void setCurrentFrame(int index) {
		if (isValidIndex(index)) {
			currentFrame = index;
			currentTime = 0f;
			
			for (int i = 0; i < index; i++)
				currentTime += frameDurations[i];
			
			updateCurrentFrame();
		}
	}
	
	public float getFps() {
		return 1f / defaultFrameDuration;
	}
	
	public void setFps(float value) {
		float newFrameDuration = value == 0 ? Float.MAX_VALUE : 1f / value;
		float acceleration = newFrameDuration / defaultFrameDuration;
		currentTime *= acceleration;
		defaultFrameDuration = newFrameDuration;
		
		for (int i = 0, n = getNumFrames(); i < n; i++)
			setDurationAtIndex(getDurationAtIndex(i) * acceleration, i);
	}
	
	float getDurationAtIndex(int index) {
		return isValidIndex(index) ? frameDurations[index] : 0f;
	}
	
	void setDurationAtIndex(float value, int index) {
		if (value > 0 && isValidIndex(index)) {
			float oldValue = frameDurations[index];
			frameDurations[index] = value;
			totalDuration += value - oldValue;
		}
	}
	
	private void updateCurrentFrame() {
		this.setAtlasRegion(mFrames.get(currentFrame));
	}
	
	private boolean isValidIndex(int index) {
		return index >= 0 && index < mFrames.size;
	}
	
	public void advanceTime(float dt) {
		if (loop && currentTime == totalDuration) currentTime = 0f;
		if (!isPlaying || dt == 0f || currentTime == totalDuration) return;
		
		float durationSum = 0f;
		float previousTime = currentTime;
		float restTime = totalDuration - currentTime;
		float carryOverTime = dt > restTime ? dt - restTime : 0f;
		currentTime = Math.min(totalDuration, currentTime + dt);
		
		for (int i = 0, n = frameDurations.length; i < n; ++i) {
			float frameDuration = frameDurations[i];
			if (durationSum + frameDuration >= currentTime) {
				if (currentFrame != i) {
					currentFrame = i;
					updateCurrentFrame();
					//playCurrentSound();
				}
				break;
			}
			
			durationSum += frameDuration;
		}
		
		if (previousTime < totalDuration && currentTime == totalDuration && hasEventListenerForType(EV_TYPE_MOVIE_COMPLETED))
			dispatchEvent(EV_TYPE_MOVIE_COMPLETED, this);
		
		advanceTime(carryOverTime);
	}

	protected EventDispatcher getEventDispatcher() {
		if (dispatcher == null)
			dispatcher = new EventDispatcher();
		return dispatcher;
	}

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
