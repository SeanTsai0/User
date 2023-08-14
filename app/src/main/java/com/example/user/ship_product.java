package com.example.user;

import static android.content.Context.MODE_PRIVATE;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONObject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ship_product extends Fragment {
    private static String TAG = "ship_product";
    private final static int UPDATE_COMPLETED_FRAGMENT = 0, HOLD_TEXT = 1, UPDATE_UNCOMPLETED_FRAGMENT = 2;
    private RecyclerView completed_recyclerView, uncompleted_recyclerView;
    private ScrollView completed_scrollView, uncompleted_scrollView;
    private Completed_Recycler_Adapter completed_recycler_adapter;
    private Uncompleted_Recycler_Adapter uncompleted_recycler_adapter;
    private ArrayList<String> mId;
    private ArrayList<String> mData = new ArrayList<>();
    private ArrayList<Bitmap> mImg = new ArrayList<>();
    private ArrayList<String> mDate = new ArrayList<>();
    private ArrayList<String> mAmount = new ArrayList<>();
    private ArrayList<String> mPrice = new ArrayList<>();
    private ArrayList<String> mAddress = new ArrayList<>();
    private ArrayList<String> mStoreName = new ArrayList<>();
    private Button waiting_completeBtn, completedBtn;
    private Map<String, String> postData = new HashMap<>();
    private ProgressBar progressBar;

    public ship_product() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ship_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar = view.findViewById(R.id.progressBar);
        completed_scrollView = view.findViewById(R.id.completed_scrollView);
        uncompleted_scrollView = view.findViewById(R.id.uncompleted_scrollView);
        completed_recyclerView = view.findViewById(R.id.completed_recyclerView);
        uncompleted_recyclerView = view.findViewById(R.id.uncompleted_recyclerView);
        waiting_completeBtn = view.findViewById(R.id.waiting_completeBtn);
        completedBtn = view.findViewById(R.id.completedBtn);

        waiting_completeBtn.setOnClickListener(v -> {
            uncompletedBtnOnClick();
        });
        completedBtn.setOnClickListener(v -> {
            completedBtnOnClick();
        });
        String SID = this.getActivity().getSharedPreferences("sharePreferences", MODE_PRIVATE).getString("SID", null);
        postData.put("SID", SID);
        completedBtnOnClick();

        getParentFragmentManager().setFragmentResultListener("backKey", this, (requestKey, result) -> {
            String tag = result.getString("status");
            Log.d(TAG, "status = " + result.getString("status"));
            switch (tag){
                case "completed":
                    completedBtnOnClick();
                    Log.d(TAG, "tag in switch : " + tag);
                    break;
                case "uncompleted":
                    uncompletedBtnOnClick();
                    Log.d(TAG, "tag in switch : " + tag);
                    break;
            }
        });
    }
    private void uncompletedBtnOnClick(){
        progressBar.setVisibility(View.VISIBLE);
        completed_scrollView.setVisibility(View.GONE);
        waiting_completeBtn.setTextColor(Color.parseColor("#FF5722"));
        completedBtn.setTextColor(Color.parseColor("#000000"));
        SelectUncompletedOrder selectUncompletedOrder = new SelectUncompletedOrder(postData);
        selectUncompletedOrder.execute("http://163.13.201.93/server-side/seller/select_uncomplete_order.php");
    }
    private void completedBtnOnClick(){
        progressBar.setVisibility(View.VISIBLE);
        uncompleted_scrollView.setVisibility(View.GONE);
        completedBtn.setTextColor(Color.parseColor("#FF5722"));
        waiting_completeBtn.setTextColor(Color.parseColor("#000000"));
        SelectCompletedOrder selectCompletedOrder = new SelectCompletedOrder(postData);
        selectCompletedOrder.execute("http://163.13.201.93/server-side/seller/select_complete_order.php");
    }
    private final Handler handler = new Handler(msg -> {
        final int what = msg.what;
        switch (what) {
            case UPDATE_COMPLETED_FRAGMENT:
                UpdateCompletedView();
                break;
            case HOLD_TEXT:
                hold();
                break;
            case UPDATE_UNCOMPLETED_FRAGMENT:
                UpdateUncompletedView();
                break;
        }
        return false;
    });
    private void hold() {

    }
    private void UpdateCompletedView() {
        completed_recycler_adapter = new Completed_Recycler_Adapter(mData);
        completed_recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        completed_recyclerView.setAdapter(completed_recycler_adapter);
        progressBar.setVisibility(View.GONE);
        completed_scrollView.setVisibility(View.VISIBLE);
        uncompleted_scrollView.setVisibility(View.GONE);

    }
    private void UpdateUncompletedView() {
        uncompleted_recycler_adapter = new Uncompleted_Recycler_Adapter(mData);
        uncompleted_recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        progressBar.setVisibility(View.GONE);
        uncompleted_recyclerView.setAdapter(uncompleted_recycler_adapter);
        uncompleted_scrollView.setVisibility(View.VISIBLE);
        completed_scrollView.setVisibility(View.GONE);
    }
    protected class Completed_Recycler_Adapter extends RecyclerView.Adapter<Completed_Recycler_Adapter.ViewHolder> {
        private List<String> mData;

        Completed_Recycler_Adapter(List<String> data) {
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
                    Bundle bundle = new Bundle();
                    bundle.putString("product_id", mId.get(getAdapterPosition()));
                    bundle.putString("tag", "ship_product_completed");
                    getParentFragmentManager().setFragmentResult("requestKey", bundle);

                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.nav_host_fragment, new product_detail(), null)
                            .addToBackStack(null)
                            .commit();
                });
            }
        }

        @Override
        public Completed_Recycler_Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.seller_order_row, parent, false);
            return new Completed_Recycler_Adapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(Completed_Recycler_Adapter.ViewHolder holder, int position) {
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
    protected class Uncompleted_Recycler_Adapter extends RecyclerView.Adapter<Uncompleted_Recycler_Adapter.ViewHolder> {
        private List<String> mData;

        Uncompleted_Recycler_Adapter(List<String> data) {
            mData = data;
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            private TextView seller_unit_order, date, amount, price, total, sender_address, store_name;
            private ImageView product_thumbnail;
            ViewHolder(View itemView) {
                super(itemView);
                seller_unit_order = itemView.findViewById(R.id.seller_unit_order);
                product_thumbnail = itemView.findViewById(R.id.product_thumbnail);
                date = itemView.findViewById(R.id.date);
                amount = itemView.findViewById(R.id.amount);
                price = itemView.findViewById(R.id.price);
                total = itemView.findViewById(R.id.total);
                sender_address = itemView.findViewById(R.id.sender_address);
                store_name = itemView.findViewById(R.id.store_name);

                itemView.setOnClickListener(v -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("product_id", mId.get(getAdapterPosition()));
                    bundle.putString("tag", "ship_product_uncompleted");
                    getParentFragmentManager().setFragmentResult("requestKey", bundle);

                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.nav_host_fragment, new product_detail(), null)
                            .addToBackStack(null)
                            .commit();
                });
            }
        }

        @Override
        public Uncompleted_Recycler_Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.uncompleted_order_row, parent, false);
            return new Uncompleted_Recycler_Adapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(Uncompleted_Recycler_Adapter.ViewHolder holder, int position) {
            holder.seller_unit_order.setText(mData.get(position));
            holder.product_thumbnail.setImageBitmap(mImg.get(position));
            holder.date.setText(mDate.get(position));
            holder.amount.setText(mAmount.get(position));
            holder.price.setText(mPrice.get(position));
            holder.total.setText(Integer.toString(Integer.parseInt(mPrice.get(position))*Integer.parseInt(mAmount.get(position))));
            holder.sender_address.setText(mAddress.get(position));
            holder.store_name.setText(mStoreName.get(position));
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }
    protected class SelectUncompletedOrder extends Http{
        public SelectUncompletedOrder(Map<String, String> postData){
            super(postData);
        }
        @Override
        protected void onPostExecute(String postResult){
            mData = new ArrayList<>();
            mImg = new ArrayList<>();
            mDate = new ArrayList<>();
            mAmount = new ArrayList<>();
            mPrice = new ArrayList<>();
            mAddress = new ArrayList<>();
            mStoreName = new ArrayList<>();
            mId = new ArrayList<>();
            try {
                JSONObject jsonObject = new JSONObject(postResult);
                if (jsonObject.getString("status").equals("1")) {
                    for (int i = 0; i < jsonObject.length()-1; i++) {
                        JSONObject jsonObj = jsonObject.getJSONObject(Integer.toString(i));
                        mData.add(jsonObj.getString("item_name"));
                        byte[] decodeString = Base64.getDecoder().decode(jsonObj.getString("image"));
                        InputStream inputStream = new ByteArrayInputStream(decodeString);
                        mImg.add(BitmapFactory.decodeStream(inputStream));
                        mDate.add(jsonObj.getString("date"));
                        mAmount.add(jsonObj.getString("amount"));
                        mPrice.add(jsonObj.getString("price"));
                        mAddress.add(jsonObj.getString("address"));
                        mStoreName.add(jsonObj.getString("store_name"));
                        mId.add(jsonObj.getString("id"));
                    }
                    handler.sendEmptyMessage(UPDATE_UNCOMPLETED_FRAGMENT);
                } else {
                    handler.sendEmptyMessage(HOLD_TEXT);
                }
            } catch(Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    protected class SelectCompletedOrder extends Http{
        public SelectCompletedOrder(Map<String, String> postData){
            super(postData);
            Log.d(TAG, postData.toString());
        }
        @Override
        protected void onPostExecute(String postResult){
            mData = new ArrayList<>();
            mImg = new ArrayList<>();
            mDate = new ArrayList<>();
            mAmount = new ArrayList<>();
            mPrice = new ArrayList<>();
            mId = new ArrayList<>();
            try {
                JSONObject jsonObject = new JSONObject(postResult);
                if (jsonObject.getString("status").equals("1")) {
                    for (int i = 0; i < jsonObject.length()-1; i++) {
                        JSONObject jsonObj = jsonObject.getJSONObject(Integer.toString(i));
                        mData.add(jsonObj.getString("item_name"));
                        byte[] decodeString = Base64.getDecoder().decode(jsonObj.getString("image"));
                        InputStream inputStream = new ByteArrayInputStream(decodeString);
                        mImg.add(BitmapFactory.decodeStream(inputStream));
                        mDate.add(jsonObj.getString("date"));
                        mAmount.add(jsonObj.getString("amount"));
                        mPrice.add(jsonObj.getString("price"));
                        mId.add((jsonObj.getString("id")));
                    }
                    handler.sendEmptyMessage(UPDATE_COMPLETED_FRAGMENT);
                } else {
                    handler.sendEmptyMessage(HOLD_TEXT);
                }
            } catch(Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}