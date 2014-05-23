package org.opencv.samples.facedetect;

import org.opencv.samples.facedetect.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	
	private Button mBtnTakePictureOpenCV;
	private Button mBtnTakePictureAndroid;
	private Button mBtnSendCoordinates;
	private static boolean mSendCoordinates = false;
	
	private Intent mLocationServiceIntent;

	// Choose to either start sending coordinates to server to indicate you wish your face to be blurred from
	// images taken by others, or
	// choose to take a picture
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Set's unique user id for each device or loads id from previous session
		Utility.setUniqueUserId(getApplicationContext());
		
		mBtnTakePictureOpenCV = (Button) findViewById(R.id.btn_take_picture_opencv);
		mBtnTakePictureAndroid = (Button) findViewById(R.id.btn_take_picture_android);

		mBtnSendCoordinates = (Button) findViewById(R.id.btn_send_location);
		
		mBtnTakePictureAndroid.setOnClickListener(this);
		mBtnTakePictureOpenCV.setOnClickListener(this);
		mBtnSendCoordinates.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		
		case R.id.btn_take_picture_opencv:
			Intent intent1 = new Intent(this, FdActivity.class);
			startActivity(intent1);
			break;
			
		case R.id.btn_take_picture_android:
			Intent intent2 = new Intent(this, CaptureImageActivity.class);
			startActivity(intent2);
			break;
			
		case R.id.btn_send_location:
			
			mSendCoordinates = !mSendCoordinates; // static value, shared across intents
			
			if (mLocationServiceIntent == null)
				mLocationServiceIntent = new Intent(this, LocationService.class);
			
			if (mSendCoordinates) 
				startService(mLocationServiceIntent);
			
			else { 
//				Toast.makeText(getApplicationContext(), "stop service called", Toast.LENGTH_SHORT).show();
				stopService(mLocationServiceIntent);	
			}
			break;
		}
	}

}
