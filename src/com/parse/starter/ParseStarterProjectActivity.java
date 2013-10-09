package com.parse.starter;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;



public class ParseStarterProjectActivity extends Activity {
	/** Called when the activity is first created. */

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		
		View title = getWindow().findViewById(android.R.id.title);
		View titleBar = (View) title.getParent();
		titleBar.setBackgroundColor(Color.BLACK);
		
		//Check the phone has a camera
		PackageManager pm = this.getPackageManager();

		if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

		}
		else {
			Toast.makeText(getApplicationContext(), "Denne applikation kræver kamera for at kunne scanne QR koder.", Toast.LENGTH_LONG).show();
		}

		final TextView loadingText = (TextView) findViewById(R.id.loadingText);


		final Button scanButton = (Button) findViewById(R.id.scanButton);
		scanButton.setVisibility(View.INVISIBLE);
		scanButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startScanner();
			}
		});
		
		final Button scoreButton = (Button) findViewById(R.id.scoreButton);
		scoreButton.setVisibility(View.INVISIBLE);
		scoreButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startResultView();
			}
		});
		
		final ImageButton button = (ImageButton) findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				button.setVisibility(View.INVISIBLE);
				loadingText.setText("Logger ind...");
				loadingText.setVisibility(View.VISIBLE);
				
				// Creating Facebook Login dialog
				ParseFacebookUtils.logIn(null, ParseStarterProjectActivity.this, 
						new LogInCallback() {
					
					
							@Override
							public void done(ParseUser user, ParseException err) {
								if (user == null) {
									Log.d("parse", err.getMessage());
									Log.d("parse",
											"Uh oh. The user cancelled the Facebook login.");
									Toast.makeText(getApplicationContext(), " Forbindelse til serveren mislykkedes, prøv venligst igen.", Toast.LENGTH_SHORT);
									onCreate(null);
									
									
									
								} else if (user.isNew()) {

									setValuesUser(user, true);
									loadingText.setText("Velkommen til RoskildePis \nFind et toilet med en QR kode, scan, svar på spørgsmålet og konkurer mod dine venner.");
									loadingText.setVisibility(View.VISIBLE);
									scanButton.setVisibility(View.VISIBLE);
									scoreButton.setVisibility(View.VISIBLE);
									
									Log.d("MyApp",
											"User signed up and logged in through Facebook!");
								} else {
									Log.d("MyApp",
											"User logged in through Facebook!");
									setValuesUser(user, false);
									loadingText.setText("Velkommen til RoskildePis \nFind et toilet med en QR kode, scan, svar på spørgsmålet og konkurrer mod dine venner.");
									loadingText.setVisibility(View.VISIBLE);
									scanButton.setVisibility(View.VISIBLE);
									scoreButton.setVisibility(View.VISIBLE);
									
								}
							}
						});
			}
			
		});
		
	}
	//starting new activity, pointskey ,-1 indicates no points obtained right now. 
	private void startResultView() {
		Intent resultIntent = new Intent();
		resultIntent.setClass(this,ResultActivity.class);
		Bundle b = new Bundle();
		b.putInt("pointsKey", -1); 
		resultIntent.putExtras(b); 
		startActivity(resultIntent);
	}
	
	private void startScanner() {
		Intent intent = new Intent();
		intent.setClass(this, CameraTestActivity.class);
		startActivity(intent);
	}
	// Saving the Facebook userId as the Parse userId and assign a new user the score 0. 
	private void setValuesUser(ParseUser user, boolean newUser) {
		Bundle args = new Bundle();
		args.putString("fields", "id");
		JSONObject result = null;
		try {
			result = new JSONObject(ParseFacebookUtils.getFacebook().request(
					"me", args));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		user.put("username", result.optString("id"));

		if (newUser == true) {
			user.put("score", 0);
		}

		user.saveEventually();
	} 
	
	
	@Override
	public void onBackPressed() {
		Toast.makeText(getApplicationContext(), "Brug \"home\" knappen hvis du vil forlade applikationen.", Toast.LENGTH_SHORT).show();

	}
}