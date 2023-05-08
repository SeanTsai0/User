package com.example.user;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Record extends AppCompatActivity {
    private ListAdapter adapter;
    private static ExpandableListView expandableListViewContainer;
    private HashMap<String, ArrayList> mainArray = new HashMap<>(); // main array
    private ArrayList<String> parentName = new ArrayList<>(); // parent title
    private ArrayList<HashMap<String, String>> childArray = new ArrayList<>(); // sub array
    private final static int DO_UPDATE_TEXT = 0;
    private final Handler handler = new Handler(msg -> {
        final int what = msg.what;
        if (what == DO_UPDATE_TEXT) {
            doUpdate();
        }
        return false;
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        ImageButton back_btn = findViewById(R.id.back_btn);

        Map<String, String> postData = new HashMap<>();
        String SID = getSharedPreferences("sharePreferences", MODE_PRIVATE).getString("SID", "null");
        postData.put("SID", SID);

        GetOrder getOrder = new GetOrder(postData);
        getOrder.execute("http://172.20.10.2:8080/server-side/order.php");

        back_btn.setOnClickListener(v -> finish());
    }
    private void doUpdate() {
        expandableListViewContainer = findViewById(R.id.expandableListViewContainer);
        adapter = new ListAdapter();
        expandableListViewContainer.setAdapter(adapter);
    }
    private class ListAdapter extends BaseExpandableListAdapter {
        @Override
        public int getGroupCount() {
            return mainArray.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return childArray.size()/mainArray.size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return null;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) Record.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.expandable_parent, null);
            }
            convertView.setTag(R.layout.expandable_parent, groupPosition);
            convertView.setTag(R.layout.expandable_parent, -1);
            TextView textView = convertView.findViewById(R.id.expandableListViewMainTitle);
            textView.setText(parentName.get(groupPosition));
            ImageView expandIcon = findViewById(R.id.expandIcon);

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) Record.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.record_list, null);
                }
            convertView.setTag(R.layout.record_list, groupPosition);
            convertView.setTag(R.layout.record_list, -1);
            TextView child1 = convertView.findViewById(R.id.expandableListViewSubTitle);
            TextView child2 = convertView.findViewById(R.id.expandableListViewSubContent);
            ArrayList<HashMap<String, String>> arr = mainArray.get(parentName.get(groupPosition));
            child1.setText(arr.get(childPosition).get("subTitle"));
            child2.setText(arr.get(childPosition).get("subContent"));
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
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
                JSONArray jsonArray = new JSONArray(postResult);

                for(int i = 0; i < jsonArray.length(); i++) {
                    ArrayList<HashMap<String, String>> subArray = new ArrayList<>(); // sub array
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Iterator<String> iterator = jsonObject.keys();
                    while (iterator.hasNext()) {
                        HashMap<String, String> childName = new HashMap<>();
                        String key = iterator.next();
                        childName.put("subTitle", key);
                        childName.put("subContent", jsonObject.getString(key));
                        subArray.add(childName);
                        Log.d("Debug", "child array in " + i + "step is " + subArray);
                        childArray.add(childName);
                    }
                    parentName.add(i, jsonArray.getJSONObject(i).getString("item_name"));
                    mainArray.put(parentName.get(i), subArray);
                }

                Log.d("Debug", "main array is " + mainArray);
                handler.sendEmptyMessage(DO_UPDATE_TEXT);

            } catch(Exception e) {
                Log.e("Debug", e.getMessage());
            }
        }
    }
}