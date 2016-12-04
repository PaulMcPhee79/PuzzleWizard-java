package com.cheekymammoth.sceneViews;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Keys;
import com.cheekymammoth.actions.EventActions;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.graphics.CMSprite;
import com.cheekymammoth.graphics.GfxMode.AlphaMode;
import com.cheekymammoth.graphics.FullscreenQuad;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.input.CMInputs;
import com.cheekymammoth.input.ControlsManager;
import com.cheekymammoth.input.IInteractable;
import com.cheekymammoth.locale.Localizer.LocaleType;
import com.cheekymammoth.puzzles.Level;
import com.cheekymammoth.puzzleui.LevelMenu;
import com.cheekymammoth.resolution.IResDependent;
import com.cheekymammoth.sceneControllers.GameController;
import com.cheekymammoth.sceneControllers.MenuController;
import com.cheekymammoth.sceneControllers.MenuController.MenuState;
import com.cheekymammoth.ui.MenuSubview;
import com.cheekymammoth.ui.ProgressBar;
import com.cheekymammoth.utils.FloatTweener;
import com.cheekymammoth.utils.LangFX;
import com.cheekymammoth.utils.Transitions;
import com.cheekymammoth.utils.Utils;

public class MenuView extends Prop implements IEventListener, IInteractable, IResDependent {
	// Title subview tags
	public static final int EV_TYPE_TITLE_SUBVIEW_DID_FADE;
	private static final int kTitleSubviewTag = Utils.getUniqueKey();
	private static final int kTitleTweenerIndexFadeIn = 0;
	private static final int kTitleTweenerIndexLogo = 1;
	private static final int kTitleTweenerIndexText = 2;
	private static final int kTitleTweenerIndexProgress = 3;
	
	private static final float kTitleTextPosYDivisor = 40f;
	
	private static final String kTitleBgQuadName = "Title_bgQuad";
	private static final String kTitleProgressBarName = "Title_progressBar";
	
	static {
		EV_TYPE_TITLE_SUBVIEW_DID_FADE = EventDispatcher.nextEvType();
	}
	
	private LevelMenu levelMenu;
	private FloatTweener[] titleTweeners;
	private MenuController controller;
	private ObjectMap<String, MenuSubview> subviews = new ObjectMap<String, MenuSubview>(10);
	private Array<MenuSubview> subviewStack = new Array<MenuSubview>(true, 10, MenuSubview.class);
	
	public MenuView(int category, MenuController controller) {
		super(category);

		this.controller = controller;
		subviews.put("Title", createTitleSubview());
		subviews.put("LevelMenu", new MenuSubview());
	}
	
