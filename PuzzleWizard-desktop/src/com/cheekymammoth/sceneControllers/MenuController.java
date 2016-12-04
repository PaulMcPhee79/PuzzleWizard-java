package com.cheekymammoth.sceneControllers;

import com.badlogic.gdx.utils.Array;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.input.CMInputs;
import com.cheekymammoth.input.InputManager;
import com.cheekymammoth.puzzleio.GameProgressController;
import com.cheekymammoth.puzzles.Level;
import com.cheekymammoth.puzzleui.LevelMenu;
import com.cheekymammoth.sceneControllers.PlayfieldController.PfState;
import com.cheekymammoth.sceneControllers.SceneUtils.PFCat;
import com.cheekymammoth.sceneManagers.MenuDialogManager;
import com.cheekymammoth.sceneViews.MenuView;
import com.cheekymammoth.ui.MenuIndicator;
import com.cheekymammoth.ui.TextUtils.CMFontType;
import com.cheekymammoth.ui.MenuButton;
import com.cheekymammoth.ui.MenuItem;
import com.cheekymammoth.ui.TextMenuItem;
import com.cheekymammoth.ui.TextUtils;
import com.cheekymammoth.utils.LangFX;
import com.cheekymammoth.utils.Utils;

public class MenuController extends EventDispatcher implements IEventListener {
	public enum MenuState { NONE, MENU_TITLE, TRANSITION_IN, MENU_IN, TRANSITION_OUT, MENU_OUT };
	
	public static final int EV_TYPE_SPLASH_DID_SHOW;
	private static final int kButtonTagNextUnsolvedPuzzle;
	private static final int kButtonTagMainMenu;
	
	static {
		EV_TYPE_SPLASH_DID_SHOW = EventDispatcher.nextEvType();
		kButtonTagNextUnsolvedPuzzle = Utils.getUniqueKey();
		kButtonTagMainMenu = Utils.getUniqueKey();
	}
	
	private boolean buyNowDirtyFlag;
	private MenuState state = MenuState.NONE;
	private MenuView view;
	private MenuDialogManager dialogManager;
	private PlayfieldController scene;
	
	public MenuController(PlayfieldController scene) {
		this.scene = scene;
		view = new MenuView(PFCat.HUD.ordinal(), this);
		view.addEventListener(MenuView.EV_TYPE_TITLE_SUBVIEW_DID_FADE, this);
		view.pushSubviewForKey("LevelMenu");
		scene.addProp(view);
		scene.addEventListener(SceneController.EV_TYPE_SCENE_IS_READY, this);
		scene.registerResDependent(view);
		scene.subscribeToInputUpdates(view);
	}
	
	public void applyGameSettings() {
		if (view != null) {
			LevelMenu levelMenu = view.getLevelMenu();
			if (levelMenu != null)
				levelMenu.refresh();
		}
		
		if (dialogManager != null)
			dialogManager.applyGameSettings();
	}
	
	public MenuState getState() { return state; }
	
	public void setState(MenuState value) {
		if (value == state)
			return;
		
		MenuState previousSate = state;
		
		// Clean up previous state
		switch (previousSate) {
			case MENU_TITLE:
				if (dialogManager == null) {
					dialogManager = new MenuDialogManager(PFCat.HUD.ordinal());
					dialogManager.addMenuButton("NextUnsolvedPuzzle", createNextUnsolvedButton());
					dialogManager.addMenuButton("MainMenu", createMainMenuButton());
					dialogManager.addEventListener(MenuDialogManager.EV_TYPE_DID_CLOSE_LEVEL_COMPLETED_DIALOG, this);
					scene.subscribeToInputUpdates(dialogManager, true);
					scene.registerResDependent(dialogManager);
					scene.registerLocalizable(dialogManager);
					scene.addProp(dialogManager);
				}					
				break;
			case MENU_IN:
				scene.popFocusState(CMInputs.FOCUS_STATE_MENU);
			default:
				break;
		}
		
		// Apply new state
		state = value;
		
		switch (state) {
			case MENU_TITLE:
				view.pushSubviewForKey("Title");
				break;
			case TRANSITION_IN:
				view.setVisible(true);
				showLevelMenu();
				break;
			case MENU_IN:
				scene.pushFocusState(CMInputs.FOCUS_STATE_MENU);
				scene.enableMenuMode(true);
				
				if (buyNowDirtyFlag) {
					buyNowDirtyFlag = false;
					if (GameController.isTrialMode() &&
							GameProgressController.GPC().isTrialModeCompleted())
						scene.showBuyNowDialog();
				}
				break;
			case TRANSITION_OUT:
				scene.enableMenuMode(false);
				hideLevelMenu();
				break;
			case MENU_OUT:
				view.setVisible(false);
				break;
			default:
				break;
		}
	}
	
