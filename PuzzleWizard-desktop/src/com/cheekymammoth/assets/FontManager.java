package com.cheekymammoth.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entries;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.ObjectMap.Keys;

public class FontManager extends ResourceManager {
	private boolean delayFontFileLoading;
	private ObjectMap<String, Texture> texCache; // <fntFileName, texture>
	private ObjectMap<String, BitmapFont> fontCache; // <fntFileName, font>
	private ObjectMap<String, String> nameCache; // <fntFileName, texFileName>
	private ObjectMap<String, String> loadingCache; // <texFileName, fntFileName>
	
	public FontManager() {
		this(5);
	}

	public FontManager(int capacity) {
		super(capacity);
		capacity = Math.max(1, capacity);
		texCache = new ObjectMap<String, Texture>();
		fontCache = new ObjectMap<String, BitmapFont>();
		nameCache = new ObjectMap<String, String>();
		loadingCache = new ObjectMap<String, String>();
	}
	
	public boolean isDelayFontFileLoading() { return delayFontFileLoading; }
	
	public void setDelayFontFileLoading(boolean value) { delayFontFileLoading = value; }
	
	public void loadDelayedFontFiles() {
		Keys<String> fntFileNameKeys = nameCache.keys();
		while (fntFileNameKeys.hasNext()) {
			String fntFileName = fntFileNameKeys.next();
			Texture texture = texCache.get(fntFileName);
			assert(texture != null) : "FontManager - invalid state for font: " + fntFileName;
			
			if (fontCache.get(fntFileName) == null) {
				BitmapFont font = new BitmapFont(Gdx.files.internal("font/" + fntFileName), new TextureRegion(texture));
				fontCache.put(fntFileName, font);
			}
		}
	}
	
	public void loadFont(String fntFileName, String texFileName, boolean async) {
		loadFont(fntFileName, texFileName, null, async);
	}
	
	public void loadFont(String fntFileName, String texFileName, TextureParameter param, boolean async) {
		AssetManager am = AssetServer.getAssetManager();
		String fullTexFileName = "font/" + texFileName;
		if (!am.isLoaded(fullTexFileName, Texture.class)) {
			loadQueue.put(fullTexFileName, Texture.class);
			am.load(fullTexFileName, Texture.class, param);
			loadingCache.put(fullTexFileName, fntFileName);
			
			if (async)
				isLoading = true;
			else {
				setDelayFontFileLoading(false);
				am.finishLoading();
				pumpLoadQueue();
				isLoading = false;
			}
		}
	}

	public void unloadFont(String filename) {
		if (filename == null)
			return;
		BitmapFont font = fontCache.get(filename);
		if (font != null) {
			fontCache.remove(filename);
			font.dispose();
			texCache.remove(filename);
			
			String texName = nameCache.get(filename);
			nameCache.remove(filename);
			AssetManager am = AssetServer.getAssetManager();
			if (am.isLoaded(texName, Texture.class))
				am.unload(texName);
		}
	}
	
	private void prepareCache(String texFileName, Texture texture) {
		String fntFileName = loadingCache.get(texFileName);
		assert(fntFileName != null) : "FontManager - invalid state for font: " + texFileName;
		
		if (!delayFontFileLoading) {
			BitmapFont font = new BitmapFont(Gdx.files.internal("font/" + fntFileName), new TextureRegion(texture));
			fontCache.put(fntFileName, font);
		}
		
		texCache.put(fntFileName, texture);
		nameCache.put(fntFileName, texFileName);
		loadingCache.remove(texFileName);
	}
	
	@Override
	protected void pumpLoadQueue() {
		Entries<String, Class<?>> loadQEntries = loadQueue.entries();
		AssetManager am = AssetServer.getAssetManager();
		while (loadQEntries.hasNext()) {
			Entry<String, Class<?>> entry = loadQEntries.next();
			if (am.isLoaded(entry.key, entry.value)) {
				if (entry.value.equals(Texture.class))
					prepareCache(entry.key, am.get(entry.key, Texture.class));
				loadDequeue.add(entry.key);
			}
		}
		
		super.pumpLoadQueue();
	}
	
	public BitmapFont fontByName(String name) {
		return fontCache.get(name, null);
	}
	
	@Override
	public void dispose() {
		Keys<String> fontKeys = fontCache.keys();
		
		while (fontKeys.hasNext()) {
			String fontKey = fontKeys.next();
			BitmapFont font = fontCache.get(fontKey);
			if (font != null)
				font.dispose();
		}
		fontCache.clear();
		
		AssetManager am = AssetServer.getAssetManager();
		Keys<String> fntFileNameKeys = nameCache.keys();
		
		while (fntFileNameKeys.hasNext()) {
			String fntFileName = fntFileNameKeys.next();
			texCache.remove(fntFileName);
			String texName = nameCache.get(fntFileName);
			if (am.isLoaded(texName, Texture.class))
				am.unload(texName);
		}
		nameCache.clear();
		
		super.dispose();
	}
}
