package com.mobiletin.inputmethod;

import android.app.Application;
import android.content.Context;

import com.mobiletin.inputmethod.Ads.AnalyticSingaltonClass;
import com.mobiletin.inputmethod.Ads.PreLoadIntersitial;

/**
 * Created by Administrator on 9/7/2016.
 */
public class MySuperAppApplication extends Application {
    private static Application instance;
    public static PreLoadIntersitial preLoadIntersitial;
    public static AnalyticSingaltonClass mAnalyticSingaltonClass;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        MySuperAppApplication.preLoadIntersitial = new PreLoadIntersitial(instance);
        MySuperAppApplication.mAnalyticSingaltonClass = AnalyticSingaltonClass.getInstance(instance);

    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    public static boolean isProbablyArabic(String s) {
        for (int i = 0; i < s.length(); ) {
            int c = s.codePointAt(i);
            if (c >= 0x0600 && c <= 0x06E0)
                return true;
            i += Character.charCount(c);
        }
        return false;
    }
}
