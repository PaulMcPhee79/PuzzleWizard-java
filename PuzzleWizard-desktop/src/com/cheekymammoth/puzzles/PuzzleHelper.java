package com.cheekymammoth.puzzles;

import com.badlogic.gdx.graphics.Color;
import com.cheekymammoth.puzzleFactories.TileFactory;
import com.cheekymammoth.puzzleViews.TileDecoration;
import com.cheekymammoth.puzzleViews.TilePiece;
import com.cheekymammoth.sceneControllers.SceneUtils.PFCat;

public class PuzzleHelper {
	private PuzzleHelper() { }
	
	// rgba8888 -> rrggbbaa
	
	// Normal Colors (100% brightness)
	@SuppressWarnings("unused")
    private static final int[] kNormalTileColors_100 = new int[]
    {
        // Red, Blue, Green, Yellow, White, Null
        0xd61906ff, 0x1d9fe8ff, 0x00e125ff, 0xfffd2fff, 0xffffffff, 0x808080ff
    };
	
	// Normal Colors (75% brightness)
	@SuppressWarnings("unused")
	private static final int[] kNormalTileColors_75 = new int[]
    {
        // Red, Blue, Green, Yellow, White, Null
        0xd62d1cff, 0x33a7e8ff, 0x16e138ff, 0xfffd40ff, 0xffffffff, 0x808080ff
    };
	
	// Normal Colors (50% brightness)
	private static final int[] kNormalTileColors_50 = new int[]
    {
        // Red, Blue, Green, Yellow, White, Null
        0xd64031ff, 0x4aafe8ff, 0x2de14bff, 0xfffd59ff, 0xf2f2f2ff, 0x808080ff
    };
	
	// Normal Colors (25% brightness)
	@SuppressWarnings("unused")
	private static final int[] kNormalTileColors_25 = new int[]
    {
        // Red, Blue, Green, Yellow, White, Null
        0xcb3e2fff, 0x409acdff, 0x25bf3fff, 0xd0ce48ff, 0xd5d1d1ff, 0x808080ff
    };
	
	// Normal Colors (25% brightness)
	@SuppressWarnings("unused")
	private static final int[] kNormalTileColors_10 = new int[]
    {
        // Red, Blue, Green, Yellow, White, Null
        0xcf584bff, 0x59a8d5ff, 0x3cd055ff, 0xd7d658ff, 0xd5d1d1ff, 0x808080ff
    };
	
	// NOTE: Change this to alter tile brightness
	private static final int[] kNormalTileColors = kNormalTileColors_50;

	@SuppressWarnings("unused")
    private static final int[] kNormalPlayerColors_100 = new int[]
    {
        // Red, Blue, Green, Yellow, White, Null
        0x960000ff, 0x005ba2ff, 0x008516ff, 0xc1a300ff, 0xcbcbcbff, 0x808080ff
    };
    
    private static final int[] kNormalPlayerColors_75 = new int[]
    {
        // Red, Blue, Green, Yellow, White, Null
        0xa60000ff, 0x006bbdff, 0x009418ff, 0xd2b200ff, 0xcbcbcbff, 0x808080ff
    };
    
    private static final int[] kNormalPlayerColors = kNormalPlayerColors_75;

    // Color Blind Colors
    private static final int[] kColorBlindTileColors = new int[]
    {
        // Red, Blue, Green, Yellow, White, Null
        0xff0000ff, 0x0066ccff, 0x00ccffff, 0xffcc00ff, 0xffffffff, 0x808080ff
    };

    private static final int[] kColorBlindPlayerColors = new int[]
    {
        // Red, Blue, Green, Yellow, White, Null
        0xcc0000ff, 0x003399ff, 0x0099ffff, 0xff9900ff, 0xcbcbcbff, 0x808080ff
    };

    private static final int[] kIQColors = new int[]
    {
        0x00ff00ff, 0xff8800ff, 0xff0000ff
    };

    public enum ColorScheme { NORMAL, COLOR_BLIND };
    public static String ColorScheme2String(ColorScheme value) {
    	switch (value) {
    		case NORMAL:
    			return "NORMAL";
    		case COLOR_BLIND:
    		default:
    			return "COLOR_BLIND";
    	}
    }
    private static ColorScheme s_ColorScheme = ColorScheme.NORMAL;

