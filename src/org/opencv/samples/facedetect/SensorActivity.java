package org.opencv.samples.facedetect;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class SensorActivity extends Activity implements SensorEventListener {
	
	private static final String TAG = "SENSOR_ACTIVITY";
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private Sensor mMagnetometer;
	private Sensor mRotationVector;
	
	private float[] mLastAccelerometer = new float[3];
	private float[] mLastMagnetometer = new float[3];
	private float[] mLastRotationVector = new float[5];
	
	private boolean mLastAccelerometerSet = false;
	private boolean mLastMagnetometerSet = false;
	private boolean mLastRotationVectorSet = false;
	
	private float[] mRotationMatrix = new float[9];
	private float[] mRotationMatrixFromVector = new float[9];
	private float[] mOrientation = new float[3]; // azimuth, pitch, roll
	
	
	private TextView mOrientXPitch;
	private TextView mOrientYRoll;
	private TextView mOrientZAzimuth;
	
	private int mRoll2, mPitch1, mAzimuth0;
		
	// Activity simply reads recordings of Accelerometer and Magnetometer for purpose of mapping readings
	// to compass directions
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensor);
		
		// access recordings of Accelerometer and Magetometer
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		
		mOrientXPitch = (TextView) findViewById(R.id.pitch_x_1_value);
		mOrientYRoll = (TextView) findViewById(R.id.roll_y_2_value);
		mOrientZAzimuth = (TextView) findViewById(R.id.azimuth_z_0_value);
		
		mOrientXPitch.setText("0.00");
		mOrientYRoll.setText("0.00");
		mOrientZAzimuth.setText("0.00");
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mLastAccelerometerSet = false;
		mLastMagnetometerSet = false;
		mLastRotationVectorSet = false;
		
		// register sensors
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
		mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
		mSensorManager.registerListener(this, mRotationVector, SensorManager.SENSOR_DELAY_UI);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor == mAccelerometer) {
			System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
			mLastAccelerometerSet = true;
		} else if (event.sensor == mMagnetometer) {
			System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
			mLastMagnetometerSet = true;
		} 
		
		if (event.sensor == mRotationVector) {
			System.arraycopy(event.values, 0, mLastRotationVector, 0, event.values.length);
			mLastRotationVectorSet = true;
		}
		
		if (mLastRotationVectorSet) {
			SensorManager.getRotationMatrixFromVector(mRotationMatrixFromVector, mLastRotationVector);
			SensorManager.getOrientation(mRotationMatrixFromVector, mOrientation);
			
			mAzimuth0 = (int) ( mOrientation[0] * 57.2957795f);
			mPitch1 =  (int) (mOrientation[1] * 57.2957795f);
			mRoll2 = (int) (mOrientation[2] * 57.2957795f);
			
			mOrientXPitch.setText(String.format("%d", mPitch1));
			mOrientYRoll.setText(String.format("%d", mRoll2));
			mOrientZAzimuth.setText(String.format("%d", mAzimuth0));
		}
		
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {	}

}
