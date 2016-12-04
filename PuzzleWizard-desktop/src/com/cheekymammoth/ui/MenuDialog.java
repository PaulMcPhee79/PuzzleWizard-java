package com.cheekymammoth.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.input.CMInputs;
import com.cheekymammoth.locale.ILocalizable;
import com.cheekymammoth.resolution.IResDependent;
import com.cheekymammoth.ui.INavigable.NavigationMap;
import com.cheekymammoth.ui.UILayout.UILayouter;
import com.cheekymammoth.utils.FloatTweener;
import com.cheekymammoth.utils.Transitions;

public class MenuDialog extends Prop implements IEventListener, IResDependent, ILocalizable {
	public static final int EV_TYPE_ITEM_DID_ACTIVATE;
	
	protected static final int kBgLayerIndex = 0;
	protected static final int kContentLayerIndex = 1;
	protected static final int kMenuItemLayerIndex = 2;
	
	static {
		EV_TYPE_ITEM_DID_ACTIVATE = EventDispatcher.nextEvType();
	}
	
	private boolean canGoBack;
	private boolean isAnimating;
	private int priority;
	private MenuItem pressedItem;
	private Array<MenuItem> menuItems = new Array<MenuItem>(true, 5, MenuItem.class);
	private Prop[] layers = new Prop[3]; // [BG, Content, MenuItem]
	private FloatTweener animTweener;
	private UILayout customLayout;
	protected ButtonsProxy buttonsProxy;
	
	public MenuDialog() {
		this(-1, 0, 0, NavigationMap.NAV_NONE);
	}

	public MenuDialog(int category, int priority, int inputFocus, NavigationMap navMap) {
		super(category);
		
		this.priority = priority;
		buttonsProxy = new ButtonsProxy(inputFocus, navMap);
		
		for (int i = 0, n = layers.length; i < n; i++) {
			Prop prop = new Prop();
			prop.setTransform(true);
			prop.setTouchable(Touchable.enabled);
			layers[i] = prop;
			addActor(prop);
		}
		
		setTransform(true);
		setAdvanceable(true);
	}
	
	protected Prop layerAtIndex(int index) {
		if (index >= 0 && index < layers.length)
			return layers[index];
		else
			return null;
	}
	
	public void show(boolean animate) {
		setVisible(true);
		
		if (animate && !isAnimating()) {
			if (animTweener == null)
				animTweener = new FloatTweener(0, Transitions.easeOutBack, this);
			
			float prevY = getY();
			setY(scene.VH() + getHeight());
			setAnimating(true);
			animTweener.resetTween(getY(), prevY, 0.75f, 0);
		}
	}
	
	public void hide() {
		setVisible(false);
		setPressedItem(null);
	}
	
	public void resetNav() { buttonsProxy.resetNav(); }
	
	public String getMenuItemTextForTag(int tag) {
		MenuItem item = getMenuItem(tag);
		return item != null ? item.getText() : null;
	}
	
	public void setMenuItemTextForTag(int tag, String text) {
		MenuItem item = getMenuItem(tag);
		if (item != null && text != null)
			item.setText(text);
	}
	
	public void addBgItem(Actor item) {
		if (item != null) {
			layers[kBgLayerIndex].addActor(item);
			setContentSize(Math.max(getWidth(),  item.getWidth()), Math.max(getHeight(), item.getHeight()));
		}
	}
	
	public void removeBgItem(Actor item) {
		if (item != null) {
			layers[kBgLayerIndex].removeActor(item);
			// TODO adjust content size if we ever actually need this
		}
	}
	
	public void addContentItem(Actor item) {
		if (item != null)
			layers[kContentLayerIndex].addActor(item);
	}
	
	public void removeContentItem(Actor item) {
		if (item != null)
			layers[kContentLayerIndex].removeActor(item);
	}
	
	public Actor getContentItem(String name) {
		if (name != null && layers[kContentLayerIndex] != null)
			return layers[kContentLayerIndex].findActor(name);
		else
			return null;
	}
	
	public void addMenuItem(MenuItem item) {
		if (item != null && !menuItems.contains(item, true)) {
			buttonsProxy.addButton(item);
			layers[kMenuItemLayerIndex].addActor(item);
			menuItems.add(item);
			item.addEventListener(EventDispatcher.EV_TYPE_ALL, this);
		}
	}
	
	public void removeMenuItem(MenuItem item) {
		if (item != null && !menuItems.contains(item, true)) {
			buttonsProxy.removeButton(item);
			layers[kMenuItemLayerIndex].removeActor(item);
			if (item == pressedItem)
				setPressedItem(null);
			menuItems.removeValue(item, true);
			item.removeEventListener(EventDispatcher.EV_TYPE_ALL, this);
		}
	}
	
