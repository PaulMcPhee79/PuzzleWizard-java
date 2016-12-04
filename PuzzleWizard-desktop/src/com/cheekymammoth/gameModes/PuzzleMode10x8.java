package com.cheekymammoth.gameModes;

public class PuzzleMode10x8 implements IPuzzleMode {
	private static final int[] kColorFloodIndexOffsets = new int[] { 1, 10, 12, 21 };
	private static final int[] kColorSwirlIndexOffsets = new int[] { 11, 1, 0, 10, 20, 21, 22, 12, 2 };
	
	public PuzzleMode10x8() { }

	@Override
	public PuzzleDimensions getDimesions() {
		return PuzzleDimensions._10x8;
	}
	
	@Override
	public int getNumColumns() {
		return 10;
	}
	
	@Override
	public int getNumRows() {
		return 8;
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
		return 5;
	}
	
	@Override
	public int getMaxMirrorImageMoves() {
		return 10;
	}
	
	@Override
	public int getColorFloodCacheSize() {
		return 80;
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
		return "data/XPlatformLevels_10x8.dat";
	}
	
	@Override
	public String getConveyorBeltHorizSoundName() {
		return "cbelt-horiz";
	}
	
	@Override
	public String getConveyorBeltVertSoundName() {
		return "cbelt-vert";
	}
}
