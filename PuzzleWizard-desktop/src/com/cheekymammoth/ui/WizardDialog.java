package com.cheekymammoth.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.LongArray;
import com.cheekymammoth.actions.EventActions;
import com.cheekymammoth.animations.ParticleProxy;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.gameModes.PuzzleMode;
import com.cheekymammoth.graphics.CMSprite;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.sceneControllers.GameController;
import com.cheekymammoth.ui.INavigable.NavigationMap;

public class WizardDialog extends MenuDialog implements Disposable {
	private static final int kNumStars = 8;
	private static final int kRgbMax = 255;
	private static final int kNumFireworks = 3;
	private static final float kStarEllipseRadiusX = 248f;
	private static final float kStarEllipseRadiusY = 300f;
	private static final int EV_TYPE_CROWD_SHOULD_CHEER;
	private static final int EV_TYPE_FIREWORKS_SHOULD_LAUNCH;
	
	static {
		EV_TYPE_CROWD_SHOULD_CHEER = EventDispatcher.nextEvType();
		EV_TYPE_FIREWORKS_SHOULD_LAUNCH = EventDispatcher.nextEvType();
	}
	
	private boolean rndToggle = false;
	private int[] rgbIncrements = new int[] { 1, 1, 1 };
	private int[] rgb = new int[] { 0, 128, 255 };
	private float starAngle;
	private float starsY;
	private Prop wizardProp;
	private Prop[] stars = new Prop[kNumStars];
	private ParticleProxy confetti;
    private ParticleProxy[] fireworks = new ParticleProxy[kNumFireworks];
    private LongArray fireworksSoundIds = new LongArray(kNumFireworks);

	public WizardDialog() {
		this(-1, 0, 0, 0);
	}

	public WizardDialog(int category, int priority, int inputFocus, float maxHeight) {
		super(category, priority, inputFocus, NavigationMap.NAV_VERT);
		
		Prop layer = layerAtIndex(kContentLayerIndex);
		
		Label title = TextUtils.create("Congratulations!", 64, TextUtils.kAlignCenter, new Color(MenuBuilder.kMenuDarkYellow));
		title.setPosition(
				-title.getWidth() / 2,
				maxHeight / 2 - (40 + title.getHeight()));
		layer.addActor(title);
		
		wizardProp = new Prop();
		wizardProp.setTransform(true);
		CMSprite wizardSprite = new CMSprite(scene.textureRegionByName(
				PuzzleMode.kLevelTextureNames[PuzzleMode.getNumLevels()-1]));
		wizardSprite.centerContent();
		wizardProp.addSpriteChild(wizardSprite);
		wizardProp.setSize(wizardSprite.getWidth(), wizardSprite.getHeight());
		wizardProp.setScale(1.6f);
		wizardProp.setY(title.getY() - 0.65f * wizardProp.getScaledHeight());
		layer.addActor(wizardProp);
		
		float angle = 0;
		starsY = wizardProp.getY() + 24;
		for (int i = 0; i < kNumStars; i++, angle += (float)Math.PI / 4f) {
			Prop star = new Prop();
			star.setTransform(true);
			
			CMSprite starSprite = new CMSprite(scene.textureRegionByName("star-full"));
			starSprite.centerContent();
			star.addSpriteChild(starSprite);
			star.setSize(starSprite.getWidth(), starSprite.getHeight());
			star.setPosition(
					wizardProp.getX() + (float)Math.cos(angle) * kStarEllipseRadiusX,
					starsY + (float)Math.sin(angle) * kStarEllipseRadiusY);
			Color color = star.getColor();
			color.r = rgb[i % 3] / (float)kRgbMax;
			color.g = rgb[(i + 1) % 3] / (float)kRgbMax;
			color.b = rgb[(i + 2) % 3] / (float)kRgbMax;
			star.setColor(color);
			stars[i] = star;
			
			if ((i & 1) == 1)
				layer.addActor(star);
			else
				layer.addActorBefore(wizardProp, star);
		}
		
		Label midLabel = TextUtils.create(
				"Puzzle Wizard",
				60,
				TextUtils.kAlignCenter,
				new Color(MenuBuilder.kMenuDarkYellow));
		midLabel.setPosition(
				-midLabel.getWidth() / 2,
				wizardProp.getY() - (wizardProp.getScaledHeight() / 2 + midLabel.getHeight() + 50));
		layer.addActor(midLabel);
		
		Label lowerLabel = TextUtils.create(
				"Master of Color",
				42,
				TextUtils.kAlignCenter,
				new Color(MenuBuilder.kMenuDarkYellow));
		lowerLabel.setPosition(
				-lowerLabel.getWidth() / 2,
				midLabel.getY() - 0.8f * midLabel.getHeight());
		layer.addActor(lowerLabel);
		
		// Applause
		addAction(EventActions.eventAction(EV_TYPE_CROWD_SHOULD_CHEER, null, this, 1.5f));
		
		// Confetti
		ParticleEffect confettiEffect = new ParticleEffect();
		confettiEffect.load(Gdx.files.internal("art/particles/confetti.p"), Gdx.files.internal("art/particles"));
		confetti = new ParticleProxy(confettiEffect, this);
		refreshConfettiSettings();
		
		// Fireworks
		for (int i = 0; i < kNumFireworks; ++i) {
			ParticleEffect fireworkEffect = new ParticleEffect();
			fireworkEffect.load(
					Gdx.files.internal("art/particles/fireworks.p"),
					Gdx.files.internal("art/particles"));
			fireworks[i] = new ParticleProxy(fireworkEffect, this);
			fireworks[i].setDelay(1000f); // Pauses effect until below action invokes it.
			randomizeFireworkPosition(fireworks[i]);
			addAction(EventActions.eventAction(
					EV_TYPE_FIREWORKS_SHOULD_LAUNCH,
					fireworks[i],
					this,
					2f + i * 1f));
		}
		
		GameController.GC().enableFrameRateClampOnNextFrame(true);
	}
	
