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
	
	private Button mBtnTakePicture;
	private Button mBtnSendCoordinates;
	private boolean mSendCoordinates = false;
	
	private Intent mLocationServiceIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Utility.setUniqueUserId(getApplicationContext());
		
		mBtnTakePicture = (Button) findViewById(R.id.btn_take_picture);
		mBtnSendCoordinates = (Button) findViewById(R.id.btn_send_location);
		
		mBtnTakePicture.setOnClickListener(this);
		mBtnSendCoordinates.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_take_picture:
			Intent intent1 = new Intent(this, FdActivity.class);
			startActivity(intent1);
			break;
		case R.id.btn_send_location:
			mSendCoordinates = !mSendCoordinates;
			if (mLocationServiceIntent == null)
				mLocationServiceIntent = new Intent(this, LocationService.class);
			if (mSendCoordinates) 
				startService(mLocationServiceIntent);
			else { 
				Toast.makeText(getApplicationContext(), "stop service called", Toast.LENGTH_SHORT).show();
				stopService(mLocationServiceIntent);
				
			}
			break;
		}
	}

}
