package com.example.ldurazo.xboxplayerexcercise.models;

import java.io.Serializable;

////A track object can be either an artist, an album or a track itself
//as the xbox music service when performing the search it returns the same values for the
//three of them: id, name, imageurl and the type is for the app to know if the object is an artist
// or a track or an album in order to do the proper workflow after the search.
public class Track implements Serializable{
    private String Id;
    private String name;
    private String imageURL;
    private String type;

    public static final String ARTISTS="Artists";
    public static final String ALBUMS="Albums";
    public static final String TRACKS="Tracks";

    public Track(String id, String name, String imageURL, String TAG) {
        Id = id;
        this.name = name;
        this.imageURL = imageURL;
        this.type = TAG;
    }

    public String getId() {
        return Id;
    }

    public String getName() {
        return name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getType() {
        return type;
    }

}
