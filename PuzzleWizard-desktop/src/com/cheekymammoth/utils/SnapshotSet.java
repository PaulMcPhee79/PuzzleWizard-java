package com.cheekymammoth.utils;

//import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;

public class SnapshotSet<T> extends SnapshotArray<T> {
	public SnapshotSet () {
        super();
	}
	
	public SnapshotSet (int capacity) {
        super(capacity);
	}
	
	public SnapshotSet (boolean ordered, int capacity) {
        super(ordered, capacity);
	}
	
	@SuppressWarnings("rawtypes")
	public SnapshotSet (boolean ordered, int capacity, Class arrayType) {
		super(ordered, capacity, arrayType);
	}

	@Override
	public void add (T value) {
		if (contains(value, true))
        	return;
		super.add(value);
	}
	
	// Routed through overloads
	//public void addAll (Array<? extends T> array);
	//public void addAll (Array<? extends T> array, int offset, int length);
	//public void addAll (T[] array);
	
	@Override
	public void addAll (T[] array, int offset, int length) {
		if (offset + length > array.length)
            throw new IllegalArgumentException("offset + length must be <= size: " + offset + " + " + length + " <= " + array.length);

		for (int i = offset, n = offset + length; i < n; i++) {
            T item = array[i];
            if (contains(item, true))
            	return;
		}
		
		super.addAll(array, offset, length);
	}
	
	@Override
	public void set (int index, T value) {
        if (contains(value, true))
        	return;
        super.set(index, value);
	}

	@Override
	public void insert (int index, T value) {
		if (contains(value, true))
        	return;
		super.insert(index, value);
	}
}
