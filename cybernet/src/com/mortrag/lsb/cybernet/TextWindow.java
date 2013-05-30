package com.mortrag.lsb.cybernet;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class TextWindow {
	protected int x, y, width, height;
	
	private BitmapFont font;
	private CharSequence str;
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	protected TextWindow(int x, int y, int width, int height) {
		this.x = y;
		this.y = y;
		this.width = width;
		this.height = height;
		this.font = new BitmapFont();
		font.setColor(Color.WHITE);
	}
	
	protected void setText(CharSequence txt) {
		str = txt;
	}
	
	protected void incPos() {
		x++;
		y++;
	}
	
	protected void draw(SpriteBatch batch) {
		font.drawWrapped(batch, str, x, y, width);
	}
}
