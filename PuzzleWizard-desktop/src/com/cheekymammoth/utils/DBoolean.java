package com.cheekymammoth.utils;

public class DBoolean {
	private boolean state;
	private boolean prevState;
	
	public DBoolean() {
		this(false, false);
	}
	
	public DBoolean(boolean state, boolean prevState) {
		this.state = state;
		this.prevState = prevState;
	}
	
	public void setState(boolean value) {
		prevState = state;
		state = value;
	}
	
	public void reset() {
		reset(false, false);
	}
	
	public void reset(boolean state, boolean prevState) {
		this.state = state;
		this.prevState = prevState;
	}
	
	public boolean isOn() { return state; }
	
	public boolean wasOn() { return prevState; }
	
	public boolean didTurnOn() { return prevState == false && state == true; } 

	public boolean didTurnOff() { return prevState == true && state == false; }
}
