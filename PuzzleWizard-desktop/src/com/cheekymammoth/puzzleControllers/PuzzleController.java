package com.cheekymammoth.puzzleControllers;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.gameModes.PuzzleMode;
import com.cheekymammoth.locale.ILocalizable;
import com.cheekymammoth.puzzleEffects.PuzzleRibbon;
import com.cheekymammoth.puzzleEffects.SolvedAnimation;
import com.cheekymammoth.puzzleInputs.PlayerController;
import com.cheekymammoth.puzzleViews.PlayerHUD;
import com.cheekymammoth.puzzleViews.PuzzleBoard;
import com.cheekymammoth.puzzleio.GameProgressController;
import com.cheekymammoth.puzzles.Level;
import com.cheekymammoth.puzzles.Player;
import com.cheekymammoth.puzzles.Puzzle;
import com.cheekymammoth.resolution.IResDependent;
import com.cheekymammoth.sceneControllers.SceneController;
import com.cheekymammoth.sceneControllers.SceneUtils.PFCat;
import com.cheekymammoth.utils.LangFX;
import com.cheekymammoth.utils.Promo;

public class PuzzleController extends EventDispatcher implements IEventListener, IResDependent, ILocalizable, Disposable {
	public static final int EV_TYPE_PUZZLE_DID_BEGIN;
	public static final int EV_TYPE_PUZZLE_SOLVED_ANIMATION_COMPLETED;
	
	static {
		EV_TYPE_PUZZLE_DID_BEGIN = EventDispatcher.nextEvType();
		EV_TYPE_PUZZLE_SOLVED_ANIMATION_COMPLETED = EventDispatcher.nextEvType();
	}
	
	private boolean wasNextLevelUnlocked;
    private boolean wasPuzzleUnsolved;
    private int levelIndexCache = -1;
    private int puzzleIndexCache = -1;

    private Puzzle puzzle;             // Puzzle data
    private PuzzleBoard puzzleBoard;   // Puzzle view
    private PlayerHUD playerHUD;

    private PuzzleOrganizer puzzleOrganizer;

    private PlayerController playerController;

    private SolvedAnimation solvedAnimation;
    private PuzzleRibbon puzzleRibbon;

    private SceneController scene;
	
	public PuzzleController(SceneController scene) {
		this.scene = scene;
		
		playerHUD = new PlayerHUD(PFCat.PLAYER_HUD.ordinal(), new Rectangle(0, 0, LangFX.getPlayerHUDSettings()[1], 144));
		playerHUD.setVisible(!Promo.isPromoEnabled());
		scene.addProp(playerHUD);
		scene.registerLocalizable(playerHUD);
		
		Puzzle puzzle = Puzzle.getPuzzle(
				Puzzle.nextPuzzleID(),
				"My Puzzle",
				PuzzleMode.getNumColumns(),
				PuzzleMode.getNumRows());
		
		puzzleBoard = PuzzleBoard.getPuzzleBoard(PFCat.BOARD.ordinal(), puzzle, false);
		puzzleBoard.enableTouchPad(true);
		puzzleBoard.addEventListener(PuzzleBoard.EV_TYPE_DID_TRANSITION_IN, this);
		puzzleBoard.addEventListener(PuzzleBoard.EV_TYPE_DID_TRANSITION_OUT, this);
		
		puzzleOrganizer = new PuzzleOrganizer();
		puzzleOrganizer.addEventListener(PuzzleOrganizer.EV_TYPE_PUZZLE_LOADED, this);
		
		playerController = PlayerController.createPlayerController(puzzleBoard);
		scene.subscribeToInputUpdates(playerController);
		
		setPuzzle(puzzle);
		
		try {
			puzzleOrganizer.load(PuzzleMode.getLevelDataFilePath());
		} catch (IOException e) {
			Gdx.app.exit();
		}
		
		solvedAnimation = new SolvedAnimation(PFCat.SUB_DIALOG.ordinal(), this);
		
		puzzleRibbon = new PuzzleRibbon(PFCat.SUB_DIALOG.ordinal());
		puzzleRibbon.setPosition(scene.VW2(), scene.VH2());
		scene.registerLocalizable(puzzleRibbon);
		scene.addProp(puzzleRibbon);
		
		scene.registerLocalizable(this);
	}
	
