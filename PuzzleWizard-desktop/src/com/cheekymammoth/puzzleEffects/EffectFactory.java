package com.cheekymammoth.puzzleEffects;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.puzzleViews.TilePiece;
import com.cheekymammoth.utils.Coord;

public class EffectFactory {

	private EffectFactory() { }
	
	public static Shield getShield(int ID, int tileIndex, IEventListener listener) {
		Shield shield = Pools.obtain(Shield.class);
		shield.setID(tileIndex);
		shield.setTileIndex(tileIndex);
		shield.setListener(listener);
		return shield;
	}
	
	public static void freeShield(Shield shield) {
		if (shield != null)
			Pools.free(shield);
	}
	
	public static TileSwapper getTileSwapper(Vector2 swapOriginA, Vector2 swapOriginB, Array<TilePiece> tilePieces) {
		TileSwapper tileSwapper = Pools.obtain(TileSwapper.class);
		tileSwapper.configure(swapOriginA, swapOriginB, tilePieces);
		return tileSwapper;
	}
	
	public static void freeTileSwapper(TileSwapper tileSwapper) {
		if (tileSwapper != null)
			Pools.free(tileSwapper);
	}
	
	public static TileConveyorBelt getTileConveyorBelt(Rectangle boardBounds, Coord animDir, int wrapIndex,
			Array<TilePiece> beltTiles) {
		TileConveyorBelt conveyorBelt = Pools.obtain(TileConveyorBelt.class);
		conveyorBelt.configure(boardBounds, animDir, wrapIndex, beltTiles);
		return conveyorBelt;
	}
	
	public static void freeTileConveyorBelt(TileConveyorBelt conveyorBelt) {
		if (conveyorBelt != null)
			Pools.free(conveyorBelt);
	}
	
	public static TileRotator getTileRotator(float posX, float posY, Rectangle boardBounds, TilePiece[] tilePieces) {
		TileRotator rotator = Pools.obtain(TileRotator.class);
		rotator.configure(posX, posY, boardBounds, tilePieces);
		return rotator;
	}
	
	public static void freeTileRotator(TileRotator rotator) {
		if (rotator != null)
			Pools.free(rotator);
	}
	
	public static SolvedAnimation getSolvedAnimation(int category) {
		SolvedAnimation solvedAnim = Pools.obtain(SolvedAnimation.class);
		solvedAnim.setCategory(category);
		return solvedAnim;
	}
	
	public static void freeSolvedAnimation(SolvedAnimation solvedAnim) {
		if (solvedAnim != null)
			Pools.free(solvedAnim);
	}
	
	public static Twinkle getTwinkle() {
		return getTwinkle(-1, 0, 0);
	}
	
	public static Twinkle getTwinkle(float x, float y) {
		return getTwinkle(-1, x, y);
	}
	
	public static Twinkle getTwinkle(int category, float x, float y) {
		Twinkle twinkle = Pools.obtain(Twinkle.class);
		twinkle.setCategory(category);
		twinkle.setPosition(x, y);
		return twinkle;
	}
	
	public static void freeTwinkle(Twinkle twinkle) {
		if (twinkle != null)
			Pools.free(twinkle);
	}
}
