package com.cheekymammoth.puzzleEffects;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntMap.Keys;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.gameModes.PuzzleMode;
import com.cheekymammoth.graphics.CroppedProp;
import com.cheekymammoth.graphics.ICustomRenderer;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.puzzleFactories.PlayerFactory;
import com.cheekymammoth.puzzleFactories.TileFactory;
import com.cheekymammoth.puzzleViews.AnimPlayerPiece;
import com.cheekymammoth.puzzleViews.ITileDecorator;
import com.cheekymammoth.puzzleViews.PlayerPiece;
import com.cheekymammoth.puzzleViews.TilePiece;
import com.cheekymammoth.puzzleViews.TilePiece.AestheticState;
import com.cheekymammoth.utils.Coord;
import com.cheekymammoth.utils.FloatTweener;
import com.cheekymammoth.utils.Transitions;
import com.cheekymammoth.utils.Utils;

public class TileConveyorBelt extends Prop implements IEventListener, Poolable {
	private enum CBeltState { STATIONARY, RAISING, SLIDING, DROPPING };
	
	public static final int EV_TYPE_ANIMATION_COMPLETED;
	private static final int kOccLayerIndex = 0;
	private static final int kPlayerLayerIndex = 1;
	private static final int kWrapLayerIndex = 2;
	private static final int kNumLayers = kWrapLayerIndex + 1;
	private static final int kHorizBelt = 1;
	private static final int kVertBelt = 2;
	private static final float kRaisedTileScale = 1.6f;
	private static final float kRaisedShadowScale = 2 * 1.8f; // x2 because image resource is half size
	private static final float kTileWidth = 144f;
	private static final float kTileHeight = 144f;
	private static final float kRaiseDurationFactor = 0.125f;
	private static final float kSlideDurationFactor = 0.75f;
	
	static {
		EV_TYPE_ANIMATION_COMPLETED = EventDispatcher.nextEvType();
	}
	
	private boolean hasAnimated;
	private boolean hasStoppedAnimating;
	private CBeltState state = CBeltState.STATIONARY;
	private int edgeStatus = PuzzleEffects.kEdgeStatusNone; // OR'ed PuzzleEffects.kEdgeStatus
	private int wrapIndex;
	private Coord animDir;
	private float duration;
	private float shadowBaseScale = 1f;
	private float raiseOffsetY;
	private Rectangle shadowBoundsCache = new Rectangle();
	private Rectangle occBoundsCache = new Rectangle();
	private Array<TilePiece> beltTilePieces = new Array<TilePiece>(true, 10, TilePiece.class);
	private Array<TilePiece> occTilePieces = new Array<TilePiece>(true, 2, TilePiece.class);
	private TilePiece wrapTile;
	private TileShadow wrapShadow;
	private CroppedProp wrapShadowCrop;
	private Prop[] layers = new Prop[kNumLayers];
	private IntMap<PlayerPiece> playerPieces = new IntMap<PlayerPiece>(2);
	private float beltMovement;
	private FloatTweener moveBeltTweener;
	private FloatTweener moveWrapTweener;
	private FloatTweener scaleWrapTweener;
	private FloatTweener raiseWrapTweener;
	private ITileDecorator decorator;
	private IEventListener listener;
	
	public TileConveyorBelt() {
		this(-1);
	}
	
	public TileConveyorBelt(int category) {
		super(category);
		
		setTransform(true);
		setCustomRenderer(new ICustomRenderer() {
			@Override
			public void preDraw(Batch batch, float parentAlpha, Object obj) {
				if (wrapShadow != null) {
					shadowBoundsCache.set(wrapShadow.getShadowBounds());
					for (int i = 0, n = occTilePieces.size; i < n; i++) {
						occBoundsCache.set(occTilePieces.get(i).getTileBounds());
						occBoundsCache = Utils.intersectionRect(shadowBoundsCache, occBoundsCache);
						
						if (occBoundsCache.width > 0 && occBoundsCache.height > 0) {
							Rectangle overlap = TileConveyorBelt.this.getOcclusionOverlap(i);
							occBoundsCache.set(
									occBoundsCache.x + overlap.x,
									occBoundsCache.y + overlap.y,
									occBoundsCache.width + overlap.width,
									occBoundsCache.height + overlap.height);
							wrapShadow.setOcclusionBounds(occBoundsCache);
							break;
						}
					}
				}
			}
			
			@Override
			public void postDraw(Batch batch, float parentAlpha, Object obj) {

			}
		});
	}
	