	public boolean didLevelUnlock() {
		if (getPuzzle() != null) {
			if (!wasNextLevelUnlocked) {
				GameProgressController gpc = GameProgressController.GPC();
				if (gpc.getNumSolvedPuzzles() >= gpc.getNumPuzzles() - (gpc.getNumPuzzlesPerLevel() + 1))
					return gpc.getNumSolvedPuzzles() == gpc.getNumPuzzles() - gpc.getNumPuzzlesPerLevel();
				else
					return gpc.isLevelUnlocked(getPuzzle().getLevelIndex() + 1);
			}
		}
		
		return false;
	}
	
	public boolean didPuzzleGetSolved() {
		if (getPuzzle() != null && wasPuzzleUnsolved)
			return  GameProgressController.GPC().hasSolved(getPuzzle().getLevelIndex(), getPuzzle().getPuzzleIndex());
		else
			return false;
	}
	
	public Puzzle getPuzzle() { return puzzle; }
	
	private void setPuzzle(Puzzle value) {
		if (puzzle != null) {
			puzzle.removeEventListener(Puzzle.EV_TYPE_PLAYER_ADDED, this);
			puzzle.removeEventListener(Puzzle.EV_TYPE_PLAYER_REMOVED, this);
			puzzle.removeEventListener(Puzzle.EV_TYPE_UNSOLVED_PUZZLE_WILL_SOLVE, this);
			
			if (playerController != null)
				puzzle.deregisterView(playerController);
			Puzzle.freePuzzle(puzzle);
		}
		
		puzzle = value;
		
		if (puzzleBoard != null)
			puzzleBoard.setData(puzzle, false);
		
		if (puzzle != null) {
			if (playerController != null) {
				playerController.reset();
				puzzle.registerView(playerController);
			}
			
			puzzle.addEventListener(Puzzle.EV_TYPE_PLAYER_ADDED, this);
			puzzle.addEventListener(Puzzle.EV_TYPE_PLAYER_REMOVED, this);
			puzzle.addEventListener(Puzzle.EV_TYPE_UNSOLVED_PUZZLE_WILL_SOLVE, this);
			beginNewPuzzle();
		}
	}
	
	public PuzzleBoard getPuzzleBoard() { return puzzleBoard; }
	
	public PuzzleOrganizer getPuzzleOrganizer() { return puzzleOrganizer; }
	
	public void setPuzzleBoardPosition(float x, float y) {
		if (puzzleBoard != null) {
			puzzleBoard.updateBounds(x, y);
			if (playerController != null)
				playerController.updateBoardBounds();
		}
	}
	
	public int getNextUnsolvedPuzzleID() {
		if (getPuzzleOrganizer() == null || getPuzzle() == null)
			return -1;
		
		int nextUnsolvedPuzzleID = -1;
		Array<Puzzle> puzzles = getPuzzleOrganizer().getPuzzles();
		if (puzzles != null) {
			int startIndex = getPuzzleOrganizer().absolutePuzzleIndexForPuzzleID(getPuzzle().getID());
			if (startIndex != -1) {
				int nextUnsolvedIndex = GameProgressController.GPC().getNextUnsolvedPuzzleIndex(startIndex);
				if (nextUnsolvedIndex != -1) {
					if (nextUnsolvedIndex < puzzles.size) {
						Puzzle puzzle = puzzles.get(nextUnsolvedIndex);
						if (puzzle != null)
							nextUnsolvedPuzzleID = puzzle.getID();
					}
				}
			}
		}
		
		return nextUnsolvedPuzzleID;
	}
	
	private void refreshWasUnlocked() {
		if (getPuzzle() != null) {
			GameProgressController gpc = GameProgressController.GPC();
			if (gpc.getNumSolvedPuzzles() == gpc.getNumPuzzles() - (gpc.getNumPuzzlesPerLevel() + 1))
				wasNextLevelUnlocked = false;
			else
				wasNextLevelUnlocked = gpc.isLevelUnlocked(getPuzzle().getLevelIndex() + 1);
		}
	}
	
	private void refreshWasSolved() {
		if (getPuzzle() != null) {
			GameProgressController gpc = GameProgressController.GPC();
			this.wasPuzzleUnsolved = !gpc.hasSolved(getPuzzle().getLevelIndex(), getPuzzle().getPuzzleIndex());
		}
	}
	
