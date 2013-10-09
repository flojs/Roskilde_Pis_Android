package com.parse.starter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseUser;
import com.parse.facebook.Facebook;

public class ResultActivity extends Activity{

	public static Facebook mFacebook;
	

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.result);
		
		mFacebook = new Facebook("201365553318811");
		
		View title = getWindow().findViewById(android.R.id.title);
		View titleBar = (View) title.getParent();
		titleBar.setBackgroundColor(Color.BLACK);
		
		Bundle b = getIntent().getExtras();
		int pointsFromQuestion = b.getInt("pointsKey");
		Log.d("parse","pointsFromQuestion: " + pointsFromQuestion);
		
		String svar; 
		if (pointsFromQuestion == -1) {
			svar = "";
			pointsFromQuestion = 0;
		}
		else if (pointsFromQuestion > 0) {
			svar = "Du svarede rigtigt!";
		} else {
			svar = "Du svarede forkert!";
		}
		ParseUser curUser = ParseUser.getCurrentUser();
		int curUserOnlineScore = (Integer) curUser.get("score") ;
		
		final int curUsercurScore = curUserOnlineScore + pointsFromQuestion;
		
		/*Log.d("score", "QuestionScore: " + pointsFromQuestion);
		Log.d("score", "Online Score: " + curUserOnlineScore);
		Log.d("score", "Score: " + curUsercurScore);*/
		TextView scoreText = (TextView) findViewById(R.id.pointsText);
		scoreText.setText("Du har " + curUsercurScore + " point.");
		TextView resultatText = (TextView) findViewById(R.id.resultatText);
		resultatText.setText( svar );
		Button buttonScan = (Button) findViewById(R.id.buttonScan);
		buttonScan.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startScanner();
			}
		});
		Button postFacebookButton = (Button) findViewById(R.id.postFacebook);
		postFacebookButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {

				Bundle parameters = new Bundle();
					//the parameters which add content to the facebook post.
			      parameters.putString("app_id", "201365553318811");
			      parameters.putString("picture", "http://roskildepis.appspot.com/roskildepis.png");
			      parameters.putString("link", "http://apps.facebook.com/roskildepis/");
			      parameters.putString("name", "Jeg har nu " + curUsercurScore + " point. Vær med til RoskildePis!");            
			      parameters.putString("caption", "Brug pissoir, lad folk sove i hegnet i fred.");   
			      parameters.putString("description", "Med RoskildePis formindsker du mængden af urinstøv samtidig med at du konkurrerer i viden om festivallen mod dine venner");

			    //invoking the facebook feed dialog.  
				mFacebook.dialog(ResultActivity.this, "feed" , parameters, new PostDialogListener());
				
			}
		});
	}
	
	private void startScanner() {
		Intent intent = new Intent();
		intent.setClass(this, CameraTestActivity.class);
		startActivity(intent);

	}
	
	
	@Override
	public void onBackPressed() {

	}
	
	public class PostDialogListener extends BaseDialogListener {
	    @Override
	    public void onComplete(Bundle values) {
	        final String postId = values.getString("post_id");
	        if (postId != null) {
	            Log.d("parse","Message posted on the wall.");
	            Toast.makeText(ResultActivity.this, "Beskeden er posted på facebook", Toast.LENGTH_SHORT);

	        } else {
	        	Log.d("parse","NO Message posted on the wall.");
	        }
	    }  
	    
	    
	}

}



