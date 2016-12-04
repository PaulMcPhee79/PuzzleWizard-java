package com.cheekymammoth.puzzleEffects;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.graphics.CroppedProp;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.puzzleFactories.PlayerFactory;
import com.cheekymammoth.puzzleFactories.TileFactory;
import com.cheekymammoth.puzzleViews.AnimPlayerPiece;
import com.cheekymammoth.puzzleViews.PlayerPiece;
import com.cheekymammoth.puzzleViews.TilePiece;
import com.cheekymammoth.puzzleViews.TilePiece.AestheticState;
import com.cheekymammoth.utils.FloatTweener;
import com.cheekymammoth.utils.Transitions;

public class TileRotator extends Prop implements IEventListener, Poolable {
	private enum RotatorState { STATIONARY, RAISING, ROTATING, DROPPING };
	
	public static final int EV_TYPE_ANIMATION_COMPLETED;
	
	static {
		EV_TYPE_ANIMATION_COMPLETED = EventDispatcher.nextEvType();
	}
	
	private static final int kNumRotatingTiles = 8;
	private static final int kShadowLayerIndex = 0;
	private static final int kTileLayerIndex = 1;
	private static final int kNumLayers = kTileLayerIndex + 1;
	private static final int kRaiseTagBase = 1000;
	private static final int kRotateTagBase = 2000;
	private static final int kDropTagBase = 3000;
	private static final int[] kTileZOrder = new int[] { 1, 2, 0, 3, 4, 7, 5, 6 };
	private static final float kRaisedTileScale = 1.6f;
	private static final float kRaisedShadowScale = 2 * 1.8f; // x2 because image resource is half size
	private static final float kRaisedAdjacentSeparation = 8f;
	private static final float kTileWidth = 144f;
	private static final float kTileHeight = 144f;
	private static final float kRaiseDurationFactor = 0.45f;
	private static final float kRotateDurationFactor = 0.45f;
	private static final float kDropDurationFactor = 0.1f;
	
	private boolean hasAnimated;
	private RotatorState state = RotatorState.STATIONARY;
	private int edgeStatus = PuzzleEffects.kEdgeStatusNone; // OR'ed PuzzleEffects.kEdgeStatus
	private int raiseCompletedTag = -1;
	private int rotateCompletedTag = -1;
	private int dropCompletedTag = -1;
	private float duration;
	private Rectangle shadowOccRegion = new Rectangle();
	private TilePiece pivotPiece;
	private PlayerPiece pivotPlayer;
	private Array<PlayerPiece> playerPieces = new Array<PlayerPiece>(true, 2, PlayerPiece.class);
	private TilePiece[] tiles = new TilePiece[kNumRotatingTiles];
	private TileShadow[] tileShadows = new TileShadow[kNumRotatingTiles];
	private Prop[] layers = new Prop[kNumLayers];
	private FloatTweener[] raiseTweeners = new FloatTweener[6 * kNumRotatingTiles];
	private FloatTweener[] rotateTweeners = new FloatTweener[4 * kNumRotatingTiles];
	private FloatTweener[] dropTweeners = new FloatTweener[6 * kNumRotatingTiles];
	private IEventListener listener;
	
	public TileRotator() {
		this(-1);
	}

	public TileRotator(int category) {
		super(category);
	}
	
