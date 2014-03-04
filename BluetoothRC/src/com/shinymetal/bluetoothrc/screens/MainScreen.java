package com.shinymetal.bluetoothrc.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.shinymetal.bluetoothrc.BluetoothRC;
import com.shinymetal.bluetoothrc.objects.GUIGroup;

public class MainScreen
    extends
        AbstractScreen
{
    private static volatile MainScreen instance;

    protected MainScreen()
    {
        super();
    }
    
	public static MainScreen getInstance() {

		MainScreen localInstance = instance;

		if (localInstance == null) {

			synchronized (MainScreen.class) {

				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new MainScreen();
				}
			}
		}

		return localInstance;
	}
	
	public class GUIInputProcessor implements InputProcessor {
		@Override
		public boolean keyDown(int keycode) {
			
			switch(keycode){
			case Input.Keys.LEFT:
				Gdx.app.log( BluetoothRC.LOG, "LEFT pressed");
				BluetoothRC.getInstance().setDirection(BluetoothRC.getInstance().getDirection() - 2);
				return true;

			case Input.Keys.RIGHT:
				Gdx.app.log( BluetoothRC.LOG, "RIGHT pressed");
				BluetoothRC.getInstance().setDirection(BluetoothRC.getInstance().getDirection() + 2);
				return true;
			}
			return false;
		}

		@Override
		public boolean keyUp(int keycode) {
			return false;
		}

		@Override
		public boolean keyTyped(char character) {
			return false;
		}

		@Override
		public boolean touchDown(int x, int y, int pointer, int button) {
			return false;
		}

		@Override
		public boolean touchUp(int x, int y, int pointer, int button) {
			return false;
		}

		@Override
		public boolean touchDragged(int x, int y, int pointer) {
			return false;
		}

		@Override
		public boolean scrolled(int amount) {
			return false;
		}

		@Override
		public boolean mouseMoved(int arg0, int arg1) {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
    @Override
    public void show()
    {
        // super.show();
    	getStage().clear();
    	
        InputMultiplexer multiplexer = new InputMultiplexer();
        
        multiplexer.addProcessor(new GUIInputProcessor());
        multiplexer.addProcessor(getStage());
        
        Gdx.input.setInputProcessor(multiplexer);
        
        getStage ().addActor( new GUIGroup() );
    }
        
    @Override
    public void render (float delta) {
    	
    	super.render( delta );
    }
    
    
    @Override
    public void resume()
    {
        super.resume();
    }
    
    @Override
    public void resize(
        int width,
        int height )
    {
    	super.resize( width, height);
    }
}
