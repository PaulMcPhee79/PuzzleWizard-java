package com.cheekymammoth.puzzles;

import com.badlogic.gdx.utils.Array;
import com.cheekymammoth.gameModes.PuzzleMode;

public class ColorFlooder {
	private static final int kNumAdjIndexes = 4;
	
	private int[] kColorFloodIndexOffsets; // Treat as final
	private int GUID;
	private int nodeIter;
	private int[] rootNodeIndexes = new int[kNumAdjIndexes];
	private Array<int[]> nodeIndexes;
	// On load from client for duration of flood call
	private Tile[] tiles;
	private Puzzle puzzle;
	
	public ColorFlooder() {
		kColorFloodIndexOffsets = PuzzleMode.getColorFloodIndexOffsets();
		fillCache(PuzzleMode.getColorFloodCacheSize());
	}
	
	@SuppressWarnings("unused")
	private void fillCache(int size) {
		assert(kNumAdjIndexes > 3 && kNumAdjIndexes < 5) : "ColorFlooder kNumAdjIndexes mismatch.";
		
		nodeIndexes = new Array<int[]>(true, size, int[].class);
		for (int i = 0; i < size; i++) {
			int[] arr = new int[] { -1, -1, -1, -1 };
			nodeIndexes.add(arr);
		}
	}
	
	private int nextGUID() {
		return GUID++;
	}
	
	public int fill(Puzzle puzzle, Tile originTile, int colorKey) {
		int fillCount = 0;
		this.puzzle = puzzle;
		if (puzzle != null) {
			GUID = 2; // 0: Invalid 1: Tile starting index
			this.tiles = puzzle.getTiles();
			fillCount = fillRoot(originTile, colorKey);
			this.puzzle = null;
			this.tiles = null;
		}
		
		return fillCount;
	}

	private int fillRoot(Tile tile, int colorKey) {
		int rootIndex = puzzle.indexForTile(tile);
		int topLeftIndex = rootIndex - (puzzle.getNumColumns()+1);
		int numLiveNodes = 0;
		int[] nodeIndexes = rootNodeIndexes;
		
		for (int i = 0; i < kNumAdjIndexes; i++) {
			nodeIndexes[i] = -1;
			
			int nodeIndex = topLeftIndex + kColorFloodIndexOffsets[i];
			if (puzzle.isValidIndex(nodeIndex)) {
				if (!puzzle.doesAdjacentPerpIndexWrap(rootIndex, nodeIndex)) {
					Tile nodeTile = tiles[nodeIndex];
					if (nodeTile.getColorKey() == colorKey && !nodeTile.isModified(Tile.kTMShielded)) {
						nodeIndexes[i] = nodeIndex;
						numLiveNodes++;
					}
				}
			}
		}
		
		int numTiles = puzzle.getNumTiles();
		for (int i = 0; i < numTiles; i++)
			tiles[i].setGUID(1);
		
		int totalFills = fillNode(rootIndex, tile, colorKey, 1, nextGUID());
		if (totalFills > 0) {
			while (numLiveNodes != 0) {
				tile.setGUID(nextGUID());
				for (int i = 0; i < kNumAdjIndexes; i++) {
					int nodeIndex = nodeIndexes[i];
					if (nodeIndex == -1)
						continue;
					
					Tile nodeTile = tiles[nodeIndex];
					int nodeFills = fillNode(nodeIndex, nodeTile, colorKey, 2, tile.getGUID());
					if (nodeFills == 0) {
						nodeIndexes[i] = -1;
						numLiveNodes--;
					}
					
					totalFills += nodeFills;
				}
			}
		}
		
		return totalFills;
	}
	
	@SuppressWarnings("unused")
	private int fillNode(int index, Tile tile, int colorKey, int fillDepth, int guid) {
		int numFills = 0, numPreFills = 0, topLeftIndex = index - (puzzle.getNumColumns()+1);
		int[] subNodeIndexes = null;
		
		if (nodeIter < nodeIndexes.size)
			subNodeIndexes = nodeIndexes.get(nodeIter);
		else {
			assert(kNumAdjIndexes > 3 && kNumAdjIndexes < 5) : "ColorFlooder kNumAdjIndexes mismatch.";
			subNodeIndexes = new int[] { -1, -1, -1, -1 };
			nodeIndexes.add(subNodeIndexes);
		}
		
		nodeIter++;
		tile.setGUID(guid);
		for (int i = 0; i < kNumAdjIndexes; i++) {
			subNodeIndexes[i] = -1;
			
			int nextIndex = topLeftIndex + kColorFloodIndexOffsets[i];
			if (puzzle.isValidIndex(nextIndex) && !puzzle.doesAdjacentPerpIndexWrap(index, nextIndex)) {
				Tile adjacentTile = tiles[nextIndex];
				if (adjacentTile.getGUID() != guid && adjacentTile.getGUID() != 0 &&
						!adjacentTile.isModified(Tile.kTMShielded)) { // Root tile will be skipped by GUID test
					if (adjacentTile.getColorKey() == colorKey) {
						adjacentTile.setDecorator(Tile.kTFColorFlood);
						adjacentTile.setDecoratorData(fillDepth+1);
						adjacentTile.setColorKey(tile.getColorKey());
						numFills++;
					} else if (adjacentTile.getColorKey() == tile.getColorKey() && adjacentTile.getGUID() != 1)
						numPreFills++; // GUID of 1 means this tile has not been colored during this fill
					else
						continue;
					
					adjacentTile.setGUID(guid);
					subNodeIndexes[i] = nextIndex;
				}
			}
		}
		
		if (numFills == 0 && numPreFills != 0) {
			for (int i = 0; i < kNumAdjIndexes; i++) {
				int nextIndex = subNodeIndexes[i];
				if (nextIndex == -1)
					continue;
				
				Tile adjacentTile = tiles[nextIndex];
				int nodeFills = fillNode(nextIndex, adjacentTile, colorKey, fillDepth+1, guid);
				numFills += nodeFills;
				
				if (nodeFills == 0)
					adjacentTile.setGUID(0); // Mark as an invalid branch
			}
		}
		
		nodeIter--;
		return numFills;
	}
}