	private MenuItem createNextUnsolvedButton() {
		MenuItem button = new TextMenuItem(
				PFCat.HUD.ordinal(),
				"Next Unsolved Puzzle",
				56,
				TextUtils.kAlignCenter | TextUtils.kAlignLeft,
				CMFontType.FX);
		button.setEnabledColors(0xa7ff67ff, 0xffffffff);
		button.setSelectedColors(0xa7ff67ff, 0xffffffff);
		button.setPressedColors(0xa7ff67ff, 0xffffffff);
		button.setIndicatorScale(1.5f);
		button.setSelected(true);
		button.setTag(kButtonTagNextUnsolvedPuzzle);
		button.addEventListener(MenuButton.EV_TYPE_RAISED, this);
		
		MenuIndicator indicator = button.getIndicator();
		indicator.setOffset(0, LangFX.getMenuDialogButtonOffset());
		
		return button;
	}
	
	private MenuItem createMainMenuButton() {
		MenuItem button = new TextMenuItem(
				PFCat.HUD.ordinal(),
				"Level Menu",
				56,
				TextUtils.kAlignCenter | TextUtils.kAlignLeft,
				CMFontType.FX);
		button.setEnabledColors(0xa7ff67ff, 0xffffffff);
		button.setSelectedColors(0xa7ff67ff, 0xffffffff);
		button.setPressedColors(0xa7ff67ff, 0xffffffff);
		button.setIndicatorScale(1.5f);
		button.setSelected(true);
		button.setTag(kButtonTagMainMenu);
		button.addEventListener(MenuButton.EV_TYPE_RAISED, this);
		
		MenuIndicator indicator = button.getIndicator();
		indicator.setOffset(0, LangFX.getMenuDialogButtonOffset());
		
		return button;
	}
	
	public void setSplashProgress(float percent) {
		if (view != null)
			view.setSplashProgress(percent);
	}
	
	public void viewDidFadeIn() {
		dispatchEvent(EV_TYPE_SPLASH_DID_SHOW, this);
	}
	
	public void showMenuDialog(String key) {
		if (dialogManager != null)
			dialogManager.pushDialogForKey(key);
	}
	
	public void showMenuButton(String key) {
		if (dialogManager != null)
			dialogManager.showMenuButton(key);
	}
	
	public boolean isEscDialogShowing() {
		return dialogManager != null && dialogManager.isEscDialogShowing();
	}
	
	public void showEscDialog(PfState state) {
		if (dialogManager != null) {
			if (state == PfState.MENU)
				dialogManager.pushDialogForKey("MenuEsc");
			else if (state == PfState.PLAYING)
				dialogManager.pushDialogForKey("PlayfieldEsc");
		}
	}
	
	public void hideEscDialog(PfState state) {
		if (dialogManager != null)
			dialogManager.popDialog();
	}
	
	public void populateLevelMenu(Array<Level> levels) {
		if (view != null)
			view.populateLevelMenuView(levels);
	}
	
	public void showLevelMenu() {
		if (view != null)
			view.showLevelMenuOverTime(0.5f);
	}
	
	public void hideLevelMenu() {
		if (view != null)
			view.hideLevelMenuOverTime(1f);
	}
	