	private void beginNewPuzzle() {
		refreshWasUnlocked();
		refreshWasSolved();
		dispatchEvent(EV_TYPE_PUZZLE_DID_BEGIN, this);
	}
	
	private Vector2 playerHUDVectorCache = new Vector2();
	private void repositionPlayerHUD() {
		PuzzleBoard board = getPuzzleBoard();
		if (board != null && playerHUD != null) {
			float hudHeight = playerHUD.getHeight();
			Rectangle boardBounds = board.getBoardBounds();
			Vector2 scaleBoardDimensions = board.getScaledBoardDimensions();
			
			playerHUDVectorCache.set(boardBounds.x, boardBounds.y);
			playerHUDVectorCache = board.localToStageCoordinates(playerHUDVectorCache);
			playerHUD.setPosition(
					playerHUDVectorCache.x + scaleBoardDimensions.x / 2,
					playerHUDVectorCache.y + scaleBoardDimensions.y + 0.05f * hudHeight);
		}
	}
	
	public void displaySolvedAnimation(float fromX, float fromY, float toX, float toY) {
		if (solvedAnimation.isAnimating())
			return;
		
		scene.addProp(solvedAnimation);
		solvedAnimation.animate(fromX, fromY, toX, toY, 1.0f, 0f);
	}
	
	public void displayPuzzleRibbon() {
		if (puzzleRibbon == null || getPuzzle() == null || getPuzzleOrganizer() == null)
			return;
		
		PuzzleOrganizer puzzleOrganizer = getPuzzleOrganizer();
		Puzzle puzzle = getPuzzle();
		Level level = puzzleOrganizer.levelForPuzzle(puzzle);
		if (level != null) {
			int levelIndex = puzzleOrganizer.levelIndexForPuzzleID(puzzle.getID());
			
			if (levelIndex >= 0 && levelIndex < PuzzleMode.getNumLevels()) {
				puzzleRibbon.setLevelText(PuzzleMode.kLevelNames[levelIndex]);
				puzzleRibbon.setLevelColor(PuzzleMode.kLevelColors[levelIndex]);
				puzzleRibbon.setPuzzleText(puzzle.getName());
				puzzleRibbon.animate(2f);
			}
		}
	}
	
	public void hideSolvedAnimation() {
		solvedAnimation.stopAnimating();
		scene.removeProp(solvedAnimation);
	}
	
	public void hidePuzzleRibbon() {
		puzzleRibbon.stopAnimating();
	}
	
	public void enableMenuMode(boolean enable) {
		PuzzleBoard board = getPuzzleBoard();
		if (board != null) {
			board.enableMenuMode(enable, false);
			board.setVisible(!enable);
			
			if (!enable)
				repositionPlayerHUD();
		}
	}
	
	public void refreshColorScheme() {
		PuzzleBoard board = getPuzzleBoard();
		if (board != null)
			board.refreshColorScheme();
	}
	
	public boolean loadPuzzleByID(int puzzleID) {
		boolean didPuzzleLoadSuccessfully = false;
		puzzleIndexCache = puzzleOrganizer.levelBasedPuzzleIndexForPuzzleID(puzzleID);
		levelIndexCache = puzzleOrganizer.levelIndexForPuzzleID(puzzleID);
		puzzleOrganizer.loadPuzzleByID(puzzleID);
		didPuzzleLoadSuccessfully = getPuzzle() != null && getPuzzle().getID() == puzzleID;
		
		puzzleRibbon.stopAnimating();
		if (didPuzzleLoadSuccessfully)
			GameProgressController.GPC().setPlayed(true, levelIndexCache, puzzleIndexCache);
		
		return didPuzzleLoadSuccessfully;
	}
	
	public void resetCurrentPuzzle() {
		PuzzleBoard currentBoard = getPuzzleBoard();
		if (currentBoard == null || currentBoard.isTransitioning())
			return;
		
		if (playerController != null) {
			playerController.enable(false);
			playerController.reset();
		}
		
		PuzzleBoard board = PuzzleBoard.getPuzzleBoard(PFCat.BOARD.ordinal(), getPuzzle().clone(), true);
		board.setPosition(currentBoard.getX(), currentBoard.getY());
		board.addEventListener(PuzzleBoard.EV_TYPE_DID_TRANSITION_OUT, this);
		scene.addToJuggler(board);
		scene.addProp(board);
		
		board.transitionOut(0.5f, 0.1f, 0.1f / 2);
		
		currentBoard.softReset();
		currentBoard.transitionIn(0.5f, 0.1f, 0.1f / 2);
		beginNewPuzzle();
		scene.playSound("reset");
	}
	
