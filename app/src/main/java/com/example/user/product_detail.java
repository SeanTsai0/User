package com.example.user;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
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

public class product_detail extends Fragment {

    private static String TAG = "product_detail";
    private ImageView closeFragment;
    private TextView product_name, product_price, sub_product_name, sub_product_price, product_scale, packing_type, product_detail;
    private ImageView product_picture;
    private String tag;
    private ScrollView scrollView;
    private ProgressBar progressBar;


    public product_detail() {
        // Required empty public constructor
    }

    private void component(View view) {
        progressBar = view.findViewById(R.id.progressBar);
        scrollView = view.findViewById(R.id.scrollView);
        closeFragment = view.findViewById(R.id.closeFragment);
        product_picture = view.findViewById(R.id.product_picture);
        product_name = view.findViewById(R.id.product_name);
        sub_product_name = view.findViewById(R.id.sub_product_name);
        product_price = view.findViewById(R.id.product_price);
        sub_product_price = view.findViewById(R.id.sub_product_price);
        product_scale = view.findViewById(R.id.product_scale);
        packing_type = view.findViewById(R.id.packing_type);
        product_detail = view.findViewById(R.id.product_detail);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getParentFragmentManager().setFragmentResultListener("requestKey", this, (requestKey, result) -> {
            String bundleKey = result.getString("product_id");
            tag = result.getString("tag");

            Map<String, String> postData = new HashMap<>();
            postData.put("product_id", bundleKey);
            getDetailProduct getDetailProduct = new getDetailProduct(postData);
            getDetailProduct.execute("http://163.13.201.93/server-side/seller/getDetailProduct.php");
            // This callback will only be called when MyFragment is at least Started.
            OnBackPressedCallback callback = new OnBackPressedCallback(false /* enabled by default */) {
                @Override
                public void handleOnBackPressed() {
                    // Handle the back button event
                    getFragmentManager().popBackStack();
                    Bundle bundle;
                    switch (tag){
                        case "ship_product_completed":
                            bundle = new Bundle();
                            bundle.putString("status", "completed");
                            getParentFragmentManager().setFragmentResult("backKey", bundle);
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new ship_product()).commit();
                            break;
                        case "ship_product_uncompleted":
                            bundle = new Bundle();
                            bundle.putString("status", "uncompleted");
                            getParentFragmentManager().setFragmentResult("backKey", bundle);
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new ship_product()).commit();
                            break;
                        case "seller_product":
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new seller_product()).commit();
                            break;
                    }
                }
            };
            requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
            callback.setEnabled(true);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_product_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saveInstanceState) {
        super.onViewCreated(view, saveInstanceState);
        component(view);

        closeFragment.setOnClickListener(v -> {
            getFragmentManager().popBackStack();
            Bundle bundle;
            switch (tag){
                case "ship_product_uncompleted":
                    bundle = new Bundle();
                    bundle.putString("status", "uncompleted");
                    getParentFragmentManager().setFragmentResult("backKey", bundle);
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new ship_product()).commit();
                    Log.d(TAG, bundle.toString());
                    break;
                case "ship_product_completed":
                    bundle = new Bundle();
                    bundle.putString("status", "completed");
                    getParentFragmentManager().setFragmentResult("backKey", bundle);
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new ship_product()).commit();
                    Log.d(TAG, bundle.toString());
                    break;
                case "seller_product":
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new seller_product()).commit();
                    break;
            }
        });
    }

    protected class getDetailProduct extends Http {
        public getDetailProduct(Map<String, String> postData){super(postData);}

        @Override
        protected void onPostExecute(String postResult) {
            try {
                JSONObject jsonObject = new JSONObject(postResult);

                Log.d(TAG, "item_name : " + jsonObject.getString("item_name"));
                Log.d(TAG, "price : " + jsonObject.getString("price"));
                Log.d(TAG, "size : " + jsonObject.getString("size"));
                Log.d(TAG, "packaging_id : " + jsonObject.getString("packaging_id"));
                Log.d(TAG, "img : " + jsonObject.getString("img"));


                byte[] decodeString = Base64.getDecoder().decode(jsonObject.getString("img"));
                InputStream inputStream = new ByteArrayInputStream(decodeString);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                product_picture.setImageBitmap(bitmap);
                product_name.setText(jsonObject.getString("item_name"));
                sub_product_name.setText(jsonObject.getString("item_name"));
                product_price.setText(jsonObject.getString("price"));
                sub_product_price.setText(jsonObject.getString("price"));
                if (jsonObject.getString("size").equals("1"))
                    product_scale.setText("大型貨件");
                else if (jsonObject.getString("size").equals("2"))
                    product_scale.setText("小型貨件");
                else
                    product_scale.setText("未分類");
                if (jsonObject.getString("packaging_id").equals("1"))
                    packing_type.setText("紙箱");
                else if (jsonObject.getString("packaging_id").equals("2"))
                    packing_type.setText("破壞袋");
                else
                    packing_type.setText("未分類");
                product_detail.setText(jsonObject.getString("about"));
                scrollView.setVisibility(View.VISIBLE);
                closeFragment.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
}