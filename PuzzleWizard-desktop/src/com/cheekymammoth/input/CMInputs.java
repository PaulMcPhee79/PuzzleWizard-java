package com.cheekymammoth.input;

import com.cheekymammoth.utils.Coord;

public class CMInputs {
	// Control input codes
	public static final int CI_NONE = 0;
	public static final int CI_UP = 1<<0;
	public static final int CI_DOWN = 1<<1;
	public static final int CI_LEFT = 1<<2;
	public static final int CI_RIGHT = 1<<3;
	public static final int CI_CONFIRM_ENTER = 1<<4;
	public static final int CI_CONFIRM_SPACEBAR = 1<<5;
	public static final int CI_CONFIRM = CI_CONFIRM_ENTER | CI_CONFIRM_SPACEBAR;
	public static final int CI_CANCEL = 1<<6;
	public static final int CI_MENU = 1<<7;
	public static final int CI_PREV_SONG = 1<<8;
	public static final int CI_NEXT_SONG = 1<<9;
	public static final int CI_PRT_SCR = 1<<10;
	public static final int CI_ALT = 1<<11;
	public static final int CI_ENTER = 1<<12;
	
	
	public static boolean isDown(int controllerState, int ctrlKey) {
		return (controllerState & ctrlKey) == ctrlKey;
	}
	
	// Notes:
	// Focus State : [8 bits category | 24 bir srate]
	// Has Focus : [8 bits category | 24 bits individual settings]
	public static final int NUM_STATE_BITS = 24;
	public static final int FOCUS_CAT_MASK = 0xff000000;
	public static final int HAS_FOCUS_MASK = ~FOCUS_CAT_MASK;
	
	// Focus categories
	public static final int FOCUS_CAT_TITLE = 1 << NUM_STATE_BITS;
	public static final int FOCUS_CAT_MENU = 1 << (NUM_STATE_BITS+1);
	public static final int FOCUS_CAT_PLAYFIELD = 1 << (NUM_STATE_BITS+2);
	public static final int FOCUS_CAT_ALL = FOCUS_CAT_MASK;
	
	// ***** Focus states *****
	public static final int FOCUS_STATE_NONE = 0;
	
	// FOCUS_CAT_TITLE
	public static final int FOCUS_STATE_TITLE = FOCUS_CAT_TITLE+1;
	
	// FOCUS_CAT_MENU
	public static final int FOCUS_STATE_MENU = FOCUS_CAT_MENU+1;
	public static final int FOCUS_STATE_PUZZLE_MENU = FOCUS_CAT_MENU+2;
	public static final int FOCUS_STATE_MENU_DIALOG = FOCUS_CAT_MENU+3;
	
	// FOCUS_CAT_PLAYFIELD
	public static final int FOCUS_STATE_PF_PLAYFIELD = FOCUS_CAT_PLAYFIELD+1;
	
	// ***** Individual bit settings *****
	
	// FOCUS_CAT_ALL
	public static final int HAS_FOCUS_ALL = FOCUS_CAT_ALL+HAS_FOCUS_MASK;
	
	// FOCUS_CAT_TITLE
	public static final int HAS_FOCUS_TITLE = FOCUS_CAT_TITLE+0x1;
	
	// FOCUS_CAT_MENU
	public static final int HAS_FOCUS_MENU = FOCUS_CAT_MENU+0x1;
	public static final int HAS_FOCUS_PUZZLE_MENU = FOCUS_CAT_MENU+0x2;
	public static final int HAS_FOCUS_MENU_DIALOG = FOCUS_CAT_MENU+0x4;
	public static final int HAS_FOCUS_MENU_ALL = FOCUS_CAT_MENU+HAS_FOCUS_MASK;
	
	// FOCUS_CAT_PLAYFIELD
	public static final int HAS_FOCUS_BOARD = FOCUS_CAT_PLAYFIELD+0x1;
	public static final int HAS_FOCUS_PAUSE_MENU = FOCUS_CAT_PLAYFIELD+0x2;
	public static final int HAS_FOCUS_PLAYFIELD_ALL = FOCUS_CAT_PLAYFIELD+HAS_FOCUS_MASK;
	
	private int inputMap;
	private int prevInputMap;
	private Coord depressedVector = new Coord();
	private Coord heldVector = new Coord();
	
	public CMInputs() {
		this(0, 0);
	}
	
	public CMInputs(int inputMap, int prevInputMap) {
		this.inputMap = inputMap;
		this.prevInputMap = prevInputMap;
	}
	
	int getInputMap() {
		return inputMap;
	}
	
	int getPrevInputMap() {
		return prevInputMap;
	}
	
	public Coord getDepressedVector() {
		return depressedVector;
	}
	
	public Coord getHeldVector() {
		return heldVector;
	}
	
	CMInputs set(int inputMap, int prevInputMap) {
		this.inputMap = inputMap;
		this.prevInputMap = prevInputMap;
		return this;
	}
	
	CMInputs set(CMInputs other) {
		this.inputMap = other.inputMap;
		this.prevInputMap = other.prevInputMap;
		this.depressedVector.set(other.depressedVector);
		this.heldVector.set(other.heldVector);
		return this;
	}
	
	void setDepressedVector(Coord value) {
		depressedVector.set(value);
	}
	
	void setHeldVector(Coord value) {
		heldVector.set(value);
	}
	
	public boolean didDepress(int ci_code) {
		// Don't confirm when user is attempting to toggle fullscreen mode.
		if (ci_code == CI_CONFIRM && (inputMap & CI_CONFIRM_ENTER) != 0 && (inputMap & CI_ALT) != 0)
			return false;
		else
			return (inputMap & ci_code) != 0 && (prevInputMap & ci_code) == 0;
	}
	
	public boolean isPressed(int ci_code) {
		return (inputMap & ci_code) != 0;
	}
	
	public boolean didRaise(int ci_code) {
		return (inputMap & ci_code) == 0 && (prevInputMap & ci_code) != 0;
	}
	
	public void clearInputs(int inputMap, int prevInputMap) {
		inputMap &= ~inputMap;
		prevInputMap &= ~prevInputMap;
	}
}