	public void configure(float posX, float posY, Rectangle boardBounds, TilePiece[] tilePieces) {
		assert(tilePieces.length == 8) : "TileRotator requires 8 non-null TilePieces.";
		
		setPosition(posX, posY);
		
		if (layers[kShadowLayerIndex] == null) {
			layers[kShadowLayerIndex] = new CroppedProp(-1, boardBounds);
			layers[kShadowLayerIndex].setTransform(true);
			addActor(layers[kShadowLayerIndex]);
		} else
			((CroppedProp)layers[kShadowLayerIndex]).setViewableRegion(boardBounds);

		if (layers[kTileLayerIndex] == null) {
			layers[kTileLayerIndex] = new Prop();
			layers[kTileLayerIndex].setTransform(true);
			addActor(layers[kTileLayerIndex]);
		}
		
		CroppedProp shadowLayer = (CroppedProp)layers[kShadowLayerIndex];
		shadowLayer.setViewableRegion(boardBounds);
		Prop tileLayer = layers[kTileLayerIndex];
		
		float originX = 0, originY = 0;
		for (int i = 0, n = tilePieces.length; i < n; i++) {
			int index = kTileZOrder[i];
			assert(tilePieces[index] != null) : "TileRotator requires 8 non-null TilePieces.";
			TilePiece tilePiece = tilePieces[index].clone();
			tilePiece.setDecorator(tilePieces[index].getDecorator());
			tilePiece.setAesState(AestheticState.EDGE);
			
			TileShadow tileShadow = tileShadows[index];
			if (tileShadow == null)
				tileShadow = new TileShadow(getCategory(), scene.textureByName("tile-shadow.png"), shadowOccRegion);
			tileShadow.setScale(kTileWidth / tileShadow.getWidth());
			
			switch (index) {
				case 0:
					originX = -kTileWidth;
					originY = kTileHeight;
					break;
				case 1:
					originX = 0;
					originY = kTileHeight;
					break;
				case 2:
					originX = kTileWidth;
					originY = kTileHeight;
					break;
				case 3:
					originX = -kTileWidth;
					originY = 0;
					break;
				case 4:
					originX = kTileWidth;
					originY = 0;
					break;
				case 5:
					originX = -kTileWidth;
					originY = -kTileHeight;
					break;
				case 6:
					originX = 0;
					originY = -kTileHeight;
					break;
				case 7:
					originX = kTileWidth;
					originY = -kTileHeight;
					break;
			}
			
			tilePiece.setPosition(originX, originY);
			tileShadow.setPosition(tilePiece.getShadowPosition().x, tilePiece.getShadowPosition().y);
			tileShadow.setY(tileShadow.getY());
			
			tiles[index] = tilePiece;
			tileShadows[index] = tileShadow;
			tileLayer.addActor(tilePiece);
			shadowLayer.addActor(tileShadow);
		}
		
		setEdgeStatus(PuzzleEffects.kEdgeStatusNone);
	}
	
	public IEventListener getListener() { return listener; }
	
	public void setListener(IEventListener value) { listener = value; }
	
	private RotatorState getState() { return state; }
	
	private void setState(RotatorState value) {
		if (state == value)
			return;
		
		switch (value) {
			case STATIONARY:
				break;
			case RAISING:
				hasAnimated = true;
				break;
			case ROTATING:
				break;
			case DROPPING:
				break;
		}
		
		state = value;
	}
	
	private Rectangle edgeOffsetCache = new Rectangle();
	public void setEdgeStatus(int value) {
		// Only proceed if it changed or it's initializing
		if (value == PuzzleEffects.kEdgeStatusNone || edgeStatus != value) {
			int edgeOverlap = 20;
			edgeOffsetCache.set(-1, -1, 1, 1);
			
			if ((value & PuzzleEffects.kEdgeStatusLeft) == PuzzleEffects.kEdgeStatusLeft) {
				edgeOffsetCache.x -= edgeOverlap;
				edgeOffsetCache.width += edgeOverlap;
			} else if ((value & PuzzleEffects.kEdgeStatusRight) == PuzzleEffects.kEdgeStatusRight)
				edgeOffsetCache.width += edgeOverlap;
			
			if ((value & PuzzleEffects.kEdgeStatusTop) == PuzzleEffects.kEdgeStatusTop)
				edgeOffsetCache.height += edgeOverlap;
			else if ((value & PuzzleEffects.kEdgeStatusBottom) == PuzzleEffects.kEdgeStatusBottom) {
				edgeOffsetCache.y -= edgeOverlap;
				edgeOffsetCache.height += edgeOverlap;
			}
			
			shadowOccRegion.set(
					edgeOffsetCache.x - (1.5f * kTileWidth + 4f),
					edgeOffsetCache.y - (1.5f * kTileHeight + 4f),
					edgeOffsetCache.width + (3f * kTileWidth + 8f),
					edgeOffsetCache.height + (3f * kTileHeight + 8f));
			
			for (int i = 0, n = tileShadows.length; i < n; i++)
				tileShadows[i].setOcclusionBounds(shadowOccRegion);
			
			edgeStatus = value;
			//Gdx.app.log("TileRotator::EdgeStatus", "" + edgeStatus);
		}
	}
	
