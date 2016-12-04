package com.cheekymammoth.puzzleui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.cheekymammoth.gameModes.PuzzleMode;
import com.cheekymammoth.graphics.CMAtlasSprite;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.locale.ILocalizable;
import com.cheekymammoth.ui.TextUtils;
import com.cheekymammoth.utils.LangFX;
import com.cheekymammoth.utils.Utils;

public class LevelIcon extends Prop implements ILocalizable {
    private int levelIndex;
    private int levelID;
    private float labelBaseScale = 1f;
    private Label levelLabel;
    private Label solvedLabel;
    private CMAtlasSprite border;
    private CMAtlasSprite highlight;
    private CMAtlasSprite solvedIcon;
    private CMAtlasSprite solvedGlow;
    private CMAtlasSprite lock;
    private Prop canvas;
    
	public LevelIcon(int levelIndex, int levelID) {
		this(-1, levelIndex, levelID);
	}

	public LevelIcon(int category, int levelIndex, int levelID) {
		super(category);
		
		this.levelIndex = levelIndex;
		this.levelID = levelID;
		setTransform(true);
		
		canvas = new Prop();
		canvas.setTransform(true);
		addActor(canvas);
		
		highlight = new CMAtlasSprite(scene.textureRegionByName("level-highlight"));
		highlight.centerContent();
		highlight.setScale(
				(140 + 2 * 256.0f) / highlight.getWidth(),
				(140 + 2 * 265.0f) / highlight.getHeight());
//				1.25f * 2 * 256.0f / highlight.getWidth(),
//				1.25f * 2 * 265.0f / highlight.getHeight());
//		highlight.setColor(highlight.getColor().set(0xffff4dbf));
		
		Color highlightColor = highlight.getColor();
		highlightColor.set(PuzzleMode.kLevelColors[levelIndex]);
		Utils.setA(highlightColor, 0.75f);
		highlight.setColor(highlightColor);
		
		highlight.setVisible(false);
		canvas.addSpriteChild(highlight);
		
		border = new CMAtlasSprite(scene.textureRegionByName("level-icon-inner"));
		border.centerContent();
		border.setColor(border.getColor().set(PuzzleMode.kLevelColors[levelIndex]));
		canvas.addSpriteChild(border);
		setContentSize(border.getScaledWidth(), border.getScaledHeight());
		
		levelLabel = TextUtils.create(
				PuzzleMode.kLevelNames[levelIndex],
				32,
				TextUtils.kAlignBottom | TextUtils.kAlignCenter,
				Color.WHITE);
		levelLabel.setName(ILocalizable.kNonLocalizableName);
		labelBaseScale = levelLabel.getFontScaleX();
		canvas.addActor(levelLabel);
		layoutLabel();
		
		CMAtlasSprite pinStripe = new CMAtlasSprite(scene.textureRegionByName("level-pin-stripe"));
		pinStripe.setScale(0.9f * border.getWidth() / pinStripe.getWidth(), 1f);
		pinStripe.setPosition(
				border.getX() + (border.getWidth() - pinStripe.getWidth()) / 2,
				LangFX.getLevelIconYOffsets()[0] - 5);
		canvas.addSpriteChild(pinStripe);
		
		CMAtlasSprite levelIcon = new CMAtlasSprite(scene.textureRegionByName(PuzzleMode.kLevelTextureNames[levelIndex]));
		//levelIcon.setScale(levelIndex == PuzzleMode.getNumLevels()-1 ? 0.965f : 1f);
		levelIcon.setPosition(-levelIcon.getWidth() / 2, -levelIcon.getHeight() / 2);
		canvas.addSpriteChild(levelIcon);
		
		// Solved label
		float solvedPosX = LangFX.getLevelIconSolvedOffsets()[0];
		float solvedPosY = LangFX.getLevelIconSolvedOffsets()[1] - getHeight() / 2;
		float solvedLabelWidth = LangFX.getLevelIconSolvedOffsets()[3];
		solvedLabel = TextUtils.create(
				"0/6",
				28,
				TextUtils.kAlignLeft,
				Color.WHITE);
		solvedLabel.setPosition(solvedPosX - solvedLabelWidth / 2, solvedPosY);
		canvas.addActor(solvedLabel);
		
		solvedIcon = new CMAtlasSprite(scene.textureRegionByName("menu-key"));
		solvedIcon.setScale(0.7f);
		solvedIcon.setPosition(
				solvedLabel.getX() + 0.45f * solvedLabelWidth,
				solvedLabel.getY() + LangFX.getLevelIconSolvedOffsets()[2]);
		canvas.addSpriteChild(solvedIcon);
		
		// Lock
		lock = new CMAtlasSprite(scene.textureRegionByName("level-lock"));
		
		Prop lockProp = new Prop();
		lockProp.setTransform(true);
		lockProp.setPosition(
				-lock.getWidth() / 2, 
				-(border.getHeight() + 0.125f * lock.getHeight()) / 2);
		lockProp.addSpriteChild(lock);
		canvas.addActor(lockProp);
	}
	
