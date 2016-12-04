package com.cheekymammoth.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.input.CMInputs;
import com.cheekymammoth.input.IInteractable;

public class ButtonsProxy extends EventDispatcher implements IInteractable, INavigable {
	private boolean locked;
    private int inputFocus;
    private UINavigator navigator;
    private MenuButton focusDeselectedButton;
    private Array<MenuButton> buttons = new Array<MenuButton>(true, 5, MenuButton.class);
    private Array<MenuButton> addQueue = new Array<MenuButton>(true, 2, MenuButton.class);
    private Array<MenuButton> removeQueue = new Array<MenuButton>(true, 2, MenuButton.class);
    
	public ButtonsProxy(int inputFocus, NavigationMap navMap) {
		this.inputFocus = inputFocus;
		navigator = new UINavigator(navMap);
	}
	
	private void setLocked(boolean value) {
		if (value == locked)
			return;
		locked = value;
		
		if (locked == false) {
			for (int i = 0; i < addQueue.size; i++)
	            addButton(addQueue.get(i));
	        addQueue.clear();
	        
	        for (int i = 0; i < removeQueue.size; i++)
	        	removeButton(addQueue.get(i));
	        removeQueue.clear();
		}
	}
	
	public boolean doesRepeat() { return navigator.doesRepeat(); }
	
	public void setRepeats(boolean value) { navigator.setRepeats(value); }
	
	public float getRepeatDelay() { return navigator.getRepeatDelay(); }
	
	public void setRepeatDelay(float value) { navigator.setRepeatDelay(value); }
	
	public int getNavIndex() { return navigator.getNavIndex(); }
	
	public int getNavCount() { return navigator.getNavCount(); }
	
	public MenuButton getSelectedButton() {
		for (int i = 0; i < buttons.size; i++) {
			MenuButton button = buttons.get(i);
			if (button.isSelected())
				return button;
		}
		
		return null;
	}
	
	public void addButton(MenuButton button) {
		if (button == null || buttons.contains(button, true))
            return;

        if (!locked) {
            buttons.add(button);

            if (navigator != null && button.isNavigable())
                navigator.addNav(button);
        } else {
        	removeQueue.removeValue(button, true);
            addQueue.add(button);
        }
	}
	
	public void removeButton(MenuButton button) {
		if (button == null)
            return;

        if (!locked) {
        	button.setSelected(false);
        	buttons.removeValue(button, true);
        	navigator.removeNav(button);
        	
            if (button == focusDeselectedButton)
                focusDeselectedButton = null;
        } else {
        	addQueue.removeValue(button, true);
            removeQueue.add(button);
        }
	}
	
	public void clear() {
		for (int i = buttons.size-1; i >= 0; i--)
			removeButton(buttons.get(i));
        focusDeselectedButton = null;
	}

	@Override
	public NavigationMap getNavMap() {
		return navigator.getNavMap();
	}

	@Override
	public void setNavMap(NavigationMap value) {
		navigator.setNavMap(value);
	}

	@Override
	public Actor getCurrentNav() {
		return navigator.getCurrentNav();
	}

	@Override
	public void resetNav() {
		navigator.resetNav();
	}

	@Override
	public void movePrevNav() {
		navigator.movePrevNav();
	}

	@Override
	public void moveNextNav() {
		navigator.moveNextNav();
	}

	@Override
	public int getInputFocus() {
		return inputFocus;
	}

	@Override
	public void didGainFocus() {
		if (focusDeselectedButton != null && focusDeselectedButton == getCurrentNav())
            focusDeselectedButton.setSelected(true);
        focusDeselectedButton = null;
	}

	@Override
	public void willLoseFocus() {
		MenuButton selectedButton = getSelectedButton();
        if (selectedButton != null) {
            selectedButton.setSelected(false);
            focusDeselectedButton = selectedButton;
        }
        else
            focusDeselectedButton = null;
	}

	@Override
	public void update(CMInputs input) {
		setLocked(true);
		for (int i = buttons.size-1; i >= 0; i--) {
			MenuButton button = buttons.get(i);
			if (button.isSelected()) {
				if (input.didDepress(CMInputs.CI_CONFIRM))
					button.depress();
				else if (input.didRaise(CMInputs.CI_CONFIRM))
					button.raise();
			} else
				button.raise();
		}
		setLocked(false);
		
		navigator.update(input);
	}
	
	public void advanceTime(float dt) {
		navigator.advanceTime(dt);
	}
}
