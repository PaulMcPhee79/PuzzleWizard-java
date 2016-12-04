package com.cheekymammoth.puzzleViews;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.cheekymammoth.animations.MovieReel;
import com.cheekymammoth.graphics.CMAtlasSprite;
import com.cheekymammoth.puzzles.PuzzleHelper;
import com.cheekymammoth.puzzles.Tile;
import com.cheekymammoth.utils.PWDebug;

public class PainterDecoration extends TileDecoration {
	public static final float kColorArrowFps = 36f;
	
	private CMAtlasSprite[] arrows;
	private MovieReel[] streaks;
	
	public PainterDecoration() {
		this(-1, 0);
		PWDebug.tileDecorationCount++;
	}
	
	public PainterDecoration(int category, int subType) {
		super(category, TilePiece.kTDKPainter, subType);
		
		String arrowTexName = "color-arrow";
		TextureRegion arrowTexRegion = scene.textureRegionByName(arrowTexName);
		Array<AtlasRegion> frames = scene.textureRegionsStartingWith("color-streak");
		
		arrows = new CMAtlasSprite[4];
		streaks = new MovieReel[4];
		
		for (int i = 0, n = arrows.length; i < n; i++) {
			int colorKey = (subType >>> (i * 4)) & Tile.kColorKeyMask;
			
			// Streak
			MovieReel streak = new MovieReel(frames, kColorArrowFps);
			Color streakColor = streak.getColor();
			Color.rgba8888ToColor(streakColor, PuzzleHelper.colorForKey(colorKey));
			streak.setColor(streakColor);
			streak.centerContent();
			streak.setRotation(i * -90);
			streak.setVisible(colorKey != 0);
			streaks[i] = streak;
			addSpriteChild(streak);
			
			// Arrow
			CMAtlasSprite arrow = new CMAtlasSprite((AtlasRegion)arrowTexRegion);
			arrow.centerContent();
			arrow.setRotation(i * -90);
			arrow.setVisible(colorKey != 0);
			arrows[i] = arrow;
			addSpriteChild(arrow);
			
			if (i == 0)
				setContentSize(arrow.getWidth(), arrow.getHeight());
		}
		
		setSize(arrows[0].getWidth(), arrows[0].getHeight());
		setAdvanceable(true);
	}
	
	@Override
	public void setSubType(int value) {
//		if (value == getSubType())
//			return;
		
		super.setSubType(value);
		
		for (int i = 0, n = arrows.length; i < n; i++) {
			int colorKey = (value >>> (i * 4)) & Tile.kColorKeyMask;
			
			CMAtlasSprite arrow = arrows[i];
			arrow.setVisible(colorKey != 0);
			
			MovieReel streak = streaks[i];
			Color streakColor = streak.getColor();
			Color.rgba8888ToColor(streakColor, PuzzleHelper.colorForKey(colorKey));
			streak.setColor(streakColor);
			streak.setVisible(colorKey != 0);
		}
	}
	
	public void setColorArrowFrameIndex(int value) {
		for (int i = 0, n = streaks.length; i < n; i++)
			streaks[i].setCurrentFrame(value);
	}
	
	public void setColorArrowTime(float value) {
		for (int i = 0, n = streaks.length; i < n; i++)
			streaks[i].setCurrentTime(value);
	}
	
	@Override
	public void syncWithDecorator() {
		ITileDecorator decorator = getDecorator();
		if (decorator != null)
			//setColorArrowFrameIndex((int)decorator.decoratorValueForKey(getType()));
			setColorArrowTime(decorator.decoratorValueForKey(getType()));
	}
	
	@Override
	public void syncWithTileDecoration(TileDecoration other) {
		if (other.getType() == getType()) {
			PainterDecoration otherPainter = (PainterDecoration)other;
			for (int i = 0, n = streaks.length; i < n; i++)
				//streaks[i].setCurrentFrame(otherPainter.streaks[i].getCurrentFrame());
				streaks[i].setCurrentTime(otherPainter.streaks[i].getCurrentTime());
		}
	}

	@Override
	public void advanceTime(float dt) {
		if (getDecorator() != null)
			syncWithDecorator();
		else {
			for (int i = 0, n = streaks.length; i < n; i++)
				streaks[i].advanceTime(dt);
		}
	}
}
