package com.cheekymammoth.graphics;

import com.badlogic.gdx.graphics.Color;

public class ColoredProp extends Prop {
	protected CMSprite icon;
	
	public ColoredProp() {
		this(-1);
	}

	public ColoredProp(int category) {
		this(category, 1, 1, null);
	}
	
	public ColoredProp(float width, float height) {
		this(-1, width, height, null);
	}
	
	public ColoredProp(float width, float height, Color color) {
		this(-1, width, height, color);
	}

	public ColoredProp(int category, float width, float height, Color color) {
		super(category);
		
		icon = new CMSprite(scene.textureByName("quad.png"));
		icon.setSize(width, height);
		addSpriteChild(icon);
		setContentSize(width, height);
		if (color != null) setColor(color);
	}
	
	public void centerContent() {
		icon.centerContent();
	}
	
	@Override
	protected void sizeChanged() {
		super.sizeChanged();
		icon.setSize(getWidth(), getHeight());
	}
}
