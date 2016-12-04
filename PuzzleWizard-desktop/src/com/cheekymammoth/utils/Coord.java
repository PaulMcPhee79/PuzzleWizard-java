package com.cheekymammoth.utils;

import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;

public class Coord implements Poolable {
	public int x;
	public int y;
	
	public Coord() { }
	
	public Coord(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Coord set(int x, int y) {
		this.x = x;
		this.y = y;
		return this;
	}
	
	public Coord set(Coord other) {
		if (other != null)
			return set(other.x, other.y);
		else
			return set(0, 0);
	}
	
	public boolean isEquivalent(Coord other) {
		return other != null && x == other.x && y == other.y;
	}

	@Override
	public void reset() {
		x = 0;
		y = 0;
	}
	
	public boolean isOrigin() {
		return x == 0 && y == 0;
	}
	
	public static Coord obtainCoord() {
		return obtainCoord(0, 0);
	}
	
	public static Coord obtainCoord(Coord c) {
		return obtainCoord(c.x, c.y);
	}
	
	public static Coord obtainCoord(int x, int y) {
		Coord c = Pools.obtain(Coord.class);
		c.set(x, y);
		return c;
	}
	
	public static void freeCoord(Coord c) {
		if (c != null)
			Pools.free(c);
	}
}
