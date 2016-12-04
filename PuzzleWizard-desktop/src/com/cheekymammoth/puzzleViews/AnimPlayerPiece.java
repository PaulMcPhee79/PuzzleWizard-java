package com.cheekymammoth.puzzleViews;

import java.nio.ByteBuffer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.cheekymammoth.actions.PropColorAction;
import com.cheekymammoth.animations.MovieReel;
import com.cheekymammoth.graphics.ICustomRenderer;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.puzzleEffects.EffectFactory;
import com.cheekymammoth.puzzleEffects.Twinkle;
import com.cheekymammoth.puzzles.Player;
import com.cheekymammoth.puzzles.Player.PlayerType;
import com.cheekymammoth.puzzles.PuzzleHelper;
import com.cheekymammoth.puzzles.Tile;
import com.cheekymammoth.utils.Coord;
import com.cheekymammoth.utils.FloatTweener;
import com.cheekymammoth.utils.Transitions;
import com.cheekymammoth.utils.Utils;

abstract public class AnimPlayerPiece extends PlayerPiece {
	private static final float kSingleMoveDuration = 0.36f;
    private static final float kMirroredAlpha = 0.8f;
    private static final float kColorTransitionDuration = 0.35f;
    private static final float kTeleportTwinkleDuration = 0.7f;
    private static final float kClipOffsetY = 64f;

    private static final int kLowerIndex = 0;
    private static final int kMidIndex = 1;
    private static final int kUpperIndex = 2;
    private static final int kNumIndexes = 3;
    
    private static int clipIndexForPlayerOrientation(int orientation) {
        if (orientation == Player.kEasternOrientation)
            return 1;
        else if (orientation == Player.kSouthernOrientation)
            return 2;
        else if (orientation == Player.kWesternOrientation)
            return 3;
        else
            return 0;
    }
    
    private boolean isInitializing = true;
    private boolean didTeleportThisFrame;
    private boolean shouldPlaySounds = true;
    private int currentReelIndex = 0;
    private int orientationCache;
    private float magicX;
    private float mirrorX;
    private MovieReel[][] idleReels = new MovieReel[kNumIndexes][4]; // [lower,mid,upper][n,e,s,w]
    private MovieReel[][] movingReels = new MovieReel[kNumIndexes][4]; // [lower,mid,upper][n,e,s,w]
    private Prop[] movingCanvas = new Prop[kNumIndexes]; // [lower,mid,upper]
    private Prop[] idleCanvas = new Prop[kNumIndexes]; // [lower,mid,upper]
    private Color lerpColor = new Color(Color.WHITE);
    private PropColorAction colorLerper = new PropColorAction();
    private Array<Twinkle> twinkles = new Array<Twinkle>(true, 3, Twinkle.class);
    private ShaderProgram colorMagicShader;
    private ShaderProgram mirrorImageShader;
    private ICustomRenderer colorMagicRenderer;
    private ICustomRenderer mirrorImageRenderer;
    private Coord moveAxis = new Coord();
    private FloatTweener moveTweener;
    private static int attrSize;
    private static ByteBuffer byteBuffer;
	
	public AnimPlayerPiece() {
		this(-1);
	}

