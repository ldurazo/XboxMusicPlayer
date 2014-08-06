package com.example.ldurazo.xboxplayerexcercise.applications;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by ldurazo on 7/24/2014 and 2:12 PM.
 */
public class AppSession {
    private long tokenExpireTime;
    private String accessToken;
    private static AppSession instance;
    private static final String TAG = "com.example.ldurazo.xboxplayerexcercise.models.appsession";
    public static final String CLIENT_SECRET = "LDKFAP345sdklfj542564654";
    public static final String CALLBACK_URL = "http://luisdurazoa.tumblr.com/";
    public static final String SERVICE = "https://datamarket.accesscontrol.windows.net/v2/OAuth2-13";
    public static final String SCOPE = "http://music.xboxlive.com";
    public static final String SCOPE_SERVICE = "https://music.xboxlive.com";
    public static final String GRANT_TYPE = "client_credentials";
    public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    public static final String CLIENT_ID = "musicplayer_internship_ldurazo";

    public static final int FLAG_DEFAULT=0;
    public static final int FLAG_TOKEN_EXPIRED=1;
    public static final int FLAG_NO_RESULT=2;

    public static void initInstance() {
        if (instance == null) {
            instance = new AppSession();
        }
    }

    public static AppSession getInstance() {
        // Return the instance

        return instance;
    }

    private AppSession() {
        // Constructor hidden because this is a singleton
    }

    public Long getTokenExpireTime() {
        return tokenExpireTime;
    }

    public void setTokenExpireTime(Long tokenExpireTime) {
        this.tokenExpireTime = tokenExpireTime;
    }

    public String getAccessToken() {
        if (accessToken != null) {
            try {
                return URLEncoder.encode(accessToken, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

}

