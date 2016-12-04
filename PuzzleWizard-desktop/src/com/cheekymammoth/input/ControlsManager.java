package com.cheekymammoth.input;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntMap.Keys;
import com.badlogic.gdx.utils.ObjectMap;
import com.cheekymammoth.utils.Coord;
import com.cheekymammoth.utils.DBoolean;
import com.sun.jna.Platform;

public class ControlsManager implements InputProcessor, ControllerListener {
	private static ControlsManager singleton;
	private static final float kMouseHideTimeout = 3f;

	private boolean cursorFadeEnabled;
	private float cursorHideTimer;
	private Cursor hiddenCursor;
	private CMInputs inputMap = new CMInputs();
	private final DBoolean defaultKeyState = new DBoolean();
	private IntIntMap keyMap = new IntIntMap(16);
	private IntMap<DBoolean> keyStates = new IntMap<DBoolean>(16);
	private ObjectMap<Controller, GamePadState> gamePads =
			new ObjectMap<Controller, GamePadState>(4);
	private InputProcessor touchProxy;
	
	private ControlsManager() {
		try {
			hiddenCursor = new Cursor(1, 1, 0, 0, 1, BufferUtils.createIntBuffer(1), null);
		} catch (LWJGLException e) {
			Gdx.app.log("Failed to create mouse cursor: ", e.getMessage());
		}
	}
	
	public static ControlsManager CM() {
		if (singleton == null)
			singleton = new ControlsManager();
        return singleton;
    }
	
	public void setTouchProxy(InputProcessor proxy) {
		touchProxy = proxy;
	}
	
	public void invalidateGamepads() {
		if (gamePads != null)
			gamePads.clear();
		setDirtyFlags();
	}
	
	private static GamePadDescriptor descriptorForName(String name) {
		if (name != null) {
			if ((name.toLowerCase().contains("xbox") || name.toLowerCase().contains("microsoft")) &&
					name.contains("360")) {
				if (Platform.isLinux())
					return new Xbox360LinuxPad();
				else if (Platform.isMac())
					return new Xbox360MacPad();
				else
					return new Xbox360Pad();
			} else if (name.toLowerCase().contains("motioninjoy")) {
				return new PS3Pad();
			}
		}

		return null;
	}
	
	public void enableCursorFading(boolean enable) {
		if (cursorFadeEnabled != enable) {
			cursorFadeEnabled = enable;
			showMouseCursor(enable ? kMouseHideTimeout : 0);
		}
	}
	
	public void showMouseCursor(float timeout) {
		try {
			Mouse.setNativeCursor(null);
			cursorHideTimer = timeout;
		} catch (LWJGLException e) {
			Gdx.app.log("Failed to show cursor: ", e.getMessage());
		}
	}
	
	public void hideMouseCursor() {
		try {
			if (hiddenCursor != null)
				Mouse.setNativeCursor(hiddenCursor);
		} catch (LWJGLException e) {
			Gdx.app.log("Failed to hide cursor: ", e.getMessage());
		}
	}
	
	private GamePadState gamePadStateForController(Controller controller) {
		GamePadState gpState = null;
		
		if (controller != null) {
			gpState = gamePads.get(controller);
			if (gpState == null) {
				GamePadDescriptor descriptor = descriptorForName(controller.getName());
				if (descriptor != null) {
					gpState = new GamePadState(descriptor);
					addController(controller, gpState);
				}
			}
		}
		return gpState;
	}
	
	private void addController(Controller controller, GamePadState gpState) {
		gamePads.put(controller, gpState);
		Gdx.app.log("Added Controller", controller.getName());
	}

	private boolean isKeyRegistered(int key) {
		return keyStates.containsKey(key);
	}
	
	private boolean wasKeyDown(int key) {
		return keyStates.get(key, defaultKeyState).wasOn();
	}
	
	private boolean wasKeyUp(int key) {
		return !wasKeyDown(key);
	}
	
	public void clearKeyStates() {
		Keys kbKeys = keyStates.keys();
		while (kbKeys.hasNext) {
			DBoolean keyState = keyStates.get(kbKeys.next());
			keyState.setState(false);
		}
		
		setDirtyFlags();
	}
	
