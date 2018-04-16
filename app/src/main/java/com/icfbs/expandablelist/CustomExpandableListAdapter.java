package com.icfbs.expandablelist;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.icfbs.IndexActivity;
import com.icfbs.R;

import java.util.ArrayList;

/**
 * Created by MOHD IMTIAZ on 27-Jan-18.
 */

public class CustomExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<ExpandableListHeader> listHeader;

    public CustomExpandableListAdapter(Context context, ArrayList<ExpandableListHeader> listHeader) {
        this.context = context;
        this.listHeader = listHeader;
    }

    @Override
    public int getGroupCount() {
        return listHeader.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return listHeader.get(i).getChildList().size();
    }

    @Override
    public Object getGroup(int i) {
        return listHeader.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return listHeader.get(i).getChildList().get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        ExpandableListHeader header= (ExpandableListHeader) getGroup(i);
        if (view==null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.expandable_list_header,null);
        }
        TextView headerText = view.findViewById(R.id.tvExpandableListHeader);
        headerText.setText(header.getHeaderData());
        headerText.setTypeface(Typeface.createFromAsset(context.getAssets(), IndexActivity.CUSTOM_FONTS[0]));
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        ExpandableListChild child= (ExpandableListChild) getChild(i,i1);
        if(view==null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.expandable_list_child,null);
        }
        TextView childText = view.findViewById(R.id.tvExpandableListChild);
        childText.setText(child.getChildData());
        childText.setTypeface(Typeface.createFromAsset(context.getAssets(), IndexActivity.CUSTOM_FONTS[0]));
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
