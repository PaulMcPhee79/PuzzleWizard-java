package com.cheekymammoth.sceneControllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Preferences;
import com.cheekymammoth.assets.AssetServer;
import com.cheekymammoth.assets.AudioManager;
import com.cheekymammoth.assets.FontManager;
import com.cheekymammoth.assets.TextureManager;
import com.cheekymammoth.gameModes.PuzzleMode;
import com.cheekymammoth.gameModes.PuzzleMode10x8;
//import com.cheekymammoth.gameModes.PuzzleMode8x6;
import com.cheekymammoth.input.ControlsManager;
import com.cheekymammoth.input.InputManager;
import com.cheekymammoth.resolution.ResManager;
import com.cheekymammoth.ui.MenuBuilder;
import com.cheekymammoth.ui.MenuBuilder.MenuBuilderType;
import com.cheekymammoth.utils.CMPreferences;
import com.cheekymammoth.utils.CMSettings;
import com.cheekymammoth.utils.CrashContext;
import com.cheekymammoth.utils.PWExceptionHandler;
import com.gdxmu.utils.Listener;
import com.gdxmu.utils.MiscUtils;
import com.sun.jna.Platform;

//import de.matthiasmann.twl.utils.PNGDecoder;

public final class GameController implements Listener {
	private static final String kWindowName = "Puzzle Wizard";
	private static GameController singleton = new GameController();
	private static boolean isPreferencesDirty;
	private static boolean isTrialMode = false;
	
	private boolean hasLaunched;
	private boolean isPaused;
	private boolean isMaximized;
	private boolean didSplashShow;
	private boolean clampFrameRate;
	private int windowedWidth;
	private int windowedHeight;
	private int launchWindowWidth;
	private int launchWindowHeight;
	private TextureManager tm;
	private FontManager fm;
	private AudioManager am;
	private SceneController currentScene;
	private CMPreferences preferences;
	
    private GameController() {
    	
    }

    public static GameController GC() {
        return singleton;
    }
    
    private static void setDefaultUncaughtExceptionHandler() {
		try {
			Thread.setDefaultUncaughtExceptionHandler(new PWExceptionHandler());
		    System.setProperty("sun.awt.exception.handler", PWExceptionHandler.class.getName());
		} catch (SecurityException e) {
			Gdx.app.log("Error", "Could not set the Default Uncaught Exception Handler: "+ e);
		}
	}
    
    public void launch(CMPreferences preferences) {
    	if (hasLaunched)
    		return;
    	hasLaunched = true;
    	
    	// Doesn't seem to be any point in clearing the log...
    	//CMExceptionLog.clearLoggedExceptions();
    	setDefaultUncaughtExceptionHandler();
		
    	this.preferences = preferences;
    	
    	if (!getPreference(CMSettings.B_SETTINGS_INITIALZED, false)) {
    		// Initialize preferences on first time played
    		setPreference(CMSettings.B_SETTINGS_INITIALZED, true);
    		setPreference(CMSettings.B_FULLSCREEN, true);
    		setPreference(CMSettings.B_COLOR_BLIND_MODE, false);
    		setPreference(CMSettings.I_SFX, CMSettings.kDefaultSfxVolume);
    		setPreference(CMSettings.I_MUSIC, CMSettings.kDefaultMusicVolume);
    		setPreference(CMSettings.I_WIN_WIDTH, Gdx.graphics.getWidth());
    		setPreference(CMSettings.I_WIN_HEIGHT, Gdx.graphics.getHeight());
    		flushPreferences();
    	}
    	
    	windowedWidth = preferences.getInteger(CMSettings.I_WIN_WIDTH, CMSettings.kDefaultWinWidth);
    	windowedHeight = preferences.getInteger(CMSettings.I_WIN_HEIGHT, CMSettings.kDefaultWinHeight);
    	launchWindowWidth = Gdx.graphics.getWidth();
    	launchWindowHeight = Gdx.graphics.getHeight();
    	//setWindowDimensions(launchWindowWidth, launchWindowHeight);
    	
//    	setFullscreen(getPreference(CMSettings.B_FULLSCREEN, false));
    	
    	// Linux app icon
//    	int iconsUsed = Display.setIcon(new ByteBuffer[] {
//    			//loadIcon(AppResources.class.getResource("icon_32.png")),
//    			//loadIcon(AppResources.class.getResource("icon_24.png")),
//    			loadIcon(AppResources.class.getResource("out.png"))
//        });
//    	
//    	Gdx.app.log("Icons Used", "Num=" + iconsUsed);
    	
    	MiscUtils.initialize(kWindowName);
    	MiscUtils.MU().setListener(this);
    	
    	setMaximized(getPreference(CMSettings.B_MAXIMIZED, true));
    	if (isMaximized()) maximizeWindow();
    	// NOTE: Change this to suit mobile devices. i.e. iPad 1 & 2 would use 768. iPhone 4 would use 640 (and 8x6 mode).
    	ResManager.CONTENT_SCALE_FACTOR = 1536f / SceneController.DEFAULT_VIEW_HEIGHT;
    	PuzzleMode.setMode(new PuzzleMode10x8());
    	MenuBuilder.initMenuBuilderType(MenuBuilderType.DESKTOP);
    	MenuBuilder.enableGodMode(false);
    	
    	AssetServer.init();
    	tm = new TextureManager();
    	fm = new FontManager();
    	am = new AudioManager();
    	InputManager.IM().enable(false);
    	ControlsManager.CM().enableCursorFading(true);
    	ControlsManager.CM().hideMouseCursor();
    	
    	// Uncomment for debug purposes.
    	//Gdx.app.log("Preferences Path", preferences.getFilePath());
    }
    
//    private ByteBuffer loadIcon(URL url) {
//    	InputStream is = null;
//        try {
//        	is = url.openStream();
//            PNGDecoder decoder = new PNGDecoder(is);
//            ByteBuffer bb = ByteBuffer.allocateDirect(decoder.getWidth()*decoder.getHeight()*4);
//            decoder.decode(bb, decoder.getWidth()*4, PNGDecoder.Format.RGBA);
//            bb.flip();
//            return bb;
//        } catch (IOException e) {
//        	Gdx.app.log("loadIcon failed", e.getMessage());
//        } finally {
//        	 try { is.close(); }
//        	 catch (IOException e) { /* ignore */ }
//        }
//        
//        return null;
//    }
    
