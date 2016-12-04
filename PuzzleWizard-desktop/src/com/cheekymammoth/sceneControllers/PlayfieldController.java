package com.cheekymammoth.sceneControllers;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.cheekymammoth.assets.AssetServer;
import com.cheekymammoth.assets.ShaderManager.ShaderDescriptor;
import com.cheekymammoth.input.CMInputs;
import com.cheekymammoth.input.ControlsManager;
import com.cheekymammoth.input.IInteractable;
import com.cheekymammoth.locale.Localizer;
import com.cheekymammoth.locale.Localizer.LocaleType;
import com.cheekymammoth.puzzleControllers.PuzzleController;
import com.cheekymammoth.puzzleControllers.PuzzleOrganizer;
import com.cheekymammoth.puzzleio.GameProgressController;
import com.cheekymammoth.puzzles.Puzzle;
import com.cheekymammoth.puzzles.PuzzleHelper.ColorScheme;
import com.cheekymammoth.sceneControllers.MenuController.MenuState;
import com.cheekymammoth.sceneControllers.SceneUtils.PFCat;
import com.cheekymammoth.sceneManagers.MenuDialogManager;
import com.cheekymammoth.sceneManagers.SceneLayerManager;
import com.cheekymammoth.sceneViews.PlayfieldView;
import com.cheekymammoth.ui.MenuBuilder;
import com.cheekymammoth.ui.TextUtils;
import com.cheekymammoth.utils.CMSettings;
import com.cheekymammoth.utils.CrashContext;
import com.cheekymammoth.utils.Jukebox;
import com.cheekymammoth.utils.LangFX;
import com.cheekymammoth.utils.Promo;
import com.cheekymammoth.utils.Promo.PromoDeviceType;
import com.cheekymammoth.utils.ScreenshotSaver;

public class PlayfieldController extends SceneController implements IInteractable {
	public enum PfState { HIBERNATING, TITLE, MENU, PLAYING };
	
	private boolean hasProcessedSolvedPuzzle;
	private PfState state = PfState.HIBERNATING;
	private PfState prevState = PfState.HIBERNATING;
	private PlayfieldView view;
	private PuzzleController puzzleController;
	private MenuController menuController;
	
	public PlayfieldController() {
		Localizer.setScene(this, this);
		TextUtils.setScene(this, this);
		MenuBuilder.setScene(this);
		
		int[] touchableLayers = {
			SceneUtils.PFCat.HUD.ordinal(),
			SceneUtils.PFCat.DIALOGS.ordinal(),
			SceneUtils.PFCat.BOARD.ordinal()
		};
		sceneLayerManager = new SceneLayerManager(
				getBaseProp(),
				SceneUtils.PFCat.PFCAT_COUNT.ordinal(),
				VW(),
				VH());
		for (int i = 0, n = touchableLayers.length; i < n; i++)
			sceneLayerManager.setTouchable(touchableLayers[i], true);
		
		jukebox = new Jukebox(new String[] {
				"01-The_tale_of_room_620-Ehren_Starks.mp3",
				"02-Sunset_in_Pensacola-Ehren_Starks.mp3",
				"04-Slippolska-Erik_Ask_Upmark.mp3",
				"08-Blekingarna-Erik_Ask_Upmark.mp3",
				"08-Virgin_Light-Cheryl_Ann_Fulton.mp3",
				"09-Florellen-Erik_Ask_Upmark.mp3",
				"09-Hidden_Sky-Jami_Sieber.mp3",
				});
		jukebox.randomize();
		
		setLocale(Localizer.ordinal2Locale(GameController.getPreference(
				CMSettings.I_LOCALE,
				Localizer.getLocaleTypeFromCurrentUICulture().ordinal())));
		
		boolean async = false;
		TextureParameter texParam = new TextureParameter();
		texParam.minFilter = TextureFilter.Nearest;
		texParam.magFilter = TextureFilter.Nearest;
		texParam.wrapU = TextureWrap.Repeat;
		texParam.wrapV = TextureWrap.Repeat;
		getTM().loadTexture("quad.png", texParam, async);
		
		texParam = new TextureParameter();
		texParam.genMipMaps = true;
		texParam.minFilter = TextureFilter.MipMapLinearNearest;
		texParam.magFilter = TextureFilter.Linear;
		texParam.wrapU = TextureWrap.ClampToEdge;
		texParam.wrapV = TextureWrap.ClampToEdge;
		
		LocaleType locale = getLocale();
		String localeString = getLocaleString();
		getTM().loadTexture("lang/title-" + localeString + ".png", texParam, async);
		getTM().loadTexture("pw-logo-bg.png", texParam, async);
		loadTexturesForKey("SplashLocaleTextures", locale);
		
		GameProgressController.GPC().load();
		menuController = new MenuController(this);
		menuController.addEventListener(MenuDialogManager.EV_TYPE_DID_CLOSE_LEVEL_COMPLETED_DIALOG, this);
		menuController.addEventListener(MenuController.EV_TYPE_SPLASH_DID_SHOW, this);
		setState(PfState.TITLE);
	}
	
