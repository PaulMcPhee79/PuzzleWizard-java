package com.cheekymammoth.sceneManagers;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Keys;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.gameModes.PuzzleMode;
import com.cheekymammoth.graphics.FullscreenQuad;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.input.CMInputs;
import com.cheekymammoth.input.ControlsManager;
import com.cheekymammoth.input.IInteractable;
import com.cheekymammoth.locale.ILocalizable;
import com.cheekymammoth.locale.Localizer.LocaleType;
import com.cheekymammoth.puzzleViews.PuzzleBoard;
import com.cheekymammoth.puzzles.PuzzleHelper.ColorScheme;
import com.cheekymammoth.resolution.IResDependent;
import com.cheekymammoth.sceneControllers.GameController;
import com.cheekymammoth.sceneControllers.PlayfieldController;
import com.cheekymammoth.ui.GaugeMenuItem;
import com.cheekymammoth.ui.MenuBuilder;
import com.cheekymammoth.ui.MenuDialog;
import com.cheekymammoth.ui.MenuIndicator;
import com.cheekymammoth.ui.MenuItem;
import com.cheekymammoth.ui.WizardDialog;
import com.cheekymammoth.utils.CMSettings;
import com.cheekymammoth.utils.CrashContext;
import com.cheekymammoth.utils.Exec;
import com.cheekymammoth.utils.FloatTweener;
import com.cheekymammoth.utils.LangFX;
import com.cheekymammoth.utils.Transitions;

public final class MenuDialogManager extends Prop implements IEventListener, IInteractable, IResDependent, ILocalizable {
	public static final int EV_TYPE_DID_CLOSE_LEVEL_COMPLETED_DIALOG;
	private static final String kBuyNowURL = "http://www.cheekymammoth.com/puzzlewizard.html";
	
	// Display change confirm dialog constants
	private static final int kRevertDuration = 15;
	private static final String kRevertLabelName = "revert_changes";
	
	// Event codes
	private static final int kEvNull = 0;
    private static final int kEvResume = 1;
    private static final int kEvOptions = 2;
    private static final int kEvResetPuzzle = 3;
    private static final int kEvMenu = 4;
    private static final int kEvMusic = 5;
    private static final int kEvSfx = 6;
    private static final int kEvDisplay = 7;
    private static final int kEvColorBlind = 8;
    private static final int kEvWindowed = 9;
    private static final int kEvFullscreen = 10;
    private static final int kEvCredits = 11;
    private static final int kEvBack = 12;
    private static final int kEvLevelUnlocked = 13;
    private static final int kEvLevelCompleted = 14;
    private static final int kEvPuzzleWizard = 15;
    private static final int kEvYes = 16;
    private static final int kEvNo = 17;
    private static final int kEvOK = 18;
    private static final int kEvConfirm = 19;
    private static final int kEvRevert = 20;
    private static final int kEvRateYes = 21;
    private static final int kEvRateNo = 22;
    private static final int kEvGodMode = 23;
   // private static final int kEvCloud = 24;
    private static final int kEvBuyNow = 25;
    private static final int kEvExit = 26;
    
    // ~100 free spaces
    private static final int kEvLang = 120;
    private static final int kEvLangEn = 121;
    private static final int kEvLangCn = 122;
    private static final int kEvLangDe = 123;
    private static final int kEvLangEs = 124;
    private static final int kEvLangFr = 125;
    private static final int kEvLangIt = 126;
    private static final int kEvLangJp = 127;
    private static final int kEvLangKr = 128;
    
	private static final int[] kEvLangs = new int[] {
		kEvLangEn, kEvLangCn, kEvLangDe, kEvLangEs, kEvLangFr, kEvLangIt, kEvLangJp, kEvLangKr
	};
	
	private static float kBgQuadTweenerDuration = 0.5f;
	private static float kBgQuadMaxOpacity = 190.0f / 255.0f; // 210.0f / 255.0f;
	
	static {
		EV_TYPE_DID_CLOSE_LEVEL_COMPLETED_DIALOG = EventDispatcher.nextEvType();
	}
	