    public static boolean isTrialMode() { return isTrialMode; }
    
    public TextureManager getTM() {
    	return tm;
    }
    
    public FontManager getFM() {
    	return fm;
    }
    
    public AudioManager getAM() {
    	return am; 
    }
    
    public String getBgTexureName() {
    	//return "data/bg-atlas.png";
    	//return "data/bg-atlas@2x.png";
    	return "art/bg.png";
    }
    
    public String getIconTexureName() {
    	//return "data/icon-concept.png";
    	//return "data/icon-concept@2x.png";
    	return "data/icon-concept@hd.png";
    }
    
    public String getShaderVertName() {
    	return "shaders/refraction.vert";
    }
    
    public String getShaderFragName() {
    	return "shaders/refraction.frag";
    }
    
    public void splashDidShow() {
    	if (didSplashShow == false) {
    		assert(currentScene != null) : "GameController - invalid state";
    		currentScene.loadContent();
    	}
    }
    
    public void enableFrameRateClampOnNextFrame(boolean enable) {
    	clampFrameRate = enable;
    }
    
    private float skipIntro = 0.25f;
    private void smoothLoadingIntro(float dt) {
    	if (skipIntro > 0) {
    		skipIntro -= Math.min(1/60f, dt);
    		
    		if (skipIntro <= 0) {
    			currentScene = new PlayfieldController();
    			splashIntro = 0.25f;
    		}
    	}
    }
    
    private float splashIntro = 0f;
    private float smoothSplashIntro(float dt) {
    	if (splashIntro > 0) {
    		float dtRet = Math.min(1/60f, dt);
    		splashIntro -= dtRet;
    		return dtRet;
    	} else
    		return dt;
    }
    
    public void advanceTime(float dt) {
    	if (clampFrameRate) {
    		enableFrameRateClampOnNextFrame(false);
    		dt = Math.min(1/60f, dt);
    	}
    	
    	smoothLoadingIntro(dt);
    	dt = smoothSplashIntro(dt);
    	
    	MiscUtils.MU().update();

    	if (currentScene != null)
    		currentScene.advanceTime(dt);
    }
    
    public void render() {
    	if (currentScene != null)
    		currentScene.render();
    }
    
    public int getLaunchWinWidth() {
    	return launchWindowWidth;
    }
    
    public int getLaunchWinHeight() {
    	return launchWindowHeight;
    }
    
