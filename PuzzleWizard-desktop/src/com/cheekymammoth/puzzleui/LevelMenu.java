package com.cheekymammoth.puzzleui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.gameModes.PuzzleMode;
import com.cheekymammoth.graphics.CMAtlasSprite;
import com.cheekymammoth.graphics.CMSprite;
import com.cheekymammoth.graphics.ColoredProp;
import com.cheekymammoth.graphics.CroppedProp;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.input.CMInputs;
import com.cheekymammoth.input.IInteractable;
import com.cheekymammoth.locale.ILocalizable;
import com.cheekymammoth.locale.Localizer.LocaleType;
import com.cheekymammoth.puzzleio.GameProgressController;
import com.cheekymammoth.puzzles.Level;
import com.cheekymammoth.puzzles.Puzzle;
import com.cheekymammoth.resolution.IResDependent;
import com.cheekymammoth.sceneControllers.GameController;
import com.cheekymammoth.ui.MenuButton;
import com.cheekymammoth.ui.MenuIndicator;
import com.cheekymammoth.ui.TextMenuIcon;
import com.cheekymammoth.ui.TextUtils;
import com.cheekymammoth.ui.UIFactory;
import com.cheekymammoth.ui.TextUtils.CMFontType;
import com.cheekymammoth.utils.Coord;
import com.cheekymammoth.utils.FloatTweener;
import com.cheekymammoth.utils.CrashContext;
import com.cheekymammoth.utils.LangFX;
import com.cheekymammoth.utils.Transitions;
import com.cheekymammoth.utils.Utils;

public class LevelMenu extends Prop implements IEventListener, IInteractable, IResDependent, ILocalizable {
	private enum LevelMenuState {
		Idle, Idle2Puzzles, Idle2Levels, Levels,
		Levels2Puzzles, Puzzles, Puzzles2Levels, Puzzles2Idle
	};
	
	public static final int EV_TYPE_DID_TRANSITION_IN;
    public static final int EV_TYPE_DID_TRANSITION_OUT;
    public static final int EV_TYPE_PUZZLE_SELECTED;
    
    private static final float kLevel2PuzzleTransitionDuration = 0.3f;
    private static final float kPuzzle2LevelTransitionDuration = 0.3f;
    private static final float kPuzzle2IdleTransitionDuration = 1f;
    
    private static final float kLevelIconWidth = 2 * 248.0f;
    private static final float kLevelPadding = 2 * 32.0f;
    private static final float kLevelWidth = 3 * kLevelIconWidth + 4 * kLevelPadding;
    private static final float kLevelSeparationX = kLevelIconWidth + kLevelPadding;
    private static final float kScrollDist = kLevelWidth - kLevelPadding;
    
    private static final float kContentYScale = 2.15f;
    private static final float kLevelScrollDuration = 0.7f;
    private static final float kHelpFadeDelay = 4.0f;
    private static final float kHelpFadeDuration = 0.5f;
    
    private static final int kNumLevelsPerPage = 3;
    private static final int kMaxLevels = 12;
    
    private static final int kTweenerTagScroll = 1;
    private static final int kTweenerTagContentOpacity = 2;
    private static final int kTweenerTagDecorationsOpacity = 3;
    private static final int kTweenerTagPuzzlePageOpacity = 4;
    private static final int kTweenerTagBgOpacity = 5;
    private static final int kTweenerTagHelpOpacity = 6;
    
    //private static final int kColorSlateBlue = 0x6a5acdff;
    
    static {
    	EV_TYPE_DID_TRANSITION_IN = EventDispatcher.nextEvType();
    	EV_TYPE_DID_TRANSITION_OUT = EventDispatcher.nextEvType();
    	EV_TYPE_PUZZLE_SELECTED = EventDispatcher.nextEvType();
    }
	
	private LevelMenuState state = LevelMenuState.Levels;
    private boolean isScrolling;
    private boolean unlockedAll;
    private Coord repeatVec;
    private float repeatCounter;
    private float repeatDelay;
    private CMSprite helpSprite;
    private Label helpLabel;
    private Prop helpProp;
    private ColoredProp headerQuad;
    private Prop headerProp;
    private CMSprite footerKey;
    private Label footerLabel;
    private Label demoLabel;
    private CMSprite prevPageArrow;
    private CMSprite nextPageArrow;
    private Prop arrowsContainer;
    private Prop decorations;
    private MenuButton backButton;
    private TextMenuIcon backButtonIcon;
    private MenuIndicator backButtonIndicator;
    private Prop content;
    private Prop contentShadows;
    private CroppedProp canvas;
    private Prop globalScaler;
    
    //private FullscreenQuad bgQuad;
    private CMSprite bgQuad;
    private Prop bgProp;
    
    private int levelIndex = 0;
    private Level[] levels;
    private LevelIcon[] levelIcons;
    private Prop[] levelIconShadows;
    private Label[] levelHeaders;
    private FloatTweener contentOpacityTweener;
    private FloatTweener decorationsOpacityTweener;
    private FloatTweener puzzlePageOpacityTweener;
    private FloatTweener bgOpacityTweener;
    private FloatTweener helpOpacityTweener;
    private FloatTweener scrollTweener;
    private int puzzleIndex;
    private PuzzlePage puzzlePage;
    private int pageIndex;
    // These temporary caches are only valid while program flow remains local to a method.
    private Vector2 tempVecCacheA = new Vector2();
    private Vector2 tempVecCacheB = new Vector2();
    private Rectangle tempRectCacheA = new Rectangle();
   // private Rectangle tempRectCacheB = new Rectangle();
    
    //private ColoredProp debugProp;

	public LevelMenu(int category, Array<Level> levels) {
		super(category);
		
		int numLevels = Math.min(kMaxLevels, levels.size);
		this.levels = new Level[numLevels];
		for (int i = 0; i < numLevels; i++)
			this.levels[i] = levels.get(i).devClone();
		levelIcons = new LevelIcon[numLevels];
		levelIconShadows = new Prop[numLevels];
		levelHeaders  = new Label[numLevels];
		repeatVec = new Coord(0, 0);
		repeatDelay = 0.25f;
		setTransform(true);
		setAdvanceable(true);
		setup();
		scene.registerResDependent(this);
		scene.registerLocalizable(this);
	}
	
