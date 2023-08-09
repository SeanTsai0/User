package com.example.user;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class seller_product extends Fragment {

    private static String TAG = "seller_product";
    private final static int DO_UPDATE_TEXT = 0, HOLD = 1;
    private TextView product_amount;
    private GridView gridView;
    private GridViewAdapter gridViewAdapter;
    private ArrayList<Bitmap> bitmapArrayList = new ArrayList<>();
    private ArrayList<String> IDArrayList = new ArrayList<>();
    private ArrayList<String> titleArrayList = new ArrayList<>();
    private ConstraintLayout amount_group;
    private ProgressBar progressBar;
    private ScrollView scrollView;

    public seller_product() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Map<String, String> postData = new HashMap<>();
        String SID = this.getActivity().getSharedPreferences("sharePreferences", MODE_PRIVATE).getString("SID", null);
        postData.put("SID", SID);

        getProduct getProduct = new getProduct(postData);
        getProduct.execute("http://163.13.201.93/server-side/seller/select_product.php");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saveInstanceState) {
        super.onViewCreated(view, saveInstanceState);
        gridView = view.findViewById(R.id.gridview);
        product_amount = view.findViewById(R.id.product_amount);
        amount_group = view.findViewById(R.id.amount_group);
        progressBar = view.findViewById(R.id.progress_circular);
        scrollView = view.findViewById(R.id.scrollView);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_seller_product, container, false);
    }

    protected class getProduct extends Http {
        public getProduct(Map<String, String> postData) {
            super(postData);
            Log.d(TAG, postData.toString());
        }

        @Override
        protected void onPostExecute(String postResult) {
            Log.d(TAG, postResult);

            try {
                JSONObject jsonObject = new JSONObject(postResult);
                Log.d(TAG, Integer.toString(jsonObject.length()));
                for (int i = 0; i < jsonObject.length()-1; i++) {
                    JSONObject jsonObj = jsonObject.getJSONObject(Integer.toString(i));
                    Log.d(TAG, "jsonObj in index of " + i + " : " + jsonObj);
                    Log.d(TAG, "img in index of " + i + " : " + jsonObj.getString("img"));

                    byte[] decodeString = Base64.getDecoder().decode(jsonObj.getString("img"));
                    InputStream inputStream = new ByteArrayInputStream(decodeString);
                    bitmapArrayList.add(BitmapFactory.decodeStream(inputStream));
                    IDArrayList.add(jsonObj.getString("id"));
                    titleArrayList.add(jsonObj.getString("item"));
                }
                Log.d(TAG, "size of bitmapArrayList : " + bitmapArrayList.size());
                Log.d(TAG, "id in IDArrayList : " + IDArrayList);
                handler.sendEmptyMessage(DO_UPDATE_TEXT);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private final Handler handler = new Handler(msg -> {
        final int what = msg.what;
        switch (what) {
            case DO_UPDATE_TEXT:
                doUpdate();
                break;
            case HOLD:
                break;
        }
        return false;
    });

    private void doUpdate() {
        gridViewAdapter = new GridViewAdapter(getActivity(), bitmapArrayList);
        gridView.setAdapter(gridViewAdapter);
        product_amount.setText(Integer.toString(bitmapArrayList.size()));
        amount_group.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

    }


    public class GridViewAdapter extends BaseAdapter
    {
        private LayoutInflater mLayoutInflater;
        List<Bitmap> mItemList;

        public GridViewAdapter(Context context,  List<Bitmap> itemList){
            mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mItemList = itemList;
        }

        @Override
        public int getCount() {
            return mItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return mItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            //設定與回傳 convertView 作為顯示在這個 position 位置的 Item 的 View。
            View v = mLayoutInflater.inflate(R.layout.seller_product_gridview, parent, false);

            ImageView product_thumbnail = v.findViewById(R.id.product_thumbnail);
            TextView thumbnail_title = v.findViewById(R.id.thumbnail_title);

            product_thumbnail.setImageBitmap(mItemList.get(position));
            thumbnail_title.setText(titleArrayList.get(position));


            product_thumbnail.setOnClickListener(v1 -> {
                Bundle bundle = new Bundle();
                bundle.putString("product_id", IDArrayList.get(position));
                getParentFragmentManager().setFragmentResult("requestKey", bundle);

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, new product_detail(), null)
                        .addToBackStack(null)
                        .commit();
            });

            return v;
        }
    }
}