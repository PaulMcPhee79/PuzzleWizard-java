package com.cheekymammoth.puzzles;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.cheekymammoth.puzzleFactories.TileFactory;
import com.cheekymammoth.puzzleViews.ITileView;
import com.cheekymammoth.utils.PWDebug;

public class Tile implements Poolable {
    // Tile Functions
    public static final int kTFNone = 0;
    public static final int kTFTeleport = 1 << 16;
    public static final int kTFColorSwap = 2 << 16;
    public static final int kTFRotate = 3 << 16;
    public static final int kTFShield = 4 << 16;
    public static final int kTFPainter = 5 << 16;
    public static final int kTFTileSwap = 6 << 16;
    public static final int kTFMirroredImage = 7 << 16;
    public static final int kTFKey = 8 << 16;
    public static final int kTFColorFlood = 9 << 16;
    public static final int kTFColorSwirl = 10 << 16;
    public static final int kTFConveyorBelt = 11 << 16;
    public static final int kTFColorMagic = 12 << 16;

    // Property masks
    public static final int kColorKeyMask = 0xf;
    public static final int kDecorationKeyMask = 0xff0;
    public static final int kDecorationStyleKeyMask = 0xf000;
    public static final int kFunctionKeyMask = 0xff0000;
    public static final int kFunctionIDMask = 0xff000000;

    // Bit shifts
    public static final int kBitShiftColor = 0;
    public static final int kBitShiftDecoration = 4;
    public static final int kBitShiftDecorationStyle = 12;
    public static final int kBitShiftFunction = 16;
    public static final int kBitShiftFunctionID = 24;

    // Extended Modifiers (can be set alongside Tile Function modifiers)
    public static final int kTMShielded = 1 << 0;

    // ----------------------------------------------
    
    // Property change codes
    public static final int kPropAll = 0;
    public static final int kPropCodeIsEdge = 1;
	
	private boolean isEdgeTile;
    private int decorator;
    private int decoratorData;
    private int GUID = 1;

    // [0-3] Color, [4-11] Decoration, [12-15] Decoration Style [16-23] Function, [24-31] Function ID
    private int properties;
    private int modifiers;
    // [0-3] North color, [4-7] East color, [8-11] South color, [12-15] West color
    private int painter;

    private int devProperties;
    private int devPainter;

    private Array<ITileView> views;
	
	public Tile() { PWDebug.tileCount++; }
	
//	public Tile(BufferedInputStream stream) throws IOException {
//		decodeFromStream(stream);
//	}

	public boolean isEdgeTile() { return isEdgeTile; }
	
	public void setEdgeTile(boolean value) {
		if (isEdgeTile != value) {
			isEdgeTile = value;
			notifyPropertyChange(kPropCodeIsEdge);
		}
	}
	
	public int getColorKey() { return properties & kColorKeyMask; }
	
	public void setColorKey(int value) {
		setProperties((properties & ~kColorKeyMask) | (value & kColorKeyMask));
	}
	
	public int getDecorationKey() { return properties & kDecorationKeyMask; }
	
	public void setDecorationKey(int value) {
		setProperties((properties & ~kDecorationKeyMask) | (value & kDecorationKeyMask));
	}

	public int getDecorationStyleKey() { return properties & kDecorationStyleKeyMask; }
	
	public void setDecorationStyleKey(int value) {
		setProperties((properties & ~kDecorationStyleKeyMask) | (value & kDecorationStyleKeyMask));
	}

	public int getFunctionKey() { return properties & kFunctionKeyMask; }
	
	public void setFunctionKey(int value) {
		setProperties((properties & ~kFunctionKeyMask) | (value & kFunctionKeyMask));
	}
	
	public int getFunctionID() { return properties & kFunctionIDMask; }
	
	public void setFunctionID(int value) {
		setProperties((properties & ~kFunctionIDMask) | (value & kFunctionIDMask));
	}
	
	public int getDevColorKey() { return devProperties & kColorKeyMask; }
	
	public void setDevColorKey(int value) {
		setProperties((devProperties & ~kColorKeyMask) | (value & kColorKeyMask));
	}
	
	public int getDevDecorationKey() { return devProperties & kDecorationKeyMask; }
	
	public void setDevDecorationKey(int value) {
		setProperties((devProperties & ~kDecorationKeyMask) | (value & kDecorationKeyMask));
	}

	public int getDevDecorationStyleKey() { return devProperties & kDecorationStyleKeyMask; }
	
	public void setDevDecorationStyleKey(int value) {
		setProperties((devProperties & ~kDecorationStyleKeyMask) | (value & kDecorationStyleKeyMask));
	}

