package com.cheekymammoth.utils;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;
import com.cheekymammoth.animations.IAnimatable;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.events.IEventListener;

public final class FloatTweener implements IAnimatable, Poolable {
	public static final int EV_TYPE_FLOAT_TWEENER_CHANGED;
	public static final int EV_TYPE_FLOAT_TWEENER_COMPLETED;
	
	static {
		EV_TYPE_FLOAT_TWEENER_CHANGED = EventDispatcher.nextEvType();
		EV_TYPE_FLOAT_TWEENER_COMPLETED = EventDispatcher.nextEvType();
	}
	
	private boolean overflows;
    private boolean isInverted;
    private int tag = -1;
    private float from;
    private float to;
    private float value;
    private float prevValue;
    private float duration;
    private float totalDuration;
    private float durationRemainder;
    private Interpolation interpolation = Transitions.linear;
    private IEventListener listener;
    
    public FloatTweener() { }
    
	public FloatTweener(float startValue, Interpolation interpolation, IEventListener listener) {
        from = to = value = prevValue = startValue;
        totalDuration = 0.01f; // Will complete next frame
        this.interpolation = interpolation != null ? interpolation : Transitions.linear;
        this.listener = listener;
	}
	
	public static FloatTweener getTweener(Interpolation interpolation, IEventListener listener) {
		FloatTweener tweener = Pools.obtain(FloatTweener.class);
		tweener.setInterpolation(interpolation);
		tweener.setListener(listener);
		return tweener;
	}
	
	public boolean getOverflows() { return overflows; }
	
	public void setOverflows(boolean value) { overflows = value; }
	
	public boolean isInverted() { return isInverted; }
	
	public void setInverted(boolean value) { isInverted = value; }
	
	public boolean isDelaying() { return duration < 0; }
	
	public int getTag() { return tag; }
	
	public void setTag(int value) { tag = value; }

	@Override
	public boolean isComplete() {
		if (getOverflows())
			return !isDelaying() && value >= to && duration >= totalDuration;
		else
			return !isDelaying() && value == to && duration == totalDuration;
	}
	
	@Override
	public Object getTarget() { return listener; }
	
	public float getTweenedValue() { return value; }
	
	public float getFromValue() { return from; }
	
	public float getDeltaValue() { return value - prevValue; }
	
	public float getTotalDeltaValue() { return value - from; }
	
	public float getPercentComplete() { return totalDuration != 0 ? duration / totalDuration : 1f; }
	
	public float getDurationPassed() { return duration; }
	
	public float getTotalDuration() { return totalDuration; }
	
	public float getDurationRemaining() { return getTotalDuration() - getDurationPassed(); }
	
	public float getDurationRemainder() { return durationRemainder; }
	
	public void setDurationRemainder(float value) { durationRemainder = value; }
	
	public void setInterpolation(Interpolation value) {
		interpolation = value != null ? value : Transitions.linear;
	}
	
	public IEventListener getListener() { return listener; }
	
	public void setListener(IEventListener value) { listener = value; }
	
	
	public void resetTween(float value) {
		this.value = prevValue = from = to = value;
		duration = totalDuration = durationRemainder = 0;
	}
	
	public void resetTween(float from, float to, float duration, float delay) {
		this.from = from;
		this.to = to;
		this.duration = -delay;
		totalDuration = Math.max(0.01f, duration);
		durationRemainder = 0;
		if (duration >= 0) {
			this.value = this.prevValue = this.from;
		}
	}
	
	public void reverse() {
		resetTween(to, from, totalDuration, 0);
	}
	
	public void syncWithTweener(FloatTweener tweener) {
		if (tweener != null && tweener != this) {
			setInverted(tweener.isInverted());
			resetTween(tweener.from, tweener.to, tweener.totalDuration, 0);
			duration = tweener.duration; // This will incorporate any delay
			updateValue();
		}
	}
	
	public void forceCompletion() {
		if (!isComplete()) {
			duration = totalDuration;
			updateValue();
		}
	}
	
	private void setValue(float value) {
		this.prevValue = this.value;
		this.value = value;
	}
	
	private void updateValue() {
		if (duration == totalDuration) {
			setValue(to);
			
			if (listener != null) {
				listener.onEvent(EV_TYPE_FLOAT_TWEENER_CHANGED, this);
				listener.onEvent(EV_TYPE_FLOAT_TWEENER_COMPLETED, this);
			}
		} else if (duration >= 0 && totalDuration > 0) {
			float ratio = duration / totalDuration;
			float transitionValue = isInverted ? 1f - interpolation.apply(1f - ratio) : interpolation.apply(ratio);
			setValue(from + (to - from) * transitionValue);
			
			if (listener != null) {
				listener.onEvent(EV_TYPE_FLOAT_TWEENER_CHANGED, this);
				
				if (overflows && duration >= totalDuration)
					listener.onEvent(EV_TYPE_FLOAT_TWEENER_COMPLETED, this);
			}
		}
	}

	@Override
	public void advanceTime(float dt) {
		if (!isComplete()) {
			float newDuration = duration + dt;
			duration = overflows ? newDuration : Math.min(totalDuration, newDuration);
			durationRemainder = overflows || newDuration <= totalDuration ? 0 : newDuration - totalDuration;
			updateValue();
		}
	}

	@Override
	public void reset() {
		overflows = isInverted = false;
		tag = -1;
		value = prevValue = from = to = 0;
		duration = totalDuration = durationRemainder = 0;
		interpolation = Transitions.linear;
		listener = null;
	}
}
