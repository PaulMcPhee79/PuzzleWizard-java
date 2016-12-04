package com.cheekymammoth.animations;

import com.badlogic.gdx.utils.Array;
import com.cheekymammoth.utils.SnapshotSet;

public class Juggler implements IAnimatable {
	private boolean isJuggling;
	private Array<IAnimatable> animatables;
	private Array<IAnimatable> addQueue;
	private Array<IAnimatable> removeQueue;
	private Array<IAnimatable> tempGarbage;
	
	public Juggler() {
		this(10);
	}
	
	public Juggler(int capacity) {
		animatables = new SnapshotSet<IAnimatable>(true, Math.max(1, capacity), IAnimatable.class);
		addQueue = new Array<IAnimatable>(false, Math.max(1, capacity / 4), IAnimatable.class);
		removeQueue = new Array<IAnimatable>(false, Math.max(1, capacity / 4), IAnimatable.class);
		tempGarbage = new Array<IAnimatable>(false, Math.max(1, capacity / 4), IAnimatable.class);
	}
	
	public void addObject(IAnimatable obj) {
		if (obj == null)
			return;
		
		if (isJuggling) {
			removeQueue.removeValue(obj, true);
			addQueue.add(obj);
		} else
			animatables.add(obj);
	}
	
	public void removeObject(IAnimatable obj) {
		if (obj == null)
			return;
		
		if (isJuggling) {
			addQueue.removeValue(obj, true);
			removeQueue.add(obj);
		} else
			animatables.removeValue(obj, true);
	}
	
	public void removeAllObjects() {
		if (isJuggling) {
			addQueue.clear();
			removeQueue.clear();
			
			for (int i = 0, n = animatables.size; i < n; i++)
				removeObject(animatables.get(i));
		} else {
			addQueue.clear();
			removeQueue.clear();
			animatables.clear();
		}
	}
	
	public void removeObjectsWithTarget(Object target) {
		if (target == null)
			return;
		
		if (isJuggling) {
			for (int i = 0, n = animatables.size; i < n; i++) {
				IAnimatable anim = animatables.get(i);
				if (target == anim.getTarget())
					removeObject(anim);
			}
			
			for (int i = 0, n = addQueue.size; i < n; i++) {
				IAnimatable anim = addQueue.get(i);
				if (target == anim.getTarget())
					tempGarbage.add(anim);
			}
			
			for (int i = 0, n = tempGarbage.size; i < n; i++)
				removeObject(tempGarbage.get(i));
			tempGarbage.clear();
		} else {
			for (int i = 0, n = animatables.size; i < n; i++) {
				IAnimatable anim = animatables.get(i);
				if (target == anim.getTarget())
					tempGarbage.add(anim);
			}
			
			for (int i = 0, n = tempGarbage.size; i < n; i++)
				removeObject(tempGarbage.get(i));
			tempGarbage.clear();
		}
	}
	
	@Override
	public boolean isComplete() {
		return false;
	}
	
	@Override
	public Object getTarget() {
		return null;
	}

	@Override
	public void advanceTime(float dt) {
		if (isJuggling || animatables.size == 0)
			return;
		
		isJuggling = true;
		for (int i = 0, n = animatables.size; i < n; i++) {
			IAnimatable anim = animatables.get(i);
			// Skip elements that have been removed during the iteration
			if (removeQueue.size > 0 && removeQueue.contains(anim, true))
				continue;
			anim.advanceTime(dt);
		}
		
		for (int i = 0, n = animatables.size; i < n; i++) {
			IAnimatable anim = animatables.get(i);
			if (anim.isComplete())
				removeObject(anim);
		}
		isJuggling = false;
		
		for (int i = 0, n = addQueue.size; i < n; i++)
			animatables.add(addQueue.get(i));
		for (int i = 0, n = removeQueue.size; i < n; i++)
			animatables.removeValue(removeQueue.get(i), true);
		
		addQueue.clear();
		removeQueue.clear();
	}
}
