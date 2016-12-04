package com.cheekymammoth.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.cheekymammoth.sceneControllers.SceneController;

public class TextUtils {
	public enum CMFontType { REGULAR, FX };
	
	public static final int kBaseFontSize = 50;
	public static final int kBaseFXFontSize = 76; //112;
	public static final int kBaseCommonFontSize = 48;
	public static final int kEnLblHeight = 150;
	public static final int kEnLblDblHeight = 256;
	
	public static final int kAlignBottom = Align.bottom;
	public static final int kAlignCenter = Align.center;
	public static final int kAlignLeft = Align.left;
	public static final int kAlignRight = Align.right;
	public static final int kAlignTop = Align.top;

	private static SceneController s_Scene = null;
	private TextUtils() { }
	
	public static void setScene(SceneController owner, SceneController setting) {
		// Don't allow previous owners to null new owners
		if (s_Scene == null || s_Scene == owner || setting != null)
			s_Scene = setting;
	}
	
	private static Label create(String text, int fontSize, int baseFontSize, int align,
			float width, float height, Color color) {
		if (s_Scene != null) {
			LabelStyle style = new LabelStyle(s_Scene.getFont(baseFontSize), color);
			Label label = new CMLabel(text, style);
			if (width >= 0 && height >= 0)
				label.setSize(width, height);
			else if (baseFontSize == kBaseFontSize)
				label.setSize(label.getWidth(), kEnLblHeight);
			label.setAlignment(align);
			label.setFontScale(fontSize / (float)baseFontSize);
			return label;
		} else
			return null;
	}
	
	public static Label create(CMFontType fontType, String text, int fontSize, int align,
			float width, float height, Color color) {
		switch (fontType) {
			case FX:
				return create(text, fontSize, kBaseFXFontSize, align, width, height, color);
			case REGULAR:
			default:
				return create(text, fontSize, kBaseFontSize, align, width, height, color);
		}
	}
	
	public static Label create(String text, int fontSize) {
		return create(s_Scene.localize(text), fontSize, kBaseFontSize, kAlignTop | kAlignLeft, -1, -1, Color.WHITE);
	}
	
	public static Label create(String text, int fontSize, Color color) {
		return create(s_Scene.localize(text), fontSize, kBaseFontSize, kAlignTop | kAlignLeft, -1, -1, color);
	}
	
	public static Label create(String text, int fontSize, int align, Color color) {
		return create(s_Scene.localize(text), fontSize, kBaseFontSize, align, -1, -1, color);
	}
	
	public static Label create(String text, int fontSize, float width, float height) {
		return create(s_Scene.localize(text), fontSize, kBaseFontSize, kAlignTop | kAlignLeft, width, height, Color.WHITE);
	}
	
	public static Label create(String text, int fontSize, int align, float width, float height) {
		return create(s_Scene.localize(text), fontSize, kBaseFontSize, align, width, height, Color.WHITE);
	}
	
	public static Label create(String text, int fontSize, int align, float width, float height, Color color) {
		return create(s_Scene.localize(text), fontSize, kBaseFontSize, align, width, height, color);
	}

	public static Label createFX(String text, int fontSize) {
		return create(s_Scene.localize(text), fontSize, kBaseFXFontSize, kAlignTop | kAlignLeft, -1, -1, Color.WHITE);
	}
	
	public static Label createFX(String text, int fontSize, Color color) {
		return create(s_Scene.localize(text), fontSize, kBaseFXFontSize, kAlignTop | kAlignLeft, -1, -1, color);
	}
	
	public static Label createFX(String text, int fontSize, int align, Color color) {
		return create(s_Scene.localize(text), fontSize, kBaseFXFontSize, align, -1, -1, color);
	}

	public static Label createFX(String text, int fontSize, int align, float width, float height) {
		return create(s_Scene.localize(text), fontSize, kBaseFXFontSize, align, width, height, Color.WHITE);
	}
	
	public static Label createFX(String text, int fontSize, int align, float width, float height, Color color) {
		return create(s_Scene.localize(text), fontSize, kBaseFXFontSize, align, width, height, color);
	}
	
	public static Label createIQ(String text, int fontSize, int align, Color color) {
		return create(text, fontSize, kBaseCommonFontSize, align, -1, -1, color);
	}
	
	public static Label createCommon(String text, int fontSize, int align, Color color) {
		return create(text, fontSize, kBaseCommonFontSize, align, -1, -1, color);
	}
	
	public static Label createGauge(String text, int fontSize, int align, Color color) {
		return create(text, fontSize, kBaseCommonFontSize, align, -1, -1, color);
	}
	
	public static void swapFont(String fontName, Label label, boolean localize) {
		if (fontName != null && label != null) {
			if (localize) {
				String localizedText = s_Scene.localize(label.getText().toString());
				label.setText("");
				LabelStyle style = label.getStyle();
				style.font = s_Scene.getFont(fontName);
				label.setStyle(style);
				label.setText(localizedText);
			} else {
				String textCache = label.getText().toString();
				label.setText("");
				LabelStyle style = label.getStyle();
				style.font = s_Scene.getFont(fontName);
				label.setStyle(style);
				label.setText(textCache);
			}
		}
	}
	
	private static float getCapHeight(int fontSize, int baseFontSize) {
		BitmapFont font = s_Scene.getFont(baseFontSize);
		float capHeight = font != null ? font.getCapHeight() * (fontSize / (float)baseFontSize) : 0;
		return capHeight;
	}
	
	public static float getCapHeight(int fontSize, CMFontType fontType) {
		return getCapHeight(fontSize, fontType == CMFontType.REGULAR ? kBaseFontSize : kBaseFXFontSize);
	}
	
	public static float getCapHeight(int fontSize) {
		return getCapHeight(fontSize, kBaseFontSize);
	}
	
	public static float getCapHeightFX(int fontSize) {
		return getCapHeight(fontSize, kBaseFXFontSize);
	}
	
	public static float getCapHeightIQ(int fontSize) {
		return getCapHeight(fontSize, kBaseCommonFontSize);
	}
	
	public static int baseFontSizeForFontType(CMFontType fontType) {
		if (fontType == null)
			return 0;
		else
			return fontType == CMFontType.REGULAR ? kBaseFontSize : kBaseFXFontSize;
	}
	
	public static float getTextBoundsX(Label label, int align) {
		if (label == null)
			return 0;
		
		float boundsX = 0;
		
		if ((align & kAlignLeft) == kAlignLeft)
			boundsX = 0;
		else if ((align & kAlignRight) == kAlignRight)
			boundsX = label.getWidth() - label.getTextBounds().width;
		else if ((align & kAlignCenter) == kAlignCenter)
			boundsX = (label.getWidth() - label.getTextBounds().width) / 2;
		
		return boundsX;
	}
	
	public static float getTextBoundsY(Label label, int align) {
		if (label == null)
			return 0;
		
		float boundsY = 0;
		
		if ((align & kAlignBottom) == kAlignBottom)
			boundsY = 0;
		else if ((align & kAlignTop) == kAlignTop)
			boundsY = label.getHeight() - label.getTextBounds().height;
		else if ((align & kAlignCenter) == kAlignCenter)
			boundsY = (label.getHeight() - label.getTextBounds().height) / 2;
		
		return boundsY;
	}
}
