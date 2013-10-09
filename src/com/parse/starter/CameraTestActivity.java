/*
 * Basic no frills app which integrates the ZBar barcode scanner with
 * the camera.
 * 
 * 
 * Based on code from:
 * https://code.google.com/p/anvm/source/browse/trunk/ANVM_MobileApp/src/anvm/gui/CameraTestActivity.java?r=4 
 * Created by lisah0 on 2012-02-24
 */
package com.parse.starter;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CameraTestActivity extends Activity
{
    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;

    TextView scanText;

    ImageScanner scanner;

    private boolean previewing = true;

    static {
        System.loadLibrary("iconv");
    } 

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scanner);
        
		View title = getWindow().findViewById(android.R.id.title);
		View titleBar = (View) title.getParent();
		titleBar.setBackgroundColor(Color.BLACK);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        autoFocusHandler = new Handler();
        mCamera = getCameraInstance();
        
        
        /* Instance barcode scanner */
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);

        mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
        FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
        preview.addView(mPreview);

        scanText = (TextView)findViewById(R.id.scanText);
        
        final Button scoreButton = (Button) findViewById(R.id.scoreButton);
		scoreButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startResultView();
			}
		});

    }

    public void onPause() {
    	Log.d("parse", "onPause");
        super.onPause();
        releaseCamera();
    }
    public void onResume() {
    	Log.d("parse", "onResume");
        super.onResume();
        
        Log.d("parse", "mCamera 1: " + mCamera);	
        if(mCamera == null){
        	mCamera = getCameraInstance();
        	mCamera.setPreviewCallback(previewCb);
        	Log.d("parse", "mCamera 2: " + mCamera);
        	
        	 mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
             FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
             preview.addView(mPreview);
        }
        
        mCamera.startPreview();
        previewing = true;
        mCamera.autoFocus(autoFocusCB);
        
    }
    public void onRestart() {
    	Log.d("parse", "onRestart");
        super.onRestart();
        onCreate(null);
    }
    //disables the back button
	@Override
	public void onBackPressed() {

	}
    

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e){
        }
        return c;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            previewing = false;
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private Runnable doAutoFocus = new Runnable() {
            public void run() {
                if (previewing)
                    mCamera.autoFocus(autoFocusCB);
            }
        };

    PreviewCallback previewCb = new PreviewCallback() {
            public void onPreviewFrame(byte[] data, Camera camera) {
                Camera.Parameters parameters = camera.getParameters();
                Size size = parameters.getPreviewSize();

                Image barcode = new Image(size.width, size.height, "Y800");
                barcode.setData(data);

                int result = scanner.scanImage(barcode);
                
                if (result != 0) {
                    previewing = false;
                    mCamera.setPreviewCallback(null);
                    mCamera.stopPreview();
                    
                    SymbolSet syms = scanner.getResults();
                    for (Symbol sym : syms) {
                        scanText.setText("barcode result " + sym.getData());
                        
                        startScoreView(sym.getData());
                    }
                }
            }
        };

    // Mimic continuous auto-focusing
    AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
            public void onAutoFocus(boolean success, Camera camera) {
                autoFocusHandler.postDelayed(doAutoFocus, 1000);
            }
        };
        //starts the ScoreActivity with the string resulting from the scan.
    	private void startScoreView(String scanString) {

    		Intent scoreIntent = new Intent();
    		scoreIntent.setClass(this,ScoreActivity.class);
    		Bundle b = new Bundle();
    		b.putString("key", scanString); //The scanned string
    		scoreIntent.putExtras(b); //assigned to the intent.
    		startActivity(scoreIntent);

    	}
    	private void startResultView() {
    		Intent resultIntent = new Intent();
    		resultIntent.setClass(this,ResultActivity.class);
    		Bundle b = new Bundle();
    		b.putInt("pointsKey", -1); 
    		resultIntent.putExtras(b); 
    		startActivity(resultIntent);
    	}
}
