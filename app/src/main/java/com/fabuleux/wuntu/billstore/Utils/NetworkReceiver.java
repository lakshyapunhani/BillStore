package com.fabuleux.wuntu.billstore.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.fabuleux.wuntu.billstore.EventBus.InternetStatus;
import com.fabuleux.wuntu.billstore.Manager.SessionManager;
import com.fabuleux.wuntu.billstore.R;

import org.greenrobot.eventbus.EventBus;


public class NetworkReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager conn = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();
        SessionManager sessionManager = new SessionManager(context);

        // Checks the user prefs and the network connection. Based on the result, decides whether
        // to refresh the display or keep the current display.
        // If the userpref is Wi-Fi only, checks to see if the device has a Wi-Fi connection.
        if (/*WIFI.equals(sPref) &&*/ networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            // If device has its Wi-Fi connection, sets refreshDisplay
            // to true. This causes the display to be refreshed when the user
            // returns to the app.
//            refreshDisplay = true;
//            Toast.makeText(context, R.string.wifi_connected, Toast.LENGTH_SHORT).show();
//            sessionManager.setInternetAvailable(true);

//                i = new Intent("DOWNLOADING_SAVED_ROUTES");
                InternetStatus internetStatus=new InternetStatus(true,context.getString(R.string.info_internet_available));
                EventBus.getDefault().post(internetStatus);

            // If the setting is ANY network and there is a network connection
            // (which by process of elimination would be mobile), sets refreshDisplay to true.
        } else if (/*ANY.equals(sPref) &&*/ networkInfo != null) {
//            refreshDisplay = true;

            // Otherwise, the app can't download content--either because there is no network
            // connection (mobile or Wi-Fi), or because the pref setting is WIFI, and there
            // is no Wi-Fi connection.
            // Sets refreshDisplay to false.
//            sessionManager.setInternetAvailable(true);

//                i = new Intent("DOWNLOADING_SAVED_ROUTES");
                InternetStatus internetStatus=new InternetStatus(true,context.getString(R.string.info_internet_available));
                EventBus.getDefault().post(internetStatus);


        } else {
//            sessionManager.setInternetAvailable(false);

                InternetStatus internetStatus=new InternetStatus(false,context.getString(R.string.info_internet_not_available));
                EventBus.getDefault().post(internetStatus);

        }

    }
}