	private AnimPlayerPiece(int category) {
		super(category);
		
		setAdvanceable(true);
		
		for (int i = 0, n = idleCanvas.length; i < n; i++) {
			idleCanvas[i] = new Prop();
			playerCanvas.addActor(idleCanvas[i]);
		}
		
		for (int i = 0, n = movingCanvas.length; i < n; i++) {
			movingCanvas[i] = new Prop();
			playerCanvas.addActor(movingCanvas[i]);
		}
		
		colorLerper.setClearsOnRestart(false);
		colorLerper.addProp(idleCanvas[kUpperIndex]);
		colorLerper.addProp(movingCanvas[kUpperIndex]);
		
		moveTweener = new FloatTweener(0, Transitions.linear, this);
		
		String[] extensions = new String[] { "n", "ew", "s", "ew" }; // n,e,s,w
		for (int i = 0, n = extensions.length; i < n; i++) {
			for (int j = 0; j < kNumIndexes; j++) {
				String framesPrefix = null;
				switch (j) {
					case kLowerIndex: framesPrefix = "idle_body_"; break;
					case kMidIndex: framesPrefix = getIdleMidFramesPrefix(); break;
					case kUpperIndex: framesPrefix = "idle_hat_"; break;
					default: continue;
				}
				
				Array<AtlasRegion> frames = scene.textureRegionsStartingWith(
						framesPrefix + extensions[i]);
				
				if (i == 0 && j == 0)
					setContentSize(frames.get(0).getRegionWidth(), frames.get(0).getRegionHeight());
				
				// Idle
				MovieReel reel = new MovieReel(frames, 24);
				if (i == 1)
					reel.setScale(-1f, 1f);
				reel.setX(-reel.getWidth() / 2);

                if (i == 1)
                	reel.setX(reel.getX()-10f);
                else if (i == 3)
                	reel.setX(reel.getX()+10f);

                reel.setY(kClipOffsetY - reel.getHeight() / 2);
                
                reel.setVisible(false);
                reel.pause();
             
                idleCanvas[j].addSpriteChild(reel);
                idleReels[j][i] = reel;
                
                // Moving
                switch (j) {
					case kLowerIndex: framesPrefix = "walk_body_"; break;
					case kMidIndex: framesPrefix = getMovingMidFramesPrefix(); break;
					case kUpperIndex: framesPrefix = "walk_hat_"; break;
					default: continue;
				}
                
                frames = scene.textureRegionsStartingWith(framesPrefix + extensions[i]);
                
                reel = new MovieReel(frames, 45);
                
                if (i == 1)
					reel.setScale(-1f, 1f);
				reel.setX(-reel.getWidth() / 2);

                if (i == 1)
                	reel.setX(reel.getX()+6f);
                else if (i == 3)
                	reel.setX(reel.getX()-6f);

                reel.setY(kClipOffsetY - reel.getHeight() / 2);

                if (i == 2)
                	reel.setY(reel.getY()-14f);
                else if (i == 1 || i == 3)
                	reel.setY(reel.getY()-8f);

                reel.setVisible(false);
                reel.pause();
                movingCanvas[j].addSpriteChild(reel);
                movingReels[j][i] = reel;
			}
		}
		
		playerCanvas.setScale(1.15f);
		
		colorMagicShader = scene.shaderByName("colorGradient");
		mirrorImageShader = scene.shaderByName("mirrorImage");
		
		colorMagicRenderer = new ICustomRenderer() {
			@Override
			public void preDraw(Batch batch, float parentAlpha, Object obj) {
				Prop prop = (Prop)obj;
				ShaderProgram shader = prop.getShader();
				batch.setShader(shader);
				scene.applyShaderDesciptor("colorGradient");
				shader.setUniformf("u_gradientCoords", magicX, 0);
			}
			
			@Override
			public void postDraw(Batch batch, float parentAlpha, Object obj) {
				batch.setShader(null);
			}
		};
		
		if (byteBuffer == null) {
			attrSize = mirrorImageShader.getAttributeSize(Utils.kShaderCoordAttrName);
			int bufferLen = attrSize * 8 * 4;
			byteBuffer = BufferUtils.newUnsafeByteBuffer(bufferLen); 
			Utils.initShaderCoordBuffer(byteBuffer, bufferLen);
		}
		
		mirrorImageRenderer = new ICustomRenderer() {
			@Override
			public void preDraw(Batch batch, float parentAlpha, Object obj) {
				Prop prop = (Prop)obj;
				ShaderProgram shader = prop.getShader();
				batch.setShader(shader);
				byteBuffer.position(0);
				shader.enableVertexAttribute(Utils.kShaderCoordAttrName);
				shader.setVertexAttribute(Utils.kShaderCoordAttrName, attrSize, GL10.GL_FLOAT, false, 8, byteBuffer);
				scene.applyShaderDesciptor("mirrorImage");
				shader.setUniformf("u_gradientCoord",
						getCurrentReelIndex() == clipIndexForPlayerOrientation(Player.kEasternOrientation)
						? -mirrorX
						: mirrorX);
			}
			
			@Override
			public void postDraw(Batch batch, float parentAlpha, Object obj) {
				batch.setShader(null);
				
				Prop prop = (Prop)obj;
				ShaderProgram shader = prop.getShader();
				shader.disableVertexAttribute(Utils.kShaderCoordAttrName);
				// TODO: ensure that glGetError() == GL_NO_ERROR
			}
		};
	}
	
