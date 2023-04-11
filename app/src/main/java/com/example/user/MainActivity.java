package com.example.user;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText userField = findViewById(R.id.userEditText);
        EditText passwordField = findViewById(R.id.passwordEditText);
        Button LogInBtn = findViewById(R.id.LogInBtn);
        Button SignUpBtn = findViewById(R.id.SignUpBtn);
        TextView forgotPassword = findViewById(R.id.forgotPasswordBtn);

        LogInBtn.setOnClickListener(v -> {
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
                Map<String, String>postData =new HashMap<>();
                postData.put("phone", phone);
                postData.put("password", password);
                HttpPostAsyncTask task =new HttpPostAsyncTask(postData);
                task.execute("http://172.20.10.2:8080/server-side/read.php");
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

                if (responseCode == HttpURLConnection.HTTP_OK) {

                    BufferedReader reader= new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                    String line = "";
                    try {
                        while((line = reader.readLine()) != null) {
                            response += line;
                        }

                    } catch (IOException e) {
                        Log.e("error",e.getMessage());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        protected void onPostExecute(String postResult) {
            try {
                JSONObject jsonObject = new JSONObject(postResult);
                int status = jsonObject.getInt("status"); // 解析php回傳的json，擷取boolean "status"

                switch (status) {
                    case 1:
                        Intent intent = new Intent(MainActivity.this, UserInterface.class);
                        startActivity(intent);
                        break;
                    case -1:
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("用戶名稱錯誤")
                                .setMessage("你輸入的號碼似乎不屬於任何帳號。請檢查電話號碼，然後再試一次。").show();
                        break;
                    case 0:
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("密碼錯誤")
                                .setMessage("請檢查密碼，並重新登入。").show();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}