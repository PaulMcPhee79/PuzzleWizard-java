package com.cheekymammoth.assets;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

abstract public class ResourceManager implements Disposable {
	protected boolean isLoading;
	protected ObjectMap<String, Class<?>> loadQueue;
	protected Array<String> loadDequeue;
	
	public ResourceManager() {
		this(1);
	}
	
	public ResourceManager(int capacity) {
		capacity = Math.max(1, capacity);
		loadQueue = new ObjectMap<String, Class<?>>(capacity);
		loadDequeue = new Array<String>(false, capacity, String.class);
	}

	public boolean isLoading() {
		return isLoading;
	}
	
	public boolean update() {
		if (isLoading) {
			isLoading = !AssetServer.getAssetManager().update((int)(1000f / 120f)); // Half a 60fps frame
			if (!isLoading)
				pumpLoadQueue();
		}
		return isLoading;
	}
	
	protected void pumpLoadQueue() {
		for (int i = 0, n = loadDequeue.size; i < n; i++)
			loadQueue.remove(loadDequeue.get(i));
		loadDequeue.clear();
	}
	
	public void dispose() {
		loadQueue.clear();
		loadDequeue.clear();
		isLoading = false;
	}
}
