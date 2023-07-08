package com.example.user;

import static android.content.Context.MODE_PRIVATE;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class userInformation extends Fragment {
    public userInformation() {
        // Required empty public constructor
    }
    private static String TAG = "userInformation";
    public static TextView nameInfo, emailInfo, phoneInfo, birthInfo;
    public static LinearLayout InfoGroup;
    public static RelativeLayout loadingPanel;
    public static ImageView img;
    static String rName , rMail, rPhone, rBirth, rImg;
    private final static int DO_UPDATE_TEXT = 0, HOLD = 1;
    private static Bitmap bitmap;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("time", java.time.LocalDate.now().toString());

        Map<String, String> postData = new HashMap<>();
        String SID = this.getActivity().getSharedPreferences("sharePreferences", MODE_PRIVATE).getString("SID", "null");
        postData.put("SID", SID);
        Log.d(TAG, SID);
        GetRecord getRecord = new GetRecord(postData);
        getRecord.execute("http://163.13.201.93/server-side/record.php");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_information, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saveInstanceState) {
        super.onViewCreated(view, saveInstanceState);
        InfoGroup = view.findViewById(R.id.InfoGroup);
        loadingPanel = view.findViewById(R.id.loadingPanel);
        nameInfo = view.findViewById(R.id.nameInfo);
        emailInfo = view.findViewById(R.id.emailInfo);
        phoneInfo = view.findViewById(R.id.phoneInfo);
        birthInfo = view.findViewById(R.id.birthInfo);
        img = view.findViewById(R.id.img);
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
                urlConnection.setUseCaches(true);

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
            Log.d(TAG, "post result = " + postResult);
            try {
                JSONObject jsonObject = new JSONObject(postResult);
                rName = jsonObject.getString("name");
                rMail = jsonObject.getString("mail");
                rPhone = jsonObject.getString("phone");
                rBirth = jsonObject.getString("birth");
                rImg = jsonObject.getString("img");
                byte[] decodeString = Base64.getDecoder().decode(rImg);
                InputStream inputStream = new ByteArrayInputStream(decodeString);
                bitmap = BitmapFactory.decodeStream(inputStream);

                Log.d("img", Integer.toString(rImg.length()));
                handler.sendEmptyMessage(DO_UPDATE_TEXT);

            } catch(Exception e) {
                Log.e("Debug", e.getMessage());
            }
        }
    }
    private static void doUpdate() {
        nameInfo.setText(rName);
        emailInfo.setText(rMail);
        phoneInfo.setText(rPhone);
        birthInfo.setText(rBirth);
        img.setImageBitmap(bitmap);
        loadingPanel.setVisibility(View.GONE);
        img.setVisibility(View.VISIBLE);
        InfoGroup.setVisibility(View.VISIBLE);
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