package com.cheekymammoth.puzzleui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.cheekymammoth.gameModes.PuzzleMode;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.locale.ILocalizable;
import com.cheekymammoth.puzzleViews.PuzzleBoard;
import com.cheekymammoth.puzzles.Puzzle;
import com.cheekymammoth.utils.LangFX;

public class PuzzlePage extends Prop implements ILocalizable {
	public static final int kNumPuzzlesPerPage = 6;
	public static final int kNumPuzzlesPerRow = 3;
	public static final int kNumPuzzlesPerColumn = 2;

	@SuppressWarnings("unused")
	private static final int kHighlightLayer = 0;
	private static final int kPuzzleLayer = 1;
	private static final int kHeaderLayer = 2;
	private static final int kNumLayers = 3;
	
	private static PuzzlePageSettings settings;
	private int numPuzzlesCache;
	@SuppressWarnings("unused")
	private Label headerLabel;
	private PuzzlePageEntry[] puzzles = new PuzzlePageEntry[kNumPuzzlesPerPage];
	private Prop[] layers = new Prop[kNumLayers];
	
	public PuzzlePage() {
		this(-1);
	}

	public PuzzlePage(int category) {
		super(category);
		
		setTransform(true);
		
		settings = LangFX.getPuzzlePageSettings();
		PuzzlePageEntry.setSettings(settings);
		
		for (int i = 0, n = layers.length; i < n; i++) {
			layers[i] = new Prop();
			layers[i].setTransform(true);
			addActor(layers[i]);
		}
		
		for (int i = 0; i < kNumPuzzlesPerPage; i++) {
			PuzzleBoard board = PuzzleBoard.getPuzzleBoard(
					getCategory(),
					Puzzle.getPuzzle(Puzzle.nextPuzzleID(), "", PuzzleMode.getNumColumns(), PuzzleMode.getNumRows()),
					true);
			board.enableMenuMode(true);
			
			PuzzlePageEntry puzzleEntry = new PuzzlePageEntry(board);
			puzzleEntry.resizeBoard();
			puzzleEntry.setPosition(
					(scene.VW() - ((kNumPuzzlesPerRow-1) * settings.puzzleHorizSeparation + settings.puzzleBoardWidth)) / 2 +
					(i % kNumPuzzlesPerRow) * settings.puzzleHorizSeparation,
					scene.VH() - (settings.puzzleEntryYOffset + (i / kNumPuzzlesPerRow) * settings.puzzleVertSeparation));
			puzzles[i] = puzzleEntry;
			layers[kPuzzleLayer].addActor(puzzleEntry);
		}
	}
	
	private Vector2 worldLeftCache = new Vector2(), worldRightCache = new Vector2();
	public void resolutionDidChange() {
		float viewportWidth = 0.925f * scene.getStage().getWidth();
		if (viewportWidth == 0)
			return;
		
		float pageWidth = (kNumPuzzlesPerRow-1) * settings.puzzleHorizSeparation + settings.puzzleBoardWidth + 96;
		worldLeftCache.set(0, 0);
		worldRightCache.set(pageWidth, 0);
		worldLeftCache = this.localToStageCoordinates(worldLeftCache);
		worldRightCache = this.localToStageCoordinates(worldRightCache);
		pageWidth = worldRightCache.x - worldLeftCache.x;
		
		float pageViewportRatio = pageWidth / viewportWidth;
		if (pageViewportRatio == 0)
			return;
		
		this.setScale(1f);
		
		float minX = Float.MAX_VALUE, maxX = Float.MIN_VALUE;
		float pageScaleX = 1f / pageViewportRatio;
		float horizSeparation = Math.max(660, Math.min(1f, pageScaleX) * settings.puzzleHorizSeparation);
		for (int i = 0; i < kNumPuzzlesPerPage; i++) {
			PuzzlePageEntry puzzleEntry = puzzles[i];
			puzzleEntry.setPosition(
					(scene.VW() - ((kNumPuzzlesPerRow-1) * horizSeparation + settings.puzzleBoardWidth)) / 2 +
					(i % kNumPuzzlesPerRow) * horizSeparation,
					scene.VH() - (settings.puzzleEntryYOffset + (i / kNumPuzzlesPerRow) * settings.puzzleVertSeparation));
			Vector2 puzzleEntryMin = puzzleEntry.localToStageCoordinates(puzzleEntry.getMinPoint());
			Vector2 puzzleEntryMax = puzzleEntry.localToStageCoordinates(puzzleEntry.getMaxPoint());
			
			if (puzzleEntryMin.x < minX) minX = puzzleEntryMin.x;
			if (puzzleEntryMax.x > maxX) maxX = puzzleEntryMax.x;
		}
		
		// Only scale down and compact if the contents of the stage is larger than the stage.
//		float xDiff = (maxX - minX) - scene.getStage().getWidth();
		float temp = maxX - minX > 0 ? Math.abs(scene.getStage().getWidth() / (maxX - minX)) : 1f;
		float scaleFactor = Math.min(1f, temp);
		if (scaleFactor < 1f) scaleFactor *= 0.975f;
		
//		for (int i = 0; xDiff > 0 && i < kNumPuzzlesPerPage; i++) {
//			if (i == 0 || i == kNumPuzzlesPerRow) {
//				PuzzlePageEntry puzzleEntry = puzzles[i];
//				puzzleEntry.setX(puzzleEntry.getX() + xDiff / 2 * (1f - scaleFactor));
//			} else if (i == kNumPuzzlesPerRow - 1 || i == kNumPuzzlesPerPage - 1) {
//				PuzzlePageEntry puzzleEntry = puzzles[i];
//				puzzleEntry.setX(puzzleEntry.getX() - xDiff / 2 * (1f - scaleFactor));
//			}
//		}
		
		this.setOrigin(scene.getStage().getWidth() / 2, scene.getStage().getHeight() / 2);
		//this.setScale(this.getScaleX() * scaleFactor);
		this.setScale(1.05f * scaleFactor);
	}

