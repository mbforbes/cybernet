package com.mortrag.lsb.cybernet;

/**
 * Simplified level--implements some functionality of Game. No pausing, resizing, etc. (yet).
 * 
 * @author mbforbes
 */
public interface Level {
	public void update();
	public void draw();
	public void dispose();
}