	private void layoutLabel() {
		levelLabel.setFontScale(labelBaseScale);
		levelLabel.setPosition(
				border.getX() + (border.getWidth() - levelLabel.getWidth()) / 2,
				border.getY() + LangFX.getLevelIconYOffsets()[1] + 16);
		
		float scaleMax = 0.85f;
		float labelWidth = levelLabel.getTextBounds().width, labelHeight = levelLabel.getTextBounds().height;
		if (labelWidth > scaleMax * border.getWidth()) {
			float scaler = (scaleMax * border.getWidth()) / labelWidth;
			levelLabel.setFontScale(labelBaseScale * scaler);
			levelLabel.setY(levelLabel.getY() + (labelHeight - levelLabel.getTextBounds().height) / 2);
		}
	}
	
	public boolean isLocked() { return lock.isVisible(); }
	
	public void setLocked(boolean locked) { lock.setVisible(locked); }
	
	public int getLevelID() { return levelID; }
	
	private Rectangle visibleBoundsCache = new Rectangle();
	public Rectangle getVisibleBounds() {
		visibleBoundsCache.set(0, 0, border.getWidth(), border.getHeight());
		return visibleBoundsCache;
	}
	
	private Vector2 worldCenterCache = new Vector2();
	public Vector2 getVisibleWorldCenter() {
		worldCenterCache.set(0, 0);
		return localToStageCoordinates(worldCenterCache);
	}
	
	private Vector2 keyCenterCache = new Vector2();
	public Vector2 getSolvedKeyWorldCenter() {
		keyCenterCache.set(
				solvedIcon.getX() + solvedIcon.getWidth() / 2,
				solvedIcon.getY() + solvedIcon.getHeight() / 2);
		return canvas.localToStageCoordinates(keyCenterCache);
	}
	
	public void enableHighlight(boolean enable) {
		highlight.setVisible(enable);
	}
	
	public void enabledSolvedGlow(boolean enable) {
		if (solvedGlow != null)
			removeSpriteChild(solvedGlow);
		
		if (enable) {
			if (solvedGlow == null) {
				solvedGlow = new CMAtlasSprite(scene.textureRegionByName("menu-key-glow"));
				solvedGlow.setScale(solvedIcon.getScaleX());
				solvedGlow.setPosition(solvedIcon.getX(), solvedIcon.getY());
				solvedGlow.setColor(Utils.setA(solvedGlow.getColor(), 0));
			}
			
			canvas.addSpriteChild(solvedGlow);
		}
	}
	
	public void setPuzzlesSolved(int numSolved, int numPuzzles) {
		solvedLabel.setText("" + numSolved + "/" + numPuzzles);
	}
	
	public void setLevelTextColor(Color color) {
		levelLabel.setColor(color);
	}
	
	public void setPuzzlesSolvedColor(Color color) {
		solvedLabel.setColor(color);
	}

	public float getSolvedGlowOpacity() {
		return 0;
	}
	
	public void setSolvedGlowOpacity(float value) {
		if (solvedGlow != null)
			solvedGlow.setColor(Utils.setA(solvedGlow.getColor(), value));
	}

	@Override
	public void localeDidChange(String fontKey, String FXFontKey) {
		levelLabel.setText("");
		TextUtils.swapFont(fontKey, levelLabel, false);
		levelLabel.setText(scene.localize(PuzzleMode.kLevelNames[levelIndex]));
		layoutLabel();
		
		String solvedText = solvedLabel.getText().toString(); 
		solvedLabel.setText("");
		TextUtils.swapFont(fontKey, solvedLabel, false);
		solvedLabel.setText(solvedText);
		
		float solvedPosX = LangFX.getLevelIconSolvedOffsets()[0];
		float solvedPosY = LangFX.getLevelIconSolvedOffsets()[1] - getHeight() / 2;
		float solvedLabelWidth = LangFX.getLevelIconSolvedOffsets()[3];
		solvedLabel.setPosition(solvedPosX - solvedLabelWidth / 2, solvedPosY);
		solvedIcon.setPosition(
				solvedLabel.getX() + 0.45f * solvedLabelWidth,
				solvedLabel.getY() + LangFX.getLevelIconSolvedOffsets()[2]);
	}
}
