package org.opencv.samples.facedetect;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class OrientationSensorActivity extends Activity implements SensorEventListener{
	
	private OrientationSensor mOrientationSensor;
	private SensorManager mSensorManager;
	private Button mButton;
	private Float mAzimuth;
	private TextView mAzimuthText, mPitchText, mCompass;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_orientation_sensor);
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

//		mOrientationSensor = new OrientationSensor(mSensorManager, );
		mOrientationSensor = new OrientationSensor(mSensorManager, OrientationSensorActivity.this);
		
		mAzimuthText = (TextView) findViewById(R.id.tv_azimuth);
		mPitchText = (TextView) findViewById(R.id.tv_pitch);
		mCompass = (TextView) findViewById(R.id.tv_compass);
		
		mButton = (Button) findViewById(R.id.btn_orientation_sensor);
		mButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mOrientationSensor.m_OrientationOK) {
					mAzimuth = mOrientationSensor.m_azimuth_radians;
					mAzimuthText.setText(Float.toString(mOrientationSensor.m_azimuth_radians));
					mPitchText.setText(Float.toString(mOrientationSensor.m_pitch_radians));
					
					
				}
			}
		});
		
		mAzimuthText.setText(Float.toString(mOrientationSensor.m_azimuth_radians));

	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// count should equal 2 for TYPE_GRAVITY and TYPE_MAGNETIC_FIELD
		int count = mOrientationSensor.Register(OrientationSensorActivity.this, SensorManager.SENSOR_DELAY_UI);
		if (count != 2) {
			Toast.makeText(getApplicationContext(), "Sensors required not available on this device", Toast.LENGTH_SHORT).show();
			mOrientationSensor.Unregister();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mOrientationSensor.Unregister();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.orientation_sensor, menu);
		return true;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		mAzimuth = mOrientationSensor.m_azimuth_radians * 57.2957795f;
		mAzimuthText.setText(Float.toString(mAzimuth));
		
		if (mAzimuth < 0 && mAzimuth > - 90)
			mCompass.setText("North-West");
		else if (mAzimuth > 0 && mAzimuth < 90)
			mCompass.setText("North-East");
		else if (mAzimuth > 90 && mAzimuth < 180)
			mCompass.setText("South-East");
		else
			mCompass.setText("South-West");
				
	}
}
