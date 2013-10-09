
package com.parse.starter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class ScoreActivity extends Activity {
	private Location theLocation;
	TextView questionText;
	ParseObject question;
	int correctAnswer;
	Button buttonA;
	Button buttonB;
	Button buttonC;
	int points;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle b = getIntent().getExtras();
		String scanValue = b.getString("key");
		setContentView(R.layout.score);
		
		View title = getWindow().findViewById(android.R.id.title);
		View titleBar = (View) title.getParent();
		titleBar.setBackgroundColor(Color.BLACK);

		questionText = (TextView) findViewById(R.id.questionText);

		buttonA = (Button) findViewById(R.id.buttonA);
		buttonB = (Button) findViewById(R.id.buttonB);
		buttonC = (Button) findViewById(R.id.buttonC);
		buttonA.setVisibility(View.INVISIBLE);
		buttonB.setVisibility(View.INVISIBLE);
		buttonC.setVisibility(View.INVISIBLE);

		setButtonListeners();

		getLocation(); 
		
		compareLocationToToilet(scanValue);

	}
	
	private void getLocation() {
		// Acquire a reference to the system Location Manager
				LocationManager locationManager = (LocationManager) this
						.getSystemService(Context.LOCATION_SERVICE);

				// Define a listener that responds to location updates
				LocationListener locationListener = new LocationListener() {
					public void onLocationChanged(Location location) {
						// Called when a new location is found by the network location
						// provider.
						theLocation = location;
					}

					public void onStatusChanged(String provider, int status,Bundle extras) {
					}

					public void onProviderEnabled(String provider) {
					}

					public void onProviderDisabled(String provider) {
					}
				};

				// Register the listener with the Location Manager to receive location
				// updates
				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
						0, locationListener);
		
	}
	
	private void compareLocationToToilet(String _scanValue) {
		
		ParseQuery query = new ParseQuery("Toilets");
		query.whereEqualTo("toiletID", _scanValue);
		query.findInBackground(new FindCallback() {
			public void done(List<ParseObject> toiletList, ParseException e) {
				if (e == null) {
					if (toiletList.size() < 1) {
						Log.d("score", "ERROR - no toilet: " + toiletList.size());
						Toast.makeText(getApplicationContext(), "Denne QR kode er ikke registreret i vores system.", Toast.LENGTH_LONG).show();
						finish();

					} else {
						//getting the location from the smart phone sensors.
						double latitude = theLocation.getLatitude();
						double longitude = theLocation.getLongitude();
						Log.d("parse", "Location,  Lat: " + latitude + " Lon: "+ longitude);

						ParseGeoPoint currentGeoPoint = new ParseGeoPoint(latitude, longitude);
						ParseGeoPoint toiletGeopoint = toiletList.get(0).getParseGeoPoint("geoPoint");

						double distToToilet = currentGeoPoint
								.distanceInKilometersTo(toiletGeopoint);
						//checking if the distance is small enough.
						if (distToToilet < 3) {
							//saving a log to the Parse Database for the current user
							ParseUser curUser = ParseUser.getCurrentUser();
							
							List<String> log = (List<String>) curUser.get("log");
							Log.d("parse", "Retrieved log 2: " + log );
							//String currentDateTimeString = DateFormat.getDateInstance().format(new Date());
							String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

							Log.d("parse", "THE date: " + currentDateTimeString );
							log.add(currentDateTimeString);
							curUser.put("log", (Object)log);
							curUser.saveEventually();
							
							getQuestions();
						} else {
							Toast.makeText(getApplicationContext(), "Du registreres ikke til at være ved pissoiret.", Toast.LENGTH_LONG).show();
							checkEnableGPS();
							finish();
						}
						
						
					}
					Log.d("score", "Retrieved " + toiletList.size() + " scores");
				} else {
					Log.d("score", "Error: " + e.getMessage());
				}
			}
		});
		
	}

	private void setButtonListeners() {
		buttonA.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (correctAnswer == 0) {
					userAnsweredCorrectly();
				} else {
					userAnsweredWrongly();
				}
				Log.d("parse", "Button A");
			}
		});

		buttonB.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (correctAnswer == 1) {
					userAnsweredCorrectly();
				} else {
					userAnsweredWrongly();
				}
				Log.d("parse", "Button B");
			}
		});

		buttonC.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (correctAnswer == 2) {
					userAnsweredCorrectly();
				} else {
					userAnsweredWrongly();
				}
				Log.d("parse", "Button C");
			}
		});
	}

	@Override
	public void onBackPressed() {

	}

	private void startResultView() {
		Intent resultIntent = new Intent();
		resultIntent.setClass(this,ResultActivity.class);
		Bundle b = new Bundle();
		b.putInt("pointsKey", points); 
		resultIntent.putExtras(b); 
		startActivity(resultIntent);
		
	}
	protected void userAnsweredWrongly() {
		startResultView();
		question.increment("wrongs");
		question.saveEventually();
	}
	//calculation of the number of points.
	protected void userAnsweredCorrectly() {
		int totalAnswers = question.getInt("rights")
				+ question.getInt("wrongs");
		int wrongs = question.getInt("wrongs");
		 points = 50;
		if (totalAnswers > 0) {
			double wrongRatio = (double) wrongs / (double) totalAnswers;
			points = (int) (wrongRatio * 80 + 10);
		}
		question.increment("rights");
		question.saveEventually();

		incrementScoreWithPoints(points);
		
		startResultView();

	}
	//save assigned points to database
	private void incrementScoreWithPoints(int _points) {
		ParseUser curUser = ParseUser.getCurrentUser();
		if (curUser.get("score") == null) {
			curUser.put("score", _points);
		} else {
			curUser.increment("score", _points);
		}

		curUser.saveEventually();

	}
	//method to get a random question.
	protected void getQuestions() {

		final ParseQuery query = new ParseQuery("Questions");
		query.countInBackground(new CountCallback() {
			public void done(int count, ParseException e) {
				if (e == null) {
					int rand = new Random().nextInt(count);
					query.setSkip(rand);
					query.setLimit(1);
					query.findInBackground(new FindCallback() {
						public void done(List<ParseObject> results,
								ParseException e) {
							if (e == null) {
								question = results.get(0);
								questionText.setText(question.getString("text"));

								List<String> options = new ArrayList<String>();
								options.add(question.getString("option1"));
								options.add(question.getString("option2"));
								options.add(question.getString("option3"));
								
								//Shuffling which button the correct answer is assigned to.
								Collections.shuffle(options);

								correctAnswer = options.indexOf(question
										.getString("option1"));
								Log.d("parse", "correctAnswer: "+ correctAnswer);
								buttonA.setText((CharSequence) options.get(0));
								buttonB.setText((CharSequence) options.get(1));
								buttonC.setText((CharSequence) options.get(2));
								buttonA.setVisibility(View.VISIBLE);
								buttonB.setVisibility(View.VISIBLE);
								buttonC.setVisibility(View.VISIBLE);

							} else {
								
							}
						}
					});
				} else {
					// The request failed
				}
			}
		});

	}
	//method to make user enable GPS in lack of location precision
	private void checkEnableGPS(){
		String provider = Settings.Secure.getString(getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if(provider.equals("") || provider.equals("network")){

			AlertDialog.Builder builder = new AlertDialog.Builder(this);  
			builder.setMessage("Du registreres ikke til at være ved pissoiret.\nAlle lokations sensorer er ikke aktiverede.\nForbedr nøjagtigheden og brugeroplevelsen ved at aktivere trådløse netwærk og GPS.")  
			.setCancelable(false)  
			.setPositiveButton("Ja", new DialogInterface.OnClickListener() {  
				public void onClick(DialogInterface dialog, int id) {  
					Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);  
					startActivityForResult(intent, 1);  
				}  
			})  
			.setNegativeButton("Nej", new DialogInterface.OnClickListener() {  
				public void onClick(DialogInterface dialog, int id) {  
					 
				}  
			}).show();  

		}
	}

}
