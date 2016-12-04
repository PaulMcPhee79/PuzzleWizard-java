package com.cheekymammoth.utils;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public final class Jukebox {
	private int currentSongIndex;
	private int nextSongIndex;
	private Array<String> songs;
	
	public Jukebox() {
		this(null);
	}
	
	public Jukebox(String[] songs) {
		currentSongIndex = nextSongIndex = 0;
		this.songs = new Array<String>(true, songs != null ? songs.length : 5, String.class);
		if (songs != null)
			this.songs.addAll(songs);
	}
	
	public void randomize() {
		if (songs.size < 2)
			return;
		
		for (int i = 0, n = songs.size; i < n; i++) {
			int rand1 = MathUtils.random(n-1);
			int rand2 = MathUtils.random(n-1);
			songs.swap(rand1, rand2);
		}
	}
	
	public boolean containsSong(String name) {
		return songs.contains(name, false);
	}
	
	public void addSong(String name) {
		songs.add(name);
	}
	
	public void removeSong(String name) {
		songs.removeValue(name, false);
	}
	
	public void clear() {
		songs.clear();
	}
	
	public String getCurrentSong() {
		if (songs.size == 0)
			return null;
		else
			return songs.get(currentSongIndex);
	}
	
	public String prevSong() {
		if (songs.size == 0)
			return null;
		else {
			nextSongIndex = currentSongIndex > 0 ? currentSongIndex-1 : songs.size-1;
			currentSongIndex = nextSongIndex;
			return songs.get(nextSongIndex++);
		}
	}
	
	public String nextSong() {
		if (songs.size == 0)
			return null;
		else {
			if (nextSongIndex >= songs.size)
				nextSongIndex = nextSongIndex % songs.size;
			currentSongIndex = nextSongIndex;
			return songs.get(nextSongIndex++);
		}
	}
}
