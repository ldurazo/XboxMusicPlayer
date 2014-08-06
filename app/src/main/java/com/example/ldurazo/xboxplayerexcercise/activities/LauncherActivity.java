package com.example.ldurazo.xboxplayerexcercise.activities;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
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


public class LauncherActivity extends BaseActivity implements TokenTaskCallback {
    private static final String TAG = "com.example.ldurazo.xboxplayerexcercise.activities.baseactivity";
    private ProgressDialog dialog;
    private TextView launcherText;
    private Animation animation;
    private PendingIntent tokenRefreshPendingIntent;
    private AlarmManager alarmManager;

    @Override
    protected void onResume() {
        super.onResume();
        if(AppSession.getInstance().getAccessToken() != null){
            Intent searchIntent = new Intent(LauncherActivity.this, SearchActivity.class);
            startActivity(searchIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        initToken();
        initVars();
        initUI();
    }

    protected void initToken(){
        new TokenObtainableAsyncTask(this).execute();
    }

    @Override
    protected void initVars() {
        dialog = new ProgressDialog(LauncherActivity.this);
        dialog.setTitle("Please wait...");
    }

    @Override
    protected void initUI(){
        launcherText = (TextView) findViewById(R.id.launchText);
        animation = AnimationUtils.loadAnimation(LauncherActivity.this, R.anim.blink);
        launcherText.startAnimation(animation);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(alarmManager!=null && tokenRefreshPendingIntent!=null){
            alarmManager.cancel(tokenRefreshPendingIntent);
        }
    }

    @Override
    public void onTokenReceived(String response) {
        Log.w(TAG, response);
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        AppSession.getInstance().setTokenExpireTime(System.currentTimeMillis() + (1000 * 60 * 9));
        Intent tokenRefreshIntent = new Intent(this, TokenRefreshBroadcastReceiver.class);
        tokenRefreshPendingIntent = PendingIntent.getBroadcast
                (LauncherActivity.this, 0, tokenRefreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, AppSession.getInstance().getTokenExpireTime(), 1000 * 60 * 9, tokenRefreshPendingIntent);
        Intent searchIntent = new Intent(LauncherActivity.this, SearchActivity.class);
        startActivity(searchIntent);
    }


    @Override
    public void onTokenNotReceived() {
        if(!dialog.isShowing()){
            dialog.show();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(LauncherActivity.this);
        builder.setMessage("We cannot connect to the service right now")
                .setNeutralButton("Ok", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
    }
}