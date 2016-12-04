package com.cheekymammoth.puzzleViews;

import com.cheekymammoth.puzzles.Player;
import com.cheekymammoth.utils.Coord;

public interface IPuzzleView {
	void puzzleSoundShouldPlay(String soundName);
    void puzzlePlayerWillMove(Player player);
    void puzzlePlayerDidMove(Player player);
    void puzzleShieldDidDeploy(int tileIndex);
    void puzzleShieldWasWithdrawn(int tileIndex);
    void puzzleTilesShouldRotate(int[] tileIndexes);
    void puzzleTileSwapWillBegin(int[][] swapIndexes, boolean isCenterValid);
    void puzzleConveyorBeltWillMove(Coord moveDir, int wrapIndex, int[] tileIndexes);
    void puzzleWasSolved(int tileIndex);
}