	public void addPivot(TilePiece tilePiece, PlayerPiece playerPiece) {
		if (tilePiece == null || pivotPiece != null || pivotPlayer != null)
			return;
		
		pivotPiece = tilePiece.clone();
		pivotPiece.setPosition(0, 0);
		pivotPiece.setDecorator(tilePiece.getDecorator());
		pivotPiece.setAesState(AestheticState.EDGE);
		
		Prop tileLayer = layers[kTileLayerIndex];
		tileLayer.clearChildren();
		for(int i = 0, n = tiles.length; i < n; i++) {
			int index = kTileZOrder[i];
			if (i == 4) // Pivot index
				tileLayer.addActor(pivotPiece);
			tileLayer.addActor(tiles[index]);
		}
		
		if (playerPiece != null) {
			pivotPlayer = playerPiece;
			pivotPlayer.setPosition(0, 0);
			pivotPiece.addActor(pivotPlayer);
		}
	}
	
	public void addPlayerPiece(int index, PlayerPiece playerPiece) {
		if (playerPiece == null)
			return;
		
		if (tiles.length > index && index >= 0) {
			playerPiece.setPosition(0, 0);
			tiles[index].addActor(playerPiece);
			playerPieces.add(playerPiece);
		} else
			PlayerFactory.freePlayerPiece((AnimPlayerPiece)playerPiece);
	}
	
	public void animate(float duration) {
		if (hasAnimated)
			return;
		hasAnimated = true;
		this.duration = duration;
		setState(RotatorState.RAISING);
		raiseTiles();
	}
	
