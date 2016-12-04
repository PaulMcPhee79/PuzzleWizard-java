package com.cheekymammoth.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.cheekymammoth.gameModes.PuzzleMode;
import com.cheekymammoth.graphics.CMSprite;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.puzzleui.LevelIcon;
import com.cheekymammoth.ui.INavigable.NavigationMap;
import com.cheekymammoth.utils.FloatTweener;
import com.cheekymammoth.utils.LangFX;
import com.cheekymammoth.utils.Transitions;

public class WizardUnlockedDialog extends MenuDialog {
	private static final float kSolvedKeyTweenDuration = 0.65f;
	private static final float kSolvedKeyTweenDelay = 0.5f;
	private static int kWizardKeyCount = 66;
	private static final int kTagX = 1000;
	private static final int kTagY = 2000;
	private static final int kTagRotation = 3000;
	private static final int kTagScale = 4000;
	private static final int kTagLockDrop = 5000;
	
	private int solvedCount;
	private int dropLockFrameCountdown;
	private Prop levelLock;
	private Prop levelUnlocked;
	private Prop totalSolvedKey;
	private Label totalSolvedLabel;
	private LevelIcon levelIcon;
	private Prop[] solvedKeys = new Prop[kWizardKeyCount];
	private FloatTweener[] tweenersX = new FloatTweener[kWizardKeyCount];
	private FloatTweener[] tweenersY = new FloatTweener[kWizardKeyCount];
	private FloatTweener[] tweenersRotation = new FloatTweener[kWizardKeyCount];
	private FloatTweener[] tweenersScale = new FloatTweener[kWizardKeyCount];
	private FloatTweener tweenerLockY;
	
	public WizardUnlockedDialog() {
		this(-1, 0, 0);
	}

	public WizardUnlockedDialog(int category, int priority, int inputFocus) {
		super(category, priority, inputFocus, NavigationMap.NAV_VERT);
	}

	@Override
	public void show(boolean animate) {
		initLayout();
		super.show(animate);
	}
	
