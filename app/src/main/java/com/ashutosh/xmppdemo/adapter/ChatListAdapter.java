package com.ashutosh.xmppdemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ashutosh.xmppdemo.ChatActivity;
import com.ashutosh.xmppdemo.util.Constants;
import com.ashutosh.xmppdemo.R;
import com.ashutosh.xmppdemo.model.RoasterModel;
import com.ashutosh.xmppdemo.util.Utils;

import java.util.ArrayList;



public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    private Context context;
    private ArrayList<RoasterModel> userArrayList;

    public ChatListAdapter(Context context, ArrayList<RoasterModel> userArrayList){
        this.context = context;
        this.userArrayList = userArrayList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView userNameTv, userStatusTv;
        ImageView statusIm;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);

            userNameTv = (TextView) view.findViewById(R.id.user_name);
            userStatusTv = (TextView) view.findViewById(R.id.user_status);
            statusIm = (ImageView) view.findViewById(R.id.status_im);
        }

        @Override
        public void onClick(View v) {
            RoasterModel userName = (RoasterModel) userArrayList.get(getAdapterPosition());
            Intent chatListIntent = new Intent(context,ChatActivity.class);
            chatListIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            chatListIntent.putExtra("roaster",userName);
            context.startActivity(chatListIntent);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_list_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final RoasterModel userName = (RoasterModel) userArrayList.get(position);
        final String userStatusMode = Utils.getStatusMode(userName.getStatus());
        holder.userNameTv.setText(userName.getRoasterEntryUser());
        //    viewHolder.userStatusTv.setText(Utils.getPresenceMode(userName.getPresenceMode()));
        holder.userStatusTv.setText(userStatusMode);

        if(userStatusMode.equals(Constants.PRESENCE_MODE_AVAILABLE)){
            holder.statusIm.setImageDrawable(context.getResources().getDrawable(R.mipmap.im_available));
        } else if(userStatusMode.equals(Constants.PRESENCE_MODE_XA)) {
            holder.statusIm.setImageDrawable(context.getResources().getDrawable(R.mipmap.im_unavailable));
        } else {
            holder.statusIm.setImageResource(0);
        }
    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }
}