	private void setup() {
		float contentX = scene.VW2(), contentY = scene.VH() / kContentYScale;

//		bgQuad = new FullscreenQuad(Color.BLACK);
//		addActor(bgQuad);
		
		// Background
		Texture bgTex = scene.textureByName("bg-menu.png");
		TextureRegion bgRegion = new TextureRegion(bgTex, 0, 0, bgTex.getWidth(), bgTex.getHeight());
		
		bgQuad = new CMSprite(bgRegion);
		bgQuad.centerContent(); // .setPosition(-bgQuad.getWidth()/2, -bgQuad.getHeight()/2);
		
		bgProp = new Prop(-1);
		bgProp.setContentSize(bgQuad.getWidth(), bgQuad.getHeight());
		bgProp.addSpriteChild(bgQuad);
		bgProp.setPosition(scene.VW2(), scene.VH2());
		addActor(bgProp);
		
//		if (Promo.isPromoEnabled())
//		{
//			Prop promoProp = new Prop();
//			promoProp.setTransform(true);
//			promoProp.setSize(scene.getStage().getWidth(), scene.getStage().getHeight());
//			promoProp.setPosition(scene.VW2(), scene.VH2());
//			addActor(promoProp);
//			
//			Texture promoTex = scene.textureByName("promo-bg.png");
//			TextureRegion promoRegion = new TextureRegion(promoTex, 0, 0, promoTex.getWidth(), promoTex.getHeight());
//			
//			CMSprite promoSprite = new CMSprite(promoRegion);
//			promoSprite.setSize(scene.getStage().getWidth(), scene.getStage().getHeight());
//			promoSprite.centerContent();
//			promoProp.addSpriteChild(promoSprite);
//		}
		
		globalScaler = new Prop();
		globalScaler.setTransform(true);
		globalScaler.setOrigin(scene.VW2(), scene.VH2());
		addActor(globalScaler);
		
		decorations = new Prop();
		decorations.setTransform(true);
		globalScaler.addActor(decorations);
		
		headerQuad = new ColoredProp(640, 250);
		headerQuad.centerContent();
		headerQuad.setPosition(
				scene.VW() / 2,
				scene.VH() - (scene.VH() / 7 + headerQuad.getHeight() / 2));
		headerQuad.setVisible(false);
		decorations.addActor(headerQuad);
		
		createLevelMenuHeader();
		
		// Footer
		footerKey = new CMAtlasSprite(scene.textureRegionByName("menu-key"));
		footerKey.setScale(0.9f);
		
		footerLabel = TextUtils.create(
				"Total Progress: 0/72",
				44,
				TextUtils.kAlignCenter, //TextUtils.kAlignTop | TextUtils.kAlignCenter,
				1024,
				128,
				new Color(0xeceb74ff));
		footerLabel.setPosition(
				contentX - (footerLabel.getWidth() + 0.9f * footerKey.getWidth()) / 2,
				0.18f * scene.VH() - footerLabel.getTextBounds().height / 2);
		decorations.addActor(footerLabel);
		decorations.addSpriteChild(footerKey);
		
		// Demo Only
		if (GameController.isTrialMode()) {
			demoLabel = TextUtils.create(
					"Demo",
					52,
					TextUtils.kAlignTop | TextUtils.kAlignCenter,
					1024,
					96,
					new Color(0xd20000ff));
			demoLabel.setPosition(
					-demoLabel.getWidth() / 2,
					-0.4f * demoLabel.getHeight());
			
			Prop demoProp = new Prop();
			demoProp.setTransform(true);
			demoProp.setRotation(20);
			demoProp.setPosition(
					footerLabel.getX() + 0.75f * footerLabel.getWidth() + demoLabel.getWidth() / 2,
					footerLabel.getY() + 0.4f * footerLabel.getHeight());
			demoProp.addActor(demoLabel);
			decorations.addActor(demoProp);
		}
		
		globalScaler.setScale(calcMaxScale());
		
		contentShadows = new Prop();
		contentShadows.setTransform(true);
		
		content = new Prop();
		content.setTransform(true);
		content.addActor(contentShadows);
		
		for (int i = 0, n = levels.length; i < n; i++) {
			LevelIcon levelIcon = new LevelIcon(getCategory(), i, levels[i].getID());
			//levelIcon.setScaleX(kLevelWidth / kLevelWidthOrig);
			levelIcon.setScale(kLevelIconWidth / levelIcon.getWidth());
			//levelIcon.setPuzzlesSolvedColor(levelIcon.getColor().set(kColorSlateBlue));
			levelIcon.setPosition(
					kLevelPadding + levelIcon.getScaledWidth() / 2 +
					(scene.VW() - kLevelWidth) / 2 + i * kLevelSeparationX,
					contentY);
			levelIcons[i] = levelIcon;
			content.addActor(levelIcon);
			
			CMSprite shadowSprite = new CMAtlasSprite(scene.textureRegionByName("shadow-square"));
			shadowSprite.centerContent();
			shadowSprite.setColor(Color.BLACK);
			
			Prop levelIconShadow = new Prop();
			levelIconShadow.setTransform(true);
			levelIconShadow.setScale(
					1.2f * levelIcon.getWidth() / shadowSprite.getWidth(),
					1.15f * levelIcon.getHeight() / shadowSprite.getHeight());
			levelIconShadow.setPosition(levelIcon.getX() + 24, levelIcon.getY() - 24);
			levelIconShadow.addSpriteChild(shadowSprite);
			levelIconShadows[i] = levelIconShadow;
			contentShadows.addActor(levelIconShadow);
			
			Label levelHeaderLabel = TextUtils.createFX(PuzzleMode.kLevelNames[i], 82, PuzzleMode.kLevelColors[i]);
			levelHeaderLabel.setPosition(
					scene.VW2() - levelHeaderLabel.getTextBounds().width / 2,
					scene.VH() - (levelHeaderLabel.getHeight() + 20));
			levelHeaders[i] = levelHeaderLabel;
		}
		
		levelIcons[levelIndex].enableHighlight(true);
		
		// Limit content scaling
		canvas = new CroppedProp(getCategory(), calcCanvasViewableRegion());
		canvas.enableCrop(false);
		//canvas.addActor(content);
		globalScaler.addActor(canvas);
		
//		debugProp = new ColoredProp(canvas.getViewableRegion().width, canvas.getViewableRegion().height);
//		debugProp.centerContent();
//		debugProp.setPosition(
//	    		canvas.getViewableRegion().x + debugProp.getWidth() / 2,
//	    		canvas.getViewableRegion().y + debugProp.getHeight() / 2);
//		debugProp.setColor(Color.RED);
//		canvas.addActorBefore(content, debugProp);
		
		// Arrows
		arrowsContainer = new Prop();
		arrowsContainer.setTransform(true);
		canvas.addActor(arrowsContainer);
		canvas.addActor(content); // Place content above arrows on the fake Z-axis.
		
		prevPageArrow = new CMAtlasSprite(scene.textureRegionByName("level-arrow-prev"));
		prevPageArrow.centerContent();
		
		Prop prevPageArrowProp = new Prop();
		prevPageArrowProp.setPosition(
				contentX - (kLevelWidth + prevPageArrow.getWidth()) / 2, //(kLevelWidth / 2 + kLevelPadding + kLevelPadding / 4),
				contentY);
		prevPageArrowProp.addSpriteChild(prevPageArrow);
		arrowsContainer.addActor(prevPageArrowProp);
		
		nextPageArrow = new CMAtlasSprite(scene.textureRegionByName("level-arrow-next"));
		nextPageArrow.centerContent();
		
		Prop nextPageArrowProp = new Prop();
		nextPageArrowProp.setPosition(
				contentX + (kLevelWidth + prevPageArrow.getWidth())  / 2, //kLevelWidth / 2 + kLevelPadding + kLevelPadding / 4,
				prevPageArrowProp.getY());
		nextPageArrowProp.addSpriteChild(nextPageArrow);
		arrowsContainer.addActor(nextPageArrowProp);
		
		// Puzzle Page
		puzzlePage = new PuzzlePage();
		Color color = puzzlePage.getColor();
		puzzlePage.setColor(color.r, color.g, color.b, 0f);
		puzzlePage.setVisible(false);
		addActor(puzzlePage);
		
		backButton = UIFactory.getTextMenuButton(getCategory(), "Back", 46, CMFontType.REGULAR, true);
		backButtonIcon = (TextMenuIcon)backButton.menuIconForTag(MenuButton.kMenuIconDefaultTag);
		backButtonIcon.setBullet(new CMAtlasSprite(scene.textureRegionByName("back-button-icon")));
		backButtonIcon.setEnabledColors(0xffffffff, 0xffe611ff);
		backButtonIndicator = (MenuIndicator)backButton.childForTag(MenuButton.kMenuIndicatorDefaultTag);
		puzzlePage.addActor(backButton);
		
		scrollTweener = new FloatTweener(0, Transitions.easeOut, this);
		scrollTweener.setTag(kTweenerTagScroll);
		
		contentOpacityTweener = new FloatTweener(1f, Transitions.linear, this);
		contentOpacityTweener.setTag(kTweenerTagContentOpacity);
		decorationsOpacityTweener = new FloatTweener(1f, Transitions.linear, this);
		decorationsOpacityTweener.setTag(kTweenerTagDecorationsOpacity);
		bgOpacityTweener = new FloatTweener(1f, Transitions.linear, this);
		bgOpacityTweener.setTag(kTweenerTagBgOpacity);
		puzzlePageOpacityTweener = new FloatTweener(0, Transitions.linear, this);
		puzzlePageOpacityTweener.setTag(kTweenerTagPuzzlePageOpacity);
		
		helpLabel = TextUtils.create(" ", 44, TextUtils.kAlignCenter, 1680, TextUtils.getCapHeight(44));
		helpLabel.setX(-helpLabel.getWidth() / 2);
		helpSprite = new CMAtlasSprite(scene.textureRegionByName("menu-key"));
		helpSprite.setX(helpLabel.getX() + 696f);
		helpSprite.setScale(0.9f);
		
		helpProp = new Prop();
		helpProp.setTransform(true);
		helpProp.addActor(helpLabel);
		helpProp.addSpriteChild(helpSprite);
		helpProp.setPosition(
				scene.VW2(),
				headerQuad.getY() - (headerQuad.getHeight() + helpLabel.getTextBounds().height) / 2);
		color = helpProp.getColor();
		helpProp.setColor(color.r, color.g, color.b, 0f);
		helpProp.setVisible(false);
		decorations.addActor(helpProp);
		
		helpOpacityTweener = new FloatTweener(helpProp.getColor().a, Transitions.linear, this);
		helpOpacityTweener.setTag(kTweenerTagHelpOpacity);		
		
		refreshArrowVisibility();
        refreshLevelLocks(-1);
        refreshLevelsSolved(-1);
        refreshPuzzlesSolved();
        refreshLevelSpriteVisibility();
        refreshLevelSpriteOpacity();
        repositionBackButton();
        resolutionDidChange((int)scene.getStage().getWidth(), (int)scene.getStage().getHeight());
	}
	
