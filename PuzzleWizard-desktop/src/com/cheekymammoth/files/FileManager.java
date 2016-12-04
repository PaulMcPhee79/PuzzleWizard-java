package com.cheekymammoth.files;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;


public class FileManager {
	private static FileManager singleton = new FileManager();
	private static final String kPathModifierOld = "_backup";
	private static final String kPathModifierNew = "_new";
	
	private static final int kNewPathIndex = 0;
	private static final int kPathIndex = 1;
	private static final int kOldPathIndex = 2;
	
	private String pathCache;
	private String[] pathsCache;
	private Object lock = new Object();
	
	private FileManager() { }

	public static FileManager FM() {
        return singleton;
    }
	
	private String[] getPaths(String path, String pathExt) {
		if (path == null || pathExt == null)
			throw new NullPointerException("FileManager load: File path cannot be null.");
		if (!(path + pathExt).equalsIgnoreCase(pathCache) || pathsCache == null) {
			if (pathsCache == null)
				pathsCache = new String[3];
			pathCache = path + pathExt;
			pathsCache[kNewPathIndex] = path + kPathModifierNew + pathExt;
			pathsCache[kPathIndex] = pathCache;
			pathsCache[kOldPathIndex] = path + kPathModifierOld + pathExt;
		}
		
		return pathsCache;
	}
	
	public byte[] loadProgress(String path, String pathExt) {
		synchronized(lock) {
			byte[] data = null;
			String[] paths = getPaths(path, pathExt);
			for (int i = 0, n = paths.length; i < n; i++) {
				FileHandle file = Gdx.files.local(paths[i]);
				if (file != null && file.exists()) {
					data = file.readBytes();
					if (data != null) {
						// If we loaded from a backup file, then promote the
						// backup to be the official saved game state.
						if (!paths[i].equalsIgnoreCase(pathCache)) {
							try {
								file.moveTo(Gdx.files.local(pathCache));
							} catch (GdxRuntimeException e) {
								Gdx.app.log("Load move failed", e.getMessage());
							}
						}
						return data;
					}
				}
			}
			
			return null;
        }
	}
	
	private int saveProgressInternal(final String path, final String pathExt, final byte[] data) {
		synchronized(lock) {
			if (path == null || pathExt == null || data == null)
				throw new NullPointerException("FileManager save: File path and data cannot be null.");

			String[] paths = getPaths(path, pathExt);
			FileHandle newFile = Gdx.files.local(paths[kNewPathIndex]);
			if (newFile != null) {
				try {
					// 1. Save game to new path
					newFile.writeBytes(data, false);
				} catch (GdxRuntimeException e) {
					Gdx.app.log("Save failed to " + paths[kNewPathIndex], e.getMessage());
					return IFileClient.SAVE_FAILED;
				}
				
				try {
					// 2. Move path to old path
					FileHandle oldFile = Gdx.files.local(paths[kPathIndex]);
					if (oldFile != null && oldFile.exists())
						oldFile.moveTo(Gdx.files.local(paths[kOldPathIndex]));
					
					// 3. Move new path to path
					newFile.moveTo(Gdx.files.local(paths[kPathIndex]));
				} catch (GdxRuntimeException e) {
					Gdx.app.log("Post-save moves failed.", e.getMessage());
				}
				
				return IFileClient.SAVE_SUCCEEDED;
			}
        }
		
		return IFileClient.SAVE_FAILED;
	}

	public void saveProgress(final String path, final String pathExt, final byte[] data, final IFileClient client) {
		new Thread(new Runnable() {
            public void run() {
            	int saveResult = saveProgressInternal(path, pathExt, data);
            	if (client != null)
            		client.onAsyncSaveCompleted(saveResult);
            }
        }).start();
	}
}
