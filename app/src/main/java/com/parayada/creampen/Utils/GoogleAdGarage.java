package com.parayada.creampen.Utils;

import android.content.Context;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;

import java.util.Arrays;

public class GoogleAdGarage {
    public static void loadBannerFromXml(Context context,AdView adView){
        MobileAds.initialize(context, initializationStatus -> { });

        // Add test device
        RequestConfiguration configuration = new RequestConfiguration.Builder()
                .setTestDeviceIds(Arrays.asList("1760BFA2815986D5277010512BD7F196"))
                .build();
        MobileAds.setRequestConfiguration(configuration);

        adView.loadAd(new AdRequest.Builder().build());
    }
}