	private void createLevelMenuHeader() {
//		if (headerProp != null) {
//			headerProp.remove();
//			scene.getTM().unloadTexture(headerProp.getName());
//			headerProp = null;
//		}
//		
//		headerProp = LangFX.getLevelMenuHeader(scene);
//		headerProp.setPosition(
//				headerQuad.getX(),
//				headerQuad.getY() - 0.4f * headerQuad.getHeight());
//		decorations.addActor(headerProp);
//		layoutLevelMenuHeader();
		
		if (headerProp != null) {
			headerProp.remove();
			scene.getTM().unloadTexture(headerProp.getName());
			headerProp = null;
		}
		
		headerProp = LangFX.getLevelMenuHeader(scene);
		headerProp.setPosition(
				headerQuad.getX() + 5, // +5 because logo shadow "un-centers" the image.
				headerQuad.getY() - headerQuad.getHeight());
		decorations.addActor(headerProp);
		layoutLevelMenuHeader();
	}
	
	private void layoutLevelMenuHeader() {
//		headerProp.setScale(1f);
//
//		if (headerProp.getWidth() != 0 && headerProp.getHeight() != 0) {
//			float maxWidth = 0.75f * kLevelWidth, maxHeight = 320;
//			int[] headerSize = LangFX.getLevelMenuHeaderSize();
//			float maxScale = Math.min(1f, Math.min(maxWidth / headerSize[0], maxHeight / headerSize[1]));
//			headerProp.setScale(maxScale);
//			
//			float viewAspect = scene.getViewAspectRatio();
//			float screenAspect = scene.getViewportAspectRatio();
//			float deltaAspect = screenAspect - viewAspect;
//			float aspectFactor = 1f + Math.max(0f, deltaAspect < 0 ? 0.2f : 0.2f - deltaAspect);
//			headerProp.setScale(aspectFactor * headerProp.getScaleX());
//		}
	}
	
	private float calcMaxScale() {
		float minY = Math.min(
				footerLabel.getY() - footerLabel.getTextBounds().height,
				headerQuad.getY() - headerQuad.getHeight() / 2);
		float maxY = Math.max(
				footerLabel.getY(),
				headerQuad.getY() + headerQuad.getHeight() / 2);
		float maxScale = Math.min(
				scene.getMaximizingContentScaleFactor(),
				0.9f * scene.VH() / Math.max(1, (maxY - minY)));
		return maxScale;
	}
	
	private Rectangle canvasVRCache = new Rectangle();
	private Rectangle calcCanvasViewableRegion() {
		float btmLeftX = levelIcons[0].getX() - (levelIcons[0].getScaledWidth() / 2 + kLevelPadding);
		float btmLeftY = levelIcons[0].getY() - (levelIcons[0].getScaledHeight() / 2 + kLevelPadding);
		float topRightX = levelIcons[2].getX() + (levelIcons[2].getScaledWidth() / 2 + kLevelPadding);
		float topRightY = levelIcons[2].getY() + levelIcons[2].getScaledHeight() / 2 + kLevelPadding;
		return canvasVRCache.set(btmLeftX, btmLeftY, topRightX - btmLeftX,  topRightY - btmLeftY);
		
//		float btmLeftX = levelIcons[1].getX() - (kLevelWidth / 2 + kLevelPadding / 4);
//		float btmLeftY = levelIcons[0].getY() - (levelIcons[0].getHeight() / 2 + kLevelPadding);
//		float topRightX = levelIcons[1].getX() + kLevelWidth / 2 + kLevelPadding / 4;
//		float topRightY = levelIcons[2].getY() + levelIcons[2].getHeight() / 2 + kLevelPadding;
//		return canvasVRCache.set(btmLeftX, btmLeftY, topRightX - btmLeftX,  topRightY - btmLeftY);
	}
	
