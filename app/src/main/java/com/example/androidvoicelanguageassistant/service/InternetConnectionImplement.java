package com.example.androidvoicelanguageassistant.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import static com.example.androidvoicelanguageassistant.utils.QueryUtils.LOG_TAG;

public class InternetConnectionImplement implements InternetConnection {

    private Context context;

    public InternetConnectionImplement(Context context) {
        this.context=context;
    }

    @Override
    public boolean isConnected() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return false;
    }
}
