package com.cheekymammoth.puzzleViews;

import java.util.Iterator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entries;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.ObjectMap.Keys;
import com.badlogic.gdx.utils.ObjectMap.Values;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.SnapshotArray;
import com.cheekymammoth.animations.IAnimatable;
import com.cheekymammoth.animations.MovieReel;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.gameModes.PuzzleMode;
import com.cheekymammoth.graphics.CMAtlasSprite;
import com.cheekymammoth.graphics.CMSprite;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.graphics.TouchPad;
import com.cheekymammoth.locale.ILocalizable;
import com.cheekymammoth.locale.Localizer.LocaleType;
import com.cheekymammoth.puzzleControllers.PuzzleController;
import com.cheekymammoth.puzzleControllers.ShieldManager;
import com.cheekymammoth.puzzleEffects.EffectFactory;
import com.cheekymammoth.puzzleEffects.PuzzleEffects;
import com.cheekymammoth.puzzleEffects.Shield;
import com.cheekymammoth.puzzleEffects.TileConveyorBelt;
import com.cheekymammoth.puzzleEffects.TileRotator;
import com.cheekymammoth.puzzleEffects.TileSwapper;
import com.cheekymammoth.puzzleFactories.PlayerFactory;
import com.cheekymammoth.puzzleFactories.TileFactory;
import com.cheekymammoth.puzzleViews.TilePiece.AestheticState;
import com.cheekymammoth.puzzles.MirroredPlayer;
import com.cheekymammoth.puzzles.Player;
import com.cheekymammoth.puzzles.Player.PlayerType;
import com.cheekymammoth.puzzles.Puzzle;
import com.cheekymammoth.puzzles.PuzzleHelper;
import com.cheekymammoth.puzzles.Tile;
import com.cheekymammoth.sceneControllers.PlayfieldController;
import com.cheekymammoth.ui.TextUtils;
import com.cheekymammoth.utils.Coord;
import com.cheekymammoth.utils.FloatTweener;
import com.cheekymammoth.utils.PWDebug;
import com.cheekymammoth.utils.Promo;
import com.cheekymammoth.utils.Transitions;
import com.cheekymammoth.utils.Utils;

