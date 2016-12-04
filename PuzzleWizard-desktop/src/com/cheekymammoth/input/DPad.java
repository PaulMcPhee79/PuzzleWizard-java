package com.cheekymammoth.input;

import com.badlogic.gdx.controllers.PovDirection;
import com.cheekymammoth.utils.DBoolean;

public class DPad {
	private boolean isDirty; // Prevents multiple events per frame from masking state changes
	private final DBoolean povState = new DBoolean();
	private PovDirection povDir = PovDirection.center;
	
	public DPad() { }
	
	public PovDirection getDepressedDir() {
		return didDPadDepress() ? povDir : PovDirection.center;
	}
	
	public PovDirection getPressedDir() {
		return isDPadPressed() ? povDir : PovDirection.center;
	}
	
	public void setPovDirection(PovDirection povDir) {
		if (povDir == null || (isDirty && povDir == this.povDir))
			return;
		
		if (this.povDir == povDir) {
			povState.setState(povState.isOn());
		} else {
			this.povDir = povDir;
			
			if (povDir == PovDirection.center)
				povState.setState(false);
			else {
				povState.reset();
				povState.setState(true);
			}
		}
		isDirty = true;
	}
	
	public void reset() {
		povState.reset();
		povDir = PovDirection.center;
		isDirty = false;
	}
	
	public void update() {
		isDirty = false;
		setPovDirection(povDir);
		isDirty = false;
	}
	
	public boolean isDPadPressed() {
		return povDir != PovDirection.center && povState.isOn();
	}
	
	public boolean didDPadDepress() {
		return povDir != PovDirection.center && povState.didTurnOn();
	}
	
	public boolean didDPadRelease() {
		return povDir != PovDirection.center && povState.didTurnOff();
	}
}
