package com.cheekymammoth.puzzleViews;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.DelayAction;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.cheekymammoth.actions.SpriteColorAction;
import com.cheekymammoth.graphics.CMAtlasSprite;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.puzzleFactories.TileFactory;
import com.cheekymammoth.puzzles.PuzzleHelper;
import com.cheekymammoth.puzzles.Tile;
import com.cheekymammoth.utils.Coord;
import com.cheekymammoth.utils.PWDebug;

public class TilePiece extends Prop implements ITileView, Poolable {
	public enum AestheticState { NORMAL, EDGE, OCCLUSION };
	
	public static final Vector2 kTileDimensions = new Vector2(144, 144);

    // Tile color keys
    public static final int kColorKeyNone = 0;
    public static final int kColorKeyRed = 1;
    public static final int kColorKeyBlue = 2;
    public static final int kColorKeyGreen = 3;
    public static final int kColorKeyYellow = 4;
    public static final int kColorKeyWhite = 5;
    public static final int kColorKeyMulti = 6;

    // Decoration keys
    public static final int kTDKNone = 0x0;
    public static final int kTDKTeleport = 0x10;
    public static final int kTDKColorSwap = 0x20;
    public static final int kTDKRotate = 0x30;
    public static final int kTDKShield = 0x40;
    public static final int kTDKPainter = 0x50;
    public static final int kTDKTileSwap = 0x60;
    public static final int kTDKMirrorImage = 0x70;
    public static final int kTDKKey = 0x80;
    public static final int kTDKColorFlood = 0x90;
    public static final int kTDKColorSwirl = 0xa0;
    public static final int kTDKConveyorBelt = 0xb0;
    public static final int kTDKColorMagic = 0xc0;

    // Decoration style key ranges
    public static final int kTDSKTeleport = 4;
    public static final int kTDSKColorSwap = 10; // rb,rg,ry,rw,bg,by,bw,gy,gw,yw

    private static final float kColorTransitionDuration = 0.5f; // 0.25f;
    private static final float kColorFloodDuration = 0.25f;
    private static final float kColorSwirlDuration = 0.25f;
    private static final float kPainterDelayDuration = 0.05f;
    private static final float kColorFillDelayDuration = 0.35f;

    private static final float kOverlapOffsetY = 8f;
    private static final float kEdgeOffsetY = 56f;
	
    private static final int[] s_colorKeys = new int[] { 
    	kColorKeyRed, kColorKeyBlue, kColorKeyGreen, kColorKeyYellow, kColorKeyWhite, kColorKeyMulti
    };
    public static int[] getColorKeys() { return s_colorKeys; }

    private static final int[] s_decorationKeys = new int[] { 
    	kTDKKey,
        kTDKTeleport, kTDKTeleport + 0x1000, kTDKTeleport + 0x2000, kTDKTeleport + 0x3000,
        kTDKColorSwirl,
        kTDKColorSwap, kTDKColorSwap + 0x1000, kTDKColorSwap + 0x2000, kTDKColorSwap + 0x3000, kTDKColorSwap + 0x4000, kTDKColorSwap + 0x5000,
        kTDKColorSwap + 0x6000, kTDKColorSwap + 0x7000, kTDKColorSwap + 0x8000, kTDKColorSwap + 0x9000,
        kTDKRotate,
        kTDKConveyorBelt, kTDKConveyorBelt + 0x1000, kTDKConveyorBelt + 0x2000, kTDKConveyorBelt + 0x3000,
        kTDKShield,
        kTDKTileSwap, kTDKTileSwap + 0x1000, // kTDKTileSwap + 0x2000, kTDKTileSwap + 0x3000,
        kTDKMirrorImage,
        kTDKColorMagic
    };
    public static int[] getDecorationKeys() { return s_decorationKeys; }
    
    public static void setColorKeysForSwapKey(int swapKey, Coord leftXRightY) {
        // rb,rg,ry,rw,bg,by,bw,gy,gw,yw
        switch (swapKey >> Tile.kBitShiftDecorationStyle)
        {
            case 0: leftXRightY.x = kColorKeyRed; leftXRightY.y = kColorKeyBlue; break;
            case 1: leftXRightY.x = kColorKeyRed; leftXRightY.y = kColorKeyGreen; break;
            case 2: leftXRightY.x = kColorKeyRed; leftXRightY.y = kColorKeyYellow; break;
            case 3: leftXRightY.x = kColorKeyRed; leftXRightY.y = kColorKeyWhite; break;
            case 4: leftXRightY.x = kColorKeyBlue; leftXRightY.y = kColorKeyGreen; break;
            case 5: leftXRightY.x = kColorKeyBlue; leftXRightY.y = kColorKeyYellow; break;
            case 6: leftXRightY.x = kColorKeyBlue; leftXRightY.y = kColorKeyWhite; break;
            case 7: leftXRightY.x = kColorKeyGreen; leftXRightY.y = kColorKeyYellow; break;
            case 8: leftXRightY.x = kColorKeyGreen; leftXRightY.y = kColorKeyWhite; break;
            case 9: leftXRightY.x = kColorKeyYellow; leftXRightY.y = kColorKeyWhite; break;
            default: leftXRightY.x = kColorKeyWhite; leftXRightY.y = kColorKeyWhite; break;
        }
    }
    
