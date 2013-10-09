package com.parse.starter;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class ParseApplication extends Application {

	

	
	@Override
	public void onCreate() {
		super.onCreate();
		

		// initialize Parse
		Parse.initialize(this, "xxxxxx", "yyyyyy"); 
		// initialize Facebook app with Parse
		ParseFacebookUtils.initialize("201365553318811");
		
		
		ParseUser.enableAutomaticUser();
		ParseACL defaultACL = new ParseACL();
		// Optionally enable public read access by default.
		// defaultACL.setPublicReadAccess(true);
		ParseACL.setDefaultACL(defaultACL, true);
		
		
		
	}
	


}
