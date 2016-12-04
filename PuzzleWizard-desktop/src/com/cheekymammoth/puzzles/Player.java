package com.cheekymammoth.puzzles;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.cheekymammoth.gameModes.PuzzleMode;
import com.cheekymammoth.puzzleViews.IPlayerView;
import com.cheekymammoth.utils.Coord;
import com.cheekymammoth.utils.PWDebug;

abstract public class Player implements Poolable {
	public enum PlayerType { INVALID, HUMAN, MIRRORED }
	
	public static final int kNorthernOrientation = 0;
    public static final int kEasternOrientation = 1;
    public static final int kSouthernOrientation = 2;
    public static final int kWesternOrientation = 3;
    
    public static int[] getOrientations() {
    	return new int[] { kNorthernOrientation, kEasternOrientation, kSouthernOrientation, kWesternOrientation };
    }

	// Property change codes
	public static final int kValueProperty = 1;
	public static final int kValueColorMagic = 2;
	public static final int kValueMirrorImage = 3;
	public static final int kValueColor = 4;
	public static final int kValueOrientation = 5;
	public static final int kValuePosition = 6;
	public static final int kValueTeleported = 7;
	public static final int kValueForceCompleteMove = 8;
	
    private int orientation;
    private int prevOrientation;
    private int colorKey;
    private int prevColorKey;
    private int futureColorKey;
    private float moveOffsetDuration;
    private Coord position = new Coord();
    private Coord queuedMove = new Coord();
    private Coord viewPositionCache = new Coord();
    private Coord viewOffset = new Coord();
    private Vector2 moveDimensions = new Vector2();
    private boolean isMoving;
    private boolean didMove;
    private Coord isMovingTo = new Coord();
    private int numColorMagicMoves;
    private int function;
    private int functionData;
    private int devOrientation;
    private int devColorKey;
    private Coord devPosition = new Coord();
    private Array<IPlayerView> views;
	
	// Property change constants
	public static int getMaxColorMagicMoves() {
		return PuzzleMode.getMaxColorMagicMoves();
	}
	
	public Player() { PWDebug.playerCount++; }
	
	public void devInit(int colorKey, Coord position, int orientation) {
		this.devColorKey = this.colorKey = colorKey;
		this.devOrientation = this.orientation = orientation;
		this.devPosition.set(position);
		this.position.set(position);
	}
	
//	public Player(int colorKey, Coord position, int orientation) {
//		this.devColorKey = this.colorKey = colorKey;
//		this.devOrientation = this.orientation = orientation;
//		this.devPosition = this.position = position;
//	}
//	
//	public Player(BufferedInputStream stream) throws IOException {
//		decodeFromStream(stream);
//	}
	
	public static int getMirroredOrientation(int orientation) {
		switch (orientation) {
			case kNorthernOrientation: return kSouthernOrientation;
			case kEasternOrientation: return kWesternOrientation;
			case kSouthernOrientation: return kNorthernOrientation;
			case kWesternOrientation: return kEasternOrientation;
			default: return kSouthernOrientation;
		}
	}
	
	public static Coord orientation2Pos(int orientation) {
		switch (orientation) {
			case kNorthernOrientation: return new Coord(0, -1);
			case kEasternOrientation: return new Coord(1, 0);
			case kSouthernOrientation: return new Coord(0, 1);
			case kWesternOrientation: return new Coord(-1, 0);
			default: return new Coord(0, 0);
		}
	}

	public static int pos2Orientation(Coord moveVec) {
		if (moveVec.x != 0) {
			return moveVec.x > 0 ? kEasternOrientation : kWesternOrientation;
		} else if (moveVec.y != 0) {
			return moveVec.y > 0 ? kSouthernOrientation : kNorthernOrientation;
		} else
			return kSouthernOrientation;
	}
	
	public PlayerType getType() { return PlayerType.INVALID; }
	
	public String getCostumeName() { return "idle_body_s_00"; }
	
	public int getOrientation() { return orientation; }
	
