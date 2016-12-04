package com.cheekymammoth.gameModes;

public interface IPuzzleMode {
	public enum PuzzleDimensions { _8x6, _10x8 }
	
	PuzzleDimensions getDimesions();
	int getNumColumns();
	int getNumRows();
	int getNumLevels();
	int getNumPuzzles();
	int getMaxColorMagicMoves();
	int getMaxMirrorImageMoves();
	int getColorFloodCacheSize();
	int[] getColorFloodIndexOffsets();
	int[] getColorSwirlIndexOffsets();
	String getLevelDataFilePath();
	String getConveyorBeltHorizSoundName();
	String getConveyorBeltVertSoundName();
}
