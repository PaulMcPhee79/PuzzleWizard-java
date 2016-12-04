package com.cheekymammoth.puzzles;

import com.cheekymammoth.gameModes.PuzzleMode;
import com.cheekymammoth.puzzleFactories.PlayerFactory;
import com.cheekymammoth.utils.Coord;

public class MirroredPlayer extends Player {
	private int numMovesRemaining;
	
	public MirroredPlayer() { }
	
	@Override
	public void devInit(int colorKey, Coord position, int orientation) {
		super.devInit(colorKey, position, orientation);
		numMovesRemaining = PuzzleMode.getMaxMirrorImageMoves();
	}
	
	@Override
	public PlayerType getType() { return PlayerType.MIRRORED; }
	
	@Override
	public String getCostumeName() { return "mirrored-player"; }
	
	public boolean hasExpired() { return getNumMovesRemaining() <= 0; }
	
	public int getNumMovesRemaining() { return numMovesRemaining; }
	
	public void setNumMovesRemaining(int value) {
		numMovesRemaining = value;
		notifyPropertyChange(Player.kValueMirrorImage, value);
	}
	
	@Override
	public void broadcastProperties() {
		super.broadcastProperties();
		
		notifyPropertyChange(Player.kValueMirrorImage, getNumMovesRemaining());
	}
	
	@Override
	public void moveTo(Coord pos) {
		if (getNumMovesRemaining() <= 0)
			return;
		
		super.moveTo(pos);
		setNumMovesRemaining(getNumMovesRemaining()-1);
	}
	
	@Override
	public void idle() {
		// Do nothing - don't call super. HumanPlayer will idle us via MirroredPlayer::mirrorIdle.
	}
	
	public void mirrorIdle() {
		super.idle();
	}
	
	@Override
	public void reset() {
		super.reset();
		setNumMovesRemaining(PuzzleMode.getMaxMirrorImageMoves());
	}
	
	@Override
	public Player clone() {
		return PlayerFactory.getMirroredPlayer(getColorKey(), getPosition(), getOrientation());
	}

	@Override
	public Player devClone() {
		return PlayerFactory.getMirroredPlayer(getDevColorKey(), getDevPosition(), getDevOrientation());
	}
}
