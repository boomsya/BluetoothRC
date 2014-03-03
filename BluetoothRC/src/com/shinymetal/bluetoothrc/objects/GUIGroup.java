package com.shinymetal.bluetoothrc.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.shinymetal.bluetoothrc.BluetoothRC;

public class GUIGroup
		extends Group {
	
    private ShapeRenderer shapeRenderer;
    
	public GUIGroup () {
		shapeRenderer = new ShapeRenderer ();
		
	}
		
	@Override
	public void act (float delta) {
		
		super.act( delta );	
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha)
	{
		super.draw (batch, parentAlpha);

		batch.end();
		shapeRenderer.setProjectionMatrix( batch.getProjectionMatrix() );
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(1, 1, 0, 1);

		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();

		shapeRenderer.rect(5, 5, width - 10, height - 10);
		
	    Matrix4 mrt = new Matrix4();
	    Gdx.input.getRotationMatrix(mrt.val);

		Matrix4 matrix = new Matrix4();

		matrix.set( batch.getProjectionMatrix() );
		matrix.translate (width / 2, height / 2, 0);
//		matrix.scl(x, y, 1);  // just to remember
		matrix.rotate( 0, 0, 1, BluetoothRC.getInstance().getDirection() );

	    shapeRenderer.setProjectionMatrix(matrix);
		shapeRenderer.rect(0, 0, 50, 50);
		shapeRenderer.end();
		
		batch.begin();
	}	
}
		