package com.cheekymammoth.pathFinding;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.cheekymammoth.utils.Heap;

// Java port of Justin Heyes-Jones' C++ original.
public class AStar {
	public enum SearchState {
		Uninitialized,
		Searching,
		Succeeded,
		Failed,
		OutOfMemory,
		Invalid
	}
	
	private static class Node extends Heap.Node implements Poolable {
		public Node(float value) {
			super(value);
			f = value;
		}

		public Node parent, child;
		public float g, h, f;
		public SearchNode userState;
		
		@Override
		public float getValue () {
			return f;
		}
		
		@Override
		public void setValue(float value) {
			super.setValue(value);
			f = value;
		}

		@Override
		public void reset() {
			parent = child = null;
			g = h = f = 0;
			setValue(0);
			userState = null;
		}
	}
	
	private Heap<Node> openList;
	private Array<Node> closedList;
	private Array<Node> successors;
	private SearchState state = SearchState.Uninitialized;
	private int numSteps;
	private Node startNode;
	private Node goalNode;
	private Node currentSolutionNode;
	private boolean cancelRequest = false;
	private int allocateNodeCount = 0; // For debugging
	
	public AStar() {
		this(1000);
	}
	
	public AStar(int maxNodes) {
		openList = new Heap<Node>(128, false);
		closedList = new Array<Node>(128);
		successors = new Array<Node>(4);
	}
	
	private void setState(SearchState state) {
		this.state = state;
	}

	public void cancelSearch() {
		cancelRequest = true;
	}
	
	// Note: These SearchNodes (start,goal) must be returned to the Pool at some point. 
	public void setStartAndGoalStates(SearchNode start, SearchNode goal) {
		cancelRequest = false;
		
		startNode = getNode();
		goalNode = getNode();
		
		startNode.userState = start;
		goalNode.userState = goal;
		
		setState(SearchState.Searching);
		
		startNode.g = 0;
		startNode.h = startNode.userState.getGoalDistanceEstimate(goalNode.userState);
		startNode.f = startNode.g + startNode.h;
		startNode.parent = null;
		
		openList.add(startNode);
		
		numSteps = 0;
	}
	
	public SearchState searchStep() {
		if (state.ordinal() <= SearchState.Uninitialized.ordinal()
				|| state.ordinal() >= SearchState.Invalid.ordinal())
			throw new IllegalStateException("Invalid search state.");
		
		if (state == SearchState.Succeeded || state == SearchState.Failed)
			return state;
		
		if (openList.size == 0 || cancelRequest) {
			freeAllNodes();
			state = SearchState.Failed;
			return state;
		}
		
		numSteps++;
		
		Node n = openList.pop();
		
		if (n.userState.isGoal(goalNode.userState)) {
			goalNode.parent = n.parent;
			
			if (false == n.userState.isSameState(startNode.userState)) {
				freeNode(n);
				
				Node nodeChild = goalNode;
				Node nodeParent = goalNode.parent;
				
				do {
					nodeParent.child = nodeChild;
					nodeChild = nodeParent;
					nodeParent = nodeParent.parent;
				} while (nodeChild != startNode);
			}
			
			freeUnusedNodes();
			state = SearchState.Succeeded;
			return state;
		} else {
			int numAncestors = getNumAncestors(n);
			
			successors.clear(); // Does this leak nodes?
			
			boolean ret = n.userState.getSuccessors(
					this,
					n.parent != null ? n.parent.userState : null,
					numAncestors);
			if (!ret) {
				for (int i = 0, fin = successors.size; i < fin; i++)
					freeNode(successors.get(i));
				successors.clear();
				
				freeAllNodes();
				
				state = SearchState.OutOfMemory;
				return state;
			}
			
			for (int i = 0, iFin = successors.size; i < iFin; i++) {
				Node successor = successors.get(i);
				float newg = n.g + n.userState.getCost(successor.userState);
				
				Node[] nodes = (AStar.Node[])openList.getNodes();
				Node openListResult = null;
				for (int j = 0, jFin = openList.size; j < jFin; j++) {
					openListResult = nodes[j];
					if (openListResult.userState.isSameState(successor.userState))
						break;
				}
				
				if (openListResult != null && openListResult != nodes[openList.size-1]) {
					if (openListResult.g <= newg) {
						freeNode(successor);
						continue;
					}
				}
				
				Node closedListResult = null;
				for (int j = 0, jFin = closedList.size; j < jFin; j++) {
					closedListResult = closedList.get(j);
					if (closedListResult.userState.isSameState(successor.userState))
						break;
				}
				
				if (closedListResult != null && closedListResult != closedList.get(closedList.size-1)) {
					if (closedListResult.g <= newg) {
						freeNode(successor);
						continue;
					}
				}
				
				successor.parent = n;
				successor.g = newg;
				successor.h = successor.userState.getGoalDistanceEstimate(goalNode.userState);
				successor.f = successor.g + successor.h;
				
				if (closedListResult != null && closedListResult != closedList.get(closedList.size-1)) {
					closedList.removeValue(closedListResult, true);
					freeNode(closedListResult);
					closedListResult = null;
				}
				
				if (openListResult != null && openListResult != nodes[openList.size-1]) {
					openList.remove(openListResult);
					freeNode(openListResult);
					openListResult = null;
				}
				
				openList.add(successor);
			}
			
			closedList.add(n);
		}
		
		return state;
	}
	