	private int queryKey = kEvNull;
	private float revertCountdown;
	private MenuItem dummyEvItem = new MenuItem();
	private ObjectMap<String, MenuDialog> menuDialogs = new ObjectMap<String, MenuDialog>(10);
	private Array<MenuDialog> menuDialogStack = new Array<MenuDialog>(true, 5, MenuDialog.class);
	private MenuItem menuButton;
	private ObjectMap<String, MenuItem> menuButtons = new ObjectMap<String, MenuItem>(2);
	private FullscreenQuad bgQuad;
	private FloatTweener bgQuadTweener;
	
	private static LocaleType evLang2Locale(int evLang) {
		LocaleType locale = LocaleType.EN;
		
		switch (evLang) {
			case kEvLangCn: locale = LocaleType.CN; break;
			case kEvLangDe: locale = LocaleType.DE; break;
			case kEvLangEs: locale = LocaleType.ES; break;
			case kEvLangFr: locale = LocaleType.FR; break;
			case kEvLangIt: locale = LocaleType.IT; break;
			case kEvLangJp: locale = LocaleType.JP; break;
			case kEvLangKr: locale = LocaleType.KR; break;
			case kEvLangEn:
			default:
				locale = LocaleType.EN; break;
		}
		
		return locale;
	}
	
	private static int locale2EvLang(LocaleType locale) {
		int evLang = kEvLangEn;
        switch (locale)
        {
            case EN: evLang = kEvLangEn; break;
            case CN: evLang = kEvLangCn; break;
            case DE: evLang = kEvLangDe; break;
            case ES: evLang = kEvLangEs; break;
            case FR: evLang = kEvLangFr; break;
            case IT: evLang = kEvLangIt; break;
            case JP: evLang = kEvLangJp; break;
            case KR: evLang = kEvLangKr; break;
            default: return kEvLangEn;
        }

        return evLang;
	}
	
	public MenuDialogManager() {
		this(-1);
	}