	public void setOrientation(int value) {
		prevOrientation = orientation;
		orientation = value;
        notifyPropertyChange(kValueProperty, -1);

        if (orientation != prevOrientation)
            notifyPropertyChange(kValueOrientation, -1);
	}
	
	public int getPrevOrientation() { return prevOrientation; }
	
	private void setPrevOrientation(int value) { prevOrientation = value; }
	
	public int getColorKey() { return colorKey; }
	
	public void setColorKey(int value) {
		setPrevColorKey(colorKey);
		colorKey = value;
        notifyPropertyChange(kValueProperty, -1);
	}
	
	public int getPrevColorKey() { return prevColorKey; }
	
	private void setPrevColorKey(int value) { prevColorKey = value; }
	
	public int getFutureColorKey() { return futureColorKey; }
	
	public void setFutureColorKey(int value) { futureColorKey = value; }
	
	public float getMoveOffsetDuration() { return moveOffsetDuration; }
	
	public void setMoveOffsetDuration(float value) { moveOffsetDuration = value; }
	
	public Coord getPosition() { return position; }
	
	private void setPosition(int x, int y) {
		position.set(x, y);
		notifyPropertyChange(kValueProperty, -1);
	}
	
	public Coord getViewOffset() { return viewOffset; }
	
	public void setViewOffset(int x, int y) {
		viewOffset.set(x, y);
	}
	
	public Coord getViewPosition() {
		viewPositionCache.set(position.x, viewOffset.y - position.y);
		return viewPositionCache;
	}
	
	private void setPosition(Coord value) {
		this.setPosition(value.x, value.y);
	}
	
	public Coord getQueuedMove() { return queuedMove; }
	
	public void setQueuedMove(int x, int y) {
		queuedMove.set(x, y);
	}
	
	public void setQueuedMove(Coord value) {
		queuedMove.set(value);
	}
	
	public Vector2 getMoveDimensions() { return moveDimensions; }
	
	public void setMoveDimensions(float x, float y) {
		moveDimensions.set(x, y);
		notifyPropertyChange(kValueProperty, -1);
	}
	
	public boolean isMoving() { return isMoving; }
	
	private void setMoving(boolean value) { isMoving = value; }
	
	public boolean didMove() { return didMove; }
	
	public void setDidMove(boolean value) { didMove = value; }
	
	public Coord isMovingTo() { return isMovingTo; }
	
//	private void setMovingTo(int x, int y) {
//		isMovingTo.set(x, y);
//	}
	
	private void setMovingTo(Coord value) {
		isMovingTo.set(value);
	}
	
	public boolean isColorMagicActive() { return numColorMagicMoves > 0; }
	
	public void setColorMagicActive(boolean value) {
		setNumColorMagicMoves(value ? getMaxColorMagicMoves() : 0);
	}
	
	public int getNumColorMagicMoves() { return numColorMagicMoves; }
	
	protected void setNumColorMagicMoves(int value) {
		boolean wasActive = isColorMagicActive();
        numColorMagicMoves = value;
        notifyPropertyChange(kValueColorMagic, value);

        if (wasActive != isColorMagicActive())
            notifyPropertyChange(kValueProperty, -1);
	}
	
	public int getFunction() { return function; }
	
	public void setFunction(int value) { function = value; }
	
	public int getFunctionData() { return functionData; }
	
	public void setFunctionData(int value) { functionData = value; }
	
	public int getDevOrientation() { return devOrientation; }
	
	public void setDevOrientation(int value) {
		devOrientation = value;
		setOrientation(value);
		setPrevOrientation(value);
	}
	
	public int getDevColorKey() { return devColorKey; }
	
	public void setDevColorKey(int value) {
		devColorKey = value;
        setColorKey(value);
        setPrevColorKey(value);
	}
	
	public Coord getDevPosition() { return devPosition; }
	
	public void setDevPosition(Coord value) {
		devPosition.set(value);
		position.set(value);
	}
	
	public void registerView(IPlayerView view) {
		if (view == null)
            return;

        if (views == null)
        	views = new Array<IPlayerView>(true, 1, IPlayerView.class);
        if (!views.contains(view, true))
        	views.add(view);
	}
	
