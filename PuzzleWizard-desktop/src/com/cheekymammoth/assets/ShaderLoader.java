package com.cheekymammoth.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

public class ShaderLoader extends SynchronousAssetLoader<ShaderProgram, ShaderLoader.ShaderParameter> {
	static final String VERT_EXT = ".vert";
    static final String FRAG_EXT = ".frag";

	ShaderProgram shader;
	
	public ShaderLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public ShaderProgram load(AssetManager assetManager, String fileName, FileHandle file, ShaderParameter parameter) {
		final ShaderProgram shader = new ShaderProgram(resolve(fileName+VERT_EXT ), resolve(fileName+FRAG_EXT));
        if (shader.isCompiled() == false)
            throw new IllegalStateException(shader.getLog());
        return shader;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, ShaderParameter parameter) {
		return null;
	}
	
	static public class ShaderParameter extends AssetLoaderParameters<ShaderProgram> {
    }
}