	private Vector2 raisedShadowPosCache = new Vector2();
	private void raiseTiles() {
		float dispersionScale = 0.9f;
		float distX = 0, distY = 0, duration = kRaiseDurationFactor * this.duration;
		float adjacentSep = kRaisedTileScale * kRaisedAdjacentSeparation;
		float raisedScale = kRaisedTileScale, scaleDelta = (kRaisedTileScale - 1f);
		for (int i = 0, n = tiles.length; i < n; i++) {
			TilePiece tile = tiles[i];
			TileShadow shadow = tileShadows[i];
			
			switch (i) {
				case 0:
					distX = -dispersionScale * raisedScale * kTileWidth;
					distY = scaleDelta * tile.getOffsetY() + scaleDelta * kTileHeight + adjacentSep;
					break;
				case 1:
					distX = 0;
					distY = dispersionScale * raisedScale * (kTileHeight + tile.getOffsetY());
					break;
				case 2:
					distX = scaleDelta * kTileWidth + adjacentSep;
					distY = dispersionScale * raisedScale * (kTileHeight + tile.getOffsetY());
					break;
				case 3:
					distX = -dispersionScale * raisedScale * kTileWidth;
					distY = scaleDelta * tile.getOffsetY();
					break;
				case 4:
					distX = dispersionScale * raisedScale * kTileWidth;
					distY = scaleDelta * tile.getOffsetY();
					break;
				case 5:
					distX = -(adjacentSep + scaleDelta * kTileWidth);
					distY = scaleDelta * tile.getOffsetY() - dispersionScale * raisedScale * kTileHeight;
					break;
				case 6:
					distX = 0;
					distY = scaleDelta * tile.getOffsetY() - dispersionScale * raisedScale * kTileHeight;
					break;
				case 7:
					distX = dispersionScale * raisedScale * kTileWidth;
					distY = scaleDelta * tile.getOffsetY() - (scaleDelta * kTileHeight + adjacentSep);
					break;
			}
			
			int baseIndex = 6 * i;
			if (raiseTweeners.length > baseIndex && raiseTweeners[baseIndex] == null) {
				for (int j = 0; j < 6; j++) {
					raiseTweeners[baseIndex+j] = new FloatTweener(0, Transitions.easeOut, this);
					raiseTweeners[baseIndex+j].setTag(kRaiseTagBase+baseIndex+j);
					raiseCompletedTag = raiseTweeners[baseIndex+j].getTag();
				}
			}
			
			// Tile tweeners
			raiseTweeners[baseIndex].resetTween(tile.getX(), tile.getX() + distX, duration, 0);
			raiseTweeners[baseIndex+1].resetTween(tile.getY(), tile.getY() + distY, duration, 0);
			raiseTweeners[baseIndex+2].resetTween(tile.getScaleX(), kRaisedTileScale, duration, 0);
			
			// Shadow tweeners
			Vector2 shadowPos = tile.getShadowPosition();
			raisedShadowPosCache.set(
					shadowPos.x + raisedScale * (shadowPos.x - tile.getX()),
					shadowPos.y + raisedScale * (shadowPos.y - tile.getY()));
			raiseTweeners[baseIndex+3].resetTween(shadowPos.x, raisedShadowPosCache.x + distX, duration, 0);
			raiseTweeners[baseIndex+4].resetTween(shadowPos.y, raisedShadowPosCache.y + distY, duration, 0);
			raiseTweeners[baseIndex+5].resetTween(shadow.getScaleX(), kRaisedShadowScale, duration, 0);
		}
		
		scene.playSound("rotate");
	}
	
	private void rotateTiles() {
		float distX = 0, distY = 0, duration = kRotateDurationFactor * this.duration;
		for (int i = 0, n = tiles.length; i < n; i++) {
			TilePiece tile = tiles[i];
			TileShadow shadow = tileShadows[i];
			
			switch (i) {
				case 0:
					distX = 0;
					distY = -kRaisedTileScale * kTileHeight;
					break;
				case 1:
					distX = -kRaisedTileScale * kTileWidth;
					distY = 0;
					break;
				case 2:
					distX = -kRaisedTileScale * kTileWidth;
					distY = 0;
					break;
				case 3:
					distX = 0;
					distY = -kRaisedTileScale * kTileHeight;
					break;
				case 4:
					distX = 0;
					distY = kRaisedTileScale * kTileHeight;
					break;
				case 5:
					distX = kRaisedTileScale * kTileWidth;
					distY = 0;
					break;
				case 6:
					distX = kRaisedTileScale * kTileWidth;
					distY = 0;
					break;
				case 7:
					distX = 0;
					distY = kRaisedTileScale * kTileHeight;
					break;
			}
			
			int baseIndex = 4 * i;
			if (rotateTweeners.length > baseIndex && rotateTweeners[baseIndex] == null) {
				for (int j = 0; j < 4; j++) {
					rotateTweeners[baseIndex+j] = new FloatTweener(0, Transitions.linear, this);
					rotateTweeners[baseIndex+j].setTag(kRotateTagBase+baseIndex+j);
					rotateCompletedTag = rotateTweeners[baseIndex+j].getTag();
				}
			}
			
			// Tile tweeners
			rotateTweeners[baseIndex].resetTween(tile.getX(), tile.getX() + distX, duration, 0);
			rotateTweeners[baseIndex+1].resetTween(tile.getY(), tile.getY() + distY, duration, 0);
			
			// Shadow tweeners
			rotateTweeners[baseIndex+2].resetTween(shadow.getX(), shadow.getX() + distX, duration, 0);
			rotateTweeners[baseIndex+3].resetTween(shadow.getY(), shadow.getY() + distY, duration, 0);
		}
	}
	
