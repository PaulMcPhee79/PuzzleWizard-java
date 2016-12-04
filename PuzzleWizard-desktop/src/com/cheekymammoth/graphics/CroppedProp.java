package com.cheekymammoth.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;

public class CroppedProp extends Prop {
	private boolean enabled = true;
	private Rectangle viewableRegion;
	private Rectangle scissors = new Rectangle();

	public CroppedProp(int category, Rectangle viewableRegion) {
		super(category);
		this.viewableRegion = new Rectangle(viewableRegion);
	}
	
	public void enableCrop(boolean enable) {
		enabled = enable; 
	}
	
	public Rectangle getViewableRegion() {
		return viewableRegion;
	}
	
	public void setViewableRegion(Rectangle value) {
		viewableRegion.set(value);
	}
	
	public void clampToContent() {
		Vector2 topLeft = localToStageCoordinates (new Vector2(0, 0));
		Vector2 btmRight = localToStageCoordinates(new Vector2(getWidth(), getHeight()));
		setViewableRegion(new Rectangle(topLeft.x, topLeft.y, btmRight.x, btmRight.y));
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		if (enabled) {
		    ScissorStack.calculateScissors(
		    		scene.getCamera(),
		    		0, 0, scene.VPW(), scene.VPH(),
		    		batch.getTransformMatrix(),
		    		viewableRegion,
		    		scissors);
		    batch.flush();
		    ScissorStack.pushScissors(scissors);
		    super.draw(batch, parentAlpha);
		    batch.flush();
		    ScissorStack.popScissors();
		} else
			super.draw(batch, parentAlpha);
	}
}
