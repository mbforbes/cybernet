package com.mortrag.lsb.cybernet;

import java.io.Reader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class TextWindow {
	protected float x, y, width, height;

	// basic
	public BitmapFont font;
	private CharSequence str;

	// from file
	private CharSequence title;
	private CharSequence filename;	
	private CharSequence fullStr;
	private StringBuilder sb;
	private int fullStrPos;
	private boolean scroll;
	private float scale;

	public int capacity; // = 7500;
	public int reveal; // = 45

	/**
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	protected TextWindow(int x, int y, int width, int height, float scale) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.font = new BitmapFont();
		this.font.setColor(0.8f, 0.8f, 0.8f, 0.5f);
		this.font.setScale(scale);
		this.str = "";
		this.title = "";
	}

	protected TextWindow(int x, int y, int width, int height, float scale, String title) {
		this(x, y, width, height, scale);
		this.title = title;
	}

	protected void saveText(String filename, int capacity, int reveal, boolean scroll) {
		this.filename = filename;
		if (Gdx.files.internal(filename).exists()) {
			FileHandle myFh = Gdx.files.internal(filename);
			fullStr = myFh.readString();
			sb = new StringBuilder(capacity);
			this.scroll = scroll;
			this.capacity = capacity;
			this.reveal = reveal; 
			fullStrPos = 0;
		} else {
			System.out.println("Problem, matey.");
		}
	}

	protected void setText(CharSequence txt) {
		str = txt;
	}

	protected void appendText(CharSequence txt) {
		str = str.toString() + txt.toString();
	}

	protected CharSequence getText() {
		return str;
	}

	private void scroll() {
		y += ((float) reveal) / 10.0f;
	}

	private void clear() {
		sb.delete(0, capacity);
	}

	protected void reveal() {
		if (sb.length() > capacity) {
			if (scroll) {
				scroll();
			} else {
				clear();
			}
		}

		// This crashes if it goes on for long enough..
		if (fullStrPos + reveal > fullStr.length()) {
			fullStrPos = 0;
		}
		sb.append(fullStr.subSequence(fullStrPos, fullStrPos + reveal));
		fullStrPos += reveal;

		str = sb.toString();
	}

	protected void incPos() {
		x++;
		y++;
	}

	protected void draw(SpriteBatch batch) {
		int offset = 0;
		if (this.title.length() > 0) {
			offset = 15;			
			this.font.drawWrapped(batch, this.title, this.x, this.y, this.width);
		}
		this.font.drawWrapped(batch, str, x, y - offset, width);		
	}
}
