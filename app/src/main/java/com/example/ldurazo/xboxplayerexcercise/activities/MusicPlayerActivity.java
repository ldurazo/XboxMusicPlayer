package com.example.ldurazo.xboxplayerexcercise.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.ldurazo.xboxplayerexcercise.R;
import com.example.ldurazo.xboxplayerexcercise.adapters.SearchAdapter;
import com.example.ldurazo.xboxplayerexcercise.applications.AppSession;
import com.example.ldurazo.xboxplayerexcercise.applications.BaseApp;
import com.example.ldurazo.xboxplayerexcercise.controllers.MusicService;
import com.example.ldurazo.xboxplayerexcercise.controllers.MusicService.MusicBinder;
import com.example.ldurazo.xboxplayerexcercise.controllers.ServiceChanges;
import com.example.ldurazo.xboxplayerexcercise.models.Track;
import com.example.ldurazo.xboxplayerexcercise.utils.Constants;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MusicPlayerActivity extends BaseActivity implements MediaPlayerControl, ServiceChanges, DrawerLayout.DrawerListener{

    private static final String TAG="com.example.ldurazo.xboxplayerexcercise.activities.musicplayer";
    private ArrayList<Track> mTrackList = new ArrayList<Track>();
    private ImageView mPlayerImageView;
    private SearchView mSearchView;
    public static final String TRACK_LIST="TrackList";
    public static final String FIRST_TRACK="FirstTrack";
    private int mCurrentTrack;
    private ImageLoader mImageLoader;
    private TextView mNowPlayingTextView;
    private static MediaController controller;
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;

    //region Lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.content_frame);
        // inflate the custom activity layout
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View activityView = layoutInflater.inflate(R.layout.activity_music_player, null,false);
        // add the custom layout of this activity to frame layout.
        frameLayout.addView(activityView);
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
        }
        startService(playIntent);
        bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        initUI();
        initVars();
    }

    @Override
    protected void onDestroy() {
        controller = null;
        unbindService(musicConnection);
        stopService(playIntent);
        super.onDestroy();
    }

    @Override
    protected void initUI() {
        mPlayerImageView = (ImageView) findViewById(R.id.PlayerImageView);
        mPlayerImageView.setVisibility(View.INVISIBLE);
        mNowPlayingTextView = (TextView) findViewById(R.id.now_playing_text_view);
        mDrawerLayout.setDrawerListener(this);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mCurrentTrack = i;
                musicSrv.initTrack(mTrackList, mCurrentTrack);
                musicSrv.prepareSong();
            }
        });
        mPlayerImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!controller.isShowing())
                    controller.show();
            }
        });
    }

    @Override
    protected void initVars() {
        mImageLoader = ImageLoader.getInstance();
        setController();
    }

    private void setController(){
        controller = new MediaController(MusicPlayerActivity.this);
        controller.setAnchorView(mPlayerImageView);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPrev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setEnabled(true);
    }


    //endregion

    //region UI Listener
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            startActivity(new Intent().setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        //Do nothing
    }

    @Override
    public void onDrawerOpened(View drawerView) {
        controller.hide();
    }

    @Override
    public void onDrawerClosed(View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {
    }
    //endregion

    //region Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        final MenuItem menuItem = menu.findItem(R.id.player_search_action);
        mSearchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                // Do something, faggot
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String search_query) {
                mTrackList.clear();
                if(search_query.equals(Constants.EMPTY_STRING)){
                    Toast.makeText(MusicPlayerActivity.this, "Please introduce a search text", Toast.LENGTH_SHORT).show();
                }
                else{
                    try {
                        search_query= URLEncoder.encode(search_query, "UTF-8");
                        String query = AppSession.SCOPE_SERVICE
                                +"/1/content/music/search?q="
                                +search_query
                                +"&accessToken=Bearer+"
                                + AppSession.getInstance().getAccessToken();
                        StringRequest request = new StringRequest(Request.Method.GET, query, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.w(TAG, response);
                                response = response.substring(3);
                                JSONObject parentData = null;
                                try {
                                    parentData = new JSONObject(response);
                                    JSONObject searchTypeObject = parentData.getJSONObject(Track.TRACKS);
                                    JSONArray searchResults = searchTypeObject.getJSONArray("Items");
                                    JSONObject searchObject;
                                    Track track;
                                    for (int i=0; i<searchResults.length();i++){
                                        searchObject = searchResults.getJSONObject(i);
                                        track = new Track(searchObject.getString("Id"),
                                                searchObject.getString("Name"),
                                                searchObject.getString("ImageUrl"),
                                                Track.TRACKS);
                                        mTrackList.add(track);
                                    }
                                    mDrawerList.setAdapter(new SearchAdapter(MusicPlayerActivity.this, mTrackList));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        });
                        BaseApp.getInstance().addToRequestQueue(request, TAG);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
                mSearchView.onActionViewCollapsed();
                mDrawerLayout.openDrawer(mDrawerLinearLayout
                );
                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            default://TODO
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    //endregion

    //region Service and media player
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder) service;
            //get service
            musicSrv = binder.getService(MusicPlayerActivity.this);
            //pass list
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    public void onNewSongPlayed(int songPosition) {
        mCurrentTrack=songPosition;
        displayImageAndTextfield();
    }

    private void displayImageAndTextfield(){
        int imageWidth = mPlayerImageView.getWidth();
        int imageHeight = mPlayerImageView.getHeight();
        mPlayerImageView.setVisibility(View.VISIBLE);
        mNowPlayingTextView.setText("Now playing: "+mTrackList.get(mCurrentTrack).getName());
        mImageLoader.displayImage(mTrackList.get(
                mCurrentTrack).getImageURL()
                +"&w="+ imageWidth
                +"&h="+ imageHeight
                ,mPlayerImageView);
        Log.d(TAG, "MENTIRAS, TU DIJISTE QUE ME AMABAS Y ERAN PURAS MENTIRAS");
    }

    //play next
    private void playNext(){
        musicSrv.playNext();
        mDrawerLayout.openDrawer(findViewById(R.id.drawer));
        mDrawerLayout.closeDrawers();
    }

    //play previous
    private void playPrev(){
        musicSrv.playPrev();
    }
    //endregion

    //region Media Controller
    @Override
    public void start() {
        musicSrv.go();
    }

    @Override
    public void pause() {
        musicSrv.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(musicSrv!=null && musicBound && musicSrv.isPlaying())
        return musicSrv.getDur();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound && musicSrv.isPlaying())
        return musicSrv.getPosn();
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicSrv!=null && musicBound)
        return musicSrv.isPlaying();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }




    //endregion


}
