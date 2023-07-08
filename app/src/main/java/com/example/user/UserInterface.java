package com.example.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UserInterface extends AppCompatActivity {

    private BottomNavigationView.OnItemSelectedListener onItemSelectedListener
            = item -> {
                switch (item.getItemId()) {
                    case R.id.generate:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new generate()).commit();
                        return true;
                    case R.id.information:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new userInformation()).commit();
                        return true;
                    case R.id.record:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new records()).commit();
                        return true;
                    case R.id.signOut:
                        dialog();
                }
                return false;
            };

    private void dialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("是否確定要登出?");
        dialog.setPositiveButton("確認", (dialog1, which) -> finish());
        dialog.setNegativeButton("取消", (dialog12, which) -> {});
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_interface);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (savedInstanceState == null) {
            this.getSupportFragmentManager().beginTransaction().add(R.id.frameLayout, new generate()).commit();
        }

        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnItemSelectedListener(onItemSelectedListener);
    }

}