	@Override
	public void setupController() {
		if (view != null)
			return;
		super.setupController();

		subscribeToInputUpdates(this);
		subscribeToInputUpdates(this, true);
		
		puzzleController = new PuzzleController(this);
		puzzleController.addEventListener(PuzzleController.EV_TYPE_PUZZLE_DID_BEGIN, this);
		puzzleController.addEventListener(PuzzleController.EV_TYPE_PUZZLE_SOLVED_ANIMATION_COMPLETED, this);
		puzzleController.addEventListener(Puzzle.EV_TYPE_UNSOLVED_PUZZLE_WILL_SOLVE, this);
		puzzleController.loadPuzzleByID(2); // Yellow Brick Road
		
		// Clear context
		CrashContext.setContext("None", CrashContext.CONTEXT_PUZZLE_NAME);
		
		view = new PlayfieldView(this);
		view.setupView();
		subscribeToInputUpdates(view);
		
		menuController.populateLevelMenu(puzzleController.getPuzzleOrganizer().getLevels());
		setState(PfState.MENU);

		//if (Gdx.input.isPeripheralAvailable(Input.Peripheral.HardwareKeyboard))
    	Gdx.input.setInputProcessor(ControlsManager.CM());
    	ControlsManager.CM().setTouchProxy(stage);
    	Controllers.addListener(ControlsManager.CM());
    	
    	for (int i = 0; i < 12; i++) {
    		for (int j = 0; j < 6; j++) {
//    			GameProgressController.GPC().setSolved(i < 4, i, j);
//    			GameProgressController.GPC().setSolved(false, i, j); // Brand New
    			GameProgressController.GPC().setSolved(true, i, j); // Finished
//    			GameProgressController.GPC().setSolved(j > 3, i, j); // Level Unlocked
//    			GameProgressController.GPC().setSolved(i < 11 && j > 0, i, j); // Level Completed
//    			GameProgressController.GPC().setSolved(i < 11 && (i > 0 || j > 0), i, j); // Level Completed + Wizard Unlocked
//    			GameProgressController.GPC().setSolved(i > 0 || j > 0, i, j); // Puzzle Wizard
//    			GameProgressController.GPC().setSolved(i < 5 && j < 3, i, j); // Trial Mode Expired
    		}
    	}

    	GameProgressController.GPC().invalidateCaches();
    	applyGameSettings();
	}
	
