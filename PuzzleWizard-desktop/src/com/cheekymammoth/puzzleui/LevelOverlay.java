package com.cheekymammoth.puzzleui;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.graphics.CMAtlasSprite;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.utils.FloatTweener;
import com.cheekymammoth.utils.Transitions;
import com.cheekymammoth.utils.Utils;

public class LevelOverlay extends Prop implements IEventListener {
	private enum LevelOverlayState { Idle, Glow, Fade };
	
	private static final int kNumRunes = 4;
    private static final float kRuneTweenDuration = 1.5f;
    private static final int[] kRuneSequence = new int[] {
        0, 1, 2, 3, -1,
        2, 1, 3, 0, -1,
        3, 2, 1, 0, -1,
        1, 2, 3, 0, -1,
        3, 1, 0, 2, -1,
        0, 3, 2, 1, -1,

        1, 0, 2, 3, -1,
        2, 0, 3, 1, -1,
        1, 2, 0, 3, -1,
        0, 2, 3, 1, -1,
        3, 0, 1, 2, -1,
        1, 3, 2, 0, -1,

        2, 1, 0, 3, -1,
        0, 1, 3, 2, -1,
        2, 0, 1, 3, -1,
        1, 0, 3, 2, -1,
        0, 2, 1, 3, -1,
        3, 0, 2, 1, -1,
        
        3, 1, 2, 0, -1,
        0, 3, 1, 2, -1,
        2, 3, 1, 0, -1,
        3, 2, 0, 1, -1,
        1, 3, 0, 2, -1,
        2, 3, 0, 1, -1
    };

    private int runeIndex = 0;
    private LevelOverlayState state = LevelOverlayState.Idle;
    private Array<CMAtlasSprite> runes = new Array<CMAtlasSprite>(true, kNumRunes, CMAtlasSprite.class);
    private CMAtlasSprite wizardHat;
    private Prop canvas;
    private FloatTweener runeTweener;

	public LevelOverlay() {
		this(-1);
	}

	public LevelOverlay(int category) {
		super(category);
		
		runeIndex = MathUtils.random(kRuneSequence.length-1);
		runeIndex = Math.min(kRuneSequence.length - 1, Math.max(0, runeIndex - runeIndex % (kNumRunes + 1)));
		
		canvas = new Prop();
		addActor(canvas);
		
		wizardHat = new CMAtlasSprite(scene.textureRegionByName("level-hat"));
		wizardHat.centerContent();
		addSpriteChild(wizardHat);
		setContentSize(wizardHat.getWidth(), wizardHat.getHeight());
		
		Vector2[] runePositions = new Vector2[] {
			new Vector2(108, 180), new Vector2(137, 195), new Vector2(179, 213), new Vector2(225, 213)
		};
		
		for (int i = 0; i < kNumRunes; ++i) {
			CMAtlasSprite rune = new CMAtlasSprite(scene.textureRegionByName("hat-glow-" + i));
            rune.setPosition(
            		wizardHat.getX() + runePositions[i].x,
            		wizardHat.getY() + wizardHat.getHeight() - (rune.getHeight() + runePositions[i].y));
            rune.setColor(Utils.setA(rune.getColor(), 0));
            runes.add(rune);
            canvas.addSpriteChild(rune);
        }

        runeTweener = new FloatTweener(0f, Transitions.linear, this);
		
		setAdvanceable(true);
		setState(LevelOverlayState.Glow);
	}
	
	private LevelOverlayState getState() { return state; }
	
	private void setState(LevelOverlayState value) {
		if (state == value)
            return;

        if (state == LevelOverlayState.Fade || state == LevelOverlayState.Glow) {
        	float tweenedValue = runeTweener.getTweenedValue();
            for (int i = 0, n = runes.size;  i < n; i++) {
                if (i == kRuneSequence[runeIndex] || kRuneSequence[runeIndex] == -1) {
                	CMAtlasSprite rune = runes.get(i);
                	rune.setColor(Utils.setA(rune.getColor(), tweenedValue));
                }
            }
        }

        switch (value) {
            case Idle:
            	for (int i = 0, n = runes.size;  i < n; i++) {
            		CMAtlasSprite rune = runes.get(i);
            		rune.setColor(Utils.setA(rune.getColor(), 0));
            	}
                break;
            case Glow:
                if (++runeIndex >= kRuneSequence.length)
                    runeIndex = 0;

                if (state == LevelOverlayState.Fade)
                    runeTweener.reverse();
                else
                    runeTweener.resetTween(0, 1f, kRuneTweenDuration, 0);
                break;
            case Fade:
                if (state == LevelOverlayState.Glow)
                    runeTweener.reverse();
                else
                	runeTweener.resetTween(1f, 0, kRuneTweenDuration, 0);
                break;
        }

        state = value;
	}

	@Override
	public void onEvent(int evType, Object evData) {
		if (evType == FloatTweener.EV_TYPE_FLOAT_TWEENER_CHANGED) {
			float tweenedValue = runeTweener.getTweenedValue();
			for (int i = 0, n = runes.size;  i < n; i++) {
				if (i == kRuneSequence[runeIndex] || kRuneSequence[runeIndex] == -1) {
					CMAtlasSprite rune = runes.get(i);
					rune.setColor(Utils.setA(rune.getColor(), tweenedValue));
				}
			}
		} else if (evType == FloatTweener.EV_TYPE_FLOAT_TWEENER_COMPLETED) {
			if (runeTweener.getTweenedValue() > 0.5f)
				setState(LevelOverlayState.Fade);
			else
				setState(LevelOverlayState.Glow);
		}
	}
	
	@Override
	public void advanceTime(float dt) {
		if (getState() == LevelOverlayState.Idle)
			return;
		runeTweener.advanceTime(dt);
	}
}
