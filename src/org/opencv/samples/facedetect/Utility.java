 package org.opencv.samples.facedetect;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
}