	@Override
	protected void loadContent() {
		super.loadContent();
		
		// Textures
		boolean async = true;
		TextureParameter texParam = new TextureParameter();
		texParam.genMipMaps = true;
		texParam.minFilter = TextureFilter.MipMapLinearNearest;
		texParam.magFilter = TextureFilter.Linear; // TextureFilter.Nearest;
		texParam.wrapU = TextureWrap.Repeat;
		texParam.wrapV = TextureWrap.Repeat;
		getTM().loadTexture("bg.png", texParam, async);
		getTM().loadTexture("bg-menu.png", texParam, async);
		
		texParam = new TextureParameter();
		texParam.minFilter = TextureFilter.Linear;
		texParam.magFilter = TextureFilter.Linear;
		texParam.wrapU = TextureWrap.Repeat;
		texParam.wrapV = TextureWrap.Repeat;
		getTM().loadTexture("refraction.png", texParam, async);
		getTM().loadTexture("color-gradient.png", texParam, async);
		getTM().loadTexture("mirror-gradient.png", texParam, async);
		getTM().loadTexture("plasma.png", texParam, async);
		getTM().loadTexture("sparkle-gradient.png", texParam, async);
		getTM().loadTexture("tile-shadow.png", texParam, async);
		
		texParam = new TextureParameter();
		texParam.genMipMaps = true;
		texParam.minFilter = TextureFilter.MipMapLinearNearest;
		texParam.magFilter = TextureFilter.Linear;
		texParam.wrapU = TextureWrap.ClampToEdge;
		texParam.wrapV = TextureWrap.ClampToEdge;
		getTM().loadTexture("noise.png", texParam, async);
//		if (Promo.isPromoEnabled())
//			getTM().loadTexture("promo-bg.png", texParam, async);
		
		texParam = new TextureParameter();
		texParam.minFilter = TextureFilter.Linear;
		texParam.magFilter = TextureFilter.Linear;
		texParam.wrapU = TextureWrap.ClampToEdge;
		texParam.wrapV = TextureWrap.ClampToEdge;
		getTM().loadTexture("dialog-bg.png", texParam, async);
		
		// These are not PoT - will they be upsized or will
		// some drivers have trouble with them?
		texParam = new TextureParameter();
		texParam.genMipMaps = true;
		texParam.minFilter = TextureFilter.MipMapLinearNearest;
		texParam.magFilter = TextureFilter.Linear;
		texParam.wrapU = TextureWrap.ClampToEdge;
		texParam.wrapV = TextureWrap.ClampToEdge;
		getTM().loadTexture("shield-dome.png", texParam, async);
		getTM().loadTexture("shield-dome-btm.png", texParam, async);
		getTM().loadTexture("shield-dome-top.png", texParam, async);
		getTM().loadTexture("buy-now.png", texParam, async);
		getTM().loadTexture("price-tag.png", texParam, async); // price-tag.png is PoT (non-square)
		
		texParam = new TextureParameter();
		texParam.minFilter = TextureFilter.Nearest;
		texParam.magFilter = TextureFilter.Nearest;
		texParam.wrapU = TextureWrap.ClampToEdge;
		texParam.wrapV = TextureWrap.ClampToEdge;
		getTM().loadTexture("shield-stencil-0.png", texParam, async);
		getTM().loadTexture("shield-stencil-1.png", texParam, async);
		getTM().loadTexture("shield-stencil-2.png", texParam, async);
		getTM().loadTexture("shield-stencil-3.png", texParam, async);
		getTM().loadTexture("shield-stencil-4.png", texParam, async);
		getTM().loadTexture("sin2x-table.png", texParam, async);
		getTM().loadTexture("tile-shadow-occ.png", texParam, async);
		
		getTM().loadAtlas("locales.atlas", async);
		getTM().loadAtlas("menu.atlas", async);
		getTM().loadAtlas("playfield.atlas", async);
		getTM().loadAtlas("wizard-idle.atlas", async);
		getTM().loadAtlas("wizard-walk.atlas", async);
		
		Localizer.preloadFontsForLocale(getLocale(), true);
		
		texParam = new TextureParameter();
		texParam.genMipMaps = true;
		texParam.minFilter = TextureFilter.MipMapLinearNearest;
		texParam.magFilter = TextureFilter.Linear;
		texParam.wrapU = TextureWrap.ClampToEdge;
		texParam.wrapV = TextureWrap.ClampToEdge;
		getFM().loadFont("IQ-48.fnt", "IQ-48_0.png", texParam, true);
		addFontName("IQ-48.fnt", 48);
		
		// Audio
		getAM().loadSound("button.ogg", async);
		getAM().loadSound("cbelt-horiz.ogg", async);
		getAM().loadSound("cbelt-vert.ogg", async);
		getAM().loadSound("color-arrow-medium.ogg", async);
		getAM().loadSound("color-arrow-short.ogg", async);
		getAM().loadSound("color-arrow.ogg", async);
		getAM().loadSound("color-flood-medium.ogg", async);
		getAM().loadSound("color-flood-short.ogg", async);
		getAM().loadSound("color-flood.ogg", async);
		getAM().loadSound("color-magic.ogg", async);
		getAM().loadSound("color-swap.ogg", async);
		getAM().loadSound("color-swirl-medium.ogg", async);
		getAM().loadSound("color-swirl-short.ogg", async);
		getAM().loadSound("color-swirl.ogg", async);
		getAM().loadSound("crowd-cheer.ogg", async);
		getAM().loadSound("error.ogg", async);
		getAM().loadSound("fireworks.ogg", async);
		getAM().loadSound("level-unlocked.ogg", async);
		getAM().loadSound("locked.ogg", async);
		getAM().loadSound("mirrored-self.ogg", async);
		getAM().loadSound("player-teleport.ogg", async);
		getAM().loadSound("reset.ogg", async);
		getAM().loadSound("rotate.ogg", async);
		getAM().loadSound("solved-short.ogg", async);
		getAM().loadSound("solved.ogg", async);
		getAM().loadSound("tile-shield-activate.ogg", async);
		getAM().loadSound("tile-shield-deactivate.ogg", async);
		getAM().loadSound("tile-swap.ogg", async);
		getAM().loadSound("unlocked.ogg", async);
	}
	
