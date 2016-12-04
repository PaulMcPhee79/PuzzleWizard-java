package com.cheekymammoth.ui;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.SnapshotArray;
import com.cheekymammoth.graphics.CroppedProp;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.input.CMInputs;
import com.cheekymammoth.ui.INavigable.NavigationMap;
import com.cheekymammoth.utils.Coord;
import com.cheekymammoth.utils.Utils;

public final class CreditsDialog extends MenuDialog {
	private static final float kDefaultScrollRate = 2f;
	private static final float kMaxScrollRate = 10f;
	private static final float kScrollRateDecelFactor = 0.9f;
	
	private float showOffsetY;
	private float scrollRate = kDefaultScrollRate;
	private float scrollRateDelta;
	private Rectangle scrollContentBounds;
	private Rectangle scrollBounds;
	private Prop scrollContainer;
	private CroppedProp scrollCropper;
	
	public CreditsDialog() {
		this(-1, 0, 0, 0);
	}

	public CreditsDialog(int category, int priority, int inputFocus, float showOffsetY) {
		super(category, priority, inputFocus, NavigationMap.NAV_VERT);
		
		this.showOffsetY = showOffsetY;
		scrollBounds = new Rectangle(0, 0, scene.VW(), scene.VH());
		scrollContentBounds = new Rectangle();
		
		scrollCropper = new CroppedProp(category, scrollBounds);
		addActor(scrollCropper);
		
		scrollContainer = new Prop(category);
		scrollCropper.addActor(scrollContainer);
	}
	
	@Override
	public void localeDidChange(String fontKey, String FXFontKey) {
		super.localeDidChange(fontKey, FXFontKey);
		scrollContainer.localizeChildren(fontKey, FXFontKey);
	}
	
	@Override
	public void show(boolean animate) {
		super.show(animate);
		
		scrollRate = kDefaultScrollRate;
		scrollContainer.setY(scrollBounds.getY() + showOffsetY);
	}
	
	public Rectangle getScrollBounds() { return scrollBounds; }
	
	public void setScrollBounds(Rectangle value) {
		if (value == null)
			scrollBounds.set(0, 0, scene.VW(), scene.VH());
		else
			scrollBounds.set(value);
		scrollCropper.setViewableRegion(scrollBounds);
	}
	
	private Rectangle srcRectCache = new Rectangle();
	public void addScrollingItem(Actor item) {
		if (item == null)
			return;
		
		scrollContainer.addActor(item);
		srcRectCache.set(item.getX(), item.getY(), item.getWidth(), item.getHeight());
		Utils.unionRect(srcRectCache, scrollContentBounds);
	}
	
	public void removeScrollingItem(Actor item) {
		if (item == null)
			return;
		
		scrollContainer.removeActor(item);
		scrollContentBounds.set(0, 0, 0, 0);
		
		SnapshotArray<Actor> snapshot = scrollContainer.getChildren();
		Actor[] children = snapshot.begin();
		for (int i = 0; i < snapshot.size; i++) {
			Actor child = children[i];
			srcRectCache.set(child.getX(), child.getY(), child.getWidth(), child.getHeight());
			Utils.unionRect(srcRectCache, scrollContentBounds);
		}
		snapshot.end();
	}
	
	private void updateContentVisibility() {
		float scrollY = scrollContainer.getY();
		SnapshotArray<Actor> snapshot = scrollContainer.getChildren();
		Actor[] children = snapshot.begin();
		for (int i = 0; i < snapshot.size; i++) {
			Actor child = children[i];
			child.setVisible(scrollBounds.contains(child.getX(), scrollY + child.getY()) ||
					scrollBounds.contains(child.getX(), scrollY + child.getY() + child.getHeight()));
		}
		snapshot.end();
	}
	
	@Override
	public void update(CMInputs input) {
		super.update(input);
		
		Coord heldVec = input.getHeldVector();
		
		if (heldVec.y != 0) {
			scrollRateDelta = -0.2f * Math.max(kDefaultScrollRate, Math.abs(scrollRate)) * heldVec.y;
			scrollRate += scrollRateDelta;
			if (Math.abs(scrollRate) > kMaxScrollRate)
				scrollRate = (scrollRate > 0 ? 1 : -1) * kMaxScrollRate;
		} else
			scrollRateDelta = 0;
	}

	@Override
	public void advanceTime(float dt) {
		super.advanceTime(dt);
		
		scrollContainer.setY(scrollContainer.getY() + scrollRate);
		if (scrollRate > 0) {
			if (scrollContainer.getY() > (scrollBounds.y + scrollBounds.height + scrollContentBounds.height))
				scrollContainer.setY(scrollBounds.y);
		} else if (scrollRate < 0) {
			if (scrollContainer.getY() < scrollBounds.y)
				scrollContainer.setY(scrollBounds.y + scrollBounds.height + scrollContentBounds.height);
		}
		
		float prevScrollRate = scrollRate;
		if (scrollRateDelta == 0 && scrollRate > 0 && scrollRate < kDefaultScrollRate)
			scrollRate *= 1.11f;
		else
			scrollRate *= kScrollRateDecelFactor;
		
		if (scrollRateDelta == 0 && scrollRate < kDefaultScrollRate && prevScrollRate >= kDefaultScrollRate)
			scrollRate = kDefaultScrollRate; // Clamp to default scroll rate
		else if (scrollRateDelta == 0 && scrollRate > -0.2f && scrollRate < 0)
			scrollRate = 0.2f * kDefaultScrollRate; // Reverse direction
		
		updateContentVisibility();
	}
}
