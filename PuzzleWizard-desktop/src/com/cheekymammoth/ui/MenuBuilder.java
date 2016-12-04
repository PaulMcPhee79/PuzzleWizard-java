package com.cheekymammoth.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.graphics.Prop9;
import com.cheekymammoth.sceneControllers.SceneController;

abstract public class MenuBuilder {
	public enum MenuBuilderType { DESKTOP, MOBILE };
	
	public static final String kGodModeOn = " "; // "-DEV-   Unlock All : On";
	public static final String kGodModeOff = " "; // "-DEV-   Unlock All : Off";
	
	public static final String kColorBlindModeOn = "Color Blind Mode: On";
	public static final String kColorBlindModeOff = "Color Blind Mode: Off";
	
	public static final int kMenuEnableColor = 0xffff4dff;
	public static final int kMenuSelectedColor = 0x22ff3eff;
	public static final int kMenuPressedColor = 0x22ff3eff;
	public static final int kMenuDisabledColor = 0xbdbdbdff;
	public static final int kMenuWhiteColor = 0xffffffff;
	public static final int kMenuDarkYellow = 0xffc71eff;
	public static final int kMenuOrange = 0xff8800ff;
	public static final int kMenuSlateBlue = 0x6a5acdff;
	public static final int kMenuFontSize = 44;
	public static final int kEnLblHeight = TextUtils.kEnLblHeight;
	public static final int kEnLblDblHeight = TextUtils.kEnLblDblHeight;
	
	private static MenuBuilder singleton;
	private static SceneController scene;
	private static boolean godMode;
	protected MenuBuilder() { }
	
	public static MenuBuilder initMenuBuilderType(MenuBuilderType type) {
		assert (singleton == null) : "Invalid - attempt to re-initialize MenBuilder.";
		if (type == MenuBuilderType.DESKTOP)
			singleton = new MenuBuilderDT();
		assert (singleton != null) : "MOBILE MenuBuilder not yet supported";
		return singleton;
	}
	
	public static boolean isGodModeEnabled() { return godMode; }
	
	public static void enableGodMode(boolean enable) { godMode = enable; }
	
	public static MenuDialog buildMenuEscDialog(int category, IEventListener listener, int[] evCodes) {
		return singleton.buildMenuEscDialog_(category, listener, evCodes);
	}
	
	public static MenuDialog buildTrialMenuEscDialog(int category, IEventListener listener, int[] evCodes) {
		return singleton.buildTrialMenuEscDialog_(category, listener, evCodes);
	}
	
	public static MenuDialog buildPlayfieldEscDialog(int category, IEventListener listener, int[] evCodes) {
		return singleton.buildPlayfieldEscDialog_(category, listener, evCodes);
	}
	
	public static MenuDialog buildTrialPlayfieldEscDialog(int category, IEventListener listener, int[] evCodes) {
		return singleton.buildTrialPlayfieldEscDialog_(category, listener, evCodes);
	}
	
	public static MenuDialog buildOptionsDialog(int category, IEventListener listener, int[] evCodes) {
		return singleton.buildOptionsDialog_(category, listener, evCodes);
	}
	
	public static MenuDialog buildLanguageDialog(int category, IEventListener listener, int[] evCodes) {
		return singleton.buildLanguageDialog_(category, listener, evCodes);
	}
	
	public static MenuDialog buildDisplayDialog(int category, IEventListener listener, int[] evCodes) {
		return singleton.buildDisplayDialog_(category, listener, evCodes);
	}
	
	public static MenuDialog buildDisplayConfirmDialog(int category, IEventListener listener, int[] evCodes,
			int duration, String msgName) {
		return singleton.buildDisplayConfirmDialog_(category, listener, evCodes, duration, msgName);
	}
	
	public static MenuDialog buildCreditsDialog(int category, IEventListener listener, int[] evCodes) {
		return singleton.buildCreditsDialog_(category, listener, evCodes);
	}
	
