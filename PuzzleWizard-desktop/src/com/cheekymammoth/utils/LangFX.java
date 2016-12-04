package com.cheekymammoth.utils;

import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.math.Vector2;
import com.cheekymammoth.graphics.CMSprite;
import com.cheekymammoth.graphics.GfxMode.AlphaMode;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.locale.Localizer;
import com.cheekymammoth.locale.Localizer.LocaleType;
import com.cheekymammoth.puzzleui.PuzzlePageSettings;
import com.cheekymammoth.sceneControllers.SceneController;

public class LangFX {
	private LangFX() { }
	
	private static final int[][] kSplashLogoOffsets = new int[][] {
		new int[] { 0	, 96 },		// EN
		new int[] { 0	, 80 },		// CN
		new int[] { 0	, 80 },		// DE
		new int[] { 0	, 80 },		// ES
		new int[] { 0	, 80 },		// FR
		new int[] { 0	, 80 },		// IT
		new int[] { 0	, 80 },		// JP
		new int[] { 0	, 80 }		// KR
	};
	public static int[] getSplashLogoOffset() {
		return kSplashLogoOffsets[Localizer.getLocaleIndex()];
	}
	
	private static final int[][] kSplashTextPuzzleOffsets = new int[][] {
		new int[] { 6	, -236 },		// EN
		new int[] { 0	, -350 },		// CN
		new int[] { 0	, -230 },		// DE
		new int[] { 0	, -250 },		// ES
		new int[] { 0	, -200 },		// FR
		new int[] { 0	, -230 },		// IT
		new int[] { 0	, -232 },		// JP
		new int[] { 0	, -360 }		// KR
	};
	public static int[] getSplashTextPuzzleOffset() {
		return kSplashTextPuzzleOffsets[Localizer.getLocaleIndex()];
	}
	
	private static final int[][] kSplashTextWizardOffsets = new int[][] {
		new int[] { -4	, -360 },		// EN
		new int[] { 0	, -360 },		// CN
		new int[] { 0	, -360 },		// DE
		new int[] { 0	, -380 },		// ES
		new int[] { 0	, -360 },		// FR
		new int[] { 0	, -360 },		// IT
		new int[] { 0	, -376 },		// JP
		new int[] { 0	, -360 }		// KR
	};
	public static int[] getSplashTextWizardOffset() {
		return kSplashTextWizardOffsets[Localizer.getLocaleIndex()];
	}
	
	private static final int[][] kSplashTextIqOffsets = new int[][] {
		new int[] { 0	, -524 },		// EN
		new int[] { 0	, -524 },		// CN
		new int[] { 0	, -524 },		// DE
		new int[] { 0	, -524 },		// ES
		new int[] { 0	, -524 },		// FR
		new int[] { 0	, -524 },		// IT
		new int[] { 0	, -524 },		// JP
		new int[] { 0	, -524 }		// KR
	};
	public static int[] getSplashTextIqOffset() {
		return kSplashTextIqOffsets[Localizer.getLocaleIndex()];
	}
	
	private static final int[][] kPlayerHUDSettings = new int[][] {
		// { LabelYOffset, BgQuadWidth }
		new int[] { 0	, 2*500 },		// EN
		new int[] { -4	, 2*330 },		// CN
		new int[] { 0	, 2*510 },		// DE
		new int[] { 0	, 2*580 },		// ES
		new int[] { 0	, 2*630 },		// FR
		new int[] { 0	, 2*460 },		// IT
		new int[] { 6	, 2*430 },		// JP
		new int[] { 14	, 2*380 }		// KR
	};
	public static int[] getPlayerHUDSettings() {
		return kPlayerHUDSettings[Localizer.getLocaleIndex()];
	}
	
