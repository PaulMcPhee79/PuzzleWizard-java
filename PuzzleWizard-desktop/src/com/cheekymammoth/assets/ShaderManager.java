package com.cheekymammoth.assets;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entries;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.ObjectMap.Keys;

// Note: Shaders added to ShaderManager cede resource management to ShaderManager
public class ShaderManager implements Disposable {
	private Texture[] textureBindings;
	private ObjectMap<String, ShaderDescriptor> shaderDescriptors;
	
	@SuppressWarnings("unused")
	private ShaderManager() {
		// No-op; won't be called
	}
	
	public ShaderManager(int maxTextureInits) {
		textureBindings = new Texture[maxTextureInits];
		shaderDescriptors = new ObjectMap<String, ShaderDescriptor>(10);
	}
	
	public void loadShader(String filename) {
		AssetManager am = AssetServer.getAssetManager();
		if (!am.isLoaded(filename, ShaderProgram.class)) {
			am.load(filename, ShaderProgram.class);
			am.finishLoading();
		}
	}
	
	public void unloadShader(String filename) {
		removeShaderDescriptor(filename);
		
		AssetManager am = AssetServer.getAssetManager();
		if (am.isLoaded(filename, ShaderProgram.class))
			am.unload(filename);
	}
	
	public ShaderProgram shaderByName(String name) {
		ShaderDescriptor desc = shaderDescriptors.get(name);
		if (desc != null)
			return desc.shader;
		
		ShaderProgram shader = null;
		AssetManager am = AssetServer.getAssetManager();
		if (am.isLoaded(name, ShaderProgram.class))
			shader = am.get(name, ShaderProgram.class);
		return shader;
	}
	
	public void addShaderDescriptor(String name, ShaderDescriptor desc) {
		shaderDescriptors.put(name, desc);
	}
	
	public void removeShaderDescriptor(String name) {
		shaderDescriptors.remove(name);
	}
	
	public void removeShaderDescriptorsWithTexture(Texture texture) {
		Array<String> removeList = new Array<String>();
		Entries<String, ShaderDescriptor> descEntries = shaderDescriptors.entries();
		Iterator<Entry<String, ShaderDescriptor>> descIt = descEntries.iterator();
		
		while (descIt.hasNext()) {
			Entry<String, ShaderDescriptor> descEntry = descIt.next();
			ShaderDescriptor desc = descEntry.value;
			for (int i = 0, n = desc.textures.length; i < n; i++) {
				if (texture == desc.textures[i]) {
					removeList.add(descEntry.key);
					break;
				}
			}
		}
		
		for (int i = 0, n = removeList.size; i < n; i++)
			shaderDescriptors.remove(removeList.get(i));
	}
	
	public void setTextureForShaderDescriptor(String name, int index, Texture texture) {
		if (name == null || texture == null || index < 0)
			return;
		
		ShaderDescriptor desc = shaderDescriptors.get(name);
		if (desc != null && index < desc.textures.length )
			desc.textures[index] = texture;
	}
	
	// Note: may reset glActiveTexture to GL20.GL_TEXTURE0
	public void applyShaderDesciptor(String name) {
		ShaderDescriptor desc = shaderDescriptors.get(name);
		assert(desc != null) : "NULL shader in ShaderManager::applyShaderDesciptor";
		
		boolean dirtyFlag = false;
		for (int i = 0, n = desc.size; i < n; i++) {
			int texUnit = desc.textureUnits[i]; 
			if (desc.textures[i] != textureBindings[texUnit]) {
				bindTexture(texUnit, desc.textures[i]);
				dirtyFlag = true;
			}
			
			desc.shader.setUniformi(desc.uniformNames[i], texUnit);
		}
		
		if (dirtyFlag)
			Gdx.graphics.getGL20().glActiveTexture(GL20.GL_TEXTURE0);
	}
	
	public void bindTexture(int index, Texture texture) {
		assert(index >=  0 && index < textureBindings.length) :
			"Index out of bounds in ShaderManager::bindTexture";
		Gdx.graphics.getGL20().glActiveTexture(GL20.GL_TEXTURE0+index);
		texture.bind();
		textureBindings[index] = texture;
	}
	
	public void dispose() {
		// Clear texture bindings
		for (int i = 0, n = textureBindings.length; i < n; i++)
			textureBindings[i] = null;
		
		// Clean up shaders
		Keys<String> keys = shaderDescriptors.keys();
		Iterator<String> keysIt = keys.iterator();
		while (keysIt.hasNext())
			unloadShader(keysIt.next());
		shaderDescriptors.clear();
	}
	
	static public class ShaderDescriptor {
		public int size;
		public int[] textureUnits;
		public String[] uniformNames;
		public Texture[] textures;
		public ShaderProgram shader;
		
		public ShaderDescriptor(ShaderProgram shader, int[] textureUnits, String[] uniformNames, Texture[] textures) {
			assert(shader != null && textureUnits != null && textures != null &&
					textureUnits.length > 0 &&
					textureUnits.length == textures.length) : "Invalid args in ShaderSettings";
			
			this.shader = shader;
			size = textureUnits.length;
					
			this.textureUnits = new int[size];
			System.arraycopy(textureUnits, 0, this.textureUnits, 0, size);
			
			this.uniformNames = new String[size];
			System.arraycopy(uniformNames, 0, this.uniformNames, 0, size);
			
			this.textures = new Texture[size];
			System.arraycopy(textures, 0, this.textures, 0, size);
		}
	}
}
