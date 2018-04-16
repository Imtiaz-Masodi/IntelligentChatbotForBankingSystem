package com.icfbs.recyclerview;

/*
 * Created by MOHD IMTIAZ on 29-Jan-18.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.icfbs.HomeActivity;
import com.icfbs.IndexActivity;
import com.icfbs.R;
import com.icfbs.expandablelist.CustomExpandableListAdapter;
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
        View view=inflater.inflate(R.layout.client_message_outlook,parent,false);
        MyRecyclerViewHolder viewHolder=new MyRecyclerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyRecyclerViewHolder holder, int position) {
        final MessageStructure mData = recyclerViewContent.get(position);
        Log.d("ICFBS","Message : "+mData.message);
        if (mData.isListMessage) {
            holder.client.setVisibility(View.GONE);
            holder.server.setVisibility(View.VISIBLE);
            holder.newChatDate.setVisibility(View.GONE);
            holder.sMessage.setVisibility(View.GONE);
            holder.buttonsLayout.setVisibility(View.GONE);
            holder.expandableListView.setVisibility(View.VISIBLE);
            holder.expandableListView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    int action = motionEvent.getAction();
                    switch (action) {
                        case MotionEvent.ACTION_MOVE:
                            view.getParent().requestDisallowInterceptTouchEvent(true);
                            break;
                    }
                    return false;
                }
            });
            CustomExpandableListAdapter adapter = new CustomExpandableListAdapter(context, (ArrayList<ExpandableListHeader>) recyclerViewContent.get(position).messageList);
            holder.expandableListView.setAdapter(adapter);
            //holder.expandableListView.computeScroll();
        }else if(mData.isLinkedMessage || mData.message.contains("<BT>") && mData.message.contains("<CON>")) {
            holder.client.setVisibility(View.GONE);
            holder.server.setVisibility(View.VISIBLE);
            holder.newChatDate.setVisibility(View.GONE);
            holder.expandableListView.setVisibility(View.GONE);
            holder.sMessage.setVisibility(View.VISIBLE);
            holder.buttonsLayout.setVisibility(View.VISIBLE);

            String m = mData.message;
            if(m.contains("<BT>")) {
                String[] mes = m.split("<BT>");
                String[] mm = mes[1].split("<CON>");
                holder.sMessage.setText(mes[0]+"\n\n"+mm[0].replace("</BT>",""));
                final String conMes = mm[1].replace("</CON>","").trim();
                String[] bData = mm[0].split("\"");
                holder.left.setText(bData[1]);
                holder.right.setText(bData[3]);
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String mes = ((Button)v).getText().toString().trim();
                        context.sendMessage((conMes.trim()+" : "+mes.trim()).toUpperCase(), false);
                    }
                };
                holder.left.setOnClickListener(listener);
                holder.right.setOnClickListener(listener);
            }
        }
        else if (!mData.isNewChat) {
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
                holder.buttonsLayout.setVisibility(View.GONE);
                holder.sMessage.setText(mData.message);
                holder.sDate.setText(mData.date);
            }
            holder.server.getRootView().setClickable(true);
            holder.server.getRootView().setLongClickable(true);
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
        Button left,right;
        ExpandableListView expandableListView;
        LinearLayout server, client, newChatDate,buttonsLayout;
        TextView sMessage, cMessage, sDate, cDate, nChatDate;
        public MyRecyclerViewHolder(View v) {
            super(v);
            expandableListView = (ExpandableListView) v.findViewById(R.id.elvTransactionMessage);
            server = (LinearLayout) v.findViewById(R.id.llServerMessageContainer);
            client = (LinearLayout) v.findViewById(R.id.llClientMessageContainer);
            newChatDate = (LinearLayout) v.findViewById(R.id.llNewChatDateContainer);
            buttonsLayout = v.findViewById(R.id.layoutMessageButtons);
            left = v.findViewById(R.id.messageButton1);
            right = v.findViewById(R.id.messageButton2);
            sMessage = (TextView) v.findViewById(R.id.tvServerMessage);
            cMessage = (TextView) v.findViewById(R.id.tvClientMessage);
            sDate = (TextView) v.findViewById(R.id.tvServerMessageDate);
            cDate = (TextView) v.findViewById(R.id.tvClientMessageDate);
            nChatDate = (TextView) v.findViewById(R.id.tvNewChatDate);

            left.setTypeface(Typeface.createFromAsset(context.getAssets(), IndexActivity.CUSTOM_FONTS[0]));
            right.setTypeface(Typeface.createFromAsset(context.getAssets(), IndexActivity.CUSTOM_FONTS[0]));
            sMessage.setTypeface(Typeface.createFromAsset(context.getAssets(), IndexActivity.CUSTOM_FONTS[0]));
            cMessage.setTypeface(Typeface.createFromAsset(context.getAssets(), IndexActivity.CUSTOM_FONTS[0]));
            sDate.setTypeface(Typeface.createFromAsset(context.getAssets(), IndexActivity.CUSTOM_FONTS[0]));
            cDate.setTypeface(Typeface.createFromAsset(context.getAssets(), IndexActivity.CUSTOM_FONTS[0]));
            nChatDate.setTypeface(Typeface.createFromAsset(context.getAssets(), IndexActivity.CUSTOM_FONTS[0]));
        }
    }
}