public final class PuzzleBoard extends Prop implements IEventListener, IPuzzleView, ITileDecorator,
															IAnimatable, ILocalizable, Poolable {
	public static final int EV_TYPE_DID_TRANSITION_IN;
	public static final int EV_TYPE_DID_TRANSITION_OUT;
	public static final int EV_TYPE_RESET_REQUESTED;
	
	// Don't overlap transition in/out tags range. They require space equal to the board's tile count.
	private static final int kTransitionInTag = 1000;
	private static final int kTransitionOutTag = 2000;
	
	private static final int kTweenerTeleportTag = 3002;
	private static final int kTweenerKeyTag = 3003;
	private static final int kTweenerIqTag = 3004;
	
	private static final int kLowerCanvas = 0;
    private static final int kLowerPlayerCanvas = 1;
    private static final int kMidCanvas = 2;
    private static final int kUpperPlayerCanvas = 3;
    private static final int kUpperCanvas = 4;
    private static final int kMaxCanvas = kUpperCanvas;
    private static final int kCanvasLen = kMaxCanvas + 1;
    
    private static final float kScaledTileWidth = 176f;
    private static final float kTileWidth = 144f;
    private static final float kTileHeight = 144f;
    private static final float kTileScaleX = kScaledTileWidth / kTileWidth;
    
    private static final float kDefaultTransitionDuration = 1.0f;
	
	static {
		EV_TYPE_DID_TRANSITION_IN = EventDispatcher.nextEvType();
		EV_TYPE_DID_TRANSITION_OUT = EventDispatcher.nextEvType();
		EV_TYPE_RESET_REQUESTED = EventDispatcher.nextEvType();
	}
	
	private boolean isLocked;
    private boolean isTransitioning;
    private boolean isMenuModeEnabled;
    private boolean ownsPuzzle;
    private float lastPlayerTransitionDelay;
    private Puzzle puzzle;

    private MovieReel colorArrowReel;
    private TilePiece[] tilePieces;
    private ObjectMap<Player, PlayerPiece> playerPieces = new ObjectMap<Player, PlayerPiece>(2);
    private Array<Player> addQueue = new Array<Player>(2);
    private Array<Player> removeQueue = new Array<Player>(2);

    private Array<TileConveyorBelt> animatingConveyorBelts = new Array<TileConveyorBelt>(true, 3, TileConveyorBelt.class);
    private Array<TileRotator> animatingRotators = new Array<TileRotator>(true, 3, TileRotator.class);
    private Array<TileSwapper> animatingTileSwappers = new Array<TileSwapper>(true, 3, TileSwapper.class);

    private Label iqLabel;
    private CMSprite iqSprite;
    private Prop iqSpriteProp;
    private Prop iqProp;
    
    private Prop highlighter;
    private Prop[] canvas = new Prop[kCanvasLen];

    private FloatTweener teleportTweener;
    private FloatTweener keyTweener;
    private FloatTweener iqTweener;

    private ShieldManager shieldManager;
	private Array<FloatTweener> transitions;
	
	private TouchPad touchPad;
	
	public PuzzleBoard() {
		this(-1);
		PWDebug.puzzleBoardCount++;
	}

	public PuzzleBoard(int category) {
		super(category);
		
		for (int i = 0, n = canvas.length; i < n; i++) {
			Prop prop = new Prop();
			prop.setTransform(true);
			canvas[i] = prop;
			addActor(prop);
		}
		
		this.setTransform(true);
		setTouchable(Touchable.enabled);
		
		teleportTweener = new FloatTweener(0, Transitions.linear, this);
		teleportTweener.setTag(kTweenerTeleportTag);
		keyTweener = new FloatTweener(0, Transitions.linear, this);
		keyTweener.setTag(kTweenerKeyTag);
		iqTweener = new FloatTweener(1.0f, Transitions.linear, this);
		iqTweener.setTag(kTweenerIqTag);
		
		Array<AtlasRegion> frames = scene.textureRegionsStartingWith("color-streak");
		colorArrowReel = new MovieReel(frames, PainterDecoration.kColorArrowFps);
	}
	
	public static PuzzleBoard getPuzzleBoard(int category, Puzzle puzzle, boolean ownsPuzzle) {
		PuzzleBoard puzzleBoard = Pools.obtain(PuzzleBoard.class);
		puzzleBoard.setCategory(category);
		puzzleBoard.setData(puzzle, ownsPuzzle);
		return puzzleBoard;
	}
	
	public static void freePuzzleBoard(PuzzleBoard puzzleBoard) {
		if (puzzleBoard != null)
			Pools.free(puzzleBoard);
	}
	
	private void setup(Puzzle puzzle) {
		assert(puzzle != null && puzzle.getNumTiles() > 0) : "Invalid state in PuzzleBoard::setup()";
		
		if (tilePieces == null || tilePieces.length != puzzle.getNumTiles())
			tilePieces = new TilePiece[puzzle.getNumTiles()];
		
		int numColumns = puzzle.getNumColumns(), numRows = puzzle.getNumRows();
		for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                int tileIndex = i * numColumns + j;
                
                Tile tile = puzzle.tileAtIndex(tileIndex);
                if (tilePieces[tileIndex] == null) {
	                TilePiece tilePiece = TileFactory.getTilePiece(tile);
	                tilePiece.setDecorator(this);
	                Vector2 tileDims = tilePiece.getTileDimensions();
	                tilePiece.setPosition(j * tileDims.x, (numRows - (i+1)) * tileDims.y);
	                canvas[kLowerCanvas].addActor(tilePiece);
	                tilePieces[tileIndex] = tilePiece;
                } else
                	tilePieces[tileIndex].setData(tile);
            }
        }
		
		Vector2 tileDims = getTileDimensions();
		canvas[kLowerCanvas].setContentSize(numColumns * tileDims.x, numRows * tileDims.y);
        setCanvasScale(calculateCanvasScaler());
		
		if (transitions == null)
			transitions = new Array<FloatTweener>(true, tilePieces.length, FloatTweener.class);
		
		// Ensure we have exactly one transition tweener for each TilePiece
		for (int i = transitions.size; i < tilePieces.length; i++)
			transitions.add(FloatTweener.getTweener(Transitions.linear, this));
		for (int i = transitions.size; i > tilePieces.length; i--) {
			FloatTweener tweener = transitions.get(i-1);
			if (tweener != null) {
				transitions.removeIndex(i-1);
				Pools.free(tweener);
			}
		}

		Array<Player> players = puzzle.getPlayers();
		if (players != null) {
			for (int i = 0, n = players.size; i < n; i++)
				addPlayerPiece(players.get(i));
		}
		
		Vector2 boardDims = getBoardDimensions();
		if (shieldManager == null) {
			shieldManager = new ShieldManager(getCategory(), numColumns, numRows, new Rectangle(
					-tileDims.x / 2, -tileDims.y / 2, boardDims.x, boardDims.y+tileDims.y));
			canvas[kMidCanvas].addActor(shieldManager);
		}
		
		teleportTweener.resetTween(
				TeleportDecoration.kRuneGlowOpacityMax,
				TeleportDecoration.kRuneGlowOpacityMin,
				TeleportDecoration.kRuneGlowDuration,
				0);
		keyTweener.resetTween(0, 6, 6, 0);
		
		// Highlighter (for menus)
		if (highlighter == null) {
			highlighter = new Prop();
			highlighter.setTransform(true);
			canvas[kLowerCanvas].addActorAt(0, highlighter);
			
			CMAtlasSprite highlight = new CMAtlasSprite(scene.textureRegionByName("puzzle-highlight"));
			highlight.centerContent();
			highlighter.addSpriteChild(highlight);
			
			highlighter.setPosition(boardDims.x / 2, boardDims.y / 2);
			highlighter.setSize(highlight.getWidth(), highlight.getHeight());
			highlighter.setScale(
					(1.335f * boardDims.x) / highlighter.getWidth(),
					(1.41f * boardDims.y) / highlighter.getHeight());
			highlighter.setVisible(false);
		}
		
		// IQ Tag
		if (iqSprite == null) {
			iqSprite = new CMAtlasSprite(scene.textureRegionByName("iq-tag"));
			iqSprite.centerContent();
			
			iqSpriteProp = new Prop();
			iqSpriteProp.setTransform(true);
			iqSpriteProp.setPosition(-14f + iqSprite.getWidth() / 2, -iqSprite.getHeight() / 2);
			iqSpriteProp.setScale(1.05f, 1f);
			iqSpriteProp.setSize(
					iqSpriteProp.getScaleX() * iqSprite.getWidth(),
					iqSpriteProp.getScaleY() * iqSprite.getHeight());
			iqSpriteProp.addSpriteChild(iqSprite);

//			iqShadowSprite = new CMAtlasSprite(scene.textureRegionByName("iq-tag-shadow"));
//			iqShadowSprite.centerContent();
//			
//			Prop iqShadowSpriteProp = new Prop();
//			iqShadowSpriteProp.setTransform(true);
//			iqShadowSpriteProp.setPosition(
//					-14f + iqSprite.getWidth() / 2 + 12,
//					-iqSprite.getHeight() / 2 - 12);
//			iqShadowSpriteProp.setScale(
//					1.25f * iqSpriteProp.getScaleX() * (iqSprite.getWidth() / iqShadowSprite.getWidth()),
//					1.25f * iqSpriteProp.getScaleY() * (iqSprite.getHeight() / iqShadowSprite.getHeight()));
//			iqShadowSpriteProp.addSpriteChild(iqShadowSprite);
			
			iqLabel = TextUtils.createIQ("000\n000", 26, TextUtils.kAlignCenter, Color.WHITE);
			iqLabel.setSize(iqLabel.getTextBounds().width, iqLabel.getTextBounds().height);
			iqLabel.setPosition(
					8,
					iqSpriteProp.getY() + (iqSprite.getHeight() -
					iqLabel.getHeight()) / 2 + 2 -iqSprite.getHeight() / 2);
			
			iqProp = new Prop();
			iqProp.setTransform(true);
			iqProp.setPosition(boardDims.x - tileDims.x / 2, boardDims.y - tileDims.y / 2);
			iqProp.setOrigin(0, 1);
			iqProp.setScale(1f, 1.33f);
			
			//iqProp.addActor(iqShadowSpriteProp);
			iqProp.addActor(iqSpriteProp);
			iqProp.addActor(iqLabel);
			canvas[kLowerCanvas].addActorAfter(highlighter, iqProp);
			
			iqTweener.resetTween(0f);
		}
		
		iqSprite.setColor(iqSprite.getColor().set(PuzzleHelper.colorForIQ(puzzle.getIQ())));
		updateIQText(puzzle);
		if (!isTransitioning) iqProp.setColor(Utils.setA(iqProp.getColor(), 1));
		
		Vector2 scaledBoardDims = getScaledBoardDimensions();
		setContentSize(scaledBoardDims.x, scaledBoardDims.y);
		setVisible(true);
	}
	
	public void enableTouchPad(boolean enable) {
		if (touchPad == null) {
			touchPad = new TouchPad(-1, this);
			touchPad.setPosition(-kTileWidth / 2, -kTileHeight / 2);
			touchPad.setContentSize(10 * kTileWidth, 8 * kTileHeight);
		}
		
		touchPad.remove();
		
		if (enable)
			addActor(touchPad);
	}
	
	public boolean isTransitioning() { return isTransitioning; }
	
	private void updateIQText(Puzzle puzzle) {
		if (iqLabel != null && puzzle != null) {
			String iqPrefix = "IQ";
			LocaleType locale = scene.getLocale();
			switch (locale) {
				case ES:
					iqPrefix = "CI";
					break;
				case FR:
				case IT:
					iqPrefix = "QI";
					break;
				default:
					break;
			}
			
			iqLabel.setText(iqPrefix + "\n" + puzzle.getIQ());
		}
	}
	
	private void setTransitioning(boolean value) {
		if (puzzle != null) {
			if (value)
				puzzle.pause();
			else
				puzzle.resume();
		}
		
		isTransitioning = value;
	}
	
	private Vector2 tileDimsCache = new Vector2();
	public Vector2 getTileDimensions() {
		if (tilePieces == null || tilePieces.length == 0 || tilePieces[0] == null)
			tileDimsCache.set(0, 0);
		else {
			Vector2 tileDims = tilePieces[0].getTileDimensions();
			tileDimsCache.set(tileDims.x, tileDims.y);
		}
		
		return tileDimsCache;
	}
	
	private Vector2 boardDimsCache = new Vector2();
	public Vector2 getBoardDimensions() {
		if (puzzle == null || tilePieces == null || tilePieces.length == 0 || tilePieces[0] == null)
			boardDimsCache.set(0, 0);
        else {
        	Vector2 tileDims = getTileDimensions();
        	boardDimsCache.set(tileDims.x * puzzle.getNumColumns(), tileDims.y * puzzle.getNumRows());
        }
		
		return boardDimsCache;
	}
	
	private Vector2 scaledTileDimsCache = new Vector2();
	public Vector2 getScaledTileDimensions() {
		scaledTileDimsCache.set(getTileDimensions());
		scaledTileDimsCache.x *= getScaleX();
		scaledTileDimsCache.y *= getScaleY();
		return scaledTileDimsCache;
	}
	
	private Vector2 scaledBoardDimsCache = new Vector2();
	public Vector2 getScaledBoardDimensions() {
		scaledBoardDimsCache.set(getBoardDimensions());
		scaledBoardDimsCache.x *= getScaleX();
		scaledBoardDimsCache.y *= getScaleY();
		return scaledBoardDimsCache;
	}
	
	private Rectangle boardBoundsCache = new Rectangle();
	public Rectangle getBoardBounds() {
		Vector2 tileDims = getTileDimensions();
		Vector2 boardDims = getBoardDimensions();
		boardBoundsCache.set(-tileDims.x / 2, -tileDims.y / 2, boardDims.x, boardDims.y);
		return boardBoundsCache;
	}
	
	public Prop getIqSpriteProp()
	{
		return iqSpriteProp;
	}
	
