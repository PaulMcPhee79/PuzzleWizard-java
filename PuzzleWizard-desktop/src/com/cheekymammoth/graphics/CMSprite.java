package com.cheekymammoth.graphics;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.cheekymammoth.graphics.GfxMode.AlphaMode;
import com.cheekymammoth.sceneControllers.SceneController;

public class CMSprite extends Sprite {
	protected static AlphaMode s_defaultAlphaMode = AlphaMode.PRE_MULTIPLIED;
	private static SceneController s_Scene;
	
	public static void setDefaultAlphaMode(AlphaMode value) {
		s_defaultAlphaMode = value;
	}
	
	private int tag = Integer.MIN_VALUE;
	private boolean isVisible = true;
	private AlphaMode alphaMode = AlphaMode.PRE_MULTIPLIED;
	private String name;
	protected SceneController scene;

	public CMSprite() {
		commonInit();
	}

	public CMSprite(Texture texture) {
		super(texture);
		commonInit();
	}

	public CMSprite(TextureRegion region) {
		super(region);
		commonInit();
	}

	public CMSprite(Sprite sprite) {
		super(sprite);
		commonInit();
	}

	public CMSprite(Texture texture, int srcWidth, int srcHeight) {
		super(texture, srcWidth, srcHeight);
		commonInit();
	}

	public CMSprite(Texture texture, int srcX, int srcY, int srcWidth,
			int srcHeight) {
		super(texture, srcX, srcY, srcWidth, srcHeight);
		commonInit();
	}

	public CMSprite(TextureRegion region, int srcX, int srcY, int srcWidth,
			int srcHeight) {
		super(region, srcX, srcY, srcWidth, srcHeight);
		commonInit();
	}
	
	public int getTag() {
		return tag;
	}
	
	public void setTag(int value) {
		tag = value;
	}
	
	public boolean isVisible() { return isVisible; }
	
	public void setVisible(boolean value) { isVisible = value; }
	
	public AlphaMode getAlphaMode() { return alphaMode; }
	
	public void setAlphaMode(AlphaMode value) { alphaMode = value; }
	
	public String getName() { return name; }
	
	public void setName(String value) { name = value; }

	private void commonInit() {
		scene = s_Scene;
		setAlphaMode(s_defaultAlphaMode);
	}
	
	public void centerContent() {
		setPosition(-getWidth() / 2, -getHeight() / 2);
		setOrigin(getWidth() / 2, getHeight() / 2);
	}
	
	public float getScaledWidth() {
		return getWidth() * getScaleX();
	}
	
	public float getScaledHeight() {
		return getHeight() * getScaleY();
	}
	
	@Override
	public void draw (Batch batch) {
		if (!isVisible())
			return;
		if (alphaMode == AlphaMode.POST_MULTIPLIED) {
			int blendSrcCache = batch.getBlendSrcFunc();
			int blendDestCache = batch.getBlendDstFunc();
			batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			super.draw(batch);
			batch.setBlendFunction(blendSrcCache, blendDestCache);
		} else
			super.draw(batch);
	}
	
	@Override
	public void draw (Batch batch, float alphaModulation) {
		if (!isVisible())
			return;
		if (alphaMode == AlphaMode.POST_MULTIPLIED) {
			int blendSrcCache = batch.getBlendSrcFunc();
			int blendDestCache = batch.getBlendDstFunc();
			batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			super.draw(batch, alphaModulation);
			batch.setBlendFunction(blendSrcCache, blendDestCache);
		} else
			super.draw(batch, alphaModulation);
	}
	
	public static SceneController getSpriteScene() {
		return s_Scene;
	}
	
	public static void setSpriteScene(SceneController value) {
		if (value != null)
			s_Scene = value;
	}
	
	public static void relinquishSpriteScene(SceneController value) {
		if (value == null)
			throw new IllegalArgumentException("Sprite scene cannot be null");
		if (value == s_Scene)
			s_Scene = null;
	}
}
