package com.example.user;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    static String TAG = "MainActivity";
    String identify = "1";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        EditText userField = findViewById(R.id.userEditText);
        EditText passwordField = findViewById(R.id.passwordEditText);
        Button LogInBtn = findViewById(R.id.LogInBtn);
        TextView SignUpBtn = findViewById(R.id.SignUpBtn);
        ImageButton psw_privacy = findViewById(R.id.psw_privacy);
        RadioGroup IdentifyGroup = findViewById(R.id.IdentifyGroup);
        Log.d(TAG,"In main activity");

        psw_privacy.setOnTouchListener((v, event) -> {

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
        });

        IdentifyGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.buyerRadioBtn) {
                identify = "1";
            } else {
                identify = "2";
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
                SignInRequest signInRequest =new SignInRequest(postData);
                signInRequest.execute("http://163.13.201.93/server-side/login.php");
            }
        });

        SignUpBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Register.class);
            startActivity(intent);
        });
        OnBackPressedCallback callback = new OnBackPressedCallback(false /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                finishAndRemoveTask();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
        callback.setEnabled(true);
    }
    protected class SignInRequest extends Http{
        public SignInRequest(Map<String, String> postData) {
            super(postData);
        }

        @Override
        protected void onPostExecute(String postResult) {
            try {
                Log.d(TAG, "post result = " + postResult);
                JSONObject jsonObject = new JSONObject(postResult);
                String status = jsonObject.getString("status"); // 解析php回傳的json，擷取int "status"
                Log.d(TAG, "response status is " + status);

                switch (status) {
                    case "1":
                        String SID = jsonObject.getString("SID");
                        String login_type = jsonObject.getString("identify");
                        SharedPreferences sharedPreferences = getSharedPreferences("sharePreferences", MODE_PRIVATE);
                        sharedPreferences.edit().putString("SID", SID).apply();

                        if (identify.equals("1") && login_type.equals("1")) {
                            Intent intent = new Intent(MainActivity.this, UserInterface.class);
                            startActivity(intent);
                        } else if (identify.equals("2") && login_type.equals("2")) {
                            Intent intent = new Intent(MainActivity.this, SellerInterface.class);
                            startActivity(intent);
                        } else {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("登入身分錯誤")
                                    .setMessage("你登入的帳號身分似乎選擇錯誤。請檢查登入身分，然後再試一次。").show();
                        }
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