	public void configure(Rectangle boardBounds, Coord animDir, int wrapIndex, Array<TilePiece> beltTiles) {
		assert(Math.abs(animDir.x + animDir.y) == 1) : "Invalid TileConveyorBelt animation direction.";
		
		this.animDir = animDir;
		this.wrapIndex = wrapIndex;

		if (layers[kOccLayerIndex] == null) {
			layers[kOccLayerIndex] = new Prop();
			layers[kOccLayerIndex].setTransform(true);
			addActor(layers[kOccLayerIndex]);
		}
		if (layers[kPlayerLayerIndex] == null) {
			layers[kPlayerLayerIndex] = new Prop();
			layers[kPlayerLayerIndex].setTransform(true);
			addActor(layers[kPlayerLayerIndex]);
		}
		if (layers[kWrapLayerIndex] == null) {
			layers[kWrapLayerIndex] = new Prop();
			layers[kWrapLayerIndex].setTransform(true);
			addActor(layers[kWrapLayerIndex]);
		}
		
		Prop occLayer = layers[kOccLayerIndex];
		@SuppressWarnings("unused")
		Prop playerLayer = layers[kPlayerLayerIndex];
		Prop wrapLayer = layers[kWrapLayerIndex];
		
		// Belt
		for (int i = 0, n = beltTiles.size; i < n; i++) {
			TilePiece tilePiece = beltTiles.get(i);
			tilePiece.setVisible(i != wrapIndex);
			beltTilePieces.add(tilePiece);
			
			if (animDir.y == 1 && i == wrapIndex - 1)
				tilePiece.setAesState(AestheticState.EDGE);
		}
		
		// Invisible occlusion helpers
		int occFactor = wrapIndex == 0 ? beltTiles.size : -beltTiles.size;
		for (int i = 0; i < 2; i++) {
			TilePiece tilePiece = beltTiles.get(wrapIndex).clone();
			tilePiece.setAesState(AestheticState.OCCLUSION);
			tilePiece.setPosition(beltTiles.get(wrapIndex).getX(),  beltTiles.get(wrapIndex).getY());
			tilePiece.setVisible(false);
			
			if (i == 1) {
				if (animDir.x != 0)
					tilePiece.setX(tilePiece.getX() + occFactor * kTileWidth);
				else
					tilePiece.setY(tilePiece.getY() - occFactor * kTileHeight);
			}
			
			occTilePieces.add(tilePiece);
			occLayer.addActor(tilePiece);
		}
		
		// Wrap
		wrapTile = beltTiles.get(wrapIndex).clone();
		if (beltTiles.size > 0)
			wrapTile.setDecorator(beltTiles.get(0).getDecorator());
		wrapTile.setAesState(AestheticState.EDGE);
		wrapTile.setPosition(beltTiles.get(wrapIndex).getX(), beltTiles.get(wrapIndex).getY());
		
		raiseOffsetY = 1.325f * (kRaisedTileScale - 1f) * (kTileHeight + wrapTile.getOffsetY());
		
		if (wrapShadow == null)
			wrapShadow = new TileShadow(getCategory(), scene.textureByName("tile-shadow.png"), occTilePieces.get(0).getTileBounds());
		else
			wrapShadow.setOcclusionBounds(occTilePieces.get(0).getTileBounds());
		wrapShadow.setPosition(wrapTile.getShadowPosition().x, wrapTile.getShadowPosition().y);
		wrapShadow.setScale(1.0f);
		shadowBaseScale = kTileWidth / wrapShadow.getWidth();
		wrapShadow.setScale(shadowBaseScale);
		
		if (wrapShadowCrop == null)
			wrapShadowCrop = new CroppedProp(-1, boardBounds);
		else
			wrapShadowCrop.setViewableRegion(boardBounds);
		wrapShadowCrop.addActor(wrapShadow);
		
		wrapLayer.addActor(wrapShadowCrop);
		wrapLayer.addActor(wrapTile);
		
		// Tweeners
		if (moveBeltTweener == null) moveBeltTweener = new FloatTweener(0, Transitions.linear, this);
		if (moveWrapTweener == null) moveWrapTweener = new FloatTweener(0, Transitions.linear, this);
		if (scaleWrapTweener == null) scaleWrapTweener = new FloatTweener(0, Transitions.easeOut, this);
		if (raiseWrapTweener == null) raiseWrapTweener = new FloatTweener(0, Transitions.easeOut, this);
		
		setEdgeStatus(PuzzleEffects.kEdgeStatusNone);
	}
	
