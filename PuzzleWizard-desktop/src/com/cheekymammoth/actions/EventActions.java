package com.cheekymammoth.actions;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.cheekymammoth.events.IEventListener;

public class EventActions {
	static public EventAction eventAction(IEventListener listener) {
		return eventAction(IEventListener.INVALID_EV_TYPE, null, listener, 0, null);
	}
	
	static public EventAction eventAction(int evType, IEventListener listener) {
		return eventAction(evType, null, listener, 0, null);
	}
	
	static public EventAction eventAction(Object evData, IEventListener listener) {
		return eventAction(IEventListener.INVALID_EV_TYPE, evData, listener, 0, null);
	}
	
	static public EventAction eventAction(int evType, Object evData, IEventListener listener, float duration) {
		return eventAction(evType, evData, listener, duration, null);
	}
	
	static public EventAction eventAction(int evType, Object evData, IEventListener listener, float duration, Interpolation interpolation) {
		EventAction action = Actions.action(EventAction.class);
		action.setEvType(evType);
		action.setEvData(evData);
		action.setListener(listener);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}
	
	static public RepeatEventAction repeatEventAction(int count, Action repeatedAction, IEventListener listener) {
		return repeatEventAction(
				count,
				repeatedAction,
				IEventListener.INVALID_EV_TYPE,
				null,
				listener);
	}
	
	static public RepeatAction foreverEventAction(Action repeatedAction, IEventListener listener) {
		return repeatEventAction(
				RepeatAction.FOREVER,
				repeatedAction,
				IEventListener.INVALID_EV_TYPE,
				null,
				listener);
	}
	
	static public RepeatAction foreverEventAction(Action repeatedAction,
			int evType, Object evData, IEventListener listener) {
		return repeatEventAction(
				RepeatAction.FOREVER,
				repeatedAction,
				evType,
				evData,
				listener);
	}
	
	static public RepeatEventAction repeatEventAction(int count, Action repeatedAction,
			int evType, Object evData, IEventListener listener) {
		RepeatEventAction action = Actions.action(RepeatEventAction.class);
        action.setCount(count);
        action.setAction(repeatedAction);
        action.setEvType(evType);
		action.setEvData(evData);
		action.setListener(listener);
        return action;
	}
}