	private MenuSubview createTitleSubview() {
		MenuSubview titleSubview = subviewForKey("Title");
		if (titleSubview != null)
			return titleSubview;
		
		titleSubview = new MenuSubview();
		titleSubview.setTag(kTitleSubviewTag);
		titleSubview.setColor(Utils.setA(titleSubview.getColor(), 0));
		//titleSubview.setPosition(scene.VW2(), scene.VH2());
		
		titleTweeners = new FloatTweener[4];
		
		final float kLogoScaleFrom = 0.95f, kLogoScaleTo = 1f;
		{
			final float kDuration = 2.0f, kDelay = 0.1f;
			// Fades in subview
			FloatTweener tweener = new FloatTweener(0, Transitions.linear, this);
			tweener.resetTween(titleSubview.getColor().a, 1f, 0.4f * kDuration, kDelay);
			tweener.setTag(kTitleTweenerIndexFadeIn);
			titleTweeners[tweener.getTag()] = tweener;
			
			// Animates logo
			tweener = new FloatTweener(0, Transitions.easeOut, this);
			tweener.resetTween(kLogoScaleFrom, kLogoScaleTo, kDuration, kDelay);
			tweener.setTag(kTitleTweenerIndexLogo);
			titleTweeners[tweener.getTag()] = tweener;
			
			// Animates text
			tweener = new FloatTweener(0, Transitions.easeOut, this);
			tweener.resetTween(0, 1f, kDuration, kDelay);
			tweener.setTag(kTitleTweenerIndexText);
			titleTweeners[tweener.getTag()] = tweener;
			
			final float kProgressTweenDuration = 0.75f;
			tweener = new FloatTweener(0, Transitions.linear, this);
			tweener.resetTween(
					0,
					1f,
					kProgressTweenDuration,
					kDelay + kDuration - kProgressTweenDuration);
			tweener.setTag(kTitleTweenerIndexProgress);
			titleTweeners[tweener.getTag()] = tweener;
		}
		
		{
			FullscreenQuad titleBgQuad = new FullscreenQuad(Color.BLACK);
			titleBgQuad.setName(kTitleBgQuadName);
			titleSubview.addActor(titleBgQuad);
		}
		
		{
			Prop logoProp = new Prop();
			logoProp.setName("LogoProp");
			logoProp.setTransform(true);
			logoProp.setScale(kLogoScaleFrom);
			
			titleSubview.addActor(logoProp);
	
			CMSprite logoSprite = new CMSprite(scene.textureByName("pw-logo-bg.png"));
			logoSprite.setAlphaMode(AlphaMode.POST_MULTIPLIED);
			logoProp.addSpriteChild(logoSprite);
			
			logoProp.setSize(logoSprite.getWidth(), logoSprite.getHeight());
			logoProp.setOrigin(logoProp.getWidth() / 2, logoProp.getHeight() / 2);
			logoProp.setPosition(
					LangFX.getSplashLogoOffset()[0] + scene.VW2() - logoProp.getWidth() / 2,
					LangFX.getSplashLogoOffset()[1] + scene.VH2() - logoProp.getHeight() / 2);
		}
		
		{
			Prop textProp = new Prop();
			textProp.setName("TextProp");
			textProp.setColor(Utils.setA(textProp.getColor(), 0));
			textProp.setY(-scene.VH() / kTitleTextPosYDivisor);
			titleSubview.addActor(textProp);
			
			LocaleType locale = scene.getLocale();
			String puzzleString = LangFX.locale2PuzzleTexSuffix(locale);
			String wizardString = LangFX.locale2WizardTexSuffix(locale);
			String iqString = LangFX.locale2IQString(scene.getLocale(), false);
			
			if (puzzleString != null) {
				CMSprite puzzleSprite = new CMSprite(scene.textureByName(
						"lang/pw-logo-puzzle-" + puzzleString + ".png"));
				puzzleSprite.setAlphaMode(AlphaMode.POST_MULTIPLIED);
				puzzleSprite.setPosition(
						LangFX.getSplashTextPuzzleOffset()[0] + LangFX.getSplashLogoOffset()[0] + scene.VW2() - puzzleSprite.getWidth() / 2,
						LangFX.getSplashTextPuzzleOffset()[1] + scene.VH2() - puzzleSprite.getHeight() / 2);
				textProp.addSpriteChild(puzzleSprite);
			}
		
			if (wizardString != null) {
				CMSprite wizardSprite = new CMSprite(scene.textureByName(
						"lang/pw-logo-wizard-" + wizardString + ".png"));
				wizardSprite.setAlphaMode(AlphaMode.POST_MULTIPLIED);
				wizardSprite.setPosition(
						LangFX.getSplashTextWizardOffset()[0] + scene.VW2() - wizardSprite.getWidth() / 2,
						LangFX.getSplashTextWizardOffset()[1] + scene.VH2() - wizardSprite.getHeight() / 2);
				textProp.addSpriteChild(wizardSprite);
			}
			
			CMSprite iqSprite = new CMSprite(scene.textureByName("lang/pw-logo-" + iqString + ".png"));
			
			iqSprite.setAlphaMode(AlphaMode.POST_MULTIPLIED);
			iqSprite.setPosition(
					LangFX.getSplashTextIqOffset()[0] + scene.VW2() - iqSprite.getWidth() / 2,
					LangFX.getSplashTextIqOffset()[1] + scene.VH2() - iqSprite.getHeight() / 2);
			textProp.addSpriteChild(iqSprite);
		}
		
		{
			ProgressBar progressBar = new ProgressBar(0.75f * scene.VW(), 6);
			progressBar.setName(kTitleProgressBarName);
	        progressBar.setPosition((scene.VW() - progressBar.getWidth()) / 2, scene.VH() / 20);
	        //progressBar.setTrackColor(new Color(0x444444ff));
	        progressBar.setProgressColor(new Color(0xa041e6ff)); // 0xa041e6ff,0xaa55eeff,0xffe611ff
	        progressBar.setColor(Utils.setA(progressBar.getColor(), 0));
	        titleSubview.addActor(progressBar);
		}
		
		layoutTitleScreenBg(titleSubview, (int)scene.getStage().getWidth(), (int)scene.getStage().getHeight());
		return titleSubview;
	}
	
	private void layoutTitleScreenBg(MenuSubview subview, int width, int height) {
		if (subview == null)
			return;
		
		FullscreenQuad titleBgQuad = (FullscreenQuad)subview.findActor(kTitleBgQuadName);
		if (titleBgQuad != null)
			titleBgQuad.resolutionDidChange(width, height);
	}
	