	private int getOrientation() { return animDir.x != 0 ? kHorizBelt : kVertBelt; }
	
	public ITileDecorator getDecorator() { return decorator; }
	
	public void setDecorator(ITileDecorator value) { decorator = value; }
	
	public IEventListener getListener() { return listener; }
	
	public void setListener(IEventListener value) { listener = value; }

	private CBeltState getState() { return state; }
	
	private void setState(CBeltState value) {
		if (state == value)
			return;
		
		switch (value) {
			case STATIONARY:
				break;
			case RAISING:
				hasAnimated = true;
				break;
			case SLIDING:
				break;
			case DROPPING:
				break;
		}
		
		state = value;
	}
	
	public void setEdgeStatus(int value) {
		edgeStatus = value;
	}
	
	private float getScaleWrapProxy() {
		return wrapTile.getScaleX();
	}
	
	private void setScaleWrapProxy(float value) {
		wrapTile.setScale(value);
		float shadowScale = shadowBaseScale + ((value - 1.0f) / (kRaisedTileScale - 1.0f)) *
				(kRaisedShadowScale - shadowBaseScale);
		wrapShadow.setScale(shadowScale);
	}
	
	private void syncWrapShadowPosition() {
		if (wrapTile != null && wrapShadow != null && kRaisedShadowScale > shadowBaseScale) {
			// 0.5f because image resource is half size
			float raisePercent = 0.5f * (wrapShadow.getScaleY() - shadowBaseScale) /
					(kRaisedShadowScale - shadowBaseScale);
			wrapShadow.setY(wrapTile.getShadowPosition().y + raisePercent * kRaisedShadowScale *
					(wrapTile.getShadowPosition().y - wrapTile.getY()));
		}
	}
	
	private float getMoveWrapProxy() {
		return getOrientation() == kHorizBelt ? wrapTile.getX() : wrapTile.getY();
	}
	
	private void setMoveWrapProxy(float value) {
		if (getOrientation() == kHorizBelt) {
			wrapTile.setX(value);
			wrapShadow.setX(value);
		} else {
			wrapTile.setY(value);
			syncWrapShadowPosition();
		}
	}
	
	private float getRaiseWrapProxy() {
		return wrapTile != null ? wrapTile.getY() : 0;
	}
	
	private void setRaiseWrapProxy(float value) {
		if (wrapTile != null)
		{
			wrapTile.setY(value);
			syncWrapShadowPosition();
		}
	}
	
//	private float getMoveBeltProxy() {
//		return beltMovement;
//	}
	