	public void hideLevelMenuInstantaneously() {
		if (view != null)
			view.hideLevelMenuInstantaneously();
	}
	
	public void puzzleWasSelectedAtMenu(int puzzleID) {
		scene.puzzleWasSelectedAtMenu(puzzleID);
	}
	
	public void jumpLevelMenuToLevel(int levelIndex, int puzzleIndex) {
		if (view != null && view.getLevelMenu() != null)
			view.getLevelMenu().jumpToLevelIndex(levelIndex, puzzleIndex);
	}
	
	public void returnToLevelMenu() {
		if (view != null && view.getLevelMenu() != null)
			view.getLevelMenu().returnToLevelMenu();
	}
	
	public void refreshLevelMenu() {
		if (view != null && view.getLevelMenu() != null)
			view.getLevelMenu().refresh();
	}
	
	public void refreshColorScheme() {
		if (view != null && view.getLevelMenu() != null)
			view.getLevelMenu().refreshColorScheme();
		if (dialogManager != null)
			dialogManager.refreshColorBlindText();
	}
	
	public void enableGodMode(boolean enable) {
		if (view != null && view.getLevelMenu() != null)
			view.getLevelMenu().setUnlockedAll(enable);
	}
	
	public void showLevelUnlockedDialog(String key, int levelIndex) {
		if (dialogManager != null) {
			dialogManager.addLevelUnlockedDialog(key, levelIndex);
			dialogManager.pushDialogForKey(key, true);
			scene.playSound("level-unlocked");
		}
	}
	
	public void showLevelCompletedDialog(int levelIndex) {
		if (dialogManager != null) {
			dialogManager.pushLevelCompletedDialog(levelIndex, true);
			scene.playSound("level-unlocked");
		}
	}
	
	public void showPuzzleWizardDialog() {
		if (dialogManager != null) {
			dialogManager.addPuzzleWizardDialog();
			scene.playSound("level-unlocked");
		}
	}

	@Override
	public void onEvent(int evType, Object evData) {
		if (evType == LevelMenu.EV_TYPE_DID_TRANSITION_IN) {
			setState(MenuState.MENU_IN);
		} else if (evType == LevelMenu.EV_TYPE_DID_TRANSITION_OUT) {
			setState(MenuState.MENU_OUT);
		} else if (evType == MenuDialogManager.EV_TYPE_DID_CLOSE_LEVEL_COMPLETED_DIALOG) {
			dispatchEvent(evType);
		} else if (evType == MenuButton.EV_TYPE_RAISED) {
			MenuItem button = (MenuItem)evData;
			if (button != null) {
				if (button.getTag() == kButtonTagNextUnsolvedPuzzle) {
					if (dialogManager != null)
						dialogManager.popAllDialogs();
					scene.proceedToNextUnsolvedPuzzle();
				} else if (button.getTag() == kButtonTagMainMenu) {
					if (dialogManager != null)
						dialogManager.popAllDialogs();
					buyNowDirtyFlag = true;
					scene.returnToLevelMenu();
				}
			}
		} else if (evType == MenuView.EV_TYPE_TITLE_SUBVIEW_DID_FADE) {
			assert(scene.isReady()) :
				"MenuController::onEvent MenuView.EV_TYPE_TITLE_SUBVIEW_DID_FADE before scene is ready.";
			if (!scene.hasPauseButton()) {
				scene.addPauseButton(scene.textureRegionByName("menu-icon"), 0.25f);
				scene.showEscKeyPrompt(3f, 1f);
			}
			InputManager.IM().enable(true);
		} else if (evType == SceneController.EV_TYPE_SCENE_IS_READY) {
			view.fadeSubviewOverTime("Title", 1.0f, 0.5f, true);
			view.fadeSplashProgressOverTime(0.25f, 0.25f);
		}
	}

	public void advanceTime(float dt) {
		if (view != null)
			view.advanceTime(dt);
		if (dialogManager != null)
			dialogManager.advanceTime(dt);
	}
}
