package com.cheekymammoth.assets;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entries;
import com.badlogic.gdx.utils.ObjectMap.Entry;

public class AudioManager extends ResourceManager {
	private String musicName;
	private Music music;
	private ObjectMap<String, Sound> soundCache;
	
	public AudioManager() {
		this(20);
	}
	
	public AudioManager(int soundCapacity) {
		super(soundCapacity);
		soundCache = new ObjectMap<String, Sound>(Math.max(1, soundCapacity));
	}
	
	public void loadSound(String filename, boolean async) {
		AssetManager am = AssetServer.getAssetManager();
		if (!am.isLoaded(filename, Sound.class)) {
			loadQueue.put(filename, Sound.class);
			am.load(filename, Sound.class);
			
			if (async)
				isLoading = true;
			else {
				am.finishLoading();
				pumpLoadQueue();
				isLoading = false;
			}
		}
	}
	
	public void unloadSound(String filename) {
		soundCache.remove(filename);
		
		AssetManager am = AssetServer.getAssetManager();
		if (am.isLoaded(filename, Sound.class))
			am.unload(filename);
	}
	
	public void loadMusic(String filename) {
		if (filename != null && musicName != null && filename.equals(musicName))
			return;
		
		unloadMusic();
		
		AssetManager am = AssetServer.getAssetManager();
		if (!am.isLoaded(filename, Music.class)) {
			Gdx.app.log("AudioManager", "loading music " + filename);
			loadQueue.put(filename, Music.class);
			am.load(filename, Music.class);
			am.finishLoading();
			pumpLoadQueue();
			isLoading = false;
		}
	}
	
	public void unloadMusic() {
		if (music != null) {
			music.stop();
			music = null;
		}
		
		if (musicName != null) {
			AssetManager am = AssetServer.getAssetManager();
			if (am.isLoaded(musicName, Music.class))
				am.unload(musicName);
			musicName = null;
		}
	}
	
	private void prepareCache(String name, Sound sound) {
		soundCache.put(name, sound);
	}
	
	private void prepareCache(String name, Music music) {
		musicName = name;
		this.music = music;
		Gdx.app.log("AudioManager", "loaded music " + name);
	}
	
	protected void pumpLoadQueue() {
		Entries<String, Class<?>> loadQEntries = loadQueue.entries();
		Iterator<Entry<String, Class<?>>> loadIt = loadQEntries.iterator();
		AssetManager am = AssetServer.getAssetManager();
		while (loadIt.hasNext()) {
			Entry<String, Class<?>> entry = loadIt.next();
			if (am.isLoaded(entry.key, entry.value)) {
				if (entry.value.equals(Sound.class))
					prepareCache(entry.key, am.get(entry.key, Sound.class));
				else if (entry.value.equals(Music.class))
					prepareCache(entry.key, am.get(entry.key, Music.class));
				loadDequeue.add(entry.key);
			}
		}
		
		super.pumpLoadQueue();
	}

	public Sound soundByName(String name) {
		return name != null ? soundCache.get(name) : null;
	}
	
	public Music getMusic() {
		return music;
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}
}