	public void registerKeys(IntIntMap keys) {
		if (keys != null) {
			IntIntMap.Keys kbKeys = keys.keys();
			while (kbKeys.hasNext) {
				int kbKey = kbKeys.next();
				keyMap.put(kbKey, keys.get(kbKey, CMInputs.CI_NONE));
				keyStates.put(kbKey, new DBoolean());
			}
		}
	}

	public void unregisterKeys(int[] keys) {
		if (keys != null) {
			for (int i = 0, n = keys.length; i < n; i++) {
				keyMap.remove(keys[i], CMInputs.CI_NONE);
				keyStates.remove(keys[i]);
			}
		}
	}

	public void clearRegisteredKeys() {
		keyMap.clear();
		keyStates.clear();
	}
	
	public boolean isKeyDown(int key) {
		return keyStates.get(key, defaultKeyState).isOn();
	}
	
	public boolean isKeyUp(int key) {
		return !isKeyDown(key);
	}
	
	public boolean didKeyDepress(int key) {
		return wasKeyUp(key) && isKeyDown(key);
	}
	
	public boolean didKeyRelease(int key) {
		return wasKeyDown(key) && isKeyUp(key);
	}
	
	public boolean didInputPress(int ci_code) {
		return inputMap.didDepress(ci_code);
	}
	
	public boolean isInputPressed(int ci_code) {
		return inputMap.isPressed(ci_code);
	}
	
	public boolean didInputRaise(int ci_code) {
		return inputMap.didRaise(ci_code);
	}

	@Override
	public boolean keyDown(int keycode) {
		if (isKeyRegistered(keycode)) {
			keyStates.get(keycode).setState(true);
			setDirtyFlags();
		}
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (isKeyRegistered(keycode)) {
			keyStates.get(keycode).setState(false);
			setDirtyFlags();
		}
		return true;
	}

	@Override
	public boolean keyTyped(char character) {
		return true;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (cursorFadeEnabled)
			showMouseCursor(0);
		
		if (touchProxy != null)
			return touchProxy.touchDown(screenX, screenY, pointer, button);
		else
			return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (cursorFadeEnabled)
			showMouseCursor(kMouseHideTimeout);
		
		if (touchProxy != null)
			return touchProxy.touchUp(screenX, screenY, pointer, button);
		else
			return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if (touchProxy != null)
			return touchProxy.touchDragged(screenX, screenY, pointer);
		else
			return true;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		if (cursorFadeEnabled)
			showMouseCursor(kMouseHideTimeout);
		
		if (touchProxy != null)
			return touchProxy.mouseMoved(screenX, screenY);
		else
			return true;
	}

	@Override
	public boolean scrolled(int amount) {
		if (touchProxy != null)
			return touchProxy.scrolled(amount);
		else
			return true;
	}
	
	public CMInputs getInputMap() {
		if (isInputMapsDirty) {
			isInputMapsDirty = false;
			refreshInputMaps();
		}
		
		return inputMap;
	}
	
	private boolean isInputMapsDirty = true;
	private void refreshInputMaps() {
		int actionMap = 0;
		Keys keys = keyStates.keys();
		while (keys.hasNext) {
			int key = keys.next();
			DBoolean keyState = keyStates.get(key, defaultKeyState);
			if (keyState.isOn())
				actionMap |= keyMap.get(key, CMInputs.CI_NONE);
		}
		
		actionMap |= getGamePadButtonState();
		inputMap.set(actionMap, inputMap.getInputMap());
		inputMap.setDepressedVector(getDepressedVector());
		inputMap.setHeldVector(getHeldVector());
	}
	
	public void update(float dt) {
		Keys kbKeys = keyStates.keys();
		while (kbKeys.hasNext) {
			DBoolean keyState = keyStates.get(kbKeys.next());
			keyState.setState(keyState.isOn());
		}
		
		ObjectMap.Keys<Controller> gpKeys = gamePads.keys();
		while (gpKeys.hasNext) {
			GamePadState gps = gamePads.get(gpKeys.next());
			gps.update();
		}
		
		if (cursorHideTimer > 0) {
			cursorHideTimer -= dt;
			if (cursorHideTimer <= 0)
				hideMouseCursor();
		}
		
		setDirtyFlags();
	}

