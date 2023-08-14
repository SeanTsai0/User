package com.example.user;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class modify_sellerInformation extends AppCompatActivity{
    private static String TAG = "modify_sellerInformation";
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 101;
    ImageView modify_seller_image;
    EditText modify_seller_name, modify_seller_phone, modify_seller_email;
    Calendar calendar;
    TextView modify_seller_birth, saveBtn, backBtn;


    private void component(){
        modify_seller_name = findViewById(R.id.modify_seller_name);
        backBtn = findViewById(R.id.backBtn);
        saveBtn = findViewById(R.id.saveBtn);
        modify_seller_image = findViewById(R.id.modify_seller_image);
        modify_seller_phone = findViewById(R.id.modify_seller_phone);
        modify_seller_email = findViewById(R.id.modify_seller_email);
        modify_seller_birth = findViewById(R.id.modify_seller_birth);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_seller_information);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        component();
        DatePickerDialog.OnDateSetListener datePicker;
        calendar = Calendar.getInstance();

        backBtn.setOnClickListener(v -> {
            finish();
        });
        modify_seller_image.setOnClickListener(v -> {
            if(checkAndRequestPermissions(this)){
                chooseImage(this);
            }
        });
        datePicker = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            modify_seller_birth.setText(year + "-" + (month+1) +  "-" + dayOfMonth);
        };
        modify_seller_birth.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(modify_sellerInformation.this,
                    datePicker, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });
        saveBtn.setOnClickListener(v -> {
            if(TextUtils.isEmpty(modify_seller_name.getText())){
                modify_seller_name.requestFocus();
                modify_seller_name.setError("使用者名稱不能為空");
            } else if(TextUtils.isEmpty(modify_seller_phone.getText())){
                modify_seller_phone.requestFocus();
                modify_seller_phone.setError("電話欄位不能為空");
            } else if(TextUtils.isEmpty(modify_seller_email.getText())){
                modify_seller_email.requestFocus();
                modify_seller_email.setError("電子郵件不能為空");
            } else{
                Map<String, String> postData = new HashMap<>();
                Bitmap imageBitmap = ((BitmapDrawable) modify_seller_image.getDrawable()).getBitmap();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] bytes = byteArrayOutputStream.toByteArray();
                String base64String = Base64.encodeToString(bytes, Base64.NO_CLOSE);
                postData.put("image", base64String);
                postData.put("name", modify_seller_name.getText().toString());
                postData.put("phone", modify_seller_phone.getText().toString());
                postData.put("email", modify_seller_email.getText().toString());
                postData.put("birth", modify_seller_birth.getText().toString());
                String SID = getSharedPreferences("sharePreferences", MODE_PRIVATE).getString("SID", "null");
                postData.put("SID", SID);
                UpdateSeller updateSeller = new UpdateSeller(postData);
                updateSeller.execute("http://163.13.201.93/server-side/seller/updateSeller.php");
            }
        });

        Bundle bundle = getIntent().getExtras();
        modify_seller_name.setText(bundle.getString("seller_name"));
        modify_seller_image.setImageBitmap(bundle.getParcelable("seller_image"));
        modify_seller_phone.setText(bundle.getString("seller_phone"));
        modify_seller_email.setText(bundle.getString("seller_email"));
        modify_seller_birth.setText(bundle.getString("seller_birth"));
    }
    protected class UpdateSeller extends Http{
        public UpdateSeller(Map<String, String> postData){super(postData);}
        @Override
        protected void onPostExecute(String postResult){
            Log.d(TAG, postResult);
            try {
                JSONObject jsonObject = new JSONObject(postResult);
                String status = jsonObject.getString("status");
                Log.d(TAG, postResult);
                if (status.equals("1")) {
                    Toast.makeText(modify_sellerInformation.this, "修改成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(modify_sellerInformation.this, SellerInterface.class);
                    startActivity(intent);
                } else if (status.equals("-1")){
                    Toast.makeText(modify_sellerInformation.this, "修改失敗", Toast.LENGTH_SHORT).show();
                } else if (status.equals("0")) {
                    Toast.makeText(modify_sellerInformation.this, "此電話已被使用", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static boolean checkAndRequestPermissions(final Activity context) {
        int WExtstorePermission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int cameraPermission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.CAMERA);
        }
        if (WExtstorePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(context, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    private void chooseImage(Context context){
        final CharSequence[] optionsMenu = {"Take Photo", "Choose from Gallery", "Exit" }; // create a menuOption Array
        // create a dialog for showing the optionsMenu
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // set the items in builder
        builder.setItems(optionsMenu, (dialogInterface, i) -> {
            if(optionsMenu[i].equals("Take Photo")){
                // Open the camera and get the photo
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, 0);
            }
            else if(optionsMenu[i].equals("Choose from Gallery")){
                // choose from  external storage
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , 1);
            }
            else if (optionsMenu[i].equals("Exit")) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS:
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),"FlagUp Requires Access to Camara.", Toast.LENGTH_SHORT).show();
                } else {
                    chooseImage(this);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        modify_seller_image.setImageBitmap(selectedImage);
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        if (selectedImage != null) {
                            modify_seller_image.setImageURI(selectedImage);
                        }
                    }
                    break;
            }
        }
    }
}