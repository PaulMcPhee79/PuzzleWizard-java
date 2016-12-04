package com.cheekymammoth.pathFinding;

import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.cheekymammoth.utils.Coord;

public class SearchNode implements Poolable {
	public int x, y;
	private int cost = -1;
	public ISearchProvider searchProvider;
	
	public SearchNode(int tileX, int tileY, ISearchProvider searchProvider) {
		x = tileX;
		y = tileY;
		this.searchProvider = searchProvider;
		
		for (int i = 0; i < successors.length; i++)
			successors[i] = new Coord();
	}
	
	public float getGoalDistanceEstimate(SearchNode nodeGoal) {
		if (searchProvider == null)
			throw new NullPointerException("Null ISearchProvider.");
				
		float dx1 = (float)x - (float)nodeGoal.x;
		float dy1 = (float)y - (float)nodeGoal.y;
		float manhattan = Math.abs(dx1) +  Math.abs(dy1);
		// Must satisfy for shortest path: p < MIN(getCost()) / MAX_PATH_LEN.
		float p = 1f / (float)searchProvider.getMaxPathLength();
		float heuristic = manhattan * (1f + p);
		return heuristic;
	}
	
	public boolean isGoal(SearchNode nodeGoal) {
		return isSameState(nodeGoal);
	}
	
	private Coord successors[] = new Coord[4];
	public boolean getSuccessors(AStar astar, SearchNode parentNode, int numAncestors) {
		if (searchProvider == null || astar == null)
			throw new NullPointerException("Null ISearchProvider.");
		
		Coord parentPos = parentNode != null
				? Coord.obtainCoord(parentNode.x, parentNode.y)
				: Coord.obtainCoord(-1, -1);
		Coord tilePos = Coord.obtainCoord(x, y);
		int numSuccessors = searchProvider.getSuccessors(tilePos, parentPos, successors, numAncestors);
		for (int i = 0; i < numSuccessors; i++) {
			// Astar will free added SearchNodes.
			SearchNode successor = getSearchNode(successors[i].x, successors[i].y, searchProvider);
			astar.addSuccessor(successor);
		}
		
		Coord.freeCoord(parentPos);
		Coord.freeCoord(tilePos);
		
		return true;
	}
	
	public float getCost(SearchNode successor) {
		if (searchProvider == null)
			throw new NullPointerException("Null ISearchProvider.");
		
		if (cost == -1) {
			Coord coord = Coord.obtainCoord(x, y);
			cost = isGoal(successor) ? 0 : 1 + 1000 * searchProvider.getSearchWeighting(coord);
			Coord.freeCoord(coord);
		}
		return cost;
	}
	
	public boolean isSameState(SearchNode rhs) {
		return rhs != null && x == rhs.x && y == rhs.y;
	}

	@Override
	public void reset() {
		x = y = 0;
		cost = -1;
		searchProvider = null;
	}
	
	public static SearchNode getSearchNode() {
		return Pools.obtain(SearchNode.class);
	}
	
	public static SearchNode getSearchNode(int tileX, int tileY, ISearchProvider searchProvider) {
		SearchNode searchNode = getSearchNode();
		searchNode.x = tileX;
		searchNode.y = tileY;
		searchNode.searchProvider = searchProvider;
		return searchNode;
	}
	
	public static void freeSearchNode(SearchNode searchNode) {
		if (searchNode != null)
			Pools.free(searchNode);
	}
}
