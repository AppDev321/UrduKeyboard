package com.mobiletin.inputmethod.Ads;/*
   Created by Noman and  Email = nomanarif.cdz@gmail.com on 6/24/2016.
*/

import android.app.Activity;
import android.content.Context;

import android.view.View;
import android.widget.LinearLayout;


import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.mobiletin.inputmethod.MySuperAppApplication;
import com.mobiletin.inputmethod.indic.R;
import com.mobiletin.inputmethod.sqlite.DBDictionary;
import com.mobiletin.inputmethod.sqlite.DataBaseHelper;


public class AdIntegration extends Activity {
    AdView mAdView;
    LinearLayout adLayout;
    InterstitialAd mInterstitialAd;
    boolean isIntersitialShow = false;
    boolean isIntertialClosed = false;

    public AdIntegration() {
        super();
    }

    public void showAdd(Context context, LinearLayout adLayout, boolean isIntersitialShow) {

        this.adLayout = adLayout;
        this.isIntersitialShow = isIntersitialShow;
        mAdView = new AdView(context);
        mAdView.setAdSize(AdSize.SMART_BANNER);
        mAdView.setAdUnitId(context.getResources().getString(R.string.banner_ad_unit_id));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        this.adLayout.addView(mAdView, params);
        adsDisplay();

        //Integrate Database
        DataBaseHelper db = new DataBaseHelper(context);

    }

    public void adsDisplay() {
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);
        if (isIntersitialShow) {
            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(this.getResources().getString(R.string.intersitial_ad_unit_id));

            /*if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }*/
            mInterstitialAd.loadAd(adRequest);
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {

                    isIntertialClosed = true;
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
                    mInterstitialAd.show();
                    super.onAdLoaded();
                }
            });

        }
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                adLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdClosed() {
                adLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                adLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAdLeftApplication() {
                adLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAdOpened() {
                adLayout.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if (mAdView != null) {
            mAdView.resume();
        }
        super.onResume();
    }


    public InterstitialAd getAd() {
        return mInterstitialAd;
    }
}