	abstract protected String getIdleMidFramesPrefix();
    abstract protected String getMovingMidFramesPrefix();

	private int getCurrentReelIndex() { return currentReelIndex; }
	
	private void setCurrentClipIndex(int value) {
		currentIdleLowerClip().setVisible(false);
		currentIdleMidClip().setVisible(false);
		currentIdleUpperClip().setVisible(false);
		currentMovingLowerClip().setVisible(false);
		currentMovingMidClip().setVisible(false);
		currentMovingUpperClip().setVisible(false);
		
		currentReelIndex = value;

        bindShaderAndRenderer();

        if (getState() == PPState.IDLE) {
        	currentIdleLowerClip().play();
        	currentIdleMidClip().play();
        	currentIdleUpperClip().play();
        	syncIdleClipFrames();
        	currentIdleLowerClip().setVisible(true);
        	currentIdleMidClip().setVisible(true);
        	currentIdleUpperClip().setVisible(true);

            currentMovingLowerClip().pause();
            currentMovingMidClip().pause();
            currentMovingUpperClip().pause();
            currentMovingLowerClip().setVisible(false);
            currentMovingMidClip().setVisible(false);
            currentMovingUpperClip().setVisible(false);
        } else {
        	currentIdleLowerClip().pause();
        	currentIdleMidClip().pause();
        	currentIdleUpperClip().pause();
            currentIdleLowerClip().setVisible(false);
            currentIdleMidClip().setVisible(false);
            currentIdleUpperClip().setVisible(false);

            currentMovingLowerClip().play();
            currentMovingMidClip().play();
            currentMovingUpperClip().play();
            currentMovingLowerClip().setVisible(true);
            currentMovingMidClip().setVisible(true);
            currentMovingUpperClip().setVisible(true);
        }
	}
	
	private void syncIdleClipFrames() {
		int currentFrame = currentIdleUpperClip().getCurrentFrame();
		currentIdleLowerClip().setCurrentFrame(currentFrame);
		currentIdleMidClip().setCurrentFrame(currentFrame);
	}
	
//	private void syncMovingClipFrames() {
//		int currentFrame = currentMovingUpperClip().getCurrentFrame();
//		currentMovingLowerClip().setCurrentFrame(currentFrame);
//		currentMovingMidClip().setCurrentFrame(currentFrame);
//	}
	
	private ShaderProgram getPlayerShader() {
		if (player == null)
			return null;
		
		return player.getType() == PlayerType.MIRRORED ? mirrorImageShader
				: (player.isColorMagicActive() ? colorMagicShader : null);
	}
	
	private ICustomRenderer getPlayerRenderer() {
		if (player == null)
			return null;
		
		return player.getType() == PlayerType.MIRRORED ? mirrorImageRenderer
				: (player.isColorMagicActive() ? colorMagicRenderer : null);
	}
	
	private MovieReel currentIdleLowerClip() { return idleReels[kLowerIndex][currentReelIndex]; }
	private MovieReel currentIdleMidClip() { return idleReels[kMidIndex][currentReelIndex]; }
	private MovieReel currentIdleUpperClip() { return idleReels[kUpperIndex][currentReelIndex]; }
	private MovieReel currentMovingLowerClip() { return movingReels[kLowerIndex][currentReelIndex]; }
	private MovieReel currentMovingMidClip() { return movingReels[kMidIndex][currentReelIndex]; }
	private MovieReel currentMovingUpperClip() { return movingReels[kUpperIndex][currentReelIndex]; }