	private static final int[][] kTitleTextOffsets = new int[][] {
		new int[] { 2*40	, 2*0 },		// EN
		new int[] { 2*48 	, 2*65 },		// CN
		new int[] { 2*25	, 2*20 },		// DE
		new int[] { 2*125	, 2*2 },		// ES
		new int[] { 2*22	, 2*1 },		// FR
		new int[] { 2*125	, 2*2 },		// IT
		new int[] { 2*58	, 2*10 },		// JP
		new int[] { 2*26	, 2*71 }		// KR
	};
	public static int[] getTitleTextOffset() {
		return kTitleTextOffsets[Localizer.getLocaleIndex()];
	}
	
	private static final int[][] kLevelIconYOffsets = new int[][] {
		// { Pinstripe, Label }
		new int[] { 2*84	, 423 },		// EN
		new int[] { 2*84 	, 435 },		// CN
		new int[] { 2*84	, 423 },		// DE
		new int[] { 2*84	, 423 },		// ES
		new int[] { 2*84	, 423 },		// FR
		new int[] { 2*84	, 423 },		// IT
		new int[] { 2*84	, 435 },		// JP
		new int[] { 2*84	, 421 }		    // KR
	};
	public static int[] getLevelIconYOffsets() {
		return kLevelIconYOffsets[Localizer.getLocaleIndex()];
	}
	
	private static final int[][] kLevelIconSolvedOffsets = new int[][] {
		// { Text X, Text Y, Key y, Text Width }
		new int[] { -4, 4, 2, 167 },		// EN
		new int[] { -4, 0, 6, 150 },		// CN
		new int[] { -4, 4, 2, 167 },		// DE
		new int[] { -4, 4, 2, 167 },		// ES
		new int[] { -4, 4, 2, 167 },		// FR
		new int[] { -4, 4, 2, 167 },		// IT
		new int[] { -4, 6, 0, 150 },		// JP
		new int[] { -4, 10, -4, 157 }		// KR
	};
	public static int[] getLevelIconSolvedOffsets() {
		return kLevelIconSolvedOffsets[Localizer.getLocaleIndex()];
	}
	
	private static final int[][] kActiveIconYOffsets = new int[][] {
		// { Letters, Numerals }
		new int[] { -3		, 15 },			// EN
		new int[] { 0 		, 18 },			// CN
		new int[] { -3		, 15 },			// DE
		new int[] { -3		, 15 },			// ES
		new int[] { -3		, 15 },			// FR
		new int[] { -3		, 15 },			// IT
		new int[] { 0		, 14 },			// JP
		new int[] { -6		, 0 }			// KR
	};
	public static int getActiveIconYOffset() {
		return getActiveIconYOffset(' ');
	}
	
	public static int getActiveIconYOffset(char firstChar) {
		int offsetY = kActiveIconYOffsets[Localizer.getLocaleIndex()][0];
		if (firstChar >= '0' && firstChar <= '9')
			offsetY += kActiveIconYOffsets[Localizer.getLocaleIndex()][1];
		return offsetY;
	}
	
	public static PuzzlePageSettings getPuzzlePageSettings() {
		PuzzlePageSettings settings = new PuzzlePageSettings();
		settings.puzzleHorizSeparation = 2 * 372f; //2 * 332f;
        settings.puzzleVertSeparation = 2 * 262f; // 2 * 256f;
        settings.puzzleBoardWidth = getMaxPuzzlePageBoardDimensions().x;
        settings.puzzleBoardHeight = getMaxPuzzlePageBoardDimensions().y;
        settings.puzzleEntryYOffset = 2 * 324;
        settings.headerImageYOffsetFactor = 1.15f;
        settings.backButtonYOffsetFactor = 0.875f;

//        LocaleType locale = Localizer.getLocale();
//        if (locale == LocaleType.EN || locale == LocaleType.CN || locale == LocaleType.KR)
//        {
//            settings.puzzleHorizSeparation = 2 * 392f;
//            settings.puzzleVertSeparation = 2 * 290f;
//            settings.puzzleBoardWidth = 2 * 300f;
//            settings.puzzleBoardHeight = 2 * 196f;
//            settings.puzzleEntryYOffset = 2 * 140;
//            settings.headerImageYOffsetFactor = 1.15f;
//            settings.backButtonYOffsetFactor = 0.875f;
//        }
//        else
//        {
//            settings.puzzleHorizSeparation = 2 * 392f;
//            settings.puzzleVertSeparation = 2 * 280f;
//            settings.puzzleBoardWidth = 2 * 276f;
//            settings.puzzleBoardHeight = 2 * 180f;
//            settings.puzzleEntryYOffset = 2 * 132;
//            settings.headerImageYOffsetFactor = 1.1f;
//            settings.backButtonYOffsetFactor = 0.95f;
//        }

        return settings;
	}
	
