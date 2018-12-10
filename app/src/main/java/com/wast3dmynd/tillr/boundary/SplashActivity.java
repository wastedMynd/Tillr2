package com.wast3dmynd.tillr.boundary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.wang.avi.AVLoadingIndicatorView;
import com.wast3dmynd.tillr.R;


/**
 * Created by sizwe on 2016/11/26 @ 7:28 AM.
 */
public class SplashActivity extends AppCompatActivity {

    private static final String KEY_SPLASH_DISPLAYED = "splash_displayed";
    private static final String KEY_SPLASH_FIRST_LOAD = "splash_first_time";
    private static final int SPLASH_TIMEOUT_SHORT = 1000*2;
    private static final int SPLASH_TIMEOUT_LONG = 1000*5;

   //region layout views
    private AVLoadingIndicatorView loader;
   //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //associate this activity with it layout (SplashActivity to activity_splash layout)
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initViews();
        redirectToMainActivity(savedInstanceState);
    }

    //region functions

    //initialize layout views
    private void initViews() {
        //get the element(s) within the activity_splash layout
        loader = findViewById(R.id.content_loader_progress);
    }

    /**
     * this method check whether the app is loaded for the first time
     **/
    private void redirectToMainActivity(final Bundle savedInstanceState) {
        if (savedInstanceState != null)//check for splash screen reentry
            closeSplashActivity(savedInstanceState);
        else {
            if (isSplashScreenFirstDisplay()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadMainActivity();
                        closeSplashActivity(savedInstanceState);//if the SplashActivity instance was previously displayed ,close it.
                    }
                }, SPLASH_TIMEOUT_LONG);
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadMainActivity();
                        closeSplashActivity(savedInstanceState);//if the SplashActivity instance was previously displayed ,close it.
                    }
                }, SPLASH_TIMEOUT_SHORT);
            }
            //log that splash was displayed
            logSplash();
        }
    }

    //goto MainActivity
    private void loadMainActivity() {
        //region change activity
        loader.smoothToHide();
        Intent launchMainActivity;
        launchMainActivity = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(launchMainActivity);
        finish();
        //endregion
    }

    private boolean isSplashScreenFirstDisplay() {
        //is  the splash screen displaying for the first time
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        return pref.getBoolean(KEY_SPLASH_FIRST_LOAD, true);
    }

    //apply decor to layout views


    //log splash screen attributes
    private void logSplash() {
        Bundle savedInstanceState;
        savedInstanceState = new Bundle();
        savedInstanceState.putBoolean(KEY_SPLASH_DISPLAYED, true);
        onSaveInstanceState(savedInstanceState);
    }

    //if this instance reloads, close it!
    private void closeSplashActivity(Bundle savedInstanceState) {
        if(savedInstanceState==null)return;
        boolean splashDisplayed;
        splashDisplayed = savedInstanceState.getBoolean(KEY_SPLASH_DISPLAYED, false);
        if (splashDisplayed) finish();
    }

    //endregion
}
