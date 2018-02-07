package com.icfbs.recyclerview;

/*
 * Created by MOHD IMTIAZ on 29-Jan-18.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.icfbs.HomeActivity;
import com.icfbs.R;
import com.icfbs.expandablelist.CustomExpandableListAdapter;
import com.icfbs.expandablelist.CustomExpandableListView;
import com.icfbs.expandablelist.ExpandableListHeader;

import java.util.ArrayList;
import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyRecyclerViewHolder> {

    private HomeActivity context;
    private LayoutInflater inflater;
    List<MessageStructure> recyclerViewContent;

    public MyRecyclerViewAdapter(Context context, List<MessageStructure> recyclerViewContent) {
        this.context= (HomeActivity) context;
        inflater=LayoutInflater.from(context);
        this.recyclerViewContent=recyclerViewContent;
    }

    @Override
    public MyRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("ICFBS","Inflating new row...");
        View view=inflater.inflate(R.layout.client_message_outlook,parent,false);
        MyRecyclerViewHolder viewHolder=new MyRecyclerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyRecyclerViewHolder holder, int position) {
        MessageStructure mData = recyclerViewContent.get(position);
        if (mData.isListMessage) {
            holder.client.setVisibility(View.GONE);
            holder.server.setVisibility(View.VISIBLE);
            holder.sMessage.setVisibility(View.GONE);
            holder.expandableListView.setVisibility(View.VISIBLE);
            CustomExpandableListAdapter adapter = new CustomExpandableListAdapter(context, (ArrayList<ExpandableListHeader>) recyclerViewContent.get(position).messageList);
            holder.expandableListView.setAdapter(adapter);
            //holder.expandableListView.computeScroll();
        } else if (!mData.isNewChat) {
            if (mData.isClient) {
                holder.server.setVisibility(View.GONE);
                holder.newChatDate.setVisibility(View.GONE);
                holder.client.setVisibility(View.VISIBLE);
                holder.cMessage.setText(mData.message);
                holder.cDate.setText(mData.date);
            } else {
                holder.expandableListView.setVisibility(View.GONE);
                holder.server.setVisibility(View.VISIBLE);
                holder.newChatDate.setVisibility(View.GONE);
                holder.client.setVisibility(View.GONE);
                holder.sMessage.setText(mData.message);
                holder.sDate.setText(mData.date);
            }
            holder.server.getRootView().setClickable(false);
            holder.server.getRootView().setLongClickable(false);
        } else {
            holder.client.setVisibility(View.GONE);
            holder.server.setVisibility(View.GONE);
            holder.newChatDate.setVisibility(View.VISIBLE);
            holder.nChatDate.setText(mData.date);
            holder.server.getRootView().setClickable(false);
            holder.server.getRootView().setLongClickable(false);
        }
        if (context.selectedMessages.containsKey(position))
            holder.server.getRootView().setBackgroundResource(R.color.colorSelectedMessage);
        else
            holder.server.getRootView().setBackgroundResource(android.R.color.transparent);

    }

    @Override
    public int getItemCount() {
        return recyclerViewContent.size();
    }

    class MyRecyclerViewHolder extends RecyclerView.ViewHolder {
        CustomExpandableListView expandableListView;
        LinearLayout server, client, newChatDate;
        TextView sMessage, cMessage, sDate, cDate, nChatDate;
        public MyRecyclerViewHolder(View v) {
            super(v);
            Log.d("ICFBS","Initializing new view obj...");
            expandableListView = (CustomExpandableListView) v.findViewById(R.id.elvTransactionMessage);
            server = (LinearLayout) v.findViewById(R.id.llServerMessageContainer);
            client = (LinearLayout) v.findViewById(R.id.llClientMessageContainer);
            newChatDate = (LinearLayout) v.findViewById(R.id.llNewChatDateContainer);
            sMessage = (TextView) v.findViewById(R.id.tvServerMessage);
            cMessage = (TextView) v.findViewById(R.id.tvClientMessage);
            sDate = (TextView) v.findViewById(R.id.tvServerMessageDate);
            cDate = (TextView) v.findViewById(R.id.tvClientMessageDate);
            nChatDate = (TextView) v.findViewById(R.id.tvNewChatDate);
        }
    }
}

