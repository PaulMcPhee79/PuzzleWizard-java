package com.cheekymammoth.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.cheekymammoth.gameModes.PuzzleMode;
import com.cheekymammoth.graphics.CMSprite;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.locale.ILocalizable;
import com.cheekymammoth.ui.INavigable.NavigationMap;
import com.cheekymammoth.utils.LangFX;

public class LevelCompletedDialog extends MenuDialog {
	private static final float kLevelPropHeight = 500f;
	private int levelIndex;
	private float maxWidth;
	//private float maxHeight;
	private Label levelHeader;
	private Label levelLabel;
	private Label completedLabel;
	private Prop levelProp;
	private Prop container;
	private Prop[] stars = new Prop[2];
	
	public LevelCompletedDialog() {
		this(-1, 0, 0, 0, 0, 0);
	}

	public LevelCompletedDialog(int category, int priority, int inputFocus, int levelIndex,
			float maxWidth, float maxHeight) {
		super(category, priority, inputFocus, NavigationMap.NAV_VERT);
		
		this.levelIndex = Math.max(0, Math.min(PuzzleMode.getNumLevels()-1, levelIndex));
		this.maxWidth = maxWidth;
		//this.maxHeight = maxHeight;
		
		Prop layer = layerAtIndex(kContentLayerIndex);
		container = new Prop();
		container.setTransform(true);
		layer.addActor(container);
		
		Label title = TextUtils.create("Congratulations!", 56, TextUtils.kAlignCenter, new Color(MenuBuilder.kMenuDarkYellow));
		title.setPosition(
				-title.getWidth() / 2,
				maxHeight / 2 - (40 + title.getHeight()));
		layer.addActor(title);
		
		levelProp = new Prop();
		levelProp.setTransform(true);
		CMSprite levelSprite = new CMSprite(scene.textureRegionByName(PuzzleMode.kLevelTextureNames[levelIndex]));
		levelSprite.centerContent();
		levelProp.addSpriteChild(levelSprite);
		levelProp.setSize(levelSprite.getWidth(), levelSprite.getHeight());
		levelProp.setScale(kLevelPropHeight / levelProp.getHeight());
		levelProp.setY(title.getY() - (levelProp.getScaledHeight() / 2 + 10));
		layer.addActorBefore(container, levelProp);
		
		levelHeader = TextUtils.createFX(
				PuzzleMode.kLevelNames[this.levelIndex],
				72,
				TextUtils.kAlignCenter,
				PuzzleMode.kLevelColors[this.levelIndex]);
		levelHeader.setName(ILocalizable.kNonLocalizableName);
		levelHeader.setPosition(
				-levelHeader.getWidth() / 2,
				levelProp.getY() - (levelProp.getScaledHeight() / 2 +
						TextUtils.getTextBoundsY(levelHeader, TextUtils.kAlignCenter) +
						levelHeader.getTextBounds().height + 20));
		container.addActor(levelHeader);
		
		if (levelHeader.getWidth() > maxWidth)
			container.setScale(maxWidth / levelHeader.getWidth());
		
		for (int i = 0, numStars = stars.length; i < numStars; i++) {
			Prop star = new Prop();
			star.setTransform(true);
			
			CMSprite starSprite = new CMSprite(scene.textureRegionByName("star-full"));
			starSprite.centerContent();
			star.addSpriteChild(starSprite);
			star.setSize(starSprite.getWidth(), starSprite.getHeight());
			
			star.setColor(PuzzleMode.kLevelColors[this.levelIndex]);
			stars[i] = star;
			container.addActor(star);
		}
		
		repositionStars();
		
		levelLabel = TextUtils.create("Level", 46, TextUtils.kAlignCenter,
				new Color(MenuBuilder.kMenuDarkYellow));
		layer.addActor(levelLabel);
		
		completedLabel = TextUtils.create("Completed", 46, TextUtils.kAlignCenter,
				new Color(MenuBuilder.kMenuDarkYellow));
		layer.addActor(completedLabel);
		
		repositionLabels();
	}
	
	private void repositionLabels() {
		if (levelHeader != null) {
			if (levelLabel != null) {
				levelLabel.setPosition(
					-levelLabel.getWidth() / 2,
					levelHeader.getY() + LangFX.getLevelCompletedLabelOffset()[0]
							- 0.7f * levelLabel.getHeight());
				
				if (completedLabel != null) {
					completedLabel.setPosition(
							-completedLabel.getWidth() / 2,
							levelLabel.getY() + LangFX.getLevelCompletedLabelOffset()[1]
									- 0.7f * levelLabel.getHeight());
				}
			}
		}
	}
	
	private void repositionStars() {
		float starOffsetY = levelHeader.getY() + TextUtils.getTextBoundsY(levelHeader, TextUtils.kAlignCenter) +
				levelHeader.getTextBounds().height + LangFX.getCustomDialogStarYOffset();
		for (int i = 0, numStars = stars.length; i < numStars; i++) {
			Prop star = stars[i];
			
			if (i == 0) { // Left
				star.setPosition(
						levelHeader.getX() + TextUtils.getTextBoundsX(levelHeader, TextUtils.kAlignCenter) +
						LangFX.getLevelCompletedStarXOffsets()[levelIndex][0],
						starOffsetY);
			} else { // Right
				star.setPosition(
						levelHeader.getX() + TextUtils.getTextBoundsX(levelHeader, TextUtils.kAlignCenter) +
							levelHeader.getTextBounds().width +
							LangFX.getLevelCompletedStarXOffsets()[levelIndex][1],
						starOffsetY);
			}
			
			if (container.getScaleX() != 0)
				star.setScale(1f / container.getScaleX());
		}
	}
	
	@Override
	public void localeDidChange(String fontKey, String FXFontKey) {
		super.localeDidChange(fontKey, FXFontKey);
		
		container.setScale(1f);
		
		TextUtils.swapFont(FXFontKey, levelHeader, true);
		if (levelHeader.getWidth() > maxWidth)
			container.setScale(maxWidth / levelHeader.getWidth());
		
		repositionLabels();
		repositionStars();
	}
	
	@Override
	public void advanceTime(float dt) {
		super.advanceTime(dt);
		
		for (int i = 0, numStars = stars.length; i < numStars; i++)
			stars[i].setRotation(stars[i].getRotation() - ((i & 1) == 1 ? -30 : 30) * dt);
	}
}
