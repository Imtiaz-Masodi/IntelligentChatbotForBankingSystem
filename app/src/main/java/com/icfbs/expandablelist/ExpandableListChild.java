package com.icfbs.expandablelist;

import java.io.Serializable;

/**
 * Created by MOHD IMTIAZ on 27-Jan-18.
 */

public class ExpandableListChild implements Serializable{
    String childData;

    public ExpandableListChild(String childData) {
        this.childData = childData;
    }

    public String getChildData() {
        return childData;
    }

    public void setChildData(String childData) {
        this.childData = childData;
    }
}