	@Override
	protected void setState(PPState value) {
		super.setState(value);
		
		switch (value) {
			case IDLE:
	        {
	        	currentIdleLowerClip().play();
	        	currentIdleMidClip().play();
	        	currentIdleUpperClip().play();
	        	syncIdleClipFrames();
	        	currentIdleLowerClip().setVisible(true);
	        	currentIdleMidClip().setVisible(true);
	        	currentIdleUpperClip().setVisible(true);
	
	        	currentMovingLowerClip().pause();
	        	currentMovingMidClip().pause();
	        	currentMovingUpperClip().pause();
	            currentMovingLowerClip().setVisible(false);
	            currentMovingMidClip().setVisible(false);
	            currentMovingUpperClip().setVisible(false);
	
	            clampToTileGrid();
	        }
	        break;
		case TREADMILL:
	    case MOVING:
	        {
	        	currentIdleLowerClip().pause();
	        	currentIdleMidClip().pause();
	        	currentIdleUpperClip().pause();
	        	currentIdleLowerClip().setVisible(false);
	        	currentIdleMidClip().setVisible(false);
	        	currentIdleUpperClip().setVisible(false);
	
	        	currentMovingLowerClip().play();
	        	currentMovingMidClip().play();
	        	currentMovingUpperClip().play();
	            //currentMovingLowerClip().setCurrentFrame(currentMovingUpperClip().getCurrentFrame());
	        	currentMovingLowerClip().setVisible(true);
	        	currentMovingMidClip().setVisible(true);
	        	currentMovingUpperClip().setVisible(true);
	
	        	if (value == PPState.MOVING) {
		            if (player != null && player.getColorKey() != player.getFutureColorKey()) {
		            	Color src = idleCanvas[kUpperIndex].getColor(), dest = lerpColor;
		                dest.set(PuzzleHelper.playerColorForKey(player.getFutureColorKey()));
		                Utils.setA(dest, src.a);
		                transitionToSkinColor(src, dest, kColorTransitionDuration);
		            }
	        	}
	        }
        break;
		}
	}
	
	protected void bindShaderAndRenderer() {
		//setShader(getPlayerShader());
        //setCustomRenderer(getPlayerRenderer());
		
		ShaderProgram shader = getPlayerShader();
		ICustomRenderer renderer = getPlayerRenderer();
		
		for (int i = kLowerIndex; i <= kUpperIndex; i++) {
			idleCanvas[i].setShader(shader);
			idleCanvas[i].setCustomRenderer(renderer);
			movingCanvas[i].setShader(shader);
			movingCanvas[i].setCustomRenderer(renderer);
		}
	}
	
	@Override
	public void transitionOut(float duration) {
		super.transitionOut(duration);
		addCurrentReelsToJuggler();
	}
	
	protected void addCurrentReelsToJuggler() {
		if (getState() == PPState.IDLE) {
        	for (int i = 0, n = idleReels.length; i < n; i++)
        		scene.addToJuggler(idleReels[i][orientationCache]);
        } else if (getState() == PPState.MOVING) {
        	for (int i = 0, n = movingReels.length; i < n; i++) 
        		scene.addToJuggler(movingReels[i][orientationCache]);
        }
	}
	
	protected void removeCurrentReelsFromJuggler() {
		if (getState() == PPState.IDLE) {
        	for (int i = 0, n = idleReels.length; i < n; i++) 
        		scene.removeFromJuggler(idleReels[i][orientationCache]);
        } else if (getState() == PPState.MOVING) {
        	for (int i = 0, n = movingReels.length; i < n; i++) 
        		scene.removeFromJuggler(movingReels[i][orientationCache]);
        }
	}
	
	@Override
	public void enableMenuMode(boolean enable) {
		super.enableMenuMode(enable);
		
		clampToTileGrid();
		setShader(null);
	}
	
	private void clampToTileGrid() {
		if (player != null) {
			Coord pos = player.getViewPosition();
	        Vector2 moveDims = player.getMoveDimensions();
			setPosition(pos.x * moveDims.x, pos.y * moveDims.y);
		}
	}
	
	@Override
	public void setPositionAestheticOnly(float x, float y) {
		super.setPositionAestheticOnly(x, y);
		setPosition(x, y);
	}
	
	@Override
	public void refreshAesthetics() {
        super.refreshAesthetics();

        if (player != null)
        	setSkinColor(Utils.setRGB(getColor(), PuzzleHelper.playerColorForKey(player.getColorKey())));
    }
	
