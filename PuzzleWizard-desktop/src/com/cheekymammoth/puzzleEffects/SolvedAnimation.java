package com.cheekymammoth.puzzleEffects;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;
import com.cheekymammoth.actions.EventActions;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.graphics.CMAtlasSprite;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.utils.Utils;

public class SolvedAnimation extends Prop implements IEventListener, Poolable {
	public static final int EV_TYPE_ANIMATION_COMPLETED;
	private static final int EV_TYPE_ROTATION_COMPLETED;
	private static final int EV_TYPE_FADE_COMPLETED;
	private static final float kTwinkleDuration = 1f;
	
	static {
		EV_TYPE_ANIMATION_COMPLETED = EventDispatcher.nextEvType();
		EV_TYPE_ROTATION_COMPLETED = EventDispatcher.nextEvType();
		EV_TYPE_FADE_COMPLETED = EventDispatcher.nextEvType();
	}
	
	private boolean isAnimating;
	private Twinkle twinkle;
	private Prop twinkleScaler;
	private CMAtlasSprite key;
	private SequenceAction animSeqAction;
	private SequenceAction fadeSeqAction;
	private IEventListener listener;
	
	public SolvedAnimation() {
		this(-1, null);
	}

	public SolvedAnimation(int category, IEventListener listener) {
		super(category);
		
		this.listener = listener;
		setTransform(true);
		
		key = new CMAtlasSprite(scene.textureRegionByName("solved-key"));
		key.centerContent();
		addSpriteChild(key);
		setContentSize(key.getWidth(), key.getHeight());
		
		twinkle = new Twinkle();
		twinkle.setPosition(0, 0);
		twinkle.setRotateBy(450);
		twinkle.setListener(this);
		
		twinkleScaler = new Prop();
		twinkleScaler.setTransform(true);
		twinkleScaler.setPosition(key.getWidth() / 3, 10);
		twinkleScaler.setScale(2.25f);
		twinkleScaler.addActor(twinkle);
		addActor(twinkleScaler);
	}
	
	public boolean isAnimating() { return isAnimating; }
	
	private void setAnimating(boolean value) { isAnimating = value; }
	
	public IEventListener getListener() { return listener; }
	
	public void setListener(IEventListener value) { listener = value; }

	public void animate(float fromX, float fromY, float toX, float toY, float duration, float delay) {
		if (isAnimating())
			return;
		
		setRotation(35);
		setScale(128f / key.getWidth());
		setPosition(fromX, fromY);
		setColor(Utils.setA(getColor(), 1f));
		
		animSeqAction = Actions.sequence(
				Actions.delay(delay),
				Actions.parallel(
						Actions.moveTo(toX, toY, duration),
						Actions.scaleTo(1f, 1f, duration),
						Actions.rotateTo(295, duration)),
				EventActions.eventAction(EV_TYPE_ROTATION_COMPLETED, this));
		addAction(animSeqAction);
		setAnimating(true);
	}
	
	public void stopAnimating() {
		if (animSeqAction != null) {
			removeAction(animSeqAction);
			Pools.free(animSeqAction);
		}
		
		if (fadeSeqAction != null) {
			removeAction(fadeSeqAction);
			Pools.free(fadeSeqAction);
		}
		
		if (twinkle != null)
			twinkle.stopAnimating();
		
		setAnimating(false);
	}
	
	@Override
	public void onEvent(int evType, Object evData) {
		if (evType == EV_TYPE_ROTATION_COMPLETED) {
			if (twinkle != null)
				twinkle.animate(kTwinkleDuration);
		} else if (evType == Twinkle.EV_TYPE_ANIMATION_COMPLETED) {
			fadeSeqAction = Actions.sequence(
					Actions.delay(1.5f),
					Actions.fadeOut(0.5f),
					EventActions.eventAction(EV_TYPE_FADE_COMPLETED, this));
			addAction(fadeSeqAction);
		} else if (evType == EV_TYPE_FADE_COMPLETED) {
			if (isAnimating()) {
				setAnimating(false);
				if (listener != null)
					listener.onEvent(EV_TYPE_ANIMATION_COMPLETED, this);
			}
		}
	}

	@Override
	public void reset() {
		stopAnimating();
		setListener(null);
	}
}