	public MenuDialogManager(int category) {
		super(category);
		
		Vector2 dialogPos = new Vector2(scene.VW2(), scene.VH2());
		
		bgQuad = new FullscreenQuad();
		bgQuad.setColor(Color.BLACK);
		addActor(bgQuad);
		
		bgQuadTweener = new FloatTweener(0, Transitions.easeOut, this);
		
		if (GameController.isTrialMode()) {
			// TrialMenuEsc
			{
				int[] evCodes = new int[] { kEvResume, kEvBuyNow, kEvOptions, kEvCredits, kEvExit };
				MenuDialog dialog = MenuBuilder.buildTrialMenuEscDialog(getCategory(), this, evCodes);
				dialog.setPosition(dialogPos.x, dialogPos.y);
				addDialog("MenuEsc", dialog);
			}
		} else {
			// MenuEsc
			{
				int[] evCodes = new int[] { kEvResume, kEvOptions, kEvCredits, kEvGodMode, kEvExit };
				MenuDialog dialog = MenuBuilder.buildMenuEscDialog(getCategory(), this, evCodes);
				dialog.setPosition(dialogPos.x, dialogPos.y);
				addDialog("MenuEsc", dialog);
			}
		}
		
		if (GameController.isTrialMode()) {
			// TrialPlayfieldEsc
			{
				int[] evCodes = new int[] { kEvResume, kEvBuyNow, kEvResetPuzzle, kEvOptions, kEvMenu };
				MenuDialog dialog = MenuBuilder.buildTrialPlayfieldEscDialog(getCategory(), this, evCodes);
				dialog.setPosition(dialogPos.x, dialogPos.y);
				addDialog("PlayfieldEsc", dialog);
			}
		} else {
			// PlayfieldEsc
			{
				int[] evCodes = new int[] { kEvResume, kEvResetPuzzle, kEvOptions, kEvMenu };
				MenuDialog dialog = MenuBuilder.buildPlayfieldEscDialog(getCategory(), this, evCodes);
				dialog.setPosition(dialogPos.x, dialogPos.y);
				addDialog("PlayfieldEsc", dialog);
			}
		}

		// Options
		{
			int[] evCodes = new int[] { kEvMusic, kEvSfx, kEvDisplay, kEvLang, kEvColorBlind, kEvBack };
			MenuDialog dialog = MenuBuilder.buildOptionsDialog(getCategory(), this, evCodes);
			dialog.setPosition(dialogPos.x, dialogPos.y);
			addDialog("Options", dialog);
		}
		
		// Display
		{
			int[] evCodes = new int[] { kEvWindowed, kEvFullscreen, kEvBack };
			MenuDialog dialog = MenuBuilder.buildDisplayDialog(getCategory(), this, evCodes);
			dialog.setPosition(dialogPos.x, dialogPos.y);
			addDialog("Display", dialog);
		}
		
		// Display Confirm Changes
		{
			int[] evCodes = new int[] { kEvConfirm, kEvRevert };
			MenuDialog dialog = MenuBuilder.buildDisplayConfirmDialog(
					getCategory(),
					this,
					evCodes,
					kRevertDuration,
					kRevertLabelName);
			dialog.setPosition(dialogPos.x, dialogPos.y);
			addDialog("DisplayConfirm", dialog);
		}
		
		// Language
		{
			int[] evCodes = new int[] {
					kEvLangEn, kEvLangCn, kEvLangDe, kEvLangEs,
					kEvLangFr, kEvLangIt, kEvLangJp, kEvLangKr,
					kEvBack
			};
			
			MenuDialog dialog = MenuBuilder.buildLanguageDialog(getCategory(), this, evCodes);
			dialog.setPosition(dialogPos.x, dialogPos.y);
			addDialog("Language", dialog);
		}
		
		// Credits
		{
			int[] evCodes = new int[] { kEvBack };
			MenuDialog dialog = MenuBuilder.buildCreditsDialog(getCategory(), this, evCodes);
			dialog.setPosition(dialogPos.x, dialogPos.y);
			addDialog("Credits", dialog);
		}
		
		// Query
		{
			int[] evCodes = new int[] { kEvYes, kEvNo };
			MenuDialog dialog = MenuBuilder.buildQueryDialog(getCategory(), this, evCodes);
			dialog.setPosition(dialogPos.x, dialogPos.y);
			addDialog("Query", dialog);
		}
		
		// BuyNowNotification
		{
			MenuDialog dialog = MenuBuilder.buildNotificationDialog(
					getCategory(),
					1500,
					400,
					kBuyNowURL,
					this,
					kEvOK);
			dialog.setPosition(dialogPos.x, dialogPos.y);
			addDialog("BuyNowNotification", dialog);
		}
		
		// BuyNowDialog
		{
			int[] evCodes = new int[] { kEvResume, kEvBuyNow };
			MenuDialog dialog = MenuBuilder.buildBuyNowDialog(getCategory(), this, evCodes);
			dialog.setPosition(dialogPos.x, dialogPos.y);
			addDialog("BuyNow", dialog);
		}
		
		setVisible(isActive());
		setTouchable(Touchable.enabled);
	}

	public void applyGameSettings() {
		refreshMusicGauge();
		refreshSfxGauge();
		refreshColorBlindText();
		refreshLanguageText();
	}
	
	public boolean isEscDialogShowing() {
		MenuDialog currentDialog = getCurrentDialog();
		return currentDialog != null &&
				(currentDialog == getDialogForKey("MenuEsc") || currentDialog == getDialogForKey("PlayfieldEsc"));
	}
	
	public void refreshMusicGauge() {
		MenuDialog dialog = getDialogForKey("Options");
		if (dialog != null) {
			MenuItem item = dialog.getMenuItem(kEvMusic);
			if (item != null) {
				GaugeMenuItem gauge = (GaugeMenuItem)item;
				gauge.setGaugeLevel(scene.getMusicVolume());
			}
		}
	}
	
	public void refreshSfxGauge() {
		MenuDialog dialog = getDialogForKey("Options");
		if (dialog != null) {
			MenuItem item = dialog.getMenuItem(kEvSfx);
			if (item != null) {
				GaugeMenuItem gauge = (GaugeMenuItem)item;
				gauge.setGaugeLevel(scene.getSfxVolume());
			}
		}
	}
	
