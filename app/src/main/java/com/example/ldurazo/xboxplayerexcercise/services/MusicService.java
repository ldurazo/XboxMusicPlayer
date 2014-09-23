package com.example.ldurazo.xboxplayerexcercise.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.example.ldurazo.xboxplayerexcercise.activities.MusicPlayerActivity;
import com.example.ldurazo.xboxplayerexcercise.applications.AppSession;
import com.example.ldurazo.xboxplayerexcercise.applications.BaseApp;
import com.example.ldurazo.xboxplayerexcercise.models.Track;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener{

    private static final String TAG = "com.example.ldurazo.xboxplayerexcercise.controllers.musicservice";
    private MediaPlayer player;
    private ArrayList<Track> trackList;
    private int songPosn;
    private ServiceChanges mCallback;
    private final IBinder musicBind = new MusicBinder();
    private String trackUrl;

    public class MusicBinder extends Binder {
        public MusicService getService(ServiceChanges callback) {
            mCallback = callback;
            return MusicService.this;
        }
    }
    public void initTrack(ArrayList<Track> trackList, int songPosn){
        this.trackList = trackList;
        this.songPosn = songPosn;
    }

    //region Service Life cycle

    @Override
    public void onCreate() {
        super.onCreate();
        //initialize position
        songPosn=0;
        player = new MediaPlayer();
        initPlayer();
    }

    public void initPlayer(){
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    //endregion

    //region PlayerMethods
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        Intent notIntent = new Intent(this, MusicPlayerActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentIntent(pendInt)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setTicker(trackList.get(songPosn).getName())
                .setOngoing(true)
                .setContentTitle(trackList.get(songPosn).getName())
                .setContentText(trackList.get(songPosn).getName());
        Notification notification = builder.build();
        startForeground(1, notification);
    }

    @Override
    //On song playback ended
    public void onCompletion(MediaPlayer mediaPlayer) {
        stopForeground(true);
        if(player.getCurrentPosition()>=0){
            mediaPlayer.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
        mediaPlayer.reset();
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        playNext();
    }

    public void playNext(){
        songPosn++;
        if(songPosn>=0 && songPosn<trackList.size()){
            prepareSong();
        }else{
            songPosn--;
        }
    }

    public void playPrev(){
        songPosn--;
        if(songPosn>=0 && songPosn<trackList.size()){
            prepareSong();
        }else{
            songPosn++;
        }
    }

    public void prepareSong(){
        player.reset();
        String URL = AppSession.SCOPE_SERVICE
                + "/1/content/"+trackList.get(songPosn).getId()+"/preview"
                + "?clientInstanceId=fa624b17-412c-454a-a5a5-950bb06ae019"
                + "&accessToken=Bearer+"
                + AppSession.getInstance().getAccessToken();
        StringRequest request = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response){
                try {
                    JSONObject parentData = new JSONObject(response);
                    playSongFromUrl(parentData.getString("Url"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.w(TAG, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });
        BaseApp.getInstance().addToRequestQueue(request, TAG);
    }

    public void playSongFromUrl(String streamURL) {
        if (streamURL != null) {
            trackUrl = streamURL;
            try {
                Uri trackUri = Uri.parse(trackUrl);
                player.setDataSource(getBaseContext(), trackUri);
                mCallback.onNewSongPlayed(songPosn);
            } catch (Exception e) {
                Log.e("MUSIC SERVICE", "Error setting data source", e);
            }
            player.prepareAsync();
        }
        else{
            prepareSong();
        }
    }

    //endregion

    //region Mediacontroller
    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPlaying(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        player.start();
    }
    //endregion
}
