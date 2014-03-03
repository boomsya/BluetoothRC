package com.shinymetal.bluetoothrc;

import com.badlogic.gdx.backends.android.AndroidApplication;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class BTRCMainActivity extends AndroidApplication {
	
    private float[] mGravity;
    private float[] mMagnetic;

    BluetoothRC mGame = BluetoothRC.getInstance();

	private SensorEventListener mSl = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {

			switch (event.sensor.getType()) {

			case Sensor.TYPE_ACCELEROMETER:
				mGravity = event.values.clone();
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				mMagnetic = event.values.clone();
				break;
			default:
				return;
			}

			if (mGravity != null && mMagnetic != null) {
				mGame.setDirection(getDirection());
			}
		}

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// TODO Auto-generated method stub
		}
	};

	private float getDirection()
    {
        float[] temp = new float[9];
        float[] R = new float[9];
        
        // Load rotation matrix into R
        SensorManager.getRotationMatrix(temp, null,
                mGravity, mMagnetic);
      
        // Remap to camera's point-of-view
        SensorManager.remapCoordinateSystem(temp,
        		SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, R);
      
        // Return the orientation values
        float[] values = new float[3];
        SensorManager.getOrientation(R, values);
      
        //Convert to degrees
        for (int i=0; i < values.length; i++) {
            Double degrees = (values[i] * 180) / Math.PI;
            values[i] = degrees.floatValue();
        }

        return values[0];      
    }	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	     // whether to use OpenGL ES 2.0
        boolean useOpenGLES2 = true;

		// Create the layout
		RelativeLayout layout = new RelativeLayout(this);

		// Do the stuff that initialize() would do for you
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// prevent the screen from dimming/sleeping (no permission required)
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().clearFlags(
				WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

		View gameView = initializeForView(mGame, useOpenGLES2);
		
		layout.addView(gameView);
		setContentView(layout);

		SensorManager sensorManager = (SensorManager) getSystemService(BTRCMainActivity.SENSOR_SERVICE);
		Sensor accel = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		Sensor magnet = sensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		Handler handler = new Handler();
		
		sensorManager.registerListener(mSl, accel,
				SensorManager.SENSOR_DELAY_FASTEST, handler);
		sensorManager.registerListener(mSl, magnet,
				SensorManager.SENSOR_DELAY_FASTEST, handler);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.btrcmain, menu);
		return true;
	}
}
