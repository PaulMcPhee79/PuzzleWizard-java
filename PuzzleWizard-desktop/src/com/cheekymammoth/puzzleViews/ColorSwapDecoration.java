package com.cheekymammoth.puzzleViews;

import com.badlogic.gdx.graphics.Color;
import com.cheekymammoth.graphics.CMAtlasSprite;
import com.cheekymammoth.puzzles.PuzzleHelper;
import com.cheekymammoth.utils.PWDebug;

public class ColorSwapDecoration extends TileDecoration {
	private CMAtlasSprite leftGlyph;
	private CMAtlasSprite rightGlyph;
	
	public ColorSwapDecoration() {
		this(-1, 0);
		PWDebug.tileDecorationCount++;
	}
	
	public ColorSwapDecoration(int category, int subType) {
		super(category, TilePiece.kTDKColorSwap, subType);
		
		leftGlyph = new CMAtlasSprite(scene.textureRegionByName("color-swap-glyph-left"));
		leftGlyph.centerContent();
		addSpriteChild(leftGlyph);
		
		rightGlyph = new CMAtlasSprite(scene.textureRegionByName("color-swap-glyph-right"));
		rightGlyph.centerContent();
		addSpriteChild(rightGlyph);
		
		setGlyphColors(subType);
		setContentSize(leftGlyph.getWidth(), leftGlyph.getHeight());
	}
	
	private void setGlyphColors(int subType) {
		Color leftColor = leftGlyph.getColor(), rightColor = rightGlyph.getColor();
		PuzzleHelper.setColorSwapColorsForTile(subType, leftColor, rightColor);
		leftGlyph.setColor(leftColor);
		rightGlyph.setColor(rightColor);
	}

	@Override
	public void setSubType(int value) {
//		if (value == getSubType())
//			return;
		
		super.setSubType(value);
		setGlyphColors(value);
	}
}
