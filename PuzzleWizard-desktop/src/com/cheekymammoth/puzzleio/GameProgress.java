package com.cheekymammoth.puzzleio;

import java.awt.EventQueue;

import com.badlogic.gdx.Gdx;
import com.cheekymammoth.files.FileManager;
import com.cheekymammoth.files.IFileClient;

public class GameProgress implements IFileClient {
	private static final String kFilePath = "SavedGames/PW_SaveGame";
	private static final String kFileExt = ".dat";
	private static final byte kDataVersion = 1;
	private static final byte kPlayedBit = 1 << 0;
	private static final byte kSolvedBit = 1 << 1;
	
	private int numLevels;
	private int numPuzzlesPerLevel;
	private int numSolvedLevelsCache = -1;
	private int numSolvedPuzzlesCache = -1;
	private byte[] levelData;
	
	private boolean isSaveBusy;
	private byte[] sharedAsyncSaveData;
	
	public GameProgress(int numLevels, int numPuzzlesPerLevel) {
		this.numLevels = numLevels;
		this.numPuzzlesPerLevel = numPuzzlesPerLevel;
		prepareLevelDataForNumLevels(numLevels);
	}
	
	public int getNumLevels() { return numLevels; }
	
	public int getNumSolvedLevels() { 
		if (numSolvedLevelsCache != -1)
			return numSolvedLevelsCache;
		
		numSolvedLevelsCache = 0;
		for (int i = 0, n = getNumLevels(); i < n; i++) {
			if (getNumSolvedPuzzlesForLevel(i) == getNumPuzzlesPerLevel())
				numSolvedLevelsCache++;
		}
		
		return numSolvedLevelsCache;
	}
	
	public int getNumPuzzles() {
		return getNumLevels() * getNumPuzzlesPerLevel();
	}
	
	public int getNumPuzzlesPerLevel() { return numPuzzlesPerLevel; }
	
	public int getNumSolvedPuzzles() {
		if (numSolvedPuzzlesCache != -1)
			return numSolvedPuzzlesCache;
		
		numSolvedPuzzlesCache = 0;
		for (int i = 0, ni = getNumLevels(); i < ni; i++) {
			for (int j = 0, nj = getNumPuzzlesPerLevel(); j < nj; j++) {
				if (hasSolved(i, j))
					numSolvedPuzzlesCache++;
			}
		}
		
		return numSolvedPuzzlesCache;
	}
	
	public boolean hasPlayed(int levelIndex, int puzzleIndex) {
		if (!isValidIndexes(levelIndex, puzzleIndex))
			return false;
		return (valueForPuzzleIndex(resolvedIndex(levelIndex, puzzleIndex)) & kPlayedBit) == kPlayedBit;
	}
	
	public void setPlayed(boolean played, int levelIndex, int puzzleIndex) {
		if (!isValidIndexes(levelIndex, puzzleIndex))
			return;
		
		if (played)
			levelData[resolvedIndex(levelIndex, puzzleIndex)] |= kPlayedBit;
		else
			levelData[resolvedIndex(levelIndex, puzzleIndex)] &= ~kPlayedBit;
	}
	
	public boolean hasSolved(int levelIndex, int puzzleIndex) {
		if (!isValidIndexes(levelIndex, puzzleIndex))
			return false;
		return (valueForPuzzleIndex(resolvedIndex(levelIndex, puzzleIndex)) & kSolvedBit) == kSolvedBit;
	}
	
	public void setSolved(boolean played, int levelIndex, int puzzleIndex) {
		if (!isValidIndexes(levelIndex, puzzleIndex))
			return;
		
		if (played)
			levelData[resolvedIndex(levelIndex, puzzleIndex)] |= kSolvedBit;
		else
			levelData[resolvedIndex(levelIndex, puzzleIndex)] &= ~kSolvedBit;
		
		invalidateCaches();
	}
	
	public int getNumSolvedPuzzlesForLevel(int levelIndex) {
		if (levelIndex < 0 || levelIndex >= getNumLevels())
			return 0;
		
		int numSolvedPuzzles = 0;
		for (int i = 0, n = getNumPuzzlesPerLevel(); i < n; i++) {
			if (hasSolved(levelIndex, i))
				numSolvedPuzzles++;
		}
		
		return numSolvedPuzzles;
	}
	
	public void invalidateCaches() {
		numSolvedPuzzlesCache = numSolvedLevelsCache = -1;
	}
	
	private void prepareLevelDataForNumLevels(int numLevels) {
		levelData = new byte[numLevels * getNumPuzzlesPerLevel()];
	}
	
	private byte valueForPuzzleIndex(int index) {
		return levelData[index];
	}
	
	private int resolvedIndex(int levelIndex, int puzzleIndex) {
		return levelIndex * getNumPuzzlesPerLevel() + puzzleIndex;
	}
	
	private boolean isValidIndexes(int levelIndex, int puzzleIndex) {
		return !(levelIndex < 0 || levelIndex >= getNumLevels()
				|| puzzleIndex < 0 || puzzleIndex >= getNumPuzzlesPerLevel());
	}
	
	public void load() {
		byte[] loadData = FileManager.FM().loadProgress(kFilePath, kFileExt);
		if (loadData != null) {
			if (levelData == null || levelData.length != loadData.length - 1)
				levelData = new byte[loadData.length - 1];
			// kDataVersion is at loadData[0] if needed to synchronize legacy data
			System.arraycopy(loadData, 1, levelData, 0, loadData.length - 1);
			Gdx.app.log("Load succeeded", "" + loadData.length + " bytes loaded.");
		} else
			Gdx.app.log("Load failed", "0 bytes loaded.");
	}
	
	public void save() {
		if (sharedAsyncSaveData == null)
			sharedAsyncSaveData = new byte[levelData.length + 1];// +1 for kDataVersion
		byte[] data = isSaveBusy() ? new byte[levelData.length + 1] : sharedAsyncSaveData;
		data[0] = kDataVersion;
		System.arraycopy(levelData, 0, data, 1, levelData.length);
		
		setSaveBusy(true);
		FileManager.FM().saveProgress(kFilePath, kFileExt, data, this);
	}
	
	private synchronized boolean isSaveBusy() {
		return isSaveBusy;
	}
	
	private synchronized void setSaveBusy(boolean isBusy) {
		isSaveBusy = isBusy;
	}

	@Override
	public void onAsyncSaveCompleted(final int saveResult) {
		setSaveBusy(false);
		
		EventQueue.invokeLater(new Runnable() { 
			  @Override
			  public void run() {
				  if (saveResult == IFileClient.SAVE_SUCCEEDED)
					  Gdx.app.log("Save completed", "Save succeeded.");
				  else
					  Gdx.app.log("Saved completed", "Save failed.");
			  }
			});
	}
}
