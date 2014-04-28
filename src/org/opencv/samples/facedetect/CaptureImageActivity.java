package org.opencv.samples.facedetect;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.SimpleAdapter.ViewBinder;

public class CaptureImageActivity extends Activity {
	
	// Future Activity representing image taking interface
	
	private static final boolean DEBUG = false;
	
	private static final String TAG = "Photoprivacy::CaptureImageActivity";
	
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private Uri fileUri;
	
	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";
	
	private String fileName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_capture_image);
		
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		
		// code below this should be called onButtonClick();
		File filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    	fileName = createImageFileName();
    	
    	
    	File file = new File(filePath, "image");
    	cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, file);

    	
    	startActivityForResult(cameraIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    	
    	Button mButton = (Button) findViewById(R.id.btn_take_pic);
    	mButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		    	startActivityForResult(cameraIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			}
		});
    	
    	if (DEBUG) {
	    	Log.i(TAG, "file name: " + fileName);
	    	Log.i(TAG, "file name: " + file);
	    	Toast.makeText(getApplicationContext(), file.toString(), Toast.LENGTH_SHORT).show();
    	}
    	
	}

	private String createImageFileName() {
		
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		return imageFileName;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			
			if (resultCode == RESULT_OK) {
				// image captured and saved
				Uri selectedImage = data.getData();
				
				String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
               //file path of captured image
                String filePath = cursor.getString(columnIndex); 
                
                //file path of captured image
                File f = new File(filePath);
                String filename= f.getName();
                cursor.close();

                if (DEBUG) {
    				Toast.makeText(getApplicationContext(), "image saved to" + data.getData(), Toast.LENGTH_LONG).show();
					Log.i(TAG, "image saved to " + data.getData());
	                Toast.makeText(getApplicationContext(), "Your Path:"+filePath, 2000).show();
	                Toast.makeText(getApplicationContext(), "Your Filename:"+filename, 2000).show();
                }
				
                
                // Now pass the filename over to the processing activity
                Intent filterIntent = new Intent(getApplicationContext(), ProcessImageActivity.class);
                filterIntent.putExtra("filename", filename);
                filterIntent.putExtra("filepath", filePath);
                startActivity(filterIntent);
                
                // or better yet simply do
                // startActivity(data);
			} else if (resultCode == RESULT_CANCELED) {
				
			} else {
				// Image capture failed
			}
			
		}
	}
}