	@Override
	public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
		return false;
	}

	@Override
	public boolean axisMoved(Controller controller, int axisCode, float value) {
		//Gdx.app.log("axisMoved", "axisCode = " + axisCode + " value = " + value);
		GamePadState gps = gamePadStateForController(controller);
		if (gps != null) {
			gps.setThumbstick(axisCode, value);
			setDirtyFlags();
		}
		return false;
	}

	@Override
	public boolean buttonDown(Controller controller, int buttonCode) {
		//Gdx.app.log("buttonDown", "buttonCode = " + buttonCode);
		GamePadState gps = gamePadStateForController(controller);
		if (gps != null) {
			gps.setButton(buttonCode, true);
			setDirtyFlags();
		}
		return false;
	}

	@Override
	public boolean buttonUp(Controller controller, int buttonCode) {
		GamePadState gps = gamePadStateForController(controller);
		if (gps != null) {
			gps.setButton(buttonCode, false);
			setDirtyFlags();
		}
		return false;
	}

	@Override
	public void connected(Controller controller) { }

	@Override
	public void disconnected(Controller controller) { }

	@Override
	public boolean povMoved(Controller controller, int povCode, PovDirection value) {
		//Gdx.app.log("povMoved", "povCode = " + povCode + " PovDirection = " + value.toString());
		GamePadState gps = gamePadStateForController(controller);
		if (gps != null) {
			gps.setDPad(povCode, value);
			setDirtyFlags();
		}
		return false;
	}

	@Override
	public boolean xSliderMoved(Controller controller, int sliderCode, boolean value) {
		return false;
	}

	@Override
	public boolean ySliderMoved(Controller controller, int sliderCode, boolean value) {
		return false;
	}
	
	public int getGamePadButtonState() {
		int buttonState = 0;
		
		ObjectMap.Keys<Controller> gpKeys = gamePads.keys();
		while (gpKeys.hasNext) {
			GamePadState gps = gamePads.get(gpKeys.next());
			buttonState |= gps.getButtonState();
		}
		
		return buttonState;
	}
	
	private void setDirtyFlags() {
		isInputMapsDirty = isDepressedVectorDirty = isHeldVectorDirty = true;
	}
	
	private boolean isDepressedVectorDirty = true;
	private Coord depressedVector = new Coord();
	public Coord getDepressedVector() {
		if (isDepressedVectorDirty) {
			isDepressedVectorDirty = false;
			depressedVector.set(0, 0);
			
			ObjectMap.Keys<Controller> gpKeys = gamePads.keys();
			while (gpKeys.hasNext && depressedVector.isOrigin()) {
				GamePadState gps = gamePads.get(gpKeys.next());
				depressedVector.set(gps.getDepressedVector());
			}
			
			if (depressedVector.isOrigin()) {
				if (didKeyDepress(Input.Keys.LEFT) || didKeyDepress(Input.Keys.A))
					depressedVector.x = -1;
				else if (didKeyDepress(Input.Keys.RIGHT) || didKeyDepress(Input.Keys.D))
					depressedVector.x = 1;
				else if (didKeyDepress(Input.Keys.UP) || didKeyDepress(Input.Keys.W))
					depressedVector.y = -1;
				else if (didKeyDepress(Input.Keys.DOWN) || didKeyDepress(Input.Keys.S))
					depressedVector.y = 1;
			}
		}
		
		return depressedVector;
	}
	
	private boolean isHeldVectorDirty = true;
	private Coord heldVector = new Coord();
	public Coord getHeldVector() {
		if (isHeldVectorDirty) {
			isHeldVectorDirty = false;
			heldVector.set(0, 0);
			
			ObjectMap.Keys<Controller> gpKeys = gamePads.keys();
			while (gpKeys.hasNext && heldVector.isOrigin()) {
				GamePadState gps = gamePads.get(gpKeys.next());
				heldVector.set(gps.getHeldVector());
			}
			
			if (heldVector.isOrigin()) {
				if (isKeyDown(Input.Keys.LEFT) || isKeyDown(Input.Keys.A))
					heldVector.x = -1;
				else if (isKeyDown(Input.Keys.RIGHT) || isKeyDown(Input.Keys.D))
					heldVector.x = 1;
				else if (isKeyDown(Input.Keys.UP) || isKeyDown(Input.Keys.W))
					heldVector.y = -1;
				else if (isKeyDown(Input.Keys.DOWN) || isKeyDown(Input.Keys.S))
					heldVector.y = 1;
			}
		}
		
		return heldVector;
	}
}