	public static MenuDialog buildQueryDialog(int category, IEventListener listener, int[] evCodes) {
		return singleton.buildQueryDialog_(category, listener, evCodes);
	}
	
	public static MenuDialog buildNotificationDialog(int category, float width, float height, String notice,
			IEventListener listener, int evCode) {
		return singleton.buildNotificationDialog_(category, width, height, notice, listener, evCode);
	}
	
	public static MenuDialog buildLevelUnlockedDialog(int category, int levelIndex,
			IEventListener listener, int[] evCodes) {
		return singleton.buildLevelUnlockedDialog_(category, levelIndex, listener, evCodes);
	}
	
	public static MenuDialog buildWizardUnlockedDialog(int category, IEventListener listener, int[] evCodes) {
		return singleton.buildWizardUnlockedDialog_(category, listener, evCodes);
	}
	
	public static MenuDialog buildLevelCompletedDialog(int category, int levelIndex,
			IEventListener listener, int[] evCodes) {
		return singleton.buildLevelCompletedDialog_(category, levelIndex, listener, evCodes);
	}
	
	public static MenuDialog buildPuzzleWizardDialog(int category, IEventListener listener, int[] evCodes) {
		return singleton.buildPuzzleWizardDialog_(category, listener, evCodes);
	}
	
	public static MenuDialog buildBuyNowDialog(int category, IEventListener listener, int[] evCodes) {
		return singleton.buildBuyNowDialog_(category, listener, evCodes);
	}
	
	protected static SceneController getScene() { return MenuBuilder.scene; }
	
	public static void setScene(SceneController scene) { MenuBuilder.scene = scene; }
	
	protected static Prop9 createDialogBg(float width, float height) {
		Prop9 bg = new Prop9(new TextureRegion(getScene().textureByName("dialog-bg.png")), 64, 64, 171, 104);
		bg.setSize(width, height);
		bg.centerPatch();
		bg.setVisible(false);
		return bg;
	}

	abstract protected MenuDialog buildMenuEscDialog_(int category, IEventListener listener, int[] evCodes);
	abstract protected MenuDialog buildTrialMenuEscDialog_(int category, IEventListener listener, int[] evCodes);
	abstract protected MenuDialog buildPlayfieldEscDialog_(int category, IEventListener listener, int[] evCodes);
	abstract protected MenuDialog buildTrialPlayfieldEscDialog_(int category, IEventListener listener, int[] evCodes);
	abstract protected MenuDialog buildOptionsDialog_(int category, IEventListener listener, int[] evCodes);
	abstract protected MenuDialog buildLanguageDialog_(int category, IEventListener listener, int[] evCodes);
	abstract protected MenuDialog buildDisplayDialog_(int category, IEventListener listener, int[] evCodes);
	abstract protected MenuDialog buildDisplayConfirmDialog_(int category, IEventListener listener, int[] evCodes,
			int duration, String msgName);
	abstract protected MenuDialog buildCreditsDialog_(int category, IEventListener listener, int[] evCodes);
	abstract protected MenuDialog buildQueryDialog_(int category, IEventListener listener, int[] evCodes);
	abstract protected MenuDialog buildNotificationDialog_(int category, float width, float height, String notice,
			IEventListener listener, int evCode);
	abstract protected MenuDialog buildLevelUnlockedDialog_(int category, int levelIndex,
			IEventListener listener, int[] evCodes);
	abstract protected MenuDialog buildWizardUnlockedDialog_(int category, IEventListener listener, int[] evCodes);
	abstract protected MenuDialog buildLevelCompletedDialog_(int category, int levelIndex,
			IEventListener listener, int[] evCodes);
	abstract protected MenuDialog buildPuzzleWizardDialog_(int category, IEventListener listener, int[] evCodes);
	abstract protected MenuDialog buildBuyNowDialog_(int category, IEventListener listener, int[] evCodes);
}
