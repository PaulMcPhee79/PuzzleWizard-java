package com.cheekymammoth.ui;

import com.badlogic.gdx.math.Rectangle;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.locale.ILocalizable;

abstract public class MenuIcon extends Prop implements IEventListener, ILocalizable {
	protected static final int kEnabledIndex = 0;
	protected static final int kDisabledIndex = 1;
	protected static final int kSelectedIndex = 2;
	protected static final int kPressedIndex = 3;
	
	protected int[] iconColors = new int[] { 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff };
	protected int[] bulletColors = new int[] { 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff };
	
	public MenuIcon() {
		this(-1);
	}

	public MenuIcon(int category) {
		super(category);
		setTransform(true);
	}
	
	private Rectangle boundsCache = new Rectangle();
	public Rectangle getIconBounds() {
		return boundsCache;
	}
	
	public void setEnabledColors(int iconColor, int bulletColor) {
		iconColors[kEnabledIndex] = iconColor;
		bulletColors[kEnabledIndex] = bulletColor;
		dispatchEvent(MenuButton.EV_TYPE_BROADCAST_STATE_REQUEST, this);
	}
	
	public void setDisabledColors(int iconColor, int bulletColor) {
		iconColors[kDisabledIndex] = iconColor;
		bulletColors[kDisabledIndex] = bulletColor;
		dispatchEvent(MenuButton.EV_TYPE_BROADCAST_STATE_REQUEST, this);
	}
	
	public void setSelectedColors(int iconColor, int bulletColor) {
		iconColors[kSelectedIndex] = iconColor;
		bulletColors[kSelectedIndex] = bulletColor;
		dispatchEvent(MenuButton.EV_TYPE_BROADCAST_STATE_REQUEST, this);
	}
	
	public void setPressedColors(int iconColor, int bulletColor) {
		iconColors[kPressedIndex] = iconColor;
		bulletColors[kPressedIndex] = bulletColor;
		dispatchEvent(MenuButton.EV_TYPE_BROADCAST_STATE_REQUEST, this);
	}
	
	protected void updateBounds() { }
	
	protected void setColorsForIndex(int index) { }
	
	@Override
	protected void sizeChanged () {
		dispatchEvent(MenuButton.EV_TYPE_ICON_SIZE_CHANGED, this);
    }

	@Override
	public void localeDidChange(String fontKey, String FXFontKey) { }
	
	@Override
	public void onEvent(int evType, Object evData) {
		// Ignore echoed message
		if (evType == MenuButton.EV_TYPE_ICON_SIZE_CHANGED)
			return;
		
		MenuButton button = (MenuButton)evData;
		if (button != null) {
			if (button.isEnabled()) {
				if (button.isPressed())
					setColorsForIndex(kPressedIndex);
				else if (button.isSelected())
					setColorsForIndex(kSelectedIndex);
				else
					setColorsForIndex(kEnabledIndex);
			} else
				setColorsForIndex(kDisabledIndex);
		}
	}
}
