package com.cheekymammoth.input;

import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntMap.Keys;
import com.cheekymammoth.utils.Coord;

public class GamePadState {
	private boolean isDepressedVectorDirty = true;
	private boolean isHeldVectorDirty = true;
	private IntMap<Button> buttons = new IntMap<Button>(10);
	private IntMap<DPad> dpads = new IntMap<DPad>(2);
	private Array<Thumbstick> thumbsticks = new Array<Thumbstick>(true, 2, Thumbstick.class);
	private GamePadDescriptor descriptor;
	
	public GamePadState(GamePadDescriptor descriptor) {
		this.descriptor = descriptor;
	}
	
	private Thumbstick thumbstickForAxisIndex(int axisIndex) {
		for (int i = 0; i < thumbsticks.size; i++) {
			Thumbstick thumbstick = thumbsticks.get(i);
			if (thumbstick.hasAxis(axisIndex))
				return thumbstick;
		}

		return null;
	}
	
	public void reset() {
		Keys keys = buttons.keys();
		while (keys.hasNext)
			buttons.get(keys.next()).reset();
		
		keys = dpads.keys();
		while (keys.hasNext)
			dpads.get(keys.next()).reset();
		
		for (int i = 0; i < thumbsticks.size; i++)
			thumbsticks.get(i).reset();
		
		isDepressedVectorDirty = isHeldVectorDirty = true;
	}
	
	public void update() {
		Keys keys = buttons.keys();
		while (keys.hasNext) {
			Button button = buttons.get(keys.next());
			button.update();
		}
		
		keys = dpads.keys();
		while (keys.hasNext) {
			DPad dpad = dpads.get(keys.next());
			dpad.update();
		}
		
		for (int i = 0; i < thumbsticks.size; i++) {
			Thumbstick thumbstick = thumbsticks.get(i);
			thumbstick.update();
		}
		
		isDepressedVectorDirty = isHeldVectorDirty = true;
	}
	
	public boolean isPressed(int normalizedButtonCode) {
		return getButton(normalizedButtonCode).isPressed();
	}
	
	public boolean wasPressed(int normalizedButtonCode) {
		return getButton(normalizedButtonCode).wasPressed();
	}
	
	public boolean didDepress(int normalizedButtonCode) {
		return getButton(normalizedButtonCode).didDepress();
	} 

	public boolean didRaise(int normalizedButtonCode) {
		return getButton(normalizedButtonCode).didRaise();
	}
	
	public int getButtonState() {
		int buttonState = 0;
		
		Keys keys = buttons.keys();
		while (keys.hasNext) {
			int key = keys.next();
			if (buttons.get(key).isPressed())
				buttonState |= key;
		}
		
		return buttonState;
	}
	
	private Coord depressedVector = new Coord();
	public Coord getDepressedVector() {
		if (isDepressedVectorDirty) {
			isDepressedVectorDirty = false;
			depressedVector.set(0, 0);
			
			for (int i = 0; i < thumbsticks.size; i++) {
				Thumbstick thumbstick = thumbsticks.get(i);
				Keys keys = thumbstick.getAxisIndexes();
				while (keys.hasNext) {
					int axisIndex = keys.next();
					int tsState = descriptor.normalizeAxis(axisIndex, thumbstick.getDidActivateDir(axisIndex));
					if (CMInputs.isDown(tsState, CMInputs.CI_LEFT))
						depressedVector.x = -1;
					else if (CMInputs.isDown(tsState, CMInputs.CI_RIGHT))
						depressedVector.x = 1;
					else if (CMInputs.isDown(tsState, CMInputs.CI_UP))
						depressedVector.y = -1;
					else if (CMInputs.isDown(tsState, CMInputs.CI_DOWN))
						depressedVector.y = 1;
					else
						continue;
					break;
				}
			}
			
			if (depressedVector.isOrigin()) {
				Keys keys = dpads.keys();
				while (keys.hasNext) {
					DPad dpad = dpads.get(keys.next());
					
					int dpadState = descriptor.normalizePov(dpad.getDepressedDir());
					if (CMInputs.isDown(dpadState, CMInputs.CI_LEFT))
						depressedVector.x = -1;
					else if (CMInputs.isDown(dpadState, CMInputs.CI_RIGHT))
						depressedVector.x = 1;
					else if (CMInputs.isDown(dpadState, CMInputs.CI_UP))
						depressedVector.y = -1;
					else if (CMInputs.isDown(dpadState, CMInputs.CI_DOWN))
						depressedVector.y = 1;
					else
						continue;
					break;
				}
			}
		}
		return depressedVector;
	}
	
