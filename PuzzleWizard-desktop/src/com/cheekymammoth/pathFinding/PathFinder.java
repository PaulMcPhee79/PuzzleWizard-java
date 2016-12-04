package com.cheekymammoth.pathFinding;

import com.badlogic.gdx.Gdx;
import com.cheekymammoth.pathFinding.AStar.SearchState;
import com.cheekymammoth.puzzleViews.TilePiece;
import com.cheekymammoth.puzzles.Player;
import com.cheekymammoth.puzzles.Puzzle;
import com.cheekymammoth.puzzles.Tile;
import com.cheekymammoth.utils.Coord;

public class PathFinder implements ISearchProvider {
	private SearchState searchState = SearchState.Uninitialized;
	private int maxSearchSize = 100;
	private Coord from = new Coord();
	private Coord to = new Coord();
	private Coord dxy = new Coord();
	private Player player;
	private Puzzle puzzle;
	private AStar astar;
	
	public PathFinder(int maxSearchSize) {
		this.maxSearchSize = Math.max(1, maxSearchSize);
		astar = new AStar(this.maxSearchSize);
	}

	@Override
	public int getSuccessors(Coord tilePos, Coord parentPos, Coord[] successors, int numAncestors) {
		if (puzzle == null || tilePos == null || parentPos == null || successors == null)
			return 0;
		
		int originIndex = puzzle.pos2Index(tilePos);
		Tile originTile = puzzle.tileAtIndex(originIndex);
		if (originTile == null)
			return 0;
		
		Coord pos = Coord.obtainCoord(-1, -1);
		boolean isColorMagicActive = false;
		if (player != null && player.isColorMagicActive()) {
			isColorMagicActive =
				player.getNumColorMagicMoves() - (player.isMoving() ? 1 : 0) > numAncestors;
		}
		
		int numSuccessors = 0, numColumns = puzzle.getNumColumns(), numRows = puzzle.getNumRows();
		for (int i = 0; i < 4; i++) {
			pos.set(-1, -1);
			
			switch (i) {
				case 0: // North
				{
					pos.x = tilePos.x;
					pos.y = tilePos.y - 1;
				}
					break;
				case 1: // East
				{
					pos.x = tilePos.x + 1;
					pos.y = tilePos.y;
				}
					break;
				case 2: // South
				{
					pos.x = tilePos.x;
					pos.y = tilePos.y + 1;
				}
					break;
				case 3: // West
				{
					pos.x = tilePos.x - 1;
					pos.y = tilePos.y;
				}
					break;
			}
			
			if (pos.x >= 0 && pos.y >= 0 && pos.x < numColumns
					&& pos.y < numRows && !pos.isEquivalent(parentPos)) {
				int tileIndex = puzzle.pos2Index(pos);
				Tile tile = puzzle.tileAtIndex(tileIndex);
				
				if (tile != null && (isColorMagicActive || (tile.getColorKey() == originTile.getColorKey()
						|| tile.getColorKey() == TilePiece.kColorKeyWhite
						|| originTile.getColorKey() == TilePiece.kColorKeyWhite)))
					successors[numSuccessors++] = pos;
			}
		}
		
		Coord.freeCoord(pos);
		return numSuccessors;
	}

	@Override
	public Coord getDxy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getSearchWeighting(Coord tilePos) {
		if (puzzle == null || from.isEquivalent(tilePos) || to.isEquivalent(tilePos))
			return 0;
		
		int index = puzzle.pos2Index(tilePos), weighting = 0;
		Tile tile = puzzle.tileAtIndex(index);
		if (tile != null)
			weighting = puzzle.getSearchWeighting(index);
		
		return weighting;
	}

	@Override
	public int getMaxPathLength() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int getFoundPath(Coord[] path, int maxLength) {
		if (puzzle == null || searchState != SearchState.Succeeded)
			return 0;
		
		int i = 0;
		SearchNode node = astar.getSolutionStart();
		
		for (; i < maxLength; i++) {
			node = astar.getSolutionNext();
			if (node == null)
				break;
			path[i].set(node.x, node.y);
		}
		
		return i;
	}
	
	public boolean findPathForPlayer(Player player, final Coord from, final Coord to, int maxIterations) {
		if (puzzle == null)
			return true;
		
		assert(this.player == null) : "Did not discard previous player.";
		this.player = player;
		
		if (searchState != SearchState.Searching) {
			// Begin new search
			astar.freeSolutionNodes();
			this.from.set(from.x, from.y);
			this.to.set(to.x, to.y);
			this.dxy.set(from.x - to.x, from.y - to.y);
			// AStar will return these nodes to the Pool.
			SearchNode start = SearchNode.getSearchNode(from.x, from.y, this);
			SearchNode goal = SearchNode.getSearchNode(to.x, to.y, this);
			astar.setStartAndGoalStates(start, goal);
		}
		
		int i = 0;
		for (; i < maxIterations || maxIterations == -1; i++) {
			searchState = astar.searchStep();
			if (searchState != SearchState.Searching)
				break;
		}
		
		this.player = null;
		
		if (searchState == SearchState.Searching) {
			Gdx.app.log("PathFinder", "Path NOT found after " + i
					+ " steps. Continuing search next frame...");
			return false;
		} else {
			if (searchState == SearchState.Succeeded)
				Gdx.app.log("PathFinder", "Path found after " + i + " steps.");
			else
				Gdx.app.log("PathFinder", "Failed to find path with state: " + searchState.toString());
			return true;
		}
	}

	public Puzzle getDataProvider() { return puzzle; }
	
	public void setDataProvider(Puzzle puzzle) { this.puzzle = puzzle; }
	
	public boolean isBusy() {
		boolean busyState = searchState == SearchState.Searching;
		if (puzzle != null)
			busyState = busyState
			|| puzzle.isConveyorBeltActive()
			|| puzzle.isRotating()
			|| puzzle.isTileSwapping();
		return busyState;
	}
	
	public void enableSearchWeighting(boolean enable) {
		if (puzzle != null)
			puzzle.enableSearchWeighting(enable);
	}
	
	public void cancelSearch() {
		if (searchState == SearchState.Searching) {
			astar.cancelSearch();
			astar.searchStep();
		}
	}
	
	public int pos2Index(final Coord pos) {
		return puzzle != null ? puzzle.pos2Index(pos) : -1;
	}
	
	public Tile tileAtIndex(int index) {
		return puzzle != null ? puzzle.tileAtIndex(index) : null;
	}
}
