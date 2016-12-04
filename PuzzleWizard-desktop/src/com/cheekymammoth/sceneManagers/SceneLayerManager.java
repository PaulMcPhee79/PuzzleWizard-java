package com.cheekymammoth.sceneManagers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.SnapshotArray;
import com.cheekymammoth.graphics.Prop;

public class SceneLayerManager {
	private int numLayers;
	private Prop baseLayer;
	
	public SceneLayerManager(Prop baseLayer, int layerCount, float width, float height) {
		this.baseLayer = baseLayer;
		numLayers = Math.max(1, layerCount);
		
		for (int i = 0; i < numLayers; ++i) {
			Prop prop = new Prop(i);
			prop.setTouchable(Touchable.disabled);
			prop.setSize(width, height);
			baseLayer.addActor(prop);
		}
	}
	
	public void setTouchable(int category, boolean touchable) {
		assert(category >= 0 && category < numLayers) : "SceneLayerManager::setTouchable index out of bounds.";
		if (category >= 0 && category < numLayers)
			layerAtCategory(category).setTouchable(touchable ? Touchable.childrenOnly : Touchable.disabled);
	}
	
	public void addChild(Group child, int category) {
		if (category < baseLayer.getChildren().size)
			layerAtCategory(category).addActor(child);
	}
	
	public void removeChild(Group child, int category) {
		layerAtCategory(category).removeActor(child);
	}
	
	public Prop layerAtCategory(int category) {
		return (Prop)baseLayer.getChildren().get(category);
	}
	
	public void clearAllLayers() {
		SnapshotArray<Actor> layers = baseLayer.getChildren();
		Actor[] items = layers.begin();
		for (int i = 0; i < layers.size; ++i) {
			Group layer = (Group)items[i];
			layer.clearChildren();
		}
		layers.end();
	}
	
	public void clearAll() {
		baseLayer.clearChildren();
	}
}
