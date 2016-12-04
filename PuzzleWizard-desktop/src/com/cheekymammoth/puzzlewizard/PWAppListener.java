package com.cheekymammoth.puzzlewizard;

import com.cheekymammoth.events.EventDispatcher;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.cheekymammoth.sceneControllers.GameController;
import com.cheekymammoth.utils.CMPreferences;

public class PWAppListener implements ApplicationListener {
	public static final int EV_TYPE_APP_DID_LAUNCH;
	public static final int EV_TYPE_APP_DID_EXIT;
	//private static final float FPS = 60.0f;
	//private static final float TPF = 1.0f/FPS;
	
	static {
		EV_TYPE_APP_DID_LAUNCH = EventDispatcher.nextEvType();
		EV_TYPE_APP_DID_EXIT = EventDispatcher.nextEvType();
	}
	
	//private float mAccum = 0;
	//private FPSLogger fpsLogger;
	private CMPreferences preferences;
	
	public PWAppListener(CMPreferences preferences) {
		this.preferences = preferences;
	}
	
	@Override
	public void create() {
		GameController.GC().launch(preferences);
		//fpsLogger = new FPSLogger();
	}

	@Override
	public void dispose() {
		GameController.flushPreferences();
	}

	@Override
	public void render() {		
		GameController gc = GameController.GC();
		
//		mAccum += Gdx.graphics.getDeltaTime();
//        while (mAccum > TPF) {
//        	gc.advanceTime(TPF);
//        	mAccum -= TPF;
//        }
		
        gc.advanceTime(Gdx.graphics.getDeltaTime());
		gc.render();
		//fpsLogger.log();
	}

	@Override
	public void resize(int width, int height) {
		if (width <= 0 || height <= 0)
			return;
		GameController.GC().resize(width, height);
	}

	@Override
	public void pause() {
		GameController.GC().pause();
	}

	@Override
	public void resume() {
		GameController.GC().resume();
	}
}
