package org.opencv.samples.facedetect;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class CaptureImageActivity extends Activity implements View.OnClickListener {
	
	// Future Activity representing image taking interface
	
	private Button mButtonCamera;
	private Button mButtonMainMenu;

	private Uri mCapturedImageURI;
	
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_capture_image);
		
    	mButtonCamera = (Button) findViewById(R.id.btn_take_pic);
    	mButtonMainMenu = (Button) findViewById(R.id.btn_main_menu);
    	mButtonCamera.setOnClickListener(this);
    	mButtonMainMenu.setOnClickListener(this);
    	
    	// automatically go to Camera API
//    	runCameraIntent();
	}
	
	public void runCameraIntent() {
		String fileName = "temp.jpg";  
        ContentValues values = new ContentValues();  
        values.put(MediaStore.Images.Media.TITLE, fileName);  
        mCapturedImageURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);  

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);  
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d("activity destoryed", "activity destoryed");
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				
				String[] projection = { MediaStore.Images.Media.DATA}; 
				
	            @SuppressWarnings("deprecation")
				Cursor cursor = managedQuery(mCapturedImageURI, projection, null, null, null); 
	            int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA); 
	            cursor.moveToFirst(); 
	            String capturedImageFilePath = cursor.getString(column_index_data);
							
				// Now pass the filename over to the processing activity
				Intent filterIntent = new Intent(this, ProcessImageActivity.class);
				filterIntent.putExtra("filepath", capturedImageFilePath);
				startActivity(filterIntent);
				
			} else if (resultCode == Activity.RESULT_CANCELED) {
				// user cancelled the image capture
			} else {
				// image capture failed, advise user
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btn_take_pic:
			runCameraIntent();
			break;
			
		case R.id.btn_main_menu:
			Intent mainMenuIntent = new Intent(this, MainActivity.class);
			startActivity(mainMenuIntent);
			break;
		}
	}
	
}
