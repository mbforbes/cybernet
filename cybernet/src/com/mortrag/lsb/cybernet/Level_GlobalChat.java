package com.mortrag.lsb.cybernet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Array;
import com.mortrag.lsb.cybernet.net.RemoteMsgManager;

public class Level_GlobalChat implements Level {

	// BIG BIG CONSTANTS
	public static final String NL = System.getProperty("line.separator");
	public static final String VERSION = "0.9";

	// MEMBERS ////////////////////////////////////////////////////////////////////////////////////		
	private static final String TAG = "Level_GlobalChat";

	private SpriteBatch batch;
	private Camera camera;
	private Array<TextWindow> textWindows;
	private Array<Sprite> sprites;
	private ShapeRenderer shapeRenderer;

	private TextWindow debugWindow;
	private TextWindow bgText, r1, r2, r3, inputWindow, saveWindow, commandWindow;

	private FPSLogger fpsLogger;

	private KeyLogger kl;
	private StringBuilder inputSb;
	private boolean inputActive = true; // start looking @ input window

	private RemoteMsgManager rmm;

	// CONSTRUCTORS ///////////////////////////////////////////////////////////////////////////////	

	/**
	 * TODO(mbforbes) what about loading beforehand? Could do in interface... 
	 * 
	 * @param batch
	 */
	public Level_GlobalChat(SpriteBatch batch) {
		this.batch = batch;

		// create things
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();		
		//camera = new OrthographicCamera(1, h/w);
		camera = new OrthographicCamera(w, h);
		textWindows = new Array<TextWindow>();
		sprites = new Array<Sprite>();
		shapeRenderer = new ShapeRenderer();
		fpsLogger = new FPSLogger();
		kl = new KeyLogger();
		inputSb = new StringBuilder();
		kl.setSB(inputSb);
		Gdx.input.setInputProcessor(kl);
		rmm = new RemoteMsgManager();

		// ip checking
		String ip = "";
		try {
			URL azn = new URL("http://checkip.amazonaws.com");
			BufferedReader in;			
			in = new BufferedReader(new InputStreamReader(
					azn.openStream()));
			ip = in.readLine(); //you get the IP as a String
			in.close();
			Gdx.app.log(TAG, "Got IP: " + ip);			
		} catch (Exception e) {
			Gdx.app.log(TAG, "IP lookup failed...", e);
		}

		if (!ip.equals(RemoteMsgManager.MAX_IP)) {
			// Cooper mode!
			rmm.getMsgsFromMax();			
		}


		// text windows!			
		debugWindow = new TextWindow((int)w/2 - 200,(int)h/2, 200, 100, 1.0f);
		debugWindow.font.setColor(Color.WHITE);
		String longfile = "txt/long.txt";		
		bgText = new TextWindow((int)-w/2, (int)h/2, (int)w/6, (int)h, 0.5f);
		bgText.saveText(longfile, 7500, 45, true);
		r1 = new TextWindow(0, (int)h/2, (int)w/6, (int)h, 0.5f);
		r1.saveText(longfile, 1500, 20, false);
		r2 = new TextWindow((int)w/6, (int)h/2, (int)w/6, (int)h, 0.5f);
		r2.saveText(longfile, 3500, 30, false);
		r3 = new TextWindow((int)w/3, (int)h/2, (int)w/6, (int)h, 0.5f);
		r3.saveText(longfile, 1000, 1, true);		
		inputWindow = new TextWindow((int)-w/6, 0, (int)w/3, (int)h/2, 1.2f, "Text input");
		inputWindow.font.setColor(Color.BLACK);
		commandWindow = new TextWindow((int)w/6, 0, (int)w/3, (int)h/2, 1.2f, "Command input");
		commandWindow.font.setColor(Color.RED);
		saveWindow = new TextWindow((int)-w/4, (int)h/2, (int)w/2, (int)h/2, 1.0f, "Record");
		saveWindow.font.setColor(Color.ORANGE);

		// images!
		// goddamn powers of two
		Texture.setEnforcePotImages(false);
		String bgImagePath = w > 1680 ? "images/namerica_Coopsize.png" : "images/namerica_Maxsize.png"; 
		Texture bgImage = new Texture(Gdx.files.internal(bgImagePath));		
		Sprite bg = new Sprite(bgImage);
		bg.setX(-w/2);
		bg.setY(-h/2);

		// pixmap!?
		Pixmap pixmap = new Pixmap( 64, 64, Format.RGBA8888 );
		pixmap.setColor( 1, 0, 0, 0.4f );
		pixmap.fillRectangle(0, 0, (int)w/25, 25);
		//pixmap.fillCircle( 32, 32, 32 );
		Texture pixmaptex = new Texture( pixmap );
		pixmap.dispose();
		Sprite pixmapsprite = new Sprite(pixmaptex);
		pixmapsprite.setX(-168.0f);
		pixmapsprite.setY(-39.0f);

		// add things
		sprites.add(bg);
		sprites.add(pixmapsprite);
		textWindows.add(debugWindow);
		textWindows.add(bgText);
		textWindows.add(r1);
		textWindows.add(r2);
		textWindows.add(r3);
		textWindows.add(inputWindow);
		textWindows.add(saveWindow);
		textWindows.add(commandWindow);
	}

	// PUBLIC ////////////////////////////////////////////////////////////////////////////////////