	public void setSplashProgress(float percent) {
		MenuSubview subview = subviewForKey("Title");
		if (subview != null) {
			ProgressBar progressBar = (ProgressBar)subview.findActor(kTitleProgressBarName);
			if (progressBar != null)
				progressBar.setProgress(percent);
		}
	}
	
	public void fadeSplashProgressOverTime(float duration, float delay) {
		MenuSubview subview = subviewForKey("Title");
		if (subview != null) {
			ProgressBar progressBar = (ProgressBar)subview.findActor(kTitleProgressBarName);
			if (progressBar != null) {
				progressBar.addAction(Actions.sequence(
						Actions.delay(delay),
						Actions.fadeOut(duration)));
			}
				
		}
	}
	
	public void attachEventListeners() { }
	
	public void detachEventListeners() { }
	
	public LevelMenu getLevelMenu() { return levelMenu; }
	
	public MenuSubview getCurrentSubview() {
		return subviewStack.size > 0 ? subviewStack.get(subviewStack.size-1) : null;
	}
	
	public MenuSubview subviewForKey(String key) {
		return key != null ? subviews.get(key) : null;
	}
	
	public MenuSubview subviewForTag(int tag) {
		Keys<String> keys = subviews.keys();
		while (keys.hasNext) {
			String key = keys.next();
			MenuSubview subview = subviews.get(key);
			if (subview != null && subview.getTag() == tag)
				return subview;
		}
		
		return null;
	}
	
	private String keyForTag(int tag) {
		Keys<String> keys = subviews.keys();
		while (keys.hasNext) {
			String key = keys.next();
			MenuSubview subview = subviews.get(key);
			if (subview != null && subview.getTag() == tag)
				return key;
		}
		
		return null;
	}
	
	public void pushSubviewForKey(String key) {
		pushSubview(subviewForKey(key));
	}
	
	private void pushSubview(MenuSubview subview) {
		if (subview != null && !subviewStack.contains(subview, true)) {
			subview.setVisible(true);
			subviewStack.add(subview);
			addActor(subview);
		}
	}

	public void popSubview() {
		if (subviewStack.size > 0) {
			int subviewIndex = subviewStack.size-1;
			MenuSubview subview = subviewStack.get(subviewIndex);
			subview.setVisible(false);
			subview.remove();
			subviewStack.removeIndex(subviewIndex);
		}
	}
	
	public void popAllSubviews() {
		while (subviewStack.size > 1)
			popSubview();
	}
	
	public void destroySubviewForKey(String key) {
		if (key == null)
			return;
		
		MenuSubview subview = subviewForKey(key);
		if (subview != null) {
			assert(!subviewStack.contains(subview, true))
			: "MenuView: Attempt to destroy a subview (" + key + ") while it is still on the stack";
			subviews.remove(key);
			subview.remove();
		}
	}
	
	private void destroySubview(MenuSubview subview) {
		if (subview != null)
			destroySubviewForKey(keyForTag(subview.getTag()));
	}
	
	public void fadeSubviewOverTime(String key, float duration, float delay, boolean destroy) {
		if (key == null)
			return;
		
		MenuSubview subview = this.subviewForKey(key);
		if (subview != null) {
			subview.setShouldDestroy(destroy);
			subview.addAction(Actions.sequence(
					Actions.delay(delay),
					Actions.fadeOut(duration),
					EventActions.eventAction(EV_TYPE_TITLE_SUBVIEW_DID_FADE, this)));
		}
	}
	
	public void showLevelMenuOverTime(float duration) {
		if (levelMenu != null)
			levelMenu.showOverTime(duration);
	}
	
	public void hideLevelMenuOverTime(float duration) {
		if (levelMenu != null)
			levelMenu.hideOverTime(duration);
	}
	
	public void hideLevelMenuInstantaneously() {
		if (levelMenu != null)
			levelMenu.hideInstantaneously();
	}
	
	public void populateLevelMenuView(Array<Level> levels) {
		if (levelMenu != null)
			return;
		
		MenuSubview subview = subviewForKey("LevelMenu");
		levelMenu = new LevelMenu(getCategory(), levels);
		levelMenu.addEventListener(LevelMenu.EV_TYPE_DID_TRANSITION_IN, this);
		levelMenu.addEventListener(LevelMenu.EV_TYPE_DID_TRANSITION_OUT, this);
		levelMenu.addEventListener(LevelMenu.EV_TYPE_PUZZLE_SELECTED, this);
		subview.addActor(levelMenu);
		subview.addedToScene();
		scene.subscribeToInputUpdates(levelMenu);
	}
	
