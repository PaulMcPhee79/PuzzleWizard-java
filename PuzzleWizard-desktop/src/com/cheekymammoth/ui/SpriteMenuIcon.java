package com.cheekymammoth.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.cheekymammoth.graphics.CMSprite;

public class SpriteMenuIcon extends MenuIcon {
	private boolean isSpriteCentered;
	private float spriteOffsetX;
	protected CMSprite sprite;
	protected CMSprite bullet;
	
	public SpriteMenuIcon() {
		this(-1, null);
	}

	public SpriteMenuIcon(int category, TextureRegion region) {
		super(category);
		
		sprite = new CMSprite(region);
		sprite.setY(-sprite.getHeight() / 2);
		addSpriteChild(sprite);
		updateBounds();
	}
	
	public boolean isSpriteCentered() { return isSpriteCentered; }
	
	public void setSpriteCentered(boolean value) {
		if (value != isSpriteCentered) {
			isSpriteCentered = value;
			updateBounds();
		}
	}
	
	public float getSpriteOffsetX() { return spriteOffsetX; } 
	
	public void setSpriteOffsetX(float value) { spriteOffsetX = value; }
	
	public CMSprite getBullet() { return bullet; }
	
	public void setBullet(CMSprite bullet) {
		if (this.bullet != null)
			removeSpriteChild(this.bullet);
		
		this.bullet = bullet;
		
		if (bullet != null)
			addSpriteChild(bullet);
		
		updateBounds();
		dispatchEvent(MenuButton.EV_TYPE_BROADCAST_STATE_REQUEST, this);
	}

	@Override
	protected void updateBounds() {
		super.updateBounds();
		
		if (isSpriteCentered) {
			if (bullet != null) {
				sprite.setX(-(bullet.getWidth() + spriteOffsetX + sprite.getWidth()) / 2);
				bullet.setX(sprite.getX() - (spriteOffsetX + bullet.getWidth()));
				bullet.setY(-bullet.getHeight() / 2);
			} else
				sprite.setX(-(spriteOffsetX + sprite.getWidth()) / 2);
		} else {
			if (bullet != null) {
				sprite.setX(bullet.getWidth() + spriteOffsetX);
				bullet.setY(-bullet.getHeight() / 2);
			} else
				sprite.setX(spriteOffsetX);
		}
		
		if (bullet != null)
			setSize(
					bullet.getWidth() + spriteOffsetX + sprite.getWidth(),
					Math.max(bullet.getHeight(), sprite.getHeight()));
		else
			setSize(
					spriteOffsetX + sprite.getWidth(),
					sprite.getHeight());
		
		// Actor won't send this message if width/height didn't change, but x/y did.
		sizeChanged();
	}
	
	@Override
	public Rectangle getIconBounds() {
		Rectangle bounds = super.getIconBounds();
		if (bullet != null) {
			bounds.set(
					getX() + Math.min(bullet.getX(), sprite.getX()),
					getY() + Math.min(bullet.getY(), sprite.getY()),
					bullet.getWidth() + spriteOffsetX + sprite.getWidth(),
					Math.max(bullet.getHeight(), sprite.getHeight()));
		} else {
			bounds.set(
					getX() + sprite.getX() - spriteOffsetX,
					getY() + sprite.getY(),
					spriteOffsetX + sprite.getWidth(),
					sprite.getHeight());
		}
		return bounds;
	}
	
	@Override
	protected void setColorsForIndex(int index) {
		sprite.setColor(sprite.getColor().set(iconColors[index]));
		if (bullet != null) bullet.setColor(bullet.getColor().set(bulletColors[index]));
	}
}
