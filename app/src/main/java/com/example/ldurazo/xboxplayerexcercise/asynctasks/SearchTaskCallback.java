package com.example.ldurazo.xboxplayerexcercise.asynctasks;

import com.example.ldurazo.xboxplayerexcercise.models.Track;

import java.util.ArrayList;

/**
 * Created by ldurazo on 7/23/2014 and 5:55 PM.
 */
public interface SearchTaskCallback {
    public void onSearchCompleted(ArrayList<Track> list, int errorFlag);
}
