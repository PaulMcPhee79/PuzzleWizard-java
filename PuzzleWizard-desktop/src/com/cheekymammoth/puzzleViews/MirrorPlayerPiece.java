package com.cheekymammoth.puzzleViews;

import com.cheekymammoth.utils.PWDebug;

public class MirrorPlayerPiece extends AnimPlayerPiece {
	public MirrorPlayerPiece() {
		PWDebug.mirrorPlayerPieceCount++;
	}
	
	@Override
	protected String getIdleMidFramesPrefix() { return "idle_rim_sml_"; }
	
	@Override
    protected String getMovingMidFramesPrefix() { return "walk_rim_sml_"; }
}