	private static final Vector2 kMaxPuzzlePageBoardDimensions = new Vector2(2 * 254, 2 * 166);
	public static Vector2 getMaxPuzzlePageBoardDimensions() { return kMaxPuzzlePageBoardDimensions; }
	
	public static Prop getLevelMenuHeader(SceneController scene) {
		String headerTexKey = "lang/title-" + Localizer.locale2String(scene.getLocale()) + ".png";
		Texture headerTex = scene.textureByName(headerTexKey);
		if (headerTex == null) {
			TextureParameter texParam = new TextureParameter();
			texParam.genMipMaps = true;
			texParam.minFilter = TextureFilter.MipMapLinearNearest;
			texParam.magFilter = TextureFilter.Linear;
			texParam.wrapU = TextureWrap.ClampToEdge;
			texParam.wrapV = TextureWrap.ClampToEdge;
			scene.getTM().loadTexture(headerTexKey, texParam, false);
			headerTex = scene.textureByName(headerTexKey);
		}

		CMSprite headerSprite = new CMSprite(headerTex);
		headerSprite.setAlphaMode(AlphaMode.POST_MULTIPLIED);
		headerSprite.setPosition(
				-headerSprite.getWidth() / 2,
				(getLevelMenuHeaderSize()[1] - headerSprite.getHeight()) / 2);
		
		Prop headerProp = new Prop();
		headerProp.setTransform(true);
		headerProp.setName(headerTexKey); // So we can unload the texture later
		headerProp.setSize(headerSprite.getWidth(), headerSprite.getHeight());
		headerProp.addSpriteChild(headerSprite);
		
		return headerProp;
	}
	
	private static final int[][] kLevelMenuHeaderSizes = new int[][] {
		new int[] { 2*512	, 2*256 },		// EN
		new int[] { 2*512 	, 2*264 },		// CN
		new int[] { 2*512	, 2*256 },		// DE
		new int[] { 2*512	, 2*232 },		// ES
		new int[] { 2*512	, 2*240 },		// FR
		new int[] { 2*512	, 2*244 },		// IT
		new int[] { 2*512	, 2*238 },		// JP
		new int[] { 2*512	, 2*240 }		// KR
	};
	public static int[] getLevelMenuHeaderSize() {
		return kLevelMenuHeaderSizes[Localizer.getLocaleIndex()];
	}
	
	private static final String[] kLevelMenuHelpUnlockPaddingStrings = new String[] {
		 "           "     // EN
        ,"   "             // CN
        ,"           "     // DE
        ,"           "     // ES
        ,"           "     // FR
        ,"           "     // IT
        ,"    "            // JP
        ,"      "          // KR
	};
	public static String getLevelMenuHelpUnlockPaddingString() {
		return kLevelMenuHelpUnlockPaddingStrings[Localizer.getLocaleIndex()];
	}
	
	private static final int[][] kLevelMenuHelpUnlockYOffsets = new int[][] {
		 // { Container (incl text), Image }
		new int[] { 2*-4	, 2*-16 },		// EN
		new int[] { 2*-40 	, 2*-21 },		// CN
		new int[] { 2*-4	, 2*-16 },		// DE
		new int[] { 2*-4	, 2*-16 },		// ES
		new int[] { 2*-4	, 2*-16 },		// FR
		new int[] { 2*-4	, 2*-16 },		// IT
		new int[] { 2*-28	, 2*-21 },		// JP
		new int[] { 2*-36	, 2*-22 }		// KR
	};
	public static int[] getLevelMenuHelpUnlockYOffsets() {
		return kLevelMenuHelpUnlockYOffsets[Localizer.getLocaleIndex()];
	}
	