	private void setMoveBeltProxy(float value) {
		float delta = value - beltMovement;
		beltMovement = value;
		
		if (getOrientation() == kHorizBelt) {
			for (int i = 0, n = beltTilePieces.size; i < n; i++)
				beltTilePieces.get(i).setX(beltTilePieces.get(i).getX() + delta);
			for (int i = 0, n = occTilePieces.size; i < n; i++)
				occTilePieces.get(i).setX(occTilePieces.get(i).getX() + delta);
		} else {
			for (int i = 0, n = beltTilePieces.size; i < n; i++)
				beltTilePieces.get(i).setY(beltTilePieces.get(i).getY() + delta);
			for (int i = 0, n = occTilePieces.size; i < n; i++)
				occTilePieces.get(i).setY(occTilePieces.get(i).getY() + delta);
		}
		
		Keys keys = playerPieces.keys();
		while (keys.hasNext) {
			int key = keys.next();
			PlayerPiece playerPiece = playerPieces.get(key);
			if (playerPiece.getParent() == wrapTile) // If on wrap tile, let it move with the wrap tile
				continue;
			TilePiece tilePiece = beltTilePieces.get(key);
			playerPiece.setPosition(tilePiece.getX(), tilePiece.getY());
		}
	}
	
	// NOTE: Currently does not support > 1 player on the same conveyorbelt tile.
	public void addPlayerPiece(int index, PlayerPiece playerPiece) {
		if (playerPiece == null)
			return;
		
		do {
			if (playerPieces.get(index) == null) {
				if (index == wrapIndex) {
					playerPiece.setPosition(0, 0);
					wrapTile.addActor(playerPiece);
					playerPieces.put(index, playerPiece);
					break;
				} else if (beltTilePieces.size > index && index >= 0 && playerPieces.get(index) == null) {
					playerPiece.setPosition(beltTilePieces.get(index).getX(), beltTilePieces.get(index).getY());
					layers[kPlayerLayerIndex].addActor(playerPiece);
					playerPieces.put(index, playerPiece);
					break;
				}
			}
			
			PlayerFactory.freePlayerPiece((AnimPlayerPiece)playerPiece);
		} while (false);
	}
	
	public void animate(float duration) {
		if (hasAnimated)
			return;
		
		this.duration = duration;
		setState(CBeltState.RAISING);
		raiseWrapTile();
	}
	
	public void stopAnimating() {
		hasStoppedAnimating = true;
		scene.removeFromJuggler(this);
	}
	
	private void raiseWrapTile() {
		scaleWrapTweener.setInterpolation(Transitions.easeOut);
		scaleWrapTweener.resetTween(
				getScaleWrapProxy(),
				kRaisedTileScale,
				kRaiseDurationFactor * duration,
				0);
		
		raiseWrapTweener.setInterpolation(Transitions.easeOut);
		raiseWrapTweener.resetTween(
				getRaiseWrapProxy(),
				getRaiseWrapProxy() + raiseOffsetY,
				kRaiseDurationFactor * duration,
				0);
	}
	
	private void slideWrapTile() {
		float dist = getOrientation() == kHorizBelt
				? -animDir.x * (beltTilePieces.size - 1) * kTileWidth
				: animDir.y * (beltTilePieces.size - 1) * kTileHeight;
		moveWrapTweener.resetTween(
				getMoveWrapProxy(),
				getMoveWrapProxy() + dist,
				kSlideDurationFactor * duration,
				0);
		scene.playSound(getOrientation() == kHorizBelt
				? PuzzleMode.getConveyorBeltHorizSoundName()
				: PuzzleMode.getConveyorBeltVertSoundName());
	}
	
	private void slideBeltTiles() {
		float dist = getOrientation() == kHorizBelt
				? animDir.x * kTileWidth
				: -animDir.y * kTileHeight;
		beltMovement = 0;
		moveBeltTweener.resetTween(
				beltMovement,
				dist,
				kSlideDurationFactor * duration,
				0);
	}
	
