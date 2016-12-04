package com.cheekymammoth.actions;

import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.cheekymammoth.events.IEventListener;

public class RepeatEventAction extends RepeatAction {
	static public final int FOREVER = -1;
    private int repeatCount, executedCount;
    private boolean finished;
    private int evType = IEventListener.INVALID_EV_TYPE;
	private Object evData;
	private IEventListener listener;
    
    @Override
    protected boolean delegate (float delta) {
        if (executedCount == repeatCount) return true;
        if (action.act(delta)) {
                if (finished) return true;
                if (repeatCount > 0) executedCount++;
                if (listener != null) listener.onEvent(evType, evData != null ? evData : action);
                if (executedCount == repeatCount) return true;
                if (action != null) action.restart();
        }
        return false;
    }

    /** Causes the action to not repeat again. */
    public void finish () {
        finished = true;
    }

    public void restart () {
        super.restart();
        executedCount = 0;
        finished = false;
    }

    /** Sets the number of times to repeat. Can be set to {@link #FOREVER}. */
    public void setCount (int count) {
        this.repeatCount = count;
    }

    public int getCount () {
        return repeatCount;
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
}