    private boolean isMenuModeEnabled;
    private AestheticState aesState = AestheticState.NORMAL;
    private Tile tile;
    private CMAtlasSprite tileIcon;
    private TileDecoration decoration;
    private ITileDecorator decorator;
    private Color lerpColor = new Color(Color.WHITE);
    private SpriteColorAction colorLerper = new SpriteColorAction();
    private DelayAction colorDelayer = new DelayAction();
    
	public TilePiece() {
		this(-1);
		PWDebug.tilePieceCount++;
	}

	public TilePiece(int category) {
		super(category);
		
		tileIcon = new CMAtlasSprite(scene.textureRegionByName("tile-half"));
		
		//tileIcon.setPosition(-tileIcon.getWidth() / 2, -tileIcon.getHeight() / 2);
		//tileIcon.setOrigin(tileIcon.getWidth() / 2, tileIcon.getHeight() / 2);		
		tileIcon.setPosition(-kTileDimensions.x / 2, -(kTileDimensions.y / 2 + getOffsetY()));
		tileIcon.setOrigin(-tileIcon.getX() / 2, -tileIcon.getY() / 2);
		
		addSpriteChild(tileIcon);
		setContentSize(tileIcon.getWidth(), tileIcon.getHeight());
		
		colorLerper.setClearsOnRestart(false);
		colorLerper.addSprite(tileIcon);
		setTransform(true);
	}
	
	public Tile getTile() { return tile; }
	
	public Vector2 getTileDimensions() { return kTileDimensions; }
	
	private Rectangle boundsCache;
	public Rectangle getTileBounds() {
		Rectangle iconBounds = tileIcon.getBoundingRectangle();
		if (boundsCache == null)
			boundsCache = new Rectangle();
		boundsCache.set(getX() + iconBounds.x, getY() + iconBounds.y, kTileDimensions.x, kTileDimensions.y);
		return boundsCache;
	}
	
	public float getOffsetY() {
		float offset = 0;
        if (tile != null) {
            switch (aesState) {
	            case NORMAL:
		            offset = tile.isEdgeTile() ? kEdgeOffsetY : kOverlapOffsetY;
		            break;
	            case EDGE:
		            offset = kEdgeOffsetY;
		            break;
	            case OCCLUSION:
		            offset = 0;
		            break;
            }
        }
        return offset;
	}
	
	private Vector2 shadowPosCache = new Vector2();
	public Vector2 getShadowPosition() {
		shadowPosCache.set(getX(), getY() - getOffsetY());
		return shadowPosCache;
	}
	
	public AestheticState getAesState() { return aesState; }

	public void setAesState(AestheticState value) {
		if (aesState == value)
            return;

        aesState = value;
        updateEdgeStatus();
	}
	
	public boolean isMenuModeEnabled() { return isMenuModeEnabled; }
	
	public void enableMenuMode(boolean enable) {
		if (isMenuModeEnabled() == enable)
			return;
		
		isMenuModeEnabled = enable;
		if (decoration != null)
			decoration.enableMenuMode(enable);
		updateEdgeStatus();
	}
	
	//private TileDecoration getDecoration() { return decoration; }
	
	private void setDecoration(TileDecoration value) {
		if (decoration == value)
            return;

        if (decoration != null)
        	TileFactory.freeDecoration(decoration);

        decoration = value;
        if (decoration != null) {
        	decoration.setDecorator(getDecorator());
        	addActor(decoration);
        }
	}
	
	public ITileDecorator getDecorator() { return decorator; }
	
	public void setDecorator(ITileDecorator value) {
		if (decorator != value) {
			decorator = value;
            if (decoration != null)
            	decoration.setDecorator(value);
        }
	}
	
