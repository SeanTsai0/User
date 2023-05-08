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
        Button pick_up_btn = findViewById(R.id.pick_up_btn);
        Button SignOut_btn = findViewById(R.id.SignOut_btn);

        userInfo_btn.setOnClickListener(v -> {
            startActivity(new Intent(UserInterface.this, UserInfo.class));
        });

        record_btn.setOnClickListener(v->{
            startActivity(new Intent(UserInterface.this, Record.class));
        });

        pick_up_btn.setOnClickListener(v -> {

        });

        SignOut_btn.setOnClickListener(v->{
            startActivity(new Intent(UserInterface.this, MainActivity.class));
        });

    }
}