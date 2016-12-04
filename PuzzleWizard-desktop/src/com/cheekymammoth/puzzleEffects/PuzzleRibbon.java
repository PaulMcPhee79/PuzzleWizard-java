package com.cheekymammoth.puzzleEffects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.graphics.ColoredProp;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.locale.ILocalizable;
import com.cheekymammoth.resolution.IResDependent;
import com.cheekymammoth.ui.TextUtils;
import com.cheekymammoth.utils.FloatTweener;
import com.cheekymammoth.utils.LangFX;
import com.cheekymammoth.utils.Transitions;
import com.cheekymammoth.utils.Utils;

public class PuzzleRibbon extends Prop implements IEventListener, ILocalizable, IResDependent {
	private static final float kBgQuadOpacityFactor = 0.7f;
	
	private float duration;
	private Label levelLabel;
	private Label puzzleLabel;
	private ColoredProp upperStripe;
	private ColoredProp lowerStripe;
	private ColoredProp bgQuad;
	private FloatTweener opacityTweener;
	private FloatTweener levelLabelTweener;
	private FloatTweener puzzleLabelTweener;
	
	public PuzzleRibbon() {
		this(-1);
	}

	public PuzzleRibbon(int category) {
		super(category);
		
		bgQuad = new ColoredProp(scene.VPW(), 368);
		bgQuad.centerContent();
		bgQuad.setColor(bgQuad.getColor().set(0, 0, 0, kBgQuadOpacityFactor));
		addActor(bgQuad);
		
		upperStripe = new ColoredProp(bgQuad.getWidth(), 20);
		upperStripe.centerContent();
		upperStripe.setPosition(bgQuad.getX(), bgQuad.getY() + bgQuad.getHeight() / 2 - upperStripe.getHeight() / 2);
		upperStripe.setColor(Color.BLACK);
		addActor(upperStripe);
		
		lowerStripe = new ColoredProp(bgQuad.getWidth(), 20);
		lowerStripe.centerContent();
		lowerStripe.setPosition(bgQuad.getX(), bgQuad.getY() - (bgQuad.getHeight() / 2 - lowerStripe.getHeight() / 2));
		lowerStripe.setColor(Color.BLACK);
		addActor(lowerStripe);
		
		levelLabel = TextUtils.create(" ", 70, TextUtils.kAlignCenter, Color.WHITE);
		addActor(levelLabel);
		
		puzzleLabel = TextUtils.create(" ", 54, TextUtils.kAlignCenter, Color.WHITE);
		addActor(puzzleLabel);
		
		refreshLabelPositionsY();
		
		//float startValue, Interpolation interpolation, IEventListener listener
		opacityTweener = new FloatTweener(0, Transitions.linear, this);
		levelLabelTweener = new FloatTweener(0, Transitions.linear, this);
		puzzleLabelTweener = new FloatTweener(0, Transitions.linear, this);
		
		setSize(bgQuad.getWidth(), bgQuad.getHeight());
		setVisible(false);
		setAdvanceable(true);
	}
	
	private void refreshLabelPositionsY() {
		levelLabel.setY(bgQuad.getY() + bgQuad.getHeight() / 2
				+ LangFX.getPuzzleRibbonLabelOffset()[0] - levelLabel.getHeight());
		puzzleLabel.setY(levelLabel.getY() + LangFX.getPuzzleRibbonLabelOffset()[1]);
	}
	
	public void setLevelText(String text) {
		levelLabel.setText(scene.localize(text));
	}
	
	public void setLevelColor(Color color) {
		levelLabel.setColor(color);
	}
	
	public void setPuzzleText(String text) {
		puzzleLabel.setText(scene.localize(text));
	}
	
	public void setPuzzleColor(Color color) {
		puzzleLabel.setColor(color);
	}
	
	public void animate(float duration) {
		stopAnimating();
		
		this.duration = duration;
		setColor(Utils.setA(getColor(), 0));
		
		float maxLabelWidth = Math.max(levelLabel.getTextBounds().width, puzzleLabel.getTextBounds().width);
		levelLabel.setX(bgQuad.getWidth() / 2 + maxLabelWidth / 2);
		puzzleLabel.setX(-(bgQuad.getWidth() / 2 + maxLabelWidth / 2));
		
		opacityTweener.resetTween(getColor().a, 1f, 0.25f * duration, 0);
		levelLabelTweener.resetTween(
				levelLabel.getX(),
				-(bgQuad.getWidth() / 2 + maxLabelWidth / 2),
				duration,
				0);
		puzzleLabelTweener.resetTween(
				puzzleLabel.getX(),
				bgQuad.getWidth() / 2 + maxLabelWidth / 2,
				duration,
				0);
		setVisible(true);
	}
	
	public void stopAnimating() {
		setVisible(false);
	}

	@Override
	public void localeDidChange(String fontKey, String FXFontKey) {
		TextUtils.swapFont(fontKey, levelLabel, true);
		TextUtils.swapFont(fontKey, puzzleLabel, true);
		refreshLabelPositionsY();
	}
	
	@Override
	public void resolutionDidChange(int width, int height) {
		float stageWidth = scene.getStage().getWidth();
		
		bgQuad.setSize(stageWidth, bgQuad.getHeight());
		bgQuad.centerContent();
		
		upperStripe.setSize(stageWidth, upperStripe.getHeight());
		upperStripe.centerContent();
		upperStripe.setY(bgQuad.getY() + bgQuad.getHeight() / 2 - upperStripe.getHeight() / 2);
		
		lowerStripe.setSize(stageWidth, lowerStripe.getHeight());
		lowerStripe.centerContent();
		lowerStripe.setY(bgQuad.getY() - (bgQuad.getHeight() / 2 - lowerStripe.getHeight() / 2));
		
		setSize(bgQuad.getWidth(), bgQuad.getHeight());
	}

	@Override
	public void onEvent(int evType, Object evData) {
		if (evType == FloatTweener.EV_TYPE_FLOAT_TWEENER_CHANGED) {
			FloatTweener tweener = (FloatTweener)evData;
			if (tweener != null) {
				float tweenedValue = tweener.getTweenedValue();
				if (tweener == opacityTweener)
					setColor(Utils.setA(getColor(), tweenedValue));
				else if (tweener == levelLabelTweener)
					levelLabel.setX(tweenedValue);
				else if (tweener == puzzleLabelTweener)
					puzzleLabel.setX(tweenedValue);
			}
		} else if (evType == FloatTweener.EV_TYPE_FLOAT_TWEENER_COMPLETED) {
			FloatTweener tweener = (FloatTweener)evData;
			if (tweener != null && tweener == opacityTweener) {
				if (getColor().a < 0.5f)
					setVisible(false);
				else
					opacityTweener.resetTween(getColor().a, 0, 0.25f * duration, 0.5f * duration);
			}
		}
	}
	
	@Override
	public void advanceTime(float dt) {
		if (!isVisible())
			return;
		
		float adjustedTime = 1.5f * Math.max(0.0275f * dt,
				dt * (float)Math.abs(Math.cos(puzzleLabelTweener.getPercentComplete() * Math.PI)));
		opacityTweener.advanceTime(adjustedTime);
		levelLabelTweener.advanceTime(adjustedTime);
		puzzleLabelTweener.advanceTime(adjustedTime);
	}
}