    private static int[] s_TileColors = kNormalTileColors;
    private static int[] s_PlayerColors = kNormalPlayerColors;

    public static final int kRedIndex = 0;
    public static final int kBlueIndex = 1;
    public static final int kGreenIndex = 2;
    public static final int kYellowIndex = 3;
    public static final int kWhiteIndex = 4;
    public static final int kNullIndex = 5;

    public static void setColorScheme(ColorScheme scheme) {
        if (scheme == s_ColorScheme)
            return;

        switch (scheme) {
            case NORMAL:
                s_TileColors = kNormalTileColors;
                s_PlayerColors = kNormalPlayerColors;
                break;
            case COLOR_BLIND:
                s_TileColors = kColorBlindTileColors;
                s_PlayerColors = kColorBlindPlayerColors;
                break;
        }

        s_ColorScheme = scheme;
    }

    public static ColorScheme getColorScheme() {
        return s_ColorScheme;
    }

    public static int tileColorForIndex(int index) {
        assert(index >= 0 && index < s_TileColors.length) : "PuzzleHelper::tileColorForIndex - index out of range.";
        return s_TileColors[index];
    }

    public static int playerColorForIndex(int index) {
        assert(index >= 0 && index < s_PlayerColors.length) : "PuzzleHelper::PlayerColorForIndex - index out of range.";
        return s_PlayerColors[index];
    }

    public static TileDecoration decorationForTile(Tile tile) {
        int type = tile.getDecorationKey(), subType = 0;

        switch (type) {
            case TilePiece.kTDKTeleport:
                subType = tile.getDecorationStyleKey() >>> Tile.kBitShiftDecorationStyle;
                break;
            case TilePiece.kTDKColorSwap:
                subType = tile.getDecorationStyleKey() >>> Tile.kBitShiftDecorationStyle;
                break;
            case TilePiece.kTDKRotate:
                break;
            case TilePiece.kTDKShield:
                break;
            case TilePiece.kTDKPainter:
                subType = tile.getPainter();
                break;
            case TilePiece.kTDKTileSwap:
                subType = tile.getDecorationStyleKey() >>> Tile.kBitShiftDecorationStyle;
                break;
            case TilePiece.kTDKMirrorImage:
                break;
            case TilePiece.kTDKKey:
                break;
            case TilePiece.kTDKColorFlood:
                subType = tile.getPainter();
                break;
            case TilePiece.kTDKColorSwirl:
                break;
            case TilePiece.kTDKConveyorBelt:
                subType = tile.getDecorationStyleKey() >>> Tile.kBitShiftDecorationStyle;
                break;
            case TilePiece.kTDKColorMagic:
                break;
            default:
                return null;
        }

        return TileFactory.getTileDecoration(PFCat.BOARD.ordinal(), type, subType);
    }

    public static int colorForKey(int key) {
        int color;

        switch (key) {
            case 1: color = tileColorForIndex(kRedIndex); break;
            case 2: color = tileColorForIndex(kBlueIndex); break;
            case 3: color = tileColorForIndex(kGreenIndex); break;
            case 4: color = tileColorForIndex(kYellowIndex); break;
            case 5: color = tileColorForIndex(kWhiteIndex); break;
            default: color = tileColorForIndex(kNullIndex); break;
        }

        return color;
    }

    public static int playerColorForKey(int key) {
        //return kPlayerWhiteColor;
        int color;
        
        switch (key) {
            case 1: color = playerColorForIndex(kRedIndex); break;
            case 2: color = playerColorForIndex(kBlueIndex); break;
            case 3: color = playerColorForIndex(kGreenIndex); break;
            case 4: color = playerColorForIndex(kYellowIndex); break;
            case 5: color = playerColorForIndex(kWhiteIndex); break;
            default: color = playerColorForIndex(kNullIndex); break;
        }

        return color;
    }