	private Coord heldVector = new Coord();
	public Coord getHeldVector() {
		if (isHeldVectorDirty) {
			isHeldVectorDirty = false;
			heldVector.set(0, 0);
			
			for (int i = 0; i < thumbsticks.size; i++) {
				Thumbstick thumbstick = thumbsticks.get(i);
				Keys keys = thumbstick.getAxisIndexes();
				while (keys.hasNext) {
					int axisIndex = keys.next();
					int tsState = descriptor.normalizeAxis(axisIndex, thumbstick.getActivatedDir(axisIndex));
					if (CMInputs.isDown(tsState, CMInputs.CI_LEFT))
						heldVector.x = -1;
					else if (CMInputs.isDown(tsState, CMInputs.CI_RIGHT))
						heldVector.x = 1;
					else if (CMInputs.isDown(tsState, CMInputs.CI_UP))
						heldVector.y = -1;
					else if (CMInputs.isDown(tsState, CMInputs.CI_DOWN))
						heldVector.y = 1;
					else
						continue;
					break;
				}
			}
			
			if (heldVector.isOrigin()) {
				Keys keys = dpads.keys();
				while (keys.hasNext) {
					DPad dpad = dpads.get(keys.next());
					
					int dpadState = descriptor.normalizePov(dpad.getPressedDir());
					if (CMInputs.isDown(dpadState, CMInputs.CI_LEFT))
						heldVector.x = -1;
					else if (CMInputs.isDown(dpadState, CMInputs.CI_RIGHT))
						heldVector.x = 1;
					else if (CMInputs.isDown(dpadState, CMInputs.CI_UP))
						heldVector.y = -1;
					else if (CMInputs.isDown(dpadState, CMInputs.CI_DOWN))
						heldVector.y = 1;
					else
						continue;
					break;
				}
			}
		}
		return heldVector;
	}

	protected Button getButton(int buttonCode) {
		if (!buttons.containsKey(buttonCode))
			buttons.put(buttonCode, new Button());
		return buttons.get(buttonCode);
	}
	
	public void setButton(int buttonCode, boolean pressed) {
		PovDirection dir = descriptor.button2PovDir(buttonCode);
		if (dir == null)
			getButton(descriptor.normalizeButton(buttonCode)).setState(pressed);
		else
			setDPad(0, pressed ? dir : PovDirection.center);
	}
	
	protected DPad getDPad(int povIndex) {
		if (!dpads.containsKey(povIndex))
			dpads.put(povIndex, new DPad());
		return dpads.get(povIndex);
	}
	
	public void setDPad(int povIndex, PovDirection povDir) {
		getDPad(povIndex).setPovDirection(povDir);
		isDepressedVectorDirty = isHeldVectorDirty = true;
	}
	
	public void addThumbstick(int axisIndex) {
		if (!descriptor.isMovementAxis(axisIndex))
			return;
		
		Coord axes = descriptor.axesForAxisIndex(axisIndex);
		thumbsticks.add(new Thumbstick(axes.x, axes.y));
	}
	
	protected Thumbstick getThumbstick(int axisIndex) {
		return thumbstickForAxisIndex(axisIndex);
	}
	
	public void setThumbstick(int axisIndex, float value) {
		if (!descriptor.isMovementAxis(axisIndex))
			return;
		
		Thumbstick thumbstick = getThumbstick(axisIndex);
		if (thumbstick == null) {
			Coord axes = descriptor.axesForAxisIndex(axisIndex);
			thumbstick = new Thumbstick(axes.x, axes.y);
			thumbsticks.add(thumbstick);
		}
		thumbstick.setValue(axisIndex, value);
		isDepressedVectorDirty = isHeldVectorDirty = true;
	}
}
