package com.cheekymammoth.puzzleViews;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.AfterAction;
import com.badlogic.gdx.scenes.scene2d.actions.DelayAction;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.cheekymammoth.actions.EventActions;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.puzzles.Player;
import com.cheekymammoth.utils.Coord;
import com.cheekymammoth.utils.Utils;

public class PlayerPiece extends Prop implements IPlayerView, IEventListener, Poolable {
	public enum PPType { STATIC, ANIMATED };
    public enum PPState { IDLE, TREADMILL, MOVING };
    
    public static final int EV_TYPE_DID_TRANSITION_OUT;
    
    static {
    	EV_TYPE_DID_TRANSITION_OUT = EventDispatcher.nextEvType();
    }
	
    public static float rotationForPlayerOrientation(int orientation) {
        if (orientation == Player.kEasternOrientation)
            return 90;
        else if (orientation == Player.kSouthernOrientation)
            return 180;
        else if (orientation == Player.kWesternOrientation)
            return 270;
        else
            return 0;
    }
	
    protected PPState state = PPState.IDLE;
    protected Player player;
    protected Prop playerCanvas;
    private DelayAction delayAction;
    private AfterAction afterAction;
    
	public PlayerPiece() {
		this(-1);
	}

	public PlayerPiece(int category) {
		super(category);
		
		delayAction = new DelayAction();
		afterAction = new AfterAction();
		playerCanvas = new Prop();
		playerCanvas.setTransform(true);
		addActor(playerCanvas);
		setTransform(true);
	}
	
	public PPState getState() { return state; }
	
	public Player getPlayer() { return player; }
	
	protected void setState(PPState value) { state = value; }
	
	public boolean isStationary() { return state != PPState.MOVING; }

	public void transitionIn(float duration, float delay) {
		this.removeAction(afterAction);
		this.removeAction(delayAction);
		
		setColor(Utils.setA(getColor(), 0));
		delayAction.reset();
		delayAction.setDuration(delay);
		delayAction.setAction(Actions.fadeIn(duration));
		addAction(delayAction);
	}
	
	public void transitionOut(float duration) {
		this.removeAction(afterAction);
		this.removeAction(delayAction);
		
		delayAction.reset();
		delayAction.setDuration(0);
		delayAction.setAction(Actions.fadeOut(duration));
		addAction(delayAction);
		
		afterAction.reset();
		afterAction.setAction(EventActions.eventAction(EV_TYPE_DID_TRANSITION_OUT, this));
		addAction(afterAction);
	}
	
	public void enableMenuMode(boolean enable) { }
	
	public void setPositionAestheticOnly(float x, float y) { }
	
	public final void setPositionAestheticOnly(Coord pos) {
		setPositionAestheticOnly(pos.x, pos.y);
	}
	
	public void refreshAesthetics() { }
	
	public void setData(Player player) {
		if (this.player != null)
			this.player.deregisterView(this);
		
		this.player = player;
		if (this.player != null) {
			this.player.registerView(this);
			syncWithData();
		}
	}
	
	public void syncWithPlayerPiece(PlayerPiece playerPiece) { }

    public void syncWithData() { }

    public void moveTo(Coord pos)
    {
        if (player != null)
        	player.didFinishMoving();
    }
    
	@Override
	public void playerValueDidChange(int code, int value) {
		syncWithData();
	}

	@Override
	public void willBeginMoving() {
		if (player != null) {
            player.didBeginMoving();
            moveTo(player.isMovingTo());
        }
	}

	@Override
	public void didFinishMoving() { }

	@Override
	public void didIdle() {
		if (getState() != PPState.IDLE)
            setState(PPState.IDLE);
	}
	
	@Override
	public void didTreadmill() {
		if (getState() != PPState.TREADMILL)
            setState(PPState.TREADMILL);
	}
	
	@Override
	public void onEvent(int evType, Object evData) {
		if (evType == EV_TYPE_DID_TRANSITION_OUT)
			dispatchEvent(evType, this);
	}
	
	public void softReset() {
		setState(PPState.IDLE);
	}
	
	@Override
	public void reset() {
		removeAction(afterAction);
		removeAction(delayAction);
		clearActions();
		remove();
		setData(null);
		setColor(Color.WHITE);
		setScale(1f);
		setRotation(0f);
		setState(PPState.IDLE);
		setVisible(true);
	}
}
