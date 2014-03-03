package com.shinymetal.bluetoothrc;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

import com.shinymetal.bluetoothrc.BluetoothRC;

/**
 * This class simply creates a desktop LWJGL application.
 */
public class BluetoothRCDesktop {
	
	private static volatile BluetoothRCDesktop instance;
	
    public static BluetoothRCDesktop getInstance() {
    	
    	BluetoothRCDesktop localInstance = instance;
    	
        if (localInstance == null) {
        	
            synchronized (BluetoothRCDesktop.class) {
            	
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new BluetoothRCDesktop();
                }
            }
        }
        
        return localInstance;
    }
	
    public static void main(
        String[] args )
    {
        // define the window's title
        String title = "LivingRoomRobot Desktop";

        // define the window's size
        int width = 960, height = 480;
        
        System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");

        // whether to use OpenGL ES 2.0
        boolean useOpenGLES2 = true;
        BluetoothRC game = BluetoothRC.getInstance ();
        
        // create the game
        new LwjglApplication( game, title, width, height, useOpenGLES2 );
    }    
}