	@Override
	public void dispose() {
		for (int i = 0, n = fireworksSoundIds.size; i < n; i++)
			scene.stopSound("fireworks", fireworksSoundIds.get(i));
		fireworksSoundIds.clear();
	}
	
	private void refreshConfettiSettings() {
		if (confetti != null) {
			confetti.setPosition(scene.VW2(), 0.55f * scene.VH() + scene.getStage().getHeight() / 2);
			Array<ParticleEmitter> emitters = confetti.getParticleEffect().getEmitters();
			for (int i = 0, n = emitters.size; i < n; i++)
				emitters.get(i).getXOffsetValue().setLow(
						-scene.getStage().getWidth() / 2,
						scene.getStage().getWidth() / 2);
		}
	}
	
	private void randomizeFireworkPosition(ParticleProxy firework) {
		float viewWidth = scene.VW(), viewHeight = scene.VH();
		Rectangle rndRegion = new Rectangle();
		if (rndToggle)
			rndRegion.set(
					0.15f * viewWidth,
					0.5f * viewHeight,
					0.15f * viewWidth,
					0.35f * viewHeight);
		else
			rndRegion.set(
					0.7f * viewWidth,
					0.5f * viewHeight,
					0.15f * viewWidth,
					0.35f * viewHeight);
		rndToggle = !rndToggle;
		
		firework.setPosition(
				MathUtils.random(rndRegion.x, rndRegion.x + rndRegion.width),
				MathUtils.random(rndRegion.y, rndRegion.y + rndRegion.height));
		
	}
	
	@Override
	public void resolutionDidChange(int width, int height) {
		super.resolutionDidChange(width, height);
		refreshConfettiSettings();
	}
	
	@Override
	public void onEvent(int evType, Object evData) {
		super.onEvent(evType, evData);
		
		if (evType == EV_TYPE_FIREWORKS_SHOULD_LAUNCH && evData != null) {
			ParticleProxy firework = (ParticleProxy)evData;
			firework.reset();
			firework.setDelay(0);
			randomizeFireworkPosition(firework);
			long soundId = scene.playSound("fireworks");
			fireworksSoundIds.insert(0, soundId);
			if (fireworksSoundIds.size > kNumFireworks)
				fireworksSoundIds.pop();
		} else if (evType == EV_TYPE_CROWD_SHOULD_CHEER) {
			scene.playSound("crowd-cheer");
		}
	}

	@Override
	public void advanceTime(float dt) {
		super.advanceTime(dt);
		
		for (int i = 0, n = rgb.length; i < n; i++) {
			rgb[i] = rgb[i] + rgbIncrements[i];
			if (rgb[i] < 0 || rgb[i] > kRgbMax) {
				rgbIncrements[i] *= -1;
				rgb[i] = Math.max(0, Math.min(rgb[i], kRgbMax));
			}
		}
		
		starAngle -= dt / 4f;
		float angle = starAngle;
		for (int i = 0; i < kNumStars; i++, angle += (float)Math.PI / 4f) {
			Prop star = stars[i];
			star.setPosition(
					wizardProp.getX() + (float)Math.cos(angle) * kStarEllipseRadiusX,
					starsY + (float)Math.sin(angle) * kStarEllipseRadiusY);
			star.setRotation(star.getRotation() + 30 * dt);
			Color color = star.getColor();
			color.r = rgb[i % 3] / (float)kRgbMax;
			color.g = rgb[(i + 1) % 3] / (float)kRgbMax;
			color.b = rgb[(i + 2) % 3] / (float)kRgbMax;
			star.setColor(color);
		}
		
		confetti.update(dt);
		
		for (int i = 0; i < kNumFireworks; ++i) {
			if (fireworks[i].isComplete())
				continue;
			
			fireworks[i].update(dt);
			if (fireworks[i].isComplete()) {
				addAction(EventActions.eventAction(
						EV_TYPE_FIREWORKS_SHOULD_LAUNCH,
						fireworks[i],
						this,
						1.5f + MathUtils.random(0, 10) * 0.05f));
			}
		}
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
		
		confetti.getParticleEffect().draw(batch);
		for (int i = 0; i < kNumFireworks; ++i)
		{
			if (!fireworks[i].isComplete())
				fireworks[i].getParticleEffect().draw(batch);
		}
	}
}
