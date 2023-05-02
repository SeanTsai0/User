package com.example.user;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class UserInterface extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_interface);

        Button userInfo_btn = findViewById(R.id.userInfo_btn);
        Button record_btn = findViewById(R.id.record_btn);
        Button SignOut_btn = findViewById(R.id.SignOut_btn);

        userInfo_btn.setOnClickListener(v -> {
            Intent intent = new Intent(UserInterface.this, UserInfo.class);
            startActivity(intent);
        });

        record_btn.setOnClickListener(v->{
            Intent intent = new Intent(UserInterface.this, Record.class);
            startActivity(intent);
        });

        SignOut_btn.setOnClickListener(v->{
            Intent intent = new Intent(UserInterface.this, MainActivity.class);
            startActivity(intent);
        });

    }
}