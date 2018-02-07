package com.icfbs.expandablelist;

/*
 * Created by MOHD IMTIAZ on 27-Jan-18.
 */

import java.util.List;

public class ExpandableListHeader {
    private String headerData;
    private List<ExpandableListChild> childList;

    public ExpandableListHeader(String headerData, List<ExpandableListChild> childList) {
        this.headerData = headerData;
        this.childList = childList;
    }

    public String getHeaderData() {
        return headerData;
    }

    public List<ExpandableListChild> getChildList() {
        return childList;
    }

    public void setHeaderData(String headerData) {
        this.headerData = headerData;
    }

    public void setChildList(List<ExpandableListChild> childList) {
        this.childList = childList;
    }
}
