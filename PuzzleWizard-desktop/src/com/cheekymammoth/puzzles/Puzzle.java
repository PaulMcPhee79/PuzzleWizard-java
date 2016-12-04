package com.cheekymammoth.puzzles;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;
import com.cheekymammoth.events.EventDispatcher;
import com.cheekymammoth.gameModes.PuzzleMode;
import com.cheekymammoth.puzzleFactories.PlayerFactory;
import com.cheekymammoth.puzzleFactories.TileFactory;
import com.cheekymammoth.puzzleViews.IPuzzleView;
import com.cheekymammoth.puzzleViews.TilePiece;
import com.cheekymammoth.puzzleio.GameProgressController;
import com.cheekymammoth.puzzles.Player.PlayerType;
import com.cheekymammoth.sceneControllers.GameController;
import com.cheekymammoth.utils.Coord;
import com.cheekymammoth.utils.PWDebug;
import com.cheekymammoth.utils.Utils;

public final class Puzzle extends EventDispatcher implements Poolable {
	public static final int EV_TYPE_PLAYER_ADDED;
    public static final int EV_TYPE_PLAYER_REMOVED;
    public static final int EV_TYPE_UNSOLVED_PUZZLE_WILL_SOLVE;
    private static final int[] kValidators;
    
    static
    {
    	EV_TYPE_PLAYER_ADDED = EventDispatcher.nextEvType();
    	EV_TYPE_PLAYER_REMOVED = EventDispatcher.nextEvType();
    	EV_TYPE_UNSOLVED_PUZZLE_WILL_SOLVE = EventDispatcher.nextEvType();
    	
    	if (GameController.isTrialMode()) {
    		kValidators = new int[] {
    				322
    				,404849376
    				,70665312
    				,338773054
    				,409438022
    				,341395038
    				,136872790
    				,38413720
    				,373122358
    				,580659838
    				,575677924
    				,273483226
    				,272172130
    				,238478452
    				,71058556
    				,408127070
    				,240444950
    				,305996928
    				,37889356
    				,105670008
    				,343361610
    				,172270960
    				,176335166
    				,918645988
    				,137266090
    				,305341512
    				,172270980
    				,171222110
    				,71058598
    				,206489048
    				,5768788
    				,7210928
    				,472761222
    				,141723754
    				,478136618
    				,510912598
    		    };
    	} else {
    		kValidators = new int[] {
    				322
    				,404849376
    				,70665312
    				,338773054
    				,409438022
    				,341395038
    				,136872790
    				,38413720
    				,373122358
    				,580659838
    				,575677924
    				,273483226
    				,272172130
    				,238478452
    				,71058556
    				,408127070
    				,240444950
    				,305996928
    				,37889356
    				,105670008
    				,343361610
    				,172270960
    				,176335166
    				,918645988
    				,137266090
    				,305341512
    				,172270980
    				,171222110
    				,71058598
    				,206489048
    				,5768788
    				,7210928
    				,472761222
    				,141723754
    				,478136618
    				,510912598
    				,370762384
    				,674661434
    				,508683844
    				,5768826
    				,340608540
    				,307832468
    				,2622250
    				,141330342
    				,72238536
    				,373384496
    				,476301090
    				,206882356
    				,143165856
    				,146574448
    				,307570212
    				,178563898
    				,478529872
    				,247655762
    				,74729526
    				,242280484
    				,177121792
    				,342050692
    				,344541648
    				,408913656
    				,4720000
    				,106981100
    				,137003984
    				,275974266
    				,314780954
    				,73025232
    				,439723068
    				,576202340
    				,645949654
    				,205833542
    				,580922020
    				,611862682
    		    };
    	}
    	
//    	for (int i = 0; i < kValidators.length; i++) {
//    		for (int j = 0; j < kValidators.length; j++) {
//    			if (i != j && kValidators[i] == kValidators[j])
//    				Gdx.app.log("DUPLICATE FOUND", "" + kValidators[i]);
//    		}
//    	}
    }
    
    private static int s_NextPuzzleID = 1;
    private int[] kColorSwirlIndexOffsets; // Treat as final
    
    protected boolean isSolved;
    protected boolean isRotating;
    protected boolean isTileSwapping;
    protected boolean isConveyorBeltActive;
    protected boolean isResetting;
    protected boolean paused;
    protected boolean isSearchWeightingEnabled = true;
    private int ID;
    private int IQ = 100;
    private int levelIndex = -1;
    private int puzzleIndex = -1;
    private String name;
    private int numColumns;
    private int numRows;
    private Tile[] tiles;
    private Array<Player> players;
    private Player[] queuedCommands = new Player[10];
    private int shieldIndex;
    private int[][] rotationIndexes = new int[2][9];
    private int[][] tileSwapIndexes = new int[2][9];

    private Coord conveyorBeltDir = new Coord();
    private int conveyorBeltWrapIndex = -1;
    private int[] vertConveyorBeltIndexes;
    private int[] horizConveyorBeltIndexes;

    private ColorFlooder colorFlooder;
    private Array<IPuzzleView> views = new Array<IPuzzleView>(true, 1, IPuzzleView.class);
	private IntIntMap commandPriorities = new IntIntMap(12);
    
	public Puzzle() {
		PWDebug.puzzleCount++;
		initCommon();
	}
	
	public static Puzzle getPuzzle(int ID) {
		Puzzle puzzle = Pools.obtain(Puzzle.class);
		puzzle.ID = ID;
		return puzzle;
	}
	
	public static Puzzle getPuzzle(int ID, String name, int numColumns, int numRows) {
		Puzzle puzzle = Pools.obtain(Puzzle.class);
		
		int oldNumCols = puzzle.numColumns, oldNumRows = puzzle.numRows;
		puzzle.name = name;
		puzzle.numColumns = numColumns;
		puzzle.numRows = numRows;
		
		if (numColumns != oldNumCols || numRows != oldNumRows) {
			puzzle.tiles = new Tile[numColumns * numRows];
			puzzle.vertConveyorBeltIndexes = new int[numRows];
			puzzle.horizConveyorBeltIndexes = new int[numColumns];
		}
		
		Tile[] tiles = puzzle.tiles;
		for (int i = 0, n = tiles.length; i < n; i++) {
			tiles[i] = TileFactory.getTile();
			tiles[i].setEdgeTile(i >= numColumns * (numRows-1));
		}
		
		if (puzzle.players == null)
			puzzle.players = new Array<Player>(true, 2, Player.class);
		
		return puzzle;
	}
	
