package com.cheekymammoth.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.locale.ILocalizable;
import com.cheekymammoth.utils.Utils;

public class MenuButton extends Prop implements ILocalizable, IEventListener {
	protected enum MenuButtonState { ENABLED, DISABLED, PRESSED };
	
	public static final int EV_TYPE_ENABLED;
	public static final int EV_TYPE_DISABLED;
	public static final int EV_TYPE_PRESSED;
	public static final int EV_TYPE_RAISED;
	public static final int EV_TYPE_CANCELLED;
	public static final int EV_TYPE_SELECTED;
	public static final int EV_TYPE_UNSELECTED;
	public static final int EV_TYPE_ICON_SIZE_CHANGED;
	public static final int EV_TYPE_BROADCAST_STATE_REQUEST;
	
	public static final int kMenuIconDefaultTag;
	public static final int kMenuIndicatorDefaultTag;
	
	static {
		EV_TYPE_ENABLED = EventDispatcher.nextEvType();
		EV_TYPE_DISABLED = EventDispatcher.nextEvType();
		EV_TYPE_PRESSED = EventDispatcher.nextEvType();
		EV_TYPE_RAISED = EventDispatcher.nextEvType();
		EV_TYPE_SELECTED = EventDispatcher.nextEvType();
		EV_TYPE_CANCELLED = EventDispatcher.nextEvType();
		EV_TYPE_UNSELECTED = EventDispatcher.nextEvType();
		EV_TYPE_ICON_SIZE_CHANGED = EventDispatcher.nextEvType();
		EV_TYPE_BROADCAST_STATE_REQUEST = EventDispatcher.nextEvType();
		kMenuIconDefaultTag = Utils.getUniqueKey();
		kMenuIndicatorDefaultTag = Utils.getUniqueKey();
	}
	
	private MenuButtonState state = MenuButtonState.ENABLED;
	private boolean suppressBoundsUpdate;
	private boolean isSelected;
	private float scaleWhenPressed = 0.9f;
	private float opacityWhenDisabled = 0.5f;
	protected String soundName = "button";
	protected Array<MenuIcon> icons;
	
	public MenuButton() {
		this(-1);
	}

	public MenuButton(int category) {
		this(category, null);
	}
	
