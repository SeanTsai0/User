package com.example.user;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText nameField = findViewById(R.id.NameField);
        EditText phoneField = findViewById(R.id.phoneField);
        EditText passwordField = findViewById(R.id.passwordField);
        EditText confirmPasswordField = findViewById(R.id.confirmPasswordField);
        Button registerBtn = findViewById(R.id.registerBtn);

        registerBtn.setOnClickListener(v -> {
            if (TextUtils.isEmpty(nameField.getText())) {
                nameField.requestFocus();
                nameField.setError("Field Cannot Be Empty");
            } else if (TextUtils.isEmpty(phoneField.getText())) {
                phoneField.requestFocus();
                phoneField.setError("Field Cannot Be Empty");
            } else if (TextUtils.isEmpty(passwordField.getText())) {
                passwordField.requestFocus();
                passwordField.setError("Field Cannot Be Empty");
            } else if (!passwordField.getText().toString().equals(confirmPasswordField.getText().toString())) {
                confirmPasswordField.requestFocus();
                confirmPasswordField.setError("重複輸入的密碼錯誤");
            }
            else {
                String name = nameField.getText().toString();
                String phone = phoneField.getText().toString();
                String password = passwordField.getText().toString();
                Map<String, String> postData = new HashMap<>();
                postData.put("name", name);
                postData.put("phone", phone);
                postData.put("password", password);
                HttpInsertAsyncTask task = new HttpInsertAsyncTask(postData);
                task.execute("http://172.20.10.2:8080/server-side/insert.php");
            }
        });
    }
    protected class HttpInsertAsyncTask extends AsyncTask<String, Void, String> {
        JSONObject postData;

        public HttpInsertAsyncTask(Map<String, String> postData) {
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
                Toast.makeText(Register.this, "註冊成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Register.this, MainActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}