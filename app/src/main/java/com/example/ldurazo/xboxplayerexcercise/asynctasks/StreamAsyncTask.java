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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;

public class StreamAsyncTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = "ldurazo.xboxplayerexcercise.asynctasks.stream";
    private Track track;
    private StreamCallback callback;

    public StreamAsyncTask(Track track, StreamCallback callback){
        this.track = track;
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(String streamURL) {
        super.onPostExecute(streamURL);
        callback.onStreamReceived(streamURL);
    }

    @Override
    protected String doInBackground(Void... voids) {
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
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
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
                    + "/1/content/"+track.getId()+"/preview"
                    + "?clientInstanceId=fa624b17-412c-454a-a5a5-950bb06ae019"
                    + "&accessToken=Bearer+"
                    + AppSession.getInstance().getAccessToken();
            Log.w(TAG, query);
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

    private String retrieveSearchResults(String jsonString){
        try {
            JSONObject parentData = new JSONObject(jsonString);
            if(!parentData.isNull("Error")){
                String errorCode = parentData.getJSONObject("Error").getString("ErrorCode");
                Log.w(TAG, errorCode);
                if(errorCode.equals("ACCESS_TOKEN_EXPIRED")){
                    Log.w(TAG, Constants.ERROR+" - "+String.valueOf(AppSession.FLAG_TOKEN_EXPIRED));
                }if(errorCode.equals("CATALOG_NO_RESULT")){
                    Log.w(TAG, Constants.ERROR+" - "+String.valueOf(AppSession.FLAG_TOKEN_EXPIRED));
                }
            }else{
                return parentData.getString("Url");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
