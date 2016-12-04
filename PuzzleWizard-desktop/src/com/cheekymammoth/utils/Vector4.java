package com.cheekymammoth.utils;

import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.Pool.Poolable;

public class Vector4 implements Poolable {
	public float x, y, z, w;
	
	public Vector4() {
		this(0, 0, 0, 0);
	}
	
	public Vector4(Vector4 v) {
		this(v.x, v.y, v.z, v.w);
	}
	
	public Vector4(float x, float y, float z, float w) {
		set(x, y, z, w);
	}
	
	public void set(Vector4 v) {
		set(v.x, v.y, v.z, v.w);
	}
	
	public void set(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	@Override
	public void reset() {
		x = 0;
		y = 0;
		z = 0;
		w = 0;
	}
	
	public static Vector4 obtainVector4() {
		return obtainVector4(0, 0, 0 ,0);
	}
	
	public static Vector4 obtainVector4(Vector4 v) {
		return obtainVector4(v.x, v.y, v.z, v.w);
	}
	
	public static Vector4 obtainVector4(float x, float y, float z, float w) {
		Vector4 v = Pools.obtain(Vector4.class);
		v.set(x, y, z, w);
		return v;
	}
	
	public static void freeVector4(Vector4 v) {
		if (v != null)
			Pools.free(v);
	}
}
