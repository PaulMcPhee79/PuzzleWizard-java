package com.cheekymammoth.graphics;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

public class CMAtlasSprite extends CMSprite {
	protected boolean isVisible = true;
	protected AtlasRegion region;
    protected float originalOffsetX, originalOffsetY;

    public CMAtlasSprite (AtlasRegion region) {
        this.region = new AtlasRegion(region);
        originalOffsetX = region.offsetX;
        originalOffsetY = region.offsetY;
        setRegion(region);
        setOrigin(region.originalWidth / 2f, region.originalHeight / 2f);
        int width = region.getRegionWidth();
        int height = region.getRegionHeight();
        if (region.rotate) {
            super.rotate90(true);
            super.setBounds(region.offsetX, region.offsetY, height, width);
        } else
            super.setBounds(region.offsetX, region.offsetY, width, height);
        setColor(1, 1, 1, 1);
    }
    
    public void setAtlasRegion(AtlasRegion region) {
    	AtlasRegion oldRegion = this.region;
    	this.region = region;
        originalOffsetX = region.offsetX;
        originalOffsetY = region.offsetY;
        setRegion(region);
        setOrigin(region.originalWidth / 2f, region.originalHeight / 2f);
        int width = region.getRegionWidth();
        int height = region.getRegionHeight();
        
        if (!region.rotate && oldRegion.rotate) {
        	// Undo previous rotation
        	super.rotate90(true);
        	super.rotate90(true);
        	super.rotate90(true);
        }
        
        if (region.rotate) {
        	super.rotate90(true);
        	super.setBounds(region.offsetX, region.offsetY, height, width);
        } else
        	super.setBounds(region.offsetX, region.offsetY, width, height);
    }

    public CMAtlasSprite (CMAtlasSprite sprite) {
        region = sprite.region;
        this.originalOffsetX = sprite.originalOffsetX;
        this.originalOffsetY = sprite.originalOffsetY;
        set(sprite);
    }

    public void setPosition (float x, float y) {
        super.setPosition(x + region.offsetX, y + region.offsetY);
    }

    public void setX (float x) {
        super.setX(x + region.offsetX);
    }

    public void setY (float y) {
        super.setY(y + region.offsetY);
    }

    public void setBounds (float x, float y, float width, float height) {
        float widthRatio = width / region.originalWidth;
        float heightRatio = height / region.originalHeight;
        region.offsetX = originalOffsetX * widthRatio;
        region.offsetY = originalOffsetY * heightRatio;
        int packedWidth = region.rotate ? region.packedHeight : region.packedWidth;
        int packedHeight = region.rotate ? region.packedWidth : region.packedHeight;
        super.setBounds(x + region.offsetX, y + region.offsetY, packedWidth * widthRatio, packedHeight * heightRatio);
    }

    public void setSize (float width, float height) {
        setBounds(getX(), getY(), width, height);
    }

    public void setOrigin (float originX, float originY) {
        super.setOrigin(originX - region.offsetX, originY - region.offsetY);
    }

    public void flip (boolean x, boolean y) {
        // Flip texture.
        super.flip(x, y);

        float oldOriginX = getOriginX();
        float oldOriginY = getOriginY();
        float oldOffsetX = region.offsetX;
        float oldOffsetY = region.offsetY;

        float widthRatio = getWidthRatio();
        float heightRatio = getHeightRatio();

        region.offsetX = originalOffsetX;
        region.offsetY = originalOffsetY;
        region.flip(x, y); // Updates x and y offsets.
        originalOffsetX = region.offsetX;
        originalOffsetY = region.offsetY;
        region.offsetX *= widthRatio;
        region.offsetY *= heightRatio;

        // Update position and origin with new offsets.
        translate(region.offsetX - oldOffsetX, region.offsetY - oldOffsetY);
        setOrigin(oldOriginX, oldOriginY);
    }

    public void rotate90 (boolean clockwise) {
        // Rotate texture.
        super.rotate90(clockwise);

        float oldOriginX = getOriginX();
        float oldOriginY = getOriginY();
        float oldOffsetX = region.offsetX;
        float oldOffsetY = region.offsetY;

        float widthRatio = getWidthRatio();
        float heightRatio = getHeightRatio();

        if (clockwise) {
                region.offsetX = oldOffsetY;
                region.offsetY = region.originalHeight * heightRatio - oldOffsetX - region.packedWidth * widthRatio;
        } else {
                region.offsetX = region.originalWidth * widthRatio - oldOffsetY - region.packedHeight * heightRatio;
                region.offsetY = oldOffsetX;
        }

        // Update position and origin with new offsets.
        translate(region.offsetX - oldOffsetX, region.offsetY - oldOffsetY);
        setOrigin(oldOriginX, oldOriginY);
    }

    public float getX () {
        return super.getX() - region.offsetX;
    }

    public float getY () {
        return super.getY() - region.offsetY;
    }

    public float getOriginX () {
        return super.getOriginX() + region.offsetX;
    }

    public float getOriginY () {
        return super.getOriginY() + region.offsetY;
    }

    public float getWidth () {
        return super.getWidth() / region.getRotatedPackedWidth() * region.originalWidth;
    }

    public float getHeight () {
        return super.getHeight() / region.getRotatedPackedHeight() * region.originalHeight;
    }

    public float getWidthRatio () {
        return super.getWidth() / region.getRotatedPackedWidth();
    }

    public float getHeightRatio () {
        return super.getHeight() / region.getRotatedPackedHeight();
    }

    public AtlasRegion getAtlasRegion () {
        return region;
    }
}
