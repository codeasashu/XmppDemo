package com.ashutosh.xmppdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.ashutosh.xmppdemo.adapter.PendingListAdapter;
import com.ashutosh.xmppdemo.xmpp.XMPPHandler;

import java.util.ArrayList;

public class PendingRequestsActivity extends AppCompatActivity {

    private PendingListAdapter mPendingListAdapter;
    private ArrayList<String> mUserList;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pendingrequest);

        listView = (ListView) findViewById(R.id.pending_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserList = ChatApplication.getmService().xmpp.getPendingRequests();
        mPendingListAdapter = new PendingListAdapter(getApplicationContext(),mUserList);
        listView.setAdapter(mPendingListAdapter);
        mPendingListAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
