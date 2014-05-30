package org.opencv.samples.facedetect;

import java.io.File;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.Toast;

public class DisplayImageActivity extends Activity {
	
	private ImageView mImageView;
	
	private final String TAG = "myopencv2"; 
	
	private String mName;
	
	private Rect[] mFacesArray;
	
	private File mFile;
	private File mEnvironmentPath;
	private File mTemplateFile;
			
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_image);

		
		loadImageFromPreviousActivity(savedInstanceState);
		
	} // end onCreate()
	
	public void loadImageFromPreviousActivity(Bundle savedInstanceState) {
		
//		Bundle extras = getIntent().getExtras();
//		if (extras != null) 
//			mName = extras.getString("PICTURE_NAME");
		
		mImageView = (ImageView) findViewById(R.id.id_captured_img);

		mEnvironmentPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		mName = "picture3.png";

    	if (mName != null)
    		mFile = new File(mEnvironmentPath, mName);
    	
    	if (mFile.exists()) {
    		Bitmap mBitmap = BitmapFactory.decodeFile(mFile.getAbsolutePath());
    		mImageView.setImageBitmap(mBitmap);
//    		Toast.makeText(getApplicationContext(), "Image Displayed", Toast.LENGTH_SHORT).show();
    		matchTemplateOnImage();
    	}
    	else {
    		Toast.makeText(getApplicationContext(), "No image found", Toast.LENGTH_SHORT).show();
    	}
	}
	
	public void matchTemplateOnImage() {
		
		Log.d(TAG, "running template matching");
		
		mTemplateFile = new File(mEnvironmentPath, "use_this_marker.png");
		if (!mTemplateFile.exists()) {
			Log.d(TAG, "couldnt load template file");
			return;
		}
		
		Mat image;
		Mat templateImage;
		
		image = Highgui.imread(mFile.toString());
		templateImage = Highgui.imread(mTemplateFile.toString());
		
		
		// / Create the result matrix
        int result_cols = image.cols() - templateImage.cols() + 1;
        int result_rows = image.rows() - templateImage.rows() + 1;
        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);
        
     // / Do the Matching and Normalize
        int matchMethod = Imgproc.TM_CCOEFF;
        Imgproc.matchTemplate(image, templateImage, result, matchMethod); // Imgproc.TM_CCOEFF is match_method
        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        
     // / Localizing the best match with minMaxLoc
        MinMaxLocResult mmr = Core.minMaxLoc(result);

        Point matchLoc;
        if (matchMethod == Imgproc.TM_SQDIFF || matchMethod == Imgproc.TM_SQDIFF_NORMED) {
            matchLoc = mmr.minLoc;
        } else {
            matchLoc = mmr.maxLoc;
        }
        
     // / Show me what you got
        Core.rectangle(image, matchLoc, new Point(matchLoc.x + templateImage.cols(),
                matchLoc.y + templateImage.rows()), new Scalar(0, 255, 0));
        
        Log.d(TAG, "image rows " + image.rows());
        Log.d(TAG, "image cols " + image.cols());
        
        Log.d(TAG, "match x plus cols " + Double.toString(matchLoc.x + templateImage.cols()));
        Log.d(TAG, "match x " + Double.toString(matchLoc.x));
        Log.d(TAG, "match cols " + Double.toString(templateImage.cols()));
        
        Log.d(TAG, "match y plus rows " + Double.toString(matchLoc.y + templateImage.rows()));
        Log.d(TAG, "match y " + Double.toString(matchLoc.y));
        Log.d(TAG, "match rows " + Double.toString(templateImage.rows()));
        
        double markerTopLeftRow = matchLoc.x;
        
        mFacesArray = Utility.getFacesArray();
        Log.d("facesarray", Integer.toString(mFacesArray.length));
        
        for (int i = 0; i < mFacesArray.length; i++) {
        	
        	boolean blurFlag = false;
        	
        	Log.d(TAG, "i " + i);
        	Log.d(TAG, "x " + mFacesArray[i].x);
        	Log.d(TAG, "y " + mFacesArray[i].y);
        	Log.d(TAG, "tl " + mFacesArray[i].tl());
        	Log.d(TAG, "br " + mFacesArray[i].br());
        	
        	// heuristic, if rows within 30 pixels of each other, blur out face
        	double faceTopLeftRow = mFacesArray[i].x;
        	 
        	if (faceTopLeftRow > markerTopLeftRow) {
        		if (faceTopLeftRow - markerTopLeftRow < 30)
        			blurFlag = true;
        	} else {
        		if (markerTopLeftRow - faceTopLeftRow < 30)
        			blurFlag = true;
        	}
        	
        	if (blurFlag) {
        		Mat face = image.submat(mFacesArray[i]);
        		Imgproc.GaussianBlur(face, face, new Size(95, 95), 0);
        		face.release();
        	}
        }
        // do not use this, makes image blue?
//        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2RGB);
    
        // Save the visualized detection.
        File outFile = new File(mEnvironmentPath, "matched_file.png");
        Highgui.imwrite(outFile.toString(), image);
        
		Bitmap mBitmap = BitmapFactory.decodeFile(outFile.getAbsolutePath());
		mImageView.setImageBitmap(mBitmap);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_image, menu);
		return true;
	}

}
