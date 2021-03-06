package com.shinymetal.bluetoothrc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import com.badlogic.gdx.backends.android.AndroidApplication;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.*;
import android.content.DialogInterface;
import android.content.Intent;

public class BTRCMainActivity extends AndroidApplication implements BTTransmitter {
	
    private float[] mGravity;
    private float[] mMagnetic;

    private BluetoothRC mGame = BluetoothRC.getInstance();
    
    private BluetoothAdapter mBluetoothAdapter;  
    private BluetoothSocket mBluetoothSocket = null;
    private ConnectedThread mConnectedThread = null;
    private Set<BluetoothDevice> mFoundDevices;
    
    private static final int RECEIVE_MESSAGE = 1;        // Status  for Handler
    private Handler mHandler;
    private StringBuilder mStringBuilder = new StringBuilder();
    
    private static final int REQUEST_ENABLE_BT = 0x100;
    
    private static final boolean ENABLE_SENSOR = false;
    
    // TODO: replace with actual HC-06 uuid (Bluetooth Serial SPP: 00001101-0000-1000-8000-00805f9b34fb)
    private static final UUID hc06uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
	private SensorEventListener mSl = new SensorEventListener() {
		@SuppressWarnings("deprecation")
		@Override
		public void onSensorChanged(SensorEvent event) {

			switch (event.sensor.getType()) {

			case Sensor.TYPE_ACCELEROMETER:
				mGravity = event.values.clone();
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				// Log.d("SensorEventListener", "TYPE_MAGNETIC_FIELD");
				mMagnetic = event.values.clone();
				break;
			default:
				return;
			}

			final int axisSwap[][] = {
					{ 1, -1, 0, 1 }, // ROTATION_0
					{ -1, -1, 1, 0 }, // ROTATION_90
					{ -1, 1, 0, 1 }, // ROTATION_180
					{ 1, 1, 1, 0 } }; // ROTATION_270

			final int[] as = axisSwap[((WindowManager) getSystemService(WINDOW_SERVICE))
					.getDefaultDisplay().getOrientation()];
			
			if (mGravity != null) {
				if (mMagnetic != null) {
					
			        float[] temp = new float[9];
			        float[] R = new float[9];
			        
			        // Load rotation matrix into R
			        SensorManager.getRotationMatrix(temp, null, mGravity, mMagnetic);
			      
			        // Remap to camera's point-of-view
			        SensorManager.remapCoordinateSystem(temp,
			        		SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, R);
			      
			        // Return the orientation values
			        float[] values = new float[3];
			        SensorManager.getOrientation(R, values);
			      
			        //Convert to degrees
			        for (int i=0; i < values.length; i++) {
			            Double degrees = (values[i] * 180) / Math.PI;
			            values[i] = degrees.floatValue();
			        }

					// Log.d("SensorEventListener", "tilt1 " + values[0] + " degrees");
					if (!mGame.isWheelDragged() && ENABLE_SENSOR)
						mGame.setDirection(values[0]);
					
				} else {
					float adjustedValues = (float) as[0] * event.values[as[2]];
					
					float tilt = adjustedValues / SensorManager.GRAVITY_EARTH * 90;					

					// Log.d("SensorEventListener", "tilt2 " + tilt + " degrees");					
					// TODO: Why do I need offset here?
					if (!mGame.isWheelDragged() && ENABLE_SENSOR)
						mGame.setDirection(tilt /* - 4*/);
				}
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// Log.d("SensorEventListener", "onAccuracyChanged: " + sensor + ", accuracy: " + accuracy);
		}
	};
	
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
		// Log.d("SensorEventListener", "Sensor.TYPE_ACCELEROMETER " + accel);
		// Log.d("SensorEventListener", "Sensor.TYPE_MAGNETIC_FIELD " + magnet);
		
		sensorManager.registerListener(mSl,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME);
		sensorManager.registerListener(mSl,
				sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_GAME);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		
		mGame.setTransmitter (this);		
		mHandler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				
				switch (msg.what) {
				
				case RECEIVE_MESSAGE: // if receive massage
					byte[] readBuf = (byte[]) msg.obj;
					String strIncom = new String(readBuf, 0, msg.arg1); 
					mStringBuilder.append(strIncom); // append string
					break;
				}
			};
		};
	}

	// @SuppressLint("NewApi")
	private BluetoothSocket createBluetoothSocket(BluetoothDevice device)
			throws IOException {
		
		// for (int i=0; i<device.getUuids().length; i++)
		//	Log.d("createBluetoothSocket", i + ": " + device.getUuids()[i].toString());
		
		if (Build.VERSION.SDK_INT >= 10) {
			try {
				final Method m = device.getClass().getMethod(
						"createInsecureRfcommSocketToServiceRecord",
						new Class[] { UUID.class });
				return (BluetoothSocket) m.invoke(device, hc06uuid);
				    
			} catch (Exception e) {
				Log.e("createBluetoothSocket",
						"Could not create Insecure RFComm Connection", e);
			}
		}

		return device.createRfcommSocketToServiceRecord(hc06uuid);
	}
	
    @Override
    public void onResume() {

		// TODO: move to menu options
		if (mBluetoothAdapter != null) {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			} else {
				mFoundDevices = mBluetoothAdapter.getBondedDevices();
			}
		}

    	super.onResume();
    }
    
    @Override
    public void onPause() {
    	
    	super.onPause();
    }
    
	private void connect(BluetoothDevice device) {

		// Two things are needed to make a connection:
		// A MAC address, which we got above.
		// A Service ID or UUID. In this case we are using the
		// UUID for SPP.

		try {
			mBluetoothSocket = createBluetoothSocket(device);
		} catch (IOException e) {
			// TODO
			Log.e("Fatal Error",
					"In onResume() and socket create failed: " + e.getMessage()	+ ".");
		}

		// Discovery is resource intensive. Make sure it isn't going on
		// when you attempt to connect and pass your message.
		mBluetoothAdapter.cancelDiscovery();

		// Establish the connection. This will block until it connects.
		Log.d("connect", "...Connecting...");
		try {
			mBluetoothSocket.connect();
			Log.d("connect", "....Connection ok...");
		} catch (IOException e) {
			try {
				mBluetoothSocket.close();
			} catch (IOException e2) {
				// TODO
				// errorExit("Fatal Error",
				// "In onResume() and unable to close socket during connection failure"
				// + e2.getMessage() + ".");
			}
		}

		// Create a data stream so we can talk to server.
		Log.d("connect", "...Create Socket...");

		mConnectedThread = new ConnectedThread(mBluetoothSocket);
		mConnectedThread.start();
	}
	
	// This one should be called to send data to the device
	public void write(String message) {
		
		if (mConnectedThread != null && mConnectedThread.isAlive()) {
			mConnectedThread.write(message);
		}		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.btrcmain, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_select:
			// TODO: new dialog with device selection, call connect() if selected
			
			LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.device_dialog, null);			
			DeviceListAdapter da = new DeviceListAdapter(mFoundDevices);
			
		    AlertDialog.Builder builder = new AlertDialog.Builder(BTRCMainActivity.this);
		    builder.setView(layout);
		    
			final ListView lView = (ListView) layout.findViewById(R.id.deviceList);
			final AlertDialog alertDialog = builder.create();
			
			lView.setAdapter(da);
			lView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {

					BluetoothDevice d = (BluetoothDevice) lView.getItemAtPosition(position);
					
					connect(d);
					alertDialog.dismiss();
				}
			});

		    alertDialog.setTitle(getString(R.string.action_select));
		    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.label_cancel),
					new DialogInterface.OnClickListener() {
	
						public void onClick(final DialogInterface dialog,
								final int which) {

						}
					});

		    alertDialog.show();			
			return true;
		default:
		}
		return true;

	}
	
	private class ConnectedThread extends Thread {
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		@SuppressLint("NewApi")
		public ConnectedThread(BluetoothSocket socket) {
			
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			Log.d("Thread.run()", "socket is connected: " + socket.isConnected());
			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				
				Log.e("Thread.run()", "streams: " + e.toString());
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
			
			Log.d("Thread.run()", "... try write ...");
			
			// TODO: remove later
			byte[] msgBuffer = new String("L6R6L0").getBytes();
			try {
				mmOutStream.write(msgBuffer);
				mmOutStream.flush();
			} catch (IOException e) {
				Log.e("Thread.run()", "write(): " + e.toString());
			}
			
			Log.d("Thread.run()", "... done ...");
		}

		public void run() {
			byte[] buffer = new byte[256];
			int bytes;

			while (true) {
				try {
					bytes = mmInStream.read(buffer);
					mHandler.obtainMessage(RECEIVE_MESSAGE, bytes, -1, buffer).sendToTarget();
					
				} catch (IOException e) {
					break;
				}
			}
		}

		/* Call this from the main activity to send data to the remote device */
		public void write(String message) {

			try {
				byte[] msgBuffer = message.getBytes("US-ASCII");
				mmOutStream.write(msgBuffer);
			} catch (Exception e) {
				// TODO
			}
		}
	}

	@Override
	public String read() {
		
//		int endOfLineIndex = mStringBuilder.indexOf("\n");
//		if (endOfLineIndex > 0) {
//
//			String sbprint = mStringBuilder.substring(0, endOfLineIndex); // extract
//			mStringBuilder.delete(0, mStringBuilder.length()); // and clear
//			
//			return sbprint;
//		}

		if (mStringBuilder.length() > 0) {
			
			String sbprint = mStringBuilder.toString();
			mStringBuilder.delete(0, mStringBuilder.length()); // and clear
			return sbprint;
		}
		
		return null;
	}
}
