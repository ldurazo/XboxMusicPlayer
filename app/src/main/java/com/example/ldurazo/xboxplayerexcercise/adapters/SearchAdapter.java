package com.example.ldurazo.xboxplayerexcercise.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ldurazo.xboxplayerexcercise.R;
import com.example.ldurazo.xboxplayerexcercise.models.Track;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;


public class SearchAdapter extends BaseAdapter{
    private ArrayList<Track> tracks;
    private LayoutInflater inflater;
    private ImageLoader imageLoader;
    private static final String TAG = "com.example.ldurazo.xboxplayerexcercise.adapters.SearchAdapter";

    public SearchAdapter(Context context, ArrayList<Track> tracks) {
        inflater = LayoutInflater.from(context);
        this.tracks = tracks;
        imageLoader = ImageLoader.getInstance();
    }

    @Override
    public int getCount() {
        return tracks.size();
    }

    @Override
    public Object getItem(int i) {
        return tracks.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if(view==null){
            view = inflater.inflate(R.layout.row_view, null);
            holder = new ViewHolder();
            holder.text = (TextView) view.findViewById(R.id.textView);
            holder.imageView = (ImageView) view.findViewById(R.id.imageView);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }
        holder.text.setText(tracks.get(i).getName());
        imageLoader.displayImage(tracks.get(i).getImageURL()+"&w=100&h=100", holder.imageView);
        holder.text.setSelected(true);
        return view;
    }

    private class ViewHolder{
        TextView text;
        ImageView imageView;
    }
}