	private void dropTiles() {
		float destX = 0, destY = 0, duration = kDropDurationFactor * this.duration;
		for (int i = 0, n = tiles.length; i < n; i++) {
			TilePiece tile = tiles[i];
			TileShadow shadow = tileShadows[i];
			
			switch (i) {
				case 0:
					destX = -kTileWidth;
					destY = 0;
					break;
				case 1:
					destX = -kTileWidth;
					destY = kTileHeight;
					break;
				case 2:
					destX = 0;
					destY = kTileHeight;
					break;
				case 3:
					destX = -kTileWidth;
					destY = -kTileHeight;
					break;
				case 4:
					destX = kTileWidth;
					destY = kTileHeight;
					break;
				case 5:
					destX = 0;
					destY = -kTileHeight;
					break;
				case 6:
					destX = kTileWidth;
					destY = -kTileHeight;
					break;
				case 7:
					destX = kTileWidth;
					destY = 0;
					break;
			}
			
			int baseIndex = 6 * i;
			if (dropTweeners.length > baseIndex && dropTweeners[baseIndex] == null) {
				for (int j = 0; j < 6; j++) {
					dropTweeners[baseIndex+j] = new FloatTweener(0, Transitions.easeIn, this);
					dropTweeners[baseIndex+j].setTag(kDropTagBase+baseIndex+j);
					dropCompletedTag = dropTweeners[baseIndex+j].getTag();
				}
			}
			
			// Tile tweeners
			dropTweeners[baseIndex].resetTween(tile.getX(), destX, duration, 0);
			dropTweeners[baseIndex+1].resetTween(tile.getY(), destY, duration, 0);
			dropTweeners[baseIndex+2].resetTween(tile.getScaleX(), 1f, duration, 0);
			
			// Shadow tweeners
			dropTweeners[baseIndex+3].resetTween(shadow.getX(), destX, duration, 0);
			dropTweeners[baseIndex+4].resetTween(shadow.getY(), destY, duration, 0);
			dropTweeners[baseIndex+5].resetTween(shadow.getScaleX(),
					tile.getTileDimensions().x / shadow.getWidth(), duration, 0);
		}
	}
	
	private int getBaseIndexFromTag(int tag) {
		if (tag >= kDropTagBase)
			return tag - kDropTagBase;
		else if (tag >= kRotateTagBase)
			return tag - kRotateTagBase;
		else if (tag >= kRaiseTagBase)
			return tag - kRaiseTagBase;
		else {
			assert(false) : "TileRotator::getBaseIndexFromTag invalid tag received.";
			return -1;
		}
	}
	
	public void stopAnimating() {
		scene.removeFromJuggler(this);
	}