	private void initLayout() {
		if (levelIcon != null) return;
		
		Prop layer = layerAtIndex(kContentLayerIndex);
		
		levelIcon = new LevelIcon(getCategory(), PuzzleMode.getNumLevels()-1, -1);
		levelIcon.setPosition(0, 48);
		levelIcon.setLocked(false);
		layer.addActor(levelIcon);
		
		levelLock = new Prop();
		CMSprite sprite = new CMSprite(scene.textureRegionByName("level-lock"));
		sprite.centerContent();
		levelLock.addSpriteChild(sprite);
		levelLock.setSize(sprite.getWidth(), sprite.getHeight());
		
		Vector2 levelIconPos = layer.stageToLocalCoordinates(levelIcon.getVisibleWorldCenter());
		Rectangle levelIconBounds = levelIcon.getVisibleBounds();
		levelLock.setPosition(
				levelIconPos.x,
				levelIconPos.y - (levelIconBounds.height - levelLock.getHeight()) / 2);
		layer.addActor(levelLock);
		
		levelUnlocked = new Prop();
		sprite = new CMSprite(scene.textureRegionByName("level-unlocked"));
		sprite.centerContent();
		levelUnlocked.addSpriteChild(sprite);
		levelUnlocked.setSize(sprite.getWidth(), sprite.getHeight());
		levelUnlocked.setPosition(
				levelLock.getX(),
				levelLock.getY() + (levelUnlocked.getHeight() - levelLock.getHeight()) / 2);
		levelUnlocked.setVisible(false);
		layer.addActor(levelUnlocked);
		
		totalSolvedLabel = TextUtils.create(
				"" + kWizardKeyCount + "/" + kWizardKeyCount,
				48,
				TextUtils.kAlignCenter | TextUtils.kAlignRight,
				250,
				TextUtils.getCapHeight(48));
		totalSolvedLabel.setPosition(
				levelIconPos.x - totalSolvedLabel.getWidth() / 2,
				levelIconPos.y - (levelIconBounds.height / 2 + totalSolvedLabel.getHeight() + 30));
		totalSolvedLabel.setText("" + solvedCount + "/" + kWizardKeyCount);
		layer.addActor(totalSolvedLabel);
		
		totalSolvedKey = new Prop();
		sprite = new CMSprite(scene.textureRegionByName("menu-key"));
		sprite.centerContent();
		totalSolvedKey.addSpriteChild(sprite);
		totalSolvedKey.setSize(sprite.getWidth(), sprite.getHeight());
		repositionTotalSolvedKey();
		layer.addActor(totalSolvedKey);
		
		float solvedKeyScale = 0.7f;
		float solvedKeyX_0 = levelIconPos.x - (0.6f * levelIconBounds.width + 2.5f * totalSolvedKey.getWidth());
		float solvedKeyX_1 = levelIconPos.x + (0.6f * levelIconBounds.width + 0.5f * totalSolvedKey.getWidth());
		float solvedKeyX = solvedKeyX_0;
		float solvedKeyY = levelIconPos.y + levelIconBounds.height / 2 +
				solvedKeyScale * totalSolvedKey.getHeight() - 24;

		TextureRegion keyTexRegion = scene.textureRegionByName("menu-key");
		for (int i = 0; i < kWizardKeyCount; i++) {
			if (i < kWizardKeyCount / 2) {
				if ((i <= 28 && i % 4 == 0) || i == 31) {
					solvedKeyX = solvedKeyX_0;
					solvedKeyY = solvedKeyY - solvedKeyScale * totalSolvedKey.getHeight();
				}
			} else if (i == kWizardKeyCount / 2) {
				solvedKeyX = solvedKeyX_1;
				solvedKeyY = levelIconPos.y + levelIconBounds.height / 2 - 24;
			} else {
				int iMod = i % (kWizardKeyCount / 2);
				if (iMod < 28 && iMod % 4 == 0) {
					solvedKeyX = solvedKeyX_1;
					solvedKeyY = solvedKeyY - solvedKeyScale * totalSolvedKey.getHeight();
				} else if (iMod == 28) {
					solvedKeyX = solvedKeyX_1 + solvedKeyScale * totalSolvedKey.getWidth();
					solvedKeyY = solvedKeyY - solvedKeyScale * totalSolvedKey.getHeight();
				} else if (iMod == 31) {
					solvedKeyX = solvedKeyX_1 + 2 * solvedKeyScale * totalSolvedKey.getWidth();
					solvedKeyY = solvedKeyY - solvedKeyScale * totalSolvedKey.getHeight();
				}
			}
			
			Prop keyProp = new Prop();
			keyProp.setTransform(true);
			CMSprite keySprite = new CMSprite(keyTexRegion);
			keySprite.centerContent();
			keyProp.addSpriteChild(keySprite);
			keyProp.setPosition(solvedKeyX, solvedKeyY);
			keyProp.setSize(keySprite.getWidth(), keySprite.getHeight());
			keyProp.setScale(solvedKeyScale);
			solvedKeys[i] = keyProp;
			layer.addActor(keyProp);
			
			FloatTweener tweenerX = new FloatTweener(0, Transitions.linear, this);
			tweenerX.setTag(kTagX + i);
			tweenerX.resetTween(
					keyProp.getX(),
					totalSolvedKey.getX(),
					kSolvedKeyTweenDuration,
					(0.25f * i + 2) * kSolvedKeyTweenDelay);
			tweenersX[i] = tweenerX;
			
			FloatTweener tweenerY = new FloatTweener(0, Transitions.linear, this);
			tweenerY.setTag(kTagY + i);
			tweenerY.resetTween(
					keyProp.getY(),
					totalSolvedKey.getY(),
					kSolvedKeyTweenDuration,
					(0.25f * i + 2) * kSolvedKeyTweenDelay);
			tweenersY[i] = tweenerY;
			
			FloatTweener tweenerRotation = new FloatTweener(0, Transitions.linear, this);
			tweenerRotation.setTag(kTagRotation + i);
			tweenerRotation.resetTween(
					keyProp.getRotation(),
					levelIcon.getRotation() - 360,
					kSolvedKeyTweenDuration,
					(0.25f * i + 2) * kSolvedKeyTweenDelay);
			tweenersRotation[i] = tweenerRotation;
			
			FloatTweener tweenerScale = new FloatTweener(0, Transitions.linear, this);
			tweenerScale.setTag(kTagScale + i);
			tweenerScale.resetTween(
					keyProp.getScaleX(),
					totalSolvedKey.getScaleX(),
					kSolvedKeyTweenDuration,
					(0.25f * i + 2) * kSolvedKeyTweenDelay);
			tweenersScale[i] = tweenerScale;
			
			solvedKeyX += solvedKeyScale * totalSolvedKey.getWidth();
		}
	}
	