	private void repositionBackButton() {
		backButton.updateBounds();
		CMSprite bullet = backButtonIcon.getBullet();
		backButton.setPosition(
				scene.VW2() - ((bullet == null ? 72 : bullet.getWidth() / 2) + backButton.getWidth() / 2),
				0.085f * scene.VH()); //0.1f * scene.VH());
		backButtonIcon.setBulletOffset(
				LangFX.getLevelMenuBackOffsets()[0],
				LangFX.getLevelMenuBackOffsets()[1]);
		backButtonIndicator.setOffset(
				LangFX.getLevelMenuBackOffsets()[2],
				LangFX.getLevelMenuBackOffsets()[3]);
	}
	
	public void refresh() {
		refreshPuzzleLocks();
		refreshPuzzlesSolved();
		refreshLevelLocks(-1);
		refreshLevelsSolved(-1);
	}
	
	private void refreshArrowVisibility() {
		Color color = prevPageArrow.getColor();
		prevPageArrow.setColor(Utils.setA(color, (getPageIndex() != 0 ? 1f : 0.4f)));
		color = nextPageArrow.getColor();
		nextPageArrow.setColor(Utils.setA(color, (getPageIndex() != Math.max(0, getNumPages()-1) ? 1f : 0.4f)));
	}
	
	private Rectangle levelSpriteViewableRectCache = new Rectangle();
	private Rectangle getLevelSpriteViewableRect(LevelIcon icon) {
		Vector2 btmLeft = tempVecCacheA, topRight = tempVecCacheB;
		
		// Project icon bounds to stage coords
		Rectangle iconBounds = tempRectCacheA;
		iconBounds.set(
				icon.getX(),
				icon.getY(),
				icon.getWidth(),
				icon.getHeight());
		
		btmLeft.set(-iconBounds.width / 2, -iconBounds.height / 2);
		icon.localToStageCoordinates(btmLeft);
		topRight.set(iconBounds.width / 2, iconBounds.height / 2);
		icon.localToStageCoordinates(topRight);
		iconBounds.set(btmLeft.x, btmLeft.y, topRight.x - btmLeft.x, topRight.y - btmLeft.y);
		
		// Project view bounds to stage coords
		Rectangle viewBounds = canvas.getViewableRegion();
		btmLeft.set(viewBounds.x, viewBounds.y);
		canvas.localToStageCoordinates(btmLeft);
		topRight.set(viewBounds.x + viewBounds.width, viewBounds.y + viewBounds.height);
		canvas.localToStageCoordinates(topRight);
		levelSpriteViewableRectCache.set(btmLeft.x, btmLeft.y, topRight.x - btmLeft.x, topRight.y - btmLeft.y);
		
		levelSpriteViewableRectCache = Utils.intersectionRect(iconBounds, levelSpriteViewableRectCache);
		return levelSpriteViewableRectCache;
	}
	
	private void refreshLevelSpriteVisibility() {
//		for (int i = 0, n = levelIcons.length; i < n; i++) {
//            if (isScrolling()) {
//                // 0,1,2,3    2,3,4,5,6    5,6,7,8,9    8,9,10,11
//                levelIcons[i].setVisible(
//                		getLevelIndex() >= (1 + i / kNumLevelsPerPage) * kNumLevelsPerPage - (1 + kNumLevelsPerPage)
//                    && getLevelIndex() <= (1 + i / kNumLevelsPerPage) * kNumLevelsPerPage);
//            } else
//                levelIcons[i].setVisible(i / kNumLevelsPerPage == getLevelIndex() / kNumLevelsPerPage);
//            levelIconShadows[i].setVisible(levelIcons[i].isVisible());
//        }
		
		for (int i = 0, n = levelIcons.length; i < n; i++) {
			Rectangle intersection = getLevelSpriteViewableRect(levelIcons[i]);
			levelIcons[i].setVisible(intersection.width > 1.0f && intersection.height > 1.0f);
			levelIconShadows[i].setVisible(levelIcons[i].isVisible());
		}
	}
	
	private void refreshLevelSpriteOpacity() {
		for (int i = 0, n = levelIcons.length; i < n; i++) {
			Rectangle intersection = getLevelSpriteViewableRect(levelIcons[i]);
			float opacity = Transitions.linear.apply(intersection.width <= 1.0f
					? 0
					: Math.min(1.0f, intersection.width / levelIcons[i].getWidth()));
			
			Color color = levelIcons[i].getColor();
			levelIcons[i].setColor(Utils.setA(color, opacity));
			color = levelIconShadows[i].getColor();
			levelIconShadows[i].setColor(Utils.setA(color, opacity));
		}
	}
	
	private void refreshLevelLocks(int levelIndex) {
		if (levelIndex == -1) {
            for (int i = 0, n = levelIcons.length; i < n; i++)
                levelIcons[i].setLocked(!isLevelUnlocked(i));
        }
        else if (levelIndex >= 0 && levelIndex < levelIcons.length)
            levelIcons[levelIndex].setLocked(!isLevelUnlocked(levelIndex));
	}
	
	private void refreshLevelsSolved(int levelIndex) {
		GameProgressController gpc = GameProgressController.GPC();

        if (levelIndex == -1) {
            for (int i = 0, n = levelIcons.length; i < n; i++)
                levelIcons[i].setPuzzlesSolved(gpc.getNumSolvedPuzzlesForLevel(i), PuzzlePage.kNumPuzzlesPerPage);
        }
        else if (levelIndex >= 0 && levelIndex < levelIcons.length)
            levelIcons[levelIndex].setPuzzlesSolved(
            		gpc.getNumSolvedPuzzlesForLevel(levelIndex), PuzzlePage.kNumPuzzlesPerPage);
	}
	
	private void refreshPuzzleLocks() {
		for (int i = 0; i < PuzzlePage.kNumPuzzlesPerPage; i++)
            puzzlePage.setLockedAtIndex(i, !isPuzzleUnlocked(getLevelIndex(), i));
	}
	
	private void refreshPuzzlesSolved() {
		GameProgressController gpc = GameProgressController.GPC();
        for (int i = 0; i < PuzzlePage.kNumPuzzlesPerPage; i++)
            puzzlePage.setSolvedAtIndex(i, gpc.hasSolved(getLevelIndex(), i));

        LocaleType locale = scene.getLocale();
        String spacer = locale == LocaleType.CN || locale == LocaleType.JP ? "" : " ";
        footerLabel.setText(
        		scene.localize("Total Progress:") + spacer + gpc.getNumSolvedPuzzles() +
        		"/" + gpc.getNumPuzzles());
        footerKey.setPosition(
				footerLabel.getX() + footerLabel.getWidth() / 2 + footerLabel.getTextBounds().width / 2
				+ LangFX.getLevelMenuProgressKeyOffset()[0],
				footerLabel.getY() + LangFX.getLevelMenuProgressKeyOffset()[1]);
	}
	
	private void populatePuzzlePage(Array<Puzzle> puzzles) {
		if (puzzles != null) {
            puzzlePage.clear();
            for (int i = 0; i < puzzles.size; i++)
                puzzlePage.setPuzzleAtIndex(i, puzzles.get(i));
        }
	}
	
	public int getSelectedPuzzleID() {
		Puzzle puzzle = getSelectedPuzzle();
		return puzzle != null ? puzzle.getID() : -1;
	}
	
