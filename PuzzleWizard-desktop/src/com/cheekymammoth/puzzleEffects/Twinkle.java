package com.cheekymammoth.puzzleEffects;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.cheekymammoth.actions.EventActions;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.graphics.CMAtlasSprite;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.utils.Transitions;

public class Twinkle extends Prop implements IEventListener, Poolable {
	public static final int EV_TYPE_ANIMATION_COMPLETED;
	private static final int EV_TYPE_ANIMATION_PEAKED;
	
	private static final float kRotateBy = 360f;
	private static final float kScaleMax = 0.625f;
	
	static {
		EV_TYPE_ANIMATION_COMPLETED = EventDispatcher.nextEvType();
		EV_TYPE_ANIMATION_PEAKED = EventDispatcher.nextEvType();
	}
	
	private boolean isAnimating;
	private float duration;
	private float durationPassed;
	private float animationScaleMax = kScaleMax;
	private float rotateBy = kRotateBy;
	private CMAtlasSprite icon;
	private IEventListener listener;
	
	public Twinkle() {
		this(-1, 0, 0);
	}
	
	public Twinkle(float x, float y) {
		this(-1, x, y);
	}

	public Twinkle(int category, float x, float y) {
		super(category);
		
		setTransform(true);
		setPosition(x, y);
		
		icon = new CMAtlasSprite(scene.textureRegionByName("twinkle"));
		icon.setPosition(-icon.getWidth()/2, -icon.getHeight()/2);
		addSpriteChild(icon);
		
		setContentSize(icon.getWidth(), icon.getHeight());
		//icon.setPosition(-getWidth()/2, -getHeight()/2);
		
		setScale(0);
		setRotation(0);
		getColor().a = 0;
	}

	public void setAnimationScaleMax(float value) { animationScaleMax = value; }
	
	public void setRotateBy(float value) { rotateBy = value; }
	
	public IEventListener getListener() { return listener; }
	
	public void setListener(IEventListener value) { listener = value; }
	
	public boolean isAnimating() { return isAnimating; }
	
	private void setAnimating(boolean value) {
		isAnimating = value;
		if (!value) {
			setScale(0);
			setRotation(0);
			getColor().a = 0;
		}
	}
	
	public void animate(float duration) {
		if (duration <= 0f)
			return;
		
		if (isAnimating())
			scene.removeFromJuggler(this);
		
		setAnimating(true);
		this.duration = duration;
		durationPassed = 0;
		setScale(0);
		setRotation(0);
		getColor().a = 0;
		
		animateIn(0.5f * duration, 0f);
		scene.addToJuggler(this);
	}
	
	private void animateIn(float duration, float fractionComplete) {
		float fractionRemaining = 1f - fractionComplete;
		addAction(Actions.parallel(
				Actions.rotateBy(fractionRemaining * rotateBy, duration),
				Actions.scaleTo(animationScaleMax, animationScaleMax, duration, Transitions.easeOut),
				Actions.alpha(1f, duration, Transitions.easeOut)));
		addAction(Actions.after(EventActions.eventAction(
				EV_TYPE_ANIMATION_PEAKED,
				null,
				this,
				0)));
	}
	
	private void animateOut(float duration, float fractionComplete) {
		float fractionRemaining = 1f - fractionComplete;
		addAction(Actions.parallel(
				Actions.rotateBy(fractionRemaining * rotateBy, duration),
				Actions.scaleTo(0f, 0f, duration, Transitions.easeIn)));
		addAction(Actions.after(EventActions.eventAction(
				EV_TYPE_ANIMATION_COMPLETED,
				null,
				this,
				0)));
	}
	
	public void fastForward(float duration) {
		float adjustedDurationPassed = durationPassed + duration;
		durationPassed = Math.min(adjustedDurationPassed, this.duration);
		if (adjustedDurationPassed >= this.duration) {
            stopAnimating();
            return;
        }
		
		setAnimating(false);
		clearActions();
		setAnimating(true);
		
		if (adjustedDurationPassed < 0.5f * this.duration) {
			float percentComplete = adjustedDurationPassed / (0.5f * this.duration);
			setScale(animationScaleMax * Transitions.easeOut.apply(percentComplete));
			getColor().a = Transitions.easeOut.apply(percentComplete);
			animateIn(0.5f * this.duration - adjustedDurationPassed, percentComplete);
		} else {
			float percentComplete = adjustedDurationPassed / this.duration;
			setScale(animationScaleMax * Transitions.easeIn.apply(percentComplete));
			animateOut(this.duration - adjustedDurationPassed, percentComplete);
		}
	}
	
	public void stopAnimating() {
		stopAnimating(false);
	}
	
	private void stopAnimating(boolean didComplete) {
		if (isAnimating()) {
			if (!didComplete)
				clearActions();
			scene.removeFromJuggler(this);
			setAnimating(false);
			if (listener != null)
				listener.onEvent(EV_TYPE_ANIMATION_COMPLETED, this);
		}
	}
	
	public void syncWithTwinkle(Twinkle twinkle) {
		if (twinkle == null)
			return;
		
		if (!isAnimating() && twinkle.isAnimating()) {
			scene.removeFromJuggler(this);
			setAnimating(false);
		} else if (!isAnimating() && twinkle.isAnimating()) {
			duration = twinkle.duration;
			setScaleX(twinkle.getScaleX());
			setScaleY(twinkle.getScaleY());
			setRotation(twinkle.getRotation());
			setColor(twinkle.getColor());
			
			setAnimating(true);
			scene.addToJuggler(this);
			fastForward(twinkle.durationPassed);
		}
	}

	@Override
	public void onEvent(int evType, Object evData) {
		if (evType == EV_TYPE_ANIMATION_PEAKED) {
			//clearActions();
			animateOut(0.5f * duration, 0f);
		} else if (evType == EV_TYPE_ANIMATION_COMPLETED) {
			stopAnimating(true);
		}
	}
	
	@Override
	public void advanceTime(float dt) {
		durationPassed += dt;
	}
	
	@Override
	public boolean isComplete() {
		return !isAnimating();
	}

	@Override
	public void reset() {
		setAnimationScaleMax(kScaleMax);
		setListener(null);
		setAnimating(false);
		clearActions();
		remove();
		scene.removeFromJuggler(this);
	}
}