	private static final int[][] kLevelMenuProgressKeyOffsets = new int[][] {
		// { X, Y }
		new int[] { 0	, -10 },		// EN
		new int[] { 0 	, -8 },			// CN
		new int[] { 0	, -10 },		// DE
		new int[] { 0	, -10 },		// ES
		new int[] { 0	, -10 },		// FR
		new int[] { 0	, -10 },		// IT
		new int[] { 0	, -14 },		// JP
		new int[] { 0	, -22 }			// KR
	};
	public static int[] getLevelMenuProgressKeyOffset() {
		return kLevelMenuProgressKeyOffsets[Localizer.getLocaleIndex()];
	}
	
	private static final int[][] kLevelMenuBackOffsets = new int[][] {
		// { bulletX, bulletY, indicatorX, indicatorY }
		new int[] { 0	, 0,	0,	0 },		// EN
		new int[] { 54 	, 0,	54,	0 },		// CN
		new int[] { 0	, 0,	0,	0 },		// DE
		new int[] { 0	, 0,	0,	0 },		// ES
		new int[] { 0	, 0,	0,	0 },		// FR
		new int[] { 0	, 0,	0,	0 },		// IT
		new int[] { 50	, -6,	50,	0 },		// JP
		new int[] { 34	, -10,	34,	0 }			// KR
	};
	public static int[] getLevelMenuBackOffsets() {
		return kLevelMenuBackOffsets[Localizer.getLocaleIndex()];
	}
	
	private static final int[] kPuzzleEntryLabelYOffsets = new int[] {
		0,		// EN
		-20,	// CN
		0,		// DE
		0,		// ES
		0,		// FR
		0,		// IT
		-10,	// JP
		12		// KR
	};
	public static int getPuzzleEntryLabelYOffset() {
		return kPuzzleEntryLabelYOffsets[Localizer.getLocaleIndex()];
	}
	
	private static final int[] kCustomDialogKeyYOffsets = new int[] {
		-6,		// EN
		-6,		// CN
		-6,		// DE
		-6,		// ES
		-6,		// FR
		-6,		// IT
		-8,		// JP
		-16		// KR
	};
	public static int getCustomDialogKeyYOffset() {
		return kCustomDialogKeyYOffsets[Localizer.getLocaleIndex()];
	}
	
	private static final int[] kCustomDialogStarYOffsets = new int[] {
		0,		// EN
		0,		// CN
		0,		// DE
		0,		// ES
		0,		// FR
		0,		// IT
		0,		// JP
		0		// KR
	};
	public static int getCustomDialogStarYOffset() {
		return kCustomDialogStarYOffsets[Localizer.getLocaleIndex()];
	}
	
	private static final int[][] kLevelCompletedLabelOffsets = new int[][] {
		// { Upper Y, Lower Y }
		new int[] { -8	, 0 },		// EN
		new int[] { -20	, -2 },		// CN
		new int[] { -8	, 0 },		// DE
		new int[] { -8	, 0 },		// ES
		new int[] { -8	, 0 },		// FR
		new int[] { -8	, 0 },		// IT
		new int[] { 0	, 0 },		// JP
		new int[] { -20	, -4 }		// KR
	};
	public static int[] getLevelCompletedLabelOffset() {
		return kLevelCompletedLabelOffsets[Localizer.getLocaleIndex()];
	}
	
	public static String locale2IQString(LocaleType locale, boolean uppercase) {
		String iqString;
		
		switch (locale) {
			case FR:
			case IT:
				iqString = "qi";
				break;
			case ES:
				iqString = "ci";
				break;
			default:
				iqString = "iq";
				break;
		}
		
		return uppercase ? iqString.toUpperCase() : iqString;
	}
	