	// Note: This SearchNode (userState) must be returned to the Pool at some point. 
	public boolean addSuccessor(SearchNode userState) {
		Node node = getNode();
		
		if (node != null) {
			node.userState = userState;
			successors.add(node);
			return true;
		}
		
		return false;
	}
	
	public void freeSolutionNodes() {
		if (startNode == null) return;
		
		Node n = startNode;
		
		if (startNode.child != null) {
			do {
				Node del = n;
				n = n.child;
				freeNode(del);
				del = null;
			} while (n != goalNode);
			
			freeNode(n);
		} else {
			freeNode(startNode);
			freeNode(goalNode);
		}
	}
	
	public SearchNode getSolutionStart() {
		currentSolutionNode = startNode;
		if (startNode != null)
			return startNode.userState;
		else
			return null;
	}
	
	public SearchNode getSolutionNext() {
		if (currentSolutionNode != null) {
			if (currentSolutionNode.child != null) {
				Node child = currentSolutionNode.child;
				currentSolutionNode = currentSolutionNode.child;
				return child.userState;
			}
		}
		
		return null;
	}
	
	public SearchNode getSolutionEnd() {
		currentSolutionNode = goalNode;
		if (goalNode != null)
			return goalNode.userState;
		else
			return null;
	}
	
	public SearchNode getSolutionPrev() {
		if (currentSolutionNode != null) {
			if (currentSolutionNode.parent != null) {
				Node parent = currentSolutionNode.parent;
				currentSolutionNode = currentSolutionNode.parent;
				return parent.userState;
			}
		}
		
		return null;
	}
	
	public int getStepCount() { return numSteps; }
	
	public void ensureMemoryFreed() {
		assert(allocateNodeCount == 0) : "AStar: Memory leak detected.";
	}
	
	private int getNumAncestors(Node node) {
		int count = 0;
		while (node != null && node.parent != null) {
			++count;
			node = node.parent;
		}
		return count;
	}
	
	private void freeAllNodes() {
		while (openList.size > 0) {
			Node node = openList.pop();
			freeNode(node);
		}
		
		for (int i = 0, n = closedList.size; i < n; i++)
			freeNode(closedList.get(i));
		closedList.clear();
		
		freeNode(goalNode);
	}
	
	private void freeUnusedNodes() {
		Node[] nodes = (AStar.Node[])openList.getNodes();
		
		for (int i = openList.size - 1; i >- 0; i--) {
			Node node = nodes[i];
			
			if (node.child == null) {
				openList.remove(node);
				freeNode(node);
			}
		}
		
		for (int i = 0, n = closedList.size; i < n; i++)
			freeNode(closedList.get(i));
		closedList.clear();
	}
	
	private Node getNode() {
		allocateNodeCount++;
		return Pools.obtain(Node.class);
	}
	
	private void freeNode(Node node) {
		if (node != null) {
			--allocateNodeCount;
			Pools.free(node);
		}
	}
}
