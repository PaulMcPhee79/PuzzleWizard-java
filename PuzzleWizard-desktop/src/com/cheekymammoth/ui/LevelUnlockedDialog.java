package com.cheekymammoth.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.cheekymammoth.gameModes.PuzzleMode;
import com.cheekymammoth.graphics.CMSprite;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.puzzleui.LevelIcon;
import com.cheekymammoth.puzzleui.PuzzlePage;
import com.cheekymammoth.ui.INavigable.NavigationMap;
import com.cheekymammoth.utils.FloatTweener;
import com.cheekymammoth.utils.Transitions;

public final class LevelUnlockedDialog extends MenuDialog {
	private static final float kSolvedKeyTweenDuration = 0.65f;
	private static final float kSolvedKeyTweenDelay = 0.5f;
	private static final int kSolvedKeyCount = 3;
	private static final int kTagX = 1000;
	private static final int kTagY = 2000;
	private static final int kTagRotation = 3000;
	private static final int kTagScale = 4000;
	private static final int kTagArrowAlpha = 5000;
	private static final int kTagArrowX = 5001;
	private static final int kTagLockDrop = 6000;
	private static final int kTagGlow = 7000;
	
	private int solvedCount;
	private int solvedGlowDir = 1;
	private int levelIndex;
	private float maxWidth;
	private Prop levelArrow;
	private Prop levelLock;
	private Prop levelUnlocked;
	private Prop[] solvedKeys = new Prop[kSolvedKeyCount];
	private LevelIcon[] levelIcons = new LevelIcon[2];
	private FloatTweener[] tweenersX = new FloatTweener[kSolvedKeyCount];
	private FloatTweener[] tweenersY = new FloatTweener[kSolvedKeyCount];
	private FloatTweener[] tweenersRotation = new FloatTweener[kSolvedKeyCount];
	private FloatTweener[] tweenersScale = new FloatTweener[kSolvedKeyCount];
	private FloatTweener tweenerArrowAlpha;
	private FloatTweener tweenerArrowX;
	private FloatTweener tweenerLockY;
	private FloatTweener tweenerGlow;
	
	public LevelUnlockedDialog() {
		this(-1, 0, 0, 0, 0);
	}

	public LevelUnlockedDialog(int category, int priority, int inputFocus, int levelIndex, float maxWidth) {
		super(category, priority, inputFocus, NavigationMap.NAV_VERT);
		
		this.maxWidth = maxWidth;
		this.levelIndex = Math.max(0, Math.min(PuzzleMode.getNumLevels()-1, levelIndex));
	}
	
	@Override
	public void show(boolean animate) {
		initLayout();
		super.show(animate);
	}
	
	private void initLayout() {
		if (levelArrow != null) return;
		
		Prop layer = layerAtIndex(kContentLayerIndex);
		
		levelArrow = new Prop();
		levelArrow.setTransform(true);
		CMSprite sprite = new CMSprite(scene.textureRegionByName("level-arrow-next"));
		sprite.centerContent();
		levelArrow.addSpriteChild(sprite);
		levelArrow.setSize(sprite.getWidth(), sprite.getHeight());
		levelArrow.setY(-48);
		Color color = levelArrow.getColor();
		levelArrow.setColor(color.r, color.g, color.b, 0);
		levelArrow.setVisible(false);
		
		for (int i = 0; i < 2; i++) {
			LevelIcon levelIcon = new LevelIcon(getCategory(), levelIndex + (i-1), -1);
			levelIcon.setPosition(
					i == 0
					? levelArrow.getX() - 0.45f * (levelArrow.getWidth() + levelIcon.getWidth())
					: levelArrow.getX() + 0.45f * (levelArrow.getWidth() + levelIcon.getWidth()),
					levelArrow.getY());
			//color = levelIcon.getColor();
			//color.set(MenuBuilder.kMenuSlateBlue);
			//levelIcon.setPuzzlesSolvedColor(color);
			levelIcon.setLocked(false);
			if (i == 0)
				levelIcon.enabledSolvedGlow(true);
			levelIcons[i] = levelIcon;
			layer.addActor(levelIcon);
		}
		
		levelLock  = new Prop();
		sprite = new CMSprite(scene.textureRegionByName("level-lock"));
		sprite.centerContent();
		levelLock.addSpriteChild(sprite);
		levelLock.setSize(sprite.getWidth(), sprite.getHeight());
		
		Vector2 levelIconPos = layer.stageToLocalCoordinates(levelIcons[1].getVisibleWorldCenter());
		Rectangle levelIconBounds = levelIcons[1].getVisibleBounds();
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
		
		float solvedKeyX = 0, solvedKeyY = 0;
		Vector2 keyCenter = levelIcons[0].getSolvedKeyWorldCenter();
		keyCenter = layer.stageToLocalCoordinates(keyCenter);
		
		for (int i = 0; i < kSolvedKeyCount; i++) {
			Prop solvedKey = new Prop();
			solvedKey.setTransform(true);
			sprite = new CMSprite(scene.textureRegionByName("menu-key"));
			sprite.centerContent();
			solvedKey.addSpriteChild(sprite);
			solvedKey.setSize(sprite.getWidth(), sprite.getHeight());
			
			if (i == 0) {
				solvedKeyX = levelArrow.getX() - 1.25f * solvedKey.getWidth();
				solvedKeyY = levelIcons[0].getY() + levelIcons[0].getVisibleBounds().height / 2 + solvedKey.getHeight();
			} else
				solvedKeyX += 1.25f * solvedKey.getWidth();
			
			solvedKey.setPosition(solvedKeyX, solvedKeyY);
			solvedKeys[i] = solvedKey;
			layer.addActor(solvedKey);
			
			FloatTweener tweenerX = new FloatTweener(0, Transitions.linear, this);
			tweenerX.setTag(kTagX + i);
			tweenerX.resetTween(
					solvedKey.getX(),
					keyCenter.x, // levelIcons[0].getX() + 0.165f * levelIcons[0].getWidth(),
					kSolvedKeyTweenDuration,
					(i + 2) * kSolvedKeyTweenDelay);
			tweenersX[i] = tweenerX;
			
			FloatTweener tweenerY = new FloatTweener(0, Transitions.linear, this);
			tweenerY.setTag(kTagY + i);
			tweenerY.resetTween(
					solvedKey.getY(),
					keyCenter.y, //levelIcons[0].getY() - 0.2925f * levelIcons[0].getHeight(),
					kSolvedKeyTweenDuration,
					(i + 2) * kSolvedKeyTweenDelay);
			tweenersY[i] = tweenerY;
			
			FloatTweener tweenerRotation = new FloatTweener(0, Transitions.linear, this);
			tweenerRotation.setTag(kTagRotation + i);
			tweenerRotation.resetTween(
					solvedKey.getRotation(),
					levelIcons[0].getRotation() - 360,
					kSolvedKeyTweenDuration,
					(i + 2) * kSolvedKeyTweenDelay);
			tweenersRotation[i] = tweenerRotation;
			
			FloatTweener tweenerScale = new FloatTweener(0, Transitions.linear, this);
			tweenerScale.setTag(kTagScale + i);
			tweenerScale.resetTween(
					solvedKey.getScaleX(),
					0.7f * levelIcons[0].getScaleX(),
					kSolvedKeyTweenDuration,
					(i + 2) * kSolvedKeyTweenDelay);
			tweenersScale[i] = tweenerScale;
		}
		
		layer.addActor(levelArrow);
	}
	
