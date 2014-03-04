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
import com.shinymetal.bluetoothrc.BluetoothRC;

public class GUIGroup
		extends Group {
	
    private ShapeRenderer shapeRenderer;
    
    private Actor spdHandle;
    private Actor wheelHandle;
    
    private float wheelRotation = BluetoothRC.getInstance().getDirection();
    private int throttle = 0;
    
    static final int SPD_LOW_MARK = 10;
    static final int SPD_HIGH_MARK = 90;
    
    static final int SPD_MAX_FWD = 9;
    static final int SPD_MAX_RWD = 4;
    
	public GUIGroup () {
		shapeRenderer = new ShapeRenderer ();

		spdHandle = new Image(new Texture(Gdx.files.internal("sphere.png")));

		// TODO: Remove once we have final image
		spdHandle.setSize(spdHandle.getWidth(), spdHandle.getHeight() / 2);
		
		float zeroPos = Gdx.graphics.getHeight() * SPD_LOW_MARK / 100
				+ Gdx.graphics.getHeight() * (SPD_HIGH_MARK - SPD_LOW_MARK) / 100
				* (SPD_MAX_RWD + 1) / (SPD_MAX_FWD + SPD_MAX_RWD + 1)
				- spdHandle.getHeight() / 2;
		
		spdHandle.setPosition(Gdx.graphics.getWidth() / 100 * 17 - spdHandle.getWidth(), zeroPos);
		spdHandle.addListener(new DragListener() {
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
            	
				float height = Gdx.graphics.getHeight();
				float lowMark = height * SPD_LOW_MARK / 100;
				float highMark = height * SPD_HIGH_MARK / 100;
				float spdY = spdHandle.getY() + y - getTouchDownY();

				if (spdY < lowMark - spdHandle.getHeight() / 2)
					spdY = lowMark - spdHandle.getHeight() / 2;
				if (spdY > highMark - spdHandle.getHeight() / 2)
					spdY = highMark - spdHandle.getHeight() / 2;

				spdHandle.setPosition(spdHandle.getX(), spdY);

				throttle = (int) ((spdY + spdHandle.getHeight() / 2 - lowMark)
						/ (highMark - lowMark)
						* (SPD_MAX_FWD + SPD_MAX_RWD + 1) - SPD_MAX_RWD - 1);
				Gdx.app.log(BluetoothRC.LOG, "throttle: " + throttle);
            }
        });		
		
		this.addActor(spdHandle);
		
		wheelHandle = new Image(new Texture(Gdx.files.internal("sphere.png")));

		// TODO: Remove once we have final image
		wheelHandle.setSize(Gdx.graphics.getWidth() * 65 / 100, Gdx.graphics.getWidth() * 65 / 100);
		wheelHandle.setPosition(Gdx.graphics.getWidth() / 10 * 3, wheelHandle.getHeight() / -10 * 4 );
		wheelHandle.setOrigin(wheelHandle.getWidth() / 2, wheelHandle.getHeight() / 2);
		wheelHandle.addListener(new DragListener() {
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
            	Gdx.app.log(BluetoothRC.LOG, "wheelHandle DragListener: x" + x + ", y " + y + " degrees " + degrees);
            }
        });		

		
		this.addActor(wheelHandle);
	}
		
	@Override
	public void act (float delta) {
		
		float angle = BluetoothRC.getInstance().getDirection();
		
		if (angle != wheelRotation) {
			
			wheelHandle.rotate(wheelRotation - angle);
			wheelRotation = angle;
		}
		
		// centering spring
		angle += angle > 0 ? -0.1 : 0.1;
		BluetoothRC.getInstance().setDirection(angle);
		
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
		