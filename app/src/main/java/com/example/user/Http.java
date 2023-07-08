package com.example.user;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Http extends AsyncTask<String, Void, String> {
    private static String TAG = "Http";
    JSONObject postData;

    public Http(Map<String, String> postData) {
        if (postData != null) {
            this.postData = new JSONObject(postData);
        }
    }

    @Override
    protected String doInBackground(String... params) {
        StringBuilder response = new StringBuilder();
        URL url;
        HttpURLConnection urlConnection;

        try {
            url = new URL(params[0]);
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);

            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestMethod("POST");

            if (this.postData != null) {
                OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                writer.write(postData.toString());
                writer.flush();
            }

            int responseCode = urlConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8));
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                } catch(Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return response.toString();
    }
}
