package com.cheekymammoth.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.events.IEventListener;

public class TouchPad extends Prop {
	public static final int EV_TYPE_TOUCH_DOWN;
	public static final int EV_TYPE_TOUCH_UP;
	public static final int EV_TYPE_TOUCH_DRAGGED;
	
	static {
		EV_TYPE_TOUCH_DOWN = EventDispatcher.nextEvType();
		EV_TYPE_TOUCH_UP = EventDispatcher.nextEvType();
		EV_TYPE_TOUCH_DRAGGED = EventDispatcher.nextEvType();
	}
	
	public int pointer, button;
	public float x, y;
	private IEventListener listener;
	
	public TouchPad(int category, IEventListener listener) {
		super(category);
		
		this.listener = listener;
		this.setTouchable(Touchable.enabled);
		
		addListener(new ClickListener() {
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				super.touchDown(event, x, y, pointer, button);
				
				setTouchDetails(x, y, pointer, button);
				if (getListener() != null)
					getListener().onEvent(EV_TYPE_TOUCH_DOWN, this);
				
				Gdx.app.log("TOUCH DOWN", "X:" + x + " Y:" + y);
				return true;
            }
			
			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				super.touchUp(event, x, y, pointer, button);
				
				setTouchDetails(x, y, pointer, button);
				if (getListener() != null)
					getListener().onEvent(EV_TYPE_TOUCH_UP, this);
				
				Gdx.app.log("TOUCH UP", "X:" + x + " Y:" + y);
			}
			
			@Override
			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				super.touchDragged(event, x, y, pointer);
				
				setTouchDetails(x, y, pointer, button);
				if (getListener() != null)
					getListener().onEvent(EV_TYPE_TOUCH_DRAGGED, this);
			}
		});
	}
	
	private IEventListener getListener() { return listener; }
	
	public void setListener(IEventListener listener) { this.listener = listener; }

	private void setTouchDetails(float x, float y, int pointer, int button) {
		this.x = x;
		this.y = y;
		this.pointer = pointer;
		this.button = button;
	}
}