	/**
	 * TODO(mbforbes) should do registration-based system to avoid draw-call chaining?...
	 */
	public void draw() {
		// clearing 
		Gdx.gl.glClearColor(0.3f, 0.3f, 0.4f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		int w = (int) Gdx.graphics.getWidth();		
		int h = Gdx.graphics.getHeight();

		// fps! Yeah I'd rather display this.
		// fpsLogger.log();		

		// sprite batch drawing
		// --------------------
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();

		// sprites
		Iterator<Sprite> it_sp = sprites.iterator();
		while (it_sp.hasNext()) {
			Sprite s = it_sp.next();
			s.draw(batch);
		}
		batch.end();		

		// shape renderer
		// --------------
		// compute some constants from current screen size
		// TODO(mbforbes): Should this only be done on resize??

		int left = -w / 2;
		int right = -left;
		int top = h / 2;
		int bottom = -top;

		// set up renderer
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 0.0f); // alpha doesn't do anything...

		// Vertical lines
		int n_segments = 25;
		for (int i = left; i <= right; i += w/n_segments) {
			shapeRenderer.line(i, top, i * 1.0f, bottom);
		}

		// horizontal lines
		float inc = 25;
		float spacing = 1.0f;
		for (int i = bottom; i <= top; i += (int)(inc *= spacing)) {
			shapeRenderer.line(left, i, right, i);
		}
		shapeRenderer.end();


		// text windows
		batch.begin();
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
		debugWindow.setText(
				"version: " + version() + NL +
				"NL len:  " + NL.length() + NL);
		bgText.reveal();
		r1.reveal();
		r2.reveal();
		r3.reveal();

		if (kl.getSwitched()) {
			if (inputActive) {
				inputWindow.setText(kl.getTxt());
				kl.setTxt(commandWindow.getText());
			} else {
				// command active
				commandWindow.setText(kl.getTxt());
				kl.setTxt(inputWindow.getText());
			}
			inputActive = !inputActive;
		}

		// Process current text
		TextWindow getter = inputActive ? inputWindow : commandWindow; 
		String inp = getter.getText().toString();

		// See about if they've pressed 'Enter'
		if (inp.contains(NL)) {
			if (inputActive) {
				saveWindow.appendText(inp);
			} else {
				processCommand(inp);
			}			
			kl.clearTxt();				
		}
		
		// Update the active display window
		TextWindow dispWin = inputActive ? inputWindow : commandWindow;
		dispWin.setText(kl.getTxt());

		// Cooper mode!
		String recClient = rmm.getClientText();
		if (recClient != null) {
			saveWindow.appendText("[MORON] " + recClient);
		}
		String recServer = rmm.getServerText();
		if (recServer != null) {
			saveWindow.appendText("[IDIOT] " + recServer);
		}
	}

	private void processCommand(CharSequence commandSeq) {
		String commandStr = commandSeq.toString().trim();
		int spaceIdx = commandStr.indexOf(' ');
		String cmd = commandStr;
		String args = null;
		if (spaceIdx > -1) {
			cmd = commandStr.substring(0, spaceIdx);
			args = commandStr.substring(spaceIdx + 1);
		}
		if (cmd.length() == 0) {
			return;
		}

		Gdx.app.log("Level_GlobalChat", "Got command: " + commandStr);
		if (cmd.equals(RemoteMsgManager.SERVER_START_CMD)) {
			saveWindow.appendText("<starting server>" + NL);
			rmm.setupServer();
		} else if (cmd.equals(RemoteMsgManager.SERVER_EXIT_CMD)) {
			saveWindow.appendText("<shutting down server>" + NL);
			rmm.shutdownServer();
		} else if (cmd.equals(RemoteMsgManager.CLIENT_SEND_CMD) && args != null) {
			saveWindow.appendText("[IDIOT] " + args + NL);
			// TODO IP (localhost) wrong and not even used!
			rmm.sendMsgToServer("localhost", args);
		} else if (cmd.equals(RemoteMsgManager.SERVER_SEND_CMD) && args != null) {
			saveWindow.appendText("[MORON] " + args + NL);
			rmm.addToServerToSend(args);
		} else {
			saveWindow.appendText("<bad cmd, args> " + cmd + ", " + args + NL);
		}
	}

	public void dispose() {

	}

	// PRIVATE ////////////////////////////////////////////////////////////////////////////////////

	/**
	 * TODO(mbforbes) should this be extracted? for sure standardized in some sort of interface...
	 */
	private void handleInput() {
		// Model: last key pressed cannot be pressed again until it is lifted up. OK.
		//Gdx.input.
		//if (lastKeyPressed == )

		// Position of debug coordinates. This will just happen as the text is also entered :-)
		int increment = 1;
		if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT) ||
				Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
			increment = 20;
		}

		//		if (Gdx.input.isKeyPressed(Input.Keys.W)) {
		//			debugWindow.y += increment;
		//		}				
		//		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
		//			debugWindow.x -= increment;
		//		}
		//		if (Gdx.input.isKeyPressed(Input.Keys.S)) {
		//			debugWindow.y -= increment;
		//		}		
		//		if (Gdx.input.isKeyPressed(Input.Keys.D)) {
		//			debugWindow.x += increment;
		//		}					
	}

	public static String version() {
		return Level_GlobalChat.VERSION;
	}
}
