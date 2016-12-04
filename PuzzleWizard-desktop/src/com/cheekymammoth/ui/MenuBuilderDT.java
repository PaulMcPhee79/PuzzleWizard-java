package com.cheekymammoth.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.graphics.CMSprite;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.graphics.Prop9;
import com.cheekymammoth.graphics.GfxMode.AlphaMode;
import com.cheekymammoth.input.CMInputs;
import com.cheekymammoth.locale.Localizer;
import com.cheekymammoth.locale.Localizer.LocaleType;
import com.cheekymammoth.sceneControllers.SceneController;
import com.cheekymammoth.ui.INavigable.NavigationMap;
import com.cheekymammoth.ui.TextUtils.CMFontType;
import com.cheekymammoth.ui.UILayout.UILayouter;
import com.cheekymammoth.utils.LangFX;

public class MenuBuilderDT extends MenuBuilder {
	private static final float kMenuVertSeparation = 128f;
	private static final float kDefaultDialogScaleFactor = 1.15f;
	private static final float kDefaultVertPadding = 40f;
	private static final float kDefaultVertSeparation = 12f;
	private static final float kDefaultIndicatorScale = 1.15f;
	private static final int kCreditsFontSize = 40;
	
	private static final int kUIDPlayfieldEsc = 1000;
	private static final int kUIDDisplay = 1001;
	private static final int kUIDCredits = 1002;
	private static final int kUIDTrialMenuEsc = 1003;
	private static final int kUIDTrialPlayfieldEsc = 1004;
	private static final int kUIDBuyNow = 1005;
	
	private static UILayouter s_CustomLayouter = new UILayouter() {
		@Override
		public void layout(UILayout layout) {
			if (layout == null)
				return;
			
			switch (layout.getLayoutID()) {
				case kUIDPlayfieldEsc:
				{
					float width = 840;
					Array<Actor> layoutItems = layout.getLayoutItems();
					if (layoutItems != null && layoutItems.size > 0) {
						switch (Localizer.getLocale()) {
							case DE: width = 1000; break;
							case FR: width = 960; break;
							case JP: width = 960; break;
							default: break;
						}
						
						Actor layoutItem = layoutItems.get(0);
						if (layoutItem instanceof Prop9) {
							Prop9 prop = (Prop9)layoutItem;
							prop.setSize(width, prop.getHeight());
							prop.centerPatch();
						}
					}
				}
					break;
				case kUIDDisplay:
				{
					float width = 760;
					Array<Actor> layoutItems = layout.getLayoutItems();
					if (layoutItems != null && layoutItems.size > 0) {
						switch (Localizer.getLocale()) {
							case ES: width = 900; break;
							case IT: width = 880; break;
							case JP: width = 920; break;
							default: break;
						}
						
						Actor layoutItem = layoutItems.get(0);
						if (layoutItem instanceof Prop9) {
							Prop9 prop = (Prop9)layoutItem;
							prop.setSize(width, prop.getHeight());
							prop.centerPatch();
						}
					}
				}
					break;
				case kUIDCredits:
				{
					float width = 1200;
					Array<Actor> layoutItems = layout.getLayoutItems();
					if (layoutItems != null && layoutItems.size >= 3) {
						switch (Localizer.getLocale()) {
							case DE: width = 1500; break;
							case ES: width = 1300; break;
							case FR: width = 1360; break;
							case IT: width = 1600; break;
							case JP: width = 1380; break;
							case KR: width = 1360; break;
							default: break;
						}
						
						int itemIndex = 0;
						Actor layoutItem = layoutItems.get(itemIndex++);
						if (layoutItem instanceof CreditsDialog) {
							CreditsDialog dialog = (CreditsDialog)layoutItem;
							layoutItem = layoutItems.get(itemIndex++);
							if (layoutItem instanceof Prop9) {
								Prop9 bg = (Prop9)layoutItem;
								bg.setSize(width, bg.getHeight());
								bg.centerPatch();
								
								dialog.setScrollBounds(new Rectangle(
										bg.getX() - bg.getWidth() / 2,
										bg.getY() - 0.25f * bg.getHeight(),
										bg.getWidth(),
										0.7125f * bg.getHeight()));
								
								layoutItem = layoutItems.get(itemIndex);
								if (layoutItem instanceof Prop) {
									Prop wizardProp = (Prop)layoutItem;
									wizardProp.setPosition(
											bg.getWidth() / 2 - (wizardProp.getWidth() + 40),
											-wizardProp.getHeight());
								}
							}
						}
					}
				}
					break;
				case kUIDTrialMenuEsc:
				{
					int itemIndex = 0;
					Array<Actor> layoutItems = layout.getLayoutItems();
					if (layoutItems != null && layoutItems.size >= 4) {
						Actor layoutItem = layoutItems.get(itemIndex++);
						if (layoutItem instanceof MenuDialog) {
							MenuDialog dialog = (MenuDialog)layoutItem;
							LocaleType locale = Localizer.getLocale();

							float width = 1360;
							switch (locale) {
								case EN:
								case KR:
									width = 1200;
									break;
								case CN:
									width = 1280;
									break;
								case FR:
									width = 1300;
									break;
								case ES:
									width = 1440;
									break;
								default:
									break;
							}
							
							layoutItem = layoutItems.get(itemIndex++);
							if (layoutItem instanceof Prop9) {
								Prop9 bg = (Prop9)layoutItem;
								bg.setSize(width, bg.getHeight());
								bg.centerPatch();
							
								layoutItem = layoutItems.get(itemIndex++);
								if (layoutItem instanceof Prop) {
									Prop gamebox = (Prop)layoutItem;
									gamebox.setX(width / 2 - (gamebox.getWidth() + 70));
									
									layoutItem = layoutItems.get(itemIndex);
									dialog.removeCustomLayoutItem(layoutItem);
									layoutItem.remove();
									
									Prop titleProp = getBuyNowTitleProp(locale, gamebox);
									gamebox.addActor(titleProp);
									dialog.insertCustomLayoutItem(itemIndex, titleProp);
									
									float xOffset = gamebox.getX() -
											(width - (gamebox.getWidth() + 100)) / 2;
									for (++itemIndex; itemIndex < layoutItems.size; itemIndex++) {
										layoutItem = layoutItems.get(itemIndex);
										layoutItem.setX(xOffset);
									}
								}
							}
						}
					}
				}
					break;
				case kUIDTrialPlayfieldEsc:
				{
					int itemIndex = 0;
					Array<Actor> layoutItems = layout.getLayoutItems();
					if (layoutItems != null && layoutItems.size >= 4) {
						Actor layoutItem = layoutItems.get(itemIndex++);
						if (layoutItem instanceof MenuDialog) {
							MenuDialog dialog = (MenuDialog)layoutItem;
							LocaleType locale = Localizer.getLocale();

							float width = 1280;
							switch (locale) {
								case ES:
								case IT:
									width = 1480;
									break;
								case FR:
								case DE:
								case JP:
									width = 1560;
									break;
								default:
									break;
							}
							
							layoutItem = layoutItems.get(itemIndex++);
							if (layoutItem instanceof Prop9) {
								Prop9 bg = (Prop9)layoutItem;
								bg.setSize(width, bg.getHeight());
								bg.centerPatch();
							
								layoutItem = layoutItems.get(itemIndex++);
								if (layoutItem instanceof Prop) {
									Prop gamebox = (Prop)layoutItem;
									gamebox.setX(width / 2 - (gamebox.getWidth() + 70));
									
									layoutItem = layoutItems.get(itemIndex);
									dialog.removeCustomLayoutItem(layoutItem);
									layoutItem.remove();
									
									Prop titleProp = getBuyNowTitleProp(locale, gamebox);
									gamebox.addActor(titleProp);
									dialog.insertCustomLayoutItem(itemIndex, titleProp);
									
									float xOffset = gamebox.getX() -
											(width - (gamebox.getWidth() + 100)) / 2;
									for (++itemIndex; itemIndex < layoutItems.size; itemIndex++) {
										layoutItem = layoutItems.get(itemIndex);
										layoutItem.setX(xOffset);
									}
								}
							}
						}
					}
				}
					break;
				case kUIDBuyNow:
				{
					Array<Actor> layoutItems = layout.getLayoutItems();
					do {
						if (layoutItems == null || layoutItems.size < 5)
							break;
						if (!(layoutItems.get(0) instanceof Prop9 &&
								layoutItems.get(1) instanceof Label &&
								layoutItems.get(2) instanceof Prop &&
								layoutItems.get(3) instanceof MenuItem &&
								layoutItems.get(4) instanceof MenuItem))
							break;
						
						int itemIndex = 0;
						Prop9 bg = (Prop9)layoutItems.get(itemIndex++);
						Label caption = (Label)layoutItems.get(itemIndex++);
						Prop wizardProp = (Prop)layoutItems.get(itemIndex++);
						MenuItem resume = (MenuItem)layoutItems.get(itemIndex++);
						MenuItem buyNow = (MenuItem)layoutItems.get(itemIndex);		
						
						float bgWidth = 1700, bgHeight = 800;
						float captionY = kEnLblHeight / 2 + 300;
						float menuItemX = 260, menuItemY = 280;
						float wizardInsetX = 32;
						
						LocaleType locale = Localizer.getLocale();
						switch (locale) {
							case EN:
								bgWidth = 1620;
								break;
							case CN:
								bgWidth = 1640;
								bgHeight = 600;
								captionY = kEnLblHeight / 2 + 140;
								menuItemY = 240;
								break;
							case DE:
								menuItemX = 300;
								wizardInsetX = -16;
								break;
							case FR:
								wizardInsetX = 72;
								break;
							case IT:
								bgWidth = 1800;
								menuItemX = 300;
								break;
							case JP:
								wizardInsetX = -20;
								break;
							case ES:
								bgWidth = 1800;
								menuItemX = 280;
								break;
							case KR:
								bgHeight = 600;
								captionY = kEnLblHeight / 2 + 140;
								menuItemY = 240;
								break;
							default:
								break;
						}
						
						bg.setSize(bgWidth, bgHeight);
						bg.centerPatch();
						
						caption.setY(bg.getHeight() / 2 - captionY);
						
						wizardProp.setPosition(
								bg.getWidth() / 2 - (wizardProp.getWidth() + wizardInsetX),
								-bg.getHeight() / 2);
						
						resume.setPosition(-menuItemX, caption.getY() - menuItemY);
						buyNow.setPosition(menuItemX, caption.getY() - menuItemY);
					} while (false);
				}
					break;
				default:
					break;
			}
		}
	};
	
