package com.cheekymammoth.puzzleui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.cheekymammoth.graphics.CMAtlasSprite;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.locale.ILocalizable;
import com.cheekymammoth.locale.Localizer.LocaleType;
import com.cheekymammoth.puzzleFactories.TileFactory;
import com.cheekymammoth.puzzleViews.PainterDecoration;
import com.cheekymammoth.puzzleViews.PuzzleBoard;
import com.cheekymammoth.puzzleViews.TilePiece;
import com.cheekymammoth.ui.TextUtils;
import com.cheekymammoth.utils.LangFX;
import com.cheekymammoth.utils.Utils;

public class PuzzlePageEntry extends Prop implements ILocalizable {
	private static final Rectangle[] s_RectCaches = new Rectangle[] {
		new Rectangle(), new Rectangle()
	};
	private static final float kBoardShadowOffsetX = 14.0f;
	private static final float kBoardShadowOffsetY = -38.0f;
	private static final float kIqTagShadowOffsetX = 6.0f;
	private static final float kIqTagShadowOffsetY = -12.0f;

	private boolean isPopulated = true;
	private boolean isLocked = true;
	private boolean isSolved = false;
	private boolean isHighlighted = false;
	private boolean isLabelShrunkToFit = false;
	private Color highlightColor = new Color(Color.WHITE);
	
	private CMAtlasSprite lock;
	private CMAtlasSprite solved;
	private Prop spriteProp;
	private PainterDecoration highlightedIcon;
	private PuzzleBoard board;
	private Prop boardContainer;
	private Prop boardShadow;
	private Prop iqTagShadow;
	private String rawText;
	private Label label;
	private float labelBaseScale = 1f;
	private static float labelWidthMax;
	private static PuzzlePageSettings settings;
	// Temporary caches
	private Vector2 vecCacheA = new Vector2();
	private Vector2 vecCacheB = new Vector2();
	private Vector2 vecCacheC = new Vector2();
	
	public static void setSettings(PuzzlePageSettings value) {
		settings = value;
	}
	
	public PuzzlePageEntry(PuzzleBoard board) {
		this(-1, board);
	}

	public PuzzlePageEntry(int category, PuzzleBoard board) {
		super(category);
		
		this.board = board;
		labelWidthMax = 1.2f * settings.puzzleBoardWidth;
		
		// Board shadow
		CMAtlasSprite shadowSprite = new CMAtlasSprite(scene.textureRegionByName("shadow-rect"));
		shadowSprite.centerContent();
		
		boardShadow = new Prop();
		boardShadow.setTransform(true);
		boardShadow.setSize(shadowSprite.getWidth(), shadowSprite.getHeight());
		boardShadow.addSpriteChild(shadowSprite);
		
		// Iq tag shadow
		shadowSprite = new CMAtlasSprite(scene.textureRegionByName("iq-tag-shadow"));
		shadowSprite.centerContent();
		
		iqTagShadow = new Prop();
		iqTagShadow.setTransform(true);
		iqTagShadow.setSize(shadowSprite.getWidth(), shadowSprite.getHeight());
		iqTagShadow.addSpriteChild(shadowSprite);
		
		boardContainer = new Prop();
		boardContainer.setTransform(true);
		boardContainer.addActor(board);
		
		int fontSize = 34; 
		label = TextUtils.create(
				" ",
				fontSize,
				TextUtils.kAlignTop | TextUtils.kAlignCenter, labelWidthMax,
				TextUtils.getCapHeight(fontSize));
		//label.setWrap(true);
		labelBaseScale = label.getFontScaleX();
		label.setOrigin(label.getWidth() / 2, label.getHeight());
		rawText = label.getText().toString();
		
		lock = new CMAtlasSprite(scene.textureRegionByName("level-lock"));
		lock.setVisible(isLocked());

		solved = new CMAtlasSprite(scene.textureRegionByName("menu-key"));
		//solved.setVisible(isSolved());
		
		//resizeBoard();
		setPopulated(false);
		
		highlightedIcon = TileFactory.getPainterDecoration(category, TilePiece.kColorKeyGreen << 4);
		highlightedIcon.setPosition(
				label.getX() + (label.getWidth() - label.getTextBounds().width) / 2 - highlightedIcon.getWidth() / 2,
				label.getY() + LangFX.getActiveIconYOffset());
		highlightedIcon.setVisible(isHighlighted());
		
		spriteProp = new Prop(category);
		spriteProp.addSpriteChild(lock);
		spriteProp.addSpriteChild(solved);
		
		addActor(boardShadow);
		addActor(iqTagShadow);
		addActor(boardContainer);
		addActor(label);
		addActor(highlightedIcon);
		addActor(spriteProp);
	}
	
	private Vector2 minPointCache = new Vector2();
	public Vector2 getMinPoint() {
		return minPointCache.set(
				label.getX() - highlightedIcon.getWidth(),
				label.getY() - label.getHeight() / 2);
	}
	
