package com.example.user;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    static String TAG = "MainActivity";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        EditText userField = findViewById(R.id.userEditText);
        EditText passwordField = findViewById(R.id.passwordEditText);
        Button LogInBtn = findViewById(R.id.LogInBtn);
        Button SignUpBtn = findViewById(R.id.SignUpBtn);
        ImageButton psw_privacy = findViewById(R.id.psw_privacy);
        Log.d(TAG,"In main activity");

        psw_privacy.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        psw_privacy.setImageResource(R.drawable.hide);
                        break;
                    case MotionEvent.ACTION_DOWN:
                        passwordField.setInputType(InputType.TYPE_CLASS_TEXT);
                        psw_privacy.setImageResource(R.drawable.view);
                        break;
                }
                return false;
            }
        });

        LogInBtn.setOnClickListener(v -> {
            Log.d(TAG, "click the sign in btn");
            if (TextUtils.isEmpty(userField.getText())) {
                userField.requestFocus();
                userField.setError("Field Cannot Be Empty");
            }
            else if (TextUtils.isEmpty(passwordField.getText())) {
                passwordField.requestFocus();
                passwordField.setError("Field Cannot Be Empty");
            }
            else {
                String phone = userField.getText().toString();
                String password = passwordField.getText().toString();
                Map<String, String> postData = new HashMap<>();
                postData.put("phone", phone);
                postData.put("password", password);
                HttpPostAsyncTask task =new HttpPostAsyncTask(postData);
                task.execute("http://172.20.10.2:8080/server-side/login.php");
            }
        });
        SignUpBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Register.class);
            startActivity(intent);
        });
    }

    protected class HttpPostAsyncTask extends AsyncTask<String, Void, String> {
        JSONObject postData;

        public HttpPostAsyncTask(Map<String, String> postData) {
            if (postData != null) {
                this.postData = new JSONObject(postData);
                Log.d(TAG, "postData is " + postData);
            }
        }

        @Override
        protected String doInBackground(String... params) {
            Log.d(TAG, "doInBackground");
            StringBuilder response = new StringBuilder();
            URL url;
            HttpURLConnection urlConnection;
            try {
                url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setUseCaches(false);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestMethod("POST");
                if (this.postData != null) {
                    OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                    writer.write(postData.toString());
                    writer.flush();
                }
                int responseCode = urlConnection.getResponseCode();
                Log.d(TAG, "response code is " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader= new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8));
                    String line;
                    try {
                        while((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                    } catch (IOException e) {
                        Log.e(TAG,e.getMessage());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            return response.toString();
        }

        protected void onPostExecute(String postResult) {
            try {
                Log.d(TAG, "post result = " + postResult);
                JSONObject jsonObject = new JSONObject(postResult);
                String status = jsonObject.getString("status"); // 解析php回傳的json，擷取int "status"
                Log.d(TAG, "response status is " + status);

                switch (status) {
                    case "1":
                        String SID = jsonObject.getString("SID");
                        SharedPreferences sharedPreferences = getSharedPreferences("sharePreferences", MODE_PRIVATE);
                        sharedPreferences.edit().putString("SID", SID).apply();
                        Intent intent = new Intent(MainActivity.this, UserInterface.class);
                        startActivity(intent);
                        break;
                    case "-1":
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("用戶名稱錯誤")
                                .setMessage("你輸入的號碼似乎不屬於任何帳號。請檢查電話號碼，然後再試一次。").show();
                        break;
                    case "0":
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("密碼錯誤")
                                .setMessage("請檢查密碼，並重新登入。").show();
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}