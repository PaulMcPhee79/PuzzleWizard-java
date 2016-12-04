package com.cheekymammoth.input;

import com.cheekymammoth.utils.DBoolean;

public class Button {
	private boolean isDirty; // Prevents multiple events per frame from masking state changes
	private DBoolean state = new DBoolean();
	
	public Button() { }
	
	public void reset() {
		state.reset();
		isDirty = false;
	}
	
	public void update() {
		setState(isPressed());
		isDirty = false;
	}
	
	public void setState(boolean value) {
		if (!isDirty || value != isPressed())
		state.setState(value);
		isDirty = true;
	}
	
	public boolean isPressed() { return state.isOn(); }
	
	public boolean wasPressed() { return state.wasOn(); }
	
	public boolean didDepress() { return wasPressed() == false && isPressed() == true; } 

	public boolean didRaise() { return wasPressed() == true && isPressed() == false; }

}