	private static MenuItem createMenuItem(String text, int tag) {
		return createMenuItem(text, tag, kMenuFontSize);
	}
	
	private static TextMenuItem createMenuItem(String text, int tag, int fontSize) {
		TextMenuItem item = new TextMenuItem(-1, text, fontSize, TextUtils.kAlignCenter, CMFontType.REGULAR);
		item.setTag(tag);
		item.setTextCentered(true);
		//item.setTouchable(Touchable.enabled);
		item.setIndicatorScale(kDefaultIndicatorScale);
		item.setEnabledColors(kMenuEnableColor, kMenuWhiteColor);
		item.setSelectedColors(kMenuSelectedColor, kMenuWhiteColor);
		item.setPressedColors(kMenuPressedColor, kMenuWhiteColor);
		return item;
	}
	
	private static SpriteMenuItem createMenuItem(TextureRegion region, int tag) {
		SpriteMenuItem item = new SpriteMenuItem(-1, region);
		item.setTag(tag);
		//item.setTouchable(Touchable.enabled);
		item.setIndicatorScale(kDefaultIndicatorScale);
		item.setEnabledColors(kMenuEnableColor, kMenuWhiteColor);
		item.setSelectedColors(kMenuSelectedColor, kMenuWhiteColor);
		item.setPressedColors(kMenuPressedColor, kMenuWhiteColor);
		return item;
	}
	
	private static GaugeMenuItem createGaugeMenuItem(String text, int tag, int fontSize,float gaugeOffset) {
		GaugeMenuItem item = new GaugeMenuItem(-1, text, fontSize, gaugeOffset);
		item.setTag(tag);
		item.setTextCentered(true);
		item.setIndicatorScale(kDefaultIndicatorScale);
		item.setEnabledColors(kMenuEnableColor, kMenuWhiteColor);
		item.setSelectedColors(kMenuSelectedColor, kMenuWhiteColor);
		item.setPressedColors(kMenuPressedColor, kMenuWhiteColor);
		item.setStrokeFilledColor(kMenuOrange);
		item.setStrokeEmptyColor(kMenuDisabledColor);
		return item;
	}
	
	private static Prop createSpriteProp(String regionName) {
		Prop prop = new Prop();
		CMSprite sprite = new CMSprite(getScene().textureRegionByName(regionName));
		prop.addSpriteChild(sprite);
		prop.setSize(sprite.getWidth(), sprite.getHeight());
		return prop;
	}
	