	public void refreshDisplayModeHighlight() {
		MenuDialog dialog = getDialogForKey("Display");
		if (dialog != null) {
			boolean isFullscreen = scene.isFullscreen();
			MenuItem item = dialog.getMenuItem(kEvWindowed);
			if (item != null) {
				item.setSelectedColors(!isFullscreen
						? MenuBuilder.kMenuOrange
						: MenuBuilder.kMenuSelectedColor,
						0xffffffff);
				item.setEnabledColors(!isFullscreen
						? MenuBuilder.kMenuOrange
						: MenuBuilder.kMenuEnableColor,
						0xffffffff);
			}
			
			item = dialog.getMenuItem(kEvFullscreen);
			if (item != null) {
				item.setSelectedColors(isFullscreen
						? MenuBuilder.kMenuOrange
						: MenuBuilder.kMenuSelectedColor,
						0xffffffff);
				item.setEnabledColors(isFullscreen
						? MenuBuilder.kMenuOrange
						: MenuBuilder.kMenuEnableColor,
						0xffffffff);
			}
		}
	}
	
	public void refreshColorBlindText() {
		MenuDialog dialog = getDialogForKey("Options");
		if (dialog != null)
			dialog.setMenuItemTextForTag(kEvColorBlind, GameController.getPreference(CMSettings.B_COLOR_BLIND_MODE, false)
					? scene.localize(MenuBuilder.kColorBlindModeOn)
					: scene.localize(MenuBuilder.kColorBlindModeOff));
	}
	
	public void refreshLanguageText() {
		MenuDialog dialog = getDialogForKey("Language");
		if (dialog != null) {
			int onTag = locale2EvLang(scene.getLocale());
			for (int i = 0, n = kEvLangs.length; i < n; i++) {
				MenuItem item = dialog.getMenuItem(kEvLangs[i]);
				if (item != null) {
					if (kEvLangs[i] == onTag) {
						item.setEnabledColors(MenuBuilder.kMenuOrange,  0xffffffff);
						item.setPressedColors(MenuBuilder.kMenuOrange,  0xffffffff);
						item.setSelectedColors(MenuBuilder.kMenuOrange,  0xffffffff);
					} else {
						item.setEnabledColors(MenuBuilder.kMenuEnableColor,  0xffffffff);
						item.setPressedColors(MenuBuilder.kMenuPressedColor,  0xffffffff);
						item.setSelectedColors(MenuBuilder.kMenuSelectedColor,  0xffffffff);
					}
				}
			}
		}
	}
	
	public void addLevelUnlockedDialog(String key, int levelIndex) {
		removeDialog(key);
		
		MenuDialog dialog = null;
		if (levelIndex == PuzzleMode.getNumLevels() - 1) {
			dialog = MenuBuilder.buildWizardUnlockedDialog(
					getCategory(),
					this,
					new int[] { kEvLevelUnlocked });
		} else {
			dialog = MenuBuilder.buildLevelUnlockedDialog(
					getCategory(),
					levelIndex,
					this,
					new int[] { kEvLevelUnlocked });
		}
		
		dialog.setPosition(scene.VW2(), scene.VH2());
		addDialog(key, dialog);
	}
	
	private MenuDialog createLevelCompletedDialog(int levelIndex) {
		MenuDialog dialog = MenuBuilder.buildLevelCompletedDialog(
				getCategory(),
				levelIndex,
				this,
				new int[] { kEvLevelCompleted });
		dialog.setPosition(scene.VW2(), scene.VH2());
		return dialog;
	}

	public void addPuzzleWizardDialog() {
		String key = "PuzzleWizard";
		if (getDialogForKey(key) == null) {
			MenuDialog dialog = MenuBuilder.buildPuzzleWizardDialog(
					getCategory(),
					this,
					new int[] { kEvPuzzleWizard });
			dialog.setPosition(scene.VW2(), scene.VH2());
			addDialog(key, dialog);
			pushDialogForKey(key, true);
		}
	}
	
	public void addDialog(String key, MenuDialog dialog) {
		if (key != null && dialog != null && !key.isEmpty() && getDialogForKey(key) == null)
			menuDialogs.put(key, dialog);
	}
	
