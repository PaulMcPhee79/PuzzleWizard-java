package com.cheekymammoth.ui;

import com.cheekymammoth.input.CMInputs;
import com.cheekymammoth.input.InputManager;
import com.cheekymammoth.ui.INavigable.NavigationMap;
import com.cheekymammoth.utils.Coord;

public class MenuGridDialog extends MenuDialog {
	private boolean isInterceptingInput;
	private int columnLen;
	private int prevNavIndex;
	private Coord prevNav = new Coord();

	public MenuGridDialog(int category, int priority, int inputFocus, int columnLen) {
		super(category, priority, inputFocus, NavigationMap.NAV_OMNI);
		this.columnLen = Math.max(1, columnLen);
	}
	
	private int getColumnLen() { return columnLen; }
	
	private int getNumColumns() {
		return getColumnLen() == 0 ? 1 : getNavCountMinusOne() / getColumnLen() +
				Math.min(1,  getNavCountMinusOne() & getColumnLen()); // TODO should this be mod(%)?
	}
	
	private int getNavCount() { return buttonsProxy.getNavCount(); }
	
	private int getNavCountMinusOne() { return getNavCount() - 1; }
	
	private int getInterceptIndex() { return getNavCountMinusOne(); }
	
	private boolean isInterceptingInput() { return isInterceptingInput; }
	
	private void setInterceptingInput(boolean value) { isInterceptingInput = value; }
	
	private void processNavInput(int prevNavIndex) {
		int horizNav = prevNav.x, vertNav = prevNav.y;
		
		if (isInterceptingInput()) {
			if (vertNav != 0) {
				if (vertNav == -1)
					navigateToIndex(
							Math.min(getNavCountMinusOne() - 1,
							(this.prevNavIndex / getColumnLen()) * getColumnLen() + (getColumnLen() - 1)));
				else
					navigateToIndex((this.prevNavIndex / getColumnLen()) * getColumnLen());
				
				setInterceptingInput(false);
			} else if (horizNav != 0)
				navigateToIndex(getInterceptIndex());
		} else {
			if (vertNav != 0) {
				if ((isTopRow(prevNavIndex) && vertNav == -1) || (isBtmRow(prevNavIndex) && vertNav == 1)) {
					this.prevNavIndex = prevNavIndex;
					navigateToIndex(getInterceptIndex());
					setInterceptingInput(true);
				}
			} else if (horizNav != 0) {
				if (horizNav == -1) {
					int navIndex = prevNavIndex - getColumnLen();
					if (navIndex < 0)
						navIndex = Math.min(getNavCountMinusOne() - 1, navIndex + getNumColumns() * getColumnLen());
					navigateToIndex(navIndex);
				} else {
					int navIndex = prevNavIndex + getColumnLen();
					if (navIndex >= getNavCountMinusOne()) {
						if (getNavCountMinusOne() % getColumnLen() != 0 && 
								prevNavIndex / getColumnLen() < getNumColumns() - 1)
							// Penultimate column, but final column is too short. Clamp to last index.
							navIndex = getNavCountMinusOne() - 1;
						else
							// Wrap around to the first column
							navIndex = prevNavIndex % getColumnLen();
					}
					navigateToIndex(navIndex);
				}
			}
		}
	}
	
	private void navigateToIndex(int navIndex) {
		if (navIndex >= 0 && navIndex < getNavCount()) {
			while (buttonsProxy.getNavIndex() != navIndex)
				buttonsProxy.moveNextNav();
		}
	}
	
	private boolean isTopRow(int navIndex) {
		return navIndex % getColumnLen() == 0;
	}
	
	private boolean isBtmRow(int navIndex) {
		return navIndex % getColumnLen() == getColumnLen() - 1 || navIndex == getNavCountMinusOne() - 1;
	}
	
	private int didNavigateHorizontally() {
		InputManager im = InputManager.IM();
		Coord navVec = im.getDepressedVector();
		if (navVec.x == 0)
			navVec = im.getHeldVector();
		return navVec.x;
	}
	
	private int didNavigateVertically() {
		InputManager im = InputManager.IM();
		Coord navVec = im.getDepressedVector();
		if (navVec.y == 0)
			navVec = im.getHeldVector();
		return navVec.y;
	}
	
	@Override
	public void resetNav() {
		super.resetNav();
		setInterceptingInput(false);
	}

	@Override
	public void update(CMInputs input) {
		int prevNavIndex = buttonsProxy.getNavIndex();
		super.update(input);
		
		if (prevNavIndex != buttonsProxy.getNavIndex()) {
			prevNav.y = didNavigateVertically();
			prevNav.x = prevNav.y == 0 ? didNavigateHorizontally() : 0;
			processNavInput(prevNavIndex);
		}
	}
	
	@Override
	public void advanceTime(float dt) {
		if (!isInterceptingInput() || prevNav.y != 0) {
			int prevNavIndex = buttonsProxy.getNavIndex();
			super.advanceTime(dt);
			
			if (prevNavIndex != buttonsProxy.getNavIndex()) {
				prevNav.y = didNavigateVertically();
				prevNav.x = prevNav.y == 0 ? didNavigateHorizontally() : 0;
				processNavInput(prevNavIndex);
			}
		}
	}
}
