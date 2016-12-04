package com.cheekymammoth.ui;

import com.cheekymammoth.input.CMInputs;

public class MenuItem extends MenuButton {
	private MenuIcon icon;
	protected MenuIndicator indicator;
	
	public MenuItem() {
		this(-1);
	}
	
	public MenuItem(int category) {
		super(category, null);
	}
	
	public void attachIndicator() {
		if (indicator == null) {
			indicator = new MenuIndicator();
			indicator.setTag(MenuButton.kMenuIndicatorDefaultTag);
			indicator.attachToMenuButton(this);
		}
	}
	
	public void detachIndicator() {
		if (indicator != null) {
			indicator.detachFromMenuButton(this);
			indicator = null;
		}
	}
	
	protected void updateIndicator() {
		if (indicator != null)
			indicator.setVisible(isSelected() && isEnabled());
	}
	
	public String getText() { return null; }
	
	public void setText(String text) { }
	
	protected void setIcon(MenuIcon value) { icon = value; }
	
	public MenuIndicator getIndicator() { return indicator; }
	
	public void setEnabledColors(int labelColor, int bulletColor) {
		icon.setEnabledColors(labelColor, bulletColor);
	}
	
	public void setDisabledColors(int labelColor, int bulletColor) {
		icon.setDisabledColors(labelColor, bulletColor);
	}
	
	public void setSelectedColors(int labelColor, int bulletColor) {
		icon.setSelectedColors(labelColor, bulletColor);
	}
	
	public void setPressedColors(int labelColor, int bulletColor) {
		icon.setPressedColors(labelColor, bulletColor);
	}
	
	public void setIndicatorScale(float scale) {
		if (indicator != null)
			indicator.setScale(scale);
	}
	
	@Override
	public void setSelected(boolean value) {
		super.setSelected(value);
		updateIndicator();
	}
	
	public void enable(boolean enable) {
		super.enable(enable);
		updateIndicator();
	}
	
	public void update(CMInputs input) { }
}
