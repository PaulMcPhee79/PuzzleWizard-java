package com.cheekymammoth.puzzleViews;

public interface IPlayerView {
	void playerValueDidChange(int code, int value);
	void willBeginMoving();
	void didFinishMoving();
	void didIdle();
	void didTreadmill();
}
