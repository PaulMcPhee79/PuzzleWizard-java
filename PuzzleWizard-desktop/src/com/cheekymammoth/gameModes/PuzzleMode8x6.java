package com.cheekymammoth.gameModes;

public class PuzzleMode8x6 implements IPuzzleMode {
	private static final int[] kColorFloodIndexOffsets = new int[] { 1, 8, 10, 17 };
	private static final int[] kColorSwirlIndexOffsets = new int[] { 9, 1, 0, 8, 16, 17, 18, 10, 2 };
	
	public PuzzleMode8x6() { }

	@Override
	public PuzzleDimensions getDimesions() {
		return PuzzleDimensions._8x6;
	}
	
	@Override
	public int getNumColumns() {
		return 8;
	}
	
	@Override
	public int getNumRows() {
		return 6;
	}
	
	@Override
	public int getNumLevels() {
		return 12;
	}
	
	@Override
	public int getNumPuzzles() {
		return 72;
	}
	
	@Override
	public int getMaxColorMagicMoves() {
		return 4;
	}
	
	@Override
	public int getMaxMirrorImageMoves() {
		return 8;
	}
	
	@Override
	public int getColorFloodCacheSize() {
		return 48;
	}
	
	@Override
	public int[] getColorFloodIndexOffsets() {
		return kColorFloodIndexOffsets;
	}
	
	@Override
	public int[] getColorSwirlIndexOffsets() {
		return kColorSwirlIndexOffsets;
	}
	
	@Override
	public String getLevelDataFilePath() {
		return "data/XPlatformLevels_8x6.dat";
	}
	
	@Override
	public String getConveyorBeltHorizSoundName() {
		return "cbelt-horiz_8x6";
	}
	
	@Override
	public String getConveyorBeltVertSoundName() {
		return "cbelt-vert_8x6";
	}
}
