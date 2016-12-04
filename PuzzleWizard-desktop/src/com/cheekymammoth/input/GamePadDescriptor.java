package com.cheekymammoth.input;

import com.badlogic.gdx.controllers.PovDirection;
import com.cheekymammoth.utils.Coord;

public interface GamePadDescriptor {
	boolean isMovementAxis(int axisIndex);
	Coord axesForAxisIndex(int axisIndex);
	PovDirection button2PovDir(int buttonCode);
	int normalizeButton(int buttonCode);
	int normalizePov(PovDirection povDir);
	int normalizeAxis(int axisIndex, int axisDir);
}
