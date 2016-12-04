package com.cheekymammoth.puzzleInputs;

import com.badlogic.gdx.utils.Array;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.input.CMInputs;
import com.cheekymammoth.input.IInteractable;
import com.cheekymammoth.puzzleViews.IPlayerView;
import com.cheekymammoth.puzzleViews.IPuzzleView;
import com.cheekymammoth.puzzleViews.PuzzleBoard;
import com.cheekymammoth.puzzles.Player;
import com.cheekymammoth.utils.Coord;

public abstract class PlayerController extends EventDispatcher implements
		IEventListener, IPuzzleView, IPlayerView, IInteractable {

	public static final int EV_TYPE_PLAYER_WILL_MOVE;
	public static final int EV_TYPE_PLAYER_STOPPED_SHORT;
	public static final int EV_TYPE_PATH_NOT_FOUND;
	
	static {
		EV_TYPE_PLAYER_WILL_MOVE = EventDispatcher.nextEvType();
		EV_TYPE_PLAYER_STOPPED_SHORT = EventDispatcher.nextEvType();
		EV_TYPE_PATH_NOT_FOUND = EventDispatcher.nextEvType();
	}
	
	public static PlayerController createPlayerController(PuzzleBoard board) {
		// TODO: Create PlayerControllerMB (mobile)
		return new PlayerControllerDT();
	}
	
	private boolean isEnabled = true;
	protected Player player;
	
	protected PlayerController() {
		
	}
	
	public void reset() { }
	
	public void updateBoardBounds() { }
	
	public void advanceTime(float dt) { }
	
	public boolean isEnabled() { return isEnabled; }
	
	public void enable(boolean enable) { isEnabled = enable; }

	public Player getPlayer() { return player; }
	
	public void setPlayer(Player value) { player = value; }
	
	public Array<Coord> getPath() { return null; }
	
	public int getPathLength() { return 0; }
	
	// TODO
	//public void setPathFinder(PathFinder value) { }
	
	@Override
	public int getInputFocus() { return 0; }

	@Override
	public void didGainFocus() { }

	@Override
	public void willLoseFocus() { }

	@Override
	public void update(CMInputs input) { }

	@Override
	public void playerValueDidChange(int code, int value) { }

	@Override
	public void willBeginMoving() { }

	@Override
	public void didFinishMoving() { }

	@Override
	public void didIdle() { }

	@Override
	public void puzzleSoundShouldPlay(String soundName) { }

	@Override
	public void puzzlePlayerWillMove(Player player) { }

	@Override
	public void puzzlePlayerDidMove(Player player) { }

	@Override
	public void puzzleShieldDidDeploy(int tileIndex) { }

	@Override
	public void puzzleShieldWasWithdrawn(int tileIndex) { }

	@Override
	public void puzzleTilesShouldRotate(int[] tileIndexes) { }

	@Override
	public void puzzleTileSwapWillBegin(int[][] swapIndexes, boolean isCenterValid) { }

	@Override
	public void puzzleConveyorBeltWillMove(Coord moveDir, int wrapIndex, int[] tileIndexes) { }

	@Override
	public void puzzleWasSolved(int tileIndex) { }

	@Override
	public void onEvent(int evType, Object evData) { }
	
}
