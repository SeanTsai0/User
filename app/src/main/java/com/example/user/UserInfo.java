package com.example.user;

import androidx.appcompat.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class UserInfo extends AppCompatActivity {
    public static TextView nameInfo, genderInfo, emailInfo, phoneInfo;
    static String rName , rGender, rMail, rPhone;
    private final static int DO_UPDATE_TEXT = 0, DO_THAT = 1;
    private static final Handler handler = new Handler(msg -> {
        final int what = msg.what;
        switch (what) {
            case DO_UPDATE_TEXT: doUpdate(); break;
        }
        return false;
    });
    private static void doUpdate() {
        nameInfo.setText(rName);
        genderInfo.setText(rGender);
        emailInfo.setText(rMail);
        phoneInfo.setText(rPhone);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        nameInfo = findViewById(R.id.nameInfo);
        genderInfo = findViewById(R.id.genderInfo);
        emailInfo = findViewById(R.id.emailInfo);
        phoneInfo = findViewById(R.id.phoneInfo);

        Map<String, String> postData = new HashMap<>();
        String SID = getSharedPreferences("sharePreferences", MODE_PRIVATE).getString("SID", "null");
        postData.put("SID", SID);

        GetRecord getRecord = new GetRecord(postData);
        getRecord.execute("http://172.20.10.2:8080/server-side/record.php");

    }

    protected static class GetRecord extends AsyncTask<String, Void, String> {
        JSONObject postData;

        public GetRecord(Map<String, String> postData) {
            if (postData != null) {
                this.postData = new JSONObject(postData);
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String response = "";
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
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                    String line = "";
                    try {
                        while ((line = reader.readLine()) != null) {
                            response += line;
                        }
                    } catch(Exception e) {
                        Log.e("Debug", e.getMessage());
                    }
                }
            } catch (Exception e) {
                Log.e("Debug", e.getMessage());
            }
            return response;
        }

        protected void onPostExecute(String postResult) {
            try {
                JSONObject jsonObject = new JSONObject(postResult);
                rName = jsonObject.getString("name");
                rGender = jsonObject.getString("gender");
                rMail = jsonObject.getString("mail");
                rPhone = jsonObject.getString("phone");

                handler.sendEmptyMessage(DO_UPDATE_TEXT);

            } catch(Exception e) {
                Log.e("Debug", e.getMessage());
            }
        }
    }
}