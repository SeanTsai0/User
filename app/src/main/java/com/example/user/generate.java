package com.example.user;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public class generate extends Fragment {
    private RecyclerView recyclerView;
    private TextView SubTitle;
    private FrameLayout nullGroup;
    private MyAdapter adapter;
    private ArrayList<String> mData = new ArrayList<>();
    private ArrayList<String> locationInfo = new ArrayList<>();
    private ArrayList<Bitmap> mImg = new ArrayList<>();
    private ArrayList<String> mDate = new ArrayList<>();
    private ArrayList<String> mAddress = new ArrayList<>();
    private ArrayList<String> mPrice = new ArrayList<>();
    private ArrayList<String> mAmount = new ArrayList<>();
    private final static int DO_UPDATE_TEXT = 0, HOLD = 1;
    Bitmap bitmap;
    public generate() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Map<String, String> postData = new HashMap<>();
        String SID = this.getActivity().getSharedPreferences("sharePreferences", MODE_PRIVATE).getString("SID", "null");
        postData.put("SID", SID);

        GetOrder getOrder = new GetOrder(postData);
        getOrder.execute("http://163.13.201.93/server-side/generate.php");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_generate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saveInstanceState) {
        super.onViewCreated(view, saveInstanceState);
        nullGroup = view.findViewById(R.id.nullGroup);
        recyclerView = view.findViewById(R.id.recyclerView);
        SubTitle = view.findViewById(R.id.SubTitle);
    }
    private final Handler handler = new Handler(msg -> {
        final int what = msg.what;
        Log.d("what", Integer.toString(what));
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
        nullGroup.setVisibility(View.VISIBLE);
        //nullData.setVisibility(View.VISIBLE);
    }

    private void doUpdate() {
        recyclerView.setVisibility(View.VISIBLE);
        adapter = new MyAdapter(mData);
        SubTitle.setText("目前有" + mData.size() + "件包裹準備領取");
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
    }

    public class popupWindow extends PopupWindow implements View.OnClickListener {
        View view;
        ImageButton closeBtn;
        ImageView img;
        TextView time_counter;
        CountDownTimer countDownTimer = new CountDownTimer(1000*300, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                time_counter.setText(Long.toString(millisUntilFinished/1000));
            }

            @Override
            public void onFinish() {
                dismiss();
                new AlertDialog.Builder(view.getContext())
                        .setMessage("您的取貨條碼已經過期，請重新產生取貨QR CODE").show();
            }
        };

        public popupWindow(Context context) {
            this.view = LayoutInflater.from(context).inflate(R.layout.show_qrcode, null);
            closeBtn = view.findViewById(R.id.closeBtn);
            time_counter = view.findViewById(R.id.time_counter);

            this.setOutsideTouchable(false);
            this.setContentView(this.view);
            this.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
            this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
            this.setBackgroundDrawable(new ColorDrawable(0xb0000000));

            closeBtn.setOnClickListener(this);
            img = view.findViewById(R.id.code_img);
            img.setImageBitmap(bitmap);
            countDownTimer.start();
        }

        @Override
        public void onClick(View v) {
            dismiss();
            countDownTimer.cancel();
        }
    }

    protected class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private List<String> mData;
        MyAdapter(List<String> data) {
            mData = data;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView product_name, arrived_date, product_address, product_price, product_amount, total_price;
            private ImageView product_img;
            private Button generateBtn;

            ViewHolder(View itemView) {
                super(itemView);
                product_name = itemView.findViewById(R.id.product_name);
                product_img = itemView.findViewById(R.id.product_img);
                generateBtn = itemView.findViewById(R.id.generateBtn);
                arrived_date = itemView.findViewById(R.id.arrived_date);
                product_address = itemView.findViewById(R.id.product_address);
                product_price = itemView.findViewById(R.id.product_price);
                product_amount = itemView.findViewById(R.id.product_amount);
                total_price = itemView.findViewById(R.id.total_price);

                generateBtn.setOnClickListener(v -> {

                    String text = "location : " + locationInfo.get(getAdapterPosition()) + ", time : " + java.time.LocalDateTime.now();
                    MultiFormatWriter writer = new MultiFormatWriter();
                    try {
                        BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, 700, 700);
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

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.product_name.setText(mData.get(position));
            holder.product_img.setImageBitmap(mImg.get(position));
            holder.arrived_date.setText(mDate.get(position));
            holder.product_address.setText(mAddress.get(position));
            holder.product_price.setText(mPrice.get(position));
            holder.product_amount.setText(mAmount.get(position));
            holder.total_price.setText(Integer.toString(Integer.parseInt(mPrice.get(position)) * Integer.parseInt(mAmount.get(position))));
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
                Log.d("postRes", postResult);
                JSONObject jsonObject = new JSONObject(postResult);
                String status = jsonObject.getString("status");
                if (status.equals("1")) {
                    for (int i = 0; i < jsonObject.length()-1; i++) {
                        JSONObject jsonObj = jsonObject.getJSONObject(Integer.toString(i));
                        mData.add(jsonObj.getString("item_name"));
                        locationInfo.add(jsonObj.getString("location_id"));
                        byte[] decodeString = Base64.getDecoder().decode(jsonObj.getString("img"));
                        InputStream inputStream = new ByteArrayInputStream(decodeString);
                        mImg.add(BitmapFactory.decodeStream(inputStream));
                        mDate.add(jsonObj.getString("date"));
                        mAddress.add(jsonObj.getString("address"));
                        mPrice.add(jsonObj.getString("item_price"));
                        mAmount.add(jsonObj.getString("amount"));
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