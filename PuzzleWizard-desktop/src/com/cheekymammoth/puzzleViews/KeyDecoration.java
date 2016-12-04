package com.cheekymammoth.puzzleViews;

import java.nio.ByteBuffer;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.cheekymammoth.graphics.CMAtlasSprite;
import com.cheekymammoth.graphics.ICustomRenderer;
import com.cheekymammoth.puzzleEffects.Twinkle;
import com.cheekymammoth.utils.PWDebug;
import com.cheekymammoth.utils.Utils;

public class KeyDecoration extends TileDecoration implements Disposable {
	private static final float kKeyTwinkleDuration = 1f;
	
	private float sparkleX;
    private Twinkle twinkle;
    private CMAtlasSprite key;
    private ShaderProgram keyShader;
    
    private static int attrSize;
    private static ByteBuffer byteBuffer;
    private ICustomRenderer keyRenderer;
    
    public KeyDecoration() {
    	PWDebug.tileDecorationCount++;
    	keyShader = scene.shaderByName("sparkle");
    	attrSize = keyShader.getAttributeSize(Utils.kShaderCoordAttrName);
		//attrType = keyShader.getAttributeType(kShaderCoordAttrName);
		
		int bufferLen = attrSize * 8 * 4; // attrSize * 8 * sizeOf(GL_FLOAT) [4 vertices, 2 floats per vert]
		byteBuffer = BufferUtils.newUnsafeByteBuffer(bufferLen); 
		Utils.initShaderCoordBuffer(byteBuffer, bufferLen);
		
		keyRenderer = new ICustomRenderer() {
			@Override
			public void preDraw(Batch batch, float parentAlpha, Object obj) {
				KeyDecoration keyDec = (KeyDecoration)obj;
				ShaderProgram shader = keyDec.getShader();
				
				if (shader == null)
					return;
				
				batch.setShader(shader);
				byteBuffer.position(0);
				keyShader.enableVertexAttribute(Utils.kShaderCoordAttrName);
				keyShader.setVertexAttribute(Utils.kShaderCoordAttrName, attrSize, GL10.GL_FLOAT, false, 8, byteBuffer);
				scene.applyShaderDesciptor("sparkle");
				shader.setUniformf("u_gradientCoord", sparkleX < 1f ? -0.75f * sparkleX : 0.0f);
			}
			
			@Override
			public void postDraw(Batch batch, float parentAlpha, Object obj) {
				KeyDecoration keyDec = (KeyDecoration)obj;
				ShaderProgram shader = keyDec.getShader();
				
				if (shader == null)
					return;
				
				batch.setShader(null);
				shader.disableVertexAttribute(Utils.kShaderCoordAttrName);
			}
		};
		
		setShader(keyShader);
		setCustomRenderer(keyRenderer);
		
		key = new CMAtlasSprite(scene.textureRegionByName("key"));
		key.centerContent();
		addSpriteChild(key);
		setContentSize(key.getWidth(), key.getHeight());
		
		twinkle = new Twinkle();
		twinkle.setPosition(0.25f * getWidth(), 0.225f * getHeight());
		addActor(twinkle);
		
		setAdvanceable(true);
    }
    
	public KeyDecoration(int category, int type, int subType) {
		this();
		setType(type);
		setSubType(subType);
	}
	
	@Override
	public void enableMenuMode(boolean enable) {
		setShader(enable ? null : keyShader);
	}
	
	@Override
	public void syncWithDecorator() {
		ITileDecorator decorator = getDecorator();
		if (decorator != null) {
			sparkleX = decorator.decoratorValueForKey(getType());
			
			if(!twinkle.isAnimating() && sparkleX >= 0.4f && sparkleX < (0.4f + 0.8f * kKeyTwinkleDuration)) {
				twinkle.animate(kKeyTwinkleDuration);
				twinkle.fastForward(sparkleX-0.4f);
			}
		}
	}
	
	@Override
	public void syncWithTileDecoration(TileDecoration other) {
		if (other instanceof KeyDecoration) {
			KeyDecoration otherKey = (KeyDecoration)other;
			sparkleX = otherKey.sparkleX;
			twinkle.syncWithTwinkle(otherKey.twinkle);
		}
	}
	
	@Override
	public void advanceTime(float dt) {
		super.advanceTime(dt);
		
		sparkleX += dt;
		if (sparkleX > 6.0f)
			sparkleX = 0.0f;
		
		syncWithDecorator();
	}
	
	@Override
	public void reset() {
		twinkle.stopAnimating();
		super.reset();
	}

	@Override
	public void dispose() {
		BufferUtils.disposeUnsafeByteBuffer(byteBuffer);
	}

//	@Override
//	public void onEvent(int evType, Object evData) {
//		if (evType == Twinkle.EV_TYPE_ANIMATION_COMPLETED)
//			twinkle.animate(1.25f);
//	}
}
