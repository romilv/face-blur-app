package org.opencv.samples.facedetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

public class FdActivity extends Activity implements CvCameraViewListener2, SensorEventListener {
	
	private static int counter = 0;

    private static final String TAG = "PhotoPrivacy::FDActivity";
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
    
    private Rect[] facesArray;

    private CameraBridgeViewBase   mOpenCvCameraView;
    
    private boolean mFlagTakePicture;
    private LinearLayout mLinearLayout;
    
    // sensor operations to obtain direction phone pointing in when image is taken
    private OrientationSensor mOrientationSensor;
    private SensorManager mSensorManager;
    
	private int[] mAzimuthHistoryByCount;
	private boolean mHistoryOverflow;
	private int mCurrentCounter;	
	private int mSensorCallCount;
	private Float mAzimuth;
	private String mCompassDirection;

	private final float RADIANS_TO_DEGREE = 57.2957795f;
	private final int NW = 1, NE = 2, SW = 3, SE = 4;
	private final int AZIMUTH_HISTORY_LENGTH = 60;
	private final int HISTORY_TO_CONSIDER = 40;

	// BaseLoader from OpenCV
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

        // openCV
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        
        // sensor
        mSensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
        mOrientationSensor = new OrientationSensor(mSensorManager, FdActivity.this);
        
		mAzimuthHistoryByCount = new int[AZIMUTH_HISTORY_LENGTH];
		mHistoryOverflow = false;
		mCurrentCounter = 0;
		mSensorCallCount = 0;
        
