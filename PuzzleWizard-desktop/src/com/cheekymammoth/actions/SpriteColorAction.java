package com.cheekymammoth.actions;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.actions.ColorAction;
import com.badlogic.gdx.utils.Array;

public class SpriteColorAction extends ColorAction {
	private boolean clearsOnRestart = true;
	private final Array<Sprite> sprites = new Array<Sprite>(true, 1, Sprite.class);

	@Override
	protected void update (float percent) {
        super.update(percent);
        
        for (int i = 0, n = sprites.size; i < n; i++) {
        	Sprite sprite = sprites.get(i);
        	sprite.setColor(getColor());
        }
	}
	
	@Override
	public void restart () {
        super.restart();
        
        if (clearsOnRestart)
        	sprites.clear();
	}
	
	public boolean getClearsOnRestart() { return clearsOnRestart; }
	
	public void setClearsOnRestart(boolean value) { clearsOnRestart = value; }
	
	public void addSprite(Sprite sprite) {
		if (sprite != null)
			sprites.add(sprite);
	}
	
	public void removeSprite(Sprite sprite) {
		if (sprite != null)
			sprites.removeValue(sprite, true);
	}

}