	public void removeDialog(String key) {
		MenuDialog dialog = getDialogForKey(key);
		if (dialog != null) {
			menuDialogs.remove(key);
			if (dialog == getCurrentDialog())
				popDialog();
			if (menuDialogStack.contains(dialog, true))
				menuDialogStack.removeValue(dialog, true);
		}
	}
	
	private String getKeyForDialog(MenuDialog dialog) {
		String dialogKey = "";
		
		if (dialog != null) {
			Keys<String> keys = menuDialogs.keys();
			while (keys.hasNext) {
				String key = keys.next();
				if (dialog == menuDialogs.get(key)) {
					dialogKey = key;
					break;
				}
			}
		}
		
		return dialogKey;
	}
	
	private MenuDialog getDialogForKey(String key) {
		MenuDialog dialog = null;
		if (key != null && !key.isEmpty())
			dialog = menuDialogs.get(key);
		return dialog;
	}
	
	public void pushDialogForKey(String key) {
		pushDialogForKey(key, false);
	}
	
	public void pushDialogForKey(String key, boolean animate) {
		MenuDialog dialog = getDialogForKey(key), topDialog = getCurrentDialog();
		if (dialog != null && !menuDialogStack.contains(dialog, true)) {
			int insertAt = -1;
			for (int i = 0; i < menuDialogStack.size; i++) {
				if (dialog.getPriority() >= menuDialogStack.get(i).getPriority()) {
                    insertAt = i;
                    break;
                }
			}
			
			CrashContext.setContext(key, CrashContext.CONTEXT_MENU);
			
			if (insertAt == -1)
                menuDialogStack.add(dialog);
            else
                menuDialogStack.insert(insertAt, dialog);

            dialog.resetNav();
            addActor(dialog);

            if (getCurrentDialog() != topDialog) {
                if (topDialog != null)
                    topDialog.hide();
                getCurrentDialog().show(animate);
            } else
                dialog.hide();

            if (getStackHeight() == 1) {
                scene.pushFocusState(CMInputs.FOCUS_STATE_MENU_DIALOG, true);
                setVisible(isActive());
                updateMenuButton();
                bgQuadTweener.resetTween(
                		bgQuad.getColor().a,
                		kBgQuadMaxOpacity,
                		((kBgQuadMaxOpacity - bgQuad.getColor().a) / kBgQuadMaxOpacity) * kBgQuadTweenerDuration,
                		0);
            }
		}
	}
	
	public void pushLevelCompletedDialog(int levelIndex, boolean animate) {
		levelIndex = Math.max(0, Math.min(PuzzleMode.getNumLevels() - 1, levelIndex));
		String key = "LevelCompleted" + levelIndex;
		if (getDialogForKey(key) == null) {
			MenuDialog dialog = createLevelCompletedDialog(levelIndex);
			addDialog(key, dialog);
		}
		
		pushDialogForKey(key, animate);
	}

	public void popDialog() {
		if (menuDialogStack.size == 0)
            return;

        if (getCurrentDialog() != null)
            removeActor(getCurrentDialog());

        menuDialogStack.removeIndex(menuDialogStack.size - 1);

        MenuDialog currentDialog = getCurrentDialog();
        if (currentDialog != null) {
        	currentDialog.show(false);
        	
        	String dialogKey = getKeyForDialog(currentDialog);
        	if (!dialogKey.isEmpty())
        		CrashContext.setContext(dialogKey, CrashContext.CONTEXT_MENU);
        }

        if (getStackHeight() == 0) {
        	if (getMenuButton() != null)
        		updateMenuButton();
        	else
        		scene.popFocusState(CMInputs.FOCUS_STATE_MENU_DIALOG, true);
        	bgQuadTweener.resetTween(
            		bgQuad.getColor().a,
            		0,
            		(bgQuad.getColor().a / kBgQuadMaxOpacity) * kBgQuadTweenerDuration,
            		0);
        	GameController.flushPreferences();
        	CrashContext.setContext("None", CrashContext.CONTEXT_MENU);
        }
	}
	
