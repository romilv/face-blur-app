package org.opencv.samples.facedetect;

import java.io.File;

import org.opencv.samples.facedetect.R;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.Toast;

public class DisplayImageActivity extends Activity {
	
	private ImageView mImageView;
	private String mName;
	private File mFile;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Bundle extras = getIntent().getExtras();
		if (extras != null) 
			mName = extras.getString("PICTURE_NAME");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_image);
		
		mImageView = (ImageView) findViewById(R.id.id_captured_img);

    	File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    	if (mName != null)
    		mFile = new File(path, mName);
    	
    	if (mFile.exists()) {
    		Bitmap mBitmap = BitmapFactory.decodeFile(mFile.getAbsolutePath());
    		mImageView.setImageBitmap(mBitmap);
    	}
    	else {
    		Toast.makeText(getApplicationContext(), "No image found", Toast.LENGTH_SHORT).show();
    	}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_image, menu);
		return true;
	}

}
