package com.example.user;

import static android.content.Context.MODE_PRIVATE;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ship_product extends Fragment {
    private static String TAG = "ship_product";
    private final static int DO_UPDATE_TEXT = 0, HOLD = 1;
    private RecyclerView recyclerView;
    private Recycler_Adapter recycler_adapter;
    private ArrayList<String> mData = new ArrayList<>();
    private ArrayList<Bitmap> mImg = new ArrayList<>();
    private ArrayList<String> mDate = new ArrayList<>();
    private ArrayList<String> mAmount = new ArrayList<>();
    private ArrayList<String> mPrice = new ArrayList<>();
    private ArrayList<HashMap<String, String>> childData = new ArrayList<>();

    public ship_product() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Map<String, String> postData = new HashMap<>();
        String SID = this.getActivity().getSharedPreferences("sharePreferences", MODE_PRIVATE).getString("SID", null);
        postData.put("SID", SID);
        SelectCompletedOrder selectCompletedOrder = new SelectCompletedOrder(postData);
        selectCompletedOrder.execute("http://163.13.201.93/server-side/seller/select_complete_order.php");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ship_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
    }
    private final Handler handler = new Handler(msg -> {
        final int what = msg.what;
        switch (what) {
            case DO_UPDATE_TEXT:
                doUpdate();
                break;
            case HOLD:
                hold();
                break;
        }
        return false;
    });
    private void hold() {

    }

    private void doUpdate() {
        recycler_adapter = new Recycler_Adapter(mData);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(recycler_adapter);
    }
    protected class Recycler_Adapter extends RecyclerView.Adapter<Recycler_Adapter.ViewHolder> {
        private List<String> mData;

        Recycler_Adapter(List<String> data) {
            mData = data;
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            private TextView seller_unit_order, date, amount, price, total;
            private ImageView product_thumbnail;
            ViewHolder(View itemView) {
                super(itemView);
                seller_unit_order = itemView.findViewById(R.id.seller_unit_order);
                product_thumbnail = itemView.findViewById(R.id.product_thumbnail);
                date = itemView.findViewById(R.id.date);
                amount = itemView.findViewById(R.id.amount);
                price = itemView.findViewById(R.id.price);
                total = itemView.findViewById(R.id.total);

                itemView.setOnClickListener(v -> {
                });
            }
        }

        @Override
        public Recycler_Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.seller_order_row, parent, false);
            return new Recycler_Adapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(Recycler_Adapter.ViewHolder holder, int position) {
            holder.seller_unit_order.setText(mData.get(position));
            holder.product_thumbnail.setImageBitmap(mImg.get(position));
            holder.date.setText(mDate.get(position));
            holder.amount.setText(mAmount.get(position));
            holder.price.setText(mPrice.get(position));
            holder.total.setText(Integer.toString(Integer.parseInt(mPrice.get(position))*Integer.parseInt(mAmount.get(position))));
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

    protected class SelectCompletedOrder extends Http{
        public SelectCompletedOrder(Map<String, String> postData){
            super(postData);
            Log.d(TAG, postData.toString());
        }
        @Override
        protected void onPostExecute(String postResult){
            Log.d(TAG, "postResult = " + postResult);
            try {
                JSONObject jsonObject = new JSONObject(postResult);
                if (jsonObject.getString("status").equals("1")) {
                    for (int i = 0; i < jsonObject.length()-1; i++) {
                        HashMap<String, String> info = new HashMap<>();
                        JSONObject jsonObj = jsonObject.getJSONObject(Integer.toString(i));
                        Log.d(TAG, "jsonObj = " + jsonObj);
                        mData.add(jsonObj.getString("item_name"));
                        info.put("item_name", jsonObj.getString("item_name"));
                        byte[] decodeString = Base64.getDecoder().decode(jsonObj.getString("image"));
                        InputStream inputStream = new ByteArrayInputStream(decodeString);
                        mImg.add(BitmapFactory.decodeStream(inputStream));
                        mDate.add(jsonObj.getString("date"));
                        mAmount.add(jsonObj.getString("amount"));
                        mPrice.add(jsonObj.getString("price"));
                        childData.add(info);
                    }
                    for (String i : mData) {
                        Log.d(TAG, "mData = " + i);
                    }
                    for (HashMap i : childData) {
                        Log.d(TAG, "map = " + i);
                    }
                    handler.sendEmptyMessage(DO_UPDATE_TEXT);
                } else {
                    handler.sendEmptyMessage(HOLD);
                }
            } catch(Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}