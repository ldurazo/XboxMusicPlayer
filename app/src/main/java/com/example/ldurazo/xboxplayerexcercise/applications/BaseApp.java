package com.example.ldurazo.xboxplayerexcercise.applications;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;
import com.example.ldurazo.xboxplayerexcercise.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;


public class BaseApp extends Application {
    /**
     * Log or request TAG
     */
    public static final String TAG = "VolleyPatterns";
    /**
     * Global request queue for Volley
     */
    private RequestQueue mRequestQueue;
    /**
     * A singleton instance of the application class for easy access in other places
     */
    private static BaseApp sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        //ImageLoader configuration initialization
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisk(true)
        .showImageForEmptyUri(R.drawable.ic_launcher)
        .showImageOnFail(R.drawable.ic_launcher)
        .showImageOnLoading(R.drawable.ic_launcher)
        .build();

        //Create ImageLoaderConfiguration
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .diskCacheFileCount(100)
                .memoryCacheSize(2 * 1024 * 1024)
                .defaultDisplayImageOptions(defaultOptions)
                .build();

        //Initialize ImageLoader with configuration
        ImageLoader.getInstance().init(config);

        //Initialize custom singletons
        initSingletons();
    }

    protected void initSingletons(){
        AppSession.initInstance();
    }

    /**
     * @return ApplicationController singleton instance
     */
    public static synchronized BaseApp getInstance() {
        return sInstance;
    }

    public RequestQueue getRequestQueue() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    /**
     * Adds the specified request to the global queue, if tag is specified
     * then it is used else Default TAG is used.
     *
     * @param req
     * @param tag
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);

        VolleyLog.d("Adding request to queue: %s", req.getUrl());

        getRequestQueue().add(req);
    }

    /**
     * Cancels all pending requests by the specified TAG, it is important
     * to specify a TAG so that the pending/ongoing requests can be cancelled.
     *
     * @param tag
     */
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}
