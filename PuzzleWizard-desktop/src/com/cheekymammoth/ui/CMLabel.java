package com.cheekymammoth.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.cheekymammoth.utils.Utils;

public class CMLabel extends Label {
	private Color colorCache = new Color();
	
	public CMLabel(CharSequence text, LabelStyle style) {
		super(text, style);
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		colorCache.set(getColor());
		Utils.premultiplyAlpha(Utils.setA(getColor(), getColor().a * parentAlpha));
		super.draw(batch, 1f);
		setColor(colorCache);
	}
}