	private void dropWrapTile() {
		scaleWrapTweener.setInterpolation(Transitions.easeIn);
		scaleWrapTweener.resetTween(
				getScaleWrapProxy(),
				1.0f,
				kRaiseDurationFactor * duration,
				0);
		
		raiseWrapTweener.setInterpolation(Transitions.easeIn);
		raiseWrapTweener.resetTween(
				getRaiseWrapProxy(),
				getRaiseWrapProxy() - raiseOffsetY,
				kRaiseDurationFactor * duration,
				0);
	}
	
	@Override
	public void onEvent(int evType, Object evData) {
		if (evType == FloatTweener.EV_TYPE_FLOAT_TWEENER_CHANGED) {
			FloatTweener tweener = (FloatTweener)evData;
			if (tweener != null) {
				float tweenedValue = tweener.getTweenedValue();
				if (tweener == moveBeltTweener)
					setMoveBeltProxy(tweenedValue);
				else if (tweener == moveWrapTweener)
					setMoveWrapProxy(tweenedValue);
				else if (tweener == scaleWrapTweener)
					setScaleWrapProxy(tweenedValue);
				else if (tweener == raiseWrapTweener)
					setRaiseWrapProxy(tweenedValue);
			}
		} else if (evType == FloatTweener.EV_TYPE_FLOAT_TWEENER_COMPLETED) {
			FloatTweener tweener = (FloatTweener)evData;
			if (tweener != null) {
				switch (getState()) {
					case RAISING:
						if (tweener == scaleWrapTweener) {
							setState(CBeltState.SLIDING);
							slideWrapTile();
							slideBeltTiles();
						}
						break;
					case SLIDING:
						if (tweener == moveWrapTweener) {
							setState(CBeltState.DROPPING);
							dropWrapTile();
						}
						break;
					case DROPPING:
						if (tweener == scaleWrapTweener) {
							setState(CBeltState.STATIONARY);
							stopAnimating();
							
							if (listener != null)
								listener.onEvent(EV_TYPE_ANIMATION_COMPLETED, this);
						}
						break;
					default:
						break;
				}
			}
		}
	}
	
	@Override
	public boolean isComplete() {
		return hasStoppedAnimating;
	}
	
	@Override
	public void advanceTime(float dt) {
		// Advance tweeners in reverse state order so we don't skip a frame. Don't combine RAISING and DROPPING.
		if (getState() == CBeltState.DROPPING) {
			raiseWrapTweener.advanceTime(dt);
			scaleWrapTweener.advanceTime(dt);
		}
		
		if (getState() == CBeltState.SLIDING) {
			moveBeltTweener.advanceTime(dt);
			moveWrapTweener.advanceTime(dt);
		}
		
		if (getState() == CBeltState.RAISING) {
			raiseWrapTweener.advanceTime(dt);
			scaleWrapTweener.advanceTime(dt);
		}
		
		Keys keys = playerPieces.keys();
		while (keys.hasNext) {
			PlayerPiece playerPiece = playerPieces.get(keys.next());
			if (playerPiece.isAdvanceable())
				playerPiece.advanceTime(dt);
		}
	}
	
