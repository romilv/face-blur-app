package org.opencv.samples.facedetect;

import java.util.HashMap;
import java.util.Map;

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
	private TextView mAzimuthText, mPitchText, mCompass, mCompass2;
	private int mSensorCallCount = 0;
	
	private final float RADIANS_TO_DEGREE = 57.2957795f;
	private final boolean DEBUG = false;

	
	// method 1, take average of values
	// bug when moving from SW to SE (very -ve value to very +ve)
	// when averaging from SW to SE, the result goes to NE or NW for a split second
	
	private float[] mAzimuthHistory;
	private boolean mHistoryOverflow;
	private int mCurrentCounter;
	private final int AZIMUTH_HISTORY_LENGTH = 60;
	private final int HISTORY_TO_CONSIDER = 40;
	
	// method 2, assign numbers to the compass directions and on basis of count
	// choose direction
	// method 2 fixes the bug in method 1
	
	private final int NW = 1, NE = 2, SW = 3, SE = 4;
	private int[] mAzimuthHistoryByCount;

	
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
		mCompass2 = (TextView) findViewById(R.id.tv_compass2);

		
		// used in tracking history of azimuth values so that we can average the direction out when
		// picture is taken
		mAzimuthHistory = new float[AZIMUTH_HISTORY_LENGTH];
		mAzimuthHistoryByCount = new int[AZIMUTH_HISTORY_LENGTH];
		mHistoryOverflow = false;
		mCurrentCounter = 0;
		
		mButton = (Button) findViewById(R.id.btn_orientation_sensor);
		mButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mOrientationSensor.m_OrientationOK) {
					mAzimuth = mOrientationSensor.m_azimuth_radians;
					mAzimuthText.setText(Float.toString(mOrientationSensor.m_azimuth_radians));
					mPitchText.setText(Float.toString(mOrientationSensor.m_pitch_radians));
					
					float sum = 0.0f;
					float finalAzimuth = 0.0f;
					
					Map<Integer, Integer> directionMap = new HashMap<Integer, Integer>();
					directionMap.put(NW, 0);
					directionMap.put(NE, 0);
					directionMap.put(SW, 0);
					directionMap.put(SE, 0);

					int key;
					if (!mHistoryOverflow) {
						for (int i = 0; i < mCurrentCounter; i++) {
							sum += mAzimuthHistory[i];
							key = mAzimuthHistoryByCount[i];
							directionMap.put(key, directionMap.get(key)+1);
						}
						
						finalAzimuth = sum / mCurrentCounter;
					}
					
					else {
						
						if (HISTORY_TO_CONSIDER > mCurrentCounter) {
							
							int difference = HISTORY_TO_CONSIDER - mCurrentCounter;
							
							for (int i = 0; i < mCurrentCounter; i++) {
								sum += mAzimuthHistory[i];
								key = mAzimuthHistoryByCount[i];
								directionMap.put(key, directionMap.get(key)+1);
							}
							
							for (int i = AZIMUTH_HISTORY_LENGTH - difference; i < AZIMUTH_HISTORY_LENGTH; i++) {
								sum += mAzimuthHistory[i];
								key = mAzimuthHistoryByCount[i];
								directionMap.put(key, directionMap.get(key)+1);
							}
							
							finalAzimuth = sum / HISTORY_TO_CONSIDER;
						} // end if
						
						else if (HISTORY_TO_CONSIDER <= mCurrentCounter) {
							for (int i = 0; i < HISTORY_TO_CONSIDER; i++) {
								sum += mAzimuthHistory[i];
								key = mAzimuthHistoryByCount[i];
								directionMap.put(key, directionMap.get(key)+1);
							}
							
							finalAzimuth = sum / HISTORY_TO_CONSIDER;
						}
					}
					
					int countNW, countNE, countSW, countSE;
					countNW = directionMap.get(NW);
					countNE = directionMap.get(NE);
					countSE = directionMap.get(SE);
					countSW = directionMap.get(SW);
					
					int direction = countNW > countNE ? NW : NE;
					int directionCount = countNW > countNE ? countNW : countNE;
					direction = countSW > directionCount ? SW : direction;
					directionCount = countSW > directionCount ? countSW : directionCount;
					direction = countSE > directionCount ? SE : direction;
					directionCount = countSE > directionCount ? countSE : directionCount; 
					
					mAzimuthText.setText(Float.toString(finalAzimuth));
//					if (finalAzimuth < 0 && finalAzimuth > - 90)
//						mCompass.setText("North-West");
//					else if (finalAzimuth > 0 && finalAzimuth < 90)
//						mCompass.setText("North-East");
//					else if (finalAzimuth > 90 && finalAzimuth < 180)
//						mCompass.setText("South-East");
//					else
//						mCompass.setText("South-West");
					
					if (direction == NW) mCompass.setText("North-West");
					else if (direction == NE) mCompass.setText("North-East");							
					else if (direction == SE) mCompass.setText("South-East");
					else if (direction == SW) mCompass.setText("South-West");

					mCompass.append("\n" + mSensorCallCount);
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
			mOrientationSensor = null;
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
		mSensorCallCount++;
		
		if (mSensorCallCount == AZIMUTH_HISTORY_LENGTH) {
			mHistoryOverflow = true;
		}
		
		
		mAzimuth = mOrientationSensor.m_azimuth_radians * RADIANS_TO_DEGREE;		
		mCurrentCounter = mSensorCallCount % AZIMUTH_HISTORY_LENGTH;
		
		// method 1
		mAzimuthHistory[mCurrentCounter] = mAzimuth;
		
		// method 2
		if (mAzimuth < 0 && mAzimuth > - 90)
			mAzimuthHistoryByCount[mCurrentCounter] = NW;
		else if (mAzimuth > 0 && mAzimuth < 90)
			mAzimuthHistoryByCount[mCurrentCounter] = NE;
		else if (mAzimuth > 90 && mAzimuth < 180)
			mAzimuthHistoryByCount[mCurrentCounter] = SE;
		else
			mAzimuthHistoryByCount[mCurrentCounter] = SW;
		
		
		if (DEBUG) {
			if (mAzimuth < 0 && mAzimuth > - 90)
				mCompass2.setText("North-West");
			else if (mAzimuth > 0 && mAzimuth < 90)
				mCompass2.setText("North-East");
			else if (mAzimuth > 90 && mAzimuth < 180)
				mCompass2.setText("South-East");
			else
				mCompass2.setText("South-West");
		}
		
	}
}