	public void unpopulateLevelMenuView() {
		if (levelMenu == null)
			return;
		
		levelMenu.removeEventListener(LevelMenu.EV_TYPE_DID_TRANSITION_IN, this);
		levelMenu.removeEventListener(LevelMenu.EV_TYPE_DID_TRANSITION_OUT, this);
		levelMenu.removeEventListener(LevelMenu.EV_TYPE_PUZZLE_SELECTED, this);
		scene.unsubscribeToInputUpdates(levelMenu);
		levelMenu.remove();
		levelMenu = null;
	}

	@Override
	public void resolutionDidChange(int width, int height) {
		MenuSubview subview = subviewForKey("Title");
		if (subview != null)
			layoutTitleScreenBg(subview, width, height);
	}

	@Override
	public int getInputFocus() {
		return CMInputs.HAS_FOCUS_MENU_ALL;
	}

	@Override
	public void didGainFocus() { }

	@Override
	public void willLoseFocus() { }
	
	@Override
	public void onEvent(int evType, Object evData) {
		if (evType == EV_TYPE_TITLE_SUBVIEW_DID_FADE) {
			MenuSubview subview = this.subviewForKey("Title");
			if (subview != null) {
				popSubview();
				destroySubview(subview);

				scene.getTM().unloadTexture("pw-logo-bg.png");
				if (!GameController.isTrialMode())
					scene.unloadTexturesForKey("SplashLocaleTextures", scene.getLocale());

				titleTweeners = null;
				dispatchEvent(evType, this);
			}
		} else if (evType == LevelMenu.EV_TYPE_PUZZLE_SELECTED) {
			controller.puzzleWasSelectedAtMenu(levelMenu.getSelectedPuzzleID());
		} else if (evType == LevelMenu.EV_TYPE_DID_TRANSITION_IN) {
			controller.onEvent(evType, evData);
		} else if (evType == LevelMenu.EV_TYPE_DID_TRANSITION_OUT) {
			controller.onEvent(evType, evData);
		} else if (evType == FloatTweener.EV_TYPE_FLOAT_TWEENER_CHANGED) {
			MenuSubview titleSubview = subviewForKey("Title");
			if (titleSubview != null && evData != null) {
				FloatTweener tweener = (FloatTweener)evData;
				
				switch (tweener.getTag()) {
					case kTitleTweenerIndexFadeIn:
					{
						float alpha = tweener.getTweenedValue();
						titleSubview.setColor(Utils.setA(titleSubview.getColor(), alpha));
					}
						break;
					case kTitleTweenerIndexLogo:
					{
						Prop logoProp = (Prop)titleSubview.findActor("LogoProp");
						logoProp.setScale(tweener.getTweenedValue());
					}
						break;
					case kTitleTweenerIndexText:
					{
						float fraction = tweener.getTweenedValue();
						Prop textProp = (Prop)titleSubview.findActor("TextProp");
						textProp.setColor(Utils.setA(textProp.getColor(), fraction));
						textProp.setY((1f - fraction) * -scene.VH() / kTitleTextPosYDivisor);
					}
						break;
					case kTitleTweenerIndexProgress:
					{
						float alpha = tweener.getTweenedValue();
						ProgressBar progressBar = (ProgressBar)titleSubview.findActor(
								kTitleProgressBarName);
						progressBar.setColor(Utils.setA(progressBar.getColor(), alpha));
					}
						break;
				}
			}
		} else if (evType == FloatTweener.EV_TYPE_FLOAT_TWEENER_COMPLETED) {
			FloatTweener tweener = (FloatTweener)evData;
			if (tweener != null && titleTweeners != null) {
				if (tweener.getTag() == kTitleTweenerIndexProgress)
					controller.viewDidFadeIn();
			}
		}
	}

	@Override
	public void update(CMInputs input) {
		if (controller.getState() == MenuState.MENU_IN || controller.getState() == MenuState.TRANSITION_IN) {
			if (input.didDepress(CMInputs.CI_MENU) ||
					ControlsManager.CM().didKeyDepress(Input.Keys.ESCAPE))
				scene.showEscDialog();
		}
	}

	@Override
	public void advanceTime(float dt) {
		if (controller == null)
			return;
		
		if (controller.getState() == MenuState.MENU_IN) {
			MenuSubview subview = getCurrentSubview();
			if (subview != null)
				subview.advanceTime(dt);
		}
		
		if (levelMenu != null && controller.getState() != MenuState.MENU_OUT)
			levelMenu.advanceTime(dt);
		
		if (titleTweeners != null) {
			for (int i = 0, iLimit = titleTweeners.length; i < iLimit; i++) {
				if (titleTweeners[i] != null)
					titleTweeners[i].advanceTime(dt);
			}
		}
	}
}