	public void popAllDialogs() {
		while (menuDialogStack.size > 0) {
            int size = menuDialogStack.size;
            popDialog();
            if (size == menuDialogStack.size) // Avoid infinite loop in case pop fails.
                break;
        }
		
		setMenuButton(null);
	}
	
	public void addMenuButton(String key, MenuItem button) {
		if (key != null && button != null) {
			removeMenuButton(key);
			menuButtons.put(key, button);
			positionMenuButton(button);
		}
	}
	
	public void removeMenuButton(String key) {
		if (key != null) {
			MenuItem button = menuButtons.get(key);
			if (button != null) {
				if (button == getMenuButton())
					setMenuButton(null);
				menuButtons.remove(key);
			}
		}
	}
	
	public void showMenuButton(String key) {
		if (key != null) {
			MenuItem button = menuButtons.get(key);
			if (button != null) {
				setMenuButton(button);
				
				if (getStackHeight() == 0)
					scene.pushFocusState(CMInputs.FOCUS_STATE_MENU_DIALOG, true);
			}
		}
	}
	
	public void hideMenuButton(String key) {
		if (key != null) {
			MenuItem button = getMenuButton();
			if (button != null && button == menuButtons.get(key))
				setMenuButton(null);
		}
	}
	
	private MenuItem getMenuButton() { return menuButton; }
	
	private void setMenuButton(MenuItem value) {
		if (value == menuButton)
			return;
		
		if (menuButton != null)
			menuButton.remove();
		
		menuButton = value;
		
		if (menuButton != null) {
			menuButton.setVisible(getStackHeight() == 0);
			addActor(menuButton);
		} else if (getStackHeight() == 0)
			scene.popFocusState(CMInputs.FOCUS_STATE_MENU_DIALOG, true);
		
		setVisible(isActive());
	}
	
	private void updateMenuButton() {
		MenuItem button = getMenuButton();
		if (button != null)
			button.setVisible(getStackHeight() == 0);
	}
	
	private void positionMenuButton(MenuItem button) {
		PlayfieldController scene = (PlayfieldController)this.scene;
		if (scene != null && scene.getPuzzleController() != null && button != null) {
			PuzzleBoard board = scene.getPuzzleController().getPuzzleBoard();
			if (board != null) {
				Vector2 boardDims = board.getScaledBoardDimensions();
				button.setPosition(
						scene.VW2() + boardDims.x / 2 - (10 + button.getWidth()),
						scene.VH2() - (boardDims.y / 2 + 0.2f * button.getHeight()));
			}
		}
	}
	
	private MenuDialog getCurrentDialog() {
		return menuDialogStack.size > 0 ? menuDialogStack.get(menuDialogStack.size-1) : null;
	}
	
	private boolean isActive() {
		return getStackHeight() > 0 || getMenuButton() != null;
	}

	private int getStackHeight() {
		return menuDialogStack.size;
	}

	@Override
	public void resolutionDidChange(int width, int height) {
		bgQuad.resolutionDidChange(width, height);
		
		Keys<String> keys = menuDialogs.keys();
		while (keys.hasNext)
			menuDialogs.get(keys.next()).resolutionDidChange(width, height);
		
		refreshDisplayModeHighlight();
	}
	
	@Override
	public void localeDidChange(String fontKey, String FXFontKey) {
		Keys<String> keys = menuDialogs.keys();
		while (keys.hasNext)
			menuDialogs.get(keys.next()).localeDidChange(fontKey, FXFontKey);
		
		keys = menuButtons.keys();
		while (keys.hasNext) {
			MenuItem button = menuButtons.get(keys.next());
			button.localeDidChange(fontKey, FXFontKey);
			
			MenuIndicator indicator = button.getIndicator();
			if (indicator != null)
				indicator.setOffset(0, LangFX.getMenuDialogButtonOffset());
			
			positionMenuButton(button);
		}
		
		refreshLanguageText();
	}
	