	@Override
	protected void setupPrograms() {
		super.setupPrograms();
		
		shaderManager.loadShader("refraction");
		shaderManager.loadShader("sparkle");
		shaderManager.loadShader("colorGradient");
		shaderManager.loadShader("mirrorImage");
		shaderManager.loadShader("shield");
		shaderManager.loadShader("dissolve");
		shaderManager.loadShader("tileShadow");
		
		int texUnit = 1;
		ShaderProgram shader = shaderManager.shaderByName("refraction");
		shaderManager.addShaderDescriptor(
				"refraction",
				new ShaderDescriptor(
						shader,
						new int[] { texUnit },
						new String[] { "u_displacementTexture" },
						new Texture[] { textureByName("refraction.png") })
				);
		
		texUnit = 2;
		shader = shaderManager.shaderByName("sparkle");
		shaderManager.addShaderDescriptor(
				"sparkle",
				new ShaderDescriptor(
						shader,
						new int[] { texUnit },
						new String[] { "u_gradientTex" },
						new Texture[] { textureByName("sparkle-gradient.png") })
				);
		
		//////// Share texUnit ////////
		texUnit = 3;
		shader = shaderManager.shaderByName("colorGradient");
		shaderManager.addShaderDescriptor(
				"colorGradient",
				new ShaderDescriptor(
						shader,
						new int[] { texUnit },
						new String[] { "u_gradientTex" },
						new Texture[] { textureByName("color-gradient.png") })
				);
		
		shader = shaderManager.shaderByName("mirrorImage");
		shaderManager.addShaderDescriptor(
				"mirrorImage",
				new ShaderDescriptor(
						shader,
						new int[] { texUnit },
						new String[] { "u_gradientTex" },
						new Texture[] { textureByName("mirror-gradient.png") })
				);
		//////////////////////////////
		
		texUnit = 4;
		shader = shaderManager.shaderByName("shield");
		shaderManager.addShaderDescriptor(
				"shield",
				new ShaderDescriptor(
						shader,
						new int[] { texUnit, texUnit+1, texUnit+2, texUnit+3 },
						new String[] { "u_plasmaTex", "u_sin2xTable", "u_stencilTex0", "u_stencilTex1" },
						new Texture[] {
								textureByName("plasma.png"),
								textureByName("sin2x-table.png"),
								textureByName("shield-stencil-0.png"),
								textureByName("shield-stencil-0.png") })
				);
		
		texUnit = 8;
		shader = shaderManager.shaderByName("dissolve");
		shaderManager.addShaderDescriptor(
				"dissolve",
				new ShaderDescriptor(
						shader,
						new int[] { texUnit },
						new String[] { "u_noiseTex" },
						new Texture[] { textureByName("noise.png") })
				);
		
		texUnit = 9;
		shader = shaderManager.shaderByName("tileShadow");
		shaderManager.addShaderDescriptor(
				"tileShadow",
				new ShaderDescriptor(
						shader,
						new int[] { texUnit },
						new String[] { "u_occTex" },
						new Texture[] { textureByName("tile-shadow-occ.png") })
				);
	}
	
	@Override
	protected void didFinishLoading() {
		if (menuController != null)
			menuController.setSplashProgress(1f);
		super.didFinishLoading();
	}
	
	public PuzzleController getPuzzleController() { return puzzleController; }
	
	@Override
	public void applyGameSettings() {
		super.applyGameSettings();
		
		setSfxVolume(GameController.getPreference(CMSettings.I_SFX, CMSettings.kDefaultSfxVolume));
		setMusicVolume(GameController.getPreference(CMSettings.I_MUSIC, CMSettings.kDefaultMusicVolume));
		setColorScheme(GameController.getPreference(CMSettings.B_COLOR_BLIND_MODE, false)
				? ColorScheme.COLOR_BLIND
				: ColorScheme.NORMAL);
		
		if (menuController != null)
			menuController.applyGameSettings();
		
		if (isMusicEnabled())
			playNextJukeboxSong(false);
	}
	
