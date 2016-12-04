package com.cheekymammoth.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.cheekymammoth.input.CMInputs;
import com.cheekymammoth.utils.Coord;

public class UINavigator implements INavigable {
	private boolean repeats;
	private int repeatDir;
	private float repeatCounter;
	private float repeatDelay = 0.25f;
	private int navIndex;
	private NavigationMap navMap;
	private Array<Actor> navigables = new Array<Actor>(true, 5, Actor.class);
	
	public UINavigator(NavigationMap navMap) {
		this.navMap = navMap;
	}
	
	public boolean doesRepeat() { return repeats; }
	
	public void setRepeats(boolean value) { repeats = value; }
	
	public float getRepeatDelay() { return repeatDelay; }
	
	public void setRepeatDelay(float value) { repeatDelay = value; }
	
	public int getNavIndex() { return navIndex; }
	
	public int getNavCount() { return navigables.size; }
	
	private void setRepeatDir(int value) {
		if (repeatDir != value) {
            repeatDir = value;
            repeatCounter = 2 * repeatDelay;
        }
	}
	
	public void addNav(Actor nav) {
		if (nav != null && !navigables.contains(nav, true)) {
			navigables.add(nav);

            if (navigables.size == 1)
                activateNav(nav);
        }
	}
	
	public void removeNav(Actor nav) {
		if (nav != null) {
            if (nav == getCurrentNav()) {
                deactivateNav(nav);
                navigables.removeValue(nav, true);
                movePrevNav();
            }
            else
            	navigables.removeValue(nav, true);
        }
	}
	
	public void activateNextActiveNav(int dir) {
		if (getCurrentNav() != null && navigables.size > 0 && (dir == 1 || dir == -1)) {
            int startIndex = navigables.indexOf(getCurrentNav(), true);
            int i = startIndex + dir;
            while (i != startIndex)  {
                if (dir == 1)
                    moveNextNav();
                else
                    movePrevNav();

                Actor nav = getCurrentNav();
                if (nav != null && nav.isVisible())
                    break;

                if (i < 0) {
                    i = navigables.size - 1;
                    continue;
                } else if (i >= navigables.size) {
                    i = 0;
                    continue;
                }

                i += dir;
            }
        }
	}
	
	private void activateNav(Actor nav)
    {
        if (nav != null && nav instanceof MenuButton)
            ((MenuButton)nav).setSelected(true);
    }

    private void deactivateNav(Actor nav)
    {
        if (nav != null && nav instanceof MenuButton)
        	((MenuButton)nav).setSelected(false);
    }

	@Override
	public NavigationMap getNavMap() { return navMap; }

	@Override
	public void setNavMap(NavigationMap value) {
		assert(value != null) : "Null NavigationMap in UINavigator::setNavMap.";
		navMap = value;
	}

	@Override
	public Actor getCurrentNav() {
		return (navIndex < navigables.size) ? navigables.get(navIndex) : null; 
	}

	@Override
	public void resetNav() {
		deactivateNav(getCurrentNav());
        navIndex = 0;

        if (getCurrentNav() != null && getCurrentNav().isVisible())
            activateNav(getCurrentNav());
        else
            activateNextActiveNav(1);
	}

	@Override
	public void movePrevNav() {
		if (navigables.size > 0) {
            deactivateNav(getCurrentNav());
            --navIndex;

            if (navIndex < 0)
                navIndex = navigables.size - 1;

            activateNav(getCurrentNav());
        }
	}

	@Override
	public void moveNextNav() {
		if (navigables.size > 0) {
            deactivateNav(getCurrentNav());
            ++navIndex;

            if (navIndex >= navigables.size)
                navIndex = 0;

            activateNav(getCurrentNav());
        }
	}
	
	public void update(CMInputs input) {
        Coord depressedVec = input.getDepressedVector(), heldVec = input.getHeldVector();
        int didNavigate = 0, didRepeat = 0;

        if (navMap == NavigationMap.NAV_VERT || navMap == NavigationMap.NAV_OMNI) {
            if (depressedVec.y == -1) {
                movePrevNav();
                didNavigate = -1;
            } else if (depressedVec.y == 1) {
                moveNextNav();
                didNavigate = 1;
            }

            if (repeats && didNavigate == 0) {
                if (heldVec.y == -1) {
                    setRepeatDir(-1);
                    didRepeat = -1;
                } else if (heldVec.y == 1) {
                	setRepeatDir(1);
                    didRepeat = 1;
                }
            }
        }

        if (didNavigate == 0 && (navMap == NavigationMap.NAV_HORIZ || navMap == NavigationMap.NAV_OMNI)) {
            if (depressedVec.x == -1) {
                movePrevNav();
                didNavigate = -1;
            } else if (depressedVec.x == 1) {
                moveNextNav();
                didNavigate = 1;
            }

            if (repeats && didRepeat == 0 && didNavigate == 0) {
                if (heldVec.x == -1) {
                	setRepeatDir(-1);
                    didRepeat = -1;
                } else if (heldVec.x == 1) {
                	setRepeatDir(1);
                    didRepeat = 1;
                }
            }
        }

        if (didRepeat == 0 || didNavigate != 0)
            setRepeatDir(0);

        // Skip invisible navs
        if (didNavigate != 0 && getCurrentNav() != null && !getCurrentNav().isVisible())
            activateNextActiveNav(didNavigate);
	}
	
	public void advanceTime(float dt) {
		if (repeats && repeatDir != 0) {
            repeatCounter -= dt;

            if (repeatCounter <= 0) {
                repeatCounter = repeatDelay;

                if (repeatDir == -1)
                    movePrevNav();
                else
                    moveNextNav();

                if (getCurrentNav() != null && !getCurrentNav().isVisible())
                    activateNextActiveNav(repeatDir);
            }
        }
	}
}
