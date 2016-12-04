package com.cheekymammoth.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entries;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.ObjectMap.Keys;

public class TextureManager extends ResourceManager {
	private static final int TEX_CACHE_CAPACITY = 30;
	private static final int ANIM_CACHE_CAPACITY = 10;
	// <AtlasName, Atlas>
	private ObjectMap<String, TextureAtlas> atlasCache;
	// <AtlasName, <TexName, Region>>
	private ObjectMap<String, ObjectMap<String, AtlasRegion>> regionCache;
	// <AtlasName, <TexName, Region>>
	private ObjectMap<String, ObjectMap<String, Array<AtlasRegion>>> animCache;
	// Non-atlas texture cache
	private ObjectMap<String, Texture> texCache;
	
	public TextureManager() {
		this(5, 10);
	}
	
	public TextureManager(int atlasCapacity, int textureCapacity) {
		super(Math.max(atlasCapacity, textureCapacity));
		int capacity = Math.max(1, atlasCapacity);
		atlasCache = new ObjectMap<String, TextureAtlas>(capacity);
		regionCache = new ObjectMap<String, ObjectMap<String, AtlasRegion>>(capacity);
		animCache = new ObjectMap<String, ObjectMap<String, Array<AtlasRegion>>>(capacity);
		
		capacity = Math.max(1, textureCapacity);
		texCache = new ObjectMap<String, Texture>(capacity);
	}
	
	// async : if false, then all queued loads are also completed synchronously
	public void loadTexture(String filename, boolean async) {
		loadTexture(filename, null, async);
	}
	
	public void loadTexture(String filename, TextureParameter param, boolean async) {
		AssetManager am = AssetServer.getAssetManager();
		if (!am.isLoaded(filename, Texture.class)) {
			loadQueue.put(filename, Texture.class);
			am.load(filename, Texture.class, param);
			
			if (async)
				isLoading = true;
			else {
				am.finishLoading();
				pumpLoadQueue();
				isLoading = false;
			}
		}
	}
	
	public void unloadTexture(String filename) {
		texCache.remove(filename);
		
		AssetManager am = AssetServer.getAssetManager();
		if (am.isLoaded(filename, Texture.class))
			am.unload(filename);
	}
	
	public void loadAtlas(String filename, boolean async) {
		AssetManager am = AssetServer.getAssetManager();
		if (!am.isLoaded(filename, TextureAtlas.class)) {
			loadQueue.put(filename, TextureAtlas.class);
			am.load(filename, TextureAtlas.class);
			
			if (async)
				isLoading = true;
			else {
				am.finishLoading();
				pumpLoadQueue();
				isLoading = false;
			}
		}
	}
	
	public void unloadAtlas(String filename) {
		atlasCache.remove(filename);
		regionCache.remove(filename);
		animCache.remove(filename);
		
		AssetManager am = AssetServer.getAssetManager();
		if (am.isLoaded(filename, TextureAtlas.class))
			am.unload(filename);
	}
	
	private void prepareCache(String name, TextureAtlas atlas) {
		atlasCache.put(name, atlas);
		regionCache.put(name, new ObjectMap<String, AtlasRegion>(TEX_CACHE_CAPACITY));
		animCache.put(name, new ObjectMap<String, Array<AtlasRegion>>(ANIM_CACHE_CAPACITY));
	}
	
	@Override
	protected void pumpLoadQueue() {
		Entries<String, Class<?>> loadQEntries = loadQueue.entries();
		AssetManager am = AssetServer.getAssetManager();
		while (loadQEntries.hasNext()) {
			Entry<String, Class<?>> entry = loadQEntries.next();
			if (am.isLoaded(entry.key, entry.value)) {
				if (entry.value.equals(TextureAtlas.class))
					prepareCache(entry.key, am.get(entry.key, TextureAtlas.class));
				loadDequeue.add(entry.key);
			}
		}
		
		super.pumpLoadQueue();
	}
	
	public Texture textureByName(String name) {
		// Try the cache
		Texture texture = texCache.get(name);

		// Try the asset manager
		if (texture == null) {
			AssetManager am = AssetServer.getAssetManager();
			if (am.isLoaded(name, Texture.class)) {
				texture = am.get(name, Texture.class);
				// Cache for future queries
				texCache.put(name, texture);
			}
		}
		
		return texture;
	}

