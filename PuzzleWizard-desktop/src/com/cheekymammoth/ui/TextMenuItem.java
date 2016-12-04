package com.cheekymammoth.ui;

import com.cheekymammoth.ui.TextUtils.CMFontType;
import com.cheekymammoth.utils.LangFX;

public class TextMenuItem extends MenuItem {
	private TextMenuIcon textMenuIcon;

	public TextMenuItem() {
		this(-1);
	}

	public TextMenuItem(int category) {
		this(category, "", 32);
	}
	
	public TextMenuItem(int category, String text, int fontSize) {
		this(category, text, fontSize, TextUtils.kAlignCenter | TextUtils.kAlignLeft, CMFontType.REGULAR);
	}
	
	public TextMenuItem(int category, String text, int fontSize, int align, CMFontType fontType) {
		this(category, text, fontSize, align, -1, -1, fontType);
	}

	public TextMenuItem(int category, String text, int fontSize, int align,
			float width, float height, CMFontType fontType) {
		super(category);
		
		textMenuIcon = new TextMenuIcon(category, text, fontSize, align, width, height, fontType);
		setIcon(textMenuIcon);
		addMenuIcon(textMenuIcon);
		
		attachIndicator();
		float indicatorScale = (fontType == CMFontType.REGULAR ? 1f : 1.25f) *
				fontSize / (float)TextUtils.kBaseFontSize;
		indicator.setScale(indicatorScale);
		indicator.setOffset(0, LangFX.getActiveIconYOffset());
		updateIndicator();
	}
	
	public TextMenuIcon getIcon() { return textMenuIcon; }
	
	public boolean isTextCentered() { return textMenuIcon.isTextCentered(); }
	
	public void setTextCentered(boolean value) {
		textMenuIcon.setTextCentered(value);
		indicator.updateAttachmentPosition();
	}
	
	@Override
	public String getText() {
		return textMenuIcon.getText();
	}
	
	@Override
	public void setText(String text) {
		textMenuIcon.setText(text);
	}
	
	@Override
	public void localeDidChange(String fontKey, String FXFontKey) {
		super.localeDidChange(fontKey, FXFontKey);
		indicator.setOffset(0, LangFX.getActiveIconYOffset());
	}
}
