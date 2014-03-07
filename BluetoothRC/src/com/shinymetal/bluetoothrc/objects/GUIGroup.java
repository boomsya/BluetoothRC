package com.shinymetal.bluetoothrc.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.shinymetal.bluetoothrc.BTTransmitter;
import com.shinymetal.bluetoothrc.BluetoothRC;

public class GUIGroup
		extends Group {
	
    private ShapeRenderer shapeRenderer;
    
    private Actor spdHandle;
    private Actor wheelHandle;
    private Actor stopBtn;
    
    private float wheelRotation = BluetoothRC.getInstance().getDirection();
    private int throttle = 0;
    
    static final int SPD_LOW_MARK = 25;
    static final int SPD_HIGH_MARK = 77;
    
    static final int SPD_MAX_FWD = 9;
    static final int SPD_MAX_RWD = 4;
    
    private long mLastSendTime = System.currentTimeMillis();
    
	public GUIGroup () {
		shapeRenderer = new ShapeRenderer ();

		Actor background = new Image(new Texture(Gdx.files.internal("graphics/background.png")));
		background.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		this.addActor(background);
		
		spdHandle = new Image(new Texture(Gdx.files.internal("graphics/handle.png")));
		spdHandle.setSize(Gdx.graphics.getWidth() / 10, Gdx.graphics.getHeight() / 10);
		
		final Actor spdHandleHole = new Actor();
		spdHandleHole.setSize(spdHandle.getWidth() * 15 / 10, Gdx.graphics.getHeight() * (SPD_HIGH_MARK - SPD_LOW_MARK));
		spdHandleHole.setPosition( (float) (Gdx.graphics.getWidth() * 13.8 / 100 - spdHandleHole
						.getWidth() / 2), Gdx.graphics.getHeight() * SPD_LOW_MARK / 100);
		
		final float zeroPos = Gdx.graphics.getHeight() * SPD_LOW_MARK / 100
				+ Gdx.graphics.getHeight() * (SPD_HIGH_MARK - SPD_LOW_MARK) / 100
				* (SPD_MAX_RWD + 1) / (SPD_MAX_FWD + SPD_MAX_RWD + 1)
				- spdHandle.getHeight() / 2;
		
		spdHandle.setPosition((int) (Gdx.graphics.getWidth() * 13.8 / 100 - spdHandle.getWidth() / 2), zeroPos);
		
		spdHandleHole.addListener(new DragListener() {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

				touchDragged (event, x, y, pointer);
				return super.touchDown(event, x, y, pointer, button);
			}
			
			public void touchDragged (InputEvent event, float x, float y, int pointer) {

				float height = Gdx.graphics.getHeight();
				float lowMark = height * SPD_LOW_MARK / 100;
				float highMark = height * SPD_HIGH_MARK / 100;
				Vector2 coords = new Vector2(x, y);
				spdHandleHole.localToStageCoordinates(coords);
				float spdY = coords.y - spdHandle.getHeight() / 2;

				if (spdY < lowMark - spdHandle.getHeight() / 2)
					spdY = lowMark - spdHandle.getHeight() / 2;
				if (spdY > highMark - spdHandle.getHeight() / 2)
					spdY = highMark - spdHandle.getHeight() / 2;

				spdHandle.setPosition(spdHandle.getX(), spdY);

				throttle = (int) ((spdY + spdHandle.getHeight() / 2 - lowMark)
						/ (highMark - lowMark)
						* (SPD_MAX_FWD + SPD_MAX_RWD + 1) - SPD_MAX_RWD - 1);
				
				BluetoothRC.getInstance().setThrottle(throttle);				
			}
		});
		
		this.addActor(spdHandle);
		this.addActor(spdHandleHole);
	
		wheelHandle = new Image(new Texture(Gdx.files.internal("graphics/volant.png")));

		// TODO: Remove once we have final image
		wheelHandle.setSize(Gdx.graphics.getWidth() * 75 / 100, Gdx.graphics.getWidth() * 75 / 100);
		wheelHandle.setPosition(Gdx.graphics.getWidth() * 25 / 100 , wheelHandle.getHeight() / -10 * 4 );
		wheelHandle.setOrigin(wheelHandle.getWidth() / 2, wheelHandle.getHeight() / 2);
		wheelHandle.addListener(new DragListener() {
			
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				
				BluetoothRC.getInstance().setIsWheelDragged(true);
				return super.touchDown(event, x, y, pointer, button);
			}
			
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				
				BluetoothRC.getInstance().setIsWheelDragged(false);
				super.touchUp(event, x, y, pointer, button);
			}
			
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
            	
            	Vector2 center = new Vector2(wheelHandle.getWidth()/2, wheelHandle.getHeight()/2);
            	Vector2 touched = new Vector2(getTouchDownX(), getTouchDownY());
            	Vector2 dragged = new Vector2(x, y);
            	
            	wheelHandle.localToStageCoordinates(center);
            	wheelHandle.localToStageCoordinates(touched);
            	wheelHandle.localToStageCoordinates(dragged);

				float degrees = (float) (
						(Math.atan2(dragged.x - center.x, dragged.y - center.y) * 180.0d / Math.PI)
						- (Math.atan2(touched.x - center.x, touched.y - center.y) * 180.0d / Math.PI));
				
            	BluetoothRC.getInstance().setDirection(BluetoothRC.getInstance().getDirection() + degrees);            	
            	// Gdx.app.log(BluetoothRC.LOG, "wheelHandle DragListener: x" + x + ", y " + y + " degrees " + degrees);
            }
        });		

		
		this.addActor(wheelHandle);
		
		stopBtn = new Image(new Texture(Gdx.files.internal("graphics/stopbtn.png")));
		stopBtn.setPosition(Gdx.graphics.getWidth() - stopBtn.getWidth(), Gdx.graphics.getHeight() - stopBtn.getHeight());
		stopBtn.addListener(new InputListener() {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				
				spdHandle.setPosition(spdHandle.getX(), (int) zeroPos);
				BluetoothRC.getInstance().setThrottle(0);
				return super.touchDown(event, x, y, pointer, button);
			}
		});
		this.addActor(stopBtn);
	}
		
	@Override
	public void act (float delta) {
		
		BluetoothRC btRC = BluetoothRC.getInstance(); 
		float angle = btRC.getDirection();
		
		if (angle != wheelRotation) {
			
			wheelHandle.rotate(wheelRotation - angle);
			wheelRotation = angle;
		}
		
		// centering spring
		if (!btRC.isWheelDragged()) {
			angle += angle > 0 ? -0.1 : 0.1;
			btRC.setDirection(angle);
		}
		
		BTTransmitter transmitter = btRC.getTransmitter();
		
		if (transmitter != null
				&& System.currentTimeMillis() > mLastSendTime + 100) {

			String readData = transmitter.read();
			if (readData != null && readData.length() > 0) {

				// TODO: handle input data
			}

			float absAngle = Math.abs(angle);
			int throttle = btRC.getThrottle();

			if (absAngle > 90)
				absAngle = 90;

			String cmd = (angle > 0 ? "R" : "L") + (int) (absAngle / 10);
			cmd += (throttle > 0 ? "F" : "B") + Math.abs(throttle);

			Gdx.app.log(BluetoothRC.LOG, "Sending cmd: " + cmd);
			transmitter.write(cmd);
			mLastSendTime = System.currentTimeMillis();
		}
		
		super.act( delta );		
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha)
	{
		super.draw (batch, parentAlpha);

		if( BluetoothRC.DEV_MODE ) {
			
			batch.end();
			
			shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(1, 1, 0, 1);

			float width = Gdx.graphics.getWidth();
			float height = Gdx.graphics.getHeight();

			Matrix4 mrt = new Matrix4();
			Gdx.input.getRotationMatrix(mrt.val);

			Matrix4 matrix = new Matrix4();

			matrix.set(batch.getProjectionMatrix());
			matrix.translate(width / 2, height / 2, 0);
			// matrix.scl(x, y, 1); // just to remember
			matrix.rotate(0, 0, 1, -wheelRotation);

			shapeRenderer.setProjectionMatrix(matrix);
			shapeRenderer.rect(0, 0, 50, 50);
			shapeRenderer.end();

			batch.begin();
		}
	}	
}
		