	@Override
	public void loadTexturesForKey(String key, LocaleType locale) {
		if (key != null && key.equals("SplashLocaleTextures")) {
			boolean async = false;
			TextureParameter texParam = new TextureParameter();
			texParam.genMipMaps = true;
			texParam.minFilter = TextureFilter.MipMapLinearNearest;
			texParam.magFilter = TextureFilter.Linear;
			texParam.wrapU = TextureWrap.ClampToEdge;
			texParam.wrapV = TextureWrap.ClampToEdge;
			
			String puzzleString = LangFX.locale2PuzzleTexSuffix(locale);
			String wizardString = LangFX.locale2WizardTexSuffix(locale);
			String iqString = LangFX.locale2IQString(locale, false);
			
			if (puzzleString != null)
				getTM().loadTexture("lang/pw-logo-puzzle-" + puzzleString + ".png", texParam, async);
			if (wizardString != null)
				getTM().loadTexture("lang/pw-logo-wizard-" + wizardString + ".png", texParam, async);
			if (iqString != null)
				getTM().loadTexture("lang/pw-logo-" + iqString + ".png", texParam, async);
		}
	}
	
	@Override
	public void unloadTexturesForKey(String key, LocaleType locale) {
		if (key != null && key.equals("SplashLocaleTextures")) {
			String puzzleString = LangFX.locale2PuzzleTexSuffix(locale);
			String wizardString = LangFX.locale2WizardTexSuffix(locale);
			String iqString = LangFX.locale2IQString(locale, false);
			
			// Hack: retain common textures if in trial mode (they are re-used elsewere)/
			if (GameController.isTrialMode()) {
				LocaleType currentLocale = getLocale();
				String puzzleString2 = LangFX.locale2PuzzleTexSuffix(currentLocale);
				String wizardString2 = LangFX.locale2WizardTexSuffix(currentLocale);
				String iqString2 = LangFX.locale2IQString(currentLocale, false);
				
				if (puzzleString != null && puzzleString.equals(puzzleString2))
					puzzleString = null;
				if (wizardString != null && wizardString.equals(wizardString2))
					wizardString = null;
				if (iqString != null && iqString.equals(iqString2))
					iqString = null;
			}
			
			if (puzzleString != null)
				getTM().unloadTexture("lang/pw-logo-puzzle-" + puzzleString + ".png");
			if (wizardString != null)
				getTM().unloadTexture("lang/pw-logo-wizard-" + wizardString + ".png");
			if (iqString != null)
				getTM().unloadTexture("lang/pw-logo-" + iqString + ".png");
		}
	}
	
	@Override
	public void setLocale(LocaleType locale) {
		if (locale != null && locale != getLocale()) {
			if (isReady() && GameController.isTrialMode()) {
				LocaleType prevLocale = getLocale(); 
				loadTexturesForKey("SplashLocaleTextures", locale);
				super.setLocale(locale);
				unloadTexturesForKey("SplashLocaleTextures", prevLocale);
			} else
				super.setLocale(locale);
		}
	}
	
	@Override
	public int getPauseCategory() {
		return PFCat.HUD.ordinal();
	}
	
	@Override
	protected void resolutionDidChange(int width, int height) throws Exception {
		super.resolutionDidChange(width, height);
		if (puzzleController != null)
			puzzleController.resolutionDidChange(width, height);
	}
	
	@Override
	public void resize(float width, float height) {
		super.resize(width, height);
		
		if (view != null)
			view.resize(width, height);
	}
	
	@Override
	public void enableMenuMode(boolean enable) {
		super.enableMenuMode(enable);
		
		if (view != null)
			view.enableMenuMode(enable);
		if (getPuzzleController() != null)
			getPuzzleController().enableMenuMode(enable);
	}
	
	@Override
	public void enableGodMode(boolean enable) {
		if (menuController != null)
			menuController.enableGodMode(enable);
		GameProgressController.GPC().setUnlockedAll(enable);
	}
	
	@Override
	public void showEscDialog() {
		super.showEscDialog();
		
		if (menuController != null)
			menuController.showEscDialog(getState());
	}
	
	@Override
	public void hideEscDialog() {
		super.hideEscDialog();
		
		if (menuController != null)
			menuController.hideEscDialog(getState());
	}
	
	@Override
	public void showBuyNowDialog() {
		if (menuController != null)
			menuController.showMenuDialog("BuyNow");
	}
	
	@Override
	public void setColorScheme(ColorScheme value) {
		super.setColorScheme(value);
		
		if (menuController != null)
			menuController.refreshColorScheme();
		if (getPuzzleController() != null)
			getPuzzleController().refreshColorScheme();
	}
	
