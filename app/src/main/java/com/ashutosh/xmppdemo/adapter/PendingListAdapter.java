package com.ashutosh.xmppdemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.ashutosh.xmppdemo.ChatApplication;
import com.ashutosh.xmppdemo.R;

import java.util.ArrayList;

public class PendingListAdapter extends BaseAdapter{

    private Context context;
    private ArrayList<String> userArrayList;
    private ViewHolder viewHolder;

    public PendingListAdapter(Context context, ArrayList<String> userArrayList){
        this.context = context;
        this.userArrayList = userArrayList;
    }

    public static class ViewHolder {
        TextView userNameTv;
        Button approveReqBtn;
        Button rejectReqBtn;
    }

    @Override
    public int getCount() {
        return userArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {

            LayoutInflater inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.pending_list_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.userNameTv = (TextView) rowView.findViewById(R.id.user_name);
            viewHolder.approveReqBtn = (Button) rowView.findViewById(R.id.accept_request);
            viewHolder.rejectReqBtn = (Button) rowView.findViewById(R.id.reject_request);
            rowView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final String userName = userArrayList.get(position);
        viewHolder.userNameTv.setText(userName);

        viewHolder.approveReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatApplication.getmService().xmpp.confirmSubscription(userName,true);
            }
        });

        viewHolder.rejectReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatApplication.getmService().xmpp.confirmSubscription(userName,false);
            }
        });

        return rowView;
    }
}