	public static String locale2PuzzleTexSuffix(LocaleType locale) {
		String texString;
		
		switch (locale) {
			case IT:
			case JP:
			case ES:
			case CN:
			case KR:
				texString = Localizer.locale2String(locale);
				break;
			default:
				texString = "EN";
				break;
		}
		
		return texString;
	}
	
	public static String locale2WizardTexSuffix(LocaleType locale) {
		String texString;
		
		switch (locale) {
			case ES:
			case IT:
			case DE:
			case FR:
			case JP:
				texString = Localizer.locale2String(locale);
				break;
			case CN:
			case KR:
				texString = null;
				break;
			default:
				texString = "EN";
				break;
		}
		
		return texString;
	}
	
	// "Next Unsolved Puzzle" & "Level Menu"
	private static final int[] kMenuDialogButtonOffsets = new int[] {
		0,		// EN
		-16,	// CN
		0,		// DE
		0,		// ES
		0,		// FR
		0,		// IT
		-12,	// JP
		-12		// KR
	};
	public static float getMenuDialogButtonOffset() {
		return kMenuDialogButtonOffsets[Localizer.getLocaleIndex()];
	}
	
	private static final int[][] kPuzzleRibbonLabelOffsets = new int[][] {
		// { Upper, Lower }
		new int[] { -28	, -166 },		// EN
		new int[] { -32	, -162 },		// CN
		new int[] { -28	, -166 },		// DE
		new int[] { -28	, -166 },		// ES
		new int[] { -28	, -166 },		// FR
		new int[] { -28	, -166 },		// IT
		new int[] { -20	, -160 },		// JP
		new int[] { -20	, -160 }		// KR
	};
	public static int[] getPuzzleRibbonLabelOffset() {
		return kPuzzleRibbonLabelOffsets[Localizer.getLocaleIndex()];
	}
	
