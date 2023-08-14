package com.example.user;

import static android.content.Context.MODE_PRIVATE;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class sellerInformation extends Fragment {
    private static String TAG = "sellerInformation";
    private ImageView seller_profile_image;
    private TextView seller_profile_name, seller_profile_product_amount,
            seller_profile_phone, seller_profile_email, seller_profile_birth,
            seller_profile_trade_amount, total_profit, seller_profile_shipping_product;
    private Button modify_seller_informationBtn;
    private ScrollView seller_information_scrollView;
    private ProgressBar progressBar;
    public sellerInformation() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Map<String, String> postData = new HashMap<>();
        String SID = this.getActivity().getSharedPreferences("sharePreferences", MODE_PRIVATE).getString("SID", null);
        postData.put("SID", SID);

        GetSellerInformation getSellerInformation = new GetSellerInformation(postData);
        getSellerInformation.execute("http://163.13.201.93/server-side/seller/sellerInformation.php");
    }

    private void component(View view) {
        progressBar = view.findViewById(R.id.progressBar);
        seller_information_scrollView = view.findViewById(R.id.seller_information_scrollView);
        seller_profile_image = view.findViewById(R.id.seller_profile_image);
        seller_profile_name = view.findViewById(R.id.seller_profile_name);
        seller_profile_product_amount = view.findViewById(R.id.seller_profile_product_amount);
        seller_profile_phone = view.findViewById(R.id.seller_profile_phone);
        seller_profile_email = view.findViewById(R.id.seller_profile_email);
        seller_profile_birth = view.findViewById(R.id.seller_profile_birth);
        seller_profile_trade_amount = view.findViewById(R.id.seller_profile_trade_amount);
        total_profit = view.findViewById(R.id.total_profit);
        seller_profile_shipping_product = view.findViewById(R.id.seller_profile_shipping_product);
        modify_seller_informationBtn = view.findViewById(R.id.modify_seller_informationBtn);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saveInstanceState){
        super.onViewCreated(view, saveInstanceState);
        component(view);
        modify_seller_informationBtn.setOnClickListener(v -> {

            Intent intent = new Intent(getActivity(), modify_sellerInformation.class);
            Bundle bundle = new Bundle();
            bundle.putString("seller_name" ,seller_profile_name.getText().toString());

            Bitmap imageBitmap = ((BitmapDrawable) seller_profile_image.getDrawable()).getBitmap();
            bundle.putParcelable("seller_image", imageBitmap);
            bundle.putString("seller_phone", seller_profile_phone.getText().toString());
            bundle.putString("seller_email", seller_profile_email.getText().toString());
            bundle.putString("seller_birth", seller_profile_birth.getText().toString());

            intent.putExtras(bundle);
            startActivity(intent);
        });
    }
    protected class GetSellerInformation extends Http{
        public GetSellerInformation(Map<String, String> postData){
            super(postData);
        }

        @Override
        protected void onPostExecute(String postResult) {
            Log.d(TAG, postResult);
            try {
                JSONObject jsonObject = new JSONObject(postResult);

                byte[] decodeString = Base64.getDecoder().decode(jsonObject.getString("img"));
                InputStream inputStream = new ByteArrayInputStream(decodeString);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                seller_profile_image.setImageBitmap(bitmap);
                seller_profile_name.setText(jsonObject.getString("name"));
                seller_profile_product_amount.setText(jsonObject.getString("amount"));
                seller_profile_phone.setText(jsonObject.getString("phone"));
                seller_profile_email.setText(jsonObject.getString("email"));
                seller_profile_birth.setText(jsonObject.getString("birth"));
                seller_profile_trade_amount.setText(jsonObject.getString("order_amount"));
                total_profit.setText(jsonObject.getString("total"));
                seller_profile_shipping_product.setText(jsonObject.getString("shipping"));
                progressBar.setVisibility(View.GONE);
                seller_information_scrollView.setVisibility(View.VISIBLE);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_seller_information, container, false);
    }
}