	public PfState getState() { return state; }
	
	public void setState(PfState value) {
		if (value == state)
			return;
		
		prevState = state;
		
		// Clean up previous state
		switch (prevState) {
			case HIBERNATING:
				break;
			case TITLE:
				popFocusState(CMInputs.FOCUS_STATE_TITLE);
				break;
			case MENU:
				break;
			case PLAYING:
				popFocusState(CMInputs.FOCUS_STATE_PF_PLAYFIELD);
				break;
		}
		
		// Apply new state
		state = value;
		
		switch (state) {
			case HIBERNATING:
				break;
			case TITLE:
				pushFocusState(CMInputs.FOCUS_STATE_TITLE);
				menuController.setState(MenuState.MENU_TITLE);
				break;
			case MENU:
				processMilestoneDialogs();
				menuController.setState(MenuState.TRANSITION_IN);
				break;
			case PLAYING:
				menuController.setState(MenuState.TRANSITION_OUT);
				pushFocusState(CMInputs.FOCUS_STATE_PF_PLAYFIELD);
				break;
		}
	}
	
	@Override
	public void resetCurrentPuzzle() {
		super.resetCurrentPuzzle();
		
		PuzzleController puzzleController = getPuzzleController();
		if (puzzleController != null) {
			puzzleController.cancelEnqueuedActions();
			processMilestoneDialogs();
			puzzleController.resetCurrentPuzzle();
		}
	}
	
	@Override
	public void returnToPuzzleMenu() {
		if (getState() == PfState.PLAYING && menuController != null && menuController.getState() == MenuState.MENU_OUT) {
			PuzzleController puzzleController = getPuzzleController();
			if (puzzleController != null) {
				puzzleController.cancelEnqueuedActions();
				
				Puzzle puzzle = puzzleController.getPuzzle();
				PuzzleOrganizer puzzleOrganizer = puzzleController.getPuzzleOrganizer();
				
				if (puzzleOrganizer != null && puzzle != null)
					menuController.jumpLevelMenuToLevel(
							puzzleOrganizer.levelIndexForPuzzleID(
									puzzle.getID()),
									puzzleOrganizer.levelBasedPuzzleIndexForPuzzleID(puzzle.getID()));
			}
			setState(PfState.MENU);
		}
	}
	
	@Override
	public void returnToLevelMenu() {
		returnToPuzzleMenu();
		
		if (menuController != null)
			menuController.returnToLevelMenu();
	}
	
	public void proceedToNextUnsolvedPuzzle() {
		PuzzleController puzzleController = getPuzzleController();
		if (puzzleController != null) {
			PuzzleOrganizer puzzleOrganizer = puzzleController.getPuzzleOrganizer();
			if (puzzleOrganizer != null) {
				int nextUnsolvedID = puzzleController.getNextUnsolvedPuzzleID();
				if (nextUnsolvedID != -1) {
					puzzleController.resetCurrentPuzzle();
					puzzleController.loadPuzzleByID(nextUnsolvedID);
					puzzleController.displayPuzzleRibbon();
					setPuzzleDuration(0);
				}
			}
		}
	}
	
//	private float countdown = 5;
//	private int testLevelIndex = 0;
	
	@Override
	public void advanceTime(float dt) {
		super.advanceTime(dt);
		
		if (isLoadingContent()) {
			if (menuController != null)
				menuController.setSplashProgress(AssetServer.getAssetManager().getProgress());
			return;
		}
		
		if (puzzleController != null)
			puzzleController.advanceTime(dt);
		
		if (menuController != null) {
			menuController.advanceTime(dt);
			if (getState() == PfState.PLAYING && menuController.isEscDialogShowing() == false)
				setPuzzleDuration(getPuzzleDuration() + dt);
		}
		
		if (view != null)
			view.advanceTime(dt);
		
		ControlsManager.CM().update(dt);
		
//		countdown -= dt;
//		if (countdown <= 0) {
//			//countdown = 10000;
//			//menuController.showLevelUnlockedDialog("LevelUnlocked", 11); //++testLevelIndex);
//			menuController.showLevelCompletedDialog(testLevelIndex++);
//			if (testLevelIndex == 12) testLevelIndex = 0;
//			//menuController.showPuzzleWizardDialog();
//		}
		
//		countdown -= dt;
//		if (countdown <= 0) {
//			countdown = 15;
//			Gdx.app.log("Puzzle", "" + PWDebug.puzzleCount);
//			Gdx.app.log("PuzzleBoard", "" + PWDebug.puzzleBoardCount);
//			Gdx.app.log("TilePiece", "" + PWDebug.tilePieceCount);
//			Gdx.app.log("Tile", "" + PWDebug.tileCount);
//			Gdx.app.log("TileDecoration", "" + PWDebug.tileDecorationCount);
//			Gdx.app.log("HumanPlayerPiece", "" + PWDebug.humanPlayerPieceCount);
//			Gdx.app.log("MirrorPlayerPiece", "" + PWDebug.mirrorPlayerPieceCount);
//			Gdx.app.log("Player", "" + PWDebug.playerCount);
//			Gdx.app.log("Shield", "" + PWDebug.shieldCount);
//			Gdx.app.log("End", "^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
//		}
		
//		if (countdown > 0) {
//			countdown -= dt;
//			if (countDown <= 0) {
//				//menuController.showLevelUnlockedDialog("LevelUnlocked" + 11, 11);
//				//countDown = 10f;
//				//menuController.showPuzzleWizardDialog();
//				showBuyNowDialog();
//			}
//		}
	}
	
