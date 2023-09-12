package com.example.user;

import static android.content.Context.MODE_PRIVATE;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.widget.NestedScrollView;
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

public class records extends Fragment {
    private static String TAG = "record";
    private final static int DO_UPDATE_TEXT = 0, HOLD = 1;
    private RecyclerView recyclerView;
    private ArrayList<String> mData = new ArrayList<>();
    private ArrayList<HashMap<String, String>> childData = new ArrayList<>();
    private ArrayList<Bitmap> imageList = new ArrayList<>();
    private Recycler_Adapter recycler_adapter;
    private TextView SubTitle;
    private FrameLayout nullGroup;
    private ProgressBar progressBar;
    private NestedScrollView scrollView;
    String videoUrl = "http://163.13.201.93/server-side/seller/item_img/51709F0D-D134-4677-9191-C7DC0B723476-91335-0000ACFBCB09FDB4_AdobeExpress.mp4";

    public records() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Map<String, String> postData = new HashMap<>();
        String SID = this.getActivity().getSharedPreferences("sharePreferences", MODE_PRIVATE).getString("SID", "null");
        postData.put("SID", SID);

        GetOrder getOrder = new GetOrder(postData);
        getOrder.execute("http://163.13.201.93/server-side/order.php");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_records, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saveInstanceState) {
        super.onViewCreated(view, saveInstanceState);
        progressBar = view.findViewById(R.id.progressBar);
        scrollView = view.findViewById(R.id.scrollView);
        nullGroup = view.findViewById(R.id.nullGroup);
        recyclerView = view.findViewById(R.id.recyclerView);
        SubTitle = view.findViewById(R.id.SubTitle);
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
        progressBar.setVisibility(View.GONE);
        nullGroup.setVisibility(View.VISIBLE);
        SubTitle.setVisibility(View.VISIBLE);
    }

    private void doUpdate() {
        progressBar.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        recycler_adapter = new Recycler_Adapter(mData);
        SubTitle.setText("您有" + mData.size() + "筆取貨記錄");
        SubTitle.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(recycler_adapter);
    }

    public class popupWindow extends PopupWindow implements View.OnClickListener {
        View view;
        ImageButton close;
        SwitchCompat switcher;
        VideoView videoView;
        ImageView package_img;

        public popupWindow(Context context) {
            this.view = LayoutInflater.from(context).inflate(R.layout.detail_record, null);

            close = view.findViewById(R.id.close);
            videoView = view.findViewById(R.id.videoView);
            switcher = view.findViewById(R.id.switcher);
            package_img = view.findViewById(R.id.package_img);

            switcher.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    Uri uri = Uri.parse(videoUrl);
                    videoView.setVideoURI(uri); // sets the resource from the videoUrl to the videoView
                    videoView.setOnPreparedListener(mp -> mp.setLooping(true));
                    videoView.start(); // starts the video
                    package_img.setVisibility(View.GONE);
                    videoView.setVisibility(View.VISIBLE);
                } else {
                    package_img.setVisibility(View.VISIBLE);
                    videoView.setVisibility(View.GONE);
                }
            });
            this.setOutsideTouchable(true);
            this.setContentView(this.view);
            this.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
            this.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
            this.setAnimationStyle(R.style.ipopwindow_anim_style);

            close.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            dismiss();
        }
    }

    protected class Recycler_Adapter extends RecyclerView.Adapter<Recycler_Adapter.ViewHolder> {
        private List<String> mData;
        TextView item_name, item_price, arrived_date, pick_up_date, location;
        ImageView package_img;

        Recycler_Adapter(List<String> data) {
            mData = data;
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            private TextView row_index_key;
            private ImageView row_index_image;
            ViewHolder(View itemView) {
                super(itemView);
                row_index_key = itemView.findViewById(R.id.row_index_key);
                row_index_image = itemView.findViewById(R.id.row_index_image);

                itemView.setOnClickListener(v -> {
                    popupWindow popupWindow = new popupWindow(getActivity());
                    View view = LayoutInflater.from(getActivity()).inflate(R.layout.detail_record, null);
                    item_name = popupWindow.view.findViewById(R.id.item_name);
                    item_price = popupWindow.view.findViewById(R.id.item_price);
                    arrived_date = popupWindow.view.findViewById(R.id.arrived_date);
                    pick_up_date = popupWindow.view.findViewById(R.id.pick_up_date);
                    location = popupWindow.view.findViewById(R.id.location);
                    package_img = popupWindow.view.findViewById(R.id.package_img);

                    item_name.setText(childData.get(getAdapterPosition()).get("item_name"));
                    item_price.setText(childData.get(getAdapterPosition()).get("item_price"));
                    arrived_date.setText(childData.get(getAdapterPosition()).get("arrived_date"));
                    pick_up_date.setText(childData.get(getAdapterPosition()).get("pick_up_date"));
                    location.setText(childData.get(getAdapterPosition()).get("pick_up_location"));
                    package_img.setImageBitmap(imageList.get(getAdapterPosition()));

                    popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 50);
                });
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.row_index_key.setText(mData.get(position));
            holder.row_index_image.setImageBitmap(imageList.get(position));
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

    protected class GetOrder extends Http {
        public GetOrder(Map<String, String> postData) {
            super(postData);
        }
        @Override
        protected void onPostExecute(String postResult) {
            Log.d("result", postResult);
            try {
                JSONObject jsonObject = new JSONObject(postResult);
                String status = jsonObject.getString("status");
                if (status.equals("1")) {
                    for (int i = 0; i < jsonObject.length()-1; i++) {
                        HashMap<String, String> info = new HashMap<>();
                        JSONObject jsonObj = jsonObject.getJSONObject(Integer.toString(i));
                        Log.d("result", "jsonObj = " + jsonObj);
                        mData.add(jsonObj.getString("item_name"));
                        byte[] decodeString = Base64.getDecoder().decode(jsonObj.getString("img"));
                        InputStream inputStream = new ByteArrayInputStream(decodeString);
                        imageList.add(BitmapFactory.decodeStream(inputStream));
                        info.put("item_name", jsonObj.getString("item_name"));
                        info.put("item_price", jsonObj.getString("item_price"));
                        info.put("arrived_date", jsonObj.getString("arrived_date"));
                        info.put("pick_up_date", jsonObj.getString("pick_up_date"));
                        info.put("pick_up_location", jsonObj.getString("pick_up_location"));
                        childData.add(info);
                    }
                    for (String i : mData) {
                        Log.d("result", "mData = " + i);
                    }
                    for (HashMap i : childData) {
                        Log.d("result", "map = " + i);
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