//	private Vector2 globalBoardClipTopLeftCache = new Vector2();
//	private Vector2 globalBoardClipBtmRightCache = new Vector2();
//	private Rectangle globalBoardClipBoundsCache = new Rectangle();
//	public Rectangle getGlobalBoardClippingBounds() {
//		if (tilePieces == null || tilePieces.length == 0)
//			globalBoardClipBoundsCache.set(0, 0, 0, 0);
//		else {
//			TilePiece topLeftTile = tilePieces[0];
//			TilePiece btmRightTile = tilePieces[tilePieces.length - 1];
//			
//			globalBoardClipTopLeftCache.set(0, topLeftTile.getTileBounds().height);
//			globalBoardClipTopLeftCache = topLeftTile.localToStageCoordinates(globalBoardClipTopLeftCache);
//			globalBoardClipTopLeftCache = stageToLocalCoordinates(globalBoardClipTopLeftCache);
//			
//			globalBoardClipBtmRightCache.set(btmRightTile.getTileBounds().width, 0);
//			globalBoardClipBtmRightCache = topLeftTile.localToStageCoordinates(globalBoardClipBtmRightCache);
//			globalBoardClipBtmRightCache = stageToLocalCoordinates(globalBoardClipBtmRightCache);
//			globalBoardClipBoundsCache.set(
//					globalBoardClipTopLeftCache.x,
//					globalBoardClipTopLeftCache.y,
//					globalBoardClipBtmRightCache.x - globalBoardClipTopLeftCache.x,
//					globalBoardClipTopLeftCache.y - globalBoardClipBtmRightCache.y);
//		}
//		
//		return globalBoardClipBoundsCache;
//	}
	