	public int getNumLevels() {
		return levels != null ? levels.length : 0;
	}
	
	private int getNumPages() { return getNumLevels() / kNumLevelsPerPage; }
	
	public Level getSelectedLevel() {
		return levels[getLevelIndex()];
	}
	
	private String getLevelName(int levelIndex) {
		if (levelIndex >= 0 && levelIndex < getNumLevels())
			return PuzzleMode.kLevelNames[levelIndex];
		else
			return null;
	}
	
	public Puzzle getSelectedPuzzle() {
		 Level level = getSelectedLevel();
         Array<Puzzle> puzzles = level != null ? level.getPuzzles() : null;
         if (puzzles != null && puzzles.size > puzzleIndex)
             return puzzles.get(puzzleIndex);
         else
             return null;
	}
	
	public int getLevelIndex() { return levelIndex; }
	
	public void setLevelIndex(int value) {
		if (getLevelIndex() != value && value >= 0 && value < levelIcons.length) {
			levelIcons[levelIndex].enableHighlight(false);
			levelIndex = value;
			refreshArrowVisibility();
			levelIcons[levelIndex].enableHighlight(true);
			setPageIndex(levelIndex / kNumLevelsPerPage);
        }
	}
	
	public int getPuzzleIndex() { return puzzleIndex; }
	
	public void setPuzzleIndex(int value) {
		Level level = getSelectedLevel();
		if (level != null) {
			Array<Puzzle> puzzles = level.getPuzzles();
			if (puzzles != null) {
				highlightPuzzle(puzzleIndex, false);
				puzzleIndex = Math.max(0, Math.min(level.getPuzzles().size-1, value));
				
				if (!isBackButtonHighlighted())
					highlightPuzzle(puzzleIndex, true);
			}
		}
	}
	
	public void refreshColorScheme() {
		puzzlePage.refreshColorScheme();
	}
	
	public void jumpToLevelIndex(int levelIndex, int puzzleIndex) {
		if (levelIndex < 0 || levelIndex >= getNumLevels() || puzzleIndex < 0 ||
				puzzleIndex >= PuzzlePage.kNumPuzzlesPerPage)
            return;

		setLevelIndex(levelIndex);
        scrollTweener.resetTween(0);
        setScrolling(false);
        setPageIndex(getLevelIndex() / kNumLevelsPerPage);
        setContentX(-pageIndex * kScrollDist);
        populatePuzzlePage(getSelectedLevel().getPuzzles());
        setPuzzleIndex(puzzleIndex);
        puzzlePage.setHeaderLabel(levelHeaders[getLevelIndex()]);
        Color levelColor = PuzzleMode.kLevelColors[getLevelIndex()];
        puzzlePage.setHighlightColor(levelColor);
        backButtonIcon.setSelectedColors(Color.rgba8888(levelColor), Color.rgba8888(levelColor));
        refreshPuzzleLocks();
        refreshPuzzlesSolved();
        refreshArrowVisibility();
	}
	
	public void returnToLevelMenu() {
		if (getState() == LevelMenuState.Levels)
			dispatchEvent(EV_TYPE_DID_TRANSITION_IN);
		if (getState() != LevelMenuState.Idle && getState() != LevelMenuState.Idle2Puzzles)
			return;
		
		float duration = kLevel2PuzzleTransitionDuration + kPuzzle2LevelTransitionDuration;
		setState(LevelMenuState.Idle2Levels);
		bgOpacityTweener.resetTween(bgQuad.getColor().a, 1f, (1f - bgQuad.getColor().a) * duration, 0);
		contentOpacityTweener.resetTween(content.getColor().a, 1f, (1f - content.getColor().a) * duration, 0);
		decorationsOpacityTweener.resetTween(decorations.getColor().a, 1f, (1f - decorations.getColor().a) * duration, 0);
	}
	
	public void showOverTime(float duration) {
		if (getState() == LevelMenuState.Levels)
			dispatchEvent(EV_TYPE_DID_TRANSITION_IN, this);
		if (getState() != LevelMenuState.Idle)
			return;
		
		setState(LevelMenuState.Idle2Puzzles);
		bgOpacityTweener.resetTween(bgQuad.getColor().a, 1f, (1f - bgQuad.getColor().a) * duration, 0);
		puzzlePageOpacityTweener.resetTween(puzzlePage.getColor().a, 1f, (1f - puzzlePage.getColor().a) * duration, 0);
	}
	
	public void hideOverTime(float duration) {
		if (getState() == LevelMenuState.Idle)
			dispatchEvent(EV_TYPE_DID_TRANSITION_OUT, this);
		if (getState() != LevelMenuState.Puzzles)
			return;
		
		setState(LevelMenuState.Puzzles2Idle);
		bgOpacityTweener.resetTween(bgQuad.getColor().a, 0f, bgQuad.getColor().a * duration, 0);
		puzzlePageOpacityTweener.resetTween(puzzlePage.getColor().a, 0f, 0.4f * puzzlePage.getColor().a * duration, 0);
	}
	
	public void hideInstantaneously() {
		helpOpacityTweener.resetTween(0);
		contentOpacityTweener.resetTween(0);
		decorationsOpacityTweener.resetTween(0);
		puzzlePageOpacityTweener.resetTween(0);
		bgOpacityTweener.resetTween(0);
		
		Color color = content.getColor();
		content.setColor(color.r, color.g, color.b, 0);
		color = arrowsContainer.getColor();
		arrowsContainer.setColor(color.r, color.g, color.b, 0);
		color = decorations.getColor();
		decorations.setColor(color.r, color.g, color.b, 0);
		color = bgQuad.getColor();
		bgQuad.setColor(color.r, color.g, color.b, 0);
		
		setState(LevelMenuState.Idle);
	}

	@Override
	public void localeDidChange(String fontKey, String FXFontKey) {
		if (footerLabel != null) {
            footerLabel.setText("");
            TextUtils.swapFont(fontKey, footerLabel, false);
            refreshPuzzlesSolved();
        }
		
		if (demoLabel != null)
			TextUtils.swapFont(fontKey, demoLabel, true);
		
		if (helpLabel != null) {
			helpLabel.setText("");
            TextUtils.swapFont(fontKey, helpLabel, false);
            helpOpacityTweener.resetTween(0);
            helpProp.setVisible(false);
		}
		
		createLevelMenuHeader();
		
		for (int i = 0, n = levelIcons.length; i < n; i++)
			levelIcons[i].localeDidChange(fontKey, FXFontKey);
		
		for (int i = 0, n = levelHeaders.length; i < n; i++) {
			Label levelHeader = levelHeaders[i];
			TextUtils.swapFont(FXFontKey, levelHeader, true);
			levelHeader.setPosition(
					scene.VW2() - levelHeader.getTextBounds().width / 2,
					scene.VH() - (levelHeader.getHeight() + 20));
		}
		
		if (puzzlePage != null)
			puzzlePage.localeDidChange(fontKey, FXFontKey);
		
		if (getSelectedLevel() != null)
			populatePuzzlePage(getSelectedLevel().getPuzzles());
		
		if (backButton != null) {
			backButton.localeDidChange(fontKey, FXFontKey);
			repositionBackButton();
		}
	}

