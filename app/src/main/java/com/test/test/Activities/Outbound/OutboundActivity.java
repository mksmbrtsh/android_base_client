package com.test.test.Activities.Outbound;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bosphere.filelogger.FL;
import com.fxn.stash.Stash;
import com.test.test.Activities.Inbound.ListActivity;
import com.test.test.Models.OutboundListModel;
import com.test.test.R;
import com.test.test.Repository.DataRepo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OutboundActivity extends AppCompatActivity {

    private ListView mOutboundListView;
    private ProgressBar mProgressBar;
    private AutoCompleteTextView mOutboundAutocomplete;
    private DataRepo.getData mDataRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outbound);
        mOutboundListView = findViewById(R.id.outbound_listview);
        mProgressBar = findViewById(R.id.progress);
        mOutboundAutocomplete = findViewById(R.id.outbound_autocomplete);

        mDataRepo = new DataRepo.getData(new DataRepo.onDataListener() {
            @Override
            public void returnData(String data) {
                mProgressBar.setVisibility(View.GONE);
                try {
                    JSONObject object = new JSONObject(data);
                    JSONArray array = object.getJSONArray("rows");
                    final List<OutboundListModel> outboundList = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++){
                        JSONObject p = array.getJSONObject(i);
                        outboundList.add(new OutboundListModel(p.getInt("id"),
                                p.getString("OutboundShipmentNumber"),
                                p.getString("Items"),
                                p.getString("Supplier"),
                                p.getString("Client"),
                                p.getInt("status_id"),
                                p.getInt("task_count")));
                    }
                    ArrayAdapter<OutboundListModel> adapter = new ArrayAdapter<>(OutboundActivity.this,
                            android.R.layout.simple_list_item_1, outboundList);
                    ArrayAdapter<OutboundListModel> adapterAutocomplete = new ArrayAdapter<>(OutboundActivity.this,
                            android.R.layout.simple_dropdown_item_1line, outboundList);
                    mOutboundAutocomplete.setAdapter(adapterAutocomplete);
                    mOutboundAutocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                            String selected = (String) adapterView.getItemAtPosition(position);
                            int pos = outboundList.indexOf(selected);
                            startItem(outboundList, pos);
                        }
                    });
                    mOutboundListView.setAdapter(adapter);
                    mOutboundListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            startItem(outboundList, i);
                        }
                    });
                } catch (JSONException e) {
                    if(Stash.getBoolean("logger")) {
                        FL.d("Error parse list: " + e.toString());
                    }
                    Toast.makeText(OutboundActivity.this, "Error parse list: " + e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        mDataRepo.getOutboundList();
        mDataRepo.start();
    }

    void startItem(List<OutboundListModel> outboundList, int id){
        Intent intent = new Intent(OutboundActivity.this, TasksActivity.class);
        intent.putExtra("id", outboundList.get(id).id);
        startActivity(intent);
    }
}