	public static void freePuzzle(Puzzle puzzle) {
		if (puzzle != null)
			Pools.free(puzzle);
	}
	
//	public Puzzle(int ID, String name, int numColumns, int numRows) {
//		initCommon();
//		this.ID = ID;
//		this.name = name;
//		this.numColumns = numColumns;
//		this.numRows = numRows;
//		
//		tiles = new Tile[numColumns * numRows];
//		vertConveyorBeltIndexes = new int[numRows];
//		horizConveyorBeltIndexes = new int[numColumns];
//		players = new Array<Player>(true, 2, Player.class);
//		
//		for (int i = 0, n = tiles.length; i < n; i++) {
//			tiles[i] = new Tile();
//			
//			if (i >= numColumns * (numRows-1))
//				tiles[i].setEdgeTile(true);
//		}
//	}
//	
//	public Puzzle(BufferedInputStream stream) throws IOException {
//		initCommon();
//		decodeFromStream(stream);
//	}
	
	protected void initCommon() {
		kColorSwirlIndexOffsets = PuzzleMode.getColorSwirlIndexOffsets();
		createCommandPriorities();
	}
	
	protected void createCommandPriorities() {
		int[] commands = new int[] {
				Tile.kTFKey, Tile.kTFTileSwap, Tile.kTFMirroredImage, Tile.kTFShield,
				Tile.kTFTeleport, Tile.kTFColorSwap, Tile.kTFColorSwirl, Tile.kTFPainter,
				Tile.kTFRotate, Tile.kTFConveyorBelt, Tile.kTFColorFlood, Tile.kTFColorMagic
			};
			
		for (int i = 0, n = commands.length; i < n; i++)
			commandPriorities.put(commands[i], i+1);
	}

	public boolean isSolved() { return isSolved; }
	
	private void setSolved(boolean value) { isSolved = value; }
	
	public boolean isResetting() { return isResetting; }
	
	protected boolean isPaused() { return paused; }
	
	public boolean isRotating() { return isRotating; }
	
	private void setRotating(boolean value) { isRotating = value; }
	
	public boolean isConveyorBeltActive() { return isConveyorBeltActive; }
	
	private void setConveyorBeltActive(boolean value) { isConveyorBeltActive = value; }
	
	public boolean isTileSwapping() { return isTileSwapping; }
	
	private void setTileSwapping(boolean value) { isTileSwapping = value; }
	
	public int getID() { return ID; }
	
	public int getIQ() { return IQ; }
	
//	private void setIQ(int value) { IQ = value; }
	
	public int getPuzzleIndex() { return puzzleIndex; }
	
	public void setPuzzleIndex(int value) { puzzleIndex = value; }
	
	public int getLevelIndex() { return levelIndex; }
	
	public void setLevelIndex(int value) { levelIndex = value; }
	
	public String getName() { return name; }
	
	void setName(String value) { name = value; }
	
	public int getNumColumns() { return numColumns; }
	
	public int getNumRows() { return numRows; }
	
	public int getNumTiles() { return getNumColumns() * getNumRows(); }
	
	public Tile[] getTiles() { return tiles; }
	
	public int getNumPlayers() { return players != null ? players.size : 0; }
	
	public Array<Player> getPlayers() { return players; }
	
	protected Array<HumanPlayer> getHumanPlayers() {
		Array<HumanPlayer> humanPlayers = new Array<HumanPlayer>(true, players.size, HumanPlayer.class);
		for (int i = 0, n = players.size; i < n; i++) {
			Player player = players.get(i);
			if (player.getType() == PlayerType.HUMAN)
				humanPlayers.add((HumanPlayer)player);
		}
		return humanPlayers;
	}
	
	public int[][] getRotationIndexes() { return rotationIndexes; }
	
	public int[][] getTileSwapIndexes() { return tileSwapIndexes; }
	
	public int getShieldIndex() { return shieldIndex; }
	
	private void setShieldIndex(int value) { shieldIndex = value; }
	
	public Coord getConveyorBeltDir() { return conveyorBeltDir; }
	
	public int getConveyorBeltWrapIndex() { return conveyorBeltWrapIndex; }
	
	private void setConveyorBeltWrapIndex(int value) { conveyorBeltWrapIndex = value; }
	
	public int[] getConveyorBeltIndexes() {
		if (conveyorBeltDir.x != 0)
			return horizConveyorBeltIndexes;
		else if (conveyorBeltDir.y != 0)
			return vertConveyorBeltIndexes;
		else
			return null;
	}
	
	public static int nextPuzzleID() { return s_NextPuzzleID++; }
	
	public boolean isValidIndex(int index) {
		return index >= 0 && index < getNumTiles();
	}
	
	public boolean isPerimeterIndex(int index)
    {
        int row = rowForIndex(index), column = columnForIndex(index);
        return row == 0 || row == getNumRows() - 1 || column == 0 || column == getNumColumns() - 1;
    }
	
	public boolean isValidPos(float x, float y) {
        return x >= 0 && x < getNumColumns() && y >= 0 && y < getNumRows();
    }
	
	public boolean isValidPos(Coord pos) {
        return isValidPos(pos.x, pos.y);
    }

    public int pos2Index(Coord pos) {
        return pos.x + pos.y * getNumColumns();
    }

    public Coord index2Pos(int index) {
        return new Coord(index % getNumColumns(), index / getNumColumns());
    }
    
    public void index2Pos(int index, Coord c) {
        c.set(index % getNumColumns(), index / getNumColumns());
    }

    public boolean isModified(int modifier, int originIndex, int numColumns, int numRows) {
        for (int i = 0; i < numColumns; ++i) {
            for (int j = 0; j < numRows; ++j) {
                int index = originIndex + i * getNumColumns() + j;
                if (isValidIndex(index)) {
                    Tile tile = tileAtIndex(index);
                    if (tile.isModified(modifier))
                        return true;
                }
            }
        }

        return false;
    }

    private boolean isIndexAxiallyAdjacent(int index, int other) {
        int indexColumn = columnForIndex(index), indexRow = rowForIndex(index);
        int otherColumn = columnForIndex(other), otherRow = rowForIndex(other);
        return otherColumn >= 0 && otherColumn < getNumColumns() && otherRow >= 0 && otherRow < getNumRows()
            && (
                ((indexColumn - otherColumn) == 0 && Math.abs(indexRow - otherRow) == 1)
            || ((indexRow - otherRow) == 0 && Math.abs(indexColumn - otherColumn) == 1));
    }

    private boolean isIndexSurroundedBy(int index, int other) {
        int indexColumn = columnForIndex(index), indexRow = rowForIndex(index);
        int otherColumn = columnForIndex(other), otherRow = rowForIndex(other);
        return otherColumn >= 0 && otherColumn < getNumColumns() && otherRow >= 0 && otherRow < getNumRows()
            && Math.abs(indexColumn - otherColumn) <= 1 && Math.abs(indexRow - otherRow) <= 1;
    }