	@Override
	public void onEvent(int evType, Object evData) {
		super.onEvent(evType, evData);
		
		if (evType == PuzzleController.EV_TYPE_PUZZLE_DID_BEGIN) {
			hasProcessedSolvedPuzzle = false;
			
			PuzzleController puzzleController = getPuzzleController();
			if (puzzleController != null) {
				Puzzle puzzle = puzzleController.getPuzzle();
				if (puzzle != null)
					CrashContext.setContext(puzzle.getName(), CrashContext.CONTEXT_PUZZLE_NAME);
			}
		} else if (evType == PuzzleController.EV_TYPE_PUZZLE_SOLVED_ANIMATION_COMPLETED) {
			if (!hasProcessedSolvedPuzzle) {
				PuzzleController puzzleController = getPuzzleController();
				if (puzzleController != null) {
					Puzzle puzzle = puzzleController.getPuzzle();
					if (puzzle != null) {
						GameProgressController gpc = GameProgressController.GPC();
						int levelIndex = puzzle.getLevelIndex();
						boolean wasLevelCompleted = puzzleController.didPuzzleGetSolved() &&
								gpc.getNumSolvedPuzzlesForLevel(levelIndex) == gpc.getNumPuzzlesPerLevel();
						
						if (getState() == PfState.PLAYING) {
							int nextUnsolvedID = puzzleController.getNextUnsolvedPuzzleID();
							if (wasLevelCompleted || nextUnsolvedID == -1)
								menuController.showMenuButton("MainMenu");
							else
								menuController.showMenuButton("NextUnsolvedPuzzle");
						}
						
						processMilestoneDialogs();
					}
				}
				
				hasProcessedSolvedPuzzle = true;
			}
		} else if (evType == Puzzle.EV_TYPE_UNSOLVED_PUZZLE_WILL_SOLVE) {
			// TODO Submit puzzle solved duration to TestFlight
		} else if (evType == MenuDialogManager.EV_TYPE_DID_CLOSE_LEVEL_COMPLETED_DIALOG) {
			PuzzleController puzzleController = getPuzzleController();
			if (puzzleController != null && puzzleController.didLevelUnlock()) {
				GameProgressController gpc = GameProgressController.GPC();
				if (gpc.getNumSolvedPuzzlesForLevel(gpc.getNumLevels()-1) == 0 && 
						gpc.getNumSolvedPuzzles() == gpc.getNumPuzzles() - gpc.getNumPuzzlesPerLevel())
					menuController.showLevelUnlockedDialog("LevelUnlocked", gpc.getNumLevels()-1);
			}
		} else if (evType == MenuController.EV_TYPE_SPLASH_DID_SHOW)
			GameController.GC().splashDidShow();
	}
	
	@Override
	protected void onPausePressed() {
		showEscDialog();
	}
	
	@Override
	public void onCompletion(Music music) {
		super.onCompletion(music);
		
		if (isMusicEnabled())
			playNextJukeboxSong(false);
	}
	
	@Override
	public void enableMusic(boolean enable) {
		super.enableMusic(enable);
		
		if (enable) {
			Music amMusic = getAM().getMusic();
			if (amMusic == null)
				playNextJukeboxSong(false);
		}
	}
	