	protected Color getTransitionDestColor() {
		if (player == null)
			return idleCanvas[kUpperIndex].getColor();
		else {
			Color src = idleCanvas[kUpperIndex].getColor(), dest = lerpColor;
			dest.set(PuzzleHelper.playerColorForKey(player.getColorKey()));
			Utils.setA(dest, src.a);
			return dest;
		}
	}
	
	protected void setSkinColor(Color color) {
		cancelColoring();
		
		idleCanvas[kUpperIndex].setColor(color);
		movingCanvas[kUpperIndex].setColor(color);
    }
	
	protected void transitionToSkinColor(Color src, Color dest, float duration) {
		cancelColoring();
		
		colorLerper.reset();
		colorLerper.setColor(src);
		colorLerper.setEndColor(dest);
		colorLerper.setDuration(duration);
		addAction(colorLerper);
    }
	
	protected void cancelColoring() {
		removeAction(colorLerper);
	}

	protected void didTeleport() {
        clampToTileGrid();

        if (didTeleportThisFrame)
            return;

        didTeleportThisFrame = true;

        if (shouldPlaySounds)
            scene.playSound("player-teleport");

        Twinkle twinkle = EffectFactory.getTwinkle();
        twinkle.setListener(this);
        twinkle.setAnimationScaleMax(1f);
        addActor(twinkle);
        twinkles.add(twinkle);
        twinkle.animate(kTeleportTwinkleDuration);
    }
	
	@Override
	public void onEvent(int evType, Object evData) {
		super.onEvent(evType, evData);
		
		if (evType == FloatTweener.EV_TYPE_FLOAT_TWEENER_CHANGED) {
			if (getState() == PPState.MOVING) {
				if (moveAxis.x != 0)
					setX(moveTweener.getTweenedValue());
				else if (moveAxis.y != 0)
					setY(moveTweener.getTweenedValue());
			}
		} else if (evType == FloatTweener.EV_TYPE_FLOAT_TWEENER_COMPLETED) {
			if (getState() == PPState.MOVING) {
                if (player != null) {
                	player.setMoveOffsetDuration(moveTweener.getDurationRemainder());
                    player.didFinishMoving();
                }
            }
		} else if (evType == Twinkle.EV_TYPE_ANIMATION_COMPLETED) {
			if (evData != null) {
                Twinkle twinkle = (Twinkle)evData;
                twinkles.removeValue(twinkle, true);
                EffectFactory.freeTwinkle(twinkle);
        	}
		}
	}
	
	@Override
	public void didFinishMoving() {
		super.didFinishMoving();
		moveTweener.forceCompletion(); // Ensures MirrorImage is synched.
	}
	
	@Override
	public void setData(Player player) {
		if (player != null) {
        	setColor(Utils.setA(getColor(), player.getType() == PlayerType.MIRRORED ? kMirroredAlpha : 1f));
        	moveTweener.setDurationRemainder(player.getMoveOffsetDuration());
        	isInitializing = true;
        }
		
        super.setData(player);
    }
	
	@Override
	public void syncWithPlayerPiece(PlayerPiece playerPiece) {
        if (playerPiece != null && playerPiece instanceof AnimPlayerPiece) {
            AnimPlayerPiece other = (AnimPlayerPiece)playerPiece;
            for (int i = 0, n = idleReels.length; i < n; i++) {
                for (int j = 0, m = idleReels[i].length; j < m; j++) {
                	idleReels[i][j].setCurrentFrame(other.idleReels[i][j].getCurrentFrame());
                	movingReels[i][j].setCurrentFrame(other.movingReels[i][j].getCurrentFrame());
                }
            }
            
            // Commented out: it is not needed due to transitioning
            // color during tile movement rather than after.
//            setSkinColor(other.idleCanvas[kUpperIndex].getColor());
//            
//            Array<Action> actions = other.getActions();
//            if (actions != null && actions.contains(other.colorLerper, true))
//            	transitionToSkinColor(
//            			other.colorLerper.getColor(),
//            			other.colorLerper.getEndColor(),
//            			other.colorLerper.getDuration() - other.colorLerper.getTime());
        }
    }

