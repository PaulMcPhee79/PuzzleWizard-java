package com.cheekymammoth.input;

import com.badlogic.gdx.controllers.PovDirection;
import com.cheekymammoth.utils.Coord;

public class Xbox360MacPad implements GamePadDescriptor {
	public static final int BUTTON_DASHBOARD = 10;
	public static final int BUTTON_X = 13;
	public static final int BUTTON_Y = 14;
	public static final int BUTTON_A = 11;
	public static final int BUTTON_B = 12;
	public static final int BUTTON_BACK = 5;
	public static final int BUTTON_START = 4;
	public static final PovDirection BUTTON_DPAD_UP = PovDirection.north; // 0
	public static final PovDirection BUTTON_DPAD_DOWN = PovDirection.south; // 1
	public static final PovDirection BUTTON_DPAD_RIGHT = PovDirection.east; // 3
	public static final PovDirection BUTTON_DPAD_LEFT = PovDirection.west; // 2
	public static final int BUTTON_LB = 8;
	public static final int BUTTON_L3 = 6;
	public static final int BUTTON_RB = 9;
	public static final int BUTTON_R3 = 7;
	public static final int AXIS_LEFT_X = 2; //-1 is left | +1 is right
	public static final int AXIS_LEFT_Y = 3; //-1 is up | +1 is down
	public static final int AXIS_LEFT_TRIGGER = 0; //value -1 to 1
	public static final int AXIS_RIGHT_X = 4; //-1 is left | +1 is right
	public static final int AXIS_RIGHT_Y = 5; //-1 is up | +1 is down
	public static final int AXIS_RIGHT_TRIGGER = 1; //value -1 to 1
	
	@Override
	public boolean isMovementAxis(int axisIndex) {
		return axisIndex == AXIS_LEFT_X || axisIndex == AXIS_LEFT_Y ||
				axisIndex == AXIS_RIGHT_X || axisIndex == AXIS_RIGHT_Y;
	}
	
	private Coord axesCoordCache = new Coord();
	@Override
	public Coord axesForAxisIndex(int axisIndex) {
		switch(axisIndex) {
			case AXIS_LEFT_Y:
			case AXIS_LEFT_X:
				return axesCoordCache.set(AXIS_LEFT_X, AXIS_LEFT_Y);
			case AXIS_RIGHT_X:
			case AXIS_RIGHT_Y:
				return axesCoordCache.set(AXIS_RIGHT_X, AXIS_RIGHT_Y);
			default:
				return axesCoordCache.set(-1, -1);
		}
	}
	
	@Override
	public PovDirection button2PovDir(int buttonCode) {
		switch (buttonCode) {
			case 0: return BUTTON_DPAD_UP;
			case 1: return BUTTON_DPAD_DOWN;
			case 2: return BUTTON_DPAD_LEFT;
			case 3: return BUTTON_DPAD_RIGHT;
			default: return null;
		}
	}

	@Override
	public int normalizeButton(int buttonCode) {
		switch(buttonCode) {
			case BUTTON_A: return CMInputs.CI_CONFIRM;
			case BUTTON_B: return CMInputs.CI_CANCEL;
			case BUTTON_BACK: return CMInputs.CI_CANCEL;
			case BUTTON_START: return CMInputs.CI_MENU;
			case BUTTON_LB: return CMInputs.CI_PREV_SONG;
			case BUTTON_RB: return CMInputs.CI_NEXT_SONG;
			default: return CMInputs.CI_NONE;
		}
	}
	
	@Override
	public int normalizePov(PovDirection povDir) {
		switch(povDir) {
			case north: return CMInputs.CI_UP;
			case south: return CMInputs.CI_DOWN;
			case east: return CMInputs.CI_RIGHT;
			case west: return CMInputs.CI_LEFT;
			default: return CMInputs.CI_NONE;
		}
	}
	
	@Override
	public int normalizeAxis(int axisIndex, int axisDir) {
		if (axisDir == 0)
			return CMInputs.CI_NONE;
		
		switch(axisIndex) {
			case AXIS_LEFT_Y:
			case AXIS_RIGHT_Y:
				return axisDir < 0 ? CMInputs.CI_UP : CMInputs.CI_DOWN;
			case AXIS_LEFT_X:
			case AXIS_RIGHT_X:
				return axisDir < 0 ? CMInputs.CI_LEFT : CMInputs.CI_RIGHT;
			default: return CMInputs.CI_NONE;
		}
	}
}
