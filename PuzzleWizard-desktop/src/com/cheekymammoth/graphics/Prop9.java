package com.cheekymammoth.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Prop9 extends Prop {
	private float patchX, patchY;
	private NinePatch ninePatch;
	
	public Prop9(TextureRegion region, int left, int right, int top, int bottom) {
		this(-1, region, left, right, top, bottom);
	}

	public Prop9(int category, TextureRegion region, int left, int right, int top, int bottom) {
		super(category);
		ninePatch = new NinePatch(region, left, right, top, bottom);
	}
	
	public void centerPatch() {
		patchX = -getWidth() / 2;
		patchY = -getHeight() / 2;
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		ICustomRenderer customerRenderer = getCustomRenderer();
		
		if (isTransform()) applyTransform(batch, computeTransform());
		if (customerRenderer != null)
			customerRenderer.preDraw(batch, parentAlpha, this);
		ninePatch.draw(batch, getX() + patchX, getY() + patchY, getWidth(), getHeight());
		drawSpriteChildren(batch, parentAlpha);
        drawChildren(batch, parentAlpha);
        if (customerRenderer != null)
        	customerRenderer.postDraw(batch, parentAlpha, this);
        if (isTransform()) resetTransform(batch);
	}
}