    public void resize(float width, float height) {
    	if (!hasLaunched) return;
    	
    	if (!isMaximized() && !isFullscreen()) {
			windowedWidth = Gdx.graphics.getWidth();
			windowedHeight = Gdx.graphics.getHeight();
			setPreference(CMSettings.I_WIN_WIDTH, windowedWidth);
	    	setPreference(CMSettings.I_WIN_HEIGHT, windowedHeight);
		}
    	
    	if (currentScene != null)
    		currentScene.resize(width, height);
    	
    	ControlsManager.CM().clearKeyStates();
    	Gdx.app.log("Resized", "w:" + width + " h:" + height);
	}
    
    public void setWindowDimensions(int width, int height) {
    	if (isFullscreen()) {
    		windowedWidth = width;
			windowedHeight = height;
    	} else
    		Gdx.graphics.setDisplayMode(width, height, false);
    }
    
    public boolean isMaximized() {
    	return Platform.isWindows() ? isMaximized : false;
    }
    
    public void setMaximized(boolean value) {
    	if (value != isMaximized) {
    		isMaximized = value;
        	setPreference(CMSettings.B_MAXIMIZED, value);
    	}
    }
    
    public void maximizeWindow() {    	
    	if (!isFullscreen())
    		MiscUtils.MU().maximizeWindow();
    }
    
    // Returns the largest fullscreen display mode that does not
    // appear to span multiple monitors.
    private DisplayMode getFullscreenDisplayMode() {
    	DisplayMode displayMode = Gdx.graphics.getDesktopDisplayMode();
    	
    	if (displayMode != null && displayMode.width != 0 && displayMode.height != 0) {
	    	if (displayMode.width / (float)displayMode.height >= 2.5f) {
	    		// This is likely a dual monitor mode that stretches across both monitors. Try to
	    		// find an equivalent single-monitor mode.
	    		DisplayMode[] displayModes = Gdx.graphics.getDisplayModes();
	        	for (int i = 0, n = displayModes.length; i < n; i++) {
	        		DisplayMode dmi = displayModes[i];
	        		if (dmi.width == displayMode.width / 2 && dmi.height == displayMode.height) {
	        			displayMode = dmi;
	        			break;
	        		}
	        	}
	    	}
    	}
    	
    	return displayMode;
    }
    
    public boolean isFullscreen() { return Gdx.graphics.isFullscreen(); }
    
	public void setFullscreen(boolean value) {
		if (value == isFullscreen())
			return;
		
		MiscUtils.MU().windowWillDestroy();

		boolean didChange = false;
		if (value) {
			DisplayMode displayMode = getFullscreenDisplayMode();
			didChange = displayMode != null && Gdx.graphics.setDisplayMode(
					displayMode.width,
					displayMode.height,
					true);
		} else {
			didChange = Gdx.graphics.setDisplayMode(
					windowedWidth,
					windowedHeight,
					false);
		}
		
		MiscUtils.MU().setWindow(kWindowName);
		
		if (didChange) {
			if (!value && isMaximized())
				maximizeWindow();
			setPreference(CMSettings.B_FULLSCREEN, value);
		}
		
		CrashContext.setContext(
				value ? CrashContext.CONTEXT_ENABLED : CrashContext.CONTEXT_DISABLED,
						CrashContext.CONTEXT_FULL_SCREEN);
	}
	
	public void enableVSync(boolean enable) {
		Gdx.graphics.setVSync(enable);
	}
	
	protected static Preferences getPreferences() { return GC().preferences; }
	
	// Reference: https://code.google.com/p/libgdx/wiki/Preferences
	public static boolean getPreference(String key, boolean defaultValue) {
		return getPreferences().getBoolean(key, defaultValue);
	}
	
	public static void setPreference(String key, boolean value) {
		if (getPreferences().getBoolean(key) != value) { 
			getPreferences().putBoolean(key, value);
			isPreferencesDirty = true;
		}
	}
	
	public static int getPreference(String key, int defaultValue) {
		return getPreferences().getInteger(key, defaultValue);
	}
	
	public static void setPreference(String key, int value) {
		if (getPreferences().getInteger(key) != value) { 
			getPreferences().putInteger(key, value);
			isPreferencesDirty = true;
		}
	}
	
	public static String getPreference(String key, String defaultValue) {
		return getPreferences().getString(key, defaultValue);
	}
	
	public static void setPreference(String key, String value) {
		if (getPreferences().getString(key) != value) { 
			getPreferences().putString(key, value);
			isPreferencesDirty = true;
		}
	}
	
	public static void flushPreferences() {
		if (isPreferencesDirty) {
			getPreferences().flush();
			isPreferencesDirty = false;
		}
	}
	
