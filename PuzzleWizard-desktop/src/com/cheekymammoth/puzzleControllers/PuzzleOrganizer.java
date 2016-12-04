package com.cheekymammoth.puzzleControllers;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.gameModes.PuzzleMode;
import com.cheekymammoth.puzzles.Level;
import com.cheekymammoth.puzzles.Puzzle;

public class PuzzleOrganizer extends EventDispatcher {
	public static final int EV_TYPE_PUZZLE_LOADED;
	public static final int EV_TYPE_PUZZLE_LOAD_ERROR;
	private static final int kOrphanedLevelID = 0;
	
	static {
		EV_TYPE_PUZZLE_LOADED = EventDispatcher.nextEvType();
		EV_TYPE_PUZZLE_LOAD_ERROR = EventDispatcher.nextEvType();
	}
	
	private Array<Puzzle> puzzlesCache;
	private Array<Level> levelsCache;
	private Array<Level> levels;
	private IntMap<Level> levelsMap;

	public PuzzleOrganizer() {
		levels = new Array<Level>(true, 13, Level.class);
		levelsMap = new IntMap<Level>(13);
		addLevel(new Level(kOrphanedLevelID, "Orphans"));
	}
	
	public int absolutePuzzleIndexForPuzzleID(int puzzleID) {
		int puzzleIndex = -1;
		Array<Puzzle> puzzles = getPuzzles();
		if (puzzles != null) {
			for (int i = 0; i < puzzles.size; i++) {
				Puzzle puzzle = puzzles.get(i);
				if (puzzle != null && puzzle.getID() == puzzleID) {
					puzzleIndex = i;
					break;
				}
			}
		}
		
		return puzzleIndex;
	}
	
	public int levelBasedPuzzleIndexForPuzzleID(int puzzleID) {
		int puzzleIndex = -1;
		for (int i = 0; i < levels.size && puzzleIndex == -1; i++) {
			Level level = levels.get(i);
			if (level == null || level.getID() == kOrphanedLevelID)
				continue;
			
			Array<Puzzle> puzzles = level.getPuzzles();
			if (puzzles != null) {
				for (int j = 0; j < puzzles.size; j++) {
					Puzzle puzzle = puzzles.get(j);
					if (puzzle != null && puzzle.getID() == puzzleID) {
						puzzleIndex = j;
						break;
					}
				}
			}
		}
		
		return puzzleIndex;
	}
	
//	public int levelIndexForPuzzleID(int puzzleID) {
//		int levelIndex = -1;
//		for (int i = 0, n = levels.size; i < n && levelIndex == -1; i++) {
//			Level level = levels.get(i);
//			if (level != null && level.getID() != kOrphanedLevelID && level.puzzleForID(puzzleID) != null) {
//				levelIndex = i;
//				break;
//			}
//		}
//		
//		return levelIndex;
//	}
	
	// Note: this is not the index in levels ivar. It is the index for the playable game levels.
	public int levelIndexForPuzzleID(int puzzleID) {
		for (int i = 0, levelIndex = 0; i < levels.size; i++) {
			Level level = levels.get(i);
			if (level != null) {
				if (level.getID() == kOrphanedLevelID)
					continue;
				if (level.puzzleForID(puzzleID) != null)
					return levelIndex;
				levelIndex++;
			}
		}
		
		return -1;
	}
	
	public Level levelForPuzzle(Puzzle puzzle) {
		if (puzzle == null)
			return null;
		
		for (int i = 0; i < levels.size; i++) {
			Level level = levels.get(i);
			if (level != null && level.puzzleForID(puzzle.getID()) != null)
				return level;
		}
		
		return null;
	}
	
	public Level levelForID(int levelID) {
		return levelsMap.get(levelID);
	}

	public void addLevel(Level level) {
		if (level != null && levelForID(level.getID()) == null) {
			levels.add(level);
			levelsMap.put(level.getID(), level);
		}
	}
	
	public Puzzle puzzleForID(int puzzleID) {
		return puzzleForID(puzzleID, -1);
	}
	
