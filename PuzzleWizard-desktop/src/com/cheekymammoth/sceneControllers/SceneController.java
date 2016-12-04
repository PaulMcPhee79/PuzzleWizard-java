package com.cheekymammoth.sceneControllers;

import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntMap.Keys;
import com.cheekymammoth.animations.IAnimatable;
import com.cheekymammoth.animations.Juggler;
import com.cheekymammoth.assets.AudioManager;
import com.cheekymammoth.assets.FontManager;
import com.cheekymammoth.assets.ShaderManager;
import com.cheekymammoth.assets.TextureManager;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.events.IEventDispatcher;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.graphics.CMAtlasSprite;
import com.cheekymammoth.graphics.CMSprite;
import com.cheekymammoth.graphics.GfxMode.AlphaMode;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.input.IInteractable;
import com.cheekymammoth.input.InputManager;
import com.cheekymammoth.locale.ILocalizable;
import com.cheekymammoth.locale.Localizer;
import com.cheekymammoth.locale.Localizer.LocaleType;
import com.cheekymammoth.puzzles.PuzzleHelper;
import com.cheekymammoth.puzzles.PuzzleHelper.ColorScheme;
import com.cheekymammoth.resolution.IResDependent;
import com.cheekymammoth.resolution.ResManager;
import com.cheekymammoth.sceneControllers.SceneUtils.PFCat;
import com.cheekymammoth.sceneManagers.SceneLayerManager;
import com.cheekymammoth.ui.MenuButton;
import com.cheekymammoth.ui.SpriteMenuItem;
import com.cheekymammoth.ui.TextUtils;
import com.cheekymammoth.utils.CMSettings;
import com.cheekymammoth.utils.CrashContext;
import com.cheekymammoth.utils.Jukebox;
import com.cheekymammoth.utils.Promo;
import com.cheekymammoth.utils.Transitions;

public abstract class SceneController implements IEventDispatcher, IEventListener, OnCompletionListener {
	public static final int EV_TYPE_SCENE_IS_READY;
	// Prefix for preferences keys because all libgdx apps on desktop use the same .prefs file
	public static final String PREFS_NAME = "puzzlewizard_settings";
	protected static final String SOUND_EXT = ".ogg";
	public static final float DEFAULT_VIEW_WIDTH = 2048f;
	public static final float DEFAULT_VIEW_HEIGHT = 1536f;
	public static final float DEFAULT_VIEW_ASPECT = DEFAULT_VIEW_WIDTH / DEFAULT_VIEW_HEIGHT;
	
	static {
		EV_TYPE_SCENE_IS_READY = EventDispatcher.nextEvType();
	}
	
	protected boolean locked;
	protected boolean isScenePaused;
	protected String sceneKey;
	protected Array<Prop> props = new Array<Prop>(true, 40, Prop.class);
	protected Array<Prop> advProps = new Array<Prop>(true, 20, Prop.class);
	protected Array<Prop> propsAddQueue = new Array<Prop>(true, 5, Prop.class);
	protected Array<Prop> propsRemoveQueue = new Array<Prop>(true, 5, Prop.class);
	protected Juggler juggler = new Juggler(30);
	protected Prop baseProp;
	protected Jukebox jukebox;
	protected ShaderManager shaderManager;
	protected SceneLayerManager sceneLayerManager;
	protected Stage stage;
	
	private boolean isReady;
	private boolean isLoadingContent;
	private boolean isSoundEnabled = true;
	private boolean isMusicEnabled = true;
	private int soundMasterVolume = CMSettings.kDefaultSfxVolume;
	private int musicMasterVolume = CMSettings.kDefaultMusicVolume;
	private float viewWidth = DEFAULT_VIEW_WIDTH;
	private float viewHeight = DEFAULT_VIEW_HEIGHT;
	private float viewportWidth;
	private float viewportHeight;
	private float puzzleDuration;
	private int lastFontSize = -1;
	private IntMap<String> fontNames = new IntMap<String>(5);
	// TODO: GL uniform locations and texture units
	private Array<IResDependent> resDependents = new Array<IResDependent>(true, 10, IResDependent.class);
	private Array<ILocalizable> localizables = new Array<ILocalizable>(true, 10, ILocalizable.class);
	private EventDispatcher dispatcher;
	