        // taking picture
        mFlagTakePicture = false;
        mLinearLayout = (LinearLayout) findViewById(R.id.fd_activity_lin_layout);
        mLinearLayout.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// if user touches screen, set flag to indicate that current frame should be saved as image
				if (event.getAction() == MotionEvent.ACTION_UP) {
					counter += 1;
					mFlagTakePicture = true;
				}
				return true;
			}
		});
                
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
        
        // load sensor to get orientation of phone when picture is taken
		int count = mOrientationSensor.Register(FdActivity.this, SensorManager.SENSOR_DELAY_UI);
		if (count != 2) {
			Toast.makeText(getApplicationContext(), "Sensors required not available on this device", Toast.LENGTH_SHORT).show();
			mOrientationSensor.Unregister();
			mOrientationSensor = null;
		}
		
        // indicate to user how to capture image
        Toast.makeText(getApplicationContext(), "TOUCH SCREEN TO TAKE A PICTURE", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        mOrientationSensor.Unregister();
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

        facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++) {
            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
        }
        
        Mat face;
        String faceName;
        
        // if take a picture flag is set, then save image
        if(mFlagTakePicture) {
        	mFlagTakePicture = false;
        	
        	if (mOrientationSensor != null)
        		computePhoneDirection();
        	
        	String s;
        	StringBuilder sb = new StringBuilder();
        	
			for(int i = 0; i < facesArray.length; i++) {
        		sb.append(i + " " + facesArray[i].x + ", " + facesArray[i].y + ", tl " + facesArray[i].tl() + " ");
			}
			s = sb.toString();
			
        	FdActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(), "Image captured", Toast.LENGTH_SHORT).show();
		        	Toast.makeText(getApplicationContext(), mCompassDirection, Toast.LENGTH_LONG).show();
				}
			});
        	
        	// to blur faces
        	for (int i = 0; i < facesArray.length; i++) {
        		faceName = "Face" + i + ".png";
        		face = mRgba.submat(facesArray[i]);
        		Imgproc.GaussianBlur(face, face, new Size(95, 95), 0);
        		saveImage(face, faceName, false);
        	}
        	
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
					intent.putExtra("PHONE_DIRECTION", mCompassDirection);
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

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	// on SensorChanged called every time onSensorChange called in OrientationSensor.java
	@Override
	public void onSensorChanged(SensorEvent event) {
		mSensorCallCount++;
		
		// if AZIMUTH_HISTORY_LENGTH of data points have been added
		if (mSensorCallCount == AZIMUTH_HISTORY_LENGTH) {
			mHistoryOverflow = true;
		}
				
		mAzimuth = mOrientationSensor.m_azimuth_radians * RADIANS_TO_DEGREE;		
		mCurrentCounter = mSensorCallCount % AZIMUTH_HISTORY_LENGTH;
		
		// map value of azimuth to compass direction
		if (mAzimuth < 0 && mAzimuth > - 90)
			mAzimuthHistoryByCount[mCurrentCounter] = NW;
		else if (mAzimuth > 0 && mAzimuth < 90)
			mAzimuthHistoryByCount[mCurrentCounter] = NE;
		else if (mAzimuth > 90 && mAzimuth < 180)
			mAzimuthHistoryByCount[mCurrentCounter] = SE;
		else
			mAzimuthHistoryByCount[mCurrentCounter] = SW;
		
	}

	// when request to take a picture is sent, we go through recent history of azimuth directions
	// and obtain its average to reliably determine direction phone was facing in when picture was taken
	protected void computePhoneDirection() {
		
		if (mOrientationSensor.m_OrientationOK) {
			mAzimuth = mOrientationSensor.m_azimuth_radians;
			
			Map<Integer, Integer> directionMap = new HashMap<Integer, Integer>();
			directionMap.put(NW, 0);
			directionMap.put(NE, 0);
			directionMap.put(SW, 0);
			directionMap.put(SE, 0);

			int key;
			// if AzimuthHistory has less than HISTORY_TO_CONSIDER elements to consider
			if (!mHistoryOverflow) {
				for (int i = 0; i < mCurrentCounter; i++) {
					key = mAzimuthHistoryByCount[i];
					directionMap.put(key, directionMap.get(key)+1);
				}				
			}
			
			else {
				
				// if counter is less than HISTORY_TO_CONSIDER, then get count from 0 to counter and then
				// from AZIMUTH_HISTORY_LENGTH minus difference left to end of AZIMUTH_HISTORY_LENGTH array
				if (HISTORY_TO_CONSIDER > mCurrentCounter) {
					
					int difference = HISTORY_TO_CONSIDER - mCurrentCounter;
					
					for (int i = 0; i < mCurrentCounter; i++) {
						key = mAzimuthHistoryByCount[i];
						directionMap.put(key, directionMap.get(key)+1);
					}
					
					for (int i = AZIMUTH_HISTORY_LENGTH - difference; i < AZIMUTH_HISTORY_LENGTH; i++) {
						key = mAzimuthHistoryByCount[i];
						directionMap.put(key, directionMap.get(key)+1);
					}					
				} // end if
				
				// else if counter is greater than or equal to HISTORY_TO_CONSIDER, simply count from 
				// 0 to HISTORY_TO_CONSIDER
				else if (HISTORY_TO_CONSIDER <= mCurrentCounter) {
					for (int i = 0; i < HISTORY_TO_CONSIDER; i++) {
						key = mAzimuthHistoryByCount[i];
						directionMap.put(key, directionMap.get(key)+1);
					}					
				}
			}
			
			int countNW, countNE, countSW, countSE;
			countNW = directionMap.get(NW);
			countNE = directionMap.get(NE);
			countSE = directionMap.get(SE);
			countSW = directionMap.get(SW);
			
			// find direction with highest count
			int direction = countNW > countNE ? NW : NE;
			int directionCount = countNW > countNE ? countNW : countNE;
			direction = countSW > directionCount ? SW : direction;
			directionCount = countSW > directionCount ? countSW : directionCount;
			direction = countSE > directionCount ? SE : direction;
			directionCount = countSE > directionCount ? countSE : directionCount; 
			
			// update mCompassDirection
			if (direction == NW) mCompassDirection = "North-West";
			else if (direction == NE) mCompassDirection = "North-East";							
			else if (direction == SE) mCompassDirection = "South-East";
			else if (direction == SW) mCompassDirection = "South-West";
			
			directionMap = null;

		}		
		else
			mCompassDirection = null;
	}
}
