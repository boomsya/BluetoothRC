package com.shinymetal.bluetoothrc.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
//import com.badlogic.gdx.graphics.g2d.SpriteBatch;
//import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Stage;
//import com.badlogic.gdx.scenes.scene2d.ui.Skin;
//import com.badlogic.gdx.scenes.scene2d.ui.Table;

import com.shinymetal.bluetoothrc.BluetoothRC;

/**
 * The base class for all game screens.
 */
public abstract class AbstractScreen
    implements
        Screen
{
    private Stage stage;

    protected AbstractScreen()
    {
        // for now measure screen in percents
        // AssetsManager.getInstance().setWorldSize ( 100, 100 );        
    }
    
    protected String getName()
    {
        return getClass().getSimpleName();
    }
    
//    protected SpriteBatch getSpriteBatch() {
//    	
//    	if ( spriteBatch == null ) {
//    		spriteBatch = new SpriteBatch ( 1000, defaultShader );
//    	}
//    	return spriteBatch;
//    }

    protected Stage getStage()
    {
        if( stage == null ) {
        	
        	float width = Gdx.graphics.getWidth();
        	float height = Gdx.graphics.getHeight();
        	
        	Gdx.app.log( BluetoothRC.LOG, "stage width: " + width + ", height: " + height );
            stage = new Stage( width, height, true /*, spriteBatch */);
        }
        return stage;
    }
    
    // Screen implementation

    @Override
    public void show()
    {
        Gdx.app.log( BluetoothRC.LOG, "Showing screen: " + getName() );

        // Some dirty magic here to workaround possible invalid shader

//        if ( defaultShader != null ) {
//        	
//	        defaultShader.dispose ();
//	    }
//        
//        Gdx.app.log( GlassAlphabet.LOG, "Compiling default shader");
//        defaultShader = SpriteBatch.createDefaultShader();        
//        getSpriteBatch().setShader( defaultShader );
//        Gdx.app.log( GlassAlphabet.LOG, "... is compiled: "
//        			+ defaultShader.isCompiled() + ", log: " + defaultShader.getLog() ); 

        // remove all stuff from stage
        getStage().clear();

        // set the stage as the input processor
        Gdx.input.setInputProcessor( getStage () );
    }

    @Override
    public void resize(
        int width,
        int height )
    {
        Gdx.app.log( BluetoothRC.LOG, "Resizing screen: " + getName() + " to: " + width + " x " + height );
    }

    @Override
    public void render(
        float delta )
    {
        // (1) process the game logic
    	
    	// if ( AssetsManager.getInstance().getQueuedAssets() > 0 ) {
    		// AssetsManager.getInstance().update();
    	// }

        // update the actors
        getStage ().act( delta );

        // (2) draw the result

        // clear the screen with the given RGB color (black)
        Gdx.gl.glClearColor( 0f, 0f, 0f, 1f );
        Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );

        // draw the actors
        getStage ().draw();
        
//       	int error;
//        while ((error = Gdx.gl.glGetError()) != GL20.GL_NO_ERROR) {
//           	Gdx.app.log ( GlassAlphabet.LOG, String.format("Abstract Screen GL error: 0x%x", error));
//        }

        // draw the table debug lines
        // Table.drawDebug( getStage () );
    }

    @Override
    public void hide()
    {
        Gdx.app.log( BluetoothRC.LOG, "Hiding screen: " + getName() );
        
//        if ( defaultShader != null ) {
//        	
//	        defaultShader.dispose ();
//	    }
    }

    @Override
    public void pause()
    {
        Gdx.app.log( BluetoothRC.LOG, "Pausing screen: " + getName() );
    }
    
        @Override
    public void resume()
    {
        Gdx.app.log( BluetoothRC.LOG, "Resuming screen: " + getName() );
    }

    @Override
    public void dispose()
    {
        Gdx.app.log( BluetoothRC.LOG, "Disposing screen: " + getName() );

        if ( stage != null ) stage.dispose();
    }
}
