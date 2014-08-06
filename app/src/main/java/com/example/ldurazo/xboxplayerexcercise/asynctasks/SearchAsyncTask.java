package com.example.ldurazo.xboxplayerexcercise.asynctasks;

import android.os.AsyncTask;
import android.util.Log;

import com.example.ldurazo.xboxplayerexcercise.applications.AppSession;
import com.example.ldurazo.xboxplayerexcercise.models.Track;
import com.example.ldurazo.xboxplayerexcercise.utils.Constants;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

public class SearchAsyncTask extends AsyncTask<Void, Void, ArrayList<Track>> {
    private static final String TAG = "com.example.ldurazo.xboxplayerexcercise.asynctasks";
    private String searchQuery;
    private String searchType;
    private SearchTaskCallback callback;
    private int searchFlag = AppSession.FLAG_DEFAULT;

    public SearchAsyncTask(String searchQuery, String searchType, SearchTaskCallback callback) {
            this.callback = callback;
            this.searchQuery=searchQuery;
            this.searchType =searchType;
    }

    @Override
    protected ArrayList<Track> doInBackground(Void... voids) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            // Calls the search flow to receive the json string
            InputStream inputStream = establishConnection();
            if(inputStream!=null){
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"),24);
                String inputLine;
                while((inputLine=bufferedReader.readLine())!=null){
                    stringBuilder.append(inputLine);
                    Log.w(TAG, inputLine);
                }
                return retrieveSearchResults(stringBuilder.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Constants.EMPTY_LIST;
    }

    private InputStream establishConnection(){
        try {
            HttpParams httpParameters = new BasicHttpParams();
            int timeoutConnection = 10000; //Timeout until a connection is established.
            int timeoutSocket = 10000; //Timeout for waiting for data.
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            HttpClient client = new DefaultHttpClient(httpParameters);
            String query = AppSession.SCOPE_SERVICE
                    +"/1/content/music/search?q="
                    +searchQuery
                    +"&accessToken=Bearer+"
                    + AppSession.getInstance().getAccessToken();
            Log.w(TAG,query);
            HttpGet request = new HttpGet(query);
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json");
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            Header contentEncoding = response.getFirstHeader("Content-Encoding");
            if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                InputStream inputStream = response.getEntity().getContent();
                inputStream = new GZIPInputStream(inputStream);
                return inputStream;
            }
            return entity.getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ArrayList<Track> retrieveSearchResults(String jsonString){
        try {
            ArrayList<Track> resultList= new ArrayList();
            JSONObject parentData = new JSONObject(jsonString);
            if(!parentData.isNull("Error")){
                String errorCode = parentData.getJSONObject("Error").getString("ErrorCode");
                Log.w(TAG, errorCode);
                if(errorCode.equals("ACCESS_TOKEN_EXPIRED")){
                    searchFlag=AppSession.FLAG_TOKEN_EXPIRED;
                    Log.w(TAG, String.valueOf(searchFlag));
                }if(errorCode.equals("CATALOG_NO_RESULT")){
                    searchFlag=AppSession.FLAG_NO_RESULT;
                    Log.w(TAG, String.valueOf(searchFlag));
                }
            }else{
                JSONObject searchTypeObject = parentData.getJSONObject(searchType);
                JSONArray searchResults = searchTypeObject.getJSONArray("Items");
                JSONObject searchObject;
                Track track;
                for (int i=0; i<searchResults.length();i++){
                    searchObject = searchResults.getJSONObject(i);
                    track = new Track(searchObject.getString("Id"),
                            searchObject.getString("Name"),
                            searchObject.getString("ImageUrl"),
                            searchType);
                    resultList.add(track);
                }
                return resultList;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Constants.EMPTY_LIST;
    }


    @Override
    protected void onPostExecute(ArrayList<Track> list) {
        callback.onSearchCompleted(list, searchFlag);
        super.onPostExecute(list);
    }
}
