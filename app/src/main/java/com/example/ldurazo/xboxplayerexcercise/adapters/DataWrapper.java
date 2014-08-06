package com.example.ldurazo.xboxplayerexcercise.adapters;

/**
 * Created by ldurazo on 8/1/2014 and 3:48 PM.
 */

import com.example.ldurazo.xboxplayerexcercise.models.Track;

import java.io.Serializable;
import java.util.ArrayList;


public class DataWrapper implements Serializable {
    private ArrayList<Track> tracks;

    public DataWrapper(ArrayList<Track> tracks) {
        this.tracks = tracks;
    }

    public ArrayList<Track> getTracks() {
        return tracks;
    }
}
