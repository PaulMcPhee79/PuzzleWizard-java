package com.cheekymammoth.graphics;

import com.badlogic.gdx.graphics.Color;
import com.cheekymammoth.resolution.IResDependent;

public class FullscreenQuad extends ColoredProp implements IResDependent {

	public FullscreenQuad() {
		this(-1);
	}

	public FullscreenQuad(int category) {
		this(category, null);
	}
	
	public FullscreenQuad(Color color) {
		this(-1, color);
	}
	
	public FullscreenQuad(int category, Color color) {
		super(category, 1, 1, color);
		
		setPosition(scene.VW2(), scene.VH2());
		resolutionDidChange((int)scene.getStage().getWidth(), (int)scene.getStage().getHeight());
	}

	@Override
	public void resolutionDidChange(int width, int height) {
		float stageWidth = scene.getStage().getWidth(), stageHeight = scene.getStage().getHeight();
		setSize(stageWidth, stageHeight);
		centerContent();
	}
}
