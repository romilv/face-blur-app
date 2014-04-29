package org.opencv.samples.facedetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.samples.facedetect.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

public class FdActivity extends Activity implements CvCameraViewListener2 {
	
	private static int counter = 0;

//    private static final String    TAG                 = "OCVSample::Activity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;

    private MenuItem               mItemFace50;
    private MenuItem               mItemFace40;
    private MenuItem               mItemFace30;
    private MenuItem               mItemFace20;
    private MenuItem               mItemType;

    private Mat                    mRgba;
    private Mat                    mGray;
    private File                   mCascadeFile;
    private CascadeClassifier      mJavaDetector;
    private DetectionBasedTracker  mNativeDetector;

    private int                    mDetectorType       = JAVA_DETECTOR;
    private String[]               mDetectorName;

    private float                  mRelativeFaceSize   = 0.35f;
    private int                    mAbsoluteFaceSize   = 0;

    private CameraBridgeViewBase   mOpenCvCameraView;
    
    private boolean mFlagTakePic;
//    private Button mButtonTakePic;
    private LinearLayout mLinearLayout;
    
    private static final String TAG = "PhotoPrivacy::FDActivity";

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
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

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "failed to load cascade", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public FdActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.face_detect_surface_view);

        mLinearLayout = (LinearLayout) findViewById(R.id.fd_activity_lin_layout);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        
        mFlagTakePic = false;
        
        mLinearLayout.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// if user touches screen, set flag to indicate that current frame should be saved as image
				if (event.getAction() == MotionEvent.ACTION_UP) {
					counter += 1;
					mFlagTakePic = true;
				}
				return true;
			}
		});
                
        // Design choice
        // Code in case button is used instead of simply touching the screen to capture image
        
//        mButtonTakePic = (Button) findViewById(R.id.btn_take_pic);
//        mButtonTakePic.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				mFlagTakePic = true;
//			}
//		});
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
        
        // indicate to user how to capture image
        Toast.makeText(getApplicationContext(), "TOUCH SCREEN TO TAKE A PICTURE", Toast.LENGTH_SHORT).show();
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        
        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        MatOfRect faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }
        else if (mDetectorType == NATIVE_DETECTOR) {
            if (mNativeDetector != null)
                mNativeDetector.detect(mGray, faces);
        }
        else {
            Log.e(TAG, "Detection method is not selected!");
        }

        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++) {
            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
        }
        
        Mat face;
        String faceName;
        
        // if take a picture flag is set, then save image
        if(mFlagTakePic) {
        	mFlagTakePic = false;
        	String s = "";
        	
			for(int i = 0; i < facesArray.length; i++) {
        		s = s + " " + i + " " + facesArray[i].x + ", " + facesArray[i].y + ", tl " + facesArray[i].tl();
			}
			
        	FdActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(), "Image captured", Toast.LENGTH_SHORT).show();
				}
			});
        	
        	// to blur faces
        	for (int i = 0; i < facesArray.length; i++) {
        		faceName = "Face" + i + ".png";
        		face = mRgba.submat(facesArray[i]);
        		Imgproc.GaussianBlur(face, face, new Size(95, 95), 0);
        		saveImage(face, faceName, false);
        	}
        	
//        	Mat mCloneRgba = mRgba.clone();  
//        	for (int i = 0; i < mCloneRgba.rows(); i ++) {
//        		for (int j = 0; j < mCloneRgba.cols(); j++) {
//        			double[] rgbArray = mCloneRgba.get(i, j);
//        			double[] grayArray = mGray.get(i, j);
////        			Log.i("rowcol", rgbArray.length + " " + grayArray.length);
////        			for (int k = 0; k < rgbArray.length; k++) {
////        				rgbArray[k] += grayArray[k];
////        			}
//        			mCloneRgba.put(i, j, rgbArray);
//        		}
//        	}
        	
//        	Mat mMat = new Mat();
//        	List<Mat> rgbMatList = new ArrayList<Mat>();
//        	Core.split(mCloneRgba, rgbMatList);
//        	rgbMatList.add(mGray);
//        	Core.merge(rgbMatList, mMat);
//        	Log.d("channels::mRgba", Integer.toString(mRgba.channels()));
//        	Log.d("channels::mGray", Integer.toString(mGray.channels()));
//        	Log.d("channels::mMat", Integer.toString(mMat.channels()));
        	
        	// only need to process if saving the image
        	processMat(mRgba);
    		saveImage(mRgba, "picture3.png", true);
        }

        return mRgba;        
    }
    
    protected void processMat(Mat mat) {
    	Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB);
    }
    
    // Mat to be saved
    // String name - name of image to be stored
    // boolean imageCaptured - true when full image is saved, false when we only try to save detected faces
    
    public void saveImage(Mat mat, final String name, boolean imageCaptured) {
    	File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    	File file = new File(path, name);
    	Boolean bool = null;
    	
    	String fileName = file.toString();
    	bool = Highgui.imwrite(fileName, mat);
    	
    	if (bool) {
    		Log.d(TAG, "file " + name + " saved successfully");
    	} else {
    		Log.d(TAG, "file " + name + " did not save");
    	}
    	
    	if (imageCaptured) {
    	
    		// intent to next activity where image with faces blurred
	    	FdActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Intent intent = new Intent(getApplicationContext(), DisplayImageActivity.class);
					intent.putExtra("PICTURE_NAME", name);
					startActivity(intent);
				}
			});
    	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemFace50 = menu.add("Face size 50%");
        mItemFace40 = menu.add("Face size 40%");
        mItemFace30 = menu.add("Face size 30%");
        mItemFace20 = menu.add("Face size 20%");
        mItemType   = menu.add(mDetectorName[mDetectorType]);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemFace50)
            setMinFaceSize(0.5f);
        else if (item == mItemFace40)
            setMinFaceSize(0.4f);
        else if (item == mItemFace30)
            setMinFaceSize(0.3f);
        else if (item == mItemFace20)
            setMinFaceSize(0.2f);
        else if (item == mItemType) {
            int tmpDetectorType = (mDetectorType + 1) % mDetectorName.length;
            item.setTitle(mDetectorName[tmpDetectorType]);
            setDetectorType(tmpDetectorType);
        }
        return true;
    }

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    private void setDetectorType(int type) {
        if (mDetectorType != type) {
            mDetectorType = type;

            if (type == NATIVE_DETECTOR) {
                Log.i(TAG, "Detection Based Tracker enabled");
                mNativeDetector.start();
            } else {
                Log.i(TAG, "Cascade detector enabled");
                mNativeDetector.stop();
            }
        }
    }
}
