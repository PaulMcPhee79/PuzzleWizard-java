package com.cheekymammoth.puzzleViews;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.cheekymammoth.graphics.Prop;

abstract public class TileDecoration extends Prop implements Poolable {
    private int type;
    private int subType;
    private ITileDecorator decorator;
    
    public TileDecoration() {
    	this(-1, TilePiece.kTDKNone, 0);
    }
    
	public TileDecoration(int category, int type, int subType) {
		super(category);
		
		this.type = type;
		this.subType = subType;
		setTransform(true);
	}
	
	public int getType() { return type; }
	
	public void setType(int value) { type = value; }
	
	public int getSubType() { return subType; }
	
	public void setSubType(int value) { subType = value; }
	
	protected ITileDecorator getDecorator() { return decorator; }
	
	public void setDecorator(ITileDecorator value) { decorator = value; }
	
	public void enableUIMode(boolean enable) { }
	
	public void enableMenuMode(boolean enable) { }
	
	public void syncWithDecorator() { }
	
	public void syncWithTileDecoration(TileDecoration other) { }
	
	public void valueDidChange(int evCode, int value) { }
	
	public void valueDidChange(int evCode, float value) { }

	public void reuse() {
		enableMenuMode(false);
		
		if (isAdvanceable())
			scene.addToJuggler(this);
	}
	
	@Override
	public void reset() {
		enableUIMode(false);
		setColor(Color.WHITE);
		
		if (isAdvanceable())
			scene.removeFromJuggler(this);
		
		setDecorator(null);
		remove();
		type = subType = 0;
	}

}
