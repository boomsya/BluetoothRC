package com.shinymetal.bluetoothrc;

import com.badlogic.gdx.backends.android.AndroidApplication;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.bluetooth.*;
import android.content.Intent;

public class BTRCMainActivity extends AndroidApplication {
	
    private float[] mGravity;
    private float[] mMagnetic;

    BluetoothRC mGame = BluetoothRC.getInstance();
    
    BluetoothAdapter mBluetoothAdapter;
    static final int REQUEST_ENABLE_BT = 0x100;
    
	private SensorEventListener mSl = new SensorEventListener() {
		@SuppressWarnings("deprecation")
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

			if (mGravity != null) {
				if (mMagnetic != null) {

					// Log.d("SensorEventListener", "tilt1 " + getDirection() + " degrees");
					mGame.setDirection(getDirection());
					
				} else {
					float[] adjustedValues = new float[3];

					final int axisSwap[][] = {
							{ 1, -1, 0, 1 }, // ROTATION_0
							{ -1, -1, 1, 0 }, // ROTATION_90
							{ -1, 1, 0, 1 }, // ROTATION_180
							{ 1, 1, 1, 0 } }; // ROTATION_270

					final int[] as = axisSwap[((WindowManager) getSystemService(WINDOW_SERVICE))
							.getDefaultDisplay().getOrientation()];
					
					adjustedValues[0] = (float) as[0] * event.values[as[2]];
					// adjustedValues[1] = (float) as[1] * event.values[ as[3] ];
					// adjustedValues[2] = event.values[2];
					
					float tilt = adjustedValues[0] / SensorManager.GRAVITY_EARTH * 90;
					// Log.d("SensorEventListener", "tilt2 " + tilt + " degrees");
					mGame.setDirection(tilt);
				}
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// Log.d("SensorEventListener", "onAccuracyChanged: " + sensor + ", accuracy: " + accuracy);
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
		
		// Log.d("SensorEventListener","Sensor.TYPE_ACCELEROMETER " + accel);
		// Log.d("SensorEventListener","Sensor.TYPE_MAGNETIC_FIELD " + magnet);
		
		sensorManager.registerListener(mSl, accel,
				SensorManager.SENSOR_DELAY_GAME, handler);
		sensorManager.registerListener(mSl, magnet,
				SensorManager.SENSOR_DELAY_GAME, handler);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (mBluetoothAdapter != null) {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			} else {
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.btrcmain, menu);
		return true;
	}
}