	private Vector2 maxPointCache = new Vector2();
	public Vector2 getMaxPoint() {
		Prop iqSpriteProp = board.getIqSpriteProp();
		if (iqSpriteProp != null) {
			maxPointCache.set(
					iqSpriteProp.getWidth() / 2,
					iqSpriteProp.getHeight() / 2);
			iqSpriteProp.localToStageCoordinates(maxPointCache);
			this.stageToLocalCoordinates(maxPointCache);
			return maxPointCache;
		} else {
			return maxPointCache.set(
					label.getX() + label.getWidth(),
					label.getY() + label.getHeight() / 2);
		}
	}
	
	public boolean isPopulated() { return isPopulated; }
	
	public void setPopulated(boolean value) {
		setVisible(value);
		isPopulated = value;
	}
	
	public boolean isLocked() { return isLocked; }
	
	public void setLocked(boolean value) {
		isLocked = value;
		if (lock != null) lock.setVisible(value);
	}
	
	public boolean isSolved() { return isSolved; }
	
	public void setSolved(boolean value) {
		isSolved = value;
		if (solved != null) solved.setVisible(value);
	}

	public boolean isHighlighted() { return isHighlighted; }
	
	public void setHighlighted(boolean value) {
		if (isHighlighted == value)
			return;
		
		label.setColor(value ? highlightColor : Color.WHITE);
		if (board != null) board.enableHighlight(value);
		
		isHighlighted = value;
		
		if (value)
			repositionHighlightedIcon();
		highlightedIcon.setVisible(value);
	}
	
	public Color getHighlightColor() { return highlightColor; }
	
	public void setHighlightColor(Color color) {
		highlightColor.set(color);
		label.setColor(isHighlighted() ? highlightColor : Color.WHITE);
		if (board != null) board.setHighlightColor(color);
	}
	
	public PuzzleBoard getPuzzleBoard() { return board; }
	
	public void setPuzzleBoard(PuzzleBoard value) { board = value; }
	
	public void resizeBoard() {
		if (boardContainer == null || board == null)
			return;
		
		Vector2 scaledBoardDims = board.getScaledBoardDimensions();
		vecCacheA.set(0.0f, 0.5f);
		board.setOrigin(vecCacheA.x * scaledBoardDims.x, vecCacheA.y * scaledBoardDims.y);
		board.setPosition(-scaledBoardDims.x / 2, -scaledBoardDims.y / 2);
		
		boardContainer.setScale(
				settings.puzzleBoardWidth / scaledBoardDims.x,
				settings.puzzleBoardHeight / scaledBoardDims.y);
		
		Vector2 maxBoardDims = LangFX.getMaxPuzzlePageBoardDimensions();
		boardContainer.setPosition(maxBoardDims.x / 2, maxBoardDims.y / 2);
		
		vecCacheA.set(board.getX(), board.getY());
		vecCacheA = this.stageToLocalCoordinates(boardContainer.localToStageCoordinates(vecCacheA));
		
		if (boardShadow != null) {
			boardShadow.setScale(1.0f);
			boardShadow.setPosition(
					vecCacheA.x + settings.puzzleBoardWidth / 2 + kBoardShadowOffsetX,
					vecCacheA.y + settings.puzzleBoardHeight / 2 + kBoardShadowOffsetY);
			boardShadow.setScale(
					1.15f * settings.puzzleBoardWidth / boardShadow.getWidth(),
					1.2f * settings.puzzleBoardHeight / boardShadow.getHeight());
		}
		
		if (iqTagShadow != null) {
			Prop iqSpriteProp = board.getIqSpriteProp();
			if (iqSpriteProp == null || iqSpriteProp.getParent() == null) {
				iqTagShadow.setVisible(false);
			} else {
				iqTagShadow.setScale(1.0f);
				iqTagShadow.setVisible(true);
				
				vecCacheB.set(
						-iqSpriteProp.getWidth() / 2,
						-iqSpriteProp.getHeight() / 2);
				vecCacheB = iqSpriteProp.localToStageCoordinates(vecCacheB);
				vecCacheC.set(
						iqSpriteProp.getWidth() / 2,
						iqSpriteProp.getHeight() / 2);
				vecCacheC = iqSpriteProp.localToStageCoordinates(vecCacheC);
				
				float srcWidth = vecCacheC.x - vecCacheB.x, srcHeight = vecCacheC.y - vecCacheB.y;

				vecCacheB.set(
						-iqTagShadow.getWidth() / 2,
						-iqTagShadow.getHeight() / 2);
				vecCacheB = iqTagShadow.localToStageCoordinates(vecCacheB);
				vecCacheC.set(
						iqTagShadow.getWidth() / 2,
						iqTagShadow.getHeight() / 2);
				vecCacheC = iqTagShadow.localToStageCoordinates(vecCacheC);
				
				float destWidth = vecCacheC.x - vecCacheB.x, destHeight = vecCacheC.y - vecCacheB.y;
				iqTagShadow.setScale(
						1.25f * srcWidth / destWidth,
						1.25f * srcHeight / destHeight);
				
				vecCacheB.set(iqSpriteProp.getX(), iqSpriteProp.getY());
				vecCacheB = iqSpriteProp.getParent().localToStageCoordinates(vecCacheB);
				vecCacheB = this.stageToLocalCoordinates(vecCacheB);
				iqTagShadow.setPosition(
						vecCacheB.x + kIqTagShadowOffsetX,
						vecCacheB.y + kIqTagShadowOffsetY);
			}
		}
		
		if (label != null) {
			float yOffset = getShrunkLabelDiffY() / 2;
			label.setPosition(
					vecCacheA.x + settings.puzzleBoardWidth / 2 - label.getWidth() / 2,
					vecCacheA.y + LangFX.getPuzzleEntryLabelYOffset() - (1.3f * label.getHeight() + yOffset));
		}
		
		if (lock != null) {
			lock.setOrigin(lock.getWidth() / 2, lock.getHeight() / 2);
			lock.setScale(settings.puzzleBoardWidth / maxBoardDims.x);
			lock.setPosition(
					vecCacheA.x + settings.puzzleBoardWidth / 2 - lock.getScaledWidth() / 2,
					vecCacheA.y + settings.puzzleBoardHeight / 2 - lock.getScaledHeight() / 2);
		}
		
		if (solved != null) {
			solved.setOrigin(solved.getWidth() / 2, solved.getHeight() / 2);
			solved.setScale(settings.puzzleBoardWidth / maxBoardDims.x);
			solved.setPosition(
					vecCacheA.x - 0.45f * solved.getScaledWidth(),
					vecCacheA.y + settings.puzzleBoardHeight - 0.525f * solved.getScaledHeight());
		}
		
		// Recalculate our size
		s_RectCaches[0].set(0, 0, scaledBoardDims.x * boardContainer.getScaleX(),
				scaledBoardDims.y * boardContainer.getScaleY());
		s_RectCaches[1].set(0, 0, label.getWidth(), label.getHeight());
		Utils.unionRect(s_RectCaches[0], s_RectCaches[1]);
		setSize(s_RectCaches[1].width, s_RectCaches[1].height);
	}
	
