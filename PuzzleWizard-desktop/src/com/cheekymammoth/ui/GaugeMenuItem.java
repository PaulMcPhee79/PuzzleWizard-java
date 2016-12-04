package com.cheekymammoth.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.input.CMInputs;
import com.cheekymammoth.locale.ILocalizable;
import com.cheekymammoth.ui.TextUtils.CMFontType;
import com.cheekymammoth.utils.Coord;

public final class GaugeMenuItem extends TextMenuItem {
	public static final int EV_TYPE_GAUGE_CHANGED;
	
	private static final int kNumStrokes = 10;
	//private static final float kLabelSeparation = 24f;
	private static final float kGaugeSeparation = 0f;
	private static final char kStrokeChar = 'l';
	private static final String kFullStrokeString = "llllllllll";
	//private static final String kHalfStrokeString = "lllll";
	
	static {
		EV_TYPE_GAUGE_CHANGED = EventDispatcher.nextEvType();
	}

	private boolean shouldPlaySound = true;
	private int inputInertia;
	private int inputThreshold = 30;
	private int gaugeLevel = kNumStrokes / 2;
	private float gaugeOffset = 40;
	private Label fillStrokes;
	private Label emptyStrokes;

	public GaugeMenuItem(int category, String text, int fontSize, float gaugeOffset) {
		super(category, text, fontSize, TextUtils.kAlignCenter, CMFontType.REGULAR);
		this.gaugeOffset = gaugeOffset;
		
		TextMenuIcon icon = getIcon();
		
		fillStrokes = TextUtils.createGauge(
				kFullStrokeString,
				fontSize,
				TextUtils.kAlignCenter | TextUtils.kAlignLeft,
				new Color(MenuBuilder.kMenuOrange));
		fillStrokes.setName(ILocalizable.kNonLocalizableName);
		fillStrokes.setScale(0.9f);
		fillStrokes.setY(icon.getY() + icon.label.getY() + (icon.label.getHeight() - fillStrokes.getHeight()) / 2);
		addActor(fillStrokes);
		
		emptyStrokes = TextUtils.createGauge(
				kFullStrokeString,
				fontSize,
				TextUtils.kAlignCenter | TextUtils.kAlignLeft,
				new Color(MenuBuilder.kMenuDisabledColor));
		emptyStrokes.setName(ILocalizable.kNonLocalizableName);
		emptyStrokes.setScale(0.9f);
		emptyStrokes.setY(fillStrokes.getY());
		addActor(emptyStrokes);
		
		setScaleWhenPressed(1f);
		layoutContents();
		updateBounds();
	}
	
	public boolean shouldPlaySound() { return shouldPlaySound; }
	
	public void setShouldPlaySound(boolean value) { shouldPlaySound = value; }
	
	public int getGaugeLevel() { return gaugeLevel; }
	
	public void setGaugeLevel(int value) {
		int val = Math.max(0, Math.min(kNumStrokes, value));
		if (val != gaugeLevel) {
			gaugeLevel = val;
			refreshGaugeDisplay();
			layoutContents();
		}
	}
	
	public void setStrokeFilledColor(int value) {
		fillStrokes.setColor(fillStrokes.getColor().set(value));
	}
	
	public void setStrokeEmptyColor(int value) {
		emptyStrokes.setColor(emptyStrokes.getColor().set(value));
	}
	
	@Override
	public void setSelected(boolean value) {
		if (value != isSelected())
			resetInertia();
		super.setSelected(value);
	}
	
	@Override
	public void depress() { /* ignore */ }
	
	@Override
	public void raise() { /* ignore */ }
	
	char[] gaugeStrokes;
	private char[] getGaugeStrokeCache() {
		if (gaugeStrokes == null) {
			gaugeStrokes = new char[kNumStrokes];
			for (int i = 0, n = gaugeStrokes.length; i < n; i++)
				gaugeStrokes[i] = kStrokeChar;
		}
		return gaugeStrokes;
	}
	
	
	private void refreshGaugeDisplay() {
		fillStrokes.setText(new String(getGaugeStrokeCache(), 0, gaugeLevel));
		emptyStrokes.setText(new String(getGaugeStrokeCache(), 0, kNumStrokes - gaugeLevel));
	}
	
	private void layoutContents() {
		TextMenuIcon icon = getIcon();
		icon.setX(-icon.label.getTextBounds().width / 2);
		icon.updateBounds();
		fillStrokes.setX(icon.getX() + icon.label.getX() + icon.label.getTextBounds().width + gaugeOffset +
				TextUtils.getTextBoundsX(icon.label, TextUtils.kAlignCenter));
		emptyStrokes.setX(fillStrokes.getX() +
				(gaugeLevel > 0 ? fillStrokes.getTextBounds().width : 0) + kGaugeSeparation);
	}
	
	private void resetInertia() {
		inputInertia = 0;
		inputThreshold = (int)(0.35f * 60); // 60fps
	}
	
	private void addToGaugeLevel(int value) {
		int prevValue = getGaugeLevel();
		setGaugeLevel(getGaugeLevel() + value);
		if (prevValue != getGaugeLevel())
			dispatchEvent(EV_TYPE_GAUGE_CHANGED, this);
	}
	
	@Override
	public void localeDidChange(String fontKey, String FXFontKey) {
		super.localeDidChange(fontKey, FXFontKey);
		
		TextMenuIcon icon = getIcon();
		//fillStrokes.setY(icon.getIconBounds().y + (icon.getIconBounds().height - fillStrokes.getTextBounds().height) / 2);
		fillStrokes.setY(icon.getY() + icon.label.getY() + (icon.label.getHeight() - fillStrokes.getHeight()) / 2);
		emptyStrokes.setY(fillStrokes.getY());
		layoutContents();
	}
	
	@Override
	public void update(CMInputs input) {
		super.update(input);
		
		if (!isSelected())
			return;
		
		Coord depressedVector = input.getDepressedVector(), heldVector = input.getHeldVector();
		if (depressedVector.x != 0) {
			addToGaugeLevel(depressedVector.x);
			resetInertia();
		} else if(heldVector.x != 0) {
			if (++inputInertia >= inputThreshold) {
				inputInertia = 0;
				inputThreshold = (int)Math.max(0.1f * 60, 0.75f * inputThreshold);
				addToGaugeLevel(heldVector.x);
			}
		} else
			resetInertia();
	}
}