	private static Prop getBuyNowTitleProp(LocaleType locale, Actor parent) {
		Prop titleProp = new Prop();
		titleProp.setTransform(true);
		CMSprite puzzle = null, wizard = null;
		
		String puzzleString = LangFX.locale2PuzzleTexSuffix(locale);
		String wizardString = LangFX.locale2WizardTexSuffix(locale);
		
		if (puzzleString != null) {
			puzzle = new CMSprite(getScene().textureByName(
					"lang/pw-logo-puzzle-" + puzzleString + ".png"));
			puzzle.setAlphaMode(AlphaMode.POST_MULTIPLIED);
			puzzle.setOrigin(0, 0);
			titleProp.addSpriteChild(puzzle);
		}
		
		if (wizardString != null) {
			wizard = new CMSprite(getScene().textureByName(
					"lang/pw-logo-wizard-" + wizardString + ".png"));
			wizard.setAlphaMode(AlphaMode.POST_MULTIPLIED);
			wizard.setOrigin(0, 0);
			titleProp.addSpriteChild(wizard);
		}
		
		switch (locale) {
			case EN:
			{
				puzzle.setScale(288f / 512f);
				puzzle.setPosition(112, parent.getHeight() + 5 - puzzle.getScaledHeight());
				
				wizard.setScale(puzzle.getScaleX());
				wizard.setPosition(-32, parent.getHeight() - (72 + wizard.getScaledHeight()));
			}
				break;
			case CN:
			{
				puzzle.setScale(512f / 1024f);
				puzzle.setPosition(0, parent.getHeight() - (54 + puzzle.getScaledHeight()));
			}
				break;
			case DE:
			{
				puzzle.setScale(288f / 512f);
				puzzle.setPosition(112, parent.getHeight() + 5 - puzzle.getScaledHeight());
				
				wizard.setScale(puzzle.getScaleX());
				wizard.setPosition(-32, parent.getHeight() - (80 + wizard.getScaledHeight()));
			}
				break;
			case ES:
			{
				puzzle.setScale(576f / 1024f);
				puzzle.setPosition(-52, parent.getHeight() - (26 + puzzle.getScaledHeight()));
				
				wizard.setScale(puzzle.getScaleX());
				wizard.setPosition(-22, parent.getHeight() - (108 + wizard.getScaledHeight()));
			}
				break;
			case FR:
			{
				puzzle.setScale(288f / 512f);
				puzzle.setPosition(112, parent.getHeight() + 5 - puzzle.getScaledHeight());
				
				wizard.setScale(puzzle.getScaleX());
				wizard.setPosition(-32, parent.getHeight() - (86 + wizard.getScaledHeight()));
			}
				break;
			case IT:
			{
				puzzle.setScale(576f / 1024f);
				puzzle.setPosition(-78, parent.getHeight() - (2 + puzzle.getScaledHeight()));
				
				wizard.setScale(puzzle.getScaleX());
				wizard.setPosition(-20, parent.getHeight() - (83 + wizard.getScaledHeight()));
			}
				break;
			case JP:
			{
				puzzle.setScale(288f / 512f);
				puzzle.setPosition(112, parent.getHeight() - puzzle.getScaledHeight());
				
				wizard.setScale(puzzle.getScaleX());
				wizard.setPosition(-36, parent.getHeight() - (88 + wizard.getScaledHeight()));
			}
				break;
			case KR:
			{
				puzzle.setScale(512f / 1024f);
				puzzle.setPosition(0, parent.getHeight() - (54 + puzzle.getScaledHeight()));
			}
				break;
			default:
				break;
		}
		
		String iqString = LangFX.locale2IQString(getScene().getLocale(), false);
		CMSprite iqSprite = new CMSprite(getScene().textureByName("lang/pw-logo-" + iqString + ".png"));
		iqSprite.setAlphaMode(AlphaMode.POST_MULTIPLIED);
		iqSprite.setOrigin(0, 0);
		iqSprite.setScale(320f / iqSprite.getWidth());
		iqSprite.setPosition(232, parent.getHeight() - (iqSprite.getScaledHeight() + 520));
		titleProp.addSpriteChild(iqSprite);
		
		CMSprite priceTag = new CMSprite(getScene().textureByName("price-tag.png"));
		priceTag.setAlphaMode(AlphaMode.POST_MULTIPLIED);
		priceTag.setPosition(parent.getWidth() - priceTag.getWidth(),
				parent.getHeight() - priceTag.getHeight());
		titleProp.addSpriteChild(priceTag);
		
		return titleProp;
		
		/*String regionName = "buy-now-" + Localizer.locale2String(locale);
		float y = 224f;
		switch (locale) {
			case CN:
				y = 164f;
				break;
			case FR:
				y = 236f;
				break;
			case ES:
			case IT:
				y = 248f;
				regionName = "buy-now-ES-IT";
				break;
			case DE:
				y = 200f;
				break;
			case JP:
				y = 222f;
				break;
			case KR:
				y = 158f;
				break;
			default:
				break;
		}
		
		Prop buyNowProp = createSpriteProp(regionName);
		buyNowProp.setPosition(
				(parent.getWidth() - buyNowProp.getWidth()) / 2,
				parent.getHeight() - y);
		return buyNowProp;*/
	}
	
	@Override
	protected MenuDialog buildMenuEscDialog_(int category, IEventListener listener, int[] evCodes) {
		assert (evCodes.length == 5) : "MenuBuilder::buildMenuEscDialog - bad args.";
		
		MenuDialog dialog = new MenuDialog(category, 30, CMInputs.HAS_FOCUS_MENU_DIALOG, NavigationMap.NAV_VERT);
		dialog.setCanGoBack(true);
		dialog.setRepeats(true);
		//dialog.setTouchable(Touchable.enabled);
		dialog.addEventListener(MenuDialog.EV_TYPE_ITEM_DID_ACTIVATE, listener);
		
		Prop9 bg = isGodModeEnabled() ? createDialogBg(1080, 800) : createDialogBg(760, 640);
		dialog.addBgItem(bg);
		dialog.setSize(bg.getWidth(), bg.getHeight());
		
		int evIndex = 0;
		MenuItem resume = createMenuItem("Resume", evCodes[evIndex++]);
		resume.setPosition(0, bg.getHeight() / 2 - 112);
		dialog.addMenuItem(resume);
		
		MenuItem options = createMenuItem("Options", evCodes[evIndex++]);
		options.setPosition(0, resume.getY() - kMenuVertSeparation);
		dialog.addMenuItem(options);
		
		MenuItem credits = createMenuItem("Credits", evCodes[evIndex++]);
		credits.setPosition(0, options.getY() - kMenuVertSeparation);
		dialog.addMenuItem(credits);
		
		MenuItem placeholder = credits;
		
		if (isGodModeEnabled()) {
			MenuItem unlockAll = createMenuItem(kGodModeOff, evCodes[evIndex]);
			unlockAll.setPosition(0, credits.getY() - kMenuVertSeparation);
			dialog.addMenuItem(unlockAll);
			placeholder = unlockAll;
		}
		
		evIndex++;
		
		MenuItem exit = createMenuItem("Exit Game", evCodes[evIndex]);
		exit.setPosition(0, placeholder.getY() - kMenuVertSeparation);
		dialog.addMenuItem(exit);
		
		dialog.setScale(kDefaultDialogScaleFactor);
		return dialog;
	}
	
