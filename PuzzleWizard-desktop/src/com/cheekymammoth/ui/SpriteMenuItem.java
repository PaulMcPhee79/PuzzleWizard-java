package com.cheekymammoth.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class SpriteMenuItem extends MenuItem {
	private SpriteMenuIcon spriteMenuIcon;
	
	public SpriteMenuItem() {
		this(-1, null);
	}

	public SpriteMenuItem(int category, TextureRegion region) {
		super(category);
		
		spriteMenuIcon = new SpriteMenuIcon(category, region);
		setIcon(spriteMenuIcon);
		addMenuIcon(spriteMenuIcon);
		
		attachIndicator();
		updateIndicator();
	}
	
	public SpriteMenuIcon getIcon() { return spriteMenuIcon; }
	
	public boolean isSpriteCentered() { return spriteMenuIcon.isSpriteCentered(); }
	
	public void setSpriteCentered(boolean value) {
		spriteMenuIcon.setSpriteCentered(value);
		if (indicator != null)
			indicator.updateAttachmentPosition();
	}
}