	public int getDevFunctionKey() { return devProperties & kFunctionKeyMask; }
	
	public void setDevFunctionKey(int value) {
		setProperties((devProperties & ~kFunctionKeyMask) | (value & kFunctionKeyMask));
	}
	
	public int getDevFunctionID() { return devProperties & kFunctionIDMask; }
	
	public void setDevFunctionID(int value) {
		setProperties((devProperties & ~kFunctionIDMask) | (value & kFunctionIDMask));
	}

	public int getDecorator() { return decorator; }
	
	public void setDecorator(int value) { decorator = value; }
	
	public int getDecoratorData() { return decoratorData; }
	
	public void setDecoratorData(int value) { decoratorData = value; }
	
	public int getGUID() { return GUID; }
	
	public void setGUID(int value) { GUID = value; }
	
	public int getProperties() { return properties; }
	
	void setProperties(int value) {
		properties = value;
		notifyPropertyChange(kPropAll);
		setDecorator(0);
		setDecoratorData(0);
	}
	
	public int getModifiers() { return modifiers; }
	
	public void setModifiers(int value) {
		modifiers = value;
		notifyModiferChange();
	}
	
	public int getPainter() { return painter; }
	
	public void setPainter(int value) {
		painter = value;
		notifyModiferChange();
	}
	
	public int getDevProperties() { return devProperties; }
	
	void setDevProperties(int value) {
		devProperties = value;
		setProperties(value);
	}
	
	public int getDevPainter() { return devPainter; }
	
	public void setDevPainter(int value) {
		devPainter = value;
		setPainter(value);
	}
	
	public void registerView(ITileView view) {
		if (view == null)
			return;
		
		if (views == null)
			views = new Array<ITileView>(true, 1, ITileView.class);
		if (!views.contains(view, true))
			views.add(view);
	}
	
	public void deregisterView(ITileView view) {
		if (view == null || views == null)
			return;
		
		views.removeValue(view, true);
	}
	
	private void notifyPropertyChange(int code) {
		if (views == null)
			return;
		
		for (int i = views.size-1; i >= 0; i--)
			views.get(i).tilePropertiesDidChange(code);
	}

	private void notifyModiferChange() {
		if (views == null)
			return;
		
		for (int i = views.size-1; i >= 0; i--)
			views.get(i).tileModifiersDidChange();
	}
	
	public boolean isModified(int modifier) {
		return (modifiers & modifier) == modifier;
	}
	
	public void modifyPainter(int painter) {
		for (int i = 0; i < 4; i++) {
			int mask = 0xf << (i * 4);
			if ((painter & mask) != 0) {
				this.painter &= ~mask;
				this.painter |= painter & mask;
			}
		}
		
		setPainter(this.painter);
	}
	
	public void devModifyPainter(int painter)
    {
        for (int i = 0; i < 4; ++i)
        {
            int mask = 0xf << (i * 4);
            if ((painter & mask) != 0)
            {
                this.devPainter &= ~mask;
                this.devPainter |= painter & mask;
            }
        }

        setDevPainter(this.devPainter);
    }
	
	public void clear() {
		setProperties(0);
		setModifiers(0);
		setPainter(0);
	}
	
	public void copy(Tile tile) {
		setProperties(tile.getProperties());
		setModifiers(tile.getModifiers());
		setPainter(tile.getPainter());
	}
	
	public void devCopy(Tile tile) {
		setDevProperties(tile.getDevProperties());
		setModifiers(tile.getModifiers());
		setDevPainter(tile.getDevPainter());
	}
	
	@Override
	public void reset() {
		setProperties(getDevProperties());
		setModifiers(0);
		setPainter(getDevPainter());
	}
	
	// Returns pooled Tile. Client responsible for repooling.
	public Tile clone() {
		Tile tile = TileFactory.getTile();
        tile.setProperties(getProperties());
        tile.setModifiers(getModifiers());
        tile.setPainter(getPainter());
        return tile;
	}
	
	// Returns pooled Tile. Client responsible for repooling.
	public Tile devClone() {
		Tile tile = TileFactory.getTile();
        tile.setDevProperties(getDevProperties());
        tile.setModifiers(getModifiers());
        tile.setDevPainter(getDevPainter());
        return tile;
	}

	protected void decodeFromStream(BufferedInputStream stream) throws IOException {
		byte[] arr = new byte[8];
		stream.read(arr, 0, arr.length);

		ByteBuffer buffer = ByteBuffer.wrap(arr);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		setDevProperties(buffer.getInt());
		setDevPainter(buffer.getInt());
	}
}
