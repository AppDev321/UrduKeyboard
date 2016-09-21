package com.mobiletin.inputmethod.Ads;/*
   Created by Noman and  Email = nomanarif.cdz@gmail.com on 7/21/2016.
*/

import android.content.Context;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.mobiletin.inputmethod.indic.R;


public class PreLoadIntersitial {
    // Static fields are shared between all instances.
    static InterstitialAd interstitialAd;
    static Context context;

    public PreLoadIntersitial(Context context) {
        this.context = context;
        createAd(context);
    }

    public void createAd(Context context) {
        // Create an ad.
        interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId(context.getResources().getString(R.string.intersitial_ad_unit_id));
       /* AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice(TEST_DEVICE_ID).build();*/
       final  AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        // Load the interstitial ad.
        interstitialAd.loadAd(adRequest);
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                interstitialAd.loadAd(adRequest);
                super.onAdClosed();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
            }
        });
    }

    public InterstitialAd getAd() {
        return interstitialAd;
    }


    //Call method for preload intersiition

   /* InterstitialAd ad = admanager.getAd();
    if (ad.isLoaded) {
        ad.show();
    }*/
}