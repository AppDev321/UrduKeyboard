package com.mobiletin.inputmethod;

import android.app.Application;
import android.content.Context;

/**
 * Created by Administrator on 9/7/2016.
 */
public class MySuperAppApplication extends Application {
    private static Application instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }
}