	@Override
	public void onEvent(int evType, Object evData) {
		if (evType == FloatTweener.EV_TYPE_FLOAT_TWEENER_CHANGED) {
			FloatTweener tweener = (FloatTweener)evData;
			if (tweener != null) {
				float tweenedValue = tweener.getTweenedValue();
				int tag = tweener.getTag();
				int baseIndex = getBaseIndexFromTag(tag);
				
				if (tag >= kDropTagBase || (tag >= kRaiseTagBase && tag < kRotateTagBase)) {
					int tileIndex = baseIndex / 6;
					int attrIndex = baseIndex - tileIndex * 6;
					
					TilePiece tilePiece = tiles[tileIndex];
					TileShadow tileShadow = tileShadows[tileIndex];
					
					switch (attrIndex) {
						case 0: tilePiece.setX(tweenedValue); break;
						case 1: tilePiece.setY(tweenedValue); break;
						case 2: tilePiece.setScale(tweenedValue); break;
						case 3: tileShadow.setX(tweenedValue); break;
						case 4: tileShadow.setY(tweenedValue); break;
						case 5: tileShadow.setScale(tweenedValue); break;
					}
				} else if (tag >= kRotateTagBase) {
					int tileIndex = baseIndex / 4;
					int attrIndex = baseIndex - tileIndex * 4;
					
					TilePiece tilePiece = tiles[tileIndex];
					TileShadow tileShadow = tileShadows[tileIndex];
					
					switch (attrIndex) {
						case 0: tilePiece.setX(tweenedValue); break;
						case 1: tilePiece.setY(tweenedValue); break;
						case 2: tileShadow.setX(tweenedValue); break;
						case 3: tileShadow.setY(tweenedValue); break; // - kRaiseOffsetY); break;
					}
				}
			}
		} else if (evType == FloatTweener.EV_TYPE_FLOAT_TWEENER_COMPLETED) {
			FloatTweener tweener = (FloatTweener)evData;
			if (tweener != null) {
				int tag = tweener.getTag();
				if (tag == raiseCompletedTag) {
					setState(RotatorState.ROTATING);
					rotateTiles();
				} else if (tag == rotateCompletedTag) {
					setState(RotatorState.DROPPING);
					dropTiles();
				} else if (tag == dropCompletedTag) {
					setState(RotatorState.STATIONARY);
					stopAnimating();
					
					if (listener != null)
						listener.onEvent(EV_TYPE_ANIMATION_COMPLETED, this);
				}
			}
		}
	}
	
	@Override
	public boolean isComplete() {
		return hasAnimated && state == RotatorState.STATIONARY;
	}
	
	@Override
	public void advanceTime(float dt) {
		// Advance tweeners in reverse state order so we don't skip a frame
		if (getState() == RotatorState.DROPPING) {
			for (int i = 0, n = dropTweeners.length; i < n; i++)
				dropTweeners[i].advanceTime(dt);
		}
		
		if (getState() == RotatorState.ROTATING) {
			for (int i = 0, n = rotateTweeners.length; i < n; i++)
				rotateTweeners[i].advanceTime(dt);
		}
		
		if (getState() == RotatorState.RAISING) {
			for (int i = 0, n = raiseTweeners.length; i < n; i++)
				raiseTweeners[i].advanceTime(dt);
		}
		
		if (pivotPlayer != null && pivotPlayer.isAdvanceable())
			pivotPlayer.advanceTime(dt);
		
		for (int i = 0, n = playerPieces.size; i < n; i++) {
			PlayerPiece playerPiece = playerPieces.get(i);
			if (playerPiece != null && playerPiece.isAdvanceable())
				playerPiece.advanceTime(dt);
		}
	}
	
	@Override
	public void reset() {
		stopAnimating();
		
		if (pivotPlayer != null) {
			PlayerFactory.freePlayerPiece((AnimPlayerPiece)pivotPlayer);
			pivotPlayer = null;
		}
		
		if (pivotPiece != null) {
			TileFactory.freeTilePiece(pivotPiece);
			pivotPiece = null;
		}
		
		for (int i = 0, n = tiles.length; i < n; i++) {
			if (tiles[i] != null) {
				TileFactory.freeTilePiece(tiles[i]);
				tiles[i] = null;
			}
		}
		
		for (int i = 0, n = playerPieces.size; i < n; i++)
			PlayerFactory.freePlayerPiece((AnimPlayerPiece)playerPieces.get(i));
		playerPieces.clear();
		
		if (layers[kShadowLayerIndex] != null)
			layers[kShadowLayerIndex].clearChildren();
		
		if (layers[kTileLayerIndex] != null)
			layers[kTileLayerIndex].clearChildren();
		
		setListener(null);
		hasAnimated = false;
		state = RotatorState.STATIONARY;
	}
}
