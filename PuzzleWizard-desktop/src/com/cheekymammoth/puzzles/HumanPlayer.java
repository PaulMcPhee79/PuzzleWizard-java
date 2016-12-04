package com.cheekymammoth.puzzles;


import com.cheekymammoth.puzzleFactories.PlayerFactory;
import com.cheekymammoth.utils.Coord;

public class HumanPlayer extends Player {
	private MirroredPlayer mirrorImage;
	
	public HumanPlayer() { }

//	public HumanPlayer(BufferedInputStream stream) throws IOException {
//		super(stream);
//	}
	
	@Override
	public PlayerType getType() { return PlayerType.HUMAN; }
	
	public MirroredPlayer getMirrorImage() { return mirrorImage; }
	
	public void setMirrorImage(MirroredPlayer value) {
		mirrorImage = value;
		if (mirrorImage != null)
			mirrorImage.setMoveOffsetDuration(getMoveOffsetDuration());
	}

	@Override
	public void broadcastProperties() {
		super.broadcastProperties();
		
		if (isColorMagicActive())
			notifyPropertyChange(Player.kValueColorMagic, this.getNumColorMagicMoves());
	}
	
	@Override
	public void beginMoveTo(Coord pos) {
		int prevX = getPosition().x, prevY = getPosition().y;
		super.beginMoveTo(pos);
		
		if (mirrorImage != null) {
			mirrorImage.forceCompleteMove();
			mirrorImage.setMoveOffsetDuration(getMoveOffsetDuration());
			mirrorImage.setQueuedMove(prevX - pos.x, prevY - pos.y);
		}
	}
	
	@Override
	public void idle() {
		super.idle();
		if (mirrorImage != null)
			mirrorImage.mirrorIdle();
	}
	
	@Override
	public void treadmill(int x, int y) {
		super.treadmill(x, y);
		
		if (mirrorImage != null)
			mirrorImage.treadmill(-x, -y);
	}
	
	@Override
	public void didFinishMoving() {
		super.didFinishMoving();
		if (mirrorImage != null) {
			// Puzzle state changes on move end, so sync players that they may react
			// to a common state.
			if (mirrorImage.isMoving())
				mirrorImage.didFinishMoving();
		}
	}
	
	@Override
	public void reset() {
		super.reset();
		setMirrorImage(null);
	}
	
	@Override
	public Player clone() {
		return PlayerFactory.getHumanPlayer(getColorKey(), getPosition(), getOrientation());
	}

	@Override
	public Player devClone() {
		return PlayerFactory.getHumanPlayer(getDevColorKey(), getDevPosition(), getDevOrientation());
	}

}
