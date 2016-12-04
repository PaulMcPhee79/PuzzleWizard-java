package com.cheekymammoth.sceneViews;

import com.cheekymammoth.graphics.CMSprite;
import com.cheekymammoth.graphics.ICustomRenderer;
import com.cheekymammoth.graphics.Prop;
import com.cheekymammoth.input.CMInputs;
import com.cheekymammoth.input.ControlsManager;
import com.cheekymammoth.input.IInteractable;
import com.cheekymammoth.puzzleViews.PuzzleBoard;
import com.cheekymammoth.sceneControllers.PlayfieldController;
import com.cheekymammoth.sceneControllers.PlayfieldController.PfState;
import com.cheekymammoth.sceneControllers.SceneController;
import com.cheekymammoth.sceneControllers.SceneUtils.PFCat;
import com.cheekymammoth.utils.Utils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;


public class PlayfieldView extends SceneView implements IInteractable {
	private static final float PIx20 = 62.83185307f * 1.5f;
	
	private float elapsedTime;
	private float totalElapsedTime;
	private Vector2 displacementScroll = new Vector2(0, 0);
	private CMSprite bgSprite;
	private Prop bgCanvas;
	private PlayfieldController controller;
	
	public PlayfieldView(PlayfieldController controller) {
		this.controller = controller;
	}
	
	@Override
	public void setupView() {
		super.setupView();
		
		Texture bgTex = controller.textureByName("bg.png");
		
		TextureRegion region = new TextureRegion(bgTex, 0, 0, bgTex.getWidth(), bgTex.getHeight());
		bgCanvas = new Prop(PFCat.BG.ordinal());
		
		bgSprite = new CMSprite(region);
		bgSprite.setPosition(-bgSprite.getWidth()/2, -bgSprite.getHeight()/2);
		bgCanvas.setContentSize(bgSprite.getWidth(), bgSprite.getHeight());
		bgCanvas.addSpriteChild(bgSprite);
		
		//bgCanvas.setTransform(true);
		bgCanvas.setPosition(controller.VW2(), controller.VH2());
		
		bgCanvas.setCustomRenderer(new ICustomRenderer()
		{
			@Override
			public void preDraw(Batch batch, float parentAlpha, Object obj) {
				//batch.flush(); // Dont for us in SpriteBatch
				
				Prop prop = (Prop)obj;
				ShaderProgram shader = prop.getShader();
				batch.setShader(shader);
				controller.applyShaderDesciptor("refraction");
				shader.setUniformf("u_scroll", -elapsedTime / PIx20);
				shader.setUniformf("u_displacementScroll", displacementScroll.x, displacementScroll.y);
			}
			
			@Override
			public void postDraw(Batch batch, float parentAlpha, Object obj) {
				batch.setShader(null);
			}
		});
		bgCanvas.setShader(controller.shaderByName("refraction"));
		controller.addProp(bgCanvas);
		
		PuzzleBoard puzzleBoard = controller.getPuzzleController().getPuzzleBoard();
		controller.addProp(puzzleBoard);
		
		Vector2 tileDims = puzzleBoard.getScaledTileDimensions(), boardDims = puzzleBoard.getScaledBoardDimensions();
		controller.getPuzzleController().setPuzzleBoardPosition(
				(controller.VW() - boardDims.x) / 2 + tileDims.x / 2,
				(controller.VH() - boardDims.y) / 2 + tileDims.y / 2);
	}
	
	public void enableMenuMode(boolean enable) {
		bgCanvas.setVisible(!enable);
	}

	@Override
	public void onEvent(int evType, Object evData) {
		super.onEvent(evType, evData);
	}
	
	@Override
	protected SceneController getController() {
		return controller;
	}
	
	@Override
	public void attachEventListeners() {
		super.attachEventListeners();
	}
	
	public void detachEventListeners() {
		super.detachEventListeners();
	}
	
	@Override
	public void resize(float width, float height) {
		super.resize(width, height);
		
		try {
			bgCanvas.setSize(controller.getStage().getWidth(), controller.getStage().getHeight());
			bgSprite.setSize(controller.getStage().getWidth(), controller.getStage().getHeight());
			bgSprite.setPosition(-bgSprite.getWidth()/2, -bgSprite.getHeight()/2);
		} catch (Exception e) {
			Gdx.app.log("Unhandled Exception in PlayfieldView.resize", e.getMessage());
		}
	}
	
	@Override
	public void advanceTime(float dt) {
		super.advanceTime(dt);
		
		elapsedTime += dt;
		totalElapsedTime += dt;
		
		if (elapsedTime > PIx20)
			elapsedTime -= PIx20;

		Utils.moveInCircle(totalElapsedTime, 0.05f, displacementScroll);
	}

	@Override
	public int getInputFocus() { return CMInputs.HAS_FOCUS_BOARD; }

	@Override
	public void didGainFocus() { }

	@Override
	public void willLoseFocus() { }

	@Override
	public void update(CMInputs input) {
		if (controller.getState() == PfState.PLAYING) {
			if (input.didDepress(CMInputs.CI_MENU) ||
					ControlsManager.CM().didKeyDepress(Input.Keys.ESCAPE))
				controller.showEscDialog();
		}
	}
}
