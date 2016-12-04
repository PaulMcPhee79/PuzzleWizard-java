package com.cheekymammoth.puzzleViews;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.gameModes.IPuzzleMode.PuzzleDimensions;
import com.cheekymammoth.gameModes.PuzzleMode;
import com.cheekymammoth.graphics.ColoredProp;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.locale.ILocalizable;
import com.cheekymammoth.puzzles.Player;
import com.cheekymammoth.utils.FloatTweener;
import com.cheekymammoth.utils.LangFX;
import com.cheekymammoth.utils.Promo;
import com.cheekymammoth.utils.Transitions;
import com.cheekymammoth.utils.Utils;

public class PlayerHUD extends Prop implements IPlayerView, IEventListener, ILocalizable {
	public static final Color kHUDRed = new Color(0xdb0000ff);
	public static final Color kHUDAmber = new Color(0xfffc00ff);
	public static final Color kHUDGreen = new Color(0x48db00ff);
	
	private static final float kTransitionInDuration = 0.25f;
	private static final float kTransitionOutDuration = 0.25f;
	
	private static final String kHudStringSingular = "? move remaining";
	private static final String kHudStringPlural = "? moves remaining";
	
	private enum HUDState { HS_OUT, HS_TRANSITION_IN, HS_IN, HS_TRANSITION_OUT };
	
	private Rectangle originBounds;
	private ColoredProp bgQuad;
	private FloatTweener transitionTweener;
	private int prevPlayerValue;
	private HUDState state;
	private Player player;
	private HUDCell hudCell;
	
	public PlayerHUD() {
		this(-1, new Rectangle());
	}

	public PlayerHUD(int category, Rectangle bounds) {
		super(category);
		
		originBounds = new Rectangle(bounds);
		bgQuad = new ColoredProp(bounds.width, bounds.height);
		bgQuad.setX(-bounds.width / 2);
		bgQuad.setColor(bgQuad.getColor().set(0x404040b2));
		addActor(bgQuad);
		
		hudCell = new HUDCell(getCategory(), bounds.width);
		hudCell.setX(-bounds.width / 2);
		addActor(hudCell);
		
		transitionTweener = new FloatTweener(0, Transitions.linear, this);
		setState(HUDState.HS_OUT);
		
		setSize(bounds.width, bounds.height);
		setAdvanceable(true);
	}
	
	private HUDState getState() { return state; }
	
	private void setState(HUDState value) {
		switch (value) {
			case HS_OUT:
				setColor(Utils.setA(getColor(), 0f));
				setVisible(false);
				transitionTweener.resetTween(getColor().a);
				break;
			case HS_TRANSITION_IN:
				transitionTweener.resetTween(getColor().a, 1f, kTransitionInDuration, 0);
				setVisible(Promo.isPlayerHudVisible());
				break;
			case HS_IN:
				setColor(Utils.setA(getColor(), 1f));
				transitionTweener.resetTween(getColor().a);
				break;
			case HS_TRANSITION_OUT:
				transitionTweener.resetTween(getColor().a, 0f, kTransitionOutDuration, 0);
				break;
		}
		
		state = value;
	}
	
	private String getHudString(int value) {
		String hudString = scene.localize(value == 1 ? kHudStringSingular : kHudStringPlural);
		return hudString.replaceFirst("\\?", "" + value);
	}
	
	private Color getTextColorForValue(int value, int range) {
		if (PuzzleMode.getDimesions() == PuzzleDimensions._8x6) {
			if (value >= 0.74f * range)
                return kHUDGreen;
            else if (value >= 0.374f * range)
                return kHUDAmber;
            else
                return kHUDRed;
		} else {
			if (value >= 0.69f * range)
                return kHUDGreen;
            else if (value >= 0.25f * range)
                return kHUDAmber;
            else
                return kHUDRed;
		}
	}
	
	public Player getPlayer() { return player; }
	
	public void setPlayer(Player value) {
		if (player == value)
			return;
		if (player != null)
			player.deregisterView(this);
		
		player = value;
		
		if (player != null)
			player.registerView(this);
		if (getState() == HUDState.HS_IN || getState() == HUDState.HS_TRANSITION_IN)
			setState(HUDState.HS_TRANSITION_OUT);
	}

	@Override
	public void localeDidChange(String fontKey, String FXFontKey) {
		hudCell.localeDidChange(fontKey, FXFontKey);
		hudCell.setText(getHudString(prevPlayerValue));
		
		originBounds.set(originBounds.x, originBounds.y, LangFX.getPlayerHUDSettings()[1], originBounds.height);
		bgQuad.setSize(originBounds.width, originBounds.height);
		bgQuad.setX(-originBounds.width / 2);
		hudCell.setX(-originBounds.width / 2);
	}

	@Override
	public void playerValueDidChange(int code, int value) {
		switch (code) {
			case Player.kValueColorMagic:
				hudCell.setIcon(code);
				hudCell.setText(getHudString(value));
				hudCell.setTextColor(getTextColorForValue(value, PuzzleMode.getMaxColorMagicMoves()));
				break;
			case Player.kValueMirrorImage:
				hudCell.setIcon(code);
				hudCell.setText(getHudString(value));
				hudCell.setTextColor(getTextColorForValue(value, PuzzleMode.getMaxMirrorImageMoves()));
				break;
			default:
				return;
		}
		
		HUDState state = getState();
		if (value == 0 && (state == HUDState.HS_IN || state == HUDState.HS_TRANSITION_IN))
			setState(HUDState.HS_TRANSITION_OUT);
		else if (value > 0 && (state == HUDState.HS_OUT || state == HUDState.HS_TRANSITION_OUT))
			setState(HUDState.HS_TRANSITION_IN);
		
		prevPlayerValue = value;
	}

	@Override
	public void willBeginMoving() { }

	@Override
	public void didFinishMoving() { }

	@Override
	public void didIdle() { }
	
	@Override
	public void onEvent(int evType, Object evData) {
		if (evType == FloatTweener.EV_TYPE_FLOAT_TWEENER_CHANGED) {
			float tweenedValue = transitionTweener.getTweenedValue();
			setColor(Utils.setA(getColor(), tweenedValue));
		} else if (evType == FloatTweener.EV_TYPE_FLOAT_TWEENER_COMPLETED) {
			if (getState() == HUDState.HS_TRANSITION_IN)
				setState(HUDState.HS_IN);
			else if (getState() == HUDState.HS_TRANSITION_OUT)
				setState(HUDState.HS_OUT);
		}
	}
	
	@Override
	public void advanceTime(float dt) {
		if (getState() == HUDState.HS_TRANSITION_IN || getState() == HUDState.HS_TRANSITION_OUT)
			transitionTweener.advanceTime(dt);
	}

	@Override
	public void didTreadmill() {
		// TODO Auto-generated method stub
		
	}
}