	@Override
	public void localeDidChange(String fontKey, String FXFontKey) {
		settings = LangFX.getPuzzlePageSettings();
		PuzzlePageEntry.setSettings(settings);
		
		for (int i = 0; i < kNumPuzzlesPerPage; i++) {
			PuzzlePageEntry puzzleEntry = puzzles[i];
			if (puzzleEntry == null)
				continue;
			puzzleEntry.localeDidChange(fontKey, FXFontKey);
		}
		
		resolutionDidChange();
	}
	
	private boolean isIndexValid(int index) {
		return index >= 0 && index < puzzles.length && puzzles[index] != null;
	}
	
	private void refreshNumPopulatedPuzzles() {
		int numPuzzles = 0;
		for (int i = 0, n = puzzles.length; i < n; i++) {
            if (puzzles[i].isPopulated())
                numPuzzles++;
        }
        setNumPuzzles(numPuzzles);
	}
	
	public void refreshColorScheme() {
		for (int i = 0, n = puzzles.length; i < n; i++) {
			PuzzlePageEntry puzzleEntry = puzzles[i];
			if (puzzleEntry != null && puzzleEntry.getPuzzleBoard() != null)
				puzzleEntry.getPuzzleBoard().refreshColorScheme();
		}
	}
	
	public PuzzlePageEntry getEntry(int index) {
		if (index >= 0 && index < kNumPuzzlesPerPage)
			return puzzles[index];
		else
			return null;
	}
	
	public int getNumPuzzles() { return numPuzzlesCache; }
	
	private void setNumPuzzles(int value) { numPuzzlesCache = value; }
	
	public void setHeaderLabel(Label label) {
		layers[kHeaderLayer].clear();
		
		if (label != null)
			layers[kHeaderLayer].addActor(label);
		headerLabel = label;
	}
	
	public void setPuzzleAtIndex(int index, Puzzle puzzle) {
		if (!isIndexValid(index))
			return;
		
		puzzles[index].getPuzzleBoard().setData(puzzle, false);
        puzzles[index].getPuzzleBoard().enableMenuMode(true);
        
        if (puzzle == null)
        	puzzles[index].setText("");
        else {
        	String puzzleName = puzzle.getName();
        	if (puzzleName == null)
        		puzzles[index].setText("");
        	else {
        		String puzzleText = scene.localize(puzzleName);
        		puzzles[index].setText("" + (index+1) + ". " + puzzleText);
        	}
        }
        
        puzzles[index].setPopulated(puzzle != null);
    	puzzles[index].resizeBoard();
    	refreshNumPopulatedPuzzles();
	}
	
	public void setLockedAtIndex(int index, boolean locked) {
		if (!isIndexValid(index))
			return;
		puzzles[index].setLocked(locked);
	}
	
	public void setSolvedAtIndex(int index, boolean solved) {
		if (!isIndexValid(index))
			return;
		puzzles[index].setSolved(solved);
	}
	
	public void setHighlightColor(Color color) {
		for (int i = 0, n = puzzles.length; i < n; i++) {
			if (puzzles[i] != null)
				puzzles[i].setHighlightColor(color);
		}
	}
	
	public void highlightPuzzle(int index, boolean enable) {
		if (!isIndexValid(index))
			return;
		for (int i = 0, n = puzzles.length; i < n; i++) {
			if (puzzles[i] != null)
				puzzles[i].setHighlighted(false);
		}
		
		puzzles[index].setHighlighted(enable);
	}
	
	public void clear() {
		for (int i = 0, n = puzzles.length; i < n; i++) {
			if (puzzles[i] != null)
				puzzles[i].setPopulated(false);
		}
		refreshNumPopulatedPuzzles();
	}
}