	@Override
	protected MenuDialog buildTrialMenuEscDialog_(int category, IEventListener listener, int[] evCodes) {
		assert (evCodes.length == 5) : "MenuBuilder::buildTrialMenuEscDialog - bad args.";

		MenuDialog dialog = new MenuDialog(category, 30, CMInputs.HAS_FOCUS_MENU_DIALOG, NavigationMap.NAV_VERT);
		dialog.setCanGoBack(true);
		dialog.setRepeats(true);
		dialog.addEventListener(MenuDialog.EV_TYPE_ITEM_DID_ACTIVATE, listener);
		
		Prop9 bg = createDialogBg(1360, 760);
		dialog.addBgItem(bg);
		dialog.setSize(bg.getWidth(), bg.getHeight());
		
		Prop gamebox = new Prop();
		CMSprite gbSprite = new CMSprite(getScene().textureByName("buy-now.png"));
		gamebox.addSpriteChild(gbSprite);
		gamebox.setSize(gbSprite.getWidth(), gbSprite.getHeight());
		gamebox.setTransform(true);
		gamebox.setPosition(
				bg.getWidth() / 2 - (gamebox.getWidth() + 70),
				-gamebox.getHeight() / 2);
		dialog.addContentItem(gamebox);
		
		Prop titleProp = getBuyNowTitleProp(Localizer.getLocale(), gamebox);
		gamebox.addActor(titleProp);
		
		int evIndex = 0;
		float xOffset = gamebox.getX() -
				(bg.getWidth() - (gamebox.getWidth() + 100)) / 2;
		MenuItem resume = createMenuItem("Resume", evCodes[evIndex++]);
		resume.setPosition(xOffset, bg.getHeight() / 2 - 112);
		dialog.addMenuItem(resume);
		
		MenuItem buyNow = createMenuItem("Buy Now", evCodes[evIndex++]);
		buyNow.setPosition(xOffset, resume.getY() - kMenuVertSeparation);
		dialog.addMenuItem(buyNow);
		
		MenuItem options = createMenuItem("Options", evCodes[evIndex++]);
		options.setPosition(xOffset, buyNow.getY() - kMenuVertSeparation);
		dialog.addMenuItem(options);
		
		MenuItem credits = createMenuItem("Credits", evCodes[evIndex++]);
		credits.setPosition(xOffset, options.getY() - kMenuVertSeparation);
		dialog.addMenuItem(credits);
		
		MenuItem exit = createMenuItem("Exit Game", evCodes[evIndex]);
		exit.setPosition(xOffset, credits.getY() - kMenuVertSeparation);
		dialog.addMenuItem(exit);
		
		dialog.enableCustomLayout(kUIDTrialMenuEsc, s_CustomLayouter);
		dialog.addCustomLayoutItem(dialog);
		dialog.addCustomLayoutItem(bg);
		dialog.addCustomLayoutItem(gamebox);
		dialog.addCustomLayoutItem(titleProp);
		dialog.addCustomLayoutItem(resume);
		dialog.addCustomLayoutItem(buyNow);
		dialog.addCustomLayoutItem(options);
		dialog.addCustomLayoutItem(credits);
		dialog.addCustomLayoutItem(exit);
		dialog.layoutCustomItems();
		
		dialog.setScale(kDefaultDialogScaleFactor);
		
		return dialog;
	}

	@Override
	protected MenuDialog buildPlayfieldEscDialog_(int category, IEventListener listener, int[] evCodes) {
		assert (evCodes.length == 4) : "MenuBuilder::buildPlayfieldEscDialog - bad args.";
		
		MenuDialog dialog = new MenuDialog(category, 30, CMInputs.HAS_FOCUS_MENU_DIALOG, NavigationMap.NAV_VERT);
		dialog.setCanGoBack(true);
		dialog.setRepeats(true);
		dialog.addEventListener(MenuDialog.EV_TYPE_ITEM_DID_ACTIVATE, listener);
		
		Prop9 bg = createDialogBg(840, 640);
		dialog.addBgItem(bg);
		
		int evIndex = 0;
		MenuItem resume = createMenuItem("Resume", evCodes[evIndex++]);
		resume.setPosition(0, bg.getHeight() / 2 - 112);
		dialog.addMenuItem(resume);
		
		MenuItem reset = createMenuItem("Reset Puzzle", evCodes[evIndex++]);
		reset.setPosition(0, resume.getY() - kMenuVertSeparation);
		dialog.addMenuItem(reset);
		
		MenuItem options = createMenuItem("Options", evCodes[evIndex++]);
		options.setPosition(0, reset.getY() - kMenuVertSeparation);
		dialog.addMenuItem(options);
		
		MenuItem menu = createMenuItem("Puzzle Menu", evCodes[evIndex]);
		menu.setPosition(0, options.getY() - kMenuVertSeparation);
		dialog.addMenuItem(menu);
		
		dialog.enableCustomLayout(kUIDPlayfieldEsc, s_CustomLayouter);
		dialog.addCustomLayoutItem(bg);
		dialog.layoutCustomItems();
		
		dialog.setScale(kDefaultDialogScaleFactor);
		return dialog;
	}
	
	@Override
	protected MenuDialog buildTrialPlayfieldEscDialog_(int category, IEventListener listener, int[] evCodes) {
		assert (evCodes.length == 5) : "MenuBuilder::buildTrialPlayfieldEscDialog - bad args.";
		
		MenuDialog dialog = new MenuDialog(category, 30, CMInputs.HAS_FOCUS_MENU_DIALOG, NavigationMap.NAV_VERT);
		dialog.setCanGoBack(true);
		dialog.setRepeats(true);
		dialog.addEventListener(MenuDialog.EV_TYPE_ITEM_DID_ACTIVATE, listener);
		
		Prop9 bg = createDialogBg(1280, 760);
		dialog.addBgItem(bg);
		
		Prop gamebox = new Prop();
		CMSprite sprite = new CMSprite(getScene().textureByName("buy-now.png"));
		gamebox.addSpriteChild(sprite);
		gamebox.setSize(sprite.getWidth(), sprite.getHeight());
		gamebox.setTransform(true);
		gamebox.setPosition(
				bg.getWidth() / 2 - (gamebox.getWidth() + 70),
				-gamebox.getHeight() / 2);
		dialog.addContentItem(gamebox);
		
		Prop titleProp = getBuyNowTitleProp(Localizer.getLocale(), gamebox);
		gamebox.addActor(titleProp);
		
		int evIndex = 0;
		float xOffset = gamebox.getX() -
				(bg.getWidth() - (gamebox.getWidth() + 100)) / 2;
		MenuItem resume = createMenuItem("Resume", evCodes[evIndex++]);
		resume.setPosition(0, bg.getHeight() / 2 - 112);
		dialog.addMenuItem(resume);
		
		MenuItem buyNow = createMenuItem("Buy Now", evCodes[evIndex++]);
		buyNow.setPosition(xOffset, resume.getY() - kMenuVertSeparation);
		dialog.addMenuItem(buyNow);
		
		MenuItem reset = createMenuItem("Reset Puzzle", evCodes[evIndex++]);
		reset.setPosition(0, buyNow.getY() - kMenuVertSeparation);
		dialog.addMenuItem(reset);
		
		MenuItem options = createMenuItem("Options", evCodes[evIndex++]);
		options.setPosition(0, reset.getY() - kMenuVertSeparation);
		dialog.addMenuItem(options);
		
		MenuItem menu = createMenuItem("Puzzle Menu", evCodes[evIndex]);
		menu.setPosition(0, options.getY() - kMenuVertSeparation);
		dialog.addMenuItem(menu);
		
		dialog.enableCustomLayout(kUIDTrialPlayfieldEsc, s_CustomLayouter);
		dialog.addCustomLayoutItem(dialog);
		dialog.addCustomLayoutItem(bg);
		dialog.addCustomLayoutItem(gamebox);
		dialog.addCustomLayoutItem(titleProp);
		dialog.addCustomLayoutItem(resume);
		dialog.addCustomLayoutItem(buyNow);
		dialog.addCustomLayoutItem(reset);
		dialog.addCustomLayoutItem(options);
		dialog.addCustomLayoutItem(menu);
		dialog.layoutCustomItems();
		
		dialog.setScale(kDefaultDialogScaleFactor);
		return dialog;
	}
	
