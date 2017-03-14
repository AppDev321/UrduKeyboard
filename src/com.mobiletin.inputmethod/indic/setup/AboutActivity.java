package com.mobiletin.inputmethod.indic.setup;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mobiletin.inputmethod.Ads.AdIntegration;
import com.mobiletin.inputmethod.MySuperAppApplication;
import com.mobiletin.inputmethod.indic.R;

/**
 * Created by Administrator on 10/7/2016.
 */

public class AboutActivity extends AdIntegration{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_about);
        ImageView back=(ImageView)findViewById(R.id.img_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        /*AnalyticSingaltonClass mAnalyticSingaltonClass = AnalyticSingaltonClass.getInstance(this);
        mAnalyticSingaltonClass.sendScreenAnalytics("About Index - Testing");*/

        super.showAdd(this,(LinearLayout)findViewById(R.id.adView),false);

        if(MySuperAppApplication.preLoadIntersitial.getAd().isLoaded())
        {
            MySuperAppApplication.preLoadIntersitial.getAd().show();
        }

    }
}
