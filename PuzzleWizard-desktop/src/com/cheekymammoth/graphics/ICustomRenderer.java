package com.cheekymammoth.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;

public interface ICustomRenderer {
	void preDraw(Batch batch, float parentAlpha, Object obj);
	void postDraw(Batch batch, float parentAlpha, Object obj);
}
