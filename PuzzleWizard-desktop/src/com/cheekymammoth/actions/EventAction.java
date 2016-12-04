package com.cheekymammoth.actions;

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.utils.Pool;
import com.cheekymammoth.events.IEventListener;

public class EventAction extends TemporalAction {
	private int evType = IEventListener.INVALID_EV_TYPE;
	private Object evData;
	private IEventListener listener;

	public EventAction() {

	}
	
	public EventAction(IEventListener listener) {
		this(IEventListener.INVALID_EV_TYPE, listener);
	}
	
	public EventAction(int evType, IEventListener listener) {
		this.evType = evType;
		this.listener = listener;
	}
	
	public EventAction(float duration, int evType, IEventListener listener) {
		super(duration);
		this.evType = evType;
		this.listener = listener;
	}
	
	public int getEvType() {
		return evType;
	}
	
	public void setEvType(int value) {
		this.evType = value;
	}
	
	public Object getEvData() {
		return evData;
	}
	
	public void setEvData(Object value) {
		this.evData = value;
	}
	
	public IEventListener getListener() {
		return listener;
	}
	
	public void setListener(IEventListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void reset () {
        super.reset();
        setEvType(IEventListener.INVALID_EV_TYPE);
        setEvData(null);
        setListener(null);
	}

	@Override
	public boolean act(float delta) {
		boolean retval = super.act(delta);
		
		@SuppressWarnings("rawtypes")
		Pool pool = getPool();
        setPool(null); // Ensure this action can't be returned to the pool while executing.
        try {
        	if (retval && listener != null)
        		listener.onEvent(evType, evData);
        	return retval;
        } finally {
        	setPool(pool);
        }
	}

	@Override
	protected void update(float percent) {
		// Do nothing
	}
}