	@Override
	public void onEvent(int evType, Object evData) {
		if (evType == MenuDialog.EV_TYPE_ITEM_DID_ACTIVATE) {
			MenuItem evItem = (MenuItem)evData;
			if (evItem != null) {
				switch (evItem.getTag()) {
					case kEvResume:
						scene.hideEscDialog();
						break;
					case kEvOptions:
						pushDialogForKey("Options");
						break;
					case kEvResetPuzzle:
						popAllDialogs();
						scene.resetCurrentPuzzle();
						break;
					case kEvMenu:
						popAllDialogs();
						scene.returnToPuzzleMenu();
						break;
					case kEvMusic:
					{
						MenuDialog dialog = getDialogForKey("Options");
						if (dialog != null) {
							MenuItem item = dialog.getMenuItem(kEvMusic);
							if (item != null) {
								GaugeMenuItem gauge = (GaugeMenuItem)item;
								scene.setMusicVolume(gauge.getGaugeLevel());
							}
						}
					}
						break;
					case kEvSfx:
					{
						MenuDialog dialog = getDialogForKey("Options");
						if (dialog != null) {
							MenuItem item = dialog.getMenuItem(kEvSfx);
							if (item != null) {
								GaugeMenuItem gauge = (GaugeMenuItem)item;
								scene.setSfxVolume(gauge.getGaugeLevel());
							}
						}
					}
						break;
					case kEvDisplay:
						refreshDisplayModeHighlight();
						pushDialogForKey("Display");
						break;
					case kEvColorBlind:
						scene.setColorScheme(scene.getColorScheme() == ColorScheme.NORMAL
								? ColorScheme.COLOR_BLIND
								: ColorScheme.NORMAL);
						break;
					case kEvWindowed:
						if (scene.isFullscreen()) {
							scene.setFullscreen(false);
							
							if (!scene.isFullscreen()) {
								refreshDisplayModeHighlight();
								pushDialogForKey("DisplayConfirm");
								revertCountdown = kRevertDuration;
							}
						}
						break;
					case kEvFullscreen:
						if (!scene.isFullscreen()) {
							scene.setFullscreen(true);
							
							if (scene.isFullscreen()) {
								refreshDisplayModeHighlight();
								pushDialogForKey("DisplayConfirm");
								revertCountdown = kRevertDuration;
							}
						}
						break;
					case kEvCredits:
						pushDialogForKey("Credits");
						break;
					case kEvBack:
						popDialog();
						break;
					case kEvLevelUnlocked:
						if (getCurrentDialog() != null && !getCurrentDialog().isAnimating())
							popDialog();
						break;
					case kEvPuzzleWizard:
						if (getCurrentDialog() != null && !getCurrentDialog().isAnimating())
						{
							popDialog();
							MenuDialog dialog = getDialogForKey("PuzzleWizard");
							if (dialog != null)
							{
								WizardDialog wizardDialog = (WizardDialog)dialog;
								removeDialog("PuzzleWizard");
								wizardDialog.dispose();
							}
						}
						break;
					case kEvLevelCompleted:
						if (getCurrentDialog() != null && !getCurrentDialog().isAnimating()) {
							popDialog();
							dispatchEvent(EV_TYPE_DID_CLOSE_LEVEL_COMPLETED_DIALOG, this);
						}
						break;
					case kEvYes:
					{
						switch (queryKey) {
							case kEvExit:
								GameController.GC().exitApp();
								break;
							case kEvNull:
							default:
								popDialog();
								break;
						}
						
						queryKey = kEvNull;
					}
						break;
					case kEvNo:
					{
//						switch (queryKey) {
//							default:
//								break;
//						}
						
						queryKey = kEvNull;
						popDialog();
					}
						break;
					case kEvOK:
						popDialog();
						break;
					case kEvConfirm:
						popDialog();
						break;
					case kEvRevert:
						scene.setFullscreen(!scene.isFullscreen());
						revertCountdown = 0;
						popDialog();
						break;
					case kEvRateYes:
						break;
					case kEvRateNo:
						break;
					case kEvGodMode:
						MenuDialog dialog = getCurrentDialog();
						if (dialog != null && dialog == getDialogForKey("MenuEsc")) {
							String itemText = dialog.getMenuItemTextForTag(kEvGodMode);
							if (itemText != null) {
								if (itemText.equalsIgnoreCase(MenuBuilder.kGodModeOn)) {
									dialog.setMenuItemTextForTag(kEvGodMode, MenuBuilder.kGodModeOn);
									scene.enableGodMode(true);
								} else {
									dialog.setMenuItemTextForTag(kEvGodMode, MenuBuilder.kGodModeOff);
									scene.enableGodMode(false);
								}
							}
						}
						break;
					case kEvBuyNow:
						if (!Exec.openURL(kBuyNowURL))
							pushDialogForKey("BuyNowNotification");
						break;
					case kEvExit:
						queryKey = kEvExit;
						pushDialogForKey("Query");
						break;
					case kEvLang:
						pushDialogForKey("Language");
						break;
					case kEvLangEn:
					case kEvLangCn:
					case kEvLangDe:
					case kEvLangEs:
					case kEvLangFr:
					case kEvLangIt:
					case kEvLangJp:
					case kEvLangKr:
						scene.setLocale(evLang2Locale(evItem.getTag()));
						break;
				}	
			}
		} else if (evType == FloatTweener.EV_TYPE_FLOAT_TWEENER_CHANGED) {
			if (bgQuad != null && bgQuadTweener != null) {
				Color color = bgQuad.getColor();
				bgQuad.setColor(bgQuad.getColor().set(color.r, color.g, color.b, bgQuadTweener.getTweenedValue()));
			}
		} else if (evType == FloatTweener.EV_TYPE_FLOAT_TWEENER_COMPLETED) {
			if (getStackHeight() == 0)
				setVisible(isActive());
		}
	}

