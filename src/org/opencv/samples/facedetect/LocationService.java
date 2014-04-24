package org.opencv.samples.facedetect;


import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

public class LocationService extends Service {
	
	private int counter = 0;
	private boolean mFirstTimeflag = true;
	
	// prevents further execution of updateWithNewLocation() upon request to stop service
	private boolean mRunService;
	
	private LocationManager mLocatiomManager;
	private LocationListener mLocationListener;
	private static Location location;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mRunService = true;
		mLocatiomManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		mLocationListener = new MyLocationList();
		location = mLocatiomManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		updateWithNewLocation(location);
		mLocatiomManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, mLocationListener);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	
	@Override
	public void onDestroy() {
		mRunService = false;
		mLocatiomManager = null;
		mLocationListener = null;
		location = null;
		super.onDestroy();
	}
	
	private void updateWithNewLocation(final Location location) {
		if (location == null)
			return;
		
		if (!mRunService)
			return;
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		String result = "";
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		String str = counter + " " + " lat " + latitude + " lon " + longitude;
		counter += 1;
		
		// when location is updated send the data
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
		
		if(isNetworkAvailable()) {

			result = postData();
			if (mFirstTimeflag) {
//				mFirstTimeflag = false;
				Toast.makeText(getApplicationContext(), "GPS SENDING SUCCESSFULLY TO SERVER", Toast.LENGTH_SHORT).show();
			}
		}
		else {
			Toast.makeText(getApplicationContext(), "KINDLY CONNECT TO INTERNET", Toast.LENGTH_SHORT).show();
			mRunService = false;
		}
	}
	
	// check if network is accessible and settings are properly configured on device
	private boolean isNetworkAvailable() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected())
			return true;
		return false;
	}
	
	public static String postData() {
		InputStream inputStream;
		String result = "";
		
		try {
			
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(Utility.getUrlNotify());
			String json;
			JSONObject jsonObject = new JSONObject();
	
			// build JSON object
			jsonObject.accumulate("user", Utility.getUserId());
			jsonObject.accumulate("lat", location.getLatitude());
			jsonObject.accumulate("lon", location.getLongitude());
	
			json = jsonObject.toString();
			StringEntity se = new StringEntity(json);
			httpPost.setEntity(se);
			
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");
			
			HttpResponse httpResponse = httpClient.execute(httpPost);
			// close the httpClient instance
			httpClient.getConnectionManager().shutdown();
			inputStream = httpResponse.getEntity().getContent();
			
			if (inputStream != null) 
				result = inputStream.toString();
			else
				result = "Error Posting Data";
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("LocationService", e.getLocalizedMessage());
		}
		
		if (result != null) {
			return result;
		}
		
		return "";
	}
	
	private class MyLocationList implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			updateWithNewLocation(location);
		}

		@Override
		public void onProviderDisabled(String provider) {
			Toast.makeText(getApplicationContext(), "GPS disabled", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onProviderEnabled(String provider) {
			Toast.makeText(getApplicationContext(), "GPS enabled", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

}
