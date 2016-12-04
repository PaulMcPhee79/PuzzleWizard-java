package com.cheekymammoth.animations;

public interface IAnimatable {
	public boolean isComplete();
	public Object getTarget();
	public void advanceTime(float dt);
}