	@Override
	public int getInputFocus() { return CMInputs.HAS_FOCUS_MENU_DIALOG; }

	@Override
	public void didGainFocus() { }

	@Override
	public void willLoseFocus() {
		popAllDialogs();
	}

	@Override
	public void update(CMInputs input) {
		MenuDialog currentDialog = getCurrentDialog();
		
		if (currentDialog != null) {
			if (currentDialog.canGoBack()) {
				if (input.didDepress(CMInputs.CI_CANCEL | CMInputs.CI_MENU)) {
					if (currentDialog == getDialogForKey("MenuEsc") || currentDialog == getDialogForKey("PlayfieldEsc"))
						scene.hideEscDialog();
					else if (input.didDepress(CMInputs.CI_CANCEL))
						popDialog();
					return;
				}
			} else {
				if (input.didDepress(CMInputs.CI_MENU) ||
						ControlsManager.CM().didKeyDepress(Input.Keys.ESCAPE)) {
					scene.showEscDialog();
					return;
				}
			}
			
			currentDialog.update(input);
		} else {
			if (input.didDepress(CMInputs.CI_MENU) ||
					ControlsManager.CM().didKeyDepress(Input.Keys.ESCAPE))
				scene.showEscDialog();
			else {
				MenuItem button = getMenuButton();
				if (button != null && button.isVisible()) {
					if (input.didDepress(CMInputs.CI_CONFIRM))
						button.depress();
					else if (input.didRaise(CMInputs.CI_CONFIRM))
						button.raise();
				}
			}
		}
	}
	
	private void refreshRevertText(int value) {
		MenuDialog dialog = getDialogForKey("DisplayConfirm");
		if (dialog != null) {
			Actor item = dialog.getContentItem(kRevertLabelName);
			if (item != null && item instanceof Label) {
				Label label = (Label)item;
				String msgString = scene.localize("Reverting in ? seconds");
				msgString = msgString.replaceFirst("\\?", "" + value);
				label.setText(msgString);
			}
		}
	}
	
	@Override
	public void advanceTime(float dt) {
		if (bgQuadTweener != null)
			bgQuadTweener.advanceTime(dt);
		
		MenuDialog dialog = getCurrentDialog();
		if (dialog != null) {
			dialog.advanceTime(dt);
			
			if (revertCountdown > 0 && dialog == getDialogForKey("DisplayConfirm")) {
				revertCountdown -= dt;
				refreshRevertText((int)revertCountdown);
				if (revertCountdown <= 0) {
					revertCountdown = 0;
					dummyEvItem.setTag(kEvRevert);
					onEvent(MenuDialog.EV_TYPE_ITEM_DID_ACTIVATE, dummyEvItem);
				}
			}
		}
	}
}