	public boolean doesAdjacentPerpIndexWrap(int index, int adjacentIndex) {
		return (index % getNumColumns() == 0 && adjacentIndex == index-1) || 
				(adjacentIndex % getNumColumns() == 0 && adjacentIndex == index+1);
	}
	
	public Player playerAtIndex(int index) {
        if (players != null && index >= 0 && index < getNumTiles()) {
        	for (int i = 0, n = players.size; i < n; i++) {
        		Player player = players.get(i);
        		if (pos2Index(player.getPosition()) == index)
        			return player;
        	}
        }

        return null;
    }
	
	public HumanPlayer getAnyHumanPlayer() {
		if (players != null) {
			for (int i = 0, n = players.size; i < n; i++) {
				Player player = players.get(i);
				if (player.getType() == PlayerType.HUMAN)
					return (HumanPlayer)player;
			}
		}
		
		return null;
	}
	
	public void addPlayer(Player player) {
        if (player != null && players != null && !players.contains(player, true)) {
            if (player.getType() == PlayerType.HUMAN)
                players.add(player);
            else {
                // We process moves in reverse order, but we must process
            	// human players prior to their mirrored counterparts.
                players.insert(0, player);
            }
            
            dispatchEvent(EV_TYPE_PLAYER_ADDED, player);
        }
    }
	
	public void removePlayer(Player player) {
        if (player != null && players != null && players.contains(player, true)) {
            players.removeValue(player, true);
            dispatchEvent(EV_TYPE_PLAYER_REMOVED, player);
            PlayerFactory.freePlayer(player);
        }
    }
	
	public void removePlayerAtIndex(int index) {
        if (players == null || index < 0 || index >= getNumTiles())
            return;

        Player removeMe = null;

        for (int i = 0, n = players.size; i < n; i++) {
        	Player player = players.get(i);
        	if (pos2Index(player.getPosition()) == index) {
        		removeMe = player;
                break;
        	}
        }

        if (removeMe != null)
            removePlayer(removeMe);
    }
	
	public int rowForIndex(int index) {
		if (tiles == null || !isValidIndex(index))
            return -1;
        else
            return index / getNumColumns();
	}
	
	public int columnForIndex(int index) {
        if (tiles == null || !isValidIndex(index))
            return -1;
        else
            return index % getNumColumns();
    }
	
	public int indexForTile(Tile tile) {
		if (tiles == null || tile == null)
            return -1;

        for (int i = 0, n = tiles.length; i < n; ++i)
        {
            if (tiles[i] == tile)
                return i;
        }

        return -1;
	}
	
	public Tile tileAtIndex(int index) {
        if (tiles != null && isValidIndex(index))
            return tiles[index];
        else
            return null;
    }

    protected Tile tileForTile(Tile tile) {
        if (tiles == null || tile == null)
            return null;
        
        for (int i = 0, n = tiles.length; i < n; ++i) {
        	Tile t = tiles[i];
        	if (t != tile && t.getFunctionKey() == tile.getFunctionKey() && t.getFunctionID() == tile.getFunctionID())
                return t;
        }
        
        return null;
    }
    
    public Array<Tile> getTileRange(int originIndex, int numColumns, int numRows) {
        if (tiles == null || originIndex >= tiles.length || numColumns <= 0 || numRows <= 0)
            return new Array<Tile>(true, 1, Tile.class);

        originIndex = Math.max(0, originIndex);
        int legalRangeX = Math.min(numColumns, getNumColumns() - columnForIndex(originIndex)),
        		legalRangeY = Math.min(numRows, getNumRows() - rowForIndex(originIndex));
        Array<Tile> rangeTiles = new Array<Tile>(legalRangeX * legalRangeY);

        for (int y = 0; y < legalRangeY; ++y) {
            int xOffset = originIndex + y * getNumColumns();
            for (int x = 0; x < legalRangeX; ++x)
            	rangeTiles.add(tiles[xOffset + x]);
        }

        return rangeTiles;
    }
    