	public boolean isPaused() { return isPaused; }
	
	public void pause() {
		isPaused = true;
	}

	public void resume() {
		isPaused = false;
	}
	
	public void exitApp() {
		flushPreferences();
		//if (getCanvas() != null)
		//	getCanvas().stop();
		//if (FSAppFrame != null)
		//	FSAppFrame.dispose();
		Gdx.app.exit();
	}
	
	@Override
	public void onGdxmuEvent(GdxmuEvent ev, int param) {
		switch (ev) {
			case ERROR:
				Gdx.app.log("Gdxmu Error", MiscUtils.MU().getLastErrorMsg());
				break;
			case NOTICE:
				switch (param) {
					case MiscUtils.PARAM_NOTICE_CTLS_CREATED:
						break;
					case MiscUtils.PARAM_NOTICE_CTLS_WILL_DESTROY:
						ControlsManager.CM().invalidateGamepads();
						break;
				}
				break;
			case HID:
				switch (param) {
					case MiscUtils.PARAM_HID_ARRIVED:
					case MiscUtils.PARAM_HID_REMOVED:
						MiscUtils.MU().refreshControllers();
						break;
				}
				break;
			case WIN_STATE:
				switch (param) {
					case MiscUtils.PARAM_WIN_STATE_RESTORED:
						setMaximized(false);
						break;
					case MiscUtils.PARAM_WIN_STATE_MINIMIZED:
						break;
					case MiscUtils.PARAM_WIN_STATE_MAXIMIZED:
						setMaximized(true);
						break;
				}
				break;
		}
	}
}




////////////////////////// Old fullscreen JFrame code ///////////////////////////////////////

//private CMLwjglCanvas getCanvas() { return (CMLwjglCanvas)Gdx.app; }
//
//public CMJFrame getAppFrame() { return appFrame; }
//
//public void setAppFrame(CMJFrame appFrame) {
//	if (this.appFrame == null)
//		this.appFrame = appFrame;
//}

//private void initAppIcon() {
//	if (appFrame == null)
//		return;
//	
//	AppResources appResources = new AppResources();
//	int[] iconSizes = new int[] { 16, 24, 32, 48, 64, 128, 256 };
//	ArrayList<Image> icons = new ArrayList<Image>(iconSizes.length);
//	for (int i = 0, n = iconSizes.length; i < n; i++) {
//		URL iconURL = appResources.getClass().getResource("icon_" + iconSizes[i] + ".png");
//		if (iconURL != null) {
//			ImageIcon icon = new ImageIcon(iconURL);
//			if (icon != null)
//				icons.add(icon.getImage());
//		}
//	}
//	
//	if (icons.size() > 0)
//		appFrame.setIconImages(icons);
//}

//WindowAdapter wa = new WindowAdapter() {
//@Override
//public void windowClosing(WindowEvent we) {
//	flushPreferences();
//}
//
//@Override
//public void windowStateChanged(WindowEvent we) {
//	isMaximized = (we.getNewState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
//	setPreference(CMSettings.B_MAXIMIZED, isMaximized);
//}
//};

//setAppFrame((CMJFrame)Frame.getFrames()[0]);
//initAppIcon();
//appFrame.setMinimumSize(new Dimension(256 + 8, 192 + 34));

//appFrame.addWindowListener(wa);
//appFrame.addWindowStateListener(wa);
//appFrame.setVisible(false);

//private boolean isContainedEqualAspectRatio(int parentWidth, int parentHeight, int childWidth, int childHeight) {
//	return parentWidth >= childWidth && parentHeight >= childHeight &&
//			parentWidth % childWidth == 0 && parentHeight % childHeight == 0;
//}

//private boolean isFullscreenEnqueued = false;
//private java.awt.Rectangle appFrameBoundsCache = new java.awt.Rectangle();

// GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//java.awt.Rectangle gfxCfgBounds = appFrame.getGraphicsConfiguration().getBounds();
//appFrame.getGraphicsConfiguration().getDevice().setFullScreenWindow(appFrame);


