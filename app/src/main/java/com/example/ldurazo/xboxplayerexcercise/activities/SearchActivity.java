package com.example.ldurazo.xboxplayerexcercise.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ldurazo.xboxplayerexcercise.R;
import com.example.ldurazo.xboxplayerexcercise.adapters.DataWrapper;
import com.example.ldurazo.xboxplayerexcercise.adapters.SearchAdapter;
import com.example.ldurazo.xboxplayerexcercise.asynctasks.SearchTaskCallback;
import com.example.ldurazo.xboxplayerexcercise.asynctasks.TokenTaskCallback;
import com.example.ldurazo.xboxplayerexcercise.asynctasks.SearchAsyncTask;
import com.example.ldurazo.xboxplayerexcercise.asynctasks.TokenObtainableAsyncTask;
import com.example.ldurazo.xboxplayerexcercise.applications.AppSession;
import com.example.ldurazo.xboxplayerexcercise.utils.Constants;
import com.example.ldurazo.xboxplayerexcercise.models.Track;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class SearchActivity extends BaseActivity implements SearchTaskCallback {
    private static final String TAG = "com.example.ldurazo.xboxplayerexcercise.activities.searchactivity";
    private ProgressDialog mDialog;
    private EditText mSearchEditText;
    private RadioButton mArtistRadioButton;
    private RadioButton mAlbumRadioButton;
    private RadioButton mSongRadioButton;
    private ListView mListView;
    private Track mTrack;
    private SearchAsyncTask mSearchAsyncTask;
    private ArrayList<Track> mTrackList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVars();
        initUI();
    }

    @Override
    protected void initVars() {
        mDialog = new ProgressDialog(SearchActivity.this);
    }

    @Override
    protected void initUI() {
        mSearchEditText = (EditText) findViewById(R.id.editText);
        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    searchForResult();
                }
                return false;
            }
        });
        mArtistRadioButton = (RadioButton) findViewById(R.id.artist_radio_button);
        mAlbumRadioButton = (RadioButton) findViewById(R.id.album_radio_button);
        mSongRadioButton = (RadioButton) findViewById(R.id.song_radio_button);
        mListView = (ListView) findViewById(R.id.listView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent playerIntent = new Intent(SearchActivity.this, MusicPlayer.class);
                playerIntent.putExtra(MusicPlayer.TRACK_LIST, new DataWrapper(mTrackList));
                playerIntent.putExtra(MusicPlayer.FIRST_TRACK, i);
                startActivity(playerIntent);
            }
        });
    }

    public void searchForResult() {
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
        String search_query;
        String searchType;
        if (!mSearchEditText.getText().toString().equals(Constants.EMPTY_STRING)) {
            search_query = mSearchEditText.getText().toString();
            try {
                search_query = URLEncoder.encode(search_query, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            searchType = getSearchType();
            // this is hardcoded expired access token for testing purposes, use it instead of AppSession.getInstance().getAccessToken()
            // http%253a%252f%252fschemas.xmlsoap.org%252fws%252f2005%252f05%252fidentity%252fclaims%252fnameidentifier%3Dmusicplayer_internship_ldurazo%26http%253a%252f%252fschemas.microsoft.com%252faccesscontrolservice%252f2010%252f07%252fclaims%252fidentityprovider%3Dhttps%253a%252f%252fdatamarket.accesscontrol.windows.net%252f%26Audience%3Dhttp%253a%252f%252fmusic.xboxlive.com%26ExpiresOn%3D1406831538%26Issuer%3Dhttps%253a%252f%252fdatamarket.accesscontrol.windows.net%252f%26HMACSHA256%3DIbUBiV9cxaxmORMnCU38%252b1jiJlDV2fUHLOX0CF6rDGo%253d
            mSearchAsyncTask = new SearchAsyncTask(search_query, searchType, this);
            mSearchAsyncTask.execute();
        } else {
            hideDialog();
            Toast.makeText(SearchActivity.this, "Please introduce a search text", Toast.LENGTH_SHORT).show();
        }
    }

    private String getSearchType() {
        if (mArtistRadioButton.isChecked()) {
            return Track.ARTISTS;
        }
        if (mAlbumRadioButton.isChecked()) {
            return Track.ALBUMS;
        }
        if (mSongRadioButton.isChecked()) {
            return Track.TRACKS;
        }
        return Track.TRACKS;
    }

    @Override
    public void onSearchCompleted(ArrayList<Track> list, int searchFlag) {
        mTrackList = list;
        hideDialog();
        switch (searchFlag){
            case AppSession.FLAG_DEFAULT:
                if (mTrackList != null) {
                    mListView.setAdapter(new SearchAdapter(SearchActivity.this, list));
                }
                break;
            case AppSession.FLAG_NO_RESULT:
                AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
                builder.setMessage("No suitable result for your search")
                        .setNeutralButton("Ok", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mSearchEditText.setText(Constants.EMPTY_STRING);
                            }
                        });
                builder.show();
                break;
            case AppSession.FLAG_TOKEN_EXPIRED:
                new TokenObtainableAsyncTask(new TokenTaskCallback() {
                    @Override
                    public void onTokenReceived(String response) {
                        AppSession.getInstance().setAccessToken(response);
                    }

                    @Override
                    public void onTokenNotReceived() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
                        builder.setMessage("We cannot connect to the service right now")
                                .setNeutralButton("Ok", new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                    }
                                });
                    }
                }).execute();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.search_action) {
            searchForResult();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent().setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));
    }

    public void hideDialog() {
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }
}
