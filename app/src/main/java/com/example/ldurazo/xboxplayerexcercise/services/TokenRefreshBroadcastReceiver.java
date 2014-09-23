package com.example.ldurazo.xboxplayerexcercise.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.ldurazo.xboxplayerexcercise.asynctasks.TokenObtainableAsyncTask;
import com.example.ldurazo.xboxplayerexcercise.asynctasks.TokenTaskCallback;

public class TokenRefreshBroadcastReceiver extends BroadcastReceiver implements TokenTaskCallback {
    private final static String TAG = "com.example.ldurazo.xboxplayerexcercise.adapters.tokenrefreshbroadcastreceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w(TAG, "Received");
        new TokenObtainableAsyncTask(this).execute();
}

    @Override
    public void onTokenReceived(String response) {
        Log.w(TAG, "Token refreshed");
    }

    @Override
    public void onTokenNotReceived() {
        Log.w(TAG, "Token not refreshed");
        // TODO handle the token problems
    }
}
