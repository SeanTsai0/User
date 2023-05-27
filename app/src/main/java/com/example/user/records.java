package com.example.user;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
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

public class records extends Fragment {
    private static String TAG = "record";
    private ListAdapter adapter;
    private static ExpandableListView expandableListViewContainer;
    private static TextView nullData;
    private HashMap<String, ArrayList> mainArray = new HashMap<>(); // main array
    private ArrayList<String> parentName = new ArrayList<>(); // parent title
    private ArrayList<HashMap<String, String>> childArray = new ArrayList<>(); // sub array
    private final static int DO_UPDATE_TEXT = 0, HOLD = 1;
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

    public records() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Map<String, String> postData = new HashMap<>();
        String SID = this.getActivity().getSharedPreferences("sharePreferences", MODE_PRIVATE).getString("SID", "null");
        postData.put("SID", SID);

        GetOrder getOrder = new GetOrder(postData);
        getOrder.execute("http://172.20.10.2:8080/server-side/order.php");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_records, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saveInstanceState) {
        super.onViewCreated(view, saveInstanceState);
        expandableListViewContainer = view.findViewById(R.id.expandableListViewContainer);
        nullData = view.findViewById(R.id.nullData);
    }
    private void doUpdate() {
        //expandableListViewContainer = expandableListViewContainer.findViewById(R.id.expandableListViewContainer);
        nullData.setVisibility(View.GONE);
        expandableListViewContainer.setVisibility(View.VISIBLE);
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
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.expandable_parent, null);
            }
            convertView.setTag(R.layout.expandable_parent, groupPosition);
            convertView.setTag(R.layout.expandable_parent, -1);
            TextView textView = convertView.findViewById(R.id.expandableListViewMainTitle);
            textView.setText(parentName.get(groupPosition));
            //ImageView expandIcon = findViewById(R.id.expandIcon);

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                           Log.e(TAG, e.getMessage());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            return response.toString();
        }

        protected void onPostExecute(String postResult) {
            try {
                Log.d(TAG, "postResult is " + postResult);
                JSONObject jsonObject = new JSONObject(postResult);
                String status = jsonObject.getString("status");
                if (status.equals("1")) {
                    Log.d(TAG, "jsonObject.length = " + jsonObject.length());
                    for (int i = 0; i < jsonObject.length()-1; i++) {
                        ArrayList<HashMap<String, String>> subArray = new ArrayList<>(); // sub array
                        JSONObject jsonObj = jsonObject.getJSONObject(Integer.toString(i));
                        Iterator<String> iterator = jsonObj.keys();
                        while (iterator.hasNext()) {
                            HashMap<String, String> childName = new HashMap<>();
                            String key = iterator.next();
                            String subTitle = "無";
                            switch (key) {
                                case "item_name" :
                                    subTitle = "產品名稱";
                                    break;
                                case "item_price" :
                                    subTitle = "金額";
                                    break;
                                case "arrived_date" :
                                    subTitle = "抵達日期";
                                    break;
                                case "pick_up_date" :
                                    subTitle = "取貨日期";
                                    break;
                                case "pick_up_location" :
                                    subTitle = "取貨門市";
                                    break;
                            }
                            childName.put("subTitle", subTitle);
                            childName.put("subContent", jsonObj.getString(key));
                            subArray.add(childName);
                            Log.d(TAG, "child array in " + i + " step is " + subArray);
                            childArray.add(childName);
                        }
                        Log.d(TAG, "childArray is " + childArray);
                        parentName.add(i, jsonObj.getString("item_name"));
                        mainArray.put(parentName.get(i), subArray);
                    }
                    Log.d(TAG, "main array is " + mainArray);
                    handler.sendEmptyMessage(DO_UPDATE_TEXT);
                } else {
                    handler.sendEmptyMessage(HOLD);
                }
            } catch(Exception e) {
                Log.e(TAG, e.getMessage() + " at line:245");
            }
        }
    }
}