	@Override
	protected MenuDialog buildOptionsDialog_(int category, IEventListener listener, int[] evCodes) {
		assert (evCodes.length == 6) : "MenuBuilder::buildOptionsDialog - bad args.";
		
		MenuDialog dialog = new MenuDialog(category, 20, CMInputs.HAS_FOCUS_MENU_DIALOG, NavigationMap.NAV_VERT);
		dialog.setCanGoBack(true);
		dialog.setRepeats(true);
		dialog.addEventListener(MenuDialog.EV_TYPE_ITEM_DID_ACTIVATE, listener);
		
		Prop9 bg = createDialogBg(1160, 890);
		dialog.addBgItem(bg);
		
		int evIndex = 0;
		GaugeMenuItem music = createGaugeMenuItem(
				"Music",
				evCodes[evIndex++],
				MenuBuilder.kMenuFontSize,
				40);
		music.setPosition(0, bg.getHeight() / 2 - 112);
		music.setShouldPlaySound(false);
		music.setGaugeLevel(6);
		dialog.addMenuItem(music);
		
		GaugeMenuItem sfx = createGaugeMenuItem(
				"Sfx",
				evCodes[evIndex++],
				MenuBuilder.kMenuFontSize,
				40);
		sfx.setPosition(0, music.getY() - kMenuVertSeparation);
		sfx.setGaugeLevel(10);
		dialog.addMenuItem(sfx);
		
		MenuItem display = createMenuItem("Display", evCodes[evIndex++]);
		display.setPosition(0, sfx.getY() - kMenuVertSeparation);
		dialog.addMenuItem(display);
		
		MenuItem lang = createMenuItem("Language", evCodes[evIndex++]);
		lang.setPosition(0, display.getY() - kMenuVertSeparation);
		dialog.addMenuItem(lang);
		
		MenuItem colorBlind = createMenuItem(kColorBlindModeOff, evCodes[evIndex++]);
		colorBlind.setPosition(0, lang.getY() - kMenuVertSeparation);
		dialog.addMenuItem(colorBlind);
		
		MenuItem back = createMenuItem("back", evCodes[evIndex]);
		back.setPosition(0, colorBlind.getY() - kMenuVertSeparation);
		dialog.addMenuItem(back);
		
		dialog.setScale(kDefaultDialogScaleFactor);
		return dialog;
	}
	
	@Override
	protected MenuDialog buildLanguageDialog_(int category, IEventListener listener, int[] evCodes) {
		assert (evCodes.length == 9) : "MenuBuilder::buildLanguageDialog - bad args.";
		
		//int category, int priority, int inputFocus, int columnLen
		MenuDialog dialog = new MenuGridDialog(category, 19, CMInputs.HAS_FOCUS_MENU_DIALOG, Localizer.kNumLocales / 2);
		dialog.setCanGoBack(true);
		dialog.setRepeats(true);
		dialog.addEventListener(MenuDialog.EV_TYPE_ITEM_DID_ACTIVATE, listener);
		
		Prop9 bg = createDialogBg(1240, 880);
		dialog.addBgItem(bg);
		
		int evIndex = 0;
		Vector2 localeOffset = new Vector2(
				bg.getWidth() / 9 - bg.getWidth() / 2,
				bg.getHeight() / 2 - 4f * kDefaultVertPadding);
		SceneController scene = getScene();
		final LocaleType[] kLocales = Localizer.kLocales;
		for (int i = 0; i < Localizer.kNumLocales; i++, evIndex++) {
			String texName = "lang-text-" + Localizer.locale2StringLower(kLocales[i]);
			SpriteMenuItem langItem = createMenuItem(scene.textureRegionByName(texName), evCodes[evIndex]);
			
			texName = "lang-icon-" + Localizer.locale2StringLower(kLocales[i]);
			SpriteMenuIcon menuIcon = langItem.getIcon();
			menuIcon.setSpriteOffsetX(20);
			menuIcon.setBullet(new CMSprite(scene.textureRegionByName(texName)));
			
			langItem.setPosition(localeOffset.x, localeOffset.y);
			dialog.addMenuItem(langItem);
			localeOffset.y -= langItem.getHeight() + 1.75f * kDefaultVertSeparation;
			if (kLocales[i] == LocaleType.ES) // Begin new column
				localeOffset.set(bg.getWidth() / 9, bg.getHeight() / 2 - 4f * kDefaultVertPadding);
		}
		
		MenuItem back = createMenuItem("back", evCodes[evIndex]);
		back.setPosition(0, 0.9f * kEnLblHeight - bg.getHeight() / 2);
		dialog.addMenuItem(back);
		
		dialog.setScale(kDefaultDialogScaleFactor);
		return dialog;
	}
	
	@Override
	protected MenuDialog buildDisplayDialog_(int category, IEventListener listener, int[] evCodes) {
		assert (evCodes.length == 3) : "MenuBuilder::buildDisplayDialog - bad args.";
		
		MenuDialog dialog = new MenuDialog(category, 19, CMInputs.HAS_FOCUS_MENU_DIALOG, NavigationMap.NAV_VERT);
		dialog.setCanGoBack(true);
		dialog.setRepeats(true);
		dialog.addEventListener(MenuDialog.EV_TYPE_ITEM_DID_ACTIVATE, listener);
		
		Prop9 bg = createDialogBg(760, 500);
		dialog.addBgItem(bg);
		
		int evIndex = 0;
		MenuItem windowed = createMenuItem("Windowed", evCodes[evIndex++]);
		windowed.setPosition(0, bg.getHeight() / 2 - 112);
		dialog.addMenuItem(windowed);
		
		MenuItem fullscreen = createMenuItem("Fullscreen", evCodes[evIndex++]);
		fullscreen.setPosition(0, windowed.getY() - kMenuVertSeparation);
		dialog.addMenuItem(fullscreen);
		
		MenuItem back = createMenuItem("back", evCodes[evIndex]);
		back.setPosition(0, fullscreen.getY() - kMenuVertSeparation);
		dialog.addMenuItem(back);
		
		dialog.enableCustomLayout(kUIDDisplay, s_CustomLayouter);
		dialog.addCustomLayoutItem(bg);
		dialog.layoutCustomItems();
		
		dialog.setScale(kDefaultDialogScaleFactor);
		return dialog;
	}
	
