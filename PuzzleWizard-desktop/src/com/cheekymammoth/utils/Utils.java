package com.cheekymammoth.utils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.cheekymammoth.events.EventDispatcher;

public final class Utils {
	public static final String kShaderCoordAttrName = "a_shaderCoords";
	
	private Utils() { }
	
	public static int getUniqueKey() { return EventDispatcher.nextEvType(); }
	
	public static void moveInCircle(float gameTime, float speed, Vector2 v) {
		float time = gameTime * speed;
		v.x = MathUtils.cos(time);
		v.y = MathUtils.sin(time);
	} 
	
	public static String trimExtension(String filename) {
		return filename.substring(0, filename.lastIndexOf('.'));
	}
	
	public static Color setR(Color color, float r) {
		color.set(r, color.g, color.b, color.a);
		return color;
	}
	
	public static Color setG(Color color, float g) {
		color.set(color.r, g, color.b, color.a);
		return color;
	}
	
	public static Color setB(Color color, float b) {
		color.set(color.r, color.g, b, color.a);
		return color;
	}
	
	public static Color setA(Color color, float a) {
		color.set(color.r, color.g, color.b, a);
		return color;
	}
	
	public static Color setRGBA(Color color, float rgba) {
		color.set(rgba, rgba, rgba, rgba);
		return color;
	}
	
	public static Color setRGB(Color color, float rgb) {
		color.set(rgb, rgb, rgb, color.a);
		return color;
	}
	
	public static Color setRGBA(Color color, int rgba) {
		color.set(rgba);
		return color;
	}
	
	public static Color setRGB(Color color, int rgba) {
		float oldA = color.a;
		color.set(rgba);
		color.a = oldA;
		return color;
	}
	
	public static Color premultiplyAlpha(Color color) {
		color.r *= color.a;
		color.g *= color.a;
		color.b *= color.a;
		return color;
	}
	
	public static void initShaderCoordBuffer(ByteBuffer byteBuffer, int bufferLen) {
		final FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
		//floatBuffer.put(new float[] { 0, 0, 0, 1, 1, 1, 1, 0 });
		floatBuffer.put(new float[] { 0, 1, 0, 0, 1, 0, 1, 1 });
	}
	
	// Returns dest set to be the intersection of src and dest.
	public static Rectangle intersectionRect(Rectangle src, Rectangle dest) {
		float left = Math.max(src.x, dest.x);
        float right = Math.min(src.x + src.width, dest.x + dest.width);
        float top = Math.max(src.y, dest.y);
        float bottom = Math.min(src.y + src.height, dest.y + dest.height);

        if (left > right || top > bottom)
        	return dest.set(0, 0, 0, 0);
        else
        	return dest.set(left, top, right - left, bottom - top);
	}
	
	// Returns dest set to be the union of src and dest.
	public static Rectangle unionRect(Rectangle src, Rectangle dest) {
		 float left = Math.min(src.x, dest.x);
         float right = Math.max(src.x + src.width, dest.x + dest.width);
         float top = Math.min(src.y, dest.y);
         float bottom = Math.max(src.y + src.height, dest.y + dest.height);
         dest.set(left, top, right - left, bottom - top);
         return dest;
	}
	
	public static byte[] stripTrailingBytes(byte strip, byte[] arr) {
		byte[] stripped = null;
		
		if (arr != null) {
			int len = 0;
			for (int i = 0, n = arr.length; i < n; i++) {
				if (arr[i] != strip)
					len++;
			}
			
			if (len > 0) {
				stripped = new byte[len];
				System.arraycopy(arr, 0, stripped, 0, len);
			}
		}
		
		return stripped;
	}
}
