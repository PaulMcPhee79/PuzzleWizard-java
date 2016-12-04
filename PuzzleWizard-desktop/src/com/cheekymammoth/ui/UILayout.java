package com.cheekymammoth.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

public class UILayout {
	private int layoutID = -1;
	private Array<Actor> layoutItems;
	private UILayouter layouter;
	
	public UILayout(int layoutID, UILayouter layouter) {
		this.layoutID = layoutID;
		this.layouter = layouter;
	}
	
	public int getLayoutID() { return layoutID; }
	
	public void setLayoutID(int layoutID) { this.layoutID = layoutID; }
	
	public UILayouter getLayouter() { return layouter; }
	
	public void setLayouter(UILayouter value) { layouter = value; }
	
	public Array<Actor> getLayoutItems() { return layoutItems; }
	
	public void addLayoutItem(Actor item) {
		int index = layoutItems != null ? layoutItems.size : 0;
		insertLayoutItem(index, item);
	}
	
	public void insertLayoutItem(int index, Actor item) {
		if (item != null) {
			if (layoutItems == null)
				layoutItems = new Array<Actor>(true, 5, Actor.class);
			if (!layoutItems.contains(item, true))
				layoutItems.insert(
						Math.max(0, Math.min(layoutItems.size, index)),
						item);
		}
	}
	
	public void removeLayoutItem(Actor item) {
		if (item != null && layoutItems != null)
			layoutItems.removeValue(item, true);
	}
	
	public void clearLayoutItems() {
		if (layoutItems != null)
			layoutItems.clear();
	}
	
	public void layout() {
		if (layouter != null)
			layouter.layout(this);
	}
	
	// Use case: Implement and switch on layout.getLayoutID()
	//           to customize layout for different languages
	public static interface UILayouter {
		public void layout(UILayout layout);
	}
}
