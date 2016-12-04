package com.cheekymammoth.puzzleio;

import com.cheekymammoth.sceneControllers.GameController;

public class GameProgressController {
	private static GameProgressController singleton = new GameProgressController();
	private static int kTrialMaxLevelIndex = 5;
	
	private boolean hasPerformedIntialLoad;
	private boolean shouldSave;
	private boolean unlockedAll;
	private GameProgress gameProgress;
	
	private GameProgressController() {
		gameProgress = new GameProgress(12, 6);
	}
	
	public static GameProgressController GPC() {
        return singleton;
    }
	
	public boolean hasPerformedInitialLoad() { return hasPerformedIntialLoad; }
	
	public int getNextUnsolvedPuzzleIndex(int startIndex) {
		int numLevels = getNumLevels();
		int puzzleIndex = -1, numPuzzlesPerLevel = getNumPuzzlesPerLevel();
		for (int i = 0; i < numLevels; i++) {
			for (int j = 0; j < numPuzzlesPerLevel; j++) {
				if (startIndex >= (i * numPuzzlesPerLevel + j))
					continue;
				
				if ((isPuzzleUnlocked(i, j) || getUnlockedAll()) && !hasSolved(i, j)) {
					puzzleIndex = i * numPuzzlesPerLevel + j;
					return puzzleIndex;
				}
			}
		}
		
		for (int i = 0; i < numLevels; i++) {
			for (int j = 0; j < numPuzzlesPerLevel; j++) {
				if ((i * numPuzzlesPerLevel + j) >= startIndex)
					return puzzleIndex;
				
				if ((isPuzzleUnlocked(i, j) || getUnlockedAll()) && !hasSolved(i, j)) {
					puzzleIndex = i * numPuzzlesPerLevel + j;
					return puzzleIndex;
				}
			}
		}
		
		return puzzleIndex;
	}
	
	public boolean isLevelUnlocked(int levelIndex) {
		// 1. No level locking
//		return true;
		
		// 2. All levels locked (except first)
//		return levelIndex == 0;
		
		// 3. First level is always unlocked. Final level is unlocked only when every previous level is completed.
//		return levelIndex >= 0 &&
//				(levelIndex == 0 ||
//				(levelIndex < getNumLevels()-1 && getNumSolvedPuzzlesForLevel(levelIndex-1) >= 3) ||
//				getNumSolvedPuzzles() >= getNumPuzzles() - getNumPuzzlesPerLevel());
		
		// 4. Unlock system to manage publisher demo Unlock All God Mode
		boolean isUnlocked = levelIndex >= 0 && levelIndex < getNumLevels() &&
				(
						(levelIndex < getNumLevels()-1 && areAllPreviousLevelsUnlocked(levelIndex)) ||
						getNumSolvedPuzzles() >= getNumPuzzles() - getNumPuzzlesPerLevel()
				);
		if (GameController.isTrialMode())
			isUnlocked = isUnlocked && levelIndex <= kTrialMaxLevelIndex;
		return isUnlocked;
	}

	public boolean isPuzzleUnlocked(int levelIndex, int puzzleIndex) {
		// 1. No puzzle locked
		// return true;
		
		// 2. Unlock puzzle by playing previous puzzle
		//return isLevelUnlocked(levelIndex) && (puzzleIndex == 0 || hasPlayed(levelIndex, puzzleIndex-1));
		
		// 3. Unlock puzzle by solving previous puzzle
		//return isLevelUnlocked(levelIndex) && (puzzleIndex == 0 || hasSolved(levelIndex, puzzleIndex-1));
		
		// 4. Unlock system to manage publisher demo Unlock All God Mode
		return isLevelUnlocked(levelIndex) &&
				(
						levelIndex == getNumLevels()-1 ||
						hasSolved(levelIndex, puzzleIndex) ||
						areAllPreviousPuzzlesUnlocked(levelIndex, puzzleIndex)
				);
	}
	
	public boolean isTrialModeCompleted() {
		//return GameController.isTrialMode() && getNumSolvedLevels() >= (kTrialMaxLevelIndex+1);
		return GameController.isTrialMode() && isLevelUnlocked(kTrialMaxLevelIndex);
	}
	
	public boolean getUnlockedAll() { return unlockedAll; }
	
	public void setUnlockedAll(boolean value) { unlockedAll = value; }
	
	private boolean areAllPreviousLevelsUnlocked(int levelIndex) {
		int countMax = Math.min(levelIndex, getNumLevels());
		for (int i = 0; i < countMax; i++) {
			if (getNumSolvedPuzzlesForLevel(i) < 3)
				return false;
		}
		
		return true;
	}
	
	private boolean areAllPreviousPuzzlesUnlocked(int levelIndex, int puzzleIndex) {
		if (levelIndex < 0 || levelIndex >= getNumLevels())
			return false;
		
		int countMax = Math.min(puzzleIndex, getNumPuzzlesPerLevel());
		for (int i = 0; i < countMax; i++) {
			if (!hasSolved(levelIndex, i))
				return false;
		}
		
		return true;
	}
	
	// Pass-throughs
	public int getNumLevels() { return gameProgress.getNumLevels(); }
	
	public int getNumSolvedLevels() { return gameProgress.getNumSolvedLevels(); }
	
	public int getNumPuzzles() { return gameProgress.getNumPuzzles(); }
	
	public int getNumPuzzlesPerLevel() { return gameProgress.getNumPuzzlesPerLevel(); }
	
	public int getNumSolvedPuzzles() { return gameProgress.getNumSolvedPuzzles(); }
	
	public boolean hasPlayed(int levelIndex, int puzzleIndex) {
		return gameProgress.hasPlayed(levelIndex, puzzleIndex);
	}
	
	public void setPlayed(boolean played, int levelIndex, int puzzleIndex) {
		if (hasPlayed(levelIndex, puzzleIndex) != played)
			gameProgress.setPlayed(played, levelIndex, puzzleIndex);
	}
	
	public boolean hasSolved(int levelIndex, int puzzleIndex) {
		return gameProgress.hasSolved(levelIndex, puzzleIndex);
	}
	
	public void setSolved(boolean solved, int levelIndex, int puzzleIndex) {
		if (hasSolved(levelIndex, puzzleIndex) != solved) {
			gameProgress.setSolved(solved, levelIndex, puzzleIndex);
			shouldSave = true;
		}
	}
	
	public int getNumSolvedPuzzlesForLevel(int levelIndex) {
		return gameProgress.getNumSolvedPuzzlesForLevel(levelIndex);
	}
	
	public void invalidateCaches() {
		gameProgress.invalidateCaches();
	}
	
	public void load() {
		gameProgress.load();
		hasPerformedIntialLoad = true;
		shouldSave = false;
	}
	
	public void save() {
		if (shouldSave) {
			gameProgress.save();
			shouldSave = false;
		}
	}
	// End pass-throughs
}
