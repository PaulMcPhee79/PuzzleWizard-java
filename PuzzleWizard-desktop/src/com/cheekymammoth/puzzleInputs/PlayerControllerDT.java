package com.cheekymammoth.puzzleInputs;

import com.cheekymammoth.input.CMInputs;
import com.cheekymammoth.utils.Coord;

public class PlayerControllerDT extends PlayerController {
	private Coord movementVector = new Coord();
	
	PlayerControllerDT() { }
	
	@Override
	public int getInputFocus() {
		return CMInputs.HAS_FOCUS_BOARD;
	}
	
	@Override
	public void willLoseFocus() {
		reset();
	}
	
	@Override
	public void update(CMInputs input) {
		movementVector.set(input.getHeldVector());
	}
	
	@Override
	public void advanceTime(float dt) {
		if (isEnabled() && player != null)
			player.setQueuedMove(movementVector);
	}
	
	@Override
	public void reset() {
		super.reset();
		movementVector.set(0, 0);
	}
	
	public Coord getMovementVector() { return movementVector; }

	@Override
	public void didTreadmill() {
		// TODO Auto-generated method stub
		
	}

}
