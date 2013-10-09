package com.parse.starter;

import android.util.Log;

import com.parse.facebook.DialogError;
import com.parse.facebook.Facebook.DialogListener;
import com.parse.facebook.FacebookError;

public abstract class BaseDialogListener implements DialogListener {
    @Override
    public void onFacebookError(FacebookError e) {
        e.printStackTrace();
        Log.d("parse", "FB error");
    }
    @Override
    public void onError(DialogError e) {
        e.printStackTrace();
        Log.d("parse", "Dialog error");
    }
    @Override
    public void onCancel() {
    	Log.d("parse", "Cancel is pressed!");
    }
}

