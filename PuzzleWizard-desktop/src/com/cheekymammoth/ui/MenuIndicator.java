package com.cheekymammoth.ui;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.puzzleViews.PainterDecoration;
import com.cheekymammoth.puzzleViews.TilePiece;

public class MenuIndicator extends Prop implements IEventListener {
	private Vector2 offset = new Vector2();
	private PainterDecoration indicator;
	private MenuButton button;
	
	public MenuIndicator() {
		this(-1, TilePiece.kColorKeyGreen << 4);
	}

	public MenuIndicator(int category, int type) {
		this(category, type, 0, 0);
	}
	
	public MenuIndicator(int category, int type, float offsetX, float offsetY) {
		super(category);
		
		offset.set(offsetX, offsetY);
		indicator = new PainterDecoration(category, type);
		indicator.setX(-indicator.getWidth() / 2);
		addActor(indicator);
		setTransform(true);
	}
	
	public Vector2 getOffset() { return offset; } 
	
	public void setOffset(float x, float y) {
		offset.set(x, y);
		updateAttachmentPosition();
	}
	
	public void attachToMenuButton(MenuButton button) {
		if (button == null)
			return;
		if (this.button != null)
			detachFromMenuButton(this.button);
		this.button = button;
		button.addActor(this);
		button.addEventListener(MenuButton.EV_TYPE_ICON_SIZE_CHANGED, this);
		updateAttachmentPosition();
	}
	
	public void detachFromMenuButton(MenuButton button) {
		if (button != null && this.button == button) {
			button.removeEventListener(MenuButton.EV_TYPE_ICON_SIZE_CHANGED, this);
			this.button = null;
			remove();
		}
	}
	
	protected void updateAttachmentPosition() {
		if (button == null)
			return;
		Rectangle iconBounds = button.getIconBounds();
		setPosition(iconBounds.x + offset.x, iconBounds.y + iconBounds.height / 2 + offset.y);
	}
	
	@Override
	public void onEvent(int evType, Object evData) {
		if (evType == MenuButton.EV_TYPE_ICON_SIZE_CHANGED) {
			updateAttachmentPosition();
		}
	}
	
	@Override
	public void act (float delta) {
		super.act(delta);
		indicator.advanceTime(delta);
	}
}
