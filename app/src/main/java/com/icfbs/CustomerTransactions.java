package com.icfbs;

import java.util.Date;

/*
 * Created by MOHD IMTIAZ on 03-Feb-18.
 */

public class CustomerTransactions {
    long sender, receiver, amount;
    Date date;
    String action;

    public CustomerTransactions(long sender, long receiver, long amount, Date date, String action) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.date = date;
        this.action = action;
    }
}
