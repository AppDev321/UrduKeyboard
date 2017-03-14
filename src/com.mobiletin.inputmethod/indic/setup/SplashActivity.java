package com.mobiletin.inputmethod.indic.setup;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.mobiletin.inputmethod.Ads.AnalyticSingaltonClass;
import com.mobiletin.inputmethod.Ads.PreLoadIntersitial;
import com.mobiletin.inputmethod.MySuperAppApplication;
import com.mobiletin.inputmethod.indic.R;

/**
 * Created by Administrator on 10/31/2016.
 */

public class SplashActivity extends Activity{
    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 2500;

    Handler mHandler;
    Runnable mRunnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            FacebookSdk.sdkInitialize(getApplicationContext());
        }

        setContentView(R.layout.activuty_splash);
        MySuperAppApplication.preLoadIntersitial = new PreLoadIntersitial(this);
        MySuperAppApplication.mAnalyticSingaltonClass = AnalyticSingaltonClass.getInstance(this);

        mHandler= new Handler();
       /* mHandler.postDelayed(new Runnable(){
            @Override
            public void run() {
                *//* Create an Intent that will start the Menu-Activity. *//*
                Intent mainIntent = new Intent(SplashActivity.this,SetupWizardActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);

*/
        mRunnable = new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Intent mainIntent = new Intent(SplashActivity.this,SetupWizardActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        };

        mHandler.postDelayed(mRunnable, SPLASH_DISPLAY_LENGTH);

    }
    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            // Logs 'install' and 'app activate' App Events.
            AppEventsLogger.activateApp(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            // Logs 'app deactivate' App Event.
            AppEventsLogger.deactivateApp(this);
        }

        if(mHandler !=null)
        {
            mHandler.removeCallbacks(mRunnable);
            mHandler=null;
        }

        finish();
    }
}