	// occIndex: 0 => wrap tile, 1 => far end tile
	private Rectangle occOverlapCache = new Rectangle();
	private Rectangle getOcclusionOverlap(int occIndex) {
		int edgeStatus = PuzzleEffects.kEdgeStatusNone;
	    int edgeOverlap = 20;
	    int orientation = getOrientation();
	    
	    if (orientation == kHorizBelt) {
	        if (wrapIndex == 0) // Left-moving belt (ie right-moving wrap tile)
	            edgeStatus |= occIndex == 0
	            	? PuzzleEffects.kEdgeStatusLeft
	            	: PuzzleEffects.kEdgeStatusRight;
	        else // Right-moving belt
	            edgeStatus |= occIndex == 0
	            	? PuzzleEffects.kEdgeStatusRight
	            	: PuzzleEffects.kEdgeStatusLeft; 
	        edgeStatus |= this.edgeStatus & (PuzzleEffects.kEdgeStatusTop | PuzzleEffects.kEdgeStatusBottom);
	    } else { // kVertBelt
	        if (wrapIndex == 0) // Up-moving belt
	            edgeStatus |= occIndex == 0
	            	? PuzzleEffects.kEdgeStatusTop
	            	: PuzzleEffects.kEdgeStatusBottom;
	        else // Down-moving belt
	            edgeStatus |= occIndex == 0
	            	? PuzzleEffects.kEdgeStatusBottom
	            	: PuzzleEffects.kEdgeStatusTop;
	        edgeStatus |= this.edgeStatus & (PuzzleEffects.kEdgeStatusLeft | PuzzleEffects.kEdgeStatusRight);
	    }
	    
	    occOverlapCache.set(0, 0, 0, 0);
	    if ((edgeStatus & PuzzleEffects.kEdgeStatusLeft) == PuzzleEffects.kEdgeStatusLeft) {
	    	occOverlapCache.set(
	    			occOverlapCache.x - edgeOverlap,
	    			occOverlapCache.y,
	    			occOverlapCache.width + edgeOverlap,
	    			occOverlapCache.height);
	    	//Gdx.app.log("TileConveyorBelt", "PADDING LEFT");
	        
	    } else if ((edgeStatus & PuzzleEffects.kEdgeStatusRight) == PuzzleEffects.kEdgeStatusRight) {
	    	occOverlapCache.set(
	    			occOverlapCache.x,
	    			occOverlapCache.y,
	    			occOverlapCache.width + edgeOverlap,
	    			occOverlapCache.height);
	    	//Gdx.app.log("TileConveyorBelt", "PADDING RIGHT");
	    }
	    
	    if ((edgeStatus & PuzzleEffects.kEdgeStatusTop) == PuzzleEffects.kEdgeStatusTop) {
	    	occOverlapCache.set(
	    			occOverlapCache.x,
	    			occOverlapCache.y,
	    			occOverlapCache.width,
	    			occOverlapCache.height + edgeOverlap);
	    	//Gdx.app.log("TileConveyorBelt", "PADDING TOP");
	    }
	    if ((edgeStatus & PuzzleEffects.kEdgeStatusBottom) == PuzzleEffects.kEdgeStatusBottom) {
	    	occOverlapCache.set(
	    			occOverlapCache.x,
	    			occOverlapCache.y - edgeOverlap,
	    			occOverlapCache.width,
	    			occOverlapCache.height + edgeOverlap);
	    	//Gdx.app.log("TileConveyorBelt", "PADDING BOTTOM");
	    }
	    
	    return occOverlapCache;
	}
	
	@Override
	public void reset() {
		// beltTilePieces are on loan (not cloned), so leave this loop commented
//		for (int i = 0, n = beltTilePieces.size; i < n; i++)
//			TileFactory.freeTilePiece(beltTilePieces.get(i));
		beltTilePieces.clear();
		
		for (int i = 0, n = occTilePieces.size; i < n; i++)
			TileFactory.freeTilePiece(occTilePieces.get(i));
		occTilePieces.clear();
		
		TileFactory.freeTilePiece(wrapTile);
		wrapTile = null;
		
		Keys keys = playerPieces.keys();
		while (keys.hasNext) {
			PlayerPiece playerPiece = playerPieces.get(keys.next());
			PlayerFactory.freePlayerPiece((AnimPlayerPiece)playerPiece);
		}
		playerPieces.clear();
		
		wrapShadowCrop.clearChildren();
		layers[kOccLayerIndex].clearChildren();
		layers[kPlayerLayerIndex].clearChildren();
		layers[kWrapLayerIndex].clearChildren();
		
		setListener(null);
		setDecorator(null);
		scene.removeFromJuggler(this);
		hasAnimated = hasStoppedAnimating = false;
		state = CBeltState.STATIONARY;
	}
}