	@Override
	protected MenuDialog buildDisplayConfirmDialog_(int category, IEventListener listener, int[] evCodes,
			int duration, String msgName) {
		assert (evCodes.length == 2) : "MenuBuilder::buildDisplayConfirmDialog - bad args.";
		
		MenuDialog dialog = new MenuDialog(category, 0, CMInputs.HAS_FOCUS_MENU_DIALOG, NavigationMap.NAV_HORIZ);
		dialog.setCanGoBack(false);
		dialog.addEventListener(MenuDialog.EV_TYPE_ITEM_DID_ACTIVATE, listener);
		
		Prop9 bg = createDialogBg(1280, 600);
		dialog.addBgItem(bg);
		
		Label caption = TextUtils.create(
				"Keep new display\nsettings?",
				kMenuFontSize,
				TextUtils.kAlignCenter,
				new Color(kMenuEnableColor));
		caption.setPosition(
				-caption.getWidth() / 2,
				bg.getHeight() / 2 - (kEnLblHeight / 2 + 112));
		dialog.addContentItem(caption);
		
		MenuItem noItem = createMenuItem("No", evCodes[1]);
		noItem.setPosition(0.175f * bg.getWidth(), caption.getY() - 0.85f * kEnLblHeight);
		dialog.addMenuItem(noItem);
		
		MenuItem yesItem = createMenuItem("Yes", evCodes[0]);
		yesItem.setPosition(-0.175f * bg.getWidth(), noItem.getY());
		dialog.addMenuItem(yesItem);
		
		SceneController scene = getScene();
		String msgString = scene.localize("Reverting in ? seconds");
		msgString = msgString.replaceFirst("\\?", "" + duration);
		Label msg = TextUtils.create(
				msgString,
				28,
				TextUtils.kAlignCenter,
				new Color(kMenuOrange));
		msg.setPosition(
				-msg.getWidth() / 2,
				yesItem.getY() - 1.35f * kEnLblHeight);
		msg.setName(msgName);
		dialog.addContentItem(msg);
		
		dialog.setScale(kDefaultDialogScaleFactor);
		return dialog;
	}
	
	@Override
	protected MenuDialog buildCreditsDialog_(int category, IEventListener listener, int[] evCodes) {
		assert (evCodes.length == 1) : "MenuBuilder::buildCreditsDialog - bad args.";
		
		CreditsDialog dialog = new CreditsDialog(category, 20, CMInputs.HAS_FOCUS_MENU_DIALOG, 200);
		dialog.setCanGoBack(true);
		dialog.addEventListener(MenuDialog.EV_TYPE_ITEM_DID_ACTIVATE, listener);
		
		Prop9 bg = createDialogBg(1200, 840);
		dialog.addBgItem(bg);
		dialog.setScrollBounds(new Rectangle(
				bg.getX() - bg.getWidth() / 2,
				bg.getY() - 0.25f * bg.getHeight(),
				bg.getWidth(),
				0.7125f * bg.getHeight()));
		
		float lblHgt = kEnLblHeight, lblHgtDbl = kEnLblDblHeight;
		float FYT = 0.8f, FYN = 0.7f; // FYT: Y factor separation after title, FY: normal Y factor separation
		Label credits = TextUtils.create("Credits", 60, TextUtils.kAlignCenter, new Color(0xe08214ff));
		credits.setPosition(-credits.getWidth() / 2, -0.25f * bg.getHeight());
		dialog.addScrollingItem(credits);
		
		Prop wizardProp = createSpriteProp("12.wizard");
		wizardProp.setPosition(bg.getWidth() / 2 - (wizardProp.getWidth() + 40), -wizardProp.getHeight());
		dialog.addScrollingItem(wizardProp);

		// Game Design
		Label design = TextUtils.create("Game Design", kMenuFontSize, TextUtils.kAlignCenter, new Color(kMenuDarkYellow));
		design.setPosition(-design.getWidth() / 2, credits.getY() - lblHgt);
		dialog.addScrollingItem(design);
		
		Label designerA = TextUtils.create("Adrian McPhee", kCreditsFontSize, TextUtils.kAlignCenter, new Color(kMenuEnableColor));
		designerA.setPosition(-designerA.getWidth() / 2, design.getY() - FYT * lblHgt);
		dialog.addScrollingItem(designerA);
		
		Label designerB = TextUtils.create("Paul McPhee", kCreditsFontSize, TextUtils.kAlignCenter, new Color(kMenuEnableColor));
		designerB.setPosition(-designerB.getWidth() / 2, designerA.getY() - FYN * lblHgt);
		dialog.addScrollingItem(designerB);
		
		// Code
		Label coding = TextUtils.create("Code", kMenuFontSize, TextUtils.kAlignCenter, new Color(kMenuDarkYellow));
		coding.setPosition(-coding.getWidth() / 2, designerB.getY() - lblHgt);
		dialog.addScrollingItem(coding);
		
		Label coder = TextUtils.create("Paul McPhee", kCreditsFontSize, TextUtils.kAlignCenter, new Color(kMenuEnableColor));
		coder.setPosition(-coder.getWidth() / 2, coding.getY() - FYT * lblHgt);
		dialog.addScrollingItem(coder);
		
		// Art
		Label art = TextUtils.create("Art", kMenuFontSize, TextUtils.kAlignCenter, new Color(kMenuDarkYellow));
		art.setPosition(-art.getWidth() / 2, coder.getY() - lblHgt);
		dialog.addScrollingItem(art);
		
		Label artistA = TextUtils.create("Chen Shin", kCreditsFontSize, TextUtils.kAlignCenter, new Color(kMenuEnableColor));
		artistA.setPosition(-artistA.getWidth() / 2, art.getY() - FYT * lblHgt);
		dialog.addScrollingItem(artistA);
		
		Label artistB = TextUtils.create("Dean Spencer", kCreditsFontSize, TextUtils.kAlignCenter, new Color(kMenuEnableColor));
		artistB.setPosition(-artistB.getWidth() / 2, artistA.getY() - FYN * lblHgt);
		dialog.addScrollingItem(artistB);
		
		Label artistC = TextUtils.create("Talia Tsur", kCreditsFontSize, TextUtils.kAlignCenter, new Color(kMenuEnableColor));
		artistC.setPosition(-artistC.getWidth() / 2, artistB.getY() - FYN * lblHgt);
		dialog.addScrollingItem(artistC);
		
		// Sound Design
		Label soundDesign = TextUtils.create("Sound Design", kMenuFontSize, TextUtils.kAlignCenter, new Color(kMenuDarkYellow));
		soundDesign.setPosition(-soundDesign.getWidth() / 2, artistC.getY() - lblHgt);
		dialog.addScrollingItem(soundDesign);
		
		Label soundDesigner = TextUtils.create("Daniel Beck", kCreditsFontSize, TextUtils.kAlignCenter, new Color(kMenuEnableColor));
		soundDesigner.setPosition(-soundDesigner.getWidth() / 2, soundDesign.getY() - FYT * lblHgt);
		dialog.addScrollingItem(soundDesigner);
		
		// UI Design
		Label uiDesign = TextUtils.create("UI Design", kMenuFontSize, TextUtils.kAlignCenter, new Color(kMenuDarkYellow));
		uiDesign.setPosition(-uiDesign.getWidth() / 2, soundDesigner.getY() - lblHgt);
		dialog.addScrollingItem(uiDesign);
		
		Label uiDesigner = TextUtils.create("Sinclair C.", kCreditsFontSize, TextUtils.kAlignCenter, new Color(kMenuEnableColor));
		uiDesigner.setPosition(-uiDesigner.getWidth() / 2, uiDesign.getY() - FYT * lblHgt);
		dialog.addScrollingItem(uiDesigner);
		
		// QA Lead
		Label qaLead = TextUtils.create("QA Lead", kMenuFontSize, TextUtils.kAlignCenter, new Color(kMenuDarkYellow));
		qaLead.setPosition(-qaLead.getWidth() / 2, uiDesigner.getY() - lblHgt);
		dialog.addScrollingItem(qaLead);
		
		Label qaLeader = TextUtils.create("Sinclair C.", kCreditsFontSize, TextUtils.kAlignCenter, new Color(kMenuEnableColor));
		qaLeader.setPosition(-qaLeader.getWidth() / 2, qaLead.getY() - FYT * lblHgt);
		dialog.addScrollingItem(qaLeader);
		
		// Music
		float FYM = 0.9f; // FYM: Y factor separation after music
		Label music = TextUtils.create("Music", kMenuFontSize, TextUtils.kAlignCenter, new Color(kMenuDarkYellow));
		music.setPosition(-music.getWidth() / 2, qaLeader.getY() - lblHgt);
		dialog.addScrollingItem(music);
		
		Label musician_0 = TextUtils.create("\"The tale of room 620\"\nEhren Starks & Kate Gurba",
				kCreditsFontSize, TextUtils.kAlignCenter, 0.9f * bg.getWidth(), lblHgtDbl, new Color(kMenuEnableColor));
		musician_0.setPosition(-musician_0.getWidth() / 2, music.getY() - FYM * lblHgtDbl);
		dialog.addScrollingItem(musician_0);
		
		Label musician_1 = TextUtils.create("\"Sunset in Pensacola\"\nEhren Starks & Kate Gurba",
				kCreditsFontSize, TextUtils.kAlignCenter,  0.9f * bg.getWidth(), lblHgtDbl, new Color(kMenuEnableColor));
		musician_1.setPosition(-musician_1.getWidth() / 2, musician_0.getY() - FYM * lblHgtDbl);
		dialog.addScrollingItem(musician_1);
		
		Label musician_2 = TextUtils.create("\"Slippolska\"\nErik Ask Upmark",
				kCreditsFontSize, TextUtils.kAlignCenter,  0.9f * bg.getWidth(), lblHgtDbl, new Color(kMenuEnableColor));
		musician_2.setPosition(-musician_2.getWidth() / 2, musician_1.getY() - FYM * lblHgtDbl);
		dialog.addScrollingItem(musician_2);
		
		Label musician_3 = TextUtils.create("\"Florellen\"\nErik Ask Upmark",
				kCreditsFontSize, TextUtils.kAlignCenter,  0.9f * bg.getWidth(), lblHgtDbl, new Color(kMenuEnableColor));
		musician_3.setPosition(-musician_3.getWidth() / 2, musician_2.getY() - FYM * lblHgtDbl);
		dialog.addScrollingItem(musician_3);
		
		Label musician_4 = TextUtils.create("\"Blekingarna\"\nErik Ask Upmark",
				kCreditsFontSize, TextUtils.kAlignCenter,  0.9f * bg.getWidth(), lblHgtDbl, new Color(kMenuEnableColor));
		musician_4.setPosition(-musician_4.getWidth() / 2, musician_3.getY() - FYM * lblHgtDbl);
		dialog.addScrollingItem(musician_4);
		
		Label musician_5 = TextUtils.create("\"Virgin Light\"\nCheryl Ann Fulton",
				kCreditsFontSize, TextUtils.kAlignCenter,  0.9f * bg.getWidth(), lblHgtDbl, new Color(kMenuEnableColor));
		musician_5.setPosition(-musician_5.getWidth() / 2, musician_4.getY() - FYM * lblHgtDbl);
		dialog.addScrollingItem(musician_5);
		
		Label musician_6 = TextUtils.create("\"Hidden Sky\"\nJami Sieber",
				kCreditsFontSize, TextUtils.kAlignCenter,  0.9f * bg.getWidth(), lblHgtDbl, new Color(kMenuEnableColor));
		musician_6.setPosition(-musician_6.getWidth() / 2, musician_5.getY() - FYM * lblHgtDbl);
		dialog.addScrollingItem(musician_6);
		
		// A game by Cheeky Mammoth
		Label byLineA = TextUtils.create("a game by", kMenuFontSize, TextUtils.kAlignCenter, new Color(kMenuDarkYellow));
		byLineA.setPosition(-byLineA.getWidth() / 2, musician_6.getY() - 1.2f * lblHgt);
		dialog.addScrollingItem(byLineA);
		
		Label byLineB = TextUtils.create("Cheeky Mammoth", kMenuFontSize, TextUtils.kAlignCenter, new Color(kMenuEnableColor));
		byLineB.setPosition(-byLineB.getWidth() / 2, byLineA.getY() - FYT * lblHgt);
		dialog.addScrollingItem(byLineB);
		
		Prop mascotProp = createSpriteProp("mascot-happy");
		mascotProp.setTransform(true);
		mascotProp.setPosition(byLineB.getX() + 0.95f * byLineB.getWidth(), byLineB.getY() + 0.175f * lblHgt);
		mascotProp.setScale(192f / mascotProp.getWidth());
		dialog.addScrollingItem(mascotProp);
		
		MenuItem back = createMenuItem("back", evCodes[0]);
		back.setPosition(0, 0.9f * kEnLblHeight - bg.getHeight() / 2);
		dialog.addMenuItem(back);
		
		dialog.enableCustomLayout(kUIDCredits, s_CustomLayouter);
		dialog.addCustomLayoutItem(dialog);
		dialog.addCustomLayoutItem(bg);
		dialog.addCustomLayoutItem(wizardProp);
		dialog.layoutCustomItems();
		
		dialog.setScale(kDefaultDialogScaleFactor);
		return dialog;
	}
	
