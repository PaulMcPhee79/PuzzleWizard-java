package com.cheekymammoth.puzzleViews;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.graphics.CMAtlasSprite;
import com.cheekymammoth.utils.PWDebug;
import com.cheekymammoth.utils.Utils;

public class TeleportDecoration extends TileDecoration {
	public static final float kRuneGlowOpacityMin = 96f / 255f;
    public static final float kRuneGlowOpacityMax = 255f / 255f;
    public static final float kRuneGlowDuration = 1.25f;
    public static final int EV_TYPE_TELEPORT_OPACITY;
	
    @SuppressWarnings("unused")
	private static final float kRotateBy = 360f;
    @SuppressWarnings("unused")
	private static final float kScaleMax = 0.625f;
	
	static {
		EV_TYPE_TELEPORT_OPACITY = EventDispatcher.nextEvType();
	}
    
    private CMAtlasSprite rune;
    private CMAtlasSprite runeGlow;
    
    public TeleportDecoration() {
    	this(-1, 0);
    	PWDebug.tileDecorationCount++;
    }
	
	public TeleportDecoration(int category, int subType) {
		super(category, TilePiece.kTDKTeleport, subType);
		
		assert(subType >= 0 && subType <= 3) : "Invalid TeleportDecoration subType.";
		
		AtlasRegion runeRegion = scene.textureRegionByName("teleport-rune-" + subType);
		rune = new CMAtlasSprite(runeRegion);
		rune.centerContent();
		addSpriteChild(rune);
		
		setContentSize(rune.getWidth(), rune.getHeight());
		
		AtlasRegion glowRegion = scene.textureRegionByName("teleport-glow-" + subType);
		runeGlow = new CMAtlasSprite(glowRegion);
		runeGlow.centerContent();
		runeGlow.setColor(Utils.setA(runeGlow.getColor(), kRuneGlowOpacityMax));
		addSpriteChild(runeGlow);
		
		setAdvanceable(true);
	}
	
	@Override
	public void setSubType(int value) {
//		if (value == getSubType())
//			return;
		
		super.setSubType(value);
		
		rune.setAtlasRegion(scene.textureRegionByName("teleport-rune-" + value));
		rune.centerContent();
		runeGlow.setAtlasRegion(scene.textureRegionByName("teleport-glow-" + value));
		runeGlow.centerContent();
	}
	
	public float getRuneGlowOpacity() { return runeGlow.getColor().a; }
	
	public void setRuneGlowOpacity(float value) {
		runeGlow.setColor(Utils.setA(runeGlow.getColor(), value));
	} 
	
	@Override
	public void syncWithDecorator() {
		ITileDecorator decorator = getDecorator();
		if (decorator != null)
			setRuneGlowOpacity(decorator.decoratorValueForKey(getType()));
	}
	
	@Override
	public void enableMenuMode(boolean enable) {
		super.enableMenuMode(enable);
		setRuneGlowOpacity(kRuneGlowOpacityMax);
		
		if (enable)
			scene.removeFromJuggler(this);
		else
			scene.addToJuggler(this);
	}
	
	@Override
	public void syncWithTileDecoration(TileDecoration other) {
		super.syncWithTileDecoration(other);
		
		if (other.getType() == getType())
			setRuneGlowOpacity(((TeleportDecoration)other).getRuneGlowOpacity());
	}
	
	@Override
	public void valueDidChange(int evCode, float value) {
		if (evCode == EV_TYPE_TELEPORT_OPACITY)
			setRuneGlowOpacity(value);
	}
	
	@Override
	public void advanceTime(float dt) {
		super.advanceTime(dt);
		syncWithDecorator();
	}
}
