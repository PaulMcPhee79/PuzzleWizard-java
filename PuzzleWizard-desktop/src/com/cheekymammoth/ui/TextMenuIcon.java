package com.cheekymammoth.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.cheekymammoth.graphics.CMSprite;
import com.cheekymammoth.ui.TextUtils.CMFontType;

public final class TextMenuIcon extends MenuIcon {	
	private boolean isTextCentered;
	private CMFontType fontType;
	private int align;
	private Vector2 bulletOffset = new Vector2();
	
	protected Label label;
	protected CMSprite bullet;
	
	public TextMenuIcon() {
		this(-1, "", 32);
	}
	
	public TextMenuIcon(int category, String text, int fontSize) {
		this(category, text, fontSize, TextUtils.kAlignCenter, -1, -1, CMFontType.REGULAR);
	}
	
	public TextMenuIcon(int category, String text, int fontSize, int align,
			float width, float height, CMFontType fontType) {
		super(category);
		
		this.fontType = fontType;
		this.align = align;
		
		if (fontType == CMFontType.REGULAR)
			label = TextUtils.create(text, fontSize, align, width, height, new Color(Color.WHITE));
		else
			label = TextUtils.createFX(text, fontSize, align, width, height, new Color(Color.WHITE));
		label.setY(-label.getHeight() / 2);
		addActor(label);
		updateBounds();
	}
	
	protected int getAlign() { return align; }
	
	protected CMFontType getFontType() { return fontType; }
	
	public boolean isTextCentered() { return isTextCentered; }
	
	public void setTextCentered(boolean value) {
		if (value != isTextCentered) {
			isTextCentered = value;
			updateBounds();
		}
	}
	
	public String getText() { return label.getText().toString(); } // Label.getText won't be null
	
	public void setText(String text) {
		label.setText(text);
		updateBounds();
	}

	public Vector2 getBulletOffset() { return bulletOffset; } 
	
	public void setBulletOffset(float x, float y) {
		bulletOffset.set(x, y);
		updateBounds();
	}
	
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
		
		if (bullet != null)
			bullet.setPosition(0, 0);
		
		if (isTextCentered) {
			if (bullet != null) {
				label.setX(-(bullet.getX() + bullet.getWidth() + label.getWidth()) / 2);
				bullet.setPosition(
						label.getX() - (bullet.getWidth() - bulletOffset.x),
						bulletOffset.y - bullet.getHeight() / 2);
			} else
				label.setX(-label.getWidth() / 2);
		} else if (bullet != null) {
			label.setX(bullet.getX() + bullet.getWidth());
			bullet.setPosition(
					bulletOffset.x,
					bulletOffset.y - bullet.getHeight() / 2);
		}
		
		if (bullet != null)
			setSize(
					(bullet.getWidth() - bulletOffset.x) + label.getTextBounds().width,
					Math.max(bullet.getHeight(), label.getTextBounds().height));
		else
			setSize(
					label.getTextBounds().width,
					label.getTextBounds().height);
		
		// Actor won't send this message if width/height didn't change, but x/y did.
		sizeChanged();
	}
	
	@Override
	public Rectangle getIconBounds() {
		Rectangle bounds = super.getIconBounds();
		if (bullet != null) {
			bounds.set(
					getX() + Math.min(bullet.getX(), label.getX()),
					getY() + Math.min(bullet.getY(), label.getY()),
					(bullet.getWidth() - bulletOffset.x) + label.getTextBounds().width,
					Math.max(bullet.getHeight(), label.getHeight()));
		} else {
			bounds.set(
					getX() + label.getX() + TextUtils.getTextBoundsX(label, align),
					getY() + label.getY(),
					label.getTextBounds().width,
					label.getHeight());
		}
		return bounds;
	}
	
	@Override
	public void localeDidChange(String fontKey, String FXFontKey) {
		if (fontType == CMFontType.REGULAR)
			TextUtils.swapFont(fontKey, label, true);
		else
			TextUtils.swapFont(FXFontKey, label, true);
		setSize(label.getTextBounds().width, label.getTextBounds().height);
	}
	
	@Override
	protected void setColorsForIndex(int index) {
		label.setColor(label.getColor().set(iconColors[index]));
		if (bullet != null) bullet.setColor(bullet.getColor().set(bulletColors[index]));
	}
}
