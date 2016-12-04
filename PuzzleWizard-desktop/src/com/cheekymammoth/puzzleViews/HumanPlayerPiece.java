package com.cheekymammoth.puzzleViews;

import com.cheekymammoth.utils.PWDebug;

public class HumanPlayerPiece extends AnimPlayerPiece {
	public HumanPlayerPiece() {
		PWDebug.humanPlayerPieceCount++;
	}
	
	@Override
	protected String getIdleMidFramesPrefix() { return "idle_rim_lge_"; }
	
	@Override
    protected String getMovingMidFramesPrefix() { return "walk_rim_lge_"; }
}
