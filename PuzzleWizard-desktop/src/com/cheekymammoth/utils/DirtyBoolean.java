package com.cheekymammoth.utils;

public class DirtyBoolean extends DBoolean {
	private boolean isDirty; // Prevents multiple events per frame from masking state changes
	
	public void setState(boolean value) {
		if (!isDirty || value != isOn())
			super.setState(value);
		isDirty = true;
	}
	
	public void reset(boolean state, boolean prevState) {
		super.reset(state, prevState);
		isDirty = false;
	}
	
	public void update() {
		isDirty = false;
	}
}
