package com.cheekymammoth.graphics;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.Color;
import com.cheekymammoth.animations.IAnimatable;
import com.cheekymammoth.events.IEventDispatcher;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.graphics.GfxMode.AlphaMode;
import com.cheekymammoth.locale.ILocalizable;
import com.cheekymammoth.sceneControllers.SceneController;
import com.cheekymammoth.ui.TextUtils;
import com.cheekymammoth.utils.Utils;

public class Prop extends Group implements IAnimatable, IEventDispatcher {
	private static SceneController s_Scene;
	private static float s_ContentScaleFactor = 1f;
	
	private int tag = Integer.MIN_VALUE;
	private int category;
	protected SceneController scene;
	private boolean isAdvanceable;
	private EventDispatcher dispatcher;
	private Array<CMSprite> spriteChildren;
	
	private ShaderProgram shader;
	private ICustomRenderer customRenderer;
	
	public Prop() {
		this(-1);
	}

	public Prop(int category) {
		this(category, s_Scene);
	}
	
	public Prop(int category, SceneController scene) {
		this.scene = scene;
		this.category = category;
		setTouchable(scene.getTouchableDefault());
		setTransform(false);
	}
	
	public void setContentSize(float width, float height) {
		super.setSize(width / getContentScaleFactor(), height / getContentScaleFactor());
	}
	
	public int getCategory() {
		return category;
	}
	
	public void setCategory(int value) {
		category = value;
	}
	
	public int getTag() {
		return tag;
	}
	
	public void setTag(int value) {
		tag = value;
	}
	
	public float getScaledWidth() {
		return getWidth() * getScaleX();
	}
	
	public float getScaledHeight() {
		return getHeight() * getScaleY();
	}
	
	public ShaderProgram getShader() {
		return shader;
	}
	
	public void setShader(ShaderProgram value) {
		shader = value;
	}
	
	public ICustomRenderer getCustomRenderer() {
		return customRenderer;
	}
	
	public void setCustomRenderer(ICustomRenderer value) {
		customRenderer = value;
	}
	
	public boolean isAdvanceable() {
		return isAdvanceable;
	}
	
	protected void setAdvanceable(boolean value) {
		isAdvanceable = value;
	}
	
	protected EventDispatcher getEventDispatcher() {
		if (dispatcher == null)
			dispatcher = new EventDispatcher();
		return dispatcher;
	}
	
	public void addedToScene() {
		SnapshotArray<Actor> snapshot = getChildren();
		Actor[] children = snapshot.begin();
		for (int i = 0; i < snapshot.size; i++) {
			Actor child = children[i];
			if (child instanceof Prop)
				((Prop)child).addedToScene();
		}
		snapshot.end();
	}
	
	public void removedFromScene() {
		SnapshotArray<Actor> snapshot = getChildren();
		Actor[] children = snapshot.begin();
		for (int i = 0; i < snapshot.size; i++) {
			Actor child = children[i];
			if (child instanceof Prop)
				((Prop)child).addedToScene();
		}
		snapshot.end();
	}
	
	public void localizeChildren(String fontKey, String FXFontKey) {
		SnapshotArray<Actor> snapshot = getChildren();
		Actor[] children = snapshot.begin();
		for (int i = 0; i < snapshot.size; i++) {
			Actor child = children[i];
			if (child instanceof Label) {
				Label label = (Label)child;
				if (!ILocalizable.kNonLocalizableName.equalsIgnoreCase(label.getName()))
					TextUtils.swapFont(fontKey, label, true);
			} else if (child instanceof ILocalizable)
				((ILocalizable)child).localeDidChange(fontKey, FXFontKey);
			
			if (child instanceof Prop)
				((Prop)child).localizeChildren(fontKey, FXFontKey);
		}
		snapshot.end();
	}
	
	public Prop childForTag(int tag) {
		SnapshotArray<Actor> snapshot = getChildren();
		Actor[] children = snapshot.begin();
		Prop foundProp = null;
		for (int i = 0; i < snapshot.size; i++) {
			Actor child = children[i];
			if (child instanceof Prop) {
				Prop prop = (Prop)child;
				if (prop.getTag() == tag) {
					foundProp = prop;
					break;
				}
			}
		}
		snapshot.end();
		return foundProp;
	}
	
	public int getNumSpriteChildren() {
		return spriteChildren != null ? spriteChildren.size : 0;
	}
	
