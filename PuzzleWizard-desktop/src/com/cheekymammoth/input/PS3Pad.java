package com.cheekymammoth.input;

import com.badlogic.gdx.controllers.PovDirection;
import com.cheekymammoth.utils.Coord;

public class PS3Pad implements GamePadDescriptor {
	public static final int BUTTON_TRIANGLE = 0;
	public static final int BUTTON_CIRCLE = 1;
	public static final int BUTTON_X = 2;
	public static final int BUTTON_SQUARE = 3;
	public static final int BUTTON_SELECT = 8;
	public static final int BUTTON_START = 9;
	public static final PovDirection BUTTON_DPAD_UP = PovDirection.north;
	public static final PovDirection BUTTON_DPAD_DOWN = PovDirection.south;
	public static final PovDirection BUTTON_DPAD_RIGHT = PovDirection.east;
	public static final PovDirection BUTTON_DPAD_LEFT = PovDirection.west;
	public static final int BUTTON_LB = 4;
	public static final int BUTTON_RB = 5;
	public static final int BUTTON_LEFT_TRIGGER = 6;
	public static final int BUTTON_RIGHT_TRIGGER = 7;
	public static final int AXIS_LEFT_X = 5; //-1 is left | +1 is right
	public static final int AXIS_LEFT_Y = 4; //-1 is up | +1 is down
	public static final int AXIS_RIGHT_X = 3; //-1 is left | +1 is right
	public static final int AXIS_RIGHT_Y = 0; //-1 is up | +1 is down
	public static final int DPAD_AXIS_X = 5; //-1 is left | +1 is right
	public static final int DPAD_AXIS_Y = 4; //-1 is up | +1 is down

	@Override
	public boolean isMovementAxis(int axisIndex) {
		return axisIndex == AXIS_LEFT_X || axisIndex == AXIS_LEFT_Y ||
				axisIndex == AXIS_RIGHT_X || axisIndex == AXIS_RIGHT_Y;
						// || axisIndex == DPAD_AXIS_X || axisIndex == DPAD_AXIS_Y; // Implied
	}

	private Coord axesCoordCache = new Coord();
	@Override
	public Coord axesForAxisIndex(int axisIndex) {
		switch(axisIndex) {
			case AXIS_LEFT_Y: // || DPAD_AXIS_Y
			case AXIS_LEFT_X: // || DPAD_AXIS_X
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
		return null;
	}

	@Override
	public int normalizeButton(int buttonCode) {
		switch(buttonCode) {
			case BUTTON_X: return CMInputs.CI_CONFIRM;
			case BUTTON_CIRCLE: return CMInputs.CI_CANCEL;
			case BUTTON_START: return CMInputs.CI_MENU;
			case BUTTON_SELECT: return CMInputs.CI_MENU;
			case BUTTON_LB: return CMInputs.CI_PREV_SONG;
			case BUTTON_RB: return CMInputs.CI_NEXT_SONG;
			default: return CMInputs.CI_NONE;
		}
	}

	@Override
	public int normalizePov(PovDirection povDir) {
		return CMInputs.CI_NONE;
	}

	@Override
	public int normalizeAxis(int axisIndex, int axisDir) {
		if (axisDir == 0)
			return CMInputs.CI_NONE;
		
		switch(axisIndex) {
			case AXIS_LEFT_Y: // || DPAD_AXIS_Y
			case AXIS_RIGHT_Y:
				return axisDir < 0 ? CMInputs.CI_UP : CMInputs.CI_DOWN;
			case AXIS_LEFT_X: // || DPAD_AXIS_X
			case AXIS_RIGHT_X:
				return axisDir < 0 ? CMInputs.CI_LEFT : CMInputs.CI_RIGHT;
			default: return CMInputs.CI_NONE;
		}
	}
}