	@Override
	public void resolutionDidChange(int width, int height) {
		globalScaler.setScale(calcMaxScale());
		layoutLevelMenuHeader();
		
		//bgQuad.resolutionDidChange(width, height);
	    bgProp.setSize(scene.getStage().getWidth(), scene.getStage().getHeight());
	    bgQuad.setSize(scene.getStage().getWidth(), scene.getStage().getHeight());
	    bgQuad.setU2(scene.VPW() / bgQuad.getTexture().getWidth());
	    bgQuad.setV2(scene.VPH() / bgQuad.getTexture().getHeight());
	    bgQuad.setPosition(-bgQuad.getWidth()/2, -bgQuad.getHeight()/2);
	    
	    canvas.setViewableRegion(calcCanvasViewableRegion());
	    
//	    debugProp.remove();
//	    debugProp = new ColoredProp(canvas.getViewableRegion().width, canvas.getViewableRegion().height);
//	    debugProp.centerContent();
//	    debugProp.setPosition(
//	    		canvas.getViewableRegion().x + debugProp.getWidth() / 2,
//	    		canvas.getViewableRegion().y + debugProp.getHeight() / 2);
//		debugProp.setColor(Color.RED);
//		canvas.addActorBefore(content, debugProp);
		
	    refreshLevelSpriteVisibility();
        refreshLevelSpriteOpacity();

		puzzlePage.resolutionDidChange();
	}
	
	private LevelMenuState getState() { return state; }
	
	private void setState(LevelMenuState value) {
		if (value == state)
			return;
		
		GameProgressController gpc = GameProgressController.GPC();
		
		// Clean up previous state
		switch (state) {
			case Idle:
				break;
			case Idle2Puzzles:
				break;
			case Idle2Levels:
				break;
			case Levels:
				break;
			case Levels2Puzzles:
				break;
			case Puzzles:
				break;
			case Puzzles2Levels:
				break;
			case Puzzles2Idle:
				break;
		}
		
		// Apply new state
		state = value;
		
		switch (state) {
			case Idle:
				setVisible(false);
				break;
			case Idle2Puzzles:
				CrashContext.setContext("None", CrashContext.CONTEXT_PUZZLE_NAME);
				CrashContext.setContext(getLevelName(getLevelIndex()), CrashContext.CONTEXT_LEVEL_NAME);
				CrashContext.setContext("PuzzleMenu", CrashContext.CONTEXT_GAME_STATE);
				
				setVisible(true);
				puzzlePage.setVisible(true);
				refreshPuzzleLocks();
				refreshPuzzlesSolved();
				refreshLevelLocks(-1);
				refreshLevelsSolved(-1);
				gpc.save();
				break;
			case Idle2Levels:
			{
				CrashContext.setContext("None", CrashContext.CONTEXT_PUZZLE_NAME);
				CrashContext.setContext("LevelMenu", CrashContext.CONTEXT_GAME_STATE);
				
				Color color = puzzlePage.getColor();
				puzzlePage.setColor(color.r, color.g, color.b, 0f);
				puzzlePageOpacityTweener.resetTween(puzzlePage.getColor().a);
				
				setVisible(true);
				content.setVisible(true);
				decorations.setVisible(true);
				helpProp.setVisible(false);
				puzzlePage.setVisible(false);
				
				refreshPuzzleLocks();
				refreshPuzzlesSolved();
				refreshLevelLocks(-1);
				refreshLevelsSolved(-1);
				gpc.save();
			}
				break;
			case Levels:
				puzzlePage.setVisible(false);
				refreshLevelSpriteOpacity();
				break;
			case Levels2Puzzles:
			{
				CrashContext.setContext(getLevelName(getLevelIndex()), CrashContext.CONTEXT_LEVEL_NAME);
				CrashContext.setContext("PuzzleMenu", CrashContext.CONTEXT_GAME_STATE);
				
				enableBackButtonHighlight(false);
				setPuzzleIndex(0);
				setContentX(-pageIndex * kScrollDist);
				puzzlePage.setHeaderLabel(levelHeaders[getLevelIndex()]);
				Color levelColor = PuzzleMode.kLevelColors[getLevelIndex()];
				puzzlePage.setHighlightColor(levelColor);
		        backButtonIcon.setSelectedColors(Color.rgba8888(levelColor), Color.rgba8888(levelColor));
		        puzzlePage.setVisible(true);
		        refreshPuzzleLocks();
		        refreshPuzzlesSolved();
			}
				break;
			case Puzzles:
				backButton.resetButton();
				content.setVisible(false);
				decorations.setVisible(false);
				helpProp.setVisible(false);
				break;
			case Puzzles2Levels:
				CrashContext.setContext("LevelMenu", CrashContext.CONTEXT_GAME_STATE);
				
				scrollTweener.resetTween(0);
				setPageIndex(getLevelIndex() / kNumLevelsPerPage);
				setContentX(-pageIndex * kScrollDist);
				content.setVisible(true);
				decorations.setVisible(true);
				break;
			case Puzzles2Idle:
				break;
		}
	}
	
	private boolean isScrolling() { return isScrolling; }
	
	private void setScrolling(boolean value) {
		isScrolling = value;
		refreshLevelSpriteVisibility();
		refreshLevelSpriteOpacity();
	}
	
	private Coord getRepeatVec() { return repeatVec; }
	
	private void setRepeatVec(int x, int y) {
		if (repeatVec.x != x || repeatVec.y != y) {
			repeatVec.set(x, y);
			repeatCounter = 2 * repeatDelay;
		}
	}
	
//	private float getRepeatCounter() { return repeatCounter; }
//	
//	private void setRepeatCounter(float value) { repeatCounter = value; }
//	
//	private float getRepeatDelay() { return repeatDelay; }
//	
//	private void setRepeatDelay(float value) { repeatDelay = value; }
	
	private boolean isBackButtonHighlighted() {
		return backButton.isSelected();
	}
	
	private void enableBackButtonHighlight(boolean enable) {
		backButton.setSelected(enable);
		backButtonIndicator.setVisible(enable);
	}
	
	private int getPageIndex() { return pageIndex; }
	
	private void setPageIndex(int index) {
		int prevPageIndex = pageIndex;
		pageIndex = Math.max(0, Math.min(getNumPages()-1, index));
		if (prevPageIndex != pageIndex)
			scrollContentTo(-pageIndex * kScrollDist);
	}
	
//	private void scrollContentBy(float x) {
//		scrollContentTo(content.getX() + x);
//	}
	
	private void scrollContentTo(float x) {
		scrollTweener.resetTween(
				content.getX(),
				x,
				Math.max(0.1f, Math.min(kLevelScrollDuration, kLevelScrollDuration *
						Math.abs(x - content.getX() / kScrollDist))),
				0);
		setScrolling(true);
	}
	
	private void setContentX(float x) {
		content.setX(Math.min(0, Math.max(x, -(kMaxLevels - kNumLevelsPerPage) * kLevelSeparationX)));
		refreshArrowVisibility();
		refreshLevelSpriteVisibility();
		refreshLevelSpriteOpacity();
	}
	
