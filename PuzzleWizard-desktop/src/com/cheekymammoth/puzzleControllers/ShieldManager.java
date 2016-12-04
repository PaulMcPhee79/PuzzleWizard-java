package com.cheekymammoth.puzzleControllers;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.cheekymammoth.events.IEventListener;
import com.cheekymammoth.graphics.CroppedProp;
import com.cheekymammoth.puzzleEffects.EffectFactory;
import com.cheekymammoth.puzzleEffects.Shield;
import com.cheekymammoth.puzzleEffects.Shield.ShieldType;

public class ShieldManager extends CroppedProp implements IEventListener, Disposable {
	private int numColumns;
	private int numRows;
	private Array<Shield> shields;
	
	public ShieldManager(int category, int numColumns, int numRows, Rectangle viewableRegion) {
		super(category, viewableRegion);
		
		this.numColumns = numColumns;
        this.numRows = numRows;
        shields = new Array<Shield>(true, 5, Shield.class);
	}
	
	private boolean isIndexSurroundedBy(int index, int other, int range) {
        int indexColumn = columnForIndex(index), indexRow = rowForIndex(index);
        int otherColumn = columnForIndex(other), otherRow = rowForIndex(other);
        return otherColumn >= 0 && otherColumn < numColumns && otherRow >= 0 && otherRow < numRows
            && Math.abs(indexColumn - otherColumn) <= range && Math.abs(indexRow - otherRow) <= range;
    }
	
	private int rowForIndex(int index) {
        return index / numColumns;
    }

    private int columnForIndex(int index) {
        return index % numColumns;
    }

    private int calcRowDelta(int index, int other) {
        return rowForIndex(other) - rowForIndex(index);
    }

    private int calcColumnDelta(int index, int other) {
        return columnForIndex(other) - columnForIndex(index);
    }
    
    // All shield rects have the same dimensions.
    private void updateFoci() {
    	for (int iOuter = 0, nOuter = shields.size; iOuter < nOuter; iOuter++) {
			Shield shieldOuter = shields.get(iOuter);
			
			if (!shieldOuter.isDeployed())
				continue;
			
			int numFociSet = 0;
            int outerTileIndex = shieldOuter.getTileIndex();
            
            shieldOuter.resetStencils();
            for (int iInner = 0, nInner = shields.size; iInner < nInner; iInner++) {
            	Shield shieldInner = shields.get(iInner);
            	if (shieldOuter == shieldInner || !shieldInner.isDeployed())
                    continue;
            	
            	int innerTileIndex = shieldInner.getTileIndex();

                if (isIndexSurroundedBy(outerTileIndex, innerTileIndex, 1)) {
                    int rowDelta = calcRowDelta(outerTileIndex, innerTileIndex);
                    int colDelta = calcColumnDelta(outerTileIndex, innerTileIndex);
                    int rowXcol = rowDelta * colDelta;

                    if (rowXcol == 0) // nesw
                        shieldOuter.setStencil(numFociSet, 1, (float)Math.atan2(-colDelta, -rowDelta));
                    else // diagonal
                        shieldOuter.setStencil(numFociSet, 2, (float)Math.atan2(-colDelta, -rowDelta));
                } else if (isIndexSurroundedBy(outerTileIndex, innerTileIndex, 2)) {
                    int rowDelta = calcRowDelta(outerTileIndex, innerTileIndex);
                    int colDelta = calcColumnDelta(outerTileIndex, innerTileIndex);
                    int rowXcol = rowDelta * colDelta;

                    if (rowXcol == 0) // nesw
                        shieldOuter.setStencil(numFociSet, 3, (float)Math.atan2(-colDelta, -rowDelta));
                    else { // diagonal
                        if (Math.abs(rowXcol) == 4)
                            continue;
                        shieldOuter.setStencil(numFociSet, 4, (float)Math.atan2(-colDelta, -rowDelta));
                    }
                } else
                    continue;

                if (++numFociSet == Shield.kMaxFoci)
                    break;
            }
		}
    }
    
    public Shield shieldForKey(int key) {
    	for (int i = 0, n = shields.size; i < n; i++) {
    		if (shields.get(i).getID() == key)
    			return shields.get(i);
    	}
        
    	return null;
    }
    
    public Shield addShield(int key, int tileIndex, float originX, float originY) {
    	Shield duplicate = shieldForKey(key);
    	
        if (duplicate != null) {
            duplicate.setID(-1); // Mark as expired
            duplicate.withdraw();
            shields.removeValue(duplicate, true);
            updateFoci();
        }

        Shield shield = EffectFactory.getShield(key, tileIndex, this);
        shield.setPosition(originX, originY);
        shields.add(shield);
        addActor(shield);

        if (rowForIndex(tileIndex) == 0)
            shield.setType(ShieldType.TOP);
        else if (rowForIndex(tileIndex) == numRows - 1)
            shield.setType(ShieldType.BOTTOM);
        else
            shield.setType(ShieldType.NORMAL);

        if (shield.isDeployed())
            updateFoci();
        return shield;
    }
    
    public void removeShield(int key) {
    	Shield shield = shieldForKey(key);
        if (shield == null)
            return;

        shields.removeValue(shield, true);
        removeActor(shield);
        EffectFactory.freeShield(shield);
        updateFoci();
    }
    
    public void withdrawAll(boolean playSound) {
    	for (int i = shields.size-1; i >= 0; i--)
    		shields.get(i).withdraw(playSound);
    }

	@Override
	public void onEvent(int evType, Object evData) {
		if (evData == null)
            return;
		
		if (evType == Shield.EV_TYPE_DEPLOYING) {
			updateFoci();
		} else if (evType == Shield.EV_TYPE_WITHDRAWING) {
			updateFoci();
		} else if (evType == Shield.EV_TYPE_WITHDRAWN) {
			Shield shield = (Shield)evData;

            if (shield.getID() != -1)
                removeShield(shield.getID());
            else {
                // Duplicate - removed earlier.
            	removeActor(shield);
            	EffectFactory.freeShield(shield);
                updateFoci();
            }
		}
	}
	
	@Override
	public void advanceTime(float dt) {
		for (int i = 0, n = shields.size; i < n; i++)
			shields.get(i).advanceTime(dt);
	}

	@Override
	public void dispose() {
		for (int i = 0, n = shields.size; i < n; i++) {
			Shield shield = shields.get(i);
			EffectFactory.freeShield(shield);
		}

		shields.clear();
	}
}
