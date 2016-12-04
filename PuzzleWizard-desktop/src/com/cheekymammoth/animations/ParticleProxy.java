package com.cheekymammoth.animations;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.utils.Disposable;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.events.IEventListener;

public class ParticleProxy implements Disposable {
	public static final int EV_TYPE_DELAY_DID_EXPIRE;
	
	static {
		EV_TYPE_DELAY_DID_EXPIRE = EventDispatcher.nextEvType();
	}
	
	private float delay;
	private ParticleEffect particleEffect;
	private IEventListener listener;
	
	public ParticleProxy(ParticleEffect effect, IEventListener eventListener) {
		assert(particleEffect != null) : "ParticleProxy::ctor requires non-null ParticleEffect.";
		particleEffect = effect;
		listener = eventListener;
	}
	
	@Override
	public void dispose() {
		particleEffect.dispose();
	}
	
	public ParticleEffect getParticleEffect() {
		return particleEffect;
	}
	
	public boolean isComplete () {
		return delay <= 0 && particleEffect.isComplete();
	}
	
	public void setDelay(float value) {
		delay = value;
	}
	
	public void setPosition(float x, float y) {
		particleEffect.setPosition(x, y);
	}
	
	public void reset () {
		particleEffect.reset();
	}

	public void update (float delta) {
		if (delay > 0) {
			delay -= delta;
			if (delay <= 0)
			{
				delta = -delay;
				if (listener != null)
					listener.onEvent(EV_TYPE_DELAY_DID_EXPIRE, this);
			}
		}
		
		if (delay <= 0)
			particleEffect.update(delta);
	}
}
