package com.cheekymammoth.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.MusicLoader;
import com.badlogic.gdx.assets.loaders.SoundLoader;
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class AssetServer {
	private static final String ART_PATH = "art/";
	private static final String FONT_PATH = "font/";
	private static final String SHADER_PATH = "shaders/";
	private static final String MUSIC_PATH = "music/";
	private static final String SOUND_PATH = "sound/";
	private static AssetManager assetManager;
	
	private AssetServer() { }
	
	public static void init() {
		assetManager = new AssetManager();
		FileHandleResolver artResolver = new FileHandleResolver() {
            public FileHandle resolve (String fileName) {
                return Gdx.files.internal(ART_PATH + fileName);
            }
        };
		assetManager.setLoader(TextureAtlas.class, new TextureAtlasLoader(artResolver));
		assetManager.setLoader(Texture.class, new TextureLoader(artResolver));
		assetManager.setLoader(BitmapFont.class, new BitmapFontLoader(new FileHandleResolver() {
            public FileHandle resolve (String fileName) {
                return Gdx.files.internal(FONT_PATH + fileName);
            }
        }));
		assetManager.setLoader(ShaderProgram.class, new ShaderLoader(new FileHandleResolver() {
            public FileHandle resolve (String fileName) {
                return Gdx.files.internal(SHADER_PATH + fileName);
            }
        }));
		assetManager.setLoader(Music.class, new MusicLoader(new FileHandleResolver() {
            public FileHandle resolve (String fileName) {
                return Gdx.files.internal(MUSIC_PATH + fileName);
            }
        }));
		assetManager.setLoader(Sound.class, new SoundLoader(new FileHandleResolver() {
            public FileHandle resolve (String fileName) {
                return Gdx.files.internal(SOUND_PATH + fileName);
            }
        }));
		Texture.setAssetManager(assetManager);
	}
	
	public static AssetManager getAssetManager() {
		if (assetManager == null)
			init();
		return assetManager;
	}
	
	static void unloadAll() {
		if (assetManager != null)
			assetManager.dispose();
	}
}
