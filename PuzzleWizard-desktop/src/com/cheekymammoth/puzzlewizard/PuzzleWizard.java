package com.cheekymammoth.puzzlewizard;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import org.lwjgl.LWJGLException;


//import com.apple.eawt.Application;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.cheekymammoth.utils.CMPreferences;
import com.cheekymammoth.utils.CMSettings;
import com.cheekymammoth.utils.CrashContext;
import com.cheekymammoth.utils.Promo;


// References:
// Index: https://code.google.com/p/libgdx-users/wiki/TaskList
// Optimizing SpriteBatch GPU buffer size (at bottom of page):
//    https://code.google.com/p/libgdx/wiki/SpriteBatch#Performance_tuning
// Collections: https://github.com/libgdx/libgdx/wiki/Collections
// Masking: https://github.com/mattdesl/lwjgl-basics/wiki/LibGDX-Masking
// SCISSOR_TEST: Actor.clipBegin(float x, float y, float width, float height)

public class PuzzleWizard {
	public static void main(String[] args) throws LWJGLException {
		// Window title for mac, if AppBundler option doesn't work out.
		//System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Puzzle Wizard");
		
		CMPreferences preferences = new CMPreferences("com.cheekymammoth.puzzlewizard.prefs");
		boolean fullscreen = preferences.getBoolean(CMSettings.B_FULLSCREEN, false);
		
		// https://code.google.com/p/libgdx/wiki/GraphicsRuntime
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.setFromDisplayMode(LwjglApplicationConfiguration.getDesktopDisplayMode());
		cfg.title = "Puzzle Wizard";
		cfg.useGL20 = true;
		
		if (Promo.isPromoDisplaySizeEnabled()) {
			cfg.width = Promo.getDisplayWidth();
			cfg.height = Promo.getDisplayHeight();
			cfg.backgroundFPS = cfg.foregroundFPS = 30;
		} else {
			if (fullscreen) {
				GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
				cfg.width = gd.getDisplayMode().getWidth();
				cfg.height = gd.getDisplayMode().getHeight();
				CrashContext.setContext(CrashContext.CONTEXT_ENABLED, CrashContext.CONTEXT_FULL_SCREEN);
			} else {
				cfg.width = preferences.getInteger(CMSettings.I_WIN_WIDTH, CMSettings.kDefaultWinWidth);
				cfg.height = preferences.getInteger(CMSettings.I_WIN_HEIGHT, CMSettings.kDefaultWinHeight);
				CrashContext.setContext(CrashContext.CONTEXT_DISABLED, CrashContext.CONTEXT_FULL_SCREEN);
			}
			
			cfg.foregroundFPS = 60;
		}
		
		cfg.samples = 4;
		cfg.resizable = true;
		//cfg.addIcon("app/icon_256.png", FileType.Internal);
		cfg.addIcon("app/icon_128.png", FileType.Internal);
		//cfg.addIcon("app/icon_64.png", FileType.Internal);
		//cfg.addIcon("app/icon_48.png", FileType.Internal);
		cfg.addIcon("app/icon_32.png", FileType.Internal);
		//cfg.addIcon("app/icon_24.png", FileType.Internal);
		cfg.addIcon("app/icon_16.png", FileType.Internal);
		cfg.vSyncEnabled = true;
		cfg.fullscreen = fullscreen;
//		cfg.setFromDisplayMode(LwjglApplicationConfiguration.getDesktopDisplayMode());
		
		new LwjglApplication(new PWAppListener(preferences), cfg);
		
		// TODO make the Mac icon rounded like the mac app store does to the iOS app store icon. Give it a border, too.
		// This will better adhere to Apple's style guidelines.
//		AppResources appResources = new AppResources();
//		URL iconURL = appResources.getClass().getResource("icon_256.png"); // Change to icon_512x512@2x (1024x1024)
//		ImageIcon icon = new ImageIcon(iconURL);
//		Application.getApplication().setDockIconImage(icon.getImage());
		
		
		
		//new LwjglFrame(new PuzzleWizard(preferences), cfg);
		//new CMJFrame(new PuzzleWizard(preferences), cfg);
		
//		String[] iconsPaths = new String[] { "app/icon_16.png", "app/icon_32.png" };
//		ByteBuffer[] icons = new ByteBuffer[iconsPaths.length];
//        for (int i = 0, n = iconsPaths.length; i < n; i++) {
//                Pixmap pixmap = new Pixmap(Gdx.files.getFileHandle(iconsPaths[i], FileType.Internal));
//                if (pixmap.getFormat() != Format.RGBA8888) {
//                        Pixmap rgba = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), Format.RGBA8888);
//                        rgba.drawPixmap(pixmap, 0, 0);
//                        pixmap = rgba;
//                }
//                icons[i] = ByteBuffer.allocateDirect(pixmap.getPixels().limit());
//                icons[i].put(pixmap.getPixels()).flip();
//                pixmap.dispose();
//        }
//        Display.setIcon(icons);
	}
}