	@Override
	protected MenuDialog buildQueryDialog_(int category, IEventListener listener, int[] evCodes) {
		assert (evCodes.length == 2) : "MenuBuilder::buildQueryDialog - bad args.";
		
		MenuDialog dialog = new MenuDialog(category, 1, CMInputs.HAS_FOCUS_MENU_DIALOG, NavigationMap.NAV_HORIZ);
		dialog.setCanGoBack(true);
		dialog.addEventListener(MenuDialog.EV_TYPE_ITEM_DID_ACTIVATE, listener);
		
		Prop9 bg = createDialogBg(1000, 400);
		dialog.addBgItem(bg);
		
		Label caption = TextUtils.create("Are you sure?", kMenuFontSize, TextUtils.kAlignCenter, new Color(0xe08214ff));
		caption.setPosition(-caption.getWidth() / 2, bg.getHeight() / 2 - (kEnLblHeight / 2 + 112));
		dialog.addContentItem(caption);
		
		MenuItem noItem = createMenuItem("No", evCodes[1]);
		noItem.setPosition(0.175f * bg.getWidth(), caption.getY() - 70);
		dialog.addMenuItem(noItem);
		
		MenuItem yesItem = createMenuItem("Yes", evCodes[0]);
		yesItem.setPosition(-0.175f * bg.getWidth(), noItem.getY());
		dialog.addMenuItem(yesItem);
		
		dialog.setScale(kDefaultDialogScaleFactor);
		return dialog;
	}
	
