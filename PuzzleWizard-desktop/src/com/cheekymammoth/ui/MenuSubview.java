package com.cheekymammoth.ui;

import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.utils.Utils;

public class MenuSubview extends Prop {
	private boolean shouldDestroy;
	
	public MenuSubview() {
		setTag(Utils.getUniqueKey());
	}
	
	public boolean shouldDestroy() { return shouldDestroy; }
	
	public void setShouldDestroy(boolean value) { shouldDestroy = value; }
}