	@Override
	public void syncWithData() {
		super.syncWithData();
		if (player == null)
			return;
		
		if (isStationary())
            clampToTileGrid();
		setCurrentClipIndex(clipIndexForPlayerOrientation(player.getOrientation()));

		Color src = idleCanvas[kUpperIndex].getColor(), dest = lerpColor;
        dest.set(PuzzleHelper.playerColorForKey(player.getColorKey()));
        Utils.setA(dest, src.a);
		
        if (isInitializing)
            setSkinColor(dest);
        else if (player.getPrevColorKey() != player.getColorKey())
            transitionToSkinColor(src, dest, kColorTransitionDuration);

        switch (player.getFunction())
        {
            case Tile.kTFTeleport:
                didTeleport();
                break;
        }

        // Toggle shader and renderer as needed to match the player state
        if (player.isColorMagicActive() != (getShader() == colorMagicShader))
        	bindShaderAndRenderer();
        
        orientationCache = player.getOrientation();
        isInitializing = false;
	}
	
	protected void cancelMoving() {
		moveTweener.resetTween(0);
	}
	
	@Override
	public void moveTo(Coord pos) {
		float durationRemainder = moveTweener.getDurationRemainder();
		cancelMoving();
        setState(PPState.MOVING);

        if (player != null) {
        	float moveToX = getX(), moveToY = getY();
        	moveAxis.set(0, 0);
        	
            if (player.getOrientation() == Player.kEasternOrientation || player.getOrientation() == Player.kWesternOrientation) {
            	moveToX = pos.x * player.getMoveDimensions().x;
            	moveAxis.x = moveToX != getX() ? 1 : 0;
            	moveTweener.resetTween(getX(), moveToX, kSingleMoveDuration, 0);
            } else {
            	moveToY = (player.getViewOffset().y - pos.y) * player.getMoveDimensions().y;
            	moveAxis.y = moveToY != getY() ? 1 : 0;
            	moveTweener.resetTween(getY(), moveToY, kSingleMoveDuration, 0);
            }
            
            if (durationRemainder != 0)
            	moveTweener.advanceTime(durationRemainder);
        }
    }
	
	@Override
	public void playerValueDidChange(int code, int value) {
		// Ignore super implementation
		switch (code) {
            case Player.kValueProperty:
                syncWithData();
                break;
            case Player.kValueColorMagic:
                break;
            case Player.kValueMirrorImage:
                break;
            case Player.kValueOrientation:
                clampToTileGrid();
                break;
            case Player.kValueTeleported:
                break;
            case Player.kValueForceCompleteMove:
            	if (getState() == PPState.MOVING)
            		moveTweener.forceCompletion();
            	break;
        }
	}
	
	@Override
	public void advanceTime(float dt) {
		moveTweener.setDurationRemainder(0);
		if (getState() == PPState.MOVING)
			moveTweener.advanceTime(dt);
		
		magicX += 0.7f * dt;
        if (magicX > 1f)
        	magicX = magicX - (int)magicX;
        
        mirrorX += 0.85f * dt;
        if (mirrorX > 1.0f)
        	mirrorX = mirrorX - (int)mirrorX;
        
        int orientation = orientationCache;
        if (getState() == PPState.IDLE) {
        	for (int i = 0, n = idleReels.length; i < n; i++) 
        		idleReels[i][orientation].advanceTime(dt);
        } else if (getState() == PPState.TREADMILL || getState() == PPState.MOVING) {
        	for (int i = 0, n = movingReels.length; i < n; i++) 
        		movingReels[i][orientation].advanceTime(dt);
        }
        
        didTeleportThisFrame = false;
	}
	
	@Override
	public void softReset() {
		cancelColoring();
		setSkinColor(getTransitionDestColor());
		cancelMoving();
		setShader(null);
		setCustomRenderer(null);
		removeCurrentReelsFromJuggler();
		for (int i = twinkles.size-1; i >= 0; i--) {
			Twinkle twinkle = twinkles.get(i);
			twinkle.stopAnimating();
		}
		
		// Call last due to state change implications to local cleanup
		super.softReset();
	}
	
	@Override
	public void reset() {
		softReset();
		super.reset();
	}

//	@Override
//	public void dispose() {
//		BufferUtils.disposeUnsafeByteBuffer(byteBuffer);
//	}
}
