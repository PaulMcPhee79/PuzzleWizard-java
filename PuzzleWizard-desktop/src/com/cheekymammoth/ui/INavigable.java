package com.cheekymammoth.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;

public interface INavigable {
	public enum NavigationMap { NAV_NONE, NAV_VERT, NAV_HORIZ, NAV_OMNI };
	
	NavigationMap getNavMap();
	void setNavMap(NavigationMap value);
	Actor getCurrentNav();
	void resetNav();
	void movePrevNav();
	void moveNextNav();
}
