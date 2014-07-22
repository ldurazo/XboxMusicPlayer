package com.example.ldurazo.xboxplayerexcercise.asynctasks;

/**
 * Created by ldurazo on 7/21/2014.
 */
public interface OnTokenTaskCallback {
    public void onTokenReceived(String response);
    public void onTokenNotReceived();
}
