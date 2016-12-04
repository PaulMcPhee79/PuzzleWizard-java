package com.cheekymammoth.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.events.IEventDispatcher;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.graphics.CMAtlasSprite;
import com.cheekymammoth.graphics.CMSprite;

public class MovieReel extends CMSprite implements IEventDispatcher, IAnimatable {
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
	private Array<CMAtlasSprite> frames;
	private EventDispatcher dispatcher;
	
	public MovieReel(Array<AtlasRegion> frames, float fps) {
		super(frames.get(0));
		
		defaultFrameDuration = fps == 0.0f ? Float.MAX_VALUE : 1f / fps;
		loop = true;
		isPlaying = true;
		currentTime = 0f;
		currentFrame = 0;
		this.frames = new Array<CMAtlasSprite>(true, frames.size, CMAtlasSprite.class);
		frameDurations = new float[frames.size];
		for (int i = 0, n = frames.size; i < n; ++i)
			addFrame(new CMAtlasSprite(frames.get(i)), defaultFrameDuration);
		setFps(fps);
	}
	
	@Override
	public Object getTarget() {
		return null;
	}
	
	private void addFrame(CMAtlasSprite frame, float duration) {
		if (frames.size == 0) {
			setSize(frame.getWidth(), frame.getHeight());
			setOrigin(getWidth() / 2, getHeight() / 2);
		}
		
		totalDuration += duration;
		frameDurations[frames.size] = duration;
		frames.add(frame);
	}
	
	public CMAtlasSprite getFrameAtIndex(int index) {
		return isValidIndex(index) ? frames.get(index) : null;
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
		return frames.size;
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
		}
	}
	
	public float getCurrentTime() { return currentTime; }
	
	public void setCurrentTime(float value) {
		currentTime = value % totalDuration;
		updateCurrentFrame();
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
	
	private boolean isValidIndex(int index) {
		return index >= 0 && index < frames.size;
	}
	
	private void updateCurrentFrame() {
		float durationSum = 0f;
		for (int i = 0, n = frameDurations.length; i < n; ++i) {
			float frameDuration = frameDurations[i];
			if (durationSum + frameDuration >= currentTime) {
				if (currentFrame != i) {
					currentFrame = i;
				}
				break;
			}
			
			durationSum += frameDuration;
		}
	}
	
	public void advanceTime(float dt) {
		if (loop && currentTime == totalDuration) currentTime = 0f;
		if (!isPlaying || dt == 0f || currentTime == totalDuration) return;
		
		float previousTime = currentTime;
		float restTime = totalDuration - currentTime;
		float carryOverTime = dt > restTime ? dt - restTime : 0f;
		currentTime = Math.min(totalDuration, currentTime + dt);
		updateCurrentFrame();
		
		if (previousTime < totalDuration && currentTime == totalDuration && hasEventListenerForType(EV_TYPE_MOVIE_COMPLETED))
			dispatchEvent(EV_TYPE_MOVIE_COMPLETED, this);
		
		advanceTime(carryOverTime);
	}
	
	@Override
	public void draw (Batch batch) {
		this.draw(batch, 1f);
	}
	
	@Override
	public void draw (Batch batch, float alphaModulation) {
		if (!isVisible() || frames.size <= 0)
			return;
		
		float x = getX(), y = getY(), rot = getRotation(), scaleX = getScaleX(), scaleY = getScaleY();
		CMAtlasSprite frame = frames.get(getCurrentFrame());
		if (x != frame.getX() || y != frame.getY())
			frame.setPosition(x, y);
		if (rot != frame.getRotation())
			frame.setRotation(rot);
		if (scaleX != frame.getScaleX() || scaleY != frame.getScaleY())
			frame.setScale(scaleX, scaleY);
		Color color = getColor();
		color.a *= alphaModulation;
		frame.setColor(color);
		frame.draw(batch, alphaModulation);
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
