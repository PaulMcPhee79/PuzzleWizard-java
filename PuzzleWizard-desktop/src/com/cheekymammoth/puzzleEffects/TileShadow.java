package com.cheekymammoth.puzzleEffects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.cheekymammoth.graphics.CMSprite;
import com.cheekymammoth.graphics.ICustomRenderer;
import com.cheekymammoth.graphics.Prop;

public class TileShadow extends Prop {
	private Vector2 localPos = new Vector2();
	private Rectangle shadowBounds = new Rectangle();
	private Rectangle occlusionBounds = new Rectangle();
	private CMSprite shadowIcon;

	public TileShadow(int category, Texture texture, Rectangle occlusionRect) {
		super(category);
		occlusionBounds.set(occlusionRect);
		shadowIcon = new CMSprite(texture);
		shadowIcon.setPosition(-shadowIcon.getWidth() / 2, -shadowIcon.getHeight() / 2);
		shadowIcon.setOrigin(shadowIcon.getWidth(), shadowIcon.getHeight());
		addSpriteChild(shadowIcon);
		setContentSize(shadowIcon.getWidth(), shadowIcon.getHeight());
		setTransform(true);
		
		setShader(scene.shaderByName("tileShadow"));
		setCustomRenderer(new ICustomRenderer() {
			@Override
			public void preDraw(Batch batch, float parentAlpha, Object obj) {
				if (getParent() == null)
					return;
				
				Prop prop = (Prop)obj;
				ShaderProgram shader = prop.getShader();
				batch.setShader(shader);
				scene.applyShaderDesciptor("tileShadow");
				
				shadowBounds = getShadowBounds();
				shader.setUniformf(
						"u_occRegion",
						(occlusionBounds.x - shadowBounds.x) / shadowBounds.width,
						(occlusionBounds.y - shadowBounds.y) / shadowBounds.height,
						Math.max(0.001f, occlusionBounds.width / shadowBounds.width),
						Math.max(0.001f, occlusionBounds.height / shadowBounds.height));
			}
			
			@Override
			public void postDraw(Batch batch, float parentAlpha, Object obj) {
				if (getParent() == null)
					return;
				
				batch.setShader(null);
			}
		});
	}
	
	public Rectangle getOcclusionBounds() { return occlusionBounds; }
	
	public void setOcclusionBounds(Rectangle value) { occlusionBounds.set(value); }
	
	public Rectangle getShadowBounds() {
		Group parent = this.getParent();
		if (parent == null) {
			shadowBounds.set(0, 0, 0, 0);
			return shadowBounds;
		}
		
		localPos.set(getX(), getY());
		//Vector2 shadowPos = parent.stageToLocalCoordinates(localToStageCoordinates(localPos));
		Vector2 shadowPos = localPos; // this.localToAscendantCoordinates(parent, localPos);
		float scaledWidth = getWidth() * getScaleX(), scaledHeight = getHeight() * getScaleY();
		shadowBounds.set(
				shadowPos.x - scaledWidth / 2,
				shadowPos.y - scaledHeight / 2,
				scaledWidth,
				scaledHeight);
		return shadowBounds;
	}
}
