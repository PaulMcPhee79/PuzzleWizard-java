package com.cheekymammoth.pathFinding;

import com.cheekymammoth.utils.Coord;

public interface ISearchProvider {
	int getSuccessors(final Coord tilePos, final Coord parentPos, Coord[] successors, int numAncestors);
	Coord getDxy();
	int getSearchWeighting(final Coord tilePos);
	int getMaxPathLength();
}
