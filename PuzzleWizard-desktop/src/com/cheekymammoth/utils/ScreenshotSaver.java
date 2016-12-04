package com.cheekymammoth.utils;

import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

public class ScreenshotSaver {

	public static void saveScreenshot(String baseName) {
		if (baseName == null)
			return;
		
		try {
	        FileHandle file = getFileHandle(baseName);
			if (file != null) {
				Pixmap pixmap = getScreenshot(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
				PixmapIO.writePNG(file, pixmap);
				pixmap.dispose();
			}
		}
		catch (Exception e) {
			Gdx.app.log("ScreenshotSaver::saveScreenshot", "Failed: " + baseName);
		}
	}

	private static FileHandle getFileHandle(String baseName) {
		int index = 0;
		for (int i = index; i < 100; i++) {
			String indexStr = i < 10 ? "_0" + i : "_" + i;
			FileHandle file = Gdx.files.local(baseName + indexStr + ".png");
			if (!file.exists())
				return file;
		}
		
		return null;
	}
	
	private static Pixmap getScreenshot(int x, int y, int w, int h, boolean flipY) {
		Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
		
		final Pixmap pixmap = new Pixmap(w, h, Format.RGB888);
		ByteBuffer pixels = pixmap.getPixels();
		Gdx.gl.glReadPixels(x, y, w, h, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, pixels);
		
		final int numBytes = w * h * 3;
		byte[] lines = new byte[numBytes];
		if (flipY) {
		        final int numBytesPerLine = w * 3;
		        for (int i = 0; i < h; i++) {
		                pixels.position((h - i - 1) * numBytesPerLine);
		                pixels.get(lines, i * numBytesPerLine, numBytesPerLine);
		        }
		        pixels.clear();
		        pixels.put(lines);
		} else {
		        pixels.clear();
		        pixels.get(lines);
		}
		
		return pixmap;
	}
	
	private static int m_width;
	private static int m_height;
	private static float m_fboScaler = 1f;
	private static boolean m_fboEnabled = true;
	private static String m_baseName;
	private static SpriteBatch m_spriteBatch;
	private static FrameBuffer m_fbo = null;
	private static TextureRegion m_fboRegion = null;
	
	public static void beginHiResRender(String baseName, int w, int h) {
		m_baseName = baseName;
		m_width = w;
		m_height = h;
		
		if (m_spriteBatch == null)
			m_spriteBatch = new SpriteBatch();
		
		if(m_fboEnabled)      // enable or disable the supersampling
	    {                  
	        if(m_fbo == null)
	        {
	            // m_fboScaler increase or decrease the antialiasing quality

	            m_fbo = new FrameBuffer(Format.RGB888, (int)(w * m_fboScaler), (int)(h * m_fboScaler), false);
	            m_fboRegion = new TextureRegion(m_fbo.getColorBufferTexture());
	            m_fboRegion.flip(false, true);
	        }

	        m_fbo.begin();
	    }
	}
	
	public static void endHiResRender() {
		if(m_fbo != null && m_spriteBatch != null)
	    {
	        m_fbo.end();

	        m_spriteBatch.begin();         
	        m_spriteBatch.draw(m_fboRegion, 0, 0, m_width, m_height);
	        m_spriteBatch.end();
	    }
		
		saveHiResScreenshot(m_baseName, m_width / 2, m_height / 2);
	}
	
	private static void saveHiResScreenshot(String baseName, int w, int h) {
		try {
	        FileHandle file = getFileHandle(baseName);
			if (file != null) {
				Pixmap pixmap = getScreenshot(0, 0, w, h, true);
				PixmapIO.writePNG(file, pixmap);
				pixmap.dispose();
			}
		}
		catch (Exception e) {
			Gdx.app.log("ScreenshotSaver::saveHiResScreenshot", "Failed: " + baseName);
		}
	}
}
