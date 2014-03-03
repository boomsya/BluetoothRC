package com.shinymetal.bluetoothrc;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.FPSLogger;

import com.shinymetal.bluetoothrc.screens.MainScreen;

/**
 * The game's main class, called as application events are fired.
 */
public class BluetoothRC extends Game {
	
	private static volatile BluetoothRC instance;
	
    private float mDirection = (float) 0.0;
    
	public void setDirection(float direction) {
		
		mDirection = direction;
	}
	
	public float getDirection() { return mDirection; }
	
    // constant useful for logging
    public static final String LOG = BluetoothRC.class.getSimpleName();

    // whether we are in development mode
    public static final boolean DEV_MODE = true;

    // a libgdx helper class that logs the current FPS each second
    private FPSLogger fpsLogger;
    
    private BluetoothRC() {
        		
        fpsLogger = new FPSLogger();
    }
    
    public static BluetoothRC getInstance() {
    	
    	BluetoothRC localInstance = instance;
    	
        if (localInstance == null) {
        	
            synchronized (BluetoothRC.class) {
            	
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new BluetoothRC();
                }
            }
        }
        
        return localInstance;
    }
    
    // Game-related methods

    @Override
    public void create()
    {
        Gdx.app.log( BluetoothRC.LOG, "Creating game on " + Gdx.app.getType() );
        
        setScreen ( MainScreen.getInstance() );
    }

    @Override
    public void resize(
        int width,
        int height )
    {
        Gdx.app.log( BluetoothRC.LOG, "Resizing game to: " + width + " x " + height );

        // show the splash screen when the game is resized for the first time;
        // this approach avoids calling the screen's resize method repeatedly
        
        super.resize( width, height );
    }
    
    @Override
    public void render()
    {
        super.render();

        // output the current FPS
        if( DEV_MODE ) fpsLogger.log();
    }

    @Override
    public void pause()
    {
        Gdx.app.log( BluetoothRC.LOG, "Pausing game" );
        
        super.pause();
    }

    @Override
    public void resume()
    {
        Gdx.app.log( BluetoothRC.LOG, "Resuming game" );
        super.resume();
    }

    @Override
    public void setScreen(
        Screen screen )
    {
        Gdx.app.log( BluetoothRC.LOG, "Setting screen: " + screen.getClass().getSimpleName() );
        
        super.setScreen( screen );
    }

    @Override
    public void dispose()
    {
        Gdx.app.log( BluetoothRC.LOG, "Disposing game" );

        super.dispose();
    }
}