	public void puzzleWasSelectedAtMenu(int puzzleID) {
		if (getState() == PfState.MENU && menuController != null && menuController.getState() == MenuState.MENU_IN) {
			PuzzleController puzzleController = getPuzzleController();
			if (puzzleController != null) {
				getPuzzleController().loadPuzzleByID(puzzleID);
				setState(PfState.PLAYING);
				setPuzzleDuration(0);
			}
		}
	}
	
	private void processMilestoneDialogs() {
		if (hasProcessedSolvedPuzzle || menuController == null)
			return;
		
		PuzzleController puzzleController = getPuzzleController();
		if (puzzleController != null && puzzleController.didPuzzleGetSolved()) {
			Puzzle puzzle = puzzleController.getPuzzle();
			if (puzzle != null) {
				GameProgressController gpc = GameProgressController.GPC();
				int levelIndex = puzzle.getLevelIndex();
				boolean wasLevelCompleted = puzzleController.didPuzzleGetSolved() &&
						gpc.getNumSolvedPuzzlesForLevel(levelIndex) == gpc.getNumPuzzlesPerLevel();
				
				if (gpc.getNumSolvedPuzzles() == gpc.getNumPuzzles()) {
					menuController.showPuzzleWizardDialog();
				} else if (levelIndex >= 0 && levelIndex < gpc.getNumLevels() - 1) {
					if (wasLevelCompleted) {
						menuController.showLevelCompletedDialog(levelIndex);
					} else if (puzzleController.didLevelUnlock())
						menuController.showLevelUnlockedDialog("LevelUnlocked", levelIndex+1);
				}
			}
		}
		
		hasProcessedSolvedPuzzle = true;
	}

	@Override
	public int getInputFocus() { return CMInputs.HAS_FOCUS_ALL; }

	@Override
	public void didGainFocus() { }

	@Override
	public void willLoseFocus() { }

	@Override
	public void update(CMInputs input) {
		if (input.didDepress(CMInputs.CI_PREV_SONG)) {
			if (isMusicEnabled())
				playNextJukeboxSong(false);
		} else if (input.didDepress(CMInputs.CI_NEXT_SONG)) {
			if (Promo.isPromoEnabled()) {
				Promo.setSlowAnimations(!Promo.isSlowAnimations());
			} else if (isMusicEnabled()) {
				this.playPrevJukeboxSong(false);
			}
		} else if (input.isPressed(CMInputs.CI_ALT) && input.didRaise(CMInputs.CI_CONFIRM_ENTER)) {
			setFullscreen(!isFullscreen());
		} else if (input.didDepress(CMInputs.CI_PRT_SCR)) {
			if (Promo.isPromoEnabled() && Promo.isDeviceScreenshotScaled(Promo.getDeviceType())) {
				PromoDeviceType deviceType = Promo.getDeviceType();
				int pixWidth = 1280, pixHeight = 720;
				
				switch (deviceType) {
					case Tablet_7: pixWidth = 1920; pixHeight = 1080; break;
					case Tablet_10: pixWidth = 2560; pixHeight = 1440; break;
					case iPad: pixWidth = 2048; pixHeight = 1536; break;
					case iPhone_5_5: pixWidth = 2208; pixHeight = 1242; break;
					case Mac: pixWidth = 2880; pixHeight = 1800; break;
					default: break;
				}

				float w = stage.getWidth();
				float h = stage.getHeight();
				String filename = "screenshots/screenshot";
				
				this.resize(pixWidth, pixHeight);
				ScreenshotSaver.beginHiResRender(filename, pixWidth, pixHeight);
				stage.getCamera().translate(0, h / 2, 0);
				GameController.GC().render();
				ScreenshotSaver.endHiResRender();
				
				ScreenshotSaver.beginHiResRender(filename, pixWidth, pixHeight);
				stage.getCamera().translate(w / 2, 0, 0);
				GameController.GC().render();
				ScreenshotSaver.endHiResRender();
				
				ScreenshotSaver.beginHiResRender(filename, pixWidth, pixHeight);
				stage.getCamera().translate(-w / 2, -h / 2, 0);
				GameController.GC().render();
				ScreenshotSaver.endHiResRender();
				
				ScreenshotSaver.beginHiResRender(filename, pixWidth, pixHeight);
				stage.getCamera().translate(w / 2, 0, 0);
				GameController.GC().render();
				ScreenshotSaver.endHiResRender();
				
				stage.getCamera().translate(-w / 2, 0, 0);
				this.resize(Promo.getDisplayWidth(), Promo.getDisplayHeight());
			} else {
				ScreenshotSaver.saveScreenshot("screenshots/screenshot");
			}
		}
	}
}
