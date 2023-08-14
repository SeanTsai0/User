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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Register extends AppCompatActivity {
    private static String TAG = "Register";
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 101;
    static ImageView photo;
    Calendar calendar;
    String signup_type = "1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        DatePickerDialog.OnDateSetListener datePicker;
        calendar = Calendar.getInstance();
        EditText nameField = findViewById(R.id.NameField);
        EditText phoneField = findViewById(R.id.phoneField);
        EditText passwordField = findViewById(R.id.passwordField);
        EditText confirmPasswordField = findViewById(R.id.confirmPasswordField);
        EditText emailField = findViewById(R.id.emailField);
        TextView birthField = findViewById(R.id.birthField);
        Button registerBtn = findViewById(R.id.registerBtn);
        ImageButton arrow_back = findViewById(R.id.arrow_back);
        TextView pickerBtn = findViewById(R.id.pickerBtn);
        TextView loginBtn = findViewById(R.id.LogInBtn);
        photo = findViewById(R.id.photo);
        RadioGroup IdentifyGroup = findViewById(R.id.IdentifyGroup);

        arrow_back.setOnClickListener(v -> finish());

        loginBtn.setOnClickListener(v -> finish());

        pickerBtn.setOnClickListener(v -> {
             if(checkAndRequestPermissions(this)){
                chooseImage(this);
             }
        });

        IdentifyGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId == R.id.buyerRadioBtn) {
                signup_type = "1";
            } else {
                signup_type = "2";
            }
        });

        datePicker = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            birthField.setText(year + "-" + (month+1) +  "-" + dayOfMonth);
        };

        birthField.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(Register.this,
                    datePicker, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });

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
            } else if (TextUtils.isEmpty(emailField.getText())) {
                emailField.requestFocus();
                emailField.setError("Field Cannot Be Empty");
            } else if (TextUtils.isEmpty(birthField.getText())) {
                birthField.requestFocus();
                birthField.setError("Field Cannot Be Empty");
            }
            else {
                String name = nameField.getText().toString();
                String phone = phoneField.getText().toString();
                String password = passwordField.getText().toString();
                String email = emailField.getText().toString();
                String birth = birthField.getText().toString();
                Map<String, String> postData = new HashMap<>();
                postData.put("name", name);
                postData.put("phone", phone);
                postData.put("password", password);
                postData.put("email", email);
                postData.put("birth", birth);
                postData.put("identify", signup_type);

                Bitmap imageBitmap = ((BitmapDrawable) photo.getDrawable()).getBitmap();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] bytes = byteArrayOutputStream.toByteArray();
                String base64String = Base64.encodeToString(bytes, Base64.NO_CLOSE);
                postData.put("img", base64String);

                Log.d("base64", "sign up button");
                SignUpAccount sign = new SignUpAccount(postData);
                sign.execute("http://163.13.201.93/server-side/insert.php");
            }
        });
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
                        photo.setImageBitmap(selectedImage);
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        if (selectedImage != null) {
                            photo.setImageURI(selectedImage);
                        }
                    }
                    break;
            }
        }
    }
    protected class SignUpAccount extends Http{
        public SignUpAccount(Map<String, String> postData){super(postData);}

        @Override
        protected void onPostExecute(String postResult) {
            Log.d("base64", "post result = " + postResult);
            try {
                JSONObject jsonObject = new JSONObject(postResult);
                String status = jsonObject.getString("status");
                Log.d(TAG, postResult);
                if (status.equals("1")) {
                    Toast.makeText(Register.this, "註冊成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Register.this, MainActivity.class);
                    startActivity(intent);
                } else if (status.equals("-1")){
                    Toast.makeText(Register.this, "註冊失敗", Toast.LENGTH_SHORT).show();
                } else if (status.equals("0")) {
                    Toast.makeText(Register.this, "使用者已存在", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}