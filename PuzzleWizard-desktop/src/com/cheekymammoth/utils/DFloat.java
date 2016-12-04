package com.cheekymammoth.utils;

public class DFloat {
	private float value;
	private float prevValue;
	
	public DFloat() {
		this(0, 0);
	}
	
	public DFloat(float value, float prevValue) {
		this.value = value;
		this.prevValue = prevValue;
	}
	
	public float getValue() { return value; }
	
	public float getPrevValue() { return prevValue; }
	
	public void setValue(float value) {
		prevValue = this.value;
		this.value = value;
	}
	
	public void reset() {
		reset(0, 0);
	}
	
	public void reset(float value, float prevValue) {
		this.value = value;
		this.prevValue = prevValue;
	}
}
