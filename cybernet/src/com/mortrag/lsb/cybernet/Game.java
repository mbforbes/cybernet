package com.mortrag.lsb.cybernet;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Game implements ApplicationListener {
	private SpriteBatch batch;
	private Level level;
	
	@Override
	public void create() {		
		batch = new SpriteBatch();
		level = new Level_GlobalChat(batch);
	}

	@Override
	public void dispose() {
		level.dispose();
	}

	@Override
	public void render() {
		update();
		draw();
	}
	
	private void update() {
		level.update();
	}
	private void draw() {
		level.draw();		
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}
