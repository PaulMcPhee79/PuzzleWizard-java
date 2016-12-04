package com.cheekymammoth.input;

import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntMap.Keys;
import com.cheekymammoth.utils.DFloat;
import com.cheekymammoth.utils.DirtyBoolean;

public class Thumbstick {
	public static final float kActivationThreshold = 0.65f;
	public static final float kDeactivationThreshold = 0.35f;
	
	private final DirtyBoolean defaultState = new DirtyBoolean();
	private final DFloat defaultValue = new DFloat();
	private IntMap<DirtyBoolean> axisStates = new IntMap<DirtyBoolean>(2);
	private IntMap<DFloat> axisValues = new IntMap<DFloat>(2);
	
	public Thumbstick(int xAxisIndex, int yAxisIndex) {
		axisValues.put(xAxisIndex, new DFloat());
		axisValues.put(yAxisIndex, new DFloat());
	}
	
	private void updateActiveState(int axisIndex) {
		float axisValue = getAxisValue(axisIndex);
		DirtyBoolean activeState = axisStates.get(axisIndex);
		boolean newState = activeState.isOn()
				? Math.abs(axisValue) > kDeactivationThreshold
				: Math.abs(axisValue) > kActivationThreshold;
				activeState.setState(newState);
	}
	
	public void reset() {
		Keys keys = axisStates.keys();
		while (keys.hasNext)
			axisStates.get(keys.next()).reset();
		
		keys = axisValues.keys();
		while (keys.hasNext)
			axisValues.get(keys.next()).reset();
	}
	
	public void update() {
		Keys keys = axisStates.keys();
		while (keys.hasNext) {
			int key = keys.next();
			DirtyBoolean activeState = axisStates.get(key);
			activeState.update();
			setValue(key, getAxisValue(key));
			activeState.update();
		}
	}
	
	public boolean hasAxis(int axisIndex) {
		return axisValues.containsKey(axisIndex);
	}
	
	public Keys getAxisIndexes() {
		return axisStates.keys();
	}
	
	public float getAxisValue(int axisIndex) {
		return axisValues.get(axisIndex, defaultValue).getValue();
	}
	
	public float getPrevAxisValue(int axisIndex) {
		return axisValues.get(axisIndex, defaultValue).getPrevValue();
	}
	
	public void setValue(int axisIndex, float value) {
		if (axisValues.containsKey(axisIndex))
			axisValues.get(axisIndex).setValue(value);
		else
			axisValues.put(axisIndex, new DFloat(value, 0));
		
		if (!axisStates.containsKey(axisIndex))
			axisStates.put(axisIndex, new DirtyBoolean());
		updateActiveState(axisIndex);
	}
	
	public int getActivatedDir(int axisIndex) {
		if (isActivated(axisIndex))
			return getAxisValue(axisIndex) > 0 ? 1 : -1;
		else
			return 0;
	}
	
	public int getDidActivateDir(int axisIndex) {
		if (didActivate(axisIndex))
			return getActivatedDir(axisIndex);
		else
			return 0;
	}
	
	public int getWasActivatedDir(int axisIndex) {
		if (didDeactivate(axisIndex))
			return getPrevAxisValue(axisIndex) > 0 ? 1 : -1;
		else
			return 0;
	}
	
	public boolean isActivated(int axisIndex) {
		return axisStates.get(axisIndex, defaultState).isOn();
	}
	
	public boolean didActivate(int axisIndex) {
		return axisStates.get(axisIndex, defaultState).didTurnOn();
	}
	
	public boolean didDeactivate(int axisIndex) {
		return axisStates.get(axisIndex, defaultState).didTurnOff();
	}
}
