package org.opencv.samples.facedetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.Toast;

public class ProcessImageActivity extends Activity {
	
	private static final String TAG = "PhotoPrivacy::ProcessImageActivity";
	
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
	
	private static final boolean DEBUG = true;
	private Mat mMat;
	private File mCascadeFile;
	
	private ImageView mImageView;

//    private CascadeClassifier      mJavaDetector;
    private DetectionBasedTracker  mNativeDetector;
	
	private BaseLoaderCallback mOpenCvCallback = new BaseLoaderCallback(this) {
		
		public void onManagerConnected(int status) {
			switch(status) {
				case LoaderCallbackInterface.SUCCESS:
				{
					Log.i(TAG, "OpenCV Loaded Successfully");
					loadClassifier();
					loadImage();
				} break;
				
				default:
				{
					super.onManagerConnected(status);
				} break;
			}	
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_process_image);
		
		mImageView = (ImageView) findViewById(R.id.img_view);
	}
	
	protected void loadClassifier() {
		System.loadLibrary("detection_based_tracker");
		
        try {
            
        	// load cascade file from application resources
        	
            InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

//            mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
//            if (mJavaDetector.empty()) {
//                Log.e(TAG, "Failed to load cascade classifier");
//                mJavaDetector = null;
//            } else
//                Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

            mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

            cascadeDir.delete();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }
	}
	
	protected void loadImage() {
		
		Intent intent = getIntent();
//		intent.getStringExtra(name);

		File filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		String filename = intent.getStringExtra("filename");
		String filepath = intent.getStringExtra("filepath");
		
		// stub for file.getAbsolutePath();
//		String path = "storage/emulated/0/DCIM/Camera/20140427_014152.jpg";

		File file = new File(filePath, filename);
//		Log.i(TAG, "filePath " + filePath);
//		Log.i(TAG, "filepath " + filepath);
//		Log.i(TAG, "filename " + filename);
//		Log.i(TAG, "file " + file);


		mMat = Highgui.imread(filepath);
		

				
		// http://stackoverflow.com/questions/13727899/android-opencv-highgui-imread-wrong-color
		if (DEBUG) {
//			Log.i(TAG, "filename " + file.getAbsolutePath());
//			Log.i(TAG, "channels " + mMat.channels());
//			Log.i(TAG, "cols " + mMat.cols());
//			Log.i(TAG, "rows " + mMat.rows());
//			Log.i(TAG, "dims " + mMat.dims());
//			Log.i(TAG, "empty" + mMat.empty());
//			Log.i(TAG, "type" + mMat.type());
//			
//			Log.i(TAG, "size" + mMat.size());
		}
		
		if (mMat != null && !mMat.empty()) {
			detectFaces();
		}
	}
	
	protected void detectFaces() {
		MatOfRect faces = new MatOfRect();
		Mat mGrayMat = new Mat();
		
		Imgproc.cvtColor(mMat, mGrayMat, Imgproc.COLOR_BGR2GRAY);
		
		Log.i(TAG, "channels " + mGrayMat.channels());
		Log.i(TAG, "type " + mGrayMat.type());
		
		if (mNativeDetector != null) {
			mNativeDetector.detect(mGrayMat, faces);
		}
		
        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++) {
            Core.rectangle(mMat, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
        }
        
        for (int i = 0; i < facesArray.length; i++) {
            Core.rectangle(mGrayMat, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
        }
        
        saveImage(mMat, "picture2.jpg", true);
        saveImage(mGrayMat, "picture2gray.jpg", true);
        
        loadToImageView(mGrayMat);

        
        
        mGrayMat.release();
	}
	
	protected void loadToImageView(Mat m) {
		Bitmap imageToShow = Bitmap.createBitmap(m.cols(), m.rows(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(m, imageToShow);
		mImageView.setImageBitmap(imageToShow);
	}
	
	
    public void saveImage(Mat mat, final String name, boolean imageCaptured) {
    	File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    	File file = new File(path, name);
    	Boolean bool = null;
    	
    	String fileN = file.toString();
    	bool = Highgui.imwrite(fileN, mat);
    	
    	if (bool) {
//    		Toast.makeText(getApplicationContext(), "file " + name + " saved successfully", Toast.LENGTH_SHORT).show();
    		Log.d(TAG, "file " + name + " saved successfully");
    	} else {
    		Log.d(TAG, "file " + name + " did not save");
    	}
    	
//    	if (imageCaptured) {
//    	
//    		// intent to next activity where image with faces blurred
//	    	FdActivity.this.runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					Intent intent = new Intent(getApplicationContext(), DisplayImageActivity.class);
//					intent.putExtra("PICTURE_NAME", name);
//					startActivity(intent);
//				}
//			});
//    	}
    }
	@Override
	protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mOpenCvCallback);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mMat != null) mMat.release();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.process_image, menu);
		return true;
	}

}