	public void cancelEnqueuedActions() {
		if (playerController != null)
			playerController.reset();
		hidePuzzleRibbon();
	    hideSolvedAnimation();
	}
	
	public void advanceTime(float dt) {
		if (puzzleBoard != null)
			puzzleBoard.advanceTime(dt);
		if (puzzle != null)
			puzzle.processPrevMovements();
		if (playerController != null)
			playerController.advanceTime(dt);
		if (puzzle != null)
			puzzle.processNextMovements();
		if (puzzleBoard != null)
			puzzleBoard.postAdvanceTime(dt);
	}
	
	@Override
	public void localeDidChange(String fontKey, String FXFontKey) {
		if (puzzleBoard != null)
			puzzleBoard.localeDidChange(fontKey, FXFontKey);
	}

	@Override
	public void onEvent(int evType, Object evData) {
		if (evType == Puzzle.EV_TYPE_PLAYER_ADDED) {
			Player player = (Player)evData;
			if (player != null) {
				if (player.getType() == Player.PlayerType.HUMAN && playerController != null
						&& playerController.getPlayer() == null) {
					playerController.setPlayer(player);
				}
				if (playerHUD != null)
					playerHUD.setPlayer(player);
				player.broadcastProperties();
			}
		} else if (evType == Puzzle.EV_TYPE_PLAYER_REMOVED) {
			Player player = (Player)evData;
			if (player != null) {
				if (playerController != null && playerController.getPlayer() == player)
					playerController.setPlayer(null);
				if (playerHUD != null && playerHUD.getPlayer() == player)
					playerHUD.setPlayer(null);
			}
		} else if (evType == Puzzle.EV_TYPE_UNSOLVED_PUZZLE_WILL_SOLVE) {
			dispatchEvent(evType, evData);
		} else if (evType == PuzzleOrganizer.EV_TYPE_PUZZLE_LOADED) {
			if (playerHUD != null)
				playerHUD.setPlayer(null);
			
			Puzzle puzzle = (Puzzle)evData;
			if (puzzle != null) {
				puzzle.setPuzzleIndex(puzzleIndexCache);
				puzzle.setLevelIndex(levelIndexCache);
				setPuzzle(puzzle);
				
				if (playerController != null) {
					playerController.reset();
					Player player = puzzle.getAnyHumanPlayer();
					if (player != null) {
						playerController.setPlayer(player);
						if (playerHUD != null)
							playerHUD.setPlayer(player);
						player.broadcastProperties();
					}
				}
			}
		} else if (evType == SolvedAnimation.EV_TYPE_ANIMATION_COMPLETED) {
			scene.removeProp(solvedAnimation);
			dispatchEvent(EV_TYPE_PUZZLE_SOLVED_ANIMATION_COMPLETED, this);
		} else if (evType == PuzzleBoard.EV_TYPE_DID_TRANSITION_IN) {
			if (playerController != null)
				playerController.enable(true);
		} else if (evType == PuzzleBoard.EV_TYPE_DID_TRANSITION_OUT) {
			PuzzleBoard board = (PuzzleBoard)evData;
			if (board != null) {
				board.removeEventListener(PuzzleBoard.EV_TYPE_DID_TRANSITION_IN, this);
				board.removeEventListener(PuzzleBoard.EV_TYPE_DID_TRANSITION_OUT, this);
				scene.removeFromJuggler(board);
				scene.removeProp(board);
				PuzzleBoard.freePuzzleBoard(board);
			}
		}
	}
	
	@Override
	public void resolutionDidChange(int width, int height) {
		repositionPlayerHUD();
		puzzleRibbon.resolutionDidChange(width, height);
	}

	@Override
	public void dispose() {
		// TODO Return puzzle and puzzleboard to Pools.
		scene.deregisterLocalizable(puzzleRibbon);
		scene.deregisterLocalizable(this);
	}
}