	private static final int[][][] kLevelCompletedStarXOffsets = new int[][][] {
		// EN
		new int[][] {
				// { leftX, rightX }
				new int[] { -26, 32 },	// First Steps
				new int[] { -10, 26 },	// Color Swap
				new int[] { -6, 50 },	// Color Shield
				new int[] { -6, 42 },	// Conveyor Belt
				new int[] { -26, 38 },	// Rotator
				new int[] { -6, 52 },	// Color Flood
				new int[] { -30, 36 },	// White Tile
				new int[] { -28, 34 },	// Mirror Image
				new int[] { -6, 50 },	// Color Swirl
				new int[] { -28, 30 },	// Tile Swap
				new int[] { -8, 28 },	// Color Magic
				new int[] { -28, 50 }	// Wizard
		},
		// CN
		new int[][] {
				// { leftX, rightX }
				new int[] { 0, 10 },     // First Steps  
				new int[] { 0, 10 },     // Color Swap   
				new int[] { 0, 14 },     // Color Shield 
				new int[] { 4, 16 },     // Conveyor Belt
				new int[] { 0, 22 },     // Rotator      
				new int[] { 0, 18 },     // Color Flood  
				new int[] { 4, 6 },     // White Tile   
				new int[] { -8, 12 },     // Mirror Image 
				new int[] { 0, 22 },     // Color Swirl  
				new int[] { -10, 10 },     // Tile Swap    
				new int[] { 0, 6 },     // Color Magic  
				new int[] { 4, 28 }      // Wizard       
		},
		//DE
		new int[][] {
				// { leftX, rightX }
				new int[] { -28, 36 },  // First Steps  
				new int[] { -30, 36 },  // Color Swap   
				new int[] { -30, 54 },  // Color Shield 
				new int[] { -30, 54 },  // Conveyor Belt
				new int[] { -30, 54 },  // Rotator      
				new int[] { -30, 46 },  // Color Flood  
				new int[] { -32, 54 },  // White Tile   
				new int[] { -18, 54 },  // Mirror Image 
				new int[] { -30, 54 },  // Color Swirl  
				new int[] { -30, 36 },  // Tile Swap    
				new int[] { -30, 38 },  // Color Magic  
				new int[] { -32, 38 }   // Wizard       
		},
		// ES
		new int[][] {
				// { leftX, rightX }
				new int[] { -30, 34 },  // First Steps  
				new int[] { -12, 38 },  // Color Swap   
				new int[] { -28, 38 },   // Color Shield 
				new int[] { -12, 36 },   // Conveyor Belt
				new int[] { -26, 30 },  // Rotator      
				new int[] { -30, 38 },   // Color Flood  
				new int[] { -26, 34 },  // White Tile   
				new int[] { -34, 38 },  // Mirror Image 
				new int[] { -34, 42 },   // Color Swirl  
				new int[] { -26, 32 },  // Tile Swap    
				new int[] { -32, 38 },   // Color Magic  
				new int[] { -32, 36 }   // Wizard       
		},
		// FR
		new int[][] {
				// { leftX, rightX }
				new int[] { -36, 32 },     // First Steps  
				new int[] { -30, 38 },     // Color Swap   
				new int[] { -30, 38 },     // Color Shield 
				new int[] { -32, 52 },     // Conveyor Belt
				new int[] { -36, 52 },     // Rotator      
				new int[] { -34, 34 },     // Color Flood  
				new int[] { -10, 36 },     // White Tile   
				new int[] { -36, 40 },     // Mirror Image 
				new int[] { -36, 34 },     // Color Swirl  
				new int[] { -32, 42 },     // Tile Swap    
				new int[] { -30, 32 },     // Color Magic  
				new int[] { -30, 32 }      // Wizard       
		},
		// IT
		new int[][] {
				// { leftX, rightX }
				new int[] { -36, 60},     // First Steps  
				new int[] { -20, 36 },     // Color Swap   
				new int[] { -12, 36 },     // Color Shield 
				new int[] { -32, 54 },     // Conveyor Belt
				new int[] { -32, 36 },     // Rotator      
				new int[] { -14, 48 },     // Color Flood  
				new int[] { -34, 48 },     // White Tile   
				new int[] { -36, 48 },     // Mirror Image 
				new int[] { -42, 58 },     // Color Swirl  
				new int[] { -22, 36 },     // Tile Swap    
				new int[] { -32, 48 },     // Color Magic  
				new int[] { -32, 36 }      // Wizard       
		},
		// JP
		new int[][] {
				// { leftX, rightX }
				new int[] { 0, 42 },     // First Steps  
				new int[] { 0, 42 },     // Color Swap   
				new int[] { 0, 28 },     // Color Shield 
				new int[] { 0, 0 },     // Conveyor Belt
				new int[] { 0, 0 },     // Rotator      
				new int[] { 0, 28 },     // Color Flood  
				new int[] { 0, 0 },     // White Tile   
				new int[] { 0, 28 },     // Mirror Image 
				new int[] { 0, 0 },     // Color Swirl  
				new int[] { 0, 42 },     // Tile Swap    
				new int[] { 0, 0 },     // Color Magic  
				new int[] { 0, 28 }      // Wizard       
		},
		// KR
		new int[][] {
				// { leftX, rightX }
				new int[] { -10, 24 },     // First Steps  
				new int[] { -20, 24 },     // Color Swap   
				new int[] { -24, 40 },     // Color Shield 
				new int[] { -16, 20 },     // Conveyor Belt
				new int[] { -16, 44 },     // Rotator      
				new int[] { -20, 22 },     // Color Flood  
				new int[] { -16, 40 },     // White Tile   
				new int[] { -12, 42 },     // Mirror Image 
				new int[] { -22, 42 },     // Color Swirl  
				new int[] { -22, 26 },     // Tile Swap    
				new int[] { -22, 40 },     // Color Magic  
				new int[] { -12, 22 }      // Wizard       
		}
	};
	public static int[][] getLevelCompletedStarXOffsets() {
		return kLevelCompletedStarXOffsets[Localizer.getLocaleIndex()];
	}
}
