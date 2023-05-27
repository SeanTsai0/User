package com.example.user;

import static android.content.Context.MODE_PRIVATE;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Inflater;

public class userInformation extends Fragment {

    public userInformation() {
        // Required empty public constructor
    }

    public static TextView nameInfo, genderInfo, emailInfo, phoneInfo;
    static String rName , rGender, rMail, rPhone;
    private final static int DO_UPDATE_TEXT = 0, HOLD = 1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Map<String, String> postData = new HashMap<>();
        String SID = this.getActivity().getSharedPreferences("sharePreferences", MODE_PRIVATE).getString("SID", "null");
        postData.put("SID", SID);

        GetRecord getRecord = new GetRecord(postData);
        getRecord.execute("http://172.20.10.2:8080/server-side/record.php");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_information, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saveInstanceState) {
        super.onViewCreated(view, saveInstanceState);
        nameInfo = view.findViewById(R.id.nameInfo);
        genderInfo = view.findViewById(R.id.genderInfo);
        emailInfo = view.findViewById(R.id.emailInfo);
        phoneInfo = view.findViewById(R.id.phoneInfo);
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
                        Log.e("Debug", e.getMessage());
                    }
                }
            } catch (Exception e) {
                Log.e("Debug", e.getMessage());
            }
            return response.toString();
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
    private static void doUpdate() {
        nameInfo.setText(rName);
        genderInfo.setText(rGender);
        emailInfo.setText(rMail);
        phoneInfo.setText(rPhone);
    }
    private static final Handler handler = new Handler(msg -> {
        final int what = msg.what;
        switch (what) {
            case DO_UPDATE_TEXT:
                doUpdate();
                break;
            case HOLD:
                break;
        }
        return false;
    });
}