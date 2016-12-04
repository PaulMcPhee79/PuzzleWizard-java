package com.cheekymammoth.ui;

import com.cheekymammoth.ui.TextUtils.CMFontType;

public class UIFactory {
	private UIFactory() { }

	public static MenuButton getTextMenuButton(int category, String text, int fontSize,
			CMFontType fontType, boolean indicator) {
		TextMenuIcon icon = new TextMenuIcon(
				category,
				text,
				fontSize,
				TextUtils.kAlignCenter | TextUtils.kAlignLeft,
				-1,
				-1,
				fontType);
		icon.setTag(MenuButton.kMenuIconDefaultTag);
		
		MenuButton button = new MenuButton(category, icon);
		
		if (indicator) {
			MenuIndicator menuIndicator = new MenuIndicator();
			menuIndicator.setTag(MenuButton.kMenuIndicatorDefaultTag);
			menuIndicator.attachToMenuButton(button);
			float indicatorScale = (fontType == CMFontType.REGULAR ? 1f : 1.25f) *
					fontSize / (float)TextUtils.kBaseFontSize;
			menuIndicator.setScale(indicatorScale);
		}
			
		return button;
	}
	
	public static MenuButton getTextMenuButton(int category, String text, int fontSize,
			float width, float height, CMFontType fontType, boolean indicator) {
		TextMenuIcon icon = new TextMenuIcon(
				category,
				text,
				fontSize,
				TextUtils.kAlignCenter | TextUtils.kAlignLeft,
				width,
				height,
				fontType);
		icon.setTag(MenuButton.kMenuIconDefaultTag);
		
		MenuButton button = new MenuButton(category, icon);
		
		if (indicator) {
			MenuIndicator menuIndicator = new MenuIndicator();
			menuIndicator.setTag(MenuButton.kMenuIndicatorDefaultTag);
			menuIndicator.attachToMenuButton(button);
			float indicatorScale = (fontType == CMFontType.REGULAR ? 1f : 1.25f) *
					fontSize / (float)TextUtils.kBaseFontSize;
			menuIndicator.setScale(indicatorScale);
		}
			
		return button;
	}
}
