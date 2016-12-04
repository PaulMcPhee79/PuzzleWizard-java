package com.cheekymammoth.actions;

import com.badlogic.gdx.scenes.scene2d.actions.ColorAction;
import com.badlogic.gdx.utils.Array;
import com.cheekymammoth.graphics.Prop;

public final class PropColorAction extends ColorAction {
	private boolean clearsOnRestart = true;
	private final Array<Prop> props = new Array<Prop>(true, 1, Prop.class);

	@Override
	protected void update (float percent) {
        super.update(percent);
        
        for (int i = 0, n = props.size; i < n; i++) {
        	Prop prop = props.get(i);
        	prop.setColor(getColor());
        }
	}
	
	@Override
	public void restart () {
        super.restart();
        
        if (clearsOnRestart)
        	props.clear();
	}
	
	public boolean getClearsOnRestart() { return clearsOnRestart; }
	
	public void setClearsOnRestart(boolean value) { clearsOnRestart = value; }
	
	public void addProp(Prop prop) {
		if (prop != null)
			props.add(prop);
	}
	
	public void removeProp(Prop prop) {
		if (prop != null)
			props.removeValue(prop, true);
	}
}