	public AtlasRegion textureRegionByName(String name) {
		Entries<String, TextureAtlas> atlasEntries = atlasCache.entries();
		
		// Try the atlas cache
		AtlasRegion region = null;
		while (region == null && atlasEntries.hasNext()) {
			String atlasKey = atlasEntries.next().key;
			ObjectMap<String, AtlasRegion> cache = regionCache.get(atlasKey);
			region = cache.get(name);
		}
		
		// Try the atlases directly
		if (region == null) {
			atlasEntries.reset();
			while (region == null && atlasEntries.hasNext()) {
				Entry<String, TextureAtlas> entry = atlasEntries.next();
				TextureAtlas atlas = entry.value;
				region = atlas.findRegion(name);
				// Cache for future queries
				if (region != null)
					regionCache.get(entry.key).put(name, region);
			}
		}
		
		// TODO: Uncomment if sharing regions causes issues. 
		//if (region != null)
		//	region = new TextureRegion(region);
		
		return region;
	}
	
	public AtlasRegion textureRegionByName(String name, int index) {
		Entries<String, TextureAtlas> atlasEntries = atlasCache.entries();
		
		// Try the atlas cache
		AtlasRegion region = null;
		while (region == null && atlasEntries.hasNext()) {
			String atlasKey = atlasEntries.next().key;
			ObjectMap<String, Array<AtlasRegion>> cache = animCache.get(atlasKey);
			Array<AtlasRegion> regions = cache.get(name);
			if (regions != null && regions.size > index)
				region = regions.get(index);
		}
		
		// Try the atlases directly
		if (region == null) {
			atlasEntries.reset();
			while (region == null && atlasEntries.hasNext()) {
				Entry<String, TextureAtlas> entry = atlasEntries.next();
				TextureAtlas atlas = entry.value;
				Array<AtlasRegion> regions = atlas.findRegions(name);
				if (regions != null && regions.size > index) {
					region = regions.get(index);
					// Cache for future queries
					animCache.get(entry.key).put(name, regions);
				}
			}
		}
		
		// TODO: Uncomment if sharing regions causes issues. 
		//if (region != null)
		//	region = new TextureRegion(region);
		
		return region;
	}
	
	public Array<AtlasRegion> textureRegionsStartingWith(String name) {
		Entries<String, TextureAtlas> atlasEntries = atlasCache.entries();
		
		// Try the atlas cache
		Array<AtlasRegion> regions = null;
		while (regions == null && atlasEntries.hasNext()) {
			String atlasKey = atlasEntries.next().key;
			ObjectMap<String, Array<AtlasRegion>> cache = animCache.get(atlasKey);
			regions = cache.get(name);
		}
		
		// Try the atlases directly
		if (regions == null) {
			atlasEntries.reset();
			while (regions == null && atlasEntries.hasNext()) {
				Entry<String, TextureAtlas> entry = atlasEntries.next();
				TextureAtlas atlas = entry.value;
				regions = atlas.findRegions(name);
				// Cache for future queries
				if (regions != null && regions.size > 0 && regions.get(0) != null)
					animCache.get(entry.key).put(name, regions);
				else
					regions = null;
			}
		}
		
		// TODO: Uncomment if sharing regions causes issues. 
//		if (regions != null) {
//			Array<AtlasRegion> tempRegions = new Array<AtlasRegion>(regions.size);
//			for (int i = 0, n = regions.size; i < n; i++)
//				tempRegions.add(new AtlasRegion(regions.get(i)));
//			regions = tempRegions;
//		}

		return regions;
	}
	
	@Override
	public void dispose() {
		animCache.clear(); // Animation frames are sourced from atlases, which are unloaded below
		
		AssetManager am = AssetServer.getAssetManager();
		
		// Unload textures that aren't sourced from atlas regions
		Keys<String> texKeys = texCache.keys();
		
		while (texKeys.hasNext()) {
			String texKey = texKeys.next();
			if (am.isLoaded(texKey, Texture.class))
				am.unload(texKey);
		}
		texCache.clear();

		// Unload atlases
		regionCache.clear();
		
		Keys<String> atlasKeys = atlasCache.keys();
		while (atlasKeys.hasNext()) {
			String atlasKey = atlasKeys.next();
			if (am.isLoaded(atlasKey, TextureAtlas.class))
				am.unload(atlasKey);
		}
		atlasCache.clear();
		
		super.dispose();
	}
}