	public boolean getUnlockedAll() { return unlockedAll; }
	
	public void setUnlockedAll(boolean value) {
		unlockedAll = value;
		refreshLevelLocks(-1);
		refreshPuzzleLocks();
	}
	
	private boolean isLevelUnlocked(int levelIndex) {
		return getUnlockedAll() || GameProgressController.GPC().isLevelUnlocked(levelIndex);
	}
	
	private boolean isPuzzleUnlocked(int levelIndex, int puzzleIndex) {
		return getUnlockedAll() || GameProgressController.GPC().isPuzzleUnlocked(levelIndex, puzzleIndex);
	}
	
	private void showHelpUnlock(int levelIndex) {
		if (levelIndex <= 0 || levelIndex >= kMaxLevels || getState() != LevelMenuState.Levels)
	        return;
		
		helpProp.setScale(1f);
		String msg = null;
		if (levelIndex == getNumLevels()-1) {
			msg = scene.localize("Unlock with") + " " + 66;
			helpLabel.setText(msg);
			helpSprite.setX(helpLabel.getX() + (helpLabel.getWidth() - helpLabel.getTextBounds().width) / 2
					+ helpLabel.getTextBounds().width);
		} else {
			msg = scene.localize("Unlock with") + " 3";
			helpLabel.setText(msg);
			float spriteOffsetX = helpLabel.getTextBounds().width;
			msg = msg + LangFX.getLevelMenuHelpUnlockPaddingString() + scene.localize("in ");
			msg = msg + scene.localize(PuzzleMode.kLevelNames[levelIndex-1]);
			helpLabel.setText(msg);
			helpSprite.setX(helpLabel.getX() + (helpLabel.getWidth() - helpLabel.getTextBounds().width) / 2
					+ spriteOffsetX);
		}
		
		helpSprite.setY(helpLabel.getY() + LangFX.getLevelMenuHelpUnlockYOffsets()[1]);
		
		helpLabel.setColor(PuzzleMode.kLevelColors[levelIndex]);
		helpProp.setY(headerQuad.getY() - (0.5f * headerQuad.getHeight() + 0.75f * helpLabel.getTextBounds().height));
		
		Color color = helpProp.getColor();
		helpProp.setColor(color.r, color.g, color.b, 1f);
		helpProp.setVisible(true);
		helpOpacityTweener.resetTween(helpProp.getColor().a, 0, kHelpFadeDuration, kHelpFadeDelay);
		
		float maxHelpWidth = 1260;
		if (helpLabel.getTextBounds().width > maxHelpWidth)
			helpProp.setScale(maxHelpWidth / helpLabel.getTextBounds().width); 
	}
	
	private void highlightPuzzle(int index, boolean enable) {
		puzzlePage.highlightPuzzle(index % PuzzlePage.kNumPuzzlesPerPage, enable);
	}
	
	private void selectCurrentLevel() {
		if (isScrolling() || getState() != LevelMenuState.Levels)
            return;

        if (!isLevelUnlocked(getLevelIndex())) {
            showHelpUnlock(getLevelIndex());
            scene.playSound("locked");
            
            if (GameController.isTrialMode() && GameProgressController.GPC().isTrialModeCompleted())
				scene.showBuyNowDialog();
        } else {
            scene.playSound("button");
            populatePuzzlePage(getSelectedLevel().getPuzzles());
            transitionLevels2Puzzles(kLevel2PuzzleTransitionDuration);
        }
	}
	
	private void selectCurrentPuzzle() {
		if (isScrolling() || getState() != LevelMenuState.Puzzles)
            return;

        if (!isPuzzleUnlocked(getLevelIndex(), getPuzzleIndex()))
            scene.playSound("locked");
        else {
            scene.playSound("button");
            transitionPuzzles2Idle(kPuzzle2IdleTransitionDuration);
            dispatchEvent(EV_TYPE_PUZZLE_SELECTED, this);
        }
	}
	
	private void transitionLevels2Puzzles(float duration) {
		if (getState() != LevelMenuState.Levels)
			return;
		setState(LevelMenuState.Levels2Puzzles);
		
		contentOpacityTweener.resetTween(content.getColor().a, 0f, content.getColor().a * duration, 0);
		decorationsOpacityTweener.resetTween(decorations.getColor().a, 0, decorations.getColor().a * duration, 0);
		puzzlePageOpacityTweener.resetTween(puzzlePage.getColor().a, 1f, (1f - puzzlePage.getColor().a) * duration, 0);
	}
	
	private void transitionPuzzles2Levels(float duration) {
		if (getState() != LevelMenuState.Puzzles)
			return;
		setState(LevelMenuState.Puzzles2Levels);
		
		contentOpacityTweener.resetTween(content.getColor().a, 1f, (1f - content.getColor().a) * duration, 0);
		decorationsOpacityTweener.resetTween(decorations.getColor().a, 1f, (1f - decorations.getColor().a) * duration, 0);
		puzzlePageOpacityTweener.resetTween(puzzlePage.getColor().a, 0, puzzlePage.getColor().a * duration, 0);
	}
	
	private void transitionPuzzles2Idle(float duration) {
		hideOverTime(duration);
	}
	
	@Override
	public int getInputFocus() {
		return CMInputs.HAS_FOCUS_MENU;
	}

	@Override
	public void didGainFocus() { }

	@Override
	public void willLoseFocus() { }
	
	@Override
	public void onEvent(int evType, Object evData) {
		if (evType == FloatTweener.EV_TYPE_FLOAT_TWEENER_CHANGED) {
			FloatTweener tweener = (FloatTweener)evData;
			if (tweener != null) {
				int tag = tweener.getTag();
				float tweenedValue = tweener.getTweenedValue();
				switch (tag) {
					case kTweenerTagScroll:
						setContentX(tweenedValue);
						break;
					case kTweenerTagContentOpacity:
					{
						Color color = content.getColor();
						content.setColor(color.r, color.g, color.b, tweenedValue);
						color = arrowsContainer.getColor();
						arrowsContainer.setColor(color.r, color.g, color.b, tweenedValue);
					}
						break;
					case kTweenerTagDecorationsOpacity:
					{
						Color color = decorations.getColor();
						decorations.setColor(color.r, color.g, color.b, tweenedValue);
					}
						break;
					case kTweenerTagPuzzlePageOpacity:
					{
						Color color = puzzlePage.getColor();
						puzzlePage.setColor(color.r, color.g, color.b, tweenedValue);
					}
						break;
					case kTweenerTagBgOpacity:
					{
						Color color = bgQuad.getColor();
						bgQuad.setColor(color.r, color.g, color.b, tweenedValue);
					}
						break;
					case kTweenerTagHelpOpacity:
					{
						Color color = helpProp.getColor();
						helpProp.setColor(color.r, color.g, color.b, tweenedValue);
					}
						break;
				}
			}
		} else if (evType == FloatTweener.EV_TYPE_FLOAT_TWEENER_COMPLETED) {
			FloatTweener tweener = (FloatTweener)evData;
			if (tweener != null) {
				int tag = tweener.getTag();
				switch (tag) {
					case kTweenerTagScroll:
						setScrolling(false);
						break;
					case kTweenerTagPuzzlePageOpacity:
						if (getState() == LevelMenuState.Levels2Puzzles)
							setState(LevelMenuState.Puzzles);
						else if (getState() == LevelMenuState.Puzzles2Levels)
							setState(LevelMenuState.Levels);
						break;
					case kTweenerTagBgOpacity:
						if (getState() == LevelMenuState.Idle2Puzzles) {
							setState(LevelMenuState.Puzzles);
							dispatchEvent(EV_TYPE_DID_TRANSITION_IN, this);
						} else if (getState() == LevelMenuState.Idle2Levels) {
							setState(LevelMenuState.Levels);
							dispatchEvent(EV_TYPE_DID_TRANSITION_IN, this);
						} else if (getState() == LevelMenuState.Puzzles2Idle) {
							setState(LevelMenuState.Idle);
							dispatchEvent(EV_TYPE_DID_TRANSITION_OUT, this);
						}
						break;
					case kTweenerTagHelpOpacity:
						helpProp.setVisible(false);
						break;
				}
			}
		} else if (evType == MenuButton.EV_TYPE_RAISED) {
			MenuButton button = (MenuButton)evData;
			if (button != null && button == backButton) {
				if (getState() == LevelMenuState.Puzzles)
					transitionPuzzles2Levels(kPuzzle2LevelTransitionDuration);
			}
		}
	}

