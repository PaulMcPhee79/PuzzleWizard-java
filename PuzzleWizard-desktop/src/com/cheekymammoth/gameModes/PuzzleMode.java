package com.cheekymammoth.gameModes;

import com.badlogic.gdx.graphics.Color;
import com.cheekymammoth.gameModes.IPuzzleMode.PuzzleDimensions;

public class PuzzleMode {
	private static IPuzzleMode mode;
	public static final String[] kLevelNames = new String[] {
		"First Steps", "Color Swap", "Color Shield", "Conveyor Belt", "Rotator", "Color Flood",
		"White Tile", "Mirror Image", "Color Swirl", "Tile Swap", "Color Magic", "Wizard"
	};
	
	public static final String[] kLevelTextureNames = new String[] {
		"1.first-steps", "2.color-swap", "3.color-shield", "4.conveyor-belt",
		"5.rotator", "6.color-flood", "7.white-tile", "8.mirror-image",
		"9.color-swirl", "10.tile-swap", "11.color-magic", "12.wizard"
	};
	
	// Original
//	public static final Color[] kLevelColors = new Color[] {
//		new Color(0x85d6eaff), new Color(0x44a4f6ff), new Color(0x007ce5ff),
//		new Color(0x77d5b9ff), new Color(0x49d47dff), new Color(0x31c84eff),
//		new Color(0xfffd5aff), new Color(0xfffd33ff), new Color(0xffff00ff),
//		new Color(0xea8585ff), new Color(0xf64444ff), new Color(0xe50000ff)
//	};
	
	// Sinclair's
//	public static final Color[] kLevelColors = new Color[] {
//		new Color(0x5b8bd2ff), new Color(0x386ab4ff), new Color(0x0155d2ff),
//		new Color(0x59c600ff), new Color(0x4eae00ff), new Color(0x439600ff),
//		new Color(0xfee510ff), new Color(0xffe139ff), new Color(0xffd801ff),
//		new Color(0xfa0c00ff), new Color(0xe20000ff), new Color(0xc00500ff)
//	};
	
	// Combined
	public static final Color[] kLevelColors = new Color[] {
		new Color(0x7dbbefff), new Color(0x44a4f6ff), new Color(0x007ce5ff),
		new Color(0x77d5b9ff), new Color(0x49d47dff), new Color(0x31c84eff),
		new Color(0xfffd5aff), new Color(0xfffd33ff), new Color(0xffff00ff),
		new Color(0xea8585ff), new Color(0xf64444ff), new Color(0xe50000ff)
	};
	
	private PuzzleMode() { }
	
	public static boolean is8x6() {
		return PuzzleMode.mode instanceof PuzzleMode8x6;
	}
	
	public static boolean is10x8() {
		return PuzzleMode.mode instanceof PuzzleMode10x8;
	}
	
	public static void setMode(IPuzzleMode mode) {
		PuzzleMode.mode = mode;
	}

	public static PuzzleDimensions getDimesions() {
		return mode.getDimesions();
	}
	
	public static int getNumColumns() {
		return mode.getNumColumns();
	}
	
	public static int getNumRows() {
		return mode.getNumRows();
	}
	
	public static int getNumLevels() {
		return mode.getNumLevels();
	}
	
	public static int getNumPuzzles() {
		return mode.getNumPuzzles();
	}

	public static int getMaxColorMagicMoves() {
		return mode.getMaxColorMagicMoves();
	}
	
	public static int getMaxMirrorImageMoves() {
		return mode.getMaxMirrorImageMoves();
	}
	
	public static int getColorFloodCacheSize() {
		return mode.getColorFloodCacheSize();
	}
	
	public static int[] getColorFloodIndexOffsets() {
		return mode.getColorFloodIndexOffsets();
	}
	
	public static int[] getColorSwirlIndexOffsets() {
		return mode.getColorSwirlIndexOffsets();
	}
	
	public static String getLevelDataFilePath() {
		return mode.getLevelDataFilePath();
	}
	
	public static String getConveyorBeltHorizSoundName() {
		return mode.getConveyorBeltHorizSoundName();
	}
	
	public static String getConveyorBeltVertSoundName() {
		return mode.getConveyorBeltVertSoundName();
	}
}
