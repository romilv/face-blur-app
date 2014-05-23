package org.opencv.samples.facedetect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.SimpleAdapter.ViewBinder;

public class CaptureImageActivity extends Activity implements View.OnClickListener {
	
	// Future Activity representing image taking interface
	
	private Button mButtonCamera;
	private Button mButtonMainMenu;
	private Bitmap mBitmap;
	private static final boolean DEBUG = false;
	
	private static final String TAG = "Photoprivacy::CaptureImageActivity";
	
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	
	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";

	// save image
	private Uri fileUri;
	private String fileName;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;


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
		
//		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//		
//		// code below this should be called onButtonClick();
//		File filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//    	fileName = createImageFileName();
//    	
//    	
//    	File file = new File(filePath, "image");
//    	cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, file);
//
//    	
//    	startActivityForResult(cameraIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

    	
	}
	
	public void runCameraIntent() {
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
	    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
    	startActivityForResult(cameraIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}
	
	// ***********************
	// moved to utility
	// ***********************
	
//	private String createImageFileName() {
//		
//		// Create an image file name
//		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
//		return imageFileName;
//	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d("activity destoryed", "activity destoryed");
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// Image captured and saved to fileUri specified in the Intent
				Toast.makeText(this, "Image saved to:\n"+ data.getData(), Toast.LENGTH_LONG).show();
				
				Intent processImageIntent = new Intent(this, ProcessImageActivity.class);
				
			} else if (resultCode == RESULT_CANCELED) {
				// user cancelled the image capture
			} else {
				// image capture failed, advise user
			}
		}
	}

//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//		super.onActivityResult(requestCode, resultCode, data);
//		
//		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
//			
//			if (resultCode == RESULT_OK) {
//				
//				if (mBitmap != null) {
//					mBitmap.recycle();
//				}
//				// new
//				
//				try {
//				InputStream mDataStream = null;
//				
//				// image captured and saved
//				Uri selectedImage = data.getData();
//				mDataStream = getContentResolver().openInputStream(data.getData());
//				mBitmap = BitmapFactory.decodeStream(mDataStream);
//				
//				} catch (FileNotFoundException e) {
//					e.printStackTrace();
//				}
//				// end new
//				
////				String[] filePathColumn = {MediaStore.Images.Media.DATA};
////
////                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
////                cursor.moveToFirst();
////
////                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
////               //file path of captured image
////                String filePath = cursor.getString(columnIndex); 
////                
////                //file path of captured image
////                File f = new File(filePath);
////                String filename= f.getName();
////                cursor.close();
//
//                if (DEBUG) {
//    				Toast.makeText(getApplicationContext(), "image saved to" + data.getData(), Toast.LENGTH_LONG).show();
//					Log.i(TAG, "image saved to " + data.getData());
//	                Toast.makeText(getApplicationContext(), "Your Path:"+filePath, 2000).show();
//	                Toast.makeText(getApplicationContext(), "Your Filename:"+filename, 2000).show();
//                }
//				
//                
//                // Now pass the filename over to the processing activity
//                Intent filterIntent = new Intent(getApplicationContext(), ProcessImageActivity.class);
//                filterIntent.putExtra("filename", filename);
//                filterIntent.putExtra("filepath", filePath);
//                startActivity(filterIntent);
//                
//                // or better yet simply do
//                // startActivity(data);
//			} else if (resultCode == RESULT_CANCELED) {
//				
//			} else {
//				// Image capture failed
//			}
//			
//		}
//	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btn_take_pic:
//			runCameraIntent();
			break;
			
		case R.id.btn_main_menu:
			Intent mainMenuIntent = new Intent(this, MainActivity.class);
			startActivity(mainMenuIntent);
			break;
		}
	}
	
	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type){
	      return Uri.fromFile(getOutputMediaFile(type));
	}
	
	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "FaceBlur");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("FaceBlurApp", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    } else if(type == MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "VID_"+ timeStamp + ".mp4");
	    } else {
	        return null;
	    }

	    return mediaFile;
	}
	
}