	private void showArrowOverTime(float duration, float delay) {
		if (tweenerArrowX == null) {
			tweenerArrowX = new FloatTweener(0, Transitions.linear, this);
			tweenerArrowX.setTag(kTagArrowX);
		}
		
		if (tweenerArrowAlpha == null) {
			tweenerArrowAlpha = new FloatTweener(0, Transitions.linear, this);
			tweenerArrowAlpha.setTag(kTagArrowAlpha);
		}
		
		levelArrow.setX(-maxWidth / 2);
		levelArrow.setVisible(true);
		tweenerArrowX.resetTween(levelArrow.getX(), 0, duration, delay);
		tweenerArrowAlpha.resetTween(levelArrow.getColor().a, 1f, duration / 3, delay);
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
	public void onEvent(int evType, Object evData) {
		super.onEvent(evType, evData);
		
		if (evType == FloatTweener.EV_TYPE_FLOAT_TWEENER_CHANGED) {
			FloatTweener tweener = (FloatTweener)evData;
			if (tweener != null) {
				int tag = tweener.getTag();
				float tweenedValue = tweener.getTweenedValue();
				
				if (tag == kTagArrowAlpha) {
					Color color = levelArrow.getColor();
					levelArrow.setColor(color.r, color.g, color.b, tweenedValue);
				} else if (tag >= kTagGlow) {
					levelIcons[0].setSolvedGlowOpacity(tweenedValue);
				} else if (tag >= kTagLockDrop) {
					if (levelUnlocked != null)
						levelUnlocked.setY(tweenedValue);
				} else if (tag >= kTagArrowX) {
					if (levelArrow != null)
						levelArrow.setX(tweenedValue);
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
			
				if (tag == kTagGlow) {
					if (solvedGlowDir == 1) {
						solvedGlowDir = -1;
						tweener.reverse();
					}
				} else if (tag == kTagLockDrop) {
					if (levelUnlocked != null)
						levelUnlocked.setVisible(false);
				} else if (tag == kTagArrowX) {
					dropLockOverTime(0.75f, 0.5f);
				} else if (tag >= kTagScale && tag < kTagArrowAlpha) {
					if (solvedCount < solvedKeys.length) {
						solvedKeys[solvedCount].setVisible(false);
						
						if (tweenerGlow == null) {
							tweenerGlow = new FloatTweener(0, Transitions.linear, this);
							tweenerGlow.setTag(kTagGlow);
						}
						
						solvedGlowDir = 1;
						tweenerGlow.resetTween(levelIcons[0].getSolvedGlowOpacity(), 1f, 0.5f * kSolvedKeyTweenDelay, 0);
						
						if (++solvedCount == kSolvedKeyCount)
							showArrowOverTime(0.4f, 0.75f);
						
						levelIcons[0].setPuzzlesSolved(solvedCount, PuzzlePage.kNumPuzzlesPerPage);
						scene.playSound(solvedCount < kSolvedKeyCount ? "solved-short" : "solved");
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
		
		if (tweenerArrowAlpha != null)
			tweenerArrowAlpha.advanceTime(dt);
		if (tweenerArrowX != null)
			tweenerArrowX.advanceTime(dt);
		if (tweenerLockY != null)
			tweenerLockY.advanceTime(dt);
		if (tweenerGlow != null)
			tweenerGlow.advanceTime(dt);
	}

}