	public Puzzle puzzleForID(int puzzleID, int levelID) {
		if (levelID == -1) {
			for (int i = 0; i < levels.size; i++) {
				Level level = levels.get(i);
				if (level != null) {
					Puzzle puzzle = level.puzzleForID(puzzleID);
					if (puzzle!= null)
						return puzzle;
				}
			}
		} else {
			Level level = levelForID(levelID);
			if (level != null)
				return level.puzzleForID(puzzleID);
		}
		
		return null;
	}
	
	public void addPuzzle(Puzzle puzzle) {
		addPuzzle(puzzle, kOrphanedLevelID);
	}
	
	public void addPuzzle(Puzzle puzzle, int levelID) {
		if (puzzle == null)
			return;
		
		if (!levelsMap.containsKey(levelID))
			addLevel(new Level(levelID, "Level" + levels.size));
		if (puzzleForID(puzzle.getID(), levelID) == null)
			levelsMap.get(levelID).addPuzzle(puzzle);
		else {
			Level level = levelsMap.get(levelID);
			int index = level.indexOfPuzzleID(puzzle.getID());
			level.removePuzzle(level.puzzleForID(puzzle.getID()));
			level.insertPuzzleAtIndex(index, puzzle);
		}
		
		puzzlesCache = null;
	}
	
	public void loadPuzzleByID(int puzzleID) {
		Puzzle puzzle = puzzleForID(puzzleID);
		if (puzzle != null)
			dispatchEvent(EV_TYPE_PUZZLE_LOADED, puzzle.devClone());
		else
			dispatchEvent(EV_TYPE_PUZZLE_LOAD_ERROR, null);
	}

	public void load(String filePath) throws IOException {
		assert(filePath != null) : "PuzzleOrganizer::load requires non-null file path.";
		
		FileHandle handle = Gdx.files.internal(PuzzleMode.getLevelDataFilePath());
		if (handle == null)
			throw new FileNotFoundException("Could not load level data from path: " + filePath);
		
		BufferedInputStream stream = handle.read(512);
		try {
			while (true) {
				Level level = new Level(Level.nextLevelID(-1), stream);
				if (level != null)
					addLevel(level);
			}
		} catch (IOException e) {
			Gdx.app.log("PuzzleOrganizer::load", e.getMessage());
		} finally {
			stream.close();
		}
	}
	
	public int getNumPuzzles() {
		int numPuzzles = 0;
		for (int i = 0, n = levels.size; i < n; i++) {
			Level level = levels.get(i);
			if (level != null && level.getID() != kOrphanedLevelID)
				numPuzzles += level.getNumPuzzles();
		}
		return numPuzzles;
	}
	
	public int getNumLevels() {
		int numLevels = 0;
		for (int i = 0, n = levels.size; i < n; i++) {
			Level level = levels.get(i);
			if (level != null && level.getID() != kOrphanedLevelID)
				numLevels++;
		}
		return numLevels;
	}
	
	public Array<Puzzle> getPuzzles() {
		int numPuzzles = getNumPuzzles();
		if (puzzlesCache != null && puzzlesCache.size == numPuzzles)
			return puzzlesCache;
		
		Array<Puzzle> puzzles = new Array<Puzzle>(true, 72, Puzzle.class);
		for (int i = 0, n = levels.size; i < n; i++) {
			Level level = levels.get(i);
			if (level != null) {
				Array<Puzzle> levelPuzzles = level.getPuzzles();
				if (levelPuzzles != null)
					puzzles.addAll(levelPuzzles);
			}
		}
		
		puzzlesCache = puzzles;
		return puzzles;
	}
	
	public Array<Level> getLevels() {
		int numLevels = getNumLevels();
		
		if (levelsCache != null && levelsCache.size == numLevels)
			return levelsCache;
		
		Array<Level> levels = new Array<Level>(true, numLevels, Level.class);
		for (int i = 0, n = this.levels.size; i < n; i++) {
			Level level = this.levels.get(i);
			if (level != null && level.getID() != kOrphanedLevelID)
				levels.add(level);
		}
		
		levelsCache = levels;
		return levels;
	}
}