// JFrame method
//int width, height;
//if (value) {
//	DisplayMode displayMode = Gdx.graphics.getDesktopDisplayMode();
//	width = displayMode.width; height = displayMode.height;
//	wasMaximized = wasMaximized || isMaximized; // Make sure it is not unset on reentry
//	
//	Dimension d = appFrame.getContentPane().getSize();
//	if (isContainedEqualAspectRatio(width, height, d.width, d.height)) {
//		isFullscreenEnqueued = false;
//		Gdx.graphics.setDisplayMode(width, height, value);
//		appFrame.setVisible(false);
//	} else {
//		isFullscreenEnqueued = true;
//		appFrame.setVisible(false);
//		appFrame.getContentPane().setPreferredSize(new Dimension(width / 2, height / 2));
//		appFrame.pack();
//		return;
//	}
//} else {
//	width = windowedWidth; height = windowedHeight;
//	Gdx.graphics.setDisplayMode(width, height, value);
//	
//	//DisplayMode displayMode = Gdx.graphics.getDesktopDisplayMode();
////	appFrame.setBounds(
////			(displayMode.width - width) / 2,
////			(displayMode.height - height) / 2,
////			width,
////			height);
//	//appFrame.getContentPane().setPreferredSize(new Dimension(width, height));
//	//appFrame.pack();
//	appFrame.setLocationRelativeTo(null); // re-center window
//	appFrame.setVisible(true);
//	
//	if (wasMaximized) {
//		wasMaximized = false;
//		maximizeWindow(false);
//		maximizeWindow(true);
//	}
//}
//
//resize(width, height);


//if (appFrame != null && !isFullscreen()) {
//GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//GraphicsDevice gd = ge.getDefaultScreenDevice();
//
//if (gd.isFullScreenSupported()) {
//	appFrame.enableFullscreen(true);
	
//	appFrame.setVisible(false);
//	//appFrame.dispose();
//	try {
//		appFrame.enableFullscreen(true);
//		appFrameBoundsCache.setBounds(appFrame.getBounds());
//		appFrame.setResizable(false);
//		//appFrame.setUndecorated(true);
//		java.awt.Rectangle gfxCfgBounds = appFrame.getGraphicsConfiguration().getBounds();
//		appFrame.setBounds(gfxCfgBounds);
//		appFrame.pack();
//		Gdx.graphics.setDisplayMode(gfxCfgBounds.width, gfxCfgBounds.height, false);
//		appFrame.getGraphicsConfiguration().getDevice().setFullScreenWindow(appFrame);
//	} catch (Exception e) {
//		appFrame.setResizable(true);
//		//appFrame.setUndecorated(false);
//		appFrame.setBounds(appFrameBoundsCache);
//		Gdx.graphics.setDisplayMode(windowedWidth, windowedHeight, value);
//		gd.setFullScreenWindow(null);
//	}
//	
//	appFrame.setVisible(true);
//	
//	if (!value) {
//		if (isMaximized)
//			appFrame.setExtendedState(appFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
//		else
//			appFrame.setLocationRelativeTo(null); // re-center window
//	}
//}
//}


//if (appFrame != null && !isFullscreen()) {
//GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//GraphicsDevice gd = ge.getDefaultScreenDevice();
//
//if (gd.isFullScreenSupported()) {
//	try {
//		appFrame.removeCanvas();
//		appFrame.setVisible(false);
//		
//		if (FSAppFrame == null) {
//			LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
//			cfg.setFromDisplayMode(LwjglApplicationConfiguration.getDesktopDisplayMode());
//			cfg.title = "Puzzle Wizard";
//			cfg.useGL20 = true;
//			cfg.width = appFrame.getBounds().width;
//			cfg.height = appFrame.getBounds().height;
//			cfg.samples = 4;
//			cfg.resizable = true;
//			FSAppFrame = new CMJFrame(Gdx.app.getApplicationListener(), cfg, appFrame.getCMCanvas());
//		} else {
//			FSAppFrame.removeCanvas();
//			FSAppFrame.getCMCanvas().setCMJFrame(FSAppFrame);
//			FSAppFrame.addCanvas();
//		}
//
//		FSAppFrame.setVisible(true);
//		//java.awt.Rectangle gfxCfgBounds = gd.getDefaultConfiguration().getBounds();
//		//Gdx.graphics.setDisplayMode(gfxCfgBounds.width, gfxCfgBounds.height, false);
//		//gd.setFullScreenWindow(FSAppFrame);
//	} catch (Exception e) {
//		Gdx.graphics.setDisplayMode(windowedWidth, windowedHeight, value);
//		gd.setFullScreenWindow(null);
//	}
//	
//	appFrame.setVisible(true);
//	
//	if (!value) {
//		if (isMaximized)
//			appFrame.setExtendedState(appFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
//		else
//			appFrame.setLocationRelativeTo(null); // re-center window
//	}
//}
//}

//////////////////////////End of old fullscreen JFrame code ///////////////////////////////////////