	public void deregisterView(IPlayerView view) {
		if (view == null || views == null)
			return;
		
		views.removeValue(view, true);
	}
	
	public void broadcastProperties() { }
	
	protected void notifyPropertyChange(int code, int value) {
		if (views == null)
			return;
		
		for (int i = views.size-1; i >= 0; --i)
			views.get(i).playerValueDidChange(code, value);
	}
	
	private void notifyWillBeginMoving() {
		if (views == null)
			return;
			
		for (int i = views.size-1; i >= 0; --i)
			views.get(i).willBeginMoving();
	}
	 
	private void notifyDidFinishMoving() {
		if (views == null)
			return;
			
		for (int i = views.size-1; i >= 0; --i)
			views.get(i).didFinishMoving();
	 }
	 
	private void notifyDidIdle() {
		if (views == null)
			return;
			
		for (int i = views.size-1; i >= 0; --i)
			views.get(i).didIdle();
	}
	
	private void notifyDidTreadmill() {
		if (views == null)
			return;
			
		for (int i = views.size-1; i >= 0; --i)
			views.get(i).didTreadmill();
	}
	
	public void rotateCW(int increments) {
		for (int i = 0; i < increments; ++i)
            orientation = (orientation + 1) % 4;
	}
	
	public void rotateCCW(int increments) {
		for (int i = 0; i < increments; ++i)
            orientation = (orientation - 1) % 4;
	}
	
	public void beginMoveTo(Coord pos) {
		if (isMoving())
            return;

        setMovingTo(pos);

        float moveX = pos.x - position.x, moveY = pos.y - position.y;
        if (moveX != 0)
            setOrientation(moveX < 0 ? kWesternOrientation : kEasternOrientation);
        else if (moveY != 0)
        	setOrientation(moveY < 0 ? kNorthernOrientation : kSouthernOrientation);

        notifyWillBeginMoving();

        if (!isMoving())
            moveTo(isMovingTo());
	}
	
	public void didBeginMoving() {
		setMoving(true);
	}
	
	public void forceCompleteMove() {
		notifyPropertyChange(kValueForceCompleteMove, 0);
	}
	
	public void didFinishMoving() {
		if (isMoving())
        {
            setMoving(false);
            moveTo(isMovingTo());
            notifyDidFinishMoving();
        }
	}
	
	public void idle() {
		notifyDidIdle();
	}
	
	public void treadmill(int x, int y) {
		if (x != 0)
            setOrientation(x < 0 ? kWesternOrientation : kEasternOrientation);
        else if (y != 0)
        	setOrientation(y < 0 ? kNorthernOrientation : kSouthernOrientation);
		
		notifyDidTreadmill();
	}
	
	public void silentMoveTo(Coord pos) {
		setPosition(pos);
	}
	
	public void moveTo(Coord pos) {
		setPosition(pos);
		setDidMove(true);

        if (isColorMagicActive())
            setNumColorMagicMoves(getNumColorMagicMoves()-1);
	}
	
	public void teleportTo(Coord pos) {
		setPosition(pos);
	}
	
	@Override
	public void reset() {
		moveOffsetDuration = 0;
		
		setOrientation(getDevOrientation());
        setColorKey(getDevColorKey());
        setPosition(getDevPosition());

        setPrevOrientation(getOrientation());
        setPrevColorKey(getColorKey());

        setFunction(0);
        setFunctionData(0);

        setColorMagicActive(false);
        setMoving(false);
        setDidMove(false);
        isMovingTo().set(0, 0);
        getQueuedMove().set(0, 0);
	}
	
	abstract public Player clone();
	
	abstract public Player devClone();
	
	protected void decodeFromStream(BufferedInputStream stream) throws IOException {
		byte[] arr = new byte[16];
		stream.read(arr, 0, arr.length);

		ByteBuffer buffer = ByteBuffer.wrap(arr);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		setDevColorKey(buffer.getInt());
		setDevOrientation(buffer.getInt());
		setDevPosition(new Coord(buffer.getInt(), buffer.getInt()));
	}
}
