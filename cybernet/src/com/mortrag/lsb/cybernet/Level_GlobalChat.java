package com.mortrag.lsb.cybernet;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class Level_GlobalChat implements Level {
	
	// MEMBERS ////////////////////////////////////////////////////////////////////////////////////		
	
	private SpriteBatch batch;
	private Camera camera;
	private Array<TextWindow> textWindows;
	private Array<Sprite> sprites;
	
	private TextWindow debugWindow;
	
	// CONSTRUCTORS ///////////////////////////////////////////////////////////////////////////////	
	
	/**
	 * TODO(mbforbes) what about loading beforehand? Could do in interface... 
	 * 
	 * @param batch
	 */
	public Level_GlobalChat(SpriteBatch batch) {
		this.batch = batch;
		
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();		
		//camera = new OrthographicCamera(1, h/w);
		camera = new OrthographicCamera(w, h);		
		
		textWindows = new Array<TextWindow>();
		sprites = new Array<Sprite>();
		
		debugWindow = new TextWindow(20,20, 200, 100);
		textWindows.add(debugWindow);
	}
	
	// PUBLIC ////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * TODO(mbforbes) should do registration-based system to avoid draw-call chaining?...
	 */
	public void draw() {
		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		Iterator<Sprite> it_sp = sprites.iterator();
		while (it_sp.hasNext()) {
			Sprite s = it_sp.next();
			s.draw(batch);
		}
		
		Iterator<TextWindow> it_tw = textWindows.iterator();
		while (it_tw.hasNext()) {
			TextWindow tw = it_tw.next();
			tw.draw(batch);
		}
		batch.end();		
	}
	
	public void update() {
		//debugWindow.incPos();
		//long time = (System.currentTimeMillis() % 100000) / 100;
		
		handleInput();
		debugWindow.setText("(" + debugWindow.x + ", " + debugWindow.y + ")");
	}
	
	public void dispose() {
		
	}
	
	// PRIVATE ////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * TODO(mbforbes) should this be extracted? for sure standardized in some sort of interface...
	 */
	private void handleInput() {		
		int increment = 1;
		if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
			increment = 20;
		}
		
		if (Gdx.input.isKeyPressed(Input.Keys.W)) {
			debugWindow.y += increment;
		}				
		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			debugWindow.x -= increment;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.S)) {
			debugWindow.y -= increment;
		}		
		if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			debugWindow.x += increment;
		}				
		
		
	}
	
	

}