	public void addSpriteChild(CMSprite child) {
		if (spriteChildren == null)
			spriteChildren = new Array<CMSprite>(true, 4, CMSprite.class);
		spriteChildren.add(child);
	}
	
	public void insertSpriteChild(int index, CMSprite child) {
		if (spriteChildren == null)
			spriteChildren = new Array<CMSprite>(true, 4, CMSprite.class);
		index = Math.max(0, Math.min(index, getNumSpriteChildren()-1));
		spriteChildren.insert(index, child);
	}
	
	public void removeSpriteChild(CMSprite child) {
		if (spriteChildren != null)
			spriteChildren.removeValue(child, true);
	}
	
	public CMSprite spriteChildForTag(int tag) {
		for (int i = 0; spriteChildren != null && i < spriteChildren.size; i++) {
			CMSprite child = spriteChildren.get(i);
			if (child != null && child.getTag() == tag)
				return child;
		}
		
		return null;
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		if (isTransform()) applyTransform(batch, computeTransform());
		if (customRenderer != null)
			customRenderer.preDraw(batch, parentAlpha, this);
		drawSpriteChildren(batch, parentAlpha);
        drawChildren(batch, parentAlpha);
        if (customRenderer != null)
			customRenderer.postDraw(batch, parentAlpha, this);
        if (isTransform()) resetTransform(batch);
	}
	
	private Color colorCache = new Color();
	private Color childColorCache = new Color();
	protected void drawSpriteChildren(Batch batch, float parentAlpha) {
		if (spriteChildren == null || spriteChildren.size == 0)
			return;
		
		//Color batchColorCache = batch.getColor();
		for (int i = 0, n = spriteChildren.size; i < n; i++) {
			CMSprite child = spriteChildren.get(i);
			childColorCache.set(child.getColor());
			
			colorCache.set(getColor());
			Utils.setA(colorCache, colorCache.a * parentAlpha);
			
			if (child.getAlphaMode() == AlphaMode.PRE_MULTIPLIED)
				child.setColor(Utils.premultiplyAlpha(child.getColor().mul(colorCache)));
			else
				child.setColor(child.getColor().mul(colorCache));
			
			if(isTransform())
				child.draw(batch); //, parentAlpha);
			else {
				float cx = child.getX(), cy = child.getY();
				child.setPosition(cx + getX(), cy + getY());
				child.draw(batch); //, parentAlpha);
				child.setPosition(cx, cy);
			}
			
			child.setColor(childColorCache);
		}
		//batch.setColor(batchColorCache);
	}

	@Override
	public void addEventListener(int evType, IEventListener listener) {
		EventDispatcher dispatcher = getEventDispatcher();
		if (dispatcher != null)
			dispatcher.addEventListener(evType, listener);
	}
	
	@Override
	public void removeEventListener(int evType, IEventListener listener) {
		EventDispatcher dispatcher = getEventDispatcher();
		if (dispatcher != null)
			dispatcher.removeEventListener(evType, listener);
	}
	
	@Override
	public void removeEventListeners(int evType) {
		EventDispatcher dispatcher = getEventDispatcher();
		if (dispatcher != null)
			dispatcher.removeEventListeners(evType);
	}
	
	@Override
	public boolean hasEventListenerForType(int evType) {
		EventDispatcher dispatcher = getEventDispatcher();
		return dispatcher != null && dispatcher.hasEventListenerForType(evType);
	}
	
	@Override
	public void dispatchEvent(int evType, Object evData) {
		EventDispatcher dispatcher = getEventDispatcher();
		if (dispatcher != null)
			dispatcher.dispatchEvent(evType, evData);
	}
	
	public void dispatchEvent(int evType) {
		dispatchEvent(evType, null);
	}

	@Override
	public void advanceTime(float dt) {
		// Do nothing
	}
	
	@Override
	public boolean isComplete() {
		return false;
	}
	
	@Override
	public Object getTarget() {
		return null;
	}
	
	public static SceneController getPropsScene() {
		return s_Scene;
	}
	
	public static void setPropScene(SceneController value) {
		if (value != null)
			s_Scene = value;
	}
	
	public static void relinquishPropScene(SceneController value) {
		if (value == null)
			throw new IllegalArgumentException("Prop scene cannot be null");
		if (value == s_Scene)
			s_Scene = null;
	}
	
	public static float getContentScaleFactor() {
		return s_ContentScaleFactor;
	}
	
	public static void setContentScaleFactor(float value) {
		assert(value > 0) : "Prop::setContentScaleFactor must be > 0";
		s_ContentScaleFactor = value;
	}
}
