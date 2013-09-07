package com.mortrag.lsb.cybernet;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

public class KeyLogger implements InputProcessor {

	public StringBuilder sb;
	private boolean switched = false;
	
	public synchronized boolean getSwitched() {
		boolean ret = switched;
		switched = false; // switch
		return ret;     // and return what it was
	}
	
	private synchronized void switchWindow() {
		switched = !switched;
	}
	
	public void setSB(StringBuilder sb) {
		this.sb = sb;
	}
	
	private synchronized void backspace() {
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
	}
	
	private synchronized void addTxt(char character) {
		sb.append(character);
	}
	
	private synchronized void addTxt(String s) {
		sb.append(s);
	}	
	
	public synchronized void clearTxt() {
		sb.delete(0, sb.length());		
	}
	
	public synchronized String getTxt() {
		String ret = sb.toString();			
		return ret;
	}
	
	public synchronized void setTxt(CharSequence txt) {
		clearTxt();
		sb.append(txt);
	}
	
	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Input.Keys.ENTER) {
			sb.append(Level_GlobalChat.NL);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		if (character == '\b') {
			backspace();
		} else if (character == '\t') {
			switchWindow();
		} else if (character == '\r' || character == '\n') {
			// Do nothing -- they keydown handles this
		} else {
			addTxt(character);
		}
		return true;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

}