	@Override
	public void update(CMInputs input) {
		if (isScrolling() || getState() == LevelMenuState.Idle)
			return;
		
		// Poll for input
		boolean didSelect = false, didGoBack = false;
		Coord depressedVec = input.getDepressedVector();
		if (depressedVec.x == 0 && depressedVec.y == 0)
			setRepeatVec(input.getHeldVector().x, input.getHeldVector().y);
		else
			setRepeatVec(0, 0);
		
		didSelect = input.didDepress(CMInputs.CI_CONFIRM);
		if (!didSelect)
			didGoBack = input.didDepress(CMInputs.CI_CANCEL);
		
		// Process polled input
		if (getState() == LevelMenuState.Levels) {
			if (didSelect) {
//				if (!isLevelUnlocked(getLevelIndex())) {
//					showHelpUnlock(getLevelIndex());
//					scene.playSound("locked");
//				} else
//					selectCurrentLevel();
				selectCurrentLevel();
			} else
				processNavInput(depressedVec);
		} else if (getState() == LevelMenuState.Puzzles) {
			if (didSelect) {
				if (isBackButtonHighlighted())
					transitionPuzzles2Levels(kPuzzle2LevelTransitionDuration);
				else
					selectCurrentPuzzle();
			} else if (didGoBack)
				transitionPuzzles2Levels(kPuzzle2LevelTransitionDuration);
			else
				processNavInput(depressedVec);
		}
	}
	
	private Coord navVec = new Coord();
	private void processNavInput(Coord moveVec) {
		navVec.set(moveVec);
		
		if (getState() == LevelMenuState.Levels) {
			 if (navVec.x == -1) {
				 if (getLevelIndex() > 0)
					 setLevelIndex(getLevelIndex()-1);
			 } else if (navVec.x == 1) {
				 if (getLevelIndex() < levels.length-1)
					 setLevelIndex(getLevelIndex()+1);
			 }
		} else if (getState() == LevelMenuState.Puzzles) {
			PuzzlePage page = puzzlePage;
			int row = getPuzzleIndex() / PuzzlePage.kNumPuzzlesPerRow;
			int column = getPuzzleIndex() % PuzzlePage.kNumPuzzlesPerRow;
			
			if (isBackButtonHighlighted())
				navVec.x = 0;
			
			if (navVec.x == -1) {
				int index = row * PuzzlePage.kNumPuzzlesPerRow + (column + (PuzzlePage.kNumPuzzlesPerRow - 1)) %
						PuzzlePage.kNumPuzzlesPerRow;
                if (index >= page.getNumPuzzles())
                    index = Math.min(page.getNumPuzzles() - 1, row * PuzzlePage.kNumPuzzlesPerRow +
                    		(PuzzlePage.kNumPuzzlesPerRow - 1));
                setPuzzleIndex(index);
			} else if (navVec.x == 1) {
				int index = row * PuzzlePage.kNumPuzzlesPerRow + (column + 1) % PuzzlePage.kNumPuzzlesPerRow;
                if (index >= page.getNumPuzzles())
                    index = row * PuzzlePage.kNumPuzzlesPerRow;
                setPuzzleIndex(index);
			} else if (navVec.y != 0) {
				boolean wasBackButtonHighlighted = isBackButtonHighlighted();
                enableBackButtonHighlight(false);

                if (wasBackButtonHighlighted) {
                    int index = (getPuzzleIndex() + PuzzlePage.kNumPuzzlesPerRow) % PuzzlePage.kNumPuzzlesPerPage;
                    if (index >= page.getNumPuzzles())
                        index = getPuzzleIndex();

                    if (moveVec.y == 1)
                        index = index % PuzzlePage.kNumPuzzlesPerRow;
                    else
                        index = index % PuzzlePage.kNumPuzzlesPerRow + PuzzlePage.kNumPuzzlesPerRow;

                    setPuzzleIndex(index);
                } else {
                    if ((moveVec.y == -1 && getPuzzleIndex() % PuzzlePage.kNumPuzzlesPerPage < PuzzlePage.kNumPuzzlesPerRow)
                        || (moveVec.y == 1 && getPuzzleIndex() % PuzzlePage.kNumPuzzlesPerPage >= PuzzlePage.kNumPuzzlesPerRow))
                    {
                        enableBackButtonHighlight(true);
                        setPuzzleIndex(getPuzzleIndex());
                    }
                    else
                    {
                        int index = (getPuzzleIndex() + PuzzlePage.kNumPuzzlesPerRow) % PuzzlePage.kNumPuzzlesPerPage;
                        if (index >= page.getNumPuzzles())
                            index = getPuzzleIndex();
                        setPuzzleIndex(index);
                    }
                }
			}
		}
	}
	
//	private float testCounter = 0f; 
//	private int totalProgressTest = 0;
	
	@Override
	public void advanceTime(float dt) {
//		testCounter += dt;
//		if (testCounter >= 5.0f) {
//			testCounter -= 1.0f;
//			totalProgressTest = Math.min(totalProgressTest + 1, 72);
//			refreshPuzzlesSolved();
//		}
		
		scrollTweener.advanceTime(dt);
		
		if (isScrolling())
			refreshLevelSpriteOpacity();
		
		contentOpacityTweener.advanceTime(dt);
		decorationsOpacityTweener.advanceTime(dt);
		puzzlePageOpacityTweener.advanceTime(dt);
		bgOpacityTweener.advanceTime(dt);
		helpOpacityTweener.advanceTime(dt);
		
		if (!isScrolling()) {
			Coord repeatVec = getRepeatVec();
			if (repeatVec.x != 0 || repeatVec.y != 0) {
				repeatCounter -= dt;
				if (repeatCounter <= 0) {
					repeatCounter = repeatDelay;
					processNavInput(repeatVec);
				}
			}
		}
	}
}
