package com.cheekymammoth.puzzleViews;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.cheekymammoth.graphics.CMAtlasSprite;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.locale.ILocalizable;
import com.cheekymammoth.puzzles.Player;
import com.cheekymammoth.ui.TextUtils;
import com.cheekymammoth.utils.LangFX;

public class HUDCell extends Prop implements ILocalizable {
	private int evCode;
	private CMAtlasSprite icon;
	private Label label;
	
	public HUDCell() {
		this(-1, 100);
	}

	public HUDCell(int category, float width) {
		super(category);
		
		icon = new CMAtlasSprite(scene.textureRegionByName("mirrored"));
		addSpriteChild(icon);
		
		label = TextUtils.create("", 48, TextUtils.kAlignCenter | TextUtils.kAlignLeft, width, icon.getHeight());
		label.setPosition(
				icon.getX() + icon.getWidth(),
				icon.getY() + LangFX.getPlayerHUDSettings()[0]);
		label.setColor(PlayerHUD.kHUDGreen);
		addActor(label);
	}
	
	public String getText() { return label.getText().toString(); }
	
	public void setText(String text) {
		label.setText(text);
	}
	
	public Color getTextColor() { return label.getColor(); }
	
	public void setTextColor(Color color) {
		label.setColor(color);
	}
	
	public void setIcon(int evCode) {
		if (this.evCode == evCode)
			return;
		
		switch (evCode) {
			case Player.kValueMirrorImage:
				icon.setRegion(scene.textureRegionByName("mirrored"));
				break;
			case Player.kValueColorMagic:
				icon.setRegion(scene.textureRegionByName("color-magic"));
				break;
		}
		
		this.evCode = evCode;
	}

	@Override
	public void localeDidChange(String fontKey, String FXFontKey) {
		label.setText("");
		TextUtils.swapFont(fontKey, label, false);
		label.setY(icon.getY() + LangFX.getPlayerHUDSettings()[0]);
	}
}