    public static void setColorSwapColorsForTile(int decorationStyleKey, Color colorLeft, Color colorRight) {
        // rb,rg,ry,rw,bg,by,bw,gy,gw,yw
        switch (decorationStyleKey) {
            case 0:
            	Color.rgba8888ToColor(colorLeft, tileColorForIndex(kRedIndex));
            	Color.rgba8888ToColor(colorRight, tileColorForIndex(kBlueIndex));
            	break;
            case 1:
            	Color.rgba8888ToColor(colorLeft, tileColorForIndex(kRedIndex));
            	Color.rgba8888ToColor(colorRight, tileColorForIndex(kGreenIndex));
            	break;
            case 2:
            	Color.rgba8888ToColor(colorLeft, tileColorForIndex(kRedIndex));
            	Color.rgba8888ToColor(colorRight, tileColorForIndex(kYellowIndex));
            	break;
            case 3:
            	Color.rgba8888ToColor(colorLeft, tileColorForIndex(kRedIndex));
            	Color.rgba8888ToColor(colorRight, tileColorForIndex(kWhiteIndex));
            	break;
            case 4:
            	Color.rgba8888ToColor(colorLeft, tileColorForIndex(kBlueIndex));
            	Color.rgba8888ToColor(colorRight, tileColorForIndex(kGreenIndex));
            	break;
            case 5:
            	Color.rgba8888ToColor(colorLeft, tileColorForIndex(kBlueIndex));
            	Color.rgba8888ToColor(colorRight, tileColorForIndex(kYellowIndex));
            	break;
            case 6:
            	Color.rgba8888ToColor(colorLeft, tileColorForIndex(kBlueIndex));
            	Color.rgba8888ToColor(colorRight, tileColorForIndex(kWhiteIndex));
            	break;
            case 7:
            	Color.rgba8888ToColor(colorLeft, tileColorForIndex(kGreenIndex));
            	Color.rgba8888ToColor(colorRight, tileColorForIndex(kYellowIndex));
            	break;
            case 8:
            	Color.rgba8888ToColor(colorLeft, tileColorForIndex(kGreenIndex));
            	Color.rgba8888ToColor(colorRight, tileColorForIndex(kWhiteIndex));
            	break;
            case 9:
            	Color.rgba8888ToColor(colorLeft, tileColorForIndex(kYellowIndex));
            	Color.rgba8888ToColor(colorRight, tileColorForIndex(kWhiteIndex));
            	break;
            default:
            	Color.rgba8888ToColor(colorLeft, 0);
            	Color.rgba8888ToColor(colorRight, 0);
            	break;
        }
    }

    public static int tileFunctionForDecorationKey(int decorationKey) {
        int tileFunction = Tile.kTFNone;

        switch (decorationKey) {
            case TilePiece.kTDKTeleport: tileFunction = Tile.kTFTeleport; break;
            case TilePiece.kTDKColorSwap: tileFunction = Tile.kTFColorSwap; break;
            case TilePiece.kTDKRotate: tileFunction = Tile.kTFRotate; break;
            case TilePiece.kTDKShield: tileFunction = Tile.kTFShield; break;
            case TilePiece.kTDKPainter: tileFunction = Tile.kTFPainter; break;
            case TilePiece.kTDKTileSwap: tileFunction = Tile.kTFTileSwap; break;
            case TilePiece.kTDKMirrorImage: tileFunction = Tile.kTFMirroredImage; break;
            case TilePiece.kTDKKey: tileFunction = Tile.kTFKey; break;
            case TilePiece.kTDKColorFlood: tileFunction = Tile.kTFColorFlood; break;
            case TilePiece.kTDKColorSwirl: tileFunction = Tile.kTFColorSwirl; break;
            case TilePiece.kTDKConveyorBelt: tileFunction = Tile.kTFConveyorBelt; break;
            case TilePiece.kTDKColorMagic: tileFunction = Tile.kTFColorMagic; break;
            default: tileFunction = Tile.kTFNone; break;
        }

        return tileFunction;
    }

    public static int colorForIQ(int IQ)
    {
        if (IQ <= 99)
            return kIQColors[0];
        else if (IQ <= 125)
            return kIQColors[1];
        else
            return kIQColors[2];
    }
}