	public MenuItem getMenuItem(int tag) {
		for (int i = 0; i < menuItems.size; i++) {
			MenuItem item = menuItems.get(i);
			if (item.getTag() == tag)
				return item;
		}
		
		return null;
	}
	
	public boolean canGoBack() { return canGoBack; }
	
	public void setCanGoBack(boolean value) { canGoBack = value; }
	
	public boolean isAnimating() { return isAnimating; }
	
	protected void setAnimating(boolean value) { isAnimating = value; }
	
	public boolean doesRepeat() { return buttonsProxy.doesRepeat(); }
	
	public void setRepeats(boolean value) { buttonsProxy.setRepeats(value); }
	
	public float getRepeatDelay() { return buttonsProxy.getRepeatDelay(); }
	
	public void setRepeatDelay(float value) { buttonsProxy.setRepeatDelay(value); }
	
	public int getPriority() { return priority; }
	
	public void setCustomLayout(UILayout layout) { customLayout = layout; }
	
	public void enableCustomLayout(int layoutID, UILayouter layouter) {
		customLayout = new UILayout(layoutID, layouter);
	}
	
	public void addCustomLayoutItem(Actor item) {
		if (item != null && customLayout != null)
			customLayout.addLayoutItem(item);
	}
	
	public void insertCustomLayoutItem(int index, Actor item) {
		if (item != null && customLayout != null)
			customLayout.insertLayoutItem(index, item);
	}
	
	public void removeCustomLayoutItem(Actor item) {
		if (item != null && customLayout != null)
			customLayout.removeLayoutItem(item);
	}
	
	public void layoutCustomItems() {
		if (customLayout != null)
			customLayout.layout();
	}
	
	private void setPressedItem(MenuItem item) {
		if (pressedItem != null)
			pressedItem.resetButton();
		pressedItem = item;
	}

	@Override
	public void localeDidChange(String fontKey, String FXFontKey) {
		for (int i = menuItems.size-1; i >= 0; i--)
			menuItems.get(i).localeDidChange(fontKey, FXFontKey);
		layers[kContentLayerIndex].localizeChildren(fontKey, FXFontKey);		
		layoutCustomItems();
	}

	@Override
	public void resolutionDidChange(int width, int height) { }
	
	public void update(CMInputs input) {
		buttonsProxy.update(input);
		
		for (int i = menuItems.size-1; i >= 0; i--)
			menuItems.get(i).update(input);
	}
	
	@Override
	public void advanceTime(float dt) {
		buttonsProxy.advanceTime(dt);
		
		if (animTweener != null)
			animTweener.advanceTime(dt);
	}

	@Override
	public void onEvent(int evType, Object evData) {
		if (evType == FloatTweener.EV_TYPE_FLOAT_TWEENER_CHANGED) {
			FloatTweener tweener = (FloatTweener)evData;
			if (tweener != null && tweener == animTweener)
				setY(tweener.getTweenedValue());
		} else if (evType == FloatTweener.EV_TYPE_FLOAT_TWEENER_COMPLETED) {
			FloatTweener tweener = (FloatTweener)evData;
			if (tweener != null && tweener == animTweener)
				setAnimating(false);
		} else if (evType == MenuButton.EV_TYPE_PRESSED) {
			MenuItem item = (MenuItem)evData;
			assert (pressedItem == null) : "MenuDialog: concurrently pressed items not permitted.";
			if (item != null)
				setPressedItem(item);
		} else if (evType == MenuButton.EV_TYPE_RAISED) {
			MenuItem item = (MenuItem)evData;
			assert (item == null || item == pressedItem) : "MenuDialog: concurrently pressed items not permitted.";
			if (item != null && item == pressedItem) {
				setPressedItem(null);
				dispatchEvent(EV_TYPE_ITEM_DID_ACTIVATE, item);
			}
		} else if (evType == MenuButton.EV_TYPE_CANCELLED){
			MenuItem item =(MenuItem)evData;
			assert (item == null || item == pressedItem) : "MenuDialog: concurrently pressed items not permitted.";
			if (item != null && item == pressedItem)
				setPressedItem(null);
		} else if (evType == GaugeMenuItem.EV_TYPE_GAUGE_CHANGED) {
			GaugeMenuItem item = (GaugeMenuItem)evData;
			if (item != null) {
				dispatchEvent(EV_TYPE_ITEM_DID_ACTIVATE, item);
				if (item.shouldPlaySound())
					scene.playSound("button");
			}
		}
	}
}
