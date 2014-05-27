 package org.opencv.samples.facedetect;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.core.Rect;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class Utility {
	
	private static final String TAG = "PhotoPrivacy::Utility";
	// app's back-end link
	protected static Location location = null;
	private static final String URL_HOST = "http://rocky-citadel-2836.herokuapp.com/";
	private static final String URL_NOTIFY = "http://rocky-citadel-2836.herokuapp.com/user/";
	private static final String URL_GET_CLOSEST_USERS = "http://rocky-citadel-2836.herokuapp.com/user/";
	private static String uniqueUserId;
	private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
	
	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";
	
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	
	private static Rect[] facesArray;
	
	public static Rect[] getFacesArray() {
		return facesArray;
	}

	public static void setFacesArray(Rect[] facesArr) {
		facesArray = facesArr;
	}

	public static String getHostUrl() {
		return URL_HOST;
	}
	
	public static String getUserId() {
		if (uniqueUserId == null)
			Log.e(TAG, "requested uniqueUserId not instantiated");
		return uniqueUserId;
	}
	
	public static String getUrlNotify() {
		return URL_NOTIFY;
	}
	
	public static String getUrlClosestUsers() {
		return URL_GET_CLOSEST_USERS;
	}
	
	public synchronized static void setUniqueUserId (Context context) {
		// if uId is null then load from shared preferences
		if (uniqueUserId == null) {
			SharedPreferences sharedPrefs = context.getSharedPreferences(PREF_UNIQUE_ID, Context.MODE_PRIVATE);
			uniqueUserId = sharedPrefs.getString(PREF_UNIQUE_ID, null);
			
			// if upon loading from shared preferences, uId is still null it implies this is first time app is opened
			// assign new id to the device for current and future sessions
			if (uniqueUserId == null) {
				uniqueUserId = UUID.randomUUID().toString();
				Editor editor = sharedPrefs.edit();
				editor.putString(PREF_UNIQUE_ID, uniqueUserId);
				editor.commit();
			}
		}
		
		Log.i(TAG, uniqueUserId);
	}
	
	// check if network is accessible and settings are properly configured on device
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected())
			return true;
		return false;
	}
	
	public static String createImageFileName() {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_" + JPEG_FILE_SUFFIX;
		return imageFileName;
	}
	

	public static JSONArray convertStringToJSON(String input) {
		/*
		 * 
		 */
		JSONArray tempJsonArray = null;
		try {
			tempJsonArray = new JSONArray(input);
			for (int i = 0; i < tempJsonArray.length(); i++) {
				JSONObject jsonObject = tempJsonArray.getJSONObject(i);
				Log.d(Integer.toString(i), jsonObject.toString());

			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return tempJsonArray;
	}
	
	public static String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
	

	/** Create a file Uri for saving an image or video */
	public static Uri getOutputMediaFileUri(int type){
	      return Uri.fromFile(getOutputMediaFile(type));
	}
	
	/** Create a File for saving an image or video */
	public static File getOutputMediaFile(int type){
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
