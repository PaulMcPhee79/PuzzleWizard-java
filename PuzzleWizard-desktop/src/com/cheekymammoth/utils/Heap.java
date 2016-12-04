package com.cheekymammoth.utils;

// Source: Altered from com.badlogic.gdx.utils.BinaryHeap
public class Heap<T extends Heap.Node> {
	public int size;

	private Node[] nodes;
	private final boolean isMaxHeap;

	public Heap () {
		this(16, false);
	}

	public Heap (int capacity, boolean isMaxHeap) {
		this.isMaxHeap = isMaxHeap;
		nodes = new Node[capacity];
	}
	
	public Node[] getNodes() {
		return nodes;
	}

	public T add (T node) {
		// Expand if necessary.
		if (size == nodes.length) {
			Node[] newNodes = new Node[size << 1];
			System.arraycopy(nodes, 0, newNodes, 0, size);
			nodes = newNodes;
		}
		// Insert at end and bubble up.
		node.index = size;
		nodes[size] = node;
		up(size++);
		return node;
	}

	public T add (T node, float value) {
		node.setValue(value);
		return add(node);
	}

	@SuppressWarnings("unchecked")
	public T peek () {
		if (size == 0) throw new IllegalStateException("The heap is empty.");
		return (T)nodes[0];
	}

	public T pop () {
		return remove(0);
	}

	public T remove (T node) {
		return remove(node.index);
	}

	@SuppressWarnings("unchecked")
	private T remove (int index) {
		Node[] nodes = this.nodes;
		Node removed = nodes[index];
		nodes[index] = nodes[--size];
		nodes[size] = null;
		if (size > 0 && index < size) down(index);
		return (T)removed;
	}

	public void clear () {
		Node[] nodes = this.nodes;
		for (int i = 0, n = size; i < n; i++)
			nodes[i] = null;
		size = 0;
	}

	public void setValue (T node, float value) {
		float oldValue = node.getValue();
		node.setValue(value);
		if (value < oldValue ^ isMaxHeap)
			up(node.index);
		else
			down(node.index);
	}

	private void up (int index) {
		Node[] nodes = this.nodes;
		Node node = nodes[index];
		float value = node.getValue();
		while (index > 0) {
			int parentIndex = (index - 1) >> 1;
			Node parent = nodes[parentIndex];
			if (value < parent.getValue() ^ isMaxHeap) {
				nodes[index] = parent;
				parent.index = index;
				index = parentIndex;
			} else
				break;
		}
		nodes[index] = node;
		node.index = index;
	}

	private void down (int index) {
		Node[] nodes = this.nodes;
		int size = this.size;

		Node node = nodes[index];
		float value = node.getValue();

		while (true) {
			int leftIndex = 1 + (index << 1);
			if (leftIndex >= size) break;
			int rightIndex = leftIndex + 1;

			// Always have a left child.
			Node leftNode = nodes[leftIndex];
			float leftValue = leftNode.getValue();

			// May have a right child.
			Node rightNode;
			float rightValue;
			if (rightIndex >= size) {
				rightNode = null;
				rightValue = isMaxHeap ? Float.MIN_VALUE : Float.MAX_VALUE;
			} else {
				rightNode = nodes[rightIndex];
				rightValue = rightNode.getValue();
			}

			// The smallest of the three values is the parent.
			if (leftValue < rightValue ^ isMaxHeap) {
				if (leftValue == value || (leftValue > value ^ isMaxHeap)) break;
				nodes[index] = leftNode;
				leftNode.index = index;
				index = leftIndex;
			} else {
				if (rightValue == value || (rightValue > value ^ isMaxHeap)) break;
				nodes[index] = rightNode;
				rightNode.index = index;
				index = rightIndex;
			}
		}

		nodes[index] = node;
		node.index = index;
	}

	public String toString () {
		if (size == 0) return "[]";
		Node[] nodes = this.nodes;
		StringBuilder buffer = new StringBuilder(32);
		buffer.append('[');
		buffer.append(nodes[0].getValue());
		for (int i = 1; i < size; i++) {
			buffer.append(", ");
			buffer.append(nodes[i].getValue());
		}
		buffer.append(']');
		return buffer.toString();
	}

	static public class Node {
		float value;
		int index;

		public Node (float value) {
			this.value = value;
		}

		public float getValue () {
			return value;
		}
		
		public void setValue(float value) {
			this.value = value;
		}

		public String toString () {
			return Float.toString(value);
		}
	}
}