    protected void enableShield(int index, boolean enable) {
        Tile centerTile = tileAtIndex(index);
        if (centerTile != null) {
            if (enable)
                centerTile.setModifiers(centerTile.getModifiers() | Tile.kTFShield);
            else
            	centerTile.setModifiers(centerTile.getModifiers() & ~Tile.kTFShield);
        }

        int topLeftShieldIndex = index - (getNumColumns() + 1);
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                int shieldIndex = topLeftShieldIndex + i * getNumColumns() + j;
                if (isIndexSurroundedBy(index, shieldIndex)) { // Prevent invalid tiles and wrap arounds
                    Tile shieldTile = tileAtIndex(shieldIndex);

                    if (enable)
                    	shieldTile.setModifiers(shieldTile.getModifiers() | Tile.kTMShielded);
                    else
                    	shieldTile.setModifiers(shieldTile.getModifiers() & ~Tile.kTMShielded);
                }
            }
        }
    }
    
    protected void refreshBoardFunction(int function) {
        switch (function) {
            case Tile.kTFShield: {
            	for (int i = 0, n = tiles.length; i < n; i++) {
            		Tile tile = tiles[i];
            		if (tile.getFunctionKey() == Tile.kTFShield && tile.isModified(Tile.kTFShield))
                        enableShield(indexForTile(tile), true);
            	}
            }
                break;
        }
    }
    
    protected void recolorPlayers() {
        // Make sure all players are legally/validly positioned.
    	for (int i = 0, n = players.size; i < n; i++) {
    		Player p = players.get(i);
    		int pIndex = pos2Index(p.getPosition());
            Tile t = tiles[pIndex];
            if (p.getColorKey() != t.getColorKey())
                p.setColorKey(t.getColorKey());
    	}
    }
    
    public boolean isPlayerOccupied(Player player) {
        if (player == null || (!isRotating && !isConveyorBeltActive))
            return false;

        boolean isOccupied = false;
        int tileIndex = pos2Index(player.getPosition());

        if (isRotating()) {
            for (int i = 0; i < 9; ++i) {
                if (tileIndex == rotationIndexes[0][i]) {
                    isOccupied = true;
                    break;
                }
            }
        }

        if (isConveyorBeltActive()) {
            int[] beltIndexes = getConveyorBeltIndexes();
            for (int i = 0; i < beltIndexes.length; ++i) {
                if (tileIndex == beltIndexes[i]) {
                    isOccupied = true;
                    break;
                }
            }
        }

        return isOccupied;
    }
    
    public void enableSearchWeighting(boolean enable) { isSearchWeightingEnabled = enable; }
    
    public int getSearchWeighting(int index) {
    	// TODO
    	return 0;
    }
    
    protected static Tile s_RotateSwapTile, s_RotateCacheTile;
    public void applyRotation() {
        if (!isRotating())
            return;
       
        if (s_RotateSwapTile == null) s_RotateSwapTile = new Tile();
        if (s_RotateCacheTile == null) s_RotateCacheTile = new Tile();
        Tile swap = s_RotateSwapTile, cache = s_RotateCacheTile;

        for (int i = 0; i < 8; ++i) {
            Tile rotateToTile = tileAtIndex(rotationIndexes[1][i + 1]);
            swap.copy(rotateToTile);
            rotateToTile.copy(i == 0 ? tileAtIndex(rotationIndexes[0][i + 1]) : cache);
            cache.copy(swap);
        }

        for (int i = 0, n = players.size; i < n; i++) {
        	Player player = players.get(i);
            int playerIndex = pos2Index(player.getPosition());
            player.setQueuedMove(0,  0);

            for (int j = 0; j < 8; ++j) {
                if (playerIndex == rotationIndexes[0][j + 1]) {
                	Coord c = Coord.obtainCoord();
                	index2Pos(rotationIndexes[1][j + 1], c);
                    player.setQueuedMove(c);
                    Coord.freeCoord(c);
                    break;
                }
            }
        }

        for (int i = 0, n = players.size; i < n; i++) {
        	Player player = players.get(i);
            Coord queuedMove = player.getQueuedMove();
            if (queuedMove.x != 0 || queuedMove.y != 0) {
            	player.silentMoveTo(queuedMove);
            	player.setQueuedMove(0, 0);
            }
        }

        setRotating(false);
    }
    
    public void applyTileSwap() {
        if (!isTileSwapping())
            return;

        setTileSwapping(false);
    }
    
    protected static Tile s_ConveyorBeltWrapTile;
    public void applyConveyorBelt() {
        if (!isConveyorBeltActive())
            return;

        Coord ori = getConveyorBeltDir();
        assert(Math.abs(ori.x + ori.y) == 1) : "Invalid Puzzle.ConveyorBeltDir orientation [" + ori.x + "," + ori.y + "].";
        assert(isValidIndex(getConveyorBeltWrapIndex())) : "Invalid Puzzle.ConveyorBeltWrapIndex " + getConveyorBeltWrapIndex();

        if (s_ConveyorBeltWrapTile == null) s_ConveyorBeltWrapTile = new Tile();
        Tile wrapTile = s_ConveyorBeltWrapTile;
        int[] beltIndexes = getConveyorBeltIndexes();

        int dir = ori.x != 0 ? ori.x : ori.y;

        if (dir == 1) {
            wrapTile.copy(tileAtIndex(beltIndexes[getConveyorBeltWrapIndex()]));
            for (int i = beltIndexes.length-1; i > 0; --i)
                tileAtIndex(beltIndexes[i]).copy(tileAtIndex(beltIndexes[i-1]));
            tileAtIndex(beltIndexes[0]).copy(wrapTile);
        } else {
            wrapTile.copy(tileAtIndex(beltIndexes[getConveyorBeltWrapIndex()]));
            for (int i = 0; i < beltIndexes.length - 1; ++i)
                tileAtIndex(beltIndexes[i]).copy(tileAtIndex(beltIndexes[i + 1]));
            tileAtIndex(beltIndexes[beltIndexes.length - 1]).copy(wrapTile);
        }

        Coord temp = Coord.obtainCoord();
        for (int i = 0, n = players.size; i < n; i++) {
        	Player player = players.get(i);
            int playerIndex = pos2Index(player.getPosition());
            // Use -1, -1 because this queued move is used as a tile index; not as a delta.
            player.setQueuedMove(-1, -1);

            for (int j = 0, m = beltIndexes.length; j < m; j++) {
                if (playerIndex == beltIndexes[j]) {
                    if (dir == 1) {
                    	index2Pos(beltIndexes[(j + 1) % beltIndexes.length], temp);
                    	player.setQueuedMove(temp);
                    } else {
                    	index2Pos(beltIndexes[j > 0 ? j - 1 : beltIndexes.length-1], temp);
                    	player.setQueuedMove(temp);
                    }
                    break;
                }
            }
        }
        Coord.freeCoord(temp);

        for (int i = 0, n = players.size; i < n; i++) {
        	Player player = players.get(i);
            Coord queuedMove = player.getQueuedMove();
            if (queuedMove.x != -1 && queuedMove.y != -1)
            	player.silentMoveTo(queuedMove);

            player.setQueuedMove(0, 0);
        }

        setConveyorBeltActive(false);
    }
    
    protected void tryMove(Player player) {
    	final int kMoveTypeIdle = 0, kMoveTypeTreadmill = 1, kMoveTypeMove = 2;
    	
    	int moveType = kMoveTypeIdle;
    	Coord queuedMove = null, currentPos = null, newPos = null;
    	do {
    		if (player == null || player.isMoving() || isRotating() || isTileSwapping() || isConveyorBeltActive())
                break;

            queuedMove = player.getQueuedMove();
            if (queuedMove.x == 0 && queuedMove.y == 0)
            	break;
            
            moveType = kMoveTypeTreadmill;

            currentPos = player.getPosition();
            newPos = Coord.obtainCoord(currentPos.x + queuedMove.x, currentPos.y + queuedMove.y);

            if (isValidPos(newPos)) {
    	        int index = pos2Index(newPos);
    	
    	        if (isValidIndex(index) && isIndexAxiallyAdjacent(pos2Index(currentPos), index)) {
    	            Tile tile = tileAtIndex(index);
    	            if (tile.getColorKey() == player.getColorKey() || tile.getColorKey() == TilePiece.kColorKeyWhite ||
    	                    player.getColorKey() == TilePiece.kColorKeyWhite ||
    	                    tile.getColorKey() == TilePiece.kColorKeyMulti || player.isColorMagicActive()) 
    	            {
    	                move(player, newPos);
    	                moveType = kMoveTypeMove;
    	            }
    	        }
            }
    	} while(false);
    	
    	if (player != null) {
    		if (moveType != kMoveTypeMove)
    			player.idle();
    		if (moveType == kMoveTypeTreadmill && queuedMove != null)
    			treadmill(player, queuedMove);
    		if (moveType == kMoveTypeMove)
    			puzzlePlayerWillMove(player);
    	}
    	
    	Coord.freeCoord(newPos);
    }
    
    protected void treadmill(Player player, Coord dir) {
    	if (player != null && dir != null)
    		player.treadmill(dir.x, dir.y);
    }
    
    protected void move(Player player, Coord pos) {
        if (player == null || pos == null)
            return;
        player.setFutureColorKey(tileAtIndex(pos2Index(pos)).getColorKey());
        player.beginMoveTo(pos);
    }
	
    protected int getCommandPriority(int command) {
    	return commandPriorities.get(command, -1);
    }
    
    protected void addQueuedCommand(Player player) {
        if (player == null)
            return;

        for (int i = 0, n = queuedCommands.length; i < n; i++) {
            if (queuedCommands[i] == null) {
                queuedCommands[i] = player;
                break;
            }

            if (getCommandPriority(player.getFunction()) < getCommandPriority(queuedCommands[i].getFunction())) {
                if (i == queuedCommands.length - 1)
                    queuedCommands[i] = player;
                else {
                    Player swap = queuedCommands[i];
                    queuedCommands[i] = player;
                    queuedCommands[i+1] = swap;
                }

                break;
            }
        }
    }
    
    protected void playerMoved(Player player) {
        Coord pos = player.getPosition();
        int index = pos2Index(pos);
        Tile tile = tileAtIndex(index);

        player.setFunction(tile.getFunctionKey());

        if (player.getFunction() != Tile.kTFNone)
            addQueuedCommand(player);
        puzzlePlayerDidMove(player);
    }
    
    protected static Tile[] s_tsSrcTiles = new Tile[9], s_tsDestTiles = new Tile[9];
    protected static Tile[] s_tsInvalidate = new Tile[] { null, null, null, null, null, null, null, null, null };
    protected void processQueuedCommands() {
    	Coord tempCoord = Coord.obtainCoord();
    	for (int commandIndex = 0, commandCount = queuedCommands.length; commandIndex < commandCount; commandIndex++) {
    		Player player = queuedCommands[commandIndex];
            if (player == null)
                break;

            Coord pos = player.getPosition();
            int index = pos2Index(pos);
            Tile tile = tileAtIndex(index);

            if (tile.getColorKey() == TilePiece.kColorKeyMulti && !tile.isModified(Tile.kTMShielded))
                tile.setColorKey(player.getColorKey());
            if (tile.getColorKey() == TilePiece.kColorKeyWhite && player.getColorKey() != TilePiece.kColorKeyWhite)
                player.setColorKey(tile.getColorKey());
            if (player.getColorKey() == TilePiece.kColorKeyWhite && tile.getColorKey() != TilePiece.kColorKeyWhite &&
            		tile.getColorKey() != TilePiece.kColorKeyMulti)
                player.setColorKey(tile.getColorKey());

            if (tile.getFunctionKey() != player.getFunction())
                continue;

            switch (player.getFunction()) {
                case Tile.kTFTeleport:
                    {
                        Tile teleportTile = tileForTile(tile);
                        if (teleportTile != null) {
                        	index2Pos(indexForTile(teleportTile), tempCoord);
                            player.teleportTo(tempCoord);
                            player.setColorKey(teleportTile.getColorKey());
                            puzzlePlayerDidMove(player);
                        }
                    }
                    break;
                case Tile.kTFColorSwap:
                    {
                        boolean didColorSwap = false;
                        int colorKeyLeft, colorKeyRight;
                        TilePiece.setColorKeysForSwapKey(tile.getDecorationStyleKey(), tempCoord);
                        colorKeyLeft = tempCoord.x; colorKeyRight = tempCoord.y;
                        
                        for (int i = 0, n = tiles.length; i < n; i++) {
                        	Tile t = tiles[i];
                            if (t.isModified(Tile.kTMShielded))
                                continue;
                            if (t.getColorKey() == colorKeyLeft) {
                                t.setDecorator(tile.getFunctionKey());
                                t.setColorKey(colorKeyRight);
                                didColorSwap = true;
                            } else if (t.getColorKey() == colorKeyRight) {
                                t.setDecorator(tile.getFunctionKey());
                                t.setColorKey(colorKeyLeft);
                                didColorSwap = true;
                            }
                        }

                        //RecolorPlayers();
                        if (didColorSwap)
                            puzzleSoundShouldPlay("color-swap");
                    }
                    break;
                case Tile.kTFRotate:
                    {
                        if (isRotating())
                            break;

                        boolean locked = false;
                        do {
                            // Prevent a rotation tile from rotating tiles off the board
                            if (isPerimeterIndex(index)) {
                                locked = true;
                                break;
                            }

                            int rotateFromIndex = index - (getNumColumns() + 1), rotateToIndex = 0;

                            rotationIndexes[0][0] = index;
                            rotationIndexes[1][0] = index;

                            // Cannot rotate if any tiles are shielded
                            if (isModified(Tile.kTMShielded, rotateFromIndex, 3, 3)) {
                                locked = true;
                                break;
                            }

                            // ACW from top left
                            for (int i = 0; i < 8; ++i) {
                                switch (i) {
                                    case 0: rotateToIndex = rotateFromIndex + getNumColumns(); break;
                                    case 1: rotateToIndex = rotateFromIndex + getNumColumns(); break;
                                    case 2: rotateToIndex = rotateFromIndex + 1; break;
                                    case 3: rotateToIndex = rotateFromIndex + 1; break;
                                    case 4: rotateToIndex = rotateFromIndex - getNumColumns(); break;
                                    case 5: rotateToIndex = rotateFromIndex - getNumColumns(); break;
                                    case 6: rotateToIndex = rotateFromIndex - 1; break;
                                    case 7: rotateToIndex = rotateFromIndex - 1; break;
                                }

                                rotationIndexes[0][i+1] = rotateFromIndex;
                                rotationIndexes[1][i+1] = rotateToIndex;
                                rotateFromIndex = rotateToIndex;
                            }
                        }
                        while (false);

                        // Lock if we're moving the conveyor belt and any tiles overlap.
                        if (!locked && isConveyorBeltActive()) {
                            int[] beltIndexes = getConveyorBeltIndexes();
                            for (int i = 0, n = beltIndexes.length; i < n; i++) {
                                for (int j = 0; j < 9; j++) {
                                    if (beltIndexes[i] == rotationIndexes[0][j]) {
                                        locked = true;
                                        break;
                                    }
                                }
                            }
                        }

                        if (!locked) {
                            setRotating(true);
                            puzzleTilesShouldRotate(rotationIndexes[0]);
                        }
                        else
                            puzzleSoundShouldPlay("locked");
                    }
                    break;
                case Tile.kTFShield:
                    boolean enable = !tile.isModified(Tile.kTFShield);
                    enableShield(index, enable);
                    refreshBoardFunction(Tile.kTFShield);
                    setShieldIndex(index);

                    if (enable)
                        puzzleShieldDidDeploy(getShieldIndex());
                    else
                        puzzleShieldWasWithdrawn(getShieldIndex());
                    break;
                case Tile.kTFPainter:
                    {
                        int painter = tile.getPainter();
                        int paintCount = 0;

                        for (int i = 0; i < 4; ++i) { // 4 directions
                            int colorKey = (painter >> (i * 4)) & Tile.kColorKeyMask;
                            if (colorKey == TilePiece.kColorKeyNone)
                                continue;

                            int paintIndex = index;
                            boolean endWhile = !isValidIndex(paintIndex);

                            int decoratorIndex = 0;
                            while (!endWhile) {
                                switch (i) {
                                    case 0:
                                        paintIndex = paintIndex - getNumColumns();
                                        break;
                                    case 1:
                                        paintIndex = paintIndex + 1;
                                        endWhile = paintIndex % getNumColumns() == 0;
                                        break;
                                    case 2:
                                        paintIndex = paintIndex + getNumColumns();
                                        break;
                                    case 3:
                                        paintIndex = paintIndex - 1;
                                        endWhile = paintIndex % getNumColumns() == getNumColumns() - 1;
                                        break;
                                }

                                if (!endWhile)
                                    endWhile = !isValidIndex(paintIndex);

                                if (!endWhile) {
                                    Tile paintTile = tileAtIndex(paintIndex);
                                    if (!paintTile.isModified(Tile.kTMShielded) && paintTile.getColorKey() != colorKey) {
                                        paintTile.setDecorator(tile.getFunctionKey());
                                        paintTile.setDecoratorData(decoratorIndex);
                                        paintTile.setColorKey(colorKey);
                                        ++decoratorIndex;
                                        ++paintCount;
                                    }
                                }
                            }
                        }

                        //RecolorPlayers();
                        if (paintCount > 0)
                            puzzleSoundShouldPlay(paintCount < 3 ? "color-arrow-short" : (paintCount < 6 ? "color-arrow-medium" : "color-arrow"));
                    }
                    break;
                case Tile.kTFTileSwap: 
                {
                        if (isTileSwapping()) {
                            assert(false) :"Attempt to TileSwap while busy TileSwapping.";
                            break;
                        }

                        // Must cache original tile states in case there is an overlap.
                        boolean isCenterValid = true;
                        int numSwaps = 0;
                        Tile[] srcTiles = s_tsSrcTiles, destTiles = s_tsDestTiles;
                        Tile destTile = tileForTile(tile), swapTile = TileFactory.getTile();
                        if (destTile != null) {
                            int destIndex = indexForTile(destTile);
                            int srcRow = rowForIndex(index), srcColumn = columnForIndex(index);
                            int destRow = rowForIndex(destIndex), destColumn = columnForIndex(destIndex);

                            resetTileSwapIndexes();

                            for (int i = 0; i < 3; ++i) {
                                // Verify rows
                                int srcRowIter = srcRow + i - 1;
                                if (srcRowIter < 0 || srcRowIter >= getNumRows())
                                    continue;

                                int desRowIter = destRow + i - 1;
                                if (desRowIter < 0 || desRowIter >= getNumRows())
                                    continue;

                                for (int j = 0; j < 3; ++j) {
                                    // Verify columns
                                    int srcColumnIter = srcColumn + j - 1;
                                    if (srcColumnIter < 0 || srcColumnIter >= getNumColumns())
                                        continue;

                                    int destColumnIter = destColumn + j - 1;
                                    if (destColumnIter < 0 || destColumnIter >= getNumColumns())
                                        continue;

                                    // Fetch verified tiles
                                    Tile srcTile = tiles[srcRowIter * getNumColumns() + srcColumnIter];
                                    destTile = tiles[desRowIter * getNumColumns() + destColumnIter];

                                    if (srcTile.isModified(Tile.kTMShielded) || destTile.isModified(Tile.kTMShielded)) {
                                        if (srcTile == tile) {
                                            // Set the center tile to be available for positioning in the view.
                                            tileSwapIndexes[0][3 * i + j] = indexForTile(srcTile);
                                            tileSwapIndexes[1][3 * i + j] = indexForTile(destTile);
                                            isCenterValid = false;
                                        }

                                        continue;
                                    }

                                    // Cache verified tiles
                                    srcTiles[3 * i + j] = srcTile.clone();
                                    destTiles[3 * i + j] = destTile.clone();

                                    // Cache tile swap indexes for view clients
                                    tileSwapIndexes[0][3 * i + j] = indexForTile(srcTile);
                                    tileSwapIndexes[1][3 * i + j] = indexForTile(destTile);
                                    ++numSwaps;
                                }
                            }

                            if (numSwaps == 0)
                                break;

                            puzzleTileSwapWillBegin(tileSwapIndexes, isCenterValid);
                            setTileSwapping(true);

                            // Shift cached tiles
                            for (int i = 0; i < 3; i++) {
                                int srcRowIter = srcRow + i - 1;
                                int desRowIter = destRow + i - 1;

                                for (int j = 0; j < 3; j++) {
                                    int srcColumnIter = srcColumn + j - 1;
                                    int destColumnIter = destColumn + j - 1;

                                    if (srcTiles[3 * i + j] != null && destTiles[3 * i + j] != null) {
                                        Tile srcTile = tiles[srcRowIter * getNumColumns() + srcColumnIter];
                                        destTile = tiles[desRowIter * getNumColumns() + destColumnIter];
                                        srcTile.copy(destTiles[3 * i + j]);
                                        destTile.copy(srcTiles[3 * i + j]);
                                    }
                                }
                            }
                        }
                        
                        TileFactory.freeTiles(srcTiles);
                        TileFactory.freeTiles(destTiles);
                        TileFactory.freeTile(swapTile);
                        System.arraycopy(s_tsInvalidate, 0, srcTiles, 0, s_tsInvalidate.length);
                        System.arraycopy(s_tsInvalidate, 0, destTiles, 0, s_tsInvalidate.length);
                    }
                    break;
                case Tile.kTFMirroredImage:
                    {
                        Tile mirroredTile = tileForTile(tile);
                        if (mirroredTile != null && player.getType() == PlayerType.HUMAN) {
                            HumanPlayer humanPlayer = (HumanPlayer)player;
                            if (humanPlayer.getMirrorImage() != null)
                                removePlayer(humanPlayer.getMirrorImage());
                            MirroredPlayer mirroredPlayer = PlayerFactory.getMirroredPlayer(
                            		mirroredTile.getColorKey(),
                            		index2Pos(indexForTile(mirroredTile)),
                            		Player.getMirroredOrientation(player.getOrientation()));
                            humanPlayer.setMirrorImage(mirroredPlayer);
                            addPlayer(mirroredPlayer);

                            puzzleSoundShouldPlay("mirrored-self");
                        }
                    }
                    break;
                case Tile.kTFKey:
                    {
                        if (!isSolved()) {
                            puzzleSoundShouldPlay("solved");
                            setSolved(true);
                            
                            GameProgressController.GPC().setSolved(isSolved, levelIndex, puzzleIndex);
                            GameProgressController.GPC().save();
                            puzzleWasSolved(indexForTile(tile));
                        }
                    }
                    break;
                case Tile.kTFColorFlood:
                    {
                        if (tile.isModified(Tile.kTMShielded))
                            break;

                        int prevColorKey = tile.getColorKey(), nextColorKey = tile.getPainter() & Tile.kColorKeyMask;
                        if (prevColorKey != nextColorKey && nextColorKey != 0) {
                            tile.setDecorator(Tile.kTFColorFlood);
                            tile.setDecoratorData(0);
                            tile.setColorKey(nextColorKey);
                            
                            if (colorFlooder == null)
                            	colorFlooder = new ColorFlooder();
                            
                            int paintCount = colorFlooder.fill(this, tile, prevColorKey);
                            if (paintCount > 0)
                            	puzzleSoundShouldPlay(paintCount < 20 ? "color-flood-short" : 
                            		(paintCount < 40 ? "color-flood-medium" : "color-flood"));
                        }
                    }
                    break;
                case Tile.kTFColorSwirl:
                    {
                        int paintCount = 0, decoratorIndex = 0;

                        int topLeftIndex = index - (getNumColumns()+1);
                        for (int i = 0; i < 3; ++i) {
                            for (int j = 0; j < 3; ++j) {
                                int paintIndex = topLeftIndex + this.kColorSwirlIndexOffsets[3*i+j];
                                if (isIndexSurroundedBy(index, paintIndex)) {
                                    Tile paintTile = tiles[paintIndex];
                                    if (paintTile.getColorKey() != tile.getColorKey() &&
                                    		!paintTile.isModified(Tile.kTMShielded)) {
                                        paintTile.setDecorator(tile.getFunctionKey());
                                        paintTile.setDecoratorData(decoratorIndex);
                                        paintTile.setColorKey(tile.getColorKey());
                                        ++decoratorIndex;
                                        ++paintCount;
                                    }
                                }
                            }
                        }

                        if (paintCount > 0)
                            puzzleSoundShouldPlay(paintCount < 3 ? "color-swirl-short" :
                            	(paintCount < 6 ? "color-swirl-medium" : "color-swirl"));
                    }
                    break;
                case Tile.kTFConveyorBelt:
                    {
                        if (isConveyorBeltActive())
                            break;

                        conveyorBeltDir = Player.orientation2Pos(tile.getDecorationStyleKey() >> Tile.kBitShiftDecorationStyle);

                        if (conveyorBeltDir.x != 0) {
                            setConveyorBeltWrapIndex(conveyorBeltDir.x == 1 ? horizConveyorBeltIndexes.length - 1 : 0);

                            int row = rowForIndex(index);
                            for (int i = 0, n = horizConveyorBeltIndexes.length; i < n; i++)
                                horizConveyorBeltIndexes[i] = row * getNumColumns() + i;
                        } else {
                            conveyorBeltWrapIndex = conveyorBeltDir.y == 1 ? vertConveyorBeltIndexes.length - 1 : 0;

                            int column = columnForIndex(index);
                            for (int i = 0, n = vertConveyorBeltIndexes.length; i < n; i++)
                                vertConveyorBeltIndexes[i] = column + i * getNumColumns();
                        }

                        boolean locked = false;
                        int[] beltIndexes = getConveyorBeltIndexes();

                        // Lock if any tile is shielded.
                        for (int i = 0; i < beltIndexes.length; ++i) {
                            if (tileAtIndex(beltIndexes[i]).isModified(Tile.kTMShielded)) {
                                locked = true;
                                break;
                            }
                        }

                        // Lock if we're rotating and any tiles overlap.
                        if (!locked && isRotating()) {
                            for (int i = 0; i < beltIndexes.length; ++i) {
                                for (int j = 0; j < 9; ++j) {
                                    if (beltIndexes[i] == rotationIndexes[0][j]) {
                                        locked = true;
                                        break;
                                    }
                                }
                            }
                        }

                        if (!locked) {
                            setConveyorBeltActive(true);
                            puzzleConveyorBeltWillMove(getConveyorBeltDir(),
                            		getConveyorBeltWrapIndex(), beltIndexes);
                        }
                        else
                            puzzleSoundShouldPlay("locked");
                    }
                    break;
                case Tile.kTFColorMagic:
                    player.setColorMagicActive(true);
                    puzzleSoundShouldPlay("color-magic");
                    break;
                default:
                    break;
            }
        }

        for (int i = 0, n = queuedCommands.length; i < n; ++i) {
            if (queuedCommands[i] != null) {
            	queuedCommands[i].setFunction(0);
            	queuedCommands[i] = null;
            }
            else
                break;
        }
        
        Coord.freeCoord(tempCoord);
    }
    
    public void registerView(IPuzzleView view)
    {
        if (view == null)
            return;

        if (!views.contains(view, true))
            views.add(view);
    }

    public void deregisterView(IPuzzleView view)
    {
        if (view == null)
            return;

        views.removeValue(view, true);
    }
    
    private void puzzleSoundShouldPlay(String soundName) {
    	for (int i = 0, n = views.size; i < n; i++)
    		views.get(i).puzzleSoundShouldPlay(soundName);
    }

    private void puzzlePlayerWillMove(Player player) {
    	for (int i = 0, n = views.size; i < n; i++)
    		views.get(i).puzzlePlayerWillMove(player);
    }

    private void puzzlePlayerDidMove(Player player) {
    	for (int i = 0, n = views.size; i < n; i++)
    		views.get(i).puzzlePlayerDidMove(player);
    }

    private void puzzleShieldDidDeploy(int tileIndex) {
    	for (int i = 0, n = views.size; i < n; i++)
    		views.get(i).puzzleShieldDidDeploy(tileIndex);
    }

    private void puzzleShieldWasWithdrawn(int tileIndex) {
    	for (int i = 0, n = views.size; i < n; i++)
    		views.get(i).puzzleShieldWasWithdrawn(tileIndex);
    }

    private void puzzleTilesShouldRotate(int[] tileIndexes) {
    	for (int i = 0, n = views.size; i < n; i++)
    		views.get(i).puzzleTilesShouldRotate(tileIndexes);
    }

    private void puzzleTileSwapWillBegin(int[][] swapIndexes, boolean isCenterValid) {
    	for (int i = 0, n = views.size; i < n; i++)
    		views.get(i).puzzleTileSwapWillBegin(swapIndexes, isCenterValid);
    }

    private void puzzleConveyorBeltWillMove(Coord moveDir, int wrapIndex, int[] tileIndexes) {
    	for (int i = 0, n = views.size; i < n; i++)
    		views.get(i).puzzleConveyorBeltWillMove(moveDir, wrapIndex, tileIndexes);
    }

    private void puzzleWasSolved(int tileIndex) {
    	for (int i = 0, n = views.size; i < n; i++)
    		views.get(i).puzzleWasSolved(tileIndex);
    }
    
    public void processPrevMovements() {
        if (isPaused())
            return;

        for (int i = players.size-1; i >= 0; i--) {
            Player player = players.get(i);
            if (player.didMove())
                playerMoved(player);
            player.setDidMove(false);
        }

        recolorPlayers(); // So that clones will receive last-minute tile color changes before setting their data to null.
        processQueuedCommands();
        recolorPlayers(); // So that players can adjust their color based on queued commands.

        for (int i = 0, n = players.size; i < n; i++) {
            Player player = players.get(i);
            if (player.getType() == PlayerType.HUMAN) {
                HumanPlayer humanPlayer = (HumanPlayer)player;
                if (humanPlayer.getMirrorImage() != null && humanPlayer.getMirrorImage().hasExpired()) {
                	MirroredPlayer mirrorImage = humanPlayer.getMirrorImage();
                	humanPlayer.setMirrorImage(null);
                    removePlayer(mirrorImage);
                }
            }
        }
    }

    public void processNextMovements() {
    	if (isPaused())
            return;
    	
        for (int i = players.size-1; i >= 0; i--) {
            Player player = players.get(i);

            if (!player.isMoving()) {
            	tryMove(player);
            	player.setQueuedMove(0, 0);
            }
        }

        recolorPlayers();
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }

    public void softReset() {
    	isResetting = true;
    	
        for (int i = 0, n = queuedCommands.length; i < n; i++) {
            if (queuedCommands[i] != null) {
                queuedCommands[i].setFunction(0);
                queuedCommands[i] = null;
            }
            else
                break;
        }

        if (tiles != null) { 
        	for (int i = 0, n = tiles.length; i < n; i++)
        		tiles[i].reset();
        }

        if (players != null) {
        	Array<Player> playersCache = new Array<Player>(players);
        	for (int i = 0; i < playersCache.size; i++) {
        		Player player = playersCache.get(i);
                
                if (player.getType() == PlayerType.MIRRORED)
                    removePlayer(player);
                else
                	player.reset();
            }
        }

        resetTileSwapIndexes();
        isResetting = isSolved = isRotating = isTileSwapping = isConveyorBeltActive = false;
    }
    
    @Override
    public void reset() {
    	softReset();
    	
    	if (tiles != null) { 
        	for (int i = 0, n = tiles.length; i < n; i++) {
        		TileFactory.freeTile(tiles[i]);
        		tiles[i] = null;
        	}
        }
    	
    	if (players != null) {
    		for (int i = 0, n = players.size; i < n; i++)
    			PlayerFactory.freePlayer(players.get(i));
    		players.clear();
    	}
    }
    
    protected void resetTileSwapIndexes() {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                tileSwapIndexes[0][3 * i + j] = -1;
                tileSwapIndexes[1][3 * i + j] = -1;
            }
        }
    }
    
    public Puzzle clone() {
        Puzzle puzzle = getPuzzle(ID, getName(), getNumColumns(), getNumRows());
        puzzle.IQ = IQ;

        if (tiles != null) {
        	for (int i = 0, n = tiles.length; i < n; i++)
                puzzle.tiles[i].copy(tiles[i]);
        }

        if (players != null) {
        	for (int i = 0, n = players.size; i < n; i++) {
        		Player player = players.get(i);
                if (player.getType() == PlayerType.HUMAN)
                    puzzle.players.add(player.clone());
            }
        }

        return puzzle;
    }

    public Puzzle devClone() {
        Puzzle puzzle = getPuzzle(ID, getName(), getNumColumns(), getNumRows());
        puzzle.ID = ID;
        puzzle.IQ = IQ;

        if (tiles != null) {
        	for (int i = 0, n = tiles.length; i < n; i++)
                puzzle.tiles[i].devCopy(tiles[i]);
        }

        if (players != null) {
        	for (int i = 0, n = players.size; i < n; i++) {
        		Player player = players.get(i);
                if (player.getType() == PlayerType.HUMAN)
                    puzzle.players.add(player.devClone());
            }
        }

        return puzzle;
    }
    
    private static int s_NextValidatorIndex = 0;
    private static boolean isValidatorRecognized(int index, int validator) {
		return index >= 0 && index < kValidators.length &&
				kValidators[index] == validator;
	}
    
	protected void decodeFromStream(BufferedInputStream stream) throws IOException {
		byte[] arr = new byte[32 + 4 + 4 + 4 + 4]; // name,iq,numColumns,numRows,numPlayers
		stream.read(arr, 0, arr.length);

		ByteBuffer buffer = ByteBuffer.wrap(arr);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		byte[] nameArr = new byte[32];
		buffer.get(nameArr, 0, 32);
		
		byte[] validName = Utils.stripTrailingBytes((byte)'\0', nameArr);
		name = validName != null ? new String(validName) : "NULL";
		IQ = buffer.getInt();
		numColumns = buffer.getInt();
		numRows = buffer.getInt();
		
		int numTiles = numColumns * numRows;
		tiles = new Tile[numTiles];
		vertConveyorBeltIndexes = new int[numRows];
		horizConveyorBeltIndexes = new int[numColumns];
		
		int validator = 0;
		for (int i = 0; i < numTiles; i++) {
			tiles[i] = TileFactory.getTile();
			tiles[i].decodeFromStream(stream);
			tiles[i].setEdgeTile(i >= numColumns * (numRows-1));
			
			if (i > 0 && (i & 1) == 0) {
				validator +=
						tiles[i-1].getDevProperties() + tiles[i].getDevProperties() +
						(tiles[i-1].getDevProperties() ^ tiles[i].getDevProperties());
			}
		}
		
		if (PuzzleMode.is10x8()) {
			if (!isValidatorRecognized(s_NextValidatorIndex++, validator)) {
				// Wipe it clear
				for (int i = 0; i < numTiles; i++) {
					tiles[i] = TileFactory.getTile();
					tiles[i].setEdgeTile(i >= numColumns * (numRows-1));
				}
			}
		}
		
		//Gdx.app.log("Puzzle Validator", "" + validator);
		
		int numPlayers = buffer.getInt();
		players = new Array<Player>(true, Math.max(1, numPlayers), Player.class);
		for (int i = 0; i < numPlayers; i++) {
			HumanPlayer player = PlayerFactory.getHumanPlayer();
			player.decodeFromStream(stream);
			addPlayer(player);
		}
	}
}
