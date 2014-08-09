package com.example.ldurazo.xboxplayerexcercise.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.example.ldurazo.xboxplayerexcercise.R;
import com.example.ldurazo.xboxplayerexcercise.applications.AppSession;
import com.example.ldurazo.xboxplayerexcercise.asynctasks.TokenObtainableAsyncTask;
import com.example.ldurazo.xboxplayerexcercise.asynctasks.TokenTaskCallback;
import com.example.ldurazo.xboxplayerexcercise.controllers.TokenRefreshBroadcastReceiver;


public class LauncherActivity extends Activity implements TokenTaskCallback {
    private static final String TAG = "com.example.ldurazo.xboxplayerexcercise.activities.baseactivity";
    private TextView launcherText;
    private Animation animation;
    private PendingIntent tokenRefreshPendingIntent;
    private AlarmManager alarmManager;


    //region Lifecycle Method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        initToken();
        initUI();
    }

    protected void initToken(){
        new TokenObtainableAsyncTask(this).execute();
    }

    protected void initUI(){
        launcherText = (TextView) findViewById(R.id.launchText);
        animation = AnimationUtils.loadAnimation(LauncherActivity.this, R.anim.blink);
        launcherText.startAnimation(animation);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(AppSession.getInstance().getAccessToken() != null){
            Intent PlayerIntent = new Intent(LauncherActivity.this, MusicPlayerActivity.class);
            startActivity(PlayerIntent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(alarmManager!=null && tokenRefreshPendingIntent!=null){
            alarmManager.cancel(tokenRefreshPendingIntent);
        }
    }

    //endregion

    @Override
    public void onTokenReceived(String response) {
        Log.w(TAG, response);
        AppSession.getInstance().setTokenExpireTime(System.currentTimeMillis() + (1000 * 60 * 9));
        Intent tokenRefreshIntent = new Intent(this, TokenRefreshBroadcastReceiver.class);
        tokenRefreshPendingIntent = PendingIntent.getBroadcast
                (LauncherActivity.this, 0, tokenRefreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, AppSession.getInstance().getTokenExpireTime(), 1000 * 60 * 9 , tokenRefreshPendingIntent);
        Intent searchIntent = new Intent(LauncherActivity.this, MusicPlayerActivity.class);
        startActivity(searchIntent);
    }


    @Override
    public void onTokenNotReceived() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LauncherActivity.this);
        builder.setMessage("We cannot connect to the service, check your internet connection and retry")
                .setNeutralButton("Ok", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                });
        builder.setCancelable(false);
        builder.show();
    }
}