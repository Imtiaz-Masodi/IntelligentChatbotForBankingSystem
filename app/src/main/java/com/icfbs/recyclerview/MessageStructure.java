package com.icfbs.recyclerview;

/*
 * Created by MOHD IMTIAZ on 19-Oct-17.
 */

import com.icfbs.expandablelist.ExpandableListHeader;

import java.io.Serializable;
import java.util.List;

public class MessageStructure implements Serializable {
    public String message, date = null;
    public long timeInMillis;
    public boolean isClient = true, isNewChat = false;
    public boolean isListMessage = false, isLinkedMessage = false;
    public List<ExpandableListHeader> messageList = null;

    public MessageStructure(String message, String date, long timeInMillis, boolean isClient, boolean isNewChat) {
        this.message = message;
        this.date = date;
        this.timeInMillis = timeInMillis;
        this.isClient = isClient;
        this.isNewChat = isNewChat;
    }

    public MessageStructure(String message, List<ExpandableListHeader> messageList, boolean isListMessage, String date, long timeInMillis) {
        this.message = message;
        this.isClient = false;
        this.messageList = messageList;
        this.isListMessage = isListMessage;
        this.date = date;
        this.timeInMillis = timeInMillis;
    }

    public MessageStructure(String message, boolean isLinkedMessage, String date, long timeInMillis) {
        this.message = message;
        this.isLinkedMessage = isLinkedMessage;
        this.date = date;
        this.timeInMillis = timeInMillis;
        isClient = false;
    }
}
