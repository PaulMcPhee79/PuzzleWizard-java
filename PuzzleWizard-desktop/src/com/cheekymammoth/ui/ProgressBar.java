package com.cheekymammoth.ui;

import com.cheekymammoth.graphics.Prop;
import com.badlogic.gdx.graphics.Color;
import com.cheekymammoth.graphics.ColoredProp;

public class ProgressBar extends Prop {
	private float progressPercentage; // 0.0f - 1.0f
	private ColoredProp trackBar;
	private ColoredProp progressBar;
	
	public ProgressBar() {
		this(-1, 1, 1);
	}

	public ProgressBar(int category) {
		this(category, 1, 1);
	}
	
	public ProgressBar(float width, float height) {
		this(-1, width, height);
	}
	
	public ProgressBar(int category, float width, float height) {
		super(category);
		
		trackBar = new ColoredProp(width, height);
		trackBar.setColor(Color.WHITE);
		addActor(trackBar);
		
		progressBar = new ColoredProp(width, height);
		progressBar.setColor(Color.GREEN);
		progressBar.setVisible(progressPercentage != 0);
		addActor(progressBar);
		
		setSize(width, height);
	}
	
	public void setTrackColor(Color color) {
		if (color != null)
			trackBar.setColor(color);
	}
	
	public void setProgressColor(Color color) {
		if (color != null)
			progressBar.setColor(color);
	}

	public void setProgress(float value) {
		if (value != progressPercentage) {
			progressPercentage = value;
			progressBar.setSize(Math.max(1f, value * trackBar.getWidth()), trackBar.getHeight());
			progressBar.setVisible(value != 0);
		}
	}
}
