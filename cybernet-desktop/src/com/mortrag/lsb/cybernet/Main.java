package com.mortrag.lsb.cybernet;

import java.awt.Toolkit;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "cybernet";
		cfg.useGL20 = false;		
		cfg.width = Toolkit.getDefaultToolkit().getScreenSize().width;
		cfg.height = Toolkit.getDefaultToolkit().getScreenSize().height;
		
		new LwjglApplication(new Game(), cfg);
	}
}
