package com.example.user;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.MultiFormatOneDReader;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class generate extends Fragment {
    private RecyclerView recyclerView;
    private TextView nullData;
    private MyAdapter adapter;
    private ArrayList<String> mData = new ArrayList<>();
    private ArrayList<String> orderInfo = new ArrayList<>();
    private final static int DO_UPDATE_TEXT = 0, HOLD = 1;

    Bitmap bitmap;
    public generate() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Map<String, String> postData = new HashMap<>();
        String SID = this.getActivity().getSharedPreferences("sharePreferences", MODE_PRIVATE).getString("SID", "null");
        postData.put("SID", SID);

        GetOrder getOrder = new GetOrder(postData);
        getOrder.execute("http://172.20.10.2:8080/server-side/generate.php");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_generate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saveInstanceState) {
        super.onViewCreated(view, saveInstanceState);

        nullData = view.findViewById(R.id.nullData);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
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
        nullData.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        adapter = new MyAdapter(mData);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
    }

    public class popupWindow extends PopupWindow implements View.OnClickListener {

        View view;
        Button closeBtn;
        ImageView img;

        public popupWindow(Context context) {
            this.view = LayoutInflater.from(context).inflate(R.layout.show_qrcode, null);
            closeBtn = view.findViewById(R.id.closeBtn);
            this.setOutsideTouchable(false);
            this.setContentView(this.view);
            this.setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
            this.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
            this.setBackgroundDrawable(new ColorDrawable(0xb0000000));

            closeBtn.setOnClickListener(this);
            img = view.findViewById(R.id.code_img);
            img.setImageBitmap(bitmap);
        }

        @Override
        public void onClick(View v) {
            dismiss();
        }
    }

    protected class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private List<String> mData;
        MyAdapter(List<String> data) {
            mData = data;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView order;
            ViewHolder(View itemView) {
                super(itemView);
                order = (TextView) itemView.findViewById(R.id.order);

                itemView.setOnClickListener(v -> {

                    String text = orderInfo.get(getAdapterPosition());
                    MultiFormatWriter writer = new MultiFormatWriter();
                    try {
                        BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, 400, 400);
                        BarcodeEncoder encoder = new BarcodeEncoder();
                        bitmap = encoder.createBitmap(matrix);
                    } catch (Exception e) {
                        Log.d("Debug", e.getMessage());
                    }
                    popupWindow popupWindow = new popupWindow(getActivity());
                    View view = LayoutInflater.from(getActivity()).inflate(R.layout.show_qrcode, null);
                    popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                });
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.order.setText(mData.get(position));
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

    protected class GetOrder extends AsyncTask<String, Void, String> {
        JSONObject postData;

        public GetOrder(Map<String, String> postData) {
            if (postData != null) {
                this.postData = new JSONObject(postData);
            }
        }

        @Override
        protected String doInBackground(String... params) {
            StringBuilder response = new StringBuilder();
            URL url;
            HttpURLConnection urlConnection;

            try {
                url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setUseCaches(false);

                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestMethod("POST");

                if (this.postData != null) {
                    OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                    writer.write(postData.toString());
                    writer.flush();
                }

                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8));
                    String line;
                    try {
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                    } catch(Exception e) {
                        Log.e("Debug", e.getMessage());
                    }
                }
            } catch (Exception e) {
                Log.e("Debug", e.getMessage());
            }
            return response.toString();
        }

        protected void onPostExecute(String postResult) {
            try {
                JSONObject jsonObject = new JSONObject(postResult);
                String status = jsonObject.getString("status");
                if (status.equals("1")) {
                    for (int i = 0; i < jsonObject.length()-1; i++) {
                        JSONObject jsonObj = jsonObject.getJSONObject(Integer.toString(i));
                        mData.add(jsonObj.getString("item_name"));
                        orderInfo.add(jsonObj.getString("order_id"));
                    }
                    handler.sendEmptyMessage(DO_UPDATE_TEXT);
                    Log.d("Debug", "" + mData);
                } else {
                    handler.sendEmptyMessage(HOLD);
                }
            } catch(Exception e) {
                Log.e("Debug", e.getMessage());
            }
        }
    }

}