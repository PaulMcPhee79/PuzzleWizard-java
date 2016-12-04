package com.cheekymammoth.puzzleViews;

import com.badlogic.gdx.graphics.Color;
import com.cheekymammoth.graphics.CMAtlasSprite;
import com.cheekymammoth.puzzles.PuzzleHelper;
import com.cheekymammoth.puzzles.Tile;
import com.cheekymammoth.utils.PWDebug;

public class StockDecoration extends TileDecoration {
	private CMAtlasSprite icon;
	
	public StockDecoration() {
		this(-1, TilePiece.kTDKNone, 0);
		PWDebug.tileDecorationCount++;
	}
	
	public StockDecoration(int category, int type, int subType) {
		super(category, type, subType);
		
		String texName = texNameForType(type, subType);
		icon = new CMAtlasSprite(scene.textureRegionByName(texName));
		icon.centerContent();
		this.addSpriteChild(icon);
		setContentSize(icon.getWidth(), icon.getHeight());
		
		if (type == TilePiece.kTDKColorFlood)
			Color.rgb888ToColor(getColor(), PuzzleHelper.colorForKey(subType & Tile.kColorKeyMask));
		else if (type == TilePiece.kTDKConveyorBelt)
			icon.setRotation(subType * -90);
	}
	
	private String texNameForType(int type, int subType) {
		String texName = null;
		
		switch (type) {
			case TilePiece.kTDKRotate: texName = "rotate"; break;
			case TilePiece.kTDKShield: texName = "shield"; break;
			case TilePiece.kTDKTileSwap: texName = "tile-swap-" + subType; break;
			case TilePiece.kTDKMirrorImage: texName = "mirrored"; break;
			case TilePiece.kTDKColorFlood: texName = "color-flood"; break;
			case TilePiece.kTDKColorSwirl: texName = "color-swirl"; break;
			case TilePiece.kTDKConveyorBelt: texName = "conveyor-belt"; break;
			case TilePiece.kTDKColorMagic:
			default:
				texName = "color-magic";
				break;
		}
		
		return texName;
	}
	
	@Override
	public void setType(int value) {
		if (value == getType())
			return;
		
		super.setType(value);
		
		int subType = getSubType();
		String texName = texNameForType(value, subType);
		icon.setAtlasRegion(scene.textureRegionByName(texName));
		icon.centerContent();
		icon.setRotation(0);
		setContentSize(icon.getWidth(), icon.getHeight());
		
		if (value == TilePiece.kTDKColorFlood)
			Color.rgba8888ToColor(getColor(), PuzzleHelper.colorForKey(subType & Tile.kColorKeyMask));
		else
			setColor(Color.WHITE);
		
		if (value == TilePiece.kTDKConveyorBelt)
			icon.setRotation(subType * -90);
		else
			setOrigin(0, 0);
	}
	
	@Override
	public void setSubType(int value) {
//		if (value == getSubType())
//			return;
		
		super.setSubType(value);
		
		int type = getType(), subType = getSubType();
		//icon.setRotation(0);
		if (type == TilePiece.kTDKTileSwap) {
			String texName = texNameForType(type, subType);
			icon.setAtlasRegion(scene.textureRegionByName(texName));
			icon.centerContent();
			setContentSize(icon.getWidth(), icon.getHeight());
		} else if (type == TilePiece.kTDKColorFlood)
			Color.rgba8888ToColor(getColor(), PuzzleHelper.colorForKey(value & Tile.kColorKeyMask));
		else if (type == TilePiece.kTDKConveyorBelt)
			icon.setRotation(value * -90);
	}
}
