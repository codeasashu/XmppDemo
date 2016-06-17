package com.ashutosh.xmppdemo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ashutosh.xmppdemo.model.ChatItem;
import com.ashutosh.xmppdemo.R;
import com.ashutosh.xmppdemo.util.SessionManager;

import java.util.ArrayList;

public class ChatAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<ChatItem> chatItemArrayList;
    public static ViewHolder viewHolder;

    SessionManager sessionManager;

    private String currentUser;

    public ChatAdapter(Context context, ArrayList<ChatItem> chatListItemArrayList){
        this.context = context;
        this.chatItemArrayList = chatListItemArrayList;

        sessionManager = new SessionManager(context);
        currentUser = sessionManager.getUser();

    }

    public static class ViewHolder {
        LinearLayout bubbleLayout;
        LinearLayout parentBubbleLayout;
        TextView chatBubble;
    }

    @Override
    public int getCount() {
        return chatItemArrayList.size();
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
            rowView = inflater.inflate(R.layout.chatbubble, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.chatBubble = (TextView) rowView.findViewById(R.id.message_text);
            viewHolder.bubbleLayout = (LinearLayout) rowView.findViewById(R.id.bubble_layout);
            viewHolder.parentBubbleLayout = (LinearLayout) rowView.findViewById(R.id.bubble_layout_parent);

            rowView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ChatItem chatItem = (ChatItem) chatItemArrayList.get(position);
        viewHolder.chatBubble.setText(chatItem.getChat());

        //If the message is sent by me
        if(chatItem.isMine()){

            try {
                viewHolder.bubbleLayout.setBackground(context.getResources().getDrawable(R.mipmap.in_message_bg));
            } catch (Exception e){
                viewHolder.bubbleLayout.setBackgroundResource(R.mipmap.in_message_bg);
            }

            viewHolder.parentBubbleLayout.setGravity(Gravity.RIGHT);

        } else {

            //Otherwise, Message was sent to me
            try {
                viewHolder.bubbleLayout.setBackground(context.getResources().getDrawable(R.mipmap.out_message_bg));
            } catch (Exception e){
                viewHolder.bubbleLayout.setBackgroundResource(R.mipmap.out_message_bg);
            }

            viewHolder.parentBubbleLayout.setGravity(Gravity.LEFT);

        }

        viewHolder.chatBubble.setTextColor(Color.BLACK);

        return rowView;
    }
}
