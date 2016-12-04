package com.cheekymammoth.puzzleEffects;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.cheekymammoth.actions.EventActions;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.graphics.CMSprite;
import com.cheekymammoth.graphics.GfxMode.AlphaMode;
import com.cheekymammoth.graphics.ICustomRenderer;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.utils.PWDebug;
import com.cheekymammoth.utils.Transitions;
import com.cheekymammoth.utils.Utils;

public class Shield extends Prop implements IEventListener, Poolable {
	public enum ShieldType { NORMAL, TOP, BOTTOM };
	
	public static final int EV_TYPE_DEPLOYING;
    public static final int EV_TYPE_DEPLOYED;
    public static final int EV_TYPE_WITHDRAWING;
    public static final int EV_TYPE_WITHDRAWN;
    
    static {
    	EV_TYPE_DEPLOYING = EventDispatcher.nextEvType();
    	EV_TYPE_DEPLOYED = EventDispatcher.nextEvType();
    	EV_TYPE_WITHDRAWING = EventDispatcher.nextEvType();
    	EV_TYPE_WITHDRAWN = EventDispatcher.nextEvType();
    }

    public static final float kShieldRadius = 112f;
    public static final float kShieldDiameter = 2 * kShieldRadius;
    public static final int kMaxFoci = 2;

    private static final float kDeployDuration = 0.4f;
    private static final float kDeployedScale = 2f;
    private static final float kDeployedAlpha = 0.85f;
    private static final float kWithdrawDuration = 0.4f;
	
	private boolean isDeployed;
    private int ID = -1;
    private int tileIndex;
    private ShieldType type = ShieldType.NORMAL;
    private float elapsedTime;
    private Vector2 displacementScroll = new Vector2();
    private String[] stencilTextures = new String[2];
    private float[] stencilRotations = new float[2];
    private CMSprite shieldDome;
    private IEventListener listener;
    private ICustomRenderer shieldRenderer;
	
    private int blendSrcCache;
    private int blendDestCache;
	public Shield() {
		PWDebug.shieldCount++;
		stencilTextures[0] = "shield-stencil-0.png";
		stencilTextures[1] = "shield-stencil-0.png";
		shieldDome = new CMSprite(textureForType(type));
		shieldDome.setPosition(-shieldDome.getWidth() / 2, -shieldDome.getHeight() / 2);
		shieldDome.setAlphaMode(AlphaMode.POST_MULTIPLIED);
		addSpriteChild(shieldDome);
		setContentSize(shieldDome.getWidth(), shieldDome.getHeight());

		shieldRenderer = new ICustomRenderer() {
			@Override
			public void preDraw(Batch batch, float parentAlpha, Object obj) {
				blendSrcCache = batch.getBlendSrcFunc();
				blendDestCache = batch.getBlendDstFunc();
				batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

				Prop prop = (Prop)obj;
				ShaderProgram shader = prop.getShader();
				batch.setShader(shader);
				scene.setTextureForShaderDescriptor("shield", 2, scene.textureByName(stencilTextures[0]));
				scene.setTextureForShaderDescriptor("shield", 3, scene.textureByName(stencilTextures[1]));
				scene.applyShaderDesciptor("shield");
				
				Utils.moveInCircle(elapsedTime, 0.15f, displacementScroll);
				shader.setUniformf("u_displacementScroll", displacementScroll.x, displacementScroll.y);
				shader.setUniformf("u_shieldAlpha", 0.65f);
				shader.setUniformf("u_stencilRotation0", stencilRotations[0]);
				shader.setUniformf("u_stencilRotation1", stencilRotations[1]);
			}
			
			@Override
			public void postDraw(Batch batch, float parentAlpha, Object obj) {
				batch.setShader(null);
				batch.setBlendFunction(blendSrcCache, blendDestCache);
			}
		};
		
		setShader(scene.shaderByName("shield"));
		setCustomRenderer(shieldRenderer);
		setAdvanceable(true);
		setTransform(true);
	}
	
	public IEventListener getListener() { return listener; }
	
	public void setListener(IEventListener value) { listener = value; }
	
	public int getID() { return ID; }
	
	public void setID(int value) { ID = value; }
	
	public boolean isDeployed() { return isDeployed; }
	
	public int getTileIndex() { return tileIndex; }
	
	public void setTileIndex(int value) { tileIndex = value; }
	
	public ShieldType getType() { return type; }
	
	public void setType(ShieldType value) {
		if (type == value)
            return;

        switch (value) {
            case NORMAL:
	            break;
            case TOP:
	            break;
            case BOTTOM:
	            break;
        }

        shieldDome.setTexture(textureForType(value));
        type = value;
	}
	
	private Texture textureForType(ShieldType type) {
		Texture texture = null;
		
		switch (type) {
	        case NORMAL:
	        	texture = scene.textureByName("shield-dome.png");
	            break;
	        case TOP:
	        	texture = scene.textureByName("shield-dome-top.png");
	            break;
	        case BOTTOM:
	        	texture = scene.textureByName("shield-dome-btm.png");
	            break;
	    }
		
		return texture;
	}
	
	public void setStencil(int index, int texIndex, float texRotation) {
		if (index >= 0 && index < stencilTextures.length && texIndex >= 0 && texIndex < 5) {
            stencilTextures[index] = "shield-stencil-" + texIndex + ".png";
            stencilRotations[index] = texRotation;
        }
	}
	
	public void resetStencils() {
		stencilTextures[0] = "shield-stencil-0.png";
        stencilTextures[1] = "shield-stencil-0.png";
        stencilRotations[0] = stencilRotations[1] = 0;
	}
	
	public void deploy() {
		if (isDeployed)
            return;

        isDeployed = true;
        clearActions();
        notifyListener(EV_TYPE_DEPLOYING);
        setScale(0);
        setVisible(true);
        
        addAction(Actions.parallel(
        		Actions.alpha(kDeployedAlpha, kDeployDuration, Transitions.easeOutBack),
        		Actions.scaleTo(kDeployedScale, kDeployedScale, kDeployDuration, Transitions.easeOutBack)));
        addAction(Actions.after(EventActions.eventAction(EV_TYPE_DEPLOYED, this)));
        scene.playSound("tile-shield-activate");
	}
	
	public void withdraw() {
		withdraw(true);
	}
	
	public void withdraw(boolean playSound) {
		if (!isDeployed)
			return;
		
		isDeployed = false;
		clearActions();
		
		addAction(Actions.parallel(
        		Actions.alpha(0, kWithdrawDuration, Transitions.easeOutBack),
        		Actions.scaleTo(0, 0, kWithdrawDuration, Transitions.easeOut)));
        addAction(Actions.after(EventActions.eventAction(EV_TYPE_WITHDRAWN, this)));

        if (playSound)
            scene.playSound("tile-shield-deactivate");
        notifyListener(EV_TYPE_WITHDRAWING);
	}
	
	private void notifyListener(int evType) {
		if (listener != null)
			listener.onEvent(evType, this);
	}

	@Override
	public void onEvent(int evType, Object evData) {
		if (evType == EV_TYPE_DEPLOYED) {
			notifyListener(EV_TYPE_DEPLOYED);
		} else if (evType == EV_TYPE_WITHDRAWN) {
			setVisible(false);
			notifyListener(EV_TYPE_WITHDRAWN);
		}
	}
	
	@Override
	public void advanceTime(float dt) {
		elapsedTime += dt;
	}
	
	@Override
	public void reset() {
		clearActions();
		resetStencils();
		setID(-1);
		setTileIndex(-1);
		setListener(null);
		setColor(Utils.setA(getColor(), 0f));
		setVisible(false);
	}
}
