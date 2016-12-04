package com.cheekymammoth.puzzleEffects;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.graphics.ICustomRenderer;
import com.cheekymammoth.graphics.Prop;

public class Dissolver extends Prop {
	public static final int EV_TYPE_DISSOLVE_CYCLE_COMPLETED;
	
	static {
		EV_TYPE_DISSOLVE_CYCLE_COMPLETED = EventDispatcher.nextEvType();
	}
	
	private int requestedDir;
	private int dir;
	private float minThreshold;
	private float maxThreshold;
	private float threshold;
	private IEventListener listener;
	
	public Dissolver() {
		this(-1);
	}
	
	public Dissolver(int category) {
		this(category, 1, 0f, 1f);
	}
	
	public Dissolver(int category, int dir, float minThreshold, float maxThreshold) {
		super(category);
		
		this.requestedDir = dir;
		this.dir = dir != 0 ? dir : 1;
		this.minThreshold = minThreshold;
		this.maxThreshold = maxThreshold;
		this.threshold = dir == 1 ? minThreshold : maxThreshold;
		
		setShader(scene.shaderByName("dissolve"));
		setCustomRenderer(new ICustomRenderer() {
			@Override
			public void preDraw(Batch batch, float parentAlpha, Object obj) {
				Prop prop = (Prop)obj;
				ShaderProgram shader = prop.getShader();
				batch.setShader(shader);
				scene.applyShaderDesciptor("dissolve");
				shader.setUniformf("u_threshold", threshold);
			}
			
			@Override
			public void postDraw(Batch batch, float parentAlpha, Object obj) {
				batch.setShader(null);
			}
		});
	}
	
	public float getThreshold() { return threshold; }
	
	public void setThreshold(float value) { threshold = value; }
	
	public IEventListener getListener() { return listener; }
	
	public void setListener(IEventListener value) { listener = value; }

	public void addDissolvee(Actor dissolvee) {
		if (dissolvee != null)
			addActor(dissolvee);
	}
	
	public void removeDissolvee(Actor dissolvee) {
		if (dissolvee != null)
			removeActor(dissolvee);
	}
	
	public void clearDissolvees() {
		clearChildren();
	}
	
	public void reset() {
		dir = requestedDir == 0 ? 1 : requestedDir;
		threshold = dir == 1 ? minThreshold : maxThreshold;
	}
	
	@Override
	public boolean isComplete() {
		return (dir == 1 && threshold == maxThreshold) || (dir == -1 && threshold == minThreshold);
	}
	
	@Override
	public void advanceTime(float dt) {
		if ((dir > 0 && threshold == maxThreshold) || (dir < 0 && threshold == minThreshold)) {
			if (listener != null)
				listener.onEvent(EV_TYPE_DISSOLVE_CYCLE_COMPLETED, this);
			if (requestedDir == 0)
				dir *= -1;
		}
		
		threshold += dir * dt;
		
		if (dir > 0 && threshold > maxThreshold)
			threshold = maxThreshold;
		else if (dir < 0 && threshold < minThreshold)
			threshold = minThreshold;
	}
}
