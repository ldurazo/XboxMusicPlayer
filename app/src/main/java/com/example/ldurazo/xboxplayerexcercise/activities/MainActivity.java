package com.example.ldurazo.xboxplayerexcercise.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ldurazo.xboxplayerexcercise.R;
import com.example.ldurazo.xboxplayerexcercise.asynctasks.SearchArtistAsyncTask;
import com.example.ldurazo.xboxplayerexcercise.models.Constants;

public class MainActivity extends Activity {
    private String accessToken;
    private String searchType="Artist";
    TextView textView;
    EditText editText;
    RadioButton artistRadioButton;
    RadioButton albumRadioButton;
    RadioButton songRadioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        accessToken=getIntent().getExtras().getString(Constants.TOKEN);
        initUI();
    }

    private void initUI(){
        textView = (TextView) findViewById(R.id.textView);
        editText = (EditText) findViewById(R.id.editText);
        artistRadioButton = (RadioButton) findViewById(R.id.artist_radio_button);
        albumRadioButton = (RadioButton) findViewById(R.id.album_radio_button);
        songRadioButton = (RadioButton) findViewById(R.id.song_radio_button);
    }

    public void searchArtist(View v){
        String search_query;
        if(!editText.getText().toString().equals(null) && !editText.getText().toString().equals("")){
            search_query=editText.getText().toString();
            search_query = search_query.replaceAll("\\s+", "+");
            searchType = getSearchType();
            new SearchArtistAsyncTask(accessToken, search_query, searchType).execute();
        }else{
            Toast.makeText(MainActivity.this, "Please introduce a search text", Toast.LENGTH_SHORT).show();
        }
    }

    private String getSearchType(){
        if(artistRadioButton.isChecked()){
            return "Artists";
        }if(albumRadioButton.isChecked()){
            return "Albums";
        }if(songRadioButton.isChecked()){
            return "Tracks";
        }
        return "Artists";
    }
}