	protected void updateEdgeStatus() {
        if (tile == null)
            return;

        if ((tile.isEdgeTile() && aesState == AestheticState.NORMAL) || aesState == AestheticState.EDGE) {
        	tileIcon.setAtlasRegion(scene.textureRegionByName("tile-full"));
        	setContentSize(tileIcon.getWidth(), tileIcon.getHeight());
        } else if (!tile.isEdgeTile() && aesState == AestheticState.NORMAL) {
        	tileIcon.setAtlasRegion(scene.textureRegionByName("tile-half"));
        	setContentSize(tileIcon.getWidth(), tileIcon.getHeight());
        } else if (aesState == AestheticState.OCCLUSION) {
        	tileIcon.setAtlasRegion(scene.textureRegionByName("tile-72"));
        	setContentSize(tileIcon.getWidth(), tileIcon.getHeight());
        }
        
        tileIcon.setPosition(-kTileDimensions.x / 2, -(kTileDimensions.y / 2 + getOffsetY()));
		tileIcon.setOrigin(-tileIcon.getX() / 2, -tileIcon.getY() / 2);
    }
	
	protected void cancelColoring() {
		removeAction(colorDelayer);
	}
	
	protected void transitionToColor(Color src, Color dest) {
		transitionToColor(src, dest, kColorTransitionDuration, 0f);
    }
	
	protected void transitionToColor(Color src, Color dest, float duration, float delay) {
		cancelColoring();
		
		colorLerper.reset();
		colorLerper.setColor(src);
		colorLerper.setEndColor(dest);
		colorLerper.setDuration(duration);
		
		colorDelayer.reset();
		colorDelayer.setDuration(delay);
		colorDelayer.setAction(colorLerper);
		
		addAction(colorDelayer);
    }
	
	public void setData(Tile tile) {
        if (this.tile != null)
        	this.tile.deregisterView(this);

        this.tile = tile;
        if (this.tile != null) {
        	this.tile.registerView(this);
            updateEdgeStatus();
            syncWithData();
        }
    }
	
	public void syncWithData() {
        if (tile == null)
            return;

        // Tile color
        Color src = tileIcon.getColor(), dest = lerpColor;
        dest.set(PuzzleHelper.colorForKey(tile.getColorKey()));

        switch (tile.getDecorator()) {
            case Tile.kTFColorSwap:
                transitionToColor(src, dest);
                break;
            case Tile.kTFPainter:
                transitionToColor(src, dest, kColorTransitionDuration / 2, tile.getDecoratorData() * kPainterDelayDuration);
                break;
            case Tile.kTFColorFlood:
                transitionToColor(
                		src,
                		dest,
                		1.35f * kColorFloodDuration,
                		(float)Math.sqrt(4 + tile.getDecoratorData()) / 2 * kColorFillDelayDuration - kColorFillDelayDuration);
                break;
            case Tile.kTFColorSwirl:
            	transitionToColor(src, dest, kColorSwirlDuration, 2 * tile.getDecoratorData() * kPainterDelayDuration);
                break;
            default:
            	cancelColoring();
            	tileIcon.setColor(dest);
                break;
        }
        
        // Tile decoration
        TileDecoration decoration = PuzzleHelper.decorationForTile(tile);
        if (decoration != null) {
        	if (this.decoration != null && this.decoration.getType() == decoration.getType()) {
        		decoration.syncWithTileDecoration(this.decoration);
        		setDecoration(decoration);
        	} else {
        		setDecoration(decoration);
        		decoration.syncWithDecorator();
        	}
        } else
        	setDecoration(decoration);
	}
	
	public void syncWithTilePiece(TilePiece tilePiece) {
        if (tilePiece != null && decoration != null && tilePiece.decoration != null)
            decoration.syncWithTileDecoration(tilePiece.decoration);
    }
	
	public void decorationValueDidChange(int evCode, float value) {
		if (decoration != null)
			decoration.valueDidChange(evCode, value);
	}
	
	@Override
	public void tilePropertiesDidChange(int code) {
		if (code == Tile.kPropCodeIsEdge)
            updateEdgeStatus();
        else
            syncWithData();
	}

	@Override
	public void tileModifiersDidChange() {
		syncWithData();
	}
	
	@Override
	public void reset() {
		cancelColoring();
		clearActions();
		setDecoration(null);
		setDecorator(null);
		setData(null);
		setShader(null);
		setCustomRenderer(null);
		
		tileIcon.setColor(Color.WHITE);
		setColor(Color.WHITE);
		setScale(1f);
		setRotation(0f);
		setVisible(true);
		enableMenuMode(false);
		setAesState(AestheticState.NORMAL);
		remove();
	}
	
	public TilePiece clone() {
        TilePiece tilePiece = TileFactory.getTilePiece(tile);
        if (tilePiece != null)
            tilePiece.syncWithTilePiece(this);
        return tilePiece;
    }
}