	@Override
	protected MenuDialog buildNotificationDialog_(int category, float width, float height, String notice,
			IEventListener listener, int evCode) {
		MenuDialog dialog = new MenuDialog(category, 1, CMInputs.HAS_FOCUS_MENU_DIALOG, NavigationMap.NAV_HORIZ);
		dialog.setCanGoBack(true);
		dialog.addEventListener(MenuDialog.EV_TYPE_ITEM_DID_ACTIVATE, listener);
		
		Prop9 bg = createDialogBg(width, height);
		dialog.addBgItem(bg);
		
		Label caption = TextUtils.createCommon(notice, kMenuFontSize, TextUtils.kAlignCenter, new Color(kMenuDarkYellow));
		caption.setPosition(-caption.getWidth() / 2, bg.getHeight() / 2 - (kEnLblHeight / 2 + 112));
		dialog.addContentItem(caption);
		
		MenuItem okItem = createMenuItem("OK", evCode);
		okItem.setPosition(0, caption.getY() - 70);
		dialog.addMenuItem(okItem);
		
		dialog.setScale(kDefaultDialogScaleFactor);
		return dialog;
	}
	
	@Override
	protected MenuDialog buildLevelUnlockedDialog_(int category, int levelIndex, IEventListener listener, int[] evCodes) {
		Prop9 bg = createDialogBg(1560, 1080);
		MenuDialog dialog = new LevelUnlockedDialog(category, 40, CMInputs.HAS_FOCUS_MENU_DIALOG,
				levelIndex, bg.getWidth());
		dialog.addEventListener(MenuDialog.EV_TYPE_ITEM_DID_ACTIVATE, listener);
		dialog.addBgItem(bg);
		
		Label title = TextUtils.create("New level unlocked!", 52, TextUtils.kAlignCenter, new Color(kMenuDarkYellow));
		title.setPosition(
				-title.getWidth() / 2,
				bg.getHeight() / 2 - (40 + kEnLblHeight));
		dialog.addContentItem(title);
		
		MenuItem okItem = createMenuItem("OK", evCodes[0], (int)(1.15f * kMenuFontSize));
		okItem.setPosition(0, 0.85f * kEnLblHeight - bg.getHeight() / 2);
		dialog.addMenuItem(okItem);
		
		return dialog;
	}
	
	protected MenuDialog buildWizardUnlockedDialog_(int category, IEventListener listener, int[] evCodes) {
		MenuDialog dialog = new WizardUnlockedDialog(category, 40, CMInputs.HAS_FOCUS_MENU_DIALOG);
		dialog.addEventListener(MenuDialog.EV_TYPE_ITEM_DID_ACTIVATE, listener);
		
		Prop9 bg = createDialogBg(1560, 1080);
		dialog.addBgItem(bg);
		
		Label title = TextUtils.create("New level unlocked!", 52, TextUtils.kAlignCenter, new Color(kMenuDarkYellow));
		title.setPosition(
				-title.getWidth() / 2,
				bg.getHeight() / 2 - (40 + kEnLblHeight));
		dialog.addContentItem(title);
		
		MenuItem okItem = createMenuItem("OK", evCodes[0], (int)(1.15f * kMenuFontSize));
		okItem.setPosition(0, 0.85f * kEnLblHeight - bg.getHeight() / 2);
		dialog.addMenuItem(okItem);
		
		return dialog;
	}
	
	@Override
	protected MenuDialog buildLevelCompletedDialog_(int category, int levelIndex, IEventListener listener, int[] evCodes) {
		Prop9 bg = createDialogBg(1480, 1320);
		MenuDialog dialog = new LevelCompletedDialog(
				category,
				40,
				CMInputs.HAS_FOCUS_MENU_DIALOG,
				levelIndex,
				bg.getWidth(),
				bg.getHeight());
		dialog.addEventListener(MenuDialog.EV_TYPE_ITEM_DID_ACTIVATE, listener);
		dialog.addBgItem(bg);
		
		MenuItem okItem = createMenuItem("Yay", evCodes[0], (int)(1.15f * kMenuFontSize));
		okItem.setPosition(0, 0.75f * kEnLblHeight - bg.getHeight() / 2);
		dialog.addMenuItem(okItem);
		
		return dialog;
	}
	
	@Override
	protected MenuDialog buildPuzzleWizardDialog_(int category, IEventListener listener, int[] evCodes) {
		Prop9 bg = createDialogBg(1480, 1320);
		MenuDialog dialog = new WizardDialog(
				category,
				40,
				CMInputs.HAS_FOCUS_MENU_DIALOG,
				bg.getHeight());
		dialog.addEventListener(MenuDialog.EV_TYPE_ITEM_DID_ACTIVATE, listener);
		dialog.addBgItem(bg);
		
		MenuItem okItem = createMenuItem("Yay", evCodes[0], (int)(1.15f * kMenuFontSize));
		okItem.setPosition(0, 0.85f * kEnLblHeight - bg.getHeight() / 2);
		dialog.addMenuItem(okItem);
		
		return dialog;
	}
	
	@Override
	protected MenuDialog buildBuyNowDialog_(int category, IEventListener listener, int[] evCodes) {
		MenuDialog dialog = new MenuDialog(category, 2, CMInputs.HAS_FOCUS_MENU_DIALOG, NavigationMap.NAV_HORIZ);
		dialog.setCanGoBack(true);
		dialog.addEventListener(MenuDialog.EV_TYPE_ITEM_DID_ACTIVATE, listener);
		
		float bgWidth = 1700, bgHeight = 800;
		float captionY = kEnLblHeight / 2 + 300;
		float menuItemX = 260, menuItemY = 280;
		
		Prop9 bg = createDialogBg(bgWidth, bgHeight);
		dialog.addBgItem(bg);
		
		Label caption = TextUtils.create(
				"Buy the full version for access to all\n" +
				"12 levels and a chance to join a select\n" +
				"group of master puzzlers who have\n" +
				"conquered the challenges of\n" +
				"Puzzle Wizard.",
				40,
				TextUtils.kAlignCenter,
				new Color(kMenuDarkYellow));
		caption.setPosition(-caption.getWidth() / 2, bg.getHeight() / 2 - captionY);
		dialog.addContentItem(caption);
		
		Prop wizardProp = createSpriteProp("12.wizard");
		wizardProp.setTransform(true);
		wizardProp.setPosition(
				bg.getWidth() / 2 - (wizardProp.getWidth() + 32),
				-bg.getHeight() / 2);
		dialog.addContentItem(wizardProp);
		
		int evIndex = 0;
		MenuItem resume = createMenuItem("Resume", evCodes[evIndex++]);
		resume.setPosition(-menuItemX, caption.getY() - menuItemY);
		dialog.addMenuItem(resume);
		
		MenuItem buyNow = createMenuItem("Buy Now", evCodes[evIndex]);
		buyNow.setPosition(menuItemX, caption.getY() - menuItemY);
		dialog.addMenuItem(buyNow);
		
		dialog.enableCustomLayout(kUIDBuyNow, s_CustomLayouter);
		dialog.addCustomLayoutItem(bg);
		dialog.addCustomLayoutItem(caption);
		dialog.addCustomLayoutItem(wizardProp);
		dialog.addCustomLayoutItem(resume);
		dialog.addCustomLayoutItem(buyNow);
		dialog.layoutCustomItems();

		dialog.setScale(kDefaultDialogScaleFactor);
		return dialog;
	}
}
