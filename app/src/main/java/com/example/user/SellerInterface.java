package com.example.user;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class SellerInterface extends AppCompatActivity {
    ActionBarDrawerToggle actionBarDrawerToggle;
    Bitmap bitmap;
    ImageView seller_img;
    View header;
    NavigationView navigationView;
    TextView seller_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_interface);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.nav_view);
        setSupportActionBar(toolbar);

        header = navigationView.getHeaderView(0);
        seller_img = header.findViewById(R.id.seller_img);
        seller_name = header.findViewById(R.id.seller_name);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (savedInstanceState == null) {
            this.getSupportFragmentManager().beginTransaction().add(R.id.nav_host_fragment, new sellerInformation()).commit();
        }

        NavigationView.OnNavigationItemSelectedListener itemSelectedListener =
                item -> {
                    switch (item.getItemId()) {
                        case R.id.ship_record:
                            getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new ship_product()).commit();
                            drawerLayout.close();
                            return true;
                        case R.id.my_product:
                            getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new seller_product()).commit();
                            drawerLayout.close();
                            return true;
                        case R.id.seller_info:
                            getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new sellerInformation()).commit();
                            drawerLayout.close();
                            return true;
                        case R.id.logout:
                            dialog();
                            return true;
                    }
                    return false;
                };

        navigationView.setNavigationItemSelectedListener(itemSelectedListener);

        Map<String, String> postData = new HashMap<>();
        String SID = getSharedPreferences("sharePreferences", MODE_PRIVATE).getString("SID", "null");
        postData.put("SID", SID);
        req http = new req(postData);
        http.execute("http://163.13.201.93/server-side/record.php");
    }
    private void dialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("是否確定要登出?");
        dialog.setPositiveButton("確認", (dialog1, which) -> finish());
        dialog.setNegativeButton("取消", (dialog12, which) -> {});
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected class req extends Http {
        public req(Map<String, String> postData) {
            super(postData);
        }
        @Override
        protected void onPostExecute(String postResult) {
            Log.d("seller", postResult);
            try {
                JSONObject jsonObject = new JSONObject(postResult);
                String rImg = jsonObject.getString("img");
                String rName = jsonObject.getString("name");
                byte[] decodeString = Base64.getDecoder().decode(rImg);
                InputStream inputStream = new ByteArrayInputStream(decodeString);
                bitmap = BitmapFactory.decodeStream(inputStream);
                seller_img.setImageBitmap(bitmap);
                seller_name.setText(rName);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
}