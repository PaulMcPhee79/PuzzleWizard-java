package com.cheekymammoth.puzzles;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;

public class Level {
	private static int s_NextLevelID = 1;
	
	private int ID;
	private String name;
	private Array<Puzzle> puzzles;
	private IntMap<Puzzle> puzzlesMap;
	
	public Level(int ID, String name) {
		this.ID = ID;
        this.name = name;
        puzzles = new Array<Puzzle>(true, 6, Puzzle.class);
        puzzlesMap = new IntMap<Puzzle>(6);
	}
	
	public Level(int ID, BufferedInputStream stream) throws IOException {
		this.ID = ID;
		decodeFromStream(stream);
	}
	
	public int getID() { return ID; }
	
	public String getName() { return name; }
	
	public void setName(String value) { name = value; }
	
	public int getNumPuzzles() { return puzzles.size; }
	
	public Array<Puzzle> getPuzzles() { return puzzles; }
	
	public static int nextLevelID(int reset) {
		if (reset != -1)
			s_NextLevelID = reset;
		return s_NextLevelID++;
	}
	
	void resetLevelID(int value) {
		s_NextLevelID = value;
	}
	
	public Puzzle puzzleForID(int id) {
		return puzzlesMap.get(id, null);
	}
	
	public int indexOfPuzzleID(int puzzleId) {
		if (puzzlesMap.containsKey(puzzleId))
			return puzzles.indexOf(puzzlesMap.get(puzzleId), true);
		else
			return -1;
	}
	
	public int indexOfPuzzle(Puzzle puzzle) {
		return puzzles.indexOf(puzzle, true);
	}
	
	public void insertPuzzleAtIndex(int index, Puzzle puzzle) {
        assert(index >= 0 && index <= puzzles.size) : "Level::InsertPuzzleAtIndex - index out of range.";

        if (puzzle == null || index < 0 || index > puzzles.size)
            return;

        puzzles.insert(index, puzzle);
        puzzlesMap.put(puzzle.getID(), puzzle);
    }
	
	public void addPuzzle(Puzzle puzzle) {
		if (puzzle != null)
			insertPuzzleAtIndex(puzzles.size, puzzle);
	}

	public void removePuzzle(Puzzle puzzle) {
	    if (puzzle != null && puzzles.contains(puzzle, true)) {
	        puzzles.removeValue(puzzle, true);
	        puzzlesMap.remove(puzzle.getID());
	    }
	}

	public Level clone() {
	    Level level = new Level(getID(), getName());

	    for (int i = 0, n = puzzles.size; i < n; i++) {
	    	Puzzle puzzle = puzzles.get(i);
	    	level.addPuzzle(puzzle.clone());
	    }
	    
	    return level;
	}
	
	public Level devClone() {
	    Level level = new Level(getID(), getName());

	    for (int i = 0, n = puzzles.size; i < n; i++) {
	    	Puzzle puzzle = puzzles.get(i);
	    	level.addPuzzle(puzzle.devClone());
	    }
	    
	    return level;
	}

	protected void decodeFromStream(BufferedInputStream stream) throws IOException {
		byte[] arr = new byte[32 + 4]; // name,numPuzzles
		int streamState = stream.read(arr, 0, arr.length);
		
		if (streamState == -1)
			throw new EOFException();

		ByteBuffer buffer = ByteBuffer.wrap(arr);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		byte[] nameArr = new byte[32];
		buffer.get(nameArr, 0, 32);
		name = new String(nameArr);
		
		int numPuzzles = Math.max(1, buffer.getInt());
		puzzles = new Array<Puzzle>(true, numPuzzles, Puzzle.class);
        puzzlesMap = new IntMap<Puzzle>(numPuzzles);
        for (int i = 0; i < numPuzzles; i++) {
        	Puzzle puzzle = Puzzle.getPuzzle(Puzzle.nextPuzzleID());
        	puzzle.decodeFromStream(stream);
        	addPuzzle(puzzle);
        }
	}
}
