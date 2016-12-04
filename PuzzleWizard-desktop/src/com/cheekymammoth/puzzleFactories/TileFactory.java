package com.cheekymammoth.puzzleFactories;

import com.badlogic.gdx.utils.Pools;
import com.cheekymammoth.puzzleViews.ColorSwapDecoration;
import com.cheekymammoth.puzzleViews.KeyDecoration;
import com.cheekymammoth.puzzleViews.PainterDecoration;
import com.cheekymammoth.puzzleViews.StockDecoration;
import com.cheekymammoth.puzzleViews.TeleportDecoration;
import com.cheekymammoth.puzzleViews.TileDecoration;
import com.cheekymammoth.puzzleViews.TilePiece;
import com.cheekymammoth.puzzles.Tile;

public class TileFactory {

	private TileFactory() { }
	
	public static Tile getTile() {
		return Pools.obtain(Tile.class);
	}
	
	public static void freeTile(Tile tile) {
		if (tile != null)
			Pools.free(tile);
	}
	
	public static void freeTiles(Tile[] tiles) {
		if (tiles != null) {
			for (int i = 0, n = tiles.length; i < n; i++)
				freeTile(tiles[i]);
		}
	}
	
	public static TilePiece getTilePiece(Tile tile) {
		TilePiece tilePiece = Pools.obtain(TilePiece.class);
		tilePiece.setData(tile);
		return tilePiece;
	}
	
	public static void freeTilePiece(TilePiece tilePiece) {
		if (tilePiece != null)
			Pools.free(tilePiece);
	}

	public static TileDecoration getTileDecoration(int category, int type, int subType) {
		TileDecoration tileDec = null;
		
		switch (type) {
			case TilePiece.kTDKTeleport:
				tileDec = Pools.obtain(TeleportDecoration.class);
				break;
			case TilePiece.kTDKColorSwap:
				tileDec = Pools.obtain(ColorSwapDecoration.class);
				break;
			case TilePiece.kTDKPainter:
				tileDec = Pools.obtain(PainterDecoration.class);
				break;
			case TilePiece.kTDKKey:
				tileDec = Pools.obtain(KeyDecoration.class);
				break;
			case TilePiece.kTDKRotate:
			case TilePiece.kTDKShield:
			case TilePiece.kTDKTileSwap:
			case TilePiece.kTDKMirrorImage:
			case TilePiece.kTDKColorFlood:
			case TilePiece.kTDKColorSwirl:
			case TilePiece.kTDKConveyorBelt:
			case TilePiece.kTDKColorMagic:
				tileDec = Pools.obtain(StockDecoration.class);
				break;
			default:
				assert(false) : "Invalid TileDecoration type in TileFactory::getTileDecoration()";
				break;
		}
		
		if (tileDec != null) {
			tileDec.setCategory(category);
			tileDec.setType(type);
			tileDec.setSubType(subType);
			tileDec.reuse();
		}
		
		return tileDec;
	}
	
	public static void freeDecoration(TileDecoration decoration) {
		if (decoration != null)
			Pools.free(decoration);
		
//		if (decoration == null)
//			return;
//		
//		switch (decoration.getType()) {
//			case TilePiece.kTDKTeleport:
//				Pools.free((TeleportDecoration)decoration);
//				break;
//			case TilePiece.kTDKColorSwap:
//				Pools.free((ColorSwapDecoration)decoration);
//				break;
//			case TilePiece.kTDKPainter:
//				Pools.free((PainterDecoration)decoration);
//				break;
//			case TilePiece.kTDKKey:
//				Pools.free((KeyDecoration)decoration);
//				break;
//			case TilePiece.kTDKRotate:
//			case TilePiece.kTDKShield:
//			case TilePiece.kTDKTileSwap:
//			case TilePiece.kTDKMirrorImage:
//			case TilePiece.kTDKColorFlood:
//			case TilePiece.kTDKColorSwirl:
//			case TilePiece.kTDKConveyorBelt:
//			case TilePiece.kTDKColorMagic:
//				Pools.free((StockDecoration)decoration);
//				break;
//			default:
//				assert(false) : "Invalid TileDecoration type in TileFactory::freeDecoration()";
//				break;
//		}
	}
	
	public static TeleportDecoration getTeleportDecoration(int category, int subType) {
		return (TeleportDecoration)getTileDecoration(category, TilePiece.kTDKTeleport, subType);
	}
	
	public static ColorSwapDecoration getColorSwapDecoration(int category, int subType) {
		return (ColorSwapDecoration)getTileDecoration(category, TilePiece.kTDKColorSwap, subType);
	}
	
	public static PainterDecoration getPainterDecoration(int category, int subType) {
		return (PainterDecoration)getTileDecoration(category, TilePiece.kTDKPainter, subType);
	}
	
	public static KeyDecoration getKeyDecoration(int category) {
		return (KeyDecoration)getTileDecoration(category, TilePiece.kTDKKey, 0);
	}
	
	public static StockDecoration getStockDecoration(int category, int type, int subType) {
		return (StockDecoration)getTileDecoration(category, type, subType);
	}

}