//	private Iterator<Player> getPlayerKeyIterator() {
//		Keys<Player> keys = playerPieces.keys();
//		return keys.iterator();
//	}
	
	private Iterator<PlayerPiece> getPlayerValueIterator() {
		Values<PlayerPiece> values = playerPieces.values();
		return values.iterator();
	}
	
	private Iterator<Entry<Player, PlayerPiece>> getPlayerEntryIterator() {
		Entries<Player, PlayerPiece> entries = playerPieces.entries();
		return entries.iterator();
	}
	
	public void updateBounds(float x, float y) {
		this.setPosition(x, y);
		
		if (shieldManager != null) {
			Vector2 tileDims = getTileDimensions();
			Vector2 boardDims = getBoardDimensions();
			shieldManager.setViewableRegion(new Rectangle(
					-tileDims.x / 2,
					-tileDims.y / 2,
					boardDims.x,
					boardDims.y+tileDims.y));
		}
	}
	
	public void refreshColorScheme() {
		if (tilePieces != null) {
			for (int i = 0, n = tilePieces.length; i < n; i++)
				tilePieces[i].syncWithData();
		}
		
		Iterator<PlayerPiece> playerIter = getPlayerValueIterator();
		while (playerIter.hasNext())
			playerIter.next().refreshAesthetics();
	}
	
	private void transitionIqTag(boolean inwards, float duration, float delay) {
		if (iqProp != null && iqTweener != null) {
            if (inwards) {
            	iqProp.setColor(Utils.setA(iqProp.getColor(), 0));
                iqTweener.resetTween(0f, 1f, duration, delay);
            } else {
            	iqProp.setColor(Utils.setA(iqProp.getColor(), 1));
                iqTweener.resetTween(1f, 0f, duration, delay);
            }
        }
	}
	
	private void transition(int tag, float duration, float columnDelay, float rowDelay, Interpolation interpolation) {
		if (puzzle == null || duration == 0)
			return;
		assert(transitions.size == tilePieces.length) : "PuzzleBoard::transition - invalid internal state.";
		
		int numCols = puzzle.getNumColumns();
		int columnIter = 0, rowIter = 0;
		float deltaX = scene.getStage().getWidth();
		for (int i = 0, n = transitions.size; i < n; i++) {
			float from = tilePieces[i].getX();
			float to = from + deltaX;
			float delay = rowDelay * rowIter + columnDelay * columnIter;
			FloatTweener transition = transitions.get(i);
			transition.resetTween(from, to, duration, delay);
			transition.setTag(tag + i);
			transition.setInterpolation(interpolation);
			
			if (++columnIter == numCols) {
				columnIter = 0;
				rowIter++;
			}
		}
	}
	
	public void transitionIn(float duration, float columnDelay, float rowDelay) {
		if (isTransitioning() || puzzle == null)
			return;
		
		setTransitioning(true);
		setVisible(true);
		
		int numRows = puzzle.getNumRows(), numCols = puzzle.getNumColumns();
		float deltaX = scene.getStage().getWidth();
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				int tileIndex = i * numCols + j;
				TilePiece tilePiece = tilePieces[tileIndex];
				Vector2 tileDims = tilePiece.getTileDimensions();
				tilePiece.setPosition(
						j * tileDims.x - deltaX,
						(numRows - (i + 1)) * tileDims.y);
				tilePiece.setAesState(AestheticState.EDGE);
				// Re-order Z-values from front to back
				tilePiece.remove();
				canvas[kLowerCanvas].addActor(tilePiece);
			}
		}
		
		transition(kTransitionInTag, duration, columnDelay, rowDelay, Transitions.easeOut);

		lastPlayerTransitionDelay = duration + numCols * columnDelay + numRows * rowDelay - 0.25f;
		Iterator<PlayerPiece> playerIter = getPlayerValueIterator();
		while (playerIter.hasNext())
			playerIter.next().transitionIn(0.25f, lastPlayerTransitionDelay);
		
		transitionIqTag(true, 1f, lastPlayerTransitionDelay);
	}
	
	public void transitionOut(float duration, float columnDelay, float rowDelay) {
		if (isTransitioning() || puzzle == null)
			return;
		
		setTransitioning(true);
		
		int numRows = puzzle.getNumRows(), numCols = puzzle.getNumColumns();
		for (int i = 0; i < numRows; i++) {
			for (int j = numCols-1; j >= 0; j--) {
				int tileIndex = i * numCols + j;
				TilePiece tilePiece = tilePieces[tileIndex];
				Vector2 tileDims = tilePiece.getTileDimensions();
				tilePiece.setPosition(
						j * tileDims.x,
						(numRows - (i + 1)) * tileDims.y);
				// Re-order Z-values from back to front
				tilePiece.setAesState(AestheticState.EDGE);
				tilePiece.remove();
				canvas[kLowerCanvas].addActor(tilePiece);
			}
		}
		
		transition(kTransitionOutTag, duration, columnDelay, rowDelay, Transitions.easeIn);

		lastPlayerTransitionDelay = duration + numCols * columnDelay + numRows * rowDelay - 0.25f;
		Iterator<PlayerPiece> playerIter = getPlayerValueIterator();
		while (playerIter.hasNext())
			playerIter.next().transitionOut(0.25f);
		
		transitionIqTag(false, 0.5f, 0f);
	}
	
	private void setCanvasScale(float value) {
		if (canvas != null && canvas[kLowerPlayerCanvas] != null && canvas[kUpperPlayerCanvas] != null) {
			setScale(value);
			setScaleX(value * kTileScaleX);
			canvas[kLowerPlayerCanvas].setScaleX(1f / kTileScaleX);
			canvas[kUpperPlayerCanvas].setScaleX(1f / kTileScaleX);
		}
    }
	
	private float calculateCanvasScaler() {
		if (PuzzleMode.is10x8())
			return 0.975f; // At 1f the IQ Tag can be slightly offscreen
		else
			return 1.3f;
	}
	
	public void enableMenuMode(boolean enable) {
		enableMenuMode(enable, true);
	}
	
	public void enableMenuMode(boolean enable, boolean useMenuScale) {
		if (tilePieces != null) {
			for (int i = 0, n = tilePieces.length; i < n; i++) {
				if (tilePieces[i] != null)
					tilePieces[i].enableMenuMode(enable);
			}
		}
		
		Vector2 scaledTileDims = getScaledTileDimensions();
		
		if (playerPieces != null) {
			Iterator<PlayerPiece> playerIter = getPlayerValueIterator();
			while (playerIter.hasNext()) {
				AnimPlayerPiece playerPiece = (AnimPlayerPiece)playerIter.next();
				if (playerPiece != null) {
					playerPiece.enableMenuMode(enable);
					if (enable)
						playerPiece.setPosition(
								playerPiece.getX() + scaledTileDims.x / 2,
								playerPiece.getY() + scaledTileDims.y / 2);
				}
			}
		}
		
		if (iqProp != null) {
			iqProp.setScaleX(enable && useMenuScale ? 2.3f : 1f);
			iqProp.setScaleY(enable && useMenuScale ? 3f : 1.33f);

			Vector2 boardDims = getBoardDimensions();
			Vector2 tileDims = getTileDimensions();
			if (enable)
				iqProp.setPosition(
						tileDims.x / 2 + boardDims.x - (tileDims.x / 2 + 4),
						tileDims.y / 2 + boardDims.y - tileDims.y / 2);
			else
				iqProp.setPosition(
						boardDims.x - (tileDims.x / 2 + 4),
						boardDims.y - tileDims.y / 2);
		}
		
		if (puzzle != null && tilePieces != null) {
			int numColumns = puzzle.getNumColumns(), numRows = puzzle.getNumRows();
			for (int i = 0; i < numRows; i++) {
	            for (int j = 0; j < numColumns; j++) {
	                int tileIndex = i * numColumns + j;
	                TilePiece tilePiece = tilePieces[tileIndex];
	                
	                if (tilePiece != null) {
	                	Vector2 tileDims = tilePiece.getTileDimensions();
		                if (enable && useMenuScale)
		                	tilePiece.setPosition(
		                			tileDims.x / 2 + j * tileDims.x,
		                			tileDims.y / 2 + (numRows - (i+1)) * tileDims.y);
		                else
		                	tilePiece.setPosition(
		                			j * tileDims.x,
		                			(numRows - (i+1)) * tileDims.y);
	                }
	            }
	        }
		}
		
		setCanvasScale(enable && useMenuScale ? 1.0f : calculateCanvasScaler());
		isMenuModeEnabled = enable;
	}
	
	@Override
	public float decoratorValueForKey(int key) {
		float value = 0f;
		
		switch (key) {
            case TilePiece.kTDKTeleport:
                value = Promo.isTeleportAnimating() ? teleportTweener.getTweenedValue() : 1f;
                break;
            case TilePiece.kTDKPainter:
            	//value = colorArrowReel != null ? colorArrowReel.getCurrentFrame() + 0.1f : 0;
            	value = colorArrowReel != null ? colorArrowReel.getCurrentTime() : 0;
            	break;
            case TilePiece.kTDKKey:
                value = Promo.isKeyAnimating() ? keyTweener.getTweenedValue() : 1f;
                break;
        }
		
		 return value;
	}
	
	private void setPuzzle(Puzzle value) {
		if (puzzle == value)
			return;
		
		if (puzzle != null) {
			puzzle.removeEventListener(Puzzle.EV_TYPE_PLAYER_ADDED, this);
			puzzle.removeEventListener(Puzzle.EV_TYPE_PLAYER_REMOVED, this);
			puzzle.deregisterView(this);
			if (ownsPuzzle) Puzzle.freePuzzle(puzzle);
		}
		
		puzzle = value;
		if (puzzle != null) {
			puzzle.addEventListener(Puzzle.EV_TYPE_PLAYER_ADDED, this);
			puzzle.addEventListener(Puzzle.EV_TYPE_PLAYER_REMOVED, this);
			puzzle.registerView(this);
		}
	}
	
	public void setData(Puzzle puzzle, boolean ownsPuzzle) {
		if (shieldManager != null)
			shieldManager.withdrawAll(false);
		
		cancelActiveAnimations();
		
		if (tilePieces != null) {
			for (int i = 0, n = tilePieces.length; i < n; i++) {
				if (tilePieces[i] != null)
					tilePieces[i].setData(null);
			}
		}
		
		setPuzzle(puzzle);
		this.ownsPuzzle = ownsPuzzle;

		if (puzzle != null) {
			if (isMenuModeEnabled) {
				isLocked = true; // Safe in menu mode
				clearPlayerPieces(0);
				isLocked = false;
				clearRemoveQueue();
			} else {
				clearPlayerPieces(0.25f);
			}
			setup(puzzle);
				
			// Hack
			if (isTransitioning()) {
				Iterator<PlayerPiece> playerIter = getPlayerValueIterator();
				while (playerIter.hasNext()) {
					PlayerPiece playerPiece = playerIter.next();
					if (playerPiece != null && playerPiece.getPlayer() != null &&
							!removeQueue.contains(playerPiece.getPlayer(), true))
						playerPiece.transitionIn(0.25f, lastPlayerTransitionDelay);
				}
			}
		} else {
			Iterator<PlayerPiece> playerIter = getPlayerValueIterator();
			
			while (playerIter.hasNext())
				playerIter.next().setData(null);
		}
		
		enableMenuMode(isMenuModeEnabled);
	}
	
	public void syncWithData() {
		if (tilePieces != null) {
			for (int i = 0, n = tilePieces.length; i < n; i++) {
				if (tilePieces[i] != null)
					tilePieces[i].syncWithData();
			}
		}
	}
	
	public void setHighlightColor(Color color) {
		if (highlighter != null)
			highlighter.setColor(color);
	}
	
	public void enableHighlight(boolean enable) {
		if (highlighter != null)
			highlighter.setVisible(enable);
	}
	
	public TilePiece getTilePieceAtIndex(int index) {
		if (tilePieces == null || index < 0 || index >= tilePieces.length)
            return null;
        else
            return tilePieces[index];
	}
	
	public void addPlayerPiece(Player player) {
        if (puzzle != null && player != null && playerPieces != null && !playerPieces.containsKey(player))
        {
            if (isLocked)
                addQueue.add(player);
            else {
                Vector2 tileDims = getTileDimensions();
                player.setMoveDimensions(tileDims.x * kTileScaleX, tileDims.y);
                player.setViewOffset(0, puzzle.getNumRows()-1);
                playerPieces.put(player, PlayerFactory.getPlayerPiece(player));
                canvas[kLowerPlayerCanvas].addActor(playerPieces.get(player));
                refreshPlayerZValues();
            }
        }
    }
	
	public void removePlayerPiece(Player player, float transitionOutDuration) {
		if (player != null && playerPieces.containsKey(player)) {
            if (isLocked)
                removeQueue.add(player);
            else {
                PlayerPiece playerPiece = playerPieces.get(player);
                playerPieces.remove(player);
                playerPiece.addEventListener(PlayerPiece.EV_TYPE_DID_TRANSITION_OUT, this);
                playerPiece.transitionOut(transitionOutDuration);
                playerPiece.setData(null);
            }
        }
	}
	
	private void clearPlayerPieces(float transitionDuration) {
		Keys<Player> playerKeys = playerPieces.keys();
		while (playerKeys.hasNext())
			removePlayerPiece(playerKeys.next(), transitionDuration);
	}
	
	private void refreshPlayerZValues() {
		if (puzzle == null || playerPieces == null || playerPieces.size == 0)
	        return;
		
		Coord tempCoord = Coord.obtainCoord();

        // Order players relative to shields.
		{
			Iterator<Entry<Player, PlayerPiece>> playerIter = getPlayerEntryIterator();
	        while (playerIter != null && playerIter.hasNext()) {
	        	Entry<Player, PlayerPiece> playerEntry = playerIter.next();
	        	Player player = playerEntry.key;
		        PlayerPiece playerPiece = playerEntry.value;
		        if (playerPiece != null && player != null) {
			        playerPiece.remove();
	
			        Coord currentPos = player.getPosition();
			        tempCoord.set(currentPos.x, currentPos.y-2);
			        int currentTileIndex = puzzle.pos2Index(currentPos);
			        int currentShieldIndex = puzzle.pos2Index(tempCoord);
	
			        Tile tile = puzzle.tileAtIndex(currentTileIndex);
			        if (tile == null || tile.isModified(Tile.kTMShielded)) {
				        canvas[kLowerPlayerCanvas].addActor(playerPiece);
				        continue;
			        }
	
			        Coord queuedMove = player.getQueuedMove();
			        boolean isPlayerMoving = queuedMove.x != 0 || queuedMove.y != 0;
	
			        if (!isPlayerMoving) {
				        tile = puzzle.tileAtIndex(currentShieldIndex);
				        if (tile != null && tile.isModified(Tile.kTMShielded)) {
					        canvas[kUpperPlayerCanvas].addActor(playerPiece);
					        continue;
				        }
			        } else {
				        Coord nextPos = player.isMovingTo();
				        tempCoord.set(nextPos.x, nextPos.y-2);
				        int nextTileIndex = puzzle.pos2Index(nextPos);
				        int nextShieldIndex = puzzle.pos2Index(tempCoord);
	
				        Tile currentShieldTile = puzzle.tileAtIndex(currentShieldIndex);
				        Tile nextTile = puzzle.tileAtIndex(nextTileIndex);
				        Tile nextShieldTile = puzzle.tileAtIndex(nextShieldIndex);
	
				        if ( (nextTile == null || !nextTile.isModified(Tile.kTMShielded))
					        &&	(	   (currentShieldTile != null && currentShieldTile.isModified(Tile.kTMShielded)) 
							        || (nextShieldTile != null && nextShieldTile.isModified(Tile.kTMShielded))
						        )
				           )
				        {
					        canvas[kUpperPlayerCanvas].addActor(playerPiece);
					        continue;
				        }
			        }
			
			        canvas[kLowerPlayerCanvas].addActor(playerPiece);	
		        }
	        }
		}

        // Order players relative to each other. There can only be two players max,
		// so the search is simplified.
		do {
			if (playerPieces.size != 2)
				break;
			
			Iterator<Entry<Player, PlayerPiece>> playerIter = getPlayerEntryIterator();
			if (playerIter == null)
				break;
			
			// Note: We have to do this hack because the map recycles Entries
			// and thus corrupts the comparison. The iterator is meant for iterating
			// and processing single Entries in series only.
			PlayerPiece paValue = null, pbValue = null;
			Player paKey = null, pbKey = null;
			{
				Entry<Player, PlayerPiece> pa = playerIter.next();
				paValue = pa.value;
				paKey = pa.key;
				
				Entry<Player, PlayerPiece> pb = playerIter.next();
				pbValue = pb.value;
				pbKey = pb.key;
			}
        	
        	if (paValue.getParent() != null && paValue.getParent() == pbValue.getParent()) {
        		Group parentAB = paValue.getParent();
        		SnapshotArray<Actor> childrenAB = parentAB.getChildren();
        		
        		if (paKey.getPosition().y > pbKey.getPosition().y) {
        			if (childrenAB.indexOf(paValue, true) < childrenAB.indexOf(pbValue, true)) {
        				paValue.remove();
                    	parentAB.addActor(paValue);
                    }
        		} else if (paKey.getPosition().y == pbKey.getPosition().y) {
                    if (paKey.getType() == PlayerType.MIRRORED && pbKey.getType() == PlayerType.HUMAN &&
                    		childrenAB.indexOf(paValue, true) < childrenAB.indexOf(pbValue, true))
                    {
                    	paValue.remove();
                    	parentAB.addActor(paValue);
                    }
        		}
        	}
		} while (false);
        
        Coord.freeCoord(tempCoord);
	}
	
	private int getEdgeStatus(int tileIndex) {
		int edgeStatus = PuzzleEffects.kEdgeStatusNone;
		if (puzzle != null) {
			int numCols = puzzle.getNumColumns(), numRows = puzzle.getNumRows();
			if (numCols > 0 && numRows > 0) {
				if (tileIndex % numCols == 0) // Left edge
					edgeStatus |= PuzzleEffects.kEdgeStatusLeft;
				else if (tileIndex % numCols == numCols-1) // Right edge
					edgeStatus |= PuzzleEffects.kEdgeStatusRight;
				
				if (tileIndex < numCols) // Top edge
					edgeStatus |= PuzzleEffects.kEdgeStatusTop;
				else if (tileIndex >= (numRows - 1) * numCols) // Bottom edge
					edgeStatus |= PuzzleEffects.kEdgeStatusBottom;
			}
		}
		
		return edgeStatus;
	}
	
	@Override
	public void puzzleSoundShouldPlay(String soundName) {
		scene.playSound(soundName);
	}

	@Override
	public void puzzlePlayerWillMove(Player player) {
		refreshPlayerZValues();
	}

	@Override
	public void puzzlePlayerDidMove(Player player) {
		refreshPlayerZValues();
	}

	@Override
	public void puzzleShieldDidDeploy(int tileIndex) {
		if (shieldManager == null || tilePieces == null || tileIndex < 0 || tileIndex >= tilePieces.length)
			return;
		
		TilePiece tilePiece = tilePieces[tileIndex];

		Shield shield = shieldManager.addShield(tileIndex, tileIndex, tilePiece.getX(), tilePiece.getY());
		if (shield != null) {
			shield.deploy();
			refreshPlayerZValues();
		}
	}

	@Override
	public void puzzleShieldWasWithdrawn(int tileIndex) {
		if (shieldManager == null)
			return;
		
		Shield shield = shieldManager.shieldForKey(tileIndex);
		if (shield != null) {
			shield.withdraw();
			refreshPlayerZValues();
		}
	}
	
	
	private static final int[] kTileRotatorEdgeIndexes = new int[] { 1, 7, 8 };
	private static final int[] kTileRotatorIndexes = new int[] { 0, 3, 5, 6, 7, 4, 2, 1 };
	private TilePiece[] rotatorCache = new TilePiece[8];

	@Override
	public void puzzleTilesShouldRotate(int[] tileIndexes) {
		if (puzzle == null || tileIndexes == null || tileIndexes.length <= rotatorCache.length)
			return;
		
		for (int i = 0, n = rotatorCache.length; i < n; i++)
			rotatorCache[kTileRotatorIndexes[i]] = tilePieces[tileIndexes[i+1]];
		
		// Present relevant tiles in the row above as edge tiles while animation is active
		for (int i = 0; i < 3; i++) {
			int edgeIndex = tileIndexes[kTileRotatorEdgeIndexes[i]] - puzzle.getNumColumns();
			if (puzzle.isValidIndex(edgeIndex))
				tilePieces[edgeIndex].setAesState(AestheticState.EDGE);
		}
		
		TilePiece centerPiece = tilePieces[tileIndexes[0]];
		float posX = centerPiece.getX(), posY = centerPiece.getY();
		Rectangle bounds = centerPiece.getTileBounds();
		bounds.set(
				posX - bounds.width / 2,
				posY - bounds.height / 2,
				bounds.width,
				bounds.height);
		Rectangle boardBounds = getBoardBounds();
		
		// Determine the rotator's edge status
		int edgeStatus = PuzzleEffects.kEdgeStatusNone;
		{
			int tileIndex = tileIndexes[1]; // Skip center tile
			edgeStatus |= getEdgeStatus(tileIndex);
			edgeStatus |= getEdgeStatus(tileIndex + 2);
			edgeStatus |= getEdgeStatus(tileIndex + 2 * puzzle.getNumColumns());
		}
		
		TileRotator rotator = EffectFactory.getTileRotator(posX, posY, boardBounds, rotatorCache);
		rotator.setListener(this);
		rotator.setEdgeStatus(edgeStatus);
		assert(!animatingRotators.contains(rotator, true)) : "PuzzleBoard: duplicate TileRotator found.";
		animatingRotators.add(rotator);
		
		scene.addToJuggler(rotator);
		rotator.animate(Promo.isPromoEnabled() ? Promo.getRotatorDuration() : 0.9f);
		
		int zOrder = canvas[kUpperCanvas].getChildren().size;
		if (puzzle.isConveyorBeltActive() && puzzle.getConveyorBeltDir().x != 0) {
			int[] beltIndexes = puzzle.getConveyorBeltIndexes();
			if (beltIndexes != null && beltIndexes.length > 0) {
				if (puzzle.rowForIndex(beltIndexes[0]) > puzzle.rowForIndex(tileIndexes[0]))
					zOrder = 0;
			}
		}
		
		canvas[kUpperCanvas].addActorAt(zOrder, rotator);
		
		// Add players that are positioned on the rotated tiles
		Array<Player> players = puzzle.getPlayers();
		if (players != null) {
			for (int i = 0, n = players.size; i < n; i++) {
				Player player = players.get(i);
				if (player != null) {
					int playerIndex = puzzle.pos2Index(player.getPosition());
					
					// Pivot piece
					if (playerIndex == tileIndexes[0]) {
						PlayerPiece playerPiece = addPlayerToEffect(player);
						if (playerPiece != null) {
							rotator.addPivot(tilePieces[tileIndexes[0]], playerPiece);
							tilePieces[tileIndexes[0]].setVisible(false);
						}
					}
					
					// Non-pivot pieces
					for (int j = 0; j < 8; j++) {
						if (playerIndex == tileIndexes[j+1]) {
							PlayerPiece playerPiece = addPlayerToEffect(player);
							if (playerPiece != null) // Rotator takes ownership of playerPiece
								rotator.addPlayerPiece(kTileRotatorIndexes[j], playerPiece);
						}
					}
				}
			}
		}
		
		
		for (int i = 0, n = rotatorCache.length; i < n; i++) {
			rotatorCache[i].setVisible(false);
			rotatorCache[i] = null;
		}
	}
	
	private Vector2 tileSwapOriginACache = new Vector2();
	private Vector2 tileSwapOriginBCache = new Vector2();
	private Array<TilePiece> tileSwapCache = new Array<TilePiece>(true, 18, TilePiece.class);
	@Override
	public void puzzleTileSwapWillBegin(int[][] swapIndexes, boolean isCenterValid) {
		tileSwapCache.clear();
		
		for (int i = 0, n = swapIndexes.length; i < n; i++) {
			for (int j = 0, jn = swapIndexes[i].length; j < jn; j++) {
				if (swapIndexes[i][j] == -1)
					tileSwapCache.add(null);
				else
					tileSwapCache.add(tilePieces[swapIndexes[i][j]]);
			}
		}
		
		for (int i = 0; i < 2; i++) {
			TilePiece centerPiece = tilePieces[swapIndexes[i][4]];
			if (i == 0)
				tileSwapOriginACache.set(centerPiece.getX(), centerPiece.getY());
			else
				tileSwapOriginBCache.set(centerPiece.getX(), centerPiece.getY());
			
			if (!isCenterValid)
				tileSwapCache.set(i * swapIndexes[0].length + 4, null);
		}
		
		TileSwapper tileSwapper = EffectFactory.getTileSwapper(tileSwapOriginACache, tileSwapOriginBCache, tileSwapCache);
		tileSwapper.setListener(this);
		assert(!animatingTileSwappers.contains(tileSwapper, true)) : "PuzzleBoard: duplicate TileSwapper found.";
		animatingTileSwappers.add(tileSwapper);
		canvas[kLowerCanvas].addActor(tileSwapper);
		scene.addToJuggler(tileSwapper);
		scene.playSound("tile-swap");
	}
	
	
	private Array<TilePiece> conveyorBeltCache = new Array<TilePiece>(true, 10, TilePiece.class);
	@Override
	public void puzzleConveyorBeltWillMove(Coord moveDir, int wrapIndex, int[] tileIndexes) {
		if (puzzle == null)
			return;
		assert(tileIndexes != null && wrapIndex >= 0 && wrapIndex < tileIndexes.length) : "Invalid args to PuzzleBoard::puzzleConveyorBeltWillMove";
		
		int numTiles = tileIndexes.length;
		for (int i = 0; i < numTiles; i++) {
			conveyorBeltCache.add(tilePieces[tileIndexes[i]]);
			
			// Mark end tiles as edge
			if (moveDir.x != 0 && (i == 0 || i == numTiles - 1)) {
				int endIndex = tileIndexes[i] - puzzle.getNumColumns();
				if (puzzle.isValidIndex(endIndex))
					tilePieces[endIndex].setAesState(AestheticState.EDGE);
			}
		}
		
		Rectangle boardBounds = getBoardBounds();
		TileConveyorBelt conveyorBelt = EffectFactory.getTileConveyorBelt(boardBounds, moveDir, wrapIndex, conveyorBeltCache);
		conveyorBelt.setListener(this);
		conveyorBelt.setDecorator(this);
		conveyorBelt.setEdgeStatus(getEdgeStatus(tileIndexes[0]));
		assert(!animatingConveyorBelts.contains(conveyorBelt, true)) : "PuzzleBoard: duplicate TileConveyorBelt found.";
		animatingConveyorBelts.add(conveyorBelt);
		scene.addToJuggler(conveyorBelt);
		
		float animDuration = Promo.isPromoEnabled() ? Promo.getConveyorBeltDuration() : 1.15f;
		if (moveDir.x != 0)
			conveyorBelt.animate((puzzle.getNumColumns() / 10f) * animDuration);
		else
			conveyorBelt.animate((puzzle.getNumRows() / 8f) * 0.9f * animDuration);
		
		int zOrder = canvas[kUpperCanvas].getChildren().size;
		if (puzzle.isRotating() && moveDir.x != 0) {
			int[][] rotationIndexes = puzzle.getRotationIndexes();
			if (puzzle.rowForIndex(rotationIndexes[0][0]) > puzzle.rowForIndex(tileIndexes[0]))
				zOrder = 0;
		}
		
		canvas[kUpperCanvas].addActorAt(zOrder, conveyorBelt);
		
		// Add players that are positioned on the conveyor belt tiles
		Array<Player> players = puzzle.getPlayers();
		if (players != null) {
			for (int i = 0, n = players.size; i < n; i++) {
				Player player = players.get(i);
				if (player != null) {
					int playerIndex = puzzle.pos2Index(player.getPosition());
					for (int j = 0; j < numTiles; j++) {
						if (playerIndex == tileIndexes[j]) {
							PlayerPiece playerPiece = addPlayerToEffect(player);
							if (playerPiece != null)
								conveyorBelt.addPlayerPiece(j, playerPiece);
						}
					}
				}
			}
		}
		
		conveyorBeltCache.clear();
	}

	private Vector2 wasSolvedVectorCache = new Vector2();
	@Override
	public void puzzleWasSolved(int tileIndex) {
		if (tileIndex >= 0 && tileIndex < tilePieces.length) {
			PlayfieldController scene = (PlayfieldController)this.scene;
			if (scene != null) {
				TilePiece tilePiece = tilePieces[tileIndex];
				wasSolvedVectorCache.set(0, 0);
				wasSolvedVectorCache = tilePiece.localToStageCoordinates(wasSolvedVectorCache);
				PuzzleController puzzleController = scene.getPuzzleController();
				if (puzzleController != null)
					puzzleController.displaySolvedAnimation(
							wasSolvedVectorCache.x,
							wasSolvedVectorCache.y,
							scene.VW2(),
							scene.VH2());
			}
		}
	}
	
	private PlayerPiece addPlayerToEffect(Player player) {
		if (player == null)
			return null;
		
		PlayerPiece playerPiece = PlayerFactory.getPlayerPiece(player);
		playerPiece.setData(null);
		playerPiece.setPositionAestheticOnly(0, 0);
		playerPiece.setScaleX(1f / kTileScaleX);
		
		PlayerPiece realPlayerPiece = playerPieces.get(player);
		if (realPlayerPiece != null) {
			playerPiece.syncWithPlayerPiece(realPlayerPiece);
			realPlayerPiece.setVisible(false);
		}
		
		if (player.getType() == PlayerType.MIRRORED) {
			MirroredPlayer mirroredPlayer = (MirroredPlayer)player;
			if (mirroredPlayer.getNumMovesRemaining() == 0) {
				// Don't hook the event up. The PlayerPiece will instead be freed in tileConveyorBeltCompleted
				//playerPiece.addEventListener(PlayerPiece.EV_TYPE_DID_TRANSITION_OUT, this);
				playerPiece.transitionOut(1f);
			}
		}
		
		return playerPiece;
	}
	
	@Override
	public void localeDidChange(String fontKey, String FXFontKey) {
		updateIQText(puzzle);
	}

	@Override
	public void onEvent(int evType, Object evData) {
		if (evType == FloatTweener.EV_TYPE_FLOAT_TWEENER_CHANGED) {
			FloatTweener tweener = (FloatTweener)evData;
			if (tweener != null) {
				int tag = tweener.getTag();
				float tweenedValue = tweener.getTweenedValue();
				
				switch (tag) {
					case kTweenerIqTag:
						if (iqProp != null)
							iqProp.setColor(Utils.setA(iqProp.getColor(), tweenedValue));
						break;
					case kTweenerTeleportTag:
					case kTweenerKeyTag:
						break;
					default:
					{
						int tileIndex = tag - (tag >= kTransitionOutTag ? kTransitionOutTag : kTransitionInTag);
						assert(tileIndex < tilePieces.length) : "PuzzleBoard - bad transition index.";
						tilePieces[tileIndex].setX(tweenedValue);
					}
						break;
				}
			}
		} else if (evType == FloatTweener.EV_TYPE_FLOAT_TWEENER_COMPLETED) {
			FloatTweener tweener = (FloatTweener)evData;
			if (tweener != null) {
				int tag = tweener.getTag();
				
				switch (tag) {
					case kTweenerTeleportTag:
						tweener.reverse();
						break;
					case kTweenerKeyTag:
						tweener.resetTween(0, 6, 6, 0);
						break;
					case kTweenerIqTag:
						break;
					default:
					{
						int tileIndex = tag - (tag >= kTransitionOutTag ? kTransitionOutTag : kTransitionInTag);
						if (tileIndex == tilePieces.length-1) {
							setTransitioning(false);
							
							for (int i = 0, n = tilePieces.length; i < n; i++)
								tilePieces[i].setAesState(AestheticState.NORMAL);
							
							if (tag >= kTransitionOutTag) {
								setVisible(false);
								dispatchEvent(EV_TYPE_DID_TRANSITION_OUT, this);
							} else
								dispatchEvent(EV_TYPE_DID_TRANSITION_IN, this);
						}
					}
						break;
				}
			}
		} else if (evType == TouchPad.EV_TYPE_TOUCH_DOWN || evType == TouchPad.EV_TYPE_TOUCH_UP) {
			float x = touchPad.x, y = touchPad.y;
			int tileIndex = 10 * (int)((8 * kTileHeight - y) / kTileHeight) + (int)(x / kTileWidth);
			tilePieces[tileIndex].setVisible(evType == TouchPad.EV_TYPE_TOUCH_UP);
		} else if (evType == PlayerPiece.EV_TYPE_DID_TRANSITION_OUT) {
			AnimPlayerPiece playerPiece = (AnimPlayerPiece)evData;
			if (playerPiece != null) {
				playerPiece.removeEventListener(PlayerPiece.EV_TYPE_DID_TRANSITION_OUT, this);
				PlayerFactory.freePlayerPiece(playerPiece);
			}
		} else if (evType == Puzzle.EV_TYPE_PLAYER_ADDED) {
			addPlayerPiece((Player)evData);
		} else if (evType == Puzzle.EV_TYPE_PLAYER_REMOVED) {
			removePlayerPiece((Player)evData, puzzle != null && puzzle.isResetting() ? 0.25f : 1.0f);
		} else if (evType == TileConveyorBelt.EV_TYPE_ANIMATION_COMPLETED) {
			tileConveyorBeltCompleted((TileConveyorBelt)evData);
		} else if (evType == TileRotator.EV_TYPE_ANIMATION_COMPLETED) {
			tileRotatorCompleted((TileRotator)evData);
		} else if (evType == TileSwapper.EV_TYPE_ANIMATION_COMPLETED) {
			tileSwapperCompleted((TileSwapper)evData);
		}
	}
	
	private void tileConveyorBeltCompleted(TileConveyorBelt conveyorBelt) {
		assert(conveyorBelt != null && animatingConveyorBelts.contains(conveyorBelt, true)) : "PuzzleBoard: invalid TileConveyorBelt completed.";
		if (conveyorBelt == null || !animatingConveyorBelts.contains(conveyorBelt, true))
			return;
		
		animatingConveyorBelts.removeValue(conveyorBelt, true);
		EffectFactory.freeTileConveyorBelt(conveyorBelt);
		
		if (puzzle != null && playerPieces != null) {
			puzzle.applyConveyorBelt();
			Iterator<Entry<Player, PlayerPiece>> playerIter = getPlayerEntryIterator();
			while (playerIter.hasNext()) {
				Entry<Player, PlayerPiece> playerEntry = playerIter.next();
				if (!puzzle.isPlayerOccupied(playerEntry.key))
					playerEntry.value.setVisible(true);
			}
			
			int[] tileIndexes = puzzle.getConveyorBeltIndexes();
			int numRows = puzzle.getNumRows(), numIndexes = tileIndexes.length;
			Coord conveyorBeltDir = puzzle.getConveyorBeltDir();
			for (int i = 0; i < numIndexes; i++) {
				int tileIndex = tileIndexes[i];
				TilePiece tilePiece = tilePieces[tileIndex];
				Vector2 tileDims = tilePiece.getTileDimensions();
				tilePiece.setPosition(
						puzzle.columnForIndex(tileIndex) * tileDims.x,
						(numRows - (puzzle.rowForIndex(tileIndex) + 1)) * tileDims.y);
				tilePiece.setAesState(AestheticState.NORMAL);
				tilePiece.setVisible(true);
				
				// Unmark end tiles as edge
				if (conveyorBeltDir.x != 0 && (i == 0 || i == numIndexes - 1)) {
					int endIndex = tileIndexes[i] - puzzle.getNumColumns();
					if (puzzle.isValidIndex(endIndex))
						tilePieces[endIndex].setAesState(AestheticState.NORMAL);
				}
			}
		}
		
		refreshPlayerZValues();
	}
	
	private void tileRotatorCompleted(TileRotator rotator) {
		assert(rotator != null && animatingRotators.contains(rotator, true)) : "PuzzleBoard: invalid TileRotator completed.";
		if (rotator == null || !animatingRotators.contains(rotator, true))
			return;
		
		animatingRotators.removeValue(rotator, true);
		EffectFactory.freeTileRotator(rotator);
		
		if (puzzle != null && playerPieces != null) {
			puzzle.applyRotation();
			
			Iterator<Entry<Player, PlayerPiece>> playerEntries = getPlayerEntryIterator();
			while (playerEntries.hasNext()) {
				Entry<Player, PlayerPiece> playerEntry = playerEntries.next();
				if (playerEntry.value != null && playerEntry.key != null && !puzzle.isPlayerOccupied(playerEntry.key))
					playerEntry.value.setVisible(true);
			}
			
			int[][] tileIndexes = puzzle.getRotationIndexes();
			for (int i = 0; i < 8; i++)
				tilePieces[tileIndexes[0][i+1]].setVisible(true);
			
			// Revert presentation of relevant tiles in the row above which were marked as edhe tiles during the animation
			for (int i = 0; i < 3; i++) {
				int edgeIndex = tileIndexes[0][kTileRotatorEdgeIndexes[i]] - puzzle.getNumColumns();
				if (puzzle.isValidIndex(edgeIndex))
					tilePieces[edgeIndex].setAesState(AestheticState.NORMAL);
			}
			
			tilePieces[tileIndexes[0][0]].setVisible(true);
		}
		
		refreshPlayerZValues();
	}
	
	private void tileSwapperCompleted(TileSwapper swapper) {
		assert(swapper != null && animatingTileSwappers.contains(swapper, true)) : "PuzzleBoard: invalid TileSwapper completed.";
		if (swapper == null || !animatingTileSwappers.contains(swapper, true))
			return;
		
		animatingTileSwappers.removeValue(swapper, true);
		EffectFactory.freeTileSwapper(swapper);
		tileSwapCache.clear();
		
		if (puzzle != null)
			puzzle.applyTileSwap();
	}
	
	private void cancelActiveAnimations() {
		for (int i = animatingConveyorBelts.size-1; i >= 0; i--) {
			TileConveyorBelt conveyorBelt = animatingConveyorBelts.get(i);
			if (conveyorBelt != null && !conveyorBelt.isComplete()) {
				conveyorBelt.stopAnimating();
				tileConveyorBeltCompleted(conveyorBelt);
			}
		}
		animatingConveyorBelts.clear();
		
		for (int i = animatingRotators.size-1; i >= 0; i--) {
			TileRotator rotator = animatingRotators.get(i);
			if (rotator != null && !rotator.isComplete()) {
				rotator.stopAnimating();
				tileRotatorCompleted(rotator);
			}
		}
		animatingRotators.clear();
		
		for (int i = animatingTileSwappers.size-1; i >= 0; i--) {
			TileSwapper swapper = animatingTileSwappers.get(i);
			if (swapper != null && !swapper.isComplete()) {
				swapper.stopAnimating();
				tileSwapperCompleted(swapper);
			}
		}
		animatingTileSwappers.clear();
	}
	
	private void clearRemoveQueue() {
		for (int i = 0; i < removeQueue.size; i++) {
			Player player = removeQueue.get(i);
			AnimPlayerPiece playerPiece = (AnimPlayerPiece)playerPieces.get(player);
            playerPieces.remove(player);
			PlayerFactory.freePlayerPiece(playerPiece);
		}
		removeQueue.clear();
	}
	
	public void softReset() {
		cancelActiveAnimations();
		
		if (puzzle != null && !isTransitioning()) {
			shieldManager.withdrawAll(false);
			puzzle.softReset();
			
			Iterator<PlayerPiece> playerIter = getPlayerValueIterator();
			while (playerIter.hasNext())
				playerIter.next().softReset();
		}
		
		addQueue.clear();
		clearRemoveQueue();
	}
	
	@Override
	public void reset() {
		cancelActiveAnimations();
		enableMenuMode(false);
		
		for (int i = 0, n = transitions.size; i < n; i++) {
			FloatTweener tweener = transitions.get(i);
			if (tweener != null)
				Pools.free(tweener);
		}
		transitions.clear();
		
		removeQueue.clear();
		
		Iterator<PlayerPiece> playerValuesIt = getPlayerValueIterator();
		while (playerValuesIt.hasNext()) {
			PlayerPiece playerPiece = playerValuesIt.next();
			PlayerFactory.freePlayerPiece((AnimPlayerPiece)playerPiece);
		}
		playerPieces.clear();
		addQueue.clear();
		
		if (tilePieces != null) {
			for (int i = 0, n = tilePieces.length; i < n; i++) {
				TilePiece tilePiece = tilePieces[i];
				if (tilePiece != null) {
					TileFactory.freeTilePiece(tilePiece);
					tilePieces[i] = null;
				}
			}
		}
		
		lastPlayerTransitionDelay = 0;
		setPuzzle(null);
		ownsPuzzle = false;
		setOrigin(0, 0); // Undo potential PuzzlePageEntry modifications
	}
	
	@Override
	public void advanceTime(float dt) {
		isLocked = true;
		Iterator<PlayerPiece> ppIt = getPlayerValueIterator();
		while (ppIt.hasNext()) {
			PlayerPiece playerPiece = ppIt.next();
			if (playerPiece.isAdvanceable())
				playerPiece.advanceTime(dt);
		}
		isLocked = false;
		
		for (int i = 0, n = addQueue.size; i < n; i++)
			addPlayerPiece(addQueue.get(i));
		for (int i = 0, n = removeQueue.size; i < n; i++)
			removePlayerPiece(removeQueue.get(i), kDefaultTransitionDuration);
		addQueue.clear();
		removeQueue.clear();
		
		shieldManager.advanceTime(dt);
		colorArrowReel.advanceTime(dt);
		
		if (isTransitioning()) {
			for (int i = 0, n = transitions.size; i < n; i++)
				transitions.get(i).advanceTime(dt);
		}
		
		if (iqTweener != null && !iqTweener.isComplete())
			iqTweener.advanceTime(dt);
	}
	
	public void postAdvanceTime(float dt) {
		teleportTweener.advanceTime(dt);
		keyTweener.advanceTime(dt);
	}
}