	private void repositionTotalSolvedKey() {
		if (totalSolvedKey != null && totalSolvedLabel != null)
			totalSolvedKey.setPosition(
					totalSolvedLabel.getX() + totalSolvedLabel.getWidth()
					+ 0.65f * totalSolvedKey.getWidth(),
					totalSolvedLabel.getY() + totalSolvedLabel.getHeight() / 2
					+ LangFX.getCustomDialogKeyYOffset());
	}
	
	private void dropLockOverTime(float duration, float delay) {
		if (levelLock == null || levelUnlocked == null)
			return;
		
		if (tweenerLockY == null) {
			tweenerLockY = new FloatTweener(0, Transitions.easeIn, this);
			tweenerLockY.setTag(kTagLockDrop);
		}
		
		tweenerLockY.resetTween(levelUnlocked.getY(), levelUnlocked.getY() - 0.7f * scene.VH(), duration, delay);
		levelLock.setVisible(false);
		levelUnlocked.setVisible(true);
		scene.playSound("unlocked");
	}
	
	@Override
	public void localeDidChange(String fontKey, String FXFontKey) {
		super.localeDidChange(fontKey, FXFontKey);
		repositionTotalSolvedKey();
	}
	
	@Override
	public void onEvent(int evType, Object evData) {
		super.onEvent(evType, evData);
		
		if (evType == FloatTweener.EV_TYPE_FLOAT_TWEENER_CHANGED) {
			FloatTweener tweener = (FloatTweener)evData;
			if (tweener != null) {
				int tag = tweener.getTag();
				float tweenedValue = tweener.getTweenedValue();
				
				if (tag >= kTagLockDrop) {
					if (levelUnlocked != null)
						levelUnlocked.setY(tweenedValue);
				} else if (tag >= kTagScale) {
					int index = tag - kTagScale;
					if (index >= 0 && index < solvedKeys.length)
						solvedKeys[index].setScale(tweenedValue);
				} else if (tag >= kTagRotation) {
					int index = tag - kTagRotation;
					if (index >= 0 && index < solvedKeys.length)
						solvedKeys[index].setRotation(tweenedValue);
				} else if (tag >= kTagY) {
					int index = tag - kTagY;
					solvedKeys[index].setY(tweenedValue);
				} else if (tag >= kTagX) {
					int index = tag - kTagX;
					solvedKeys[index].setX(tweenedValue);
				}
			}
		} else if (evType == FloatTweener.EV_TYPE_FLOAT_TWEENER_COMPLETED) {
			FloatTweener tweener = (FloatTweener)evData;
			if (tweener != null) {
				int tag = tweener.getTag();
			
				if (tag == kTagLockDrop) {
					if (levelUnlocked != null)
						levelUnlocked.setVisible(false);
				} else if (tag >= kTagScale) {
					if (solvedCount < solvedKeys.length) {
						solvedKeys[solvedCount].setVisible(false);
						
						++solvedCount;
						totalSolvedLabel.setText("" + solvedCount + "/" + kWizardKeyCount);
						
						if (solvedCount == kWizardKeyCount) {
							Color color = totalSolvedLabel.getColor();
							color.set(0x22ff3eff);
							totalSolvedLabel.setColor(color);
							scene.playSound("solved");
							dropLockFrameCountdown = 60;
						} else
							scene.playSound("solved-short");
					}
				}
			}
		}
	}
	
	@Override
	public void advanceTime(float dt) {
		super.advanceTime(dt);
		
		for (int i = 0, n = tweenersX.length; i < n; i++)
			tweenersX[i].advanceTime(dt);
		
		for (int i = 0, n = tweenersY.length; i < n; i++)
			tweenersY[i].advanceTime(dt);
		
		for (int i = 0, n = tweenersRotation.length; i < n; i++)
			tweenersRotation[i].advanceTime(dt);
		
		for (int i = 0, n = tweenersScale.length; i < n; i++)
			tweenersScale[i].advanceTime(dt);
		
		if (tweenerLockY != null)
			tweenerLockY.advanceTime(dt);

		if (dropLockFrameCountdown > 0) {
			--dropLockFrameCountdown;
			if (dropLockFrameCountdown == 0)
				dropLockOverTime(0.75f, 0.5f);
		}
	}
}