	public String getText() {
		return rawText; //label.getText().toString();
	}
	
	public void setText(String text) {
		isLabelShrunkToFit = false;
		rawText = text;
		label.setText(text);
		resizeLabel(); // Resize to default size.
		
		if (label.getTextBounds().width > labelWidthMax) {
			String[] textTokens = text.split(" ");
			if (textTokens != null && textTokens.length > 1) {
				LocaleType locale = scene.getLocale();
		        if (locale == LocaleType.JP) {
		        	isLabelShrunkToFit = true;
		        } else {
					String wrapText = textTokens[0];
					for (int i = 1, n = textTokens.length-1; i < n; i++)
						wrapText += " " + textTokens[i];
					wrapText += "\n" + textTokens[textTokens.length-1];
					label.setText(wrapText);
		        }
		        
		        resizeLabel(); // Scale based on new text (for appropriate locales).
			}
		}
		
		repositionHighlightedIcon();
	}
	
	private void resizeLabel() {
		label.setFontScale(labelBaseScale);
		
		if (isLabelShrunkToFit) {
			float scaleMax = 1f;
    		float labelWidth = label.getTextBounds().width;
    		if (labelWidth > scaleMax * labelWidthMax) {
    			float scaler = (scaleMax * labelWidthMax) / labelWidth;
    			label.setFontScale(labelBaseScale * scaler);
    		}
		}
	}
	
	public void setFontFile(String value) {
		TextUtils.swapFont(value, label, true);
	}
	
	private float getShrunkLabelDiffY() {
		float scaleY = label.getFontScaleY();
		if (isLabelShrunkToFit && scaleY != 0)
			return (labelBaseScale / scaleY) * label.getHeight() - label.getHeight();
		else
			return 0;
	}
	
	private void repositionHighlightedIcon() {
		String text = getText();
		int langOffsetY = text != null && text.length() > 0
				? LangFX.getActiveIconYOffset(text.charAt(0))
				: LangFX.getActiveIconYOffset();
		float yOffset = langOffsetY + getShrunkLabelDiffY() / 2;
		highlightedIcon.setPosition(
				label.getX() + (label.getWidth() - label.getTextBounds().width) / 2 - highlightedIcon.getWidth() / 2,
				label.getY() + yOffset);
	}

	@Override
	public void localeDidChange(String fontKey, String FXFontKey) {
		setText("");
		setFontFile(fontKey);
		if (board != null)
			board.localeDidChange(fontKey, FXFontKey);
		resizeBoard();
	}
}