	public MenuButton(int category, MenuIcon icon) {
		super(category);
		setTransform(true);
		addMenuIcon(icon);
		
		addListener(new ClickListener() {
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				super.touchDown(event, x, y, pointer, button);
				MenuButton.this.depress();
				return true;
            }
			
			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				super.touchUp(event, x, y, pointer, button);
				MenuButton.this.raise();
			}
			
			@Override
			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				super.touchDragged(event, x, y, pointer);
				
                if (!isPressed() && MenuButton.this.isPressed())
                	MenuButton.this.cancel();
			}
		});
	}
	
	public MenuButtonState getState() { return state; }
	
	protected void setState(MenuButtonState value) {
		if (value == state)
			return;
		
		// Clean up previous state
		MenuButtonState prevState = state;
		switch (prevState) {
			case ENABLED:
				break;
			case DISABLED:
				Color color = getColor();
				setColor(color.r, color.g, color.b, 1f);
				break;
			case PRESSED:
				setScale(1f);
				break;
		}
		
		// Apply new state
		state = value;
		
		switch (value) {
			case ENABLED:
				break;
			case DISABLED:
				Color color = getColor();
				setColor(color.r, color.g, color.b, opacityWhenDisabled);
				break;
			case PRESSED:
				setScale(scaleWhenPressed);
				break;
		}
	}
	
	protected int state2EvType(MenuButtonState state) {
		switch (state) {
			case DISABLED: return EV_TYPE_DISABLED;
			case PRESSED: return EV_TYPE_PRESSED;
			case ENABLED:
			default:
				return isSelected() ? EV_TYPE_SELECTED : EV_TYPE_ENABLED;
		}
	}
	
	public void setSoundName(String name) { soundName = name; }
	
	public float getScaleWhenPressed() { return scaleWhenPressed; }
	
	public void setScaleWhenPressed(float value) { 
		if (value != scaleWhenPressed) {
			scaleWhenPressed = value;
			if (getState() == MenuButtonState.PRESSED)
				setScale(scaleWhenPressed);
		}
	}
	
	public float getOpacityWhenDisabled() { return opacityWhenDisabled; }
	
	public void setOpacityWhenDisabled(float value) { 
		if (value != opacityWhenDisabled) {
			opacityWhenDisabled = value;
			if (getState() == MenuButtonState.DISABLED) {
				Color color = getColor();
				setColor(color.r, color.g, color.b, opacityWhenDisabled);
			}
		}
	}
	
	public boolean isSelected() { return isSelected; }
	
	public void setSelected(boolean value) {
		if (value != isSelected) {
			isSelected = value;
			dispatchEvent(isSelected ? EV_TYPE_SELECTED : EV_TYPE_UNSELECTED, this);
		}
	}
	
	public boolean isEnabled() { return state != MenuButtonState.DISABLED; }
	
	public void enable(boolean enable) {
		if (enable) {
			if (getState() == MenuButtonState.DISABLED) {
				setState(MenuButtonState.ENABLED);
				dispatchEvent(EV_TYPE_ENABLED, this);
			}
		} else {
			if (getState() == MenuButtonState.ENABLED) {
				setState(MenuButtonState.DISABLED);
				dispatchEvent(EV_TYPE_DISABLED, this);
			}
		}
	}
	
	public boolean isPressed() { return state == MenuButtonState.PRESSED; }
	
	public boolean isNavigable() { return true; }
	
	public void depress() {
		if (getState() == MenuButtonState.ENABLED) {
			setState(MenuButtonState.PRESSED);
			dispatchEvent(EV_TYPE_PRESSED, this);
		}
	}
	
	public void raise() {
		if (getState() == MenuButtonState.PRESSED) {
			setState(MenuButtonState.ENABLED);
			scene.playSound(soundName);
			dispatchEvent(EV_TYPE_RAISED, this);
		}
	}
	
	public void cancel() {
		if (getState() == MenuButtonState.PRESSED) {
			setState(MenuButtonState.ENABLED);
			dispatchEvent(EV_TYPE_ENABLED, this);
			dispatchEvent(EV_TYPE_CANCELLED, this);
		}
	}
	
	public void resetButton() {
		if (getState() != MenuButtonState.DISABLED) {
			setState(MenuButtonState.ENABLED);
			dispatchEvent(EV_TYPE_ENABLED, this);
		}
	}

	public void addMenuIcon(MenuIcon icon) {
		addMenuIconAt(-1, icon);
	}
	
	public MenuIcon menuIconForTag(int tag) {
		if (icons != null) {
			for (int i = 0; i < icons.size; i++) {
				MenuIcon icon = icons.get(i);
				if (icon.getTag() == tag)
					return icon;
			}
		}
		
		return null;
	}
	
	public MenuIcon menuIconAtIndex(int index) {
		if (icons != null && index >= 0 && index < icons.size)
			return icons.get(index);
		else
			return null;
	}

	public void addMenuIconAt(int index, MenuIcon icon) {
		if (icon == null)
			return;
		if (icons == null)
			icons = new Array<MenuIcon>(true, 1, MenuIcon.class);
		if (!icons.contains(icon, true)) {
			int validIndex = index < 0 ? icons.size : Math.min(icons.size, index);
			icons.insert(validIndex, icon);
			addActorAt(validIndex, icon);
			// TODO Test that the JVM GC can break this cycle
			icon.addEventListener(EventDispatcher.EV_TYPE_ALL, this);
			addEventListener(EventDispatcher.EV_TYPE_ALL, icon);
			
			// Initialize our new icon's presentation
			if (isSelected())
				icon.onEvent(EV_TYPE_SELECTED, this);
			int evType = state2EvType(getState());
			if (evType != -1)
				icon.onEvent(evType, this);
			
			updateBounds();
		}
	}
	
	public void removeMenuIcon(MenuIcon icon) {
		if (icon == null || icons == null)
			return;
		
		icon.removeEventListener(EventDispatcher.EV_TYPE_ALL, this);
		removeEventListener(EventDispatcher.EV_TYPE_ALL, icon);
		removeActor(icon);
		icons.removeValue(icon, true);
		updateBounds();
	}
	
	public void clearMenuIcons() {
		if (icons == null)
			return;
		suppressBoundsUpdate = true;
		for (int i = icons.size-1; i >= 0; i--)
			removeMenuIcon(icons.get(i));
		suppressBoundsUpdate = false;
		updateBounds();
	}
	
	private Rectangle iconBoundsCache = new Rectangle();
	public Rectangle getIconBounds() {
		return iconBoundsCache;
	}
	
	public void updateBounds() {
		if (suppressBoundsUpdate)
			return;
			
		iconBoundsCache.set(0, 0, 0, 0);
		
		for (int i = icons.size-1; i >= 0; i--) {
			MenuIcon icon = icons.get(i);
			Rectangle iconBounds = icon.getIconBounds();
			Utils.unionRect(iconBounds, iconBoundsCache);
		}
		
		setSize(iconBoundsCache.width, iconBoundsCache.height);
	}
	
	@Override
	public Actor hit (float x, float y, boolean touchable) {
        if (touchable && this.getTouchable() != Touchable.enabled) return null;
        return x >= iconBoundsCache.x && x < iconBoundsCache.x + iconBoundsCache.width &&
        		y >= iconBoundsCache.y && y < iconBoundsCache.y + iconBoundsCache.height ? this : null;
	}

	@Override
	public void localeDidChange(String fontKey, String FXFontKey) {
		if (icons != null) {
			for (int i = icons.size-1; i >= 0; i--)
				icons.get(i).localeDidChange(fontKey, FXFontKey);
			updateBounds();
		}
	}
	
	@Override
	public void addedToScene() {
		super.addedToScene();
		updateBounds();
	}

	@Override
	public void onEvent(int evType, Object evData) {
		if (evType == EV_TYPE_ICON_SIZE_CHANGED) {
			updateBounds();
			dispatchEvent(evType, evData);
		} else if (evType == EV_TYPE_BROADCAST_STATE_REQUEST) {
			dispatchEvent(state2EvType(getState()), this);
		}
	}
}
