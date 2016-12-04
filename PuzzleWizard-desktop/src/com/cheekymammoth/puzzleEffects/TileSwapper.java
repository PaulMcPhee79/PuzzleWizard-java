package com.cheekymammoth.puzzleEffects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.puzzleFactories.TileFactory;
import com.cheekymammoth.puzzleViews.TilePiece;
import com.cheekymammoth.puzzleViews.TilePiece.AestheticState;

public class TileSwapper extends Prop implements IEventListener, Poolable {
	public static final int EV_TYPE_ANIMATION_COMPLETED;
	
	private static final float kTileWidth = 144f;
	private static final float kTileHeight = 144f;
	
	static {
		EV_TYPE_ANIMATION_COMPLETED = EventDispatcher.nextEvType();
	}
	
	private boolean hasStoppedAnimating;
	private Array<TilePiece> tilePieces = new Array<TilePiece>(true, 16, TilePiece.class);
	private Dissolver dissolver;
	private IEventListener listener;
	
	public TileSwapper() {
		this(-1);
	}

	public TileSwapper(int category) {
		super(category);
		
		dissolver = new Dissolver(getCategory(), 1, 0f, 1f);
		dissolver.setListener(this);
		addActor(dissolver);
	}
	
	void configure(Vector2 swapOriginA, Vector2 swapOriginB, Array<TilePiece> tilePieces) {
		assert (tilePieces != null && tilePieces.size > 1 && (tilePieces.size & 1) == 0) : "Bad args in TileSwapper::configure";
		
		float tileOriginX = 0, tileOriginY = 0;
		for (int i = 0; i < 2; i++) {
			Vector2 swapOrigin = i == 0 ? swapOriginA : swapOriginB;
			for (int j = 0, jn = tilePieces.size / 2; j < jn; j++) {
				int tileIndex = i * tilePieces.size / 2 + j;
				if (tilePieces.get(tileIndex) == null)
					continue;
				
				TilePiece tilePiece = tilePieces.get(tileIndex).clone();
				if (tilePiece.getTile() != null && !tilePiece.getTile().isEdgeTile())
					tilePiece.setAesState(AestheticState.OCCLUSION);
				tilePiece.setData(null);
				
				switch (j) {
					case 0:
						tileOriginX = -kTileWidth;
						tileOriginY = kTileHeight;
						break;
					case 1:
						tileOriginX = 0;
						tileOriginY = kTileHeight;
						break;
					case 2:
						tileOriginX = kTileWidth;
						tileOriginY = kTileHeight;
						break;
					case 3:
						tileOriginX = -kTileWidth;
						tileOriginY = 0;
						break;
					case 4:
						tileOriginX = 0;
						tileOriginY = 0;
						break;
					case 5:
						tileOriginX = kTileWidth;
						tileOriginY = 0;
						break;
					case 6:
						tileOriginX = -kTileWidth;
						tileOriginY = -kTileHeight;
						break;
					case 7:
						tileOriginX = 0;
						tileOriginY = -kTileHeight;
						break;
					case 8:
						tileOriginX = kTileWidth;
						tileOriginY = -kTileHeight;
						break;
				}
				
				tileOriginX += swapOrigin.x;
				tileOriginY += swapOrigin.y;
				
				tilePiece.setPosition(tileOriginX, tileOriginY);
				this.tilePieces.add(tilePiece);
				dissolver.addDissolvee(tilePiece);
			}
		}
	}
	
	public IEventListener getListener() { return listener; }
	
	public void setListener(IEventListener value) { listener = value; }
	
	public void stopAnimating() {
		hasStoppedAnimating = true;
		scene.removeFromJuggler(this);
	}
	
	@Override
	public boolean isComplete() {
		return hasStoppedAnimating;
	}
	
	@Override
	public void advanceTime(float dt) {
		if (hasStoppedAnimating)
			return;
		
		if (dissolver != null)
			dissolver.advanceTime(dt);
	}
	
	@Override
	public void onEvent(int evType, Object evData) {
		if (evType == Dissolver.EV_TYPE_DISSOLVE_CYCLE_COMPLETED) {
			stopAnimating();
			if (listener != null)
				listener.onEvent(EV_TYPE_ANIMATION_COMPLETED, this);
		}
	}

	@Override
	public void reset() {
		dissolver.clearDissolvees();
		dissolver.reset();
		
		for (int i = 0, n = tilePieces.size; i < n; i++)
			TileFactory.freeTilePiece(tilePieces.get(i));
		tilePieces.clear();
		
		stopAnimating();
		remove();
		setListener(null);
		hasStoppedAnimating = false;
	}
}