	public SceneController() {
//		float w = Gdx.graphics.getWidth();
//		float h = Gdx.graphics.getHeight();
//		
//		float stageWidth, stageHeight;
//		float aspectRatio = w / h;
//		if (aspectRatio >= 1f) {
//			stageWidth = aspectRatio;
//			stageHeight = 1f;
//		} else {
//			stageWidth = 1f;
//			stageHeight = 1f / aspectRatio;
//		}
		
		//mViewWidth = DEFAULT_VIEW_WIDTH * ResManager.CONTENT_SCALE_FACTOR;
		//mViewHeight = DEFAULT_VIEW_HEIGHT * ResManager.CONTENT_SCALE_FACTOR;
		
		viewportWidth = GameController.GC().getLaunchWinWidth();
        viewportHeight = GameController.GC().getLaunchWinHeight();
		
		//mStage = new Stage(stageWidth, stageHeight, true);
		stage = new Stage(viewWidth, viewHeight, true);
		stage.getCamera().position.set(stage.getWidth() / 2, stage.getHeight() / 2, 0f);
		//mStage.setCamera(new OrthographicCamera(DEFAULT_VIEW_WIDTH, DEFAULT_VIEW_HEIGHT));
		Gdx.input.setInputProcessor(stage);
		Gdx.gl20.glClearColor(0f, 0f, 0f, 1);
		
		stage.getSpriteBatch().setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
		//stage.getSpriteBatch().setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		//Gdx.gl20.glEnable(GL20.GL_BLEND);
		// Post-multiplied
        //Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		// Pre-multiplied
		//Gdx.gl20.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        baseProp = new Prop(-1, this);
        baseProp.setTransform(true);
        baseProp.setTouchable(Touchable.childrenOnly);
        //baseProp.setSize(viewWidth, viewHeight); //mStage.getWidth(), mStage.getHeight());
//        baseProp.addListener(new ClickListener() {
//        	private boolean isPressed;
//            public void clicked (InputEvent event, float x, float y) {
//            	if (isPressed)
//            		baseProp.setColor(1f, 1f, 1f, 1f);
//            	else
//            		baseProp.setColor(0.5f, 0.5f, 0.5f, 0.5f);
//            	isPressed = !isPressed;
//            }
//		});
        //mBaseProp.setPosition(-mViewWidth/2, -mViewHeight/2);
        stage.addActor(baseProp);
        
        Prop.setContentScaleFactor(ResManager.CONTENT_SCALE_FACTOR);
		Prop.setPropScene(this);
		CMSprite.setSpriteScene(this);
		CMSprite.setDefaultAlphaMode(AlphaMode.PRE_MULTIPLIED);
		
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	
	public void setupController() { }
	
	public void willGainSceneFocus() {
		Prop.setPropScene(this);
		attachEventListeners();
	}
	
	public void willLoseSceneFocus() {
		detachEventListeners();
	}
	
	public void attachEventListeners() {
		// TODO
	}
	
	public void detachEventListeners() {
		// TODO
	}
	
	public void applyGameSettings() {
		// TODO
	}
	
	protected void setupFonts() {
		if (getFM().isDelayFontFileLoading()) {
			getFM().loadDelayedFontFiles();
			getFM().setDelayFontFileLoading(false);
		}
	}
	
	protected void setupPrograms() {
		IntBuffer buf = BufferUtils.newIntBuffer(16);
		Gdx.gl.glGetIntegerv(GL20.GL_MAX_TEXTURE_IMAGE_UNITS, buf);
		int maxTextureUnits = buf.get();
		shaderManager = new ShaderManager(maxTextureUnits);
	}
	
	public void loadTexturesForKey(String key, LocaleType locale) { }
	
	public void unloadTexturesForKey(String key, LocaleType locale) { }
	
	public boolean isPaused() { return GameController.GC().isPaused(); }
	
	public boolean isReady() {
		return isReady;
	}
	
	protected void didFinishLoading() {
		if (isReady)
			return;
		
		setupFonts();
		setupPrograms();
		setupController();
		//resize(GameController.GC().getLaunchWinWidth(), GameController.GC().getLaunchWinHeight());
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		isReady = true;
		dispatchEvent(EV_TYPE_SCENE_IS_READY);
		removeEventListeners(EV_TYPE_SCENE_IS_READY);
	}
	
	protected void loadContent() {
		isLoadingContent = true;
	}
	
	protected void reloadContent() {
		
	}
	
	public boolean isLoadingContent() {
		return isLoadingContent;
	}
	
	protected void setLoadingContent(boolean value) {
		isLoadingContent = value;
	}
	
	public TextureManager getTM() {
		return GameController.GC().getTM();
	}
	
	public FontManager getFM() {
		return GameController.GC().getFM();
	}
	
	public AudioManager getAM() {
		return GameController.GC().getAM();
	}
	
	public void resize(float width, float height) {
		stage.setViewport(viewWidth, viewHeight, true); //, 0, 0, width, height);
		stage.getCamera().translate(-stage.getGutterWidth(), -stage.getGutterHeight(), 0);
		
		viewportWidth = width;
		viewportHeight = height;
		
		try {
			resolutionDidChange((int)width, (int)height);
		} catch (Exception e) {
			Gdx.app.log("Unhandled Exception in SceneController.resize", e.getMessage());
		}
	}
	
	public void enableMenuMode(boolean enable) { }
	
	public void showEscDialog() { }
	
	public void hideEscDialog() { }
	
	public void showBuyNowDialog() { }
	
	public void applicationDidEnterBackground() {
		// TODO
	}
	
	public void applicationWillEnterForeground() {
		// TODO
	}
	
	public void invalidateStateCaches() {
		// TODO
	}
	
	public boolean isScenePaused() {
		// TODO
		return false;
	}
	
	public void setScenePaused(boolean value) {
		// TODO
	}
	
	public Touchable getTouchableDefault() { 
		return Touchable.disabled;
	}
	
	public int getPauseCategory() {
		return 0;
	}
	
	public Stage getStage() {
		return stage;
	}
	
	public Camera getCamera() {
		return stage.getCamera();
	}
	
	public final float VW() {
		return viewWidth;
	}
	
	public final float VH() {
		return viewHeight;
	}
	
	public final float VW2() {
		return VW() / 2;
	}
	
	public final float VH2() {
		return VH() / 2;
	}
	
	public final float VPW() {
		return viewportWidth;
	}
	
	public final float VPH() {
		return viewportHeight;
	}

	public final float VPW2() {
		return VPW() / 2;
	}
	
	public final float VPH2() {
		return VPH() / 2;
	}
	
	public final float VinVPRatioW() {
		return VPW()/VW();
	}
	
	public final float VinVPRatioH() {
		return VPH()/VH();
	}
	
	public final float getViewAspectRatio() {
		return VW() / VH(); 
	}
	
	public final float getViewportAspectRatio() {
		return VPW() / VPH(); 
	}
	
	public final float getStageAspectRatio() {
		return stage.getWidth() / stage.getHeight();
	}
	
	public float getMaximizingContentScaleFactor() {
		return Math.max(1f, getViewportAspectRatio() / getViewAspectRatio());
	}
	
	// Returns the base font size of the font name last returned by getFontName()
	public int getLastFontSize() { return lastFontSize; }
	
	public String getFontName(int fontSize) {
		int closestKey = -1;
		// Try to find the closest size that is >= the requested size so that we can scale down
		Keys keys = fontNames.keys();
		while (keys.hasNext) {
			int key = keys.next();
			if (key >= fontSize) {
				if (closestKey == -1 || key - fontSize < closestKey - fontSize)
					closestKey = key;
			}
		}
		
		String fontName = fontNames.get(closestKey);
		
		if (fontName == null) {
			// Now just settle for the closest size
			keys = fontNames.keys();
			while (keys.hasNext) {
				int key = keys.next();
				if (closestKey == -1 || Math.abs(key - fontSize) < Math.abs(closestKey - fontSize))
					closestKey = key;
			}
			
			fontName = fontNames.get(closestKey);
		}
		
		assert(fontName != null) : "SceneController::getFontName - bad state.";
		lastFontSize = closestKey;
		return fontName;
	}
	
	public void addFontName(String fontName, int fontSize) {
		if (fontName != null && fontSize > 0)
			fontNames.put(fontSize, fontName);
	}
	
	public void removeFontName(int fontSize) {
		fontNames.remove(fontSize);
	}
	
	public BitmapFont getFont(int fontSize) {
		return getFont(getFontName(fontSize));
	}
	
	public BitmapFont getFont(String fontName) {
		return fontName != null ? getFM().fontByName(fontName) : null;
	}
	
	public void registerResDependent(IResDependent resDependent) {
		resDependents.add(resDependent);
	}
	
	public void deregisterResDependent(IResDependent resDependent) {
		resDependents.removeValue(resDependent, true);
	}
	
	protected void resolutionDidChange(int width, int height) throws Exception {
		updatePauseButton();
		updateEscKeyPrompt();
		
		for (int i = resDependents.size-1; i >= 0; i--)
			resDependents.get(i).resolutionDidChange(width, height);
	}
	
	public void registerLocalizable(ILocalizable localizable) {
		if (localizable != null && !localizables.contains(localizable, true))
			localizables.add(localizable);
	}
	
	public void deregisterLocalizable(ILocalizable localizable) {
		if (localizable != null)
			localizables.removeValue(localizable, true);
	}
	
	public void localeDidChange() {
		updateEscKeyPrompt();
		
		String fontKey = getFontName(TextUtils.kBaseFontSize);
		String FXFontKey = getFontName(TextUtils.kBaseFXFontSize);
		try {
			for (int i = 0, n = localizables.size; i < n; i++)
				localizables.get(i).localeDidChange(fontKey, FXFontKey);
		} catch (Exception e) {
			Gdx.app.log("localeDidChange", e.getMessage());
			e.printStackTrace();
		}
	}
	
	public LocaleType getLocale() { return Localizer.getLocale(); }
	
	public void setLocale(LocaleType locale) {
		if (locale != null && locale != getLocale()) {
			LocaleType prevLocale = getLocale();
			Localizer.initLocalizationStrings(LocaleType.EN, locale);
			Localizer.initLocalizationContent(locale);
			Localizer.setLocale(locale);
			localeDidChange();
			Localizer.purgeLocale(prevLocale, locale);
			GameController.setPreference(CMSettings.I_LOCALE, locale.ordinal());
			CrashContext.setContext(Localizer.locale2String(locale), CrashContext.CONTEXT_LOCALE);
		}
	}
	
	public String getLocaleString() { return Localizer.locale2String(getLocale()); }
	
	public String localize(String text) { return Localizer.localize(text); }
	
	public void subscribeToInputUpdates(IInteractable client) {
		InputManager.IM().subscribe(client);
	}
	
	public void subscribeToInputUpdates(IInteractable client, boolean modal) {
		InputManager.IM().subscribe(client, modal);
	}
	
	public void unsubscribeToInputUpdates(IInteractable client) {
		InputManager.IM().unsubscribe(client);
	}
	
	public void unsubscribeToInputUpdates(IInteractable client, boolean modal) {
		InputManager.IM().unsubscribe(client, modal);
	}
	
	public boolean hasInputFocus(int focus) {
		return InputManager.IM().hasFocus(focus);
	}
	
	public void pushFocusState(int focusState) {
		InputManager.IM().pushFocusState(focusState);
	}
	
	public void pushFocusState(int focusState, boolean modal) {
		InputManager.IM().pushFocusState(focusState, modal);
	}
	
	public void popFocusState() {
		InputManager.IM().popFocusState();
	}
	
	public void popFocusState(int focusState) {
		InputManager.IM().popFocusState(focusState);
	}
	
	public void popFocusState(int focusState, boolean modal) {
		InputManager.IM().popFocusState(focusState, modal);
	}
	
	public void popToFocusState(int focusState) {
		InputManager.IM().popToFocusState(focusState);
	}
	
	public void popToFocusState(int focusState, boolean modal) {
		InputManager.IM().popToFocusState(focusState, modal);
	}
	
	public void addToJuggler(IAnimatable obj) {
		juggler.addObject(obj);
	}
	
	public void removeFromJuggler(IAnimatable obj) {
		juggler.removeObject(obj);
	}
	
	public void removeAnimatablesWithTarget(Object target) {
		juggler.removeObjectsWithTarget(target);
	}
	
	public Texture textureByName(String name) {
		return getTM().textureByName(name);
	}
	
	public AtlasRegion textureRegionByName(String name) {
		return getTM().textureRegionByName(name);
	}
	
	public AtlasRegion textureRegionByName(String name, int index) {
		return getTM().textureRegionByName(name, index);
	}
	
	public Array<TextureAtlas.AtlasRegion> textureRegionsStartingWith(String name) {
		return getTM().textureRegionsStartingWith(name);
	}
	
	public BitmapFont fontByName(String name) {
		return getFM().fontByName(name);
	}
	
	public ShaderProgram shaderByName(String name) {
		return shaderManager.shaderByName(name);
	}
	
	public void bindTexture(int index, Texture texture) {
		shaderManager.bindTexture(index, texture);
	}
	
	public void applyShaderDesciptor(String name) {
		shaderManager.applyShaderDesciptor(name);
	}
	
	public void setTextureForShaderDescriptor(String name, int index, Texture texture) {
		shaderManager.setTextureForShaderDescriptor(name, index, texture);
	}
	
	public float getPuzzleDuration() { return puzzleDuration; }
	
	protected void setPuzzleDuration(float value) { puzzleDuration = Math.max(0, value); }
	
	public ColorScheme getColorScheme() { return PuzzleHelper.getColorScheme(); }
	
	public void setColorScheme(ColorScheme value) {
		if (value != getColorScheme()) {
			PuzzleHelper.setColorScheme(value);
			GameController.setPreference(CMSettings.B_COLOR_BLIND_MODE, value == ColorScheme.COLOR_BLIND);
			CrashContext.setContext(PuzzleHelper.ColorScheme2String(value), CrashContext.CONTEXT_COLOR_BLIND);
		}
	}
	
	public void resetCurrentPuzzle() { }
	
	public void returnToPuzzleMenu() { }
	
	public void returnToLevelMenu() { }
	
	public void enableGodMode(boolean enable) { }
	
	public void enableSound(boolean enable) {
		isSoundEnabled = enable;
	}
	
	public boolean isSoundEnabled() {
		return isSoundEnabled;
	}
	
	public void enableMusic(boolean enable) {
		this.isMusicEnabled = enable;
		
		Music music = getAM().getMusic();
		if (music != null) {
			if (enable && music.isPlaying() == false)
				music.play();
			else if (enable == false && music.isPlaying())
				music.pause();
		}
	}
	
	public boolean isMusicEnabled() {
		return isMusicEnabled;
	}
	
	public int getSfxVolume() {
		return soundMasterVolume;
	}
	
	public void setSfxVolume(int value) {
		soundMasterVolume = value;
		GameController.setPreference(CMSettings.I_SFX, value);
		CrashContext.setContext("" + value, CrashContext.CONTEXT_SFX);
	}
	
	public int getMusicVolume() {
		return musicMasterVolume;
	}
	
	public void setMusicVolume(int value) {
		musicMasterVolume = Math.max(0, Math.min(value, CMSettings.kMaxVolume));
		GameController.setPreference(CMSettings.I_MUSIC, value);

		if (isMusicEnabled()) {
			Music music = getAM().getMusic();
			if (music != null) {
				music.setVolume(value / 10f);
				if (value == 0 && music.isPlaying()) pauseMusic();
				else if (value != 0 && !music.isPlaying()) music.play();
			}
		}
		
		CrashContext.setContext("" + value, CrashContext.CONTEXT_MUSIC);
	}
	
	public long playSound(String name) {
		return playSound(name, soundMasterVolume / 10f, 1f);
	}
	
	public long playSound(String name, float volume) {
		return playSound(name, volume, 1f);
	}
	
	public long playSound(String name, float volume, float pitch) {
		long soundId = -1;
		
		if (isSoundEnabled) {
			Sound sound = getAM().soundByName(name + SOUND_EXT);
			if (sound != null)
				soundId = sound.play(volume, pitch, 0f);
		}
		
		return soundId;
	}
	
	public void stopSound(String name, long soundId) {
		Sound sound = getAM().soundByName(name + SOUND_EXT);
		if (sound != null)
			sound.stop(soundId);
	}
	
	public void playMusic(String name, boolean loop) {
		getAM().loadMusic(name);
		Music music = getAM().getMusic();
		if (music != null) {
			music.setOnCompletionListener(this);
			music.setLooping(loop);
			music.setVolume(musicMasterVolume / 10f);
			
			if (musicMasterVolume != 0)
				music.play();
		}
	}
	
	public void pauseMusic() {
		Music music = getAM().getMusic();
		if (music != null && music.isPlaying())
			music.pause();
	}
	
	public void stopMusic() {
		Music music = getAM().getMusic();
		if (music != null)
			music.stop();
	}
	
	public void onCompletion(Music music) {
		Music amMusic = getAM().getMusic();
		if (music == amMusic)
			getAM().unloadMusic();
	}
	
	protected String prevJukeboxSong() {
		if (jukebox != null)
			return jukebox.prevSong();
		else
			return null;
	}
	
	protected String getCurrentJukeboxSong() {
		if (jukebox != null)
			return jukebox.getCurrentSong();
		else
			return null;
	}
	
	public String nextJukeboxSong() {
		if (jukebox != null)
			return jukebox.nextSong();
		else
			return null;
	}
	
	public void playPrevJukeboxSong(boolean loop) {
		String songName = prevJukeboxSong();
		if (songName != null)
			playMusic(songName, loop);
	}
	
	public void playNextJukeboxSong(boolean loop) {
		String songName = nextJukeboxSong();
		if (songName != null)
			playMusic(songName, loop);
	}
	
	protected boolean tickContentLoad() {
		if (!isLoadingContent)
			return false;
		
		// The AssetManager is shared, so we do this dance to ensure we only tick it once per frame
		// but still have all ResourceManagers unpack their loaded resources at the end and still
		// tick even if some ResourceManagers do not have anything queued to load.
		isLoadingContent = getTM().isLoading() && getTM().update();
		isLoadingContent = isLoadingContent || (getFM().isLoading() && getFM().update());
		isLoadingContent = isLoadingContent || (getAM().isLoading() && getAM().update());
		if (!isLoadingContent)
			didFinishLoading();
		return true;
	}
	
	public void advanceTime(float dt) {
		if (tickContentLoad())
			return;
		
		InputManager.IM().update();
		stage.act(dt);
		
		locked = true; // *** LOCK ***
		
		for (int i = 0, n = advProps.size; i < n; i++)
			advProps.get(i).advanceTime(dt);
		juggler.advanceTime(dt);
		
		locked = false; // *** UNLOCK ***
		
		removeQueuedProps();
		addQueuedProps();
	}
	
	public void render() {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
	}
	
	public void addProp(Prop prop) {
		if (prop == null)
			return;
		
		if (locked)
			propsAddQueue.add(prop);
		else {
			props.add(prop);
			
			if (prop.isAdvanceable())
				advProps.add(prop);
			sceneLayerManager.addChild(prop, prop.getCategory());
			prop.addedToScene();
		}
	}
	
	public void removeProp(Prop prop) {
		if (prop == null)
			return;
		
		if (locked)
			propsRemoveQueue.add(prop);
		else {
			sceneLayerManager.removeChild(prop, prop.getCategory());
			if (prop.isAdvanceable())
				advProps.removeValue(prop, true);
			props.removeValue(prop, true);
			prop.removedFromScene();
		}
	}
	
	private void addQueuedProps() {
		assert(!locked) : "SceneController::addQueuedProps attempted when mLocked == true";

		if (propsAddQueue.size > 0) {
			for (int i = 0, n = propsAddQueue.size; i < n; i++)
				addProp(propsAddQueue.get(i));
			propsAddQueue.clear();
		}
	}
	
	private void removeQueuedProps() {
		assert(!locked) : "SceneController::removeQueuedProps attempted when mLocked == true";
		
		if (propsRemoveQueue.size > 0) {
			for (int i = 0, n = propsRemoveQueue.size; i < n; i++)
				removeProp(propsRemoveQueue.get(i));
			propsRemoveQueue.clear();
		}
	}
	
	protected SpriteMenuItem pauseButton;
	public boolean hasPauseButton() { return pauseButton != null; }
	
	public void addPauseButton(TextureRegion region, float fadeInDuration) {
		removePauseButton();
		
		pauseButton = new SpriteMenuItem(PFCat.HUD.ordinal(), region);
		pauseButton.addEventListener(MenuButton.EV_TYPE_RAISED, this);
		pauseButton.detachIndicator();
		pauseButton.setSpriteCentered(true);
		pauseButton.setTouchable(Touchable.enabled);
		pauseButton.setVisible(Promo.isPauseButtonVisible());
		addProp(pauseButton);
		updatePauseButton();
		
		if (fadeInDuration == 0)
			pauseButton.setColor(1f, 1f, 1f, 1.0f);
		else {
			pauseButton.setColor(1f, 1f, 1f, 0.0f);
			pauseButton.addAction(Actions.alpha(1.0f, fadeInDuration));
		}
	}
	
	public void removePauseButton() {
		if (pauseButton != null) {
			pauseButton.clearActions();
			pauseButton.remove();
			pauseButton = null;
		}
	}
	
	private void updatePauseButton() {
		if (pauseButton != null)
			pauseButton.setPosition(
					VW() + stage.getGutterWidth() - pauseButton.getWidth(),
					VH() + stage.getGutterHeight() - pauseButton.getHeight());
	}
	
	protected void onPausePressed() { }
	
	private final int kEscPromptSpriteTag = 123; 
	protected Prop escKeyPrompt;
	public void addEscKeyPrompt() {
		removeEscKeyPrompt();
		
		escKeyPrompt = new Prop(PFCat.HUD.ordinal());
		escKeyPrompt.setTransform(true);
		addProp(escKeyPrompt);
		
		CMAtlasSprite escKeySprite = new CMAtlasSprite(textureRegionByName("menu-esc"));
		escKeySprite.setTag(kEscPromptSpriteTag);
		escKeyPrompt.addSpriteChild(escKeySprite);
		
		updateEscKeyPrompt();
	}
	
	public void removeEscKeyPrompt() {
		if (escKeyPrompt != null) {
			escKeyPrompt.clearActions();
			escKeyPrompt.remove();
			escKeyPrompt = null;
		}
	}
	
	private void updateEscKeyPrompt() {
		if (escKeyPrompt == null)
			return;
		
		CMAtlasSprite escKeySprite = (CMAtlasSprite)escKeyPrompt.spriteChildForTag(kEscPromptSpriteTag);
		if (escKeySprite != null) {
			LocaleType locale = getLocale();
			String regionName = "menu-esc";
			switch (locale) {
				case FR:
					regionName = "menu-esc-FR";
					break;
				case CN:
					regionName = "menu-esc-CN";
					break;
				default:
					break;
			}
			
			AtlasRegion region = textureRegionByName(regionName);
			if (region != null)
				escKeySprite.setAtlasRegion(region);
			
			escKeyPrompt.setSize(escKeySprite.getWidth(), escKeySprite.getHeight());
		}

		escKeyPrompt.setPosition(
				VW() + stage.getGutterWidth() - 1.6f * escKeyPrompt.getWidth(),
				VH() + stage.getGutterHeight() - (escKeyPrompt.getHeight() - 4));
	}
	
	public void showEscKeyPrompt(float duration, float delay) {
		if (escKeyPrompt == null)
			addEscKeyPrompt();
		
		escKeyPrompt.setColor(1, 1, 1, 0);
		escKeyPrompt.setScale(0f);
		escKeyPrompt.clearActions();
		
		SequenceAction action = Actions.sequence(
				Actions.delay(delay),
				Actions.alpha(1.0f, 0.5f, Transitions.linear),
				Actions.scaleTo(1f, 1f, 1f, Transitions.linear),
				Actions.delay(duration),
				Actions.alpha(0.0f, 0.5f, Transitions.linear),
				Actions.hide());
		escKeyPrompt.addAction(action);
	}
	
	public boolean isFullscreen() {
		return GameController.GC().isFullscreen();
	}
	
	public void setFullscreen(boolean value) {
		GameController.GC().setFullscreen(value);
	}
	
	public Prop getBaseProp() {
		// TODO
		return baseProp;
	}

	@Override
	public void onEvent(int evType, Object evData) {
		if (evType == MenuButton.EV_TYPE_RAISED) {
			if (pauseButton != null && pauseButton == evData) {
				onPausePressed();
			}
		}
	}
	
	protected EventDispatcher getEventDispatcher() {
		if (dispatcher == null)
			dispatcher = new EventDispatcher();
		return dispatcher;
	}

	@Override
	public void addEventListener(int evType, IEventListener listener) {
		EventDispatcher dispatcher = getEventDispatcher();
		if (dispatcher != null)
			dispatcher.addEventListener(evType, listener);
	}
	
	@Override
	public void removeEventListener(int evType, IEventListener listener) {
		EventDispatcher dispatcher = getEventDispatcher();
		if (dispatcher != null)
			dispatcher.removeEventListener(evType, listener);
	}
	
	@Override
	public void removeEventListeners(int evType) {
		EventDispatcher dispatcher = getEventDispatcher();
		if (dispatcher != null)
			dispatcher.removeEventListeners(evType);
	}
	
	@Override
	public boolean hasEventListenerForType(int evType) {
		EventDispatcher dispatcher = getEventDispatcher();
		return dispatcher != null && dispatcher.hasEventListenerForType(evType);
	}
	
	@Override
	public void dispatchEvent(int evType, Object evData) {
		EventDispatcher dispatcher = getEventDispatcher();
		if (dispatcher != null)
			dispatcher.dispatchEvent(evType, evData);
	}
	
	public void dispatchEvent(int evType) {
		dispatchEvent(evType, null);
	}
}
