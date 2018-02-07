package com.icfbs;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.icfbs.expandablelist.ExpandableListChild;
import com.icfbs.expandablelist.ExpandableListHeader;
import com.icfbs.recyclerview.MessageStructure;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/*
 * Created by MOHD IMTIAZ on 30-Jan-18.
 */

public class SendMessage extends AsyncTask<MessageStructure, Integer, String> {
    private HomeActivity context;
    private String accountNo;
    private final String JSON_FORMAT="application/json";

    SendMessage(Context context, String accountNo) {
        super();
        this.context = (HomeActivity) context;
        this.accountNo = accountNo;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(MessageStructure... params) {
        URL url;
        HttpURLConnection connection;
        MessageStructure message;
        try {
            message = params[0];
            String msg = message.message;
            url = new URL("http://" + HomeActivity.IP_ADDRESS + ":8080/Swiss/SendMessage");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(3000);
            //connection.connect();
            OutputStream os = connection.getOutputStream();
            os.write(("message=" + msg + "&userid="+accountNo).getBytes());

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStreamReader isr = new InputStreamReader(connection.getInputStream());
                StringBuilder res = new StringBuilder();
                int c = isr.read();
                while (c != -1) {
                    res.append((char) c);
                    c = isr.read();
                }
                Log.d("ICFBS","Content Type : "+connection.getContentType());
                Log.d("ICFBS","Full Result : "+res);
                if((connection.getContentType().split(";"))[0].equals(JSON_FORMAT)) {
                    JSONObject jsonObject=new JSONObject(res.toString());
                    Iterator<String> iterator = jsonObject.keys();
                    List<ExpandableListHeader> listHeaders = new ArrayList<>();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        JSONArray jsonArray=jsonObject.getJSONArray(key);
                        Log.d("ICFBS","TTTT : "+jsonArray.getJSONObject(0).toString());

                        JSONObject jObj = jsonArray.getJSONObject(0);
                        long send = jObj.getLong("sender");
                        long recv = jObj.getLong("receiver");
                        long amt = jObj.getLong("amount");
                        long d = jObj.getLong("date");
                        String act = jObj.getString("action");

                        List<ExpandableListChild> listChildren = new ArrayList<>();
                        listChildren.add(new ExpandableListChild("Amount : "+amt));
                        listChildren.add(new ExpandableListChild("Action : "+ act));
                        listChildren.add(new ExpandableListChild("Date : "+new SimpleDateFormat("dd/MMM/yy").format(d)));
                        listHeaders.add(new ExpandableListHeader(act+":"+amt, listChildren));
                    }
                    long timeMillis = Calendar.getInstance().getTimeInMillis();
                    context.receiveMessage(new MessageStructure(listHeaders, true, new SimpleDateFormat("hh:mm").format(timeMillis), timeMillis));
                    return null;
                }
                return res.toString().trim();
            } else {
                throw new Exception("SERVER_DOWN. Response Code : " + connection.getResponseCode());
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String res) {
        if(res == null)
            return;
        super.onPostExecute(res);
        if (res.contains("failed to connect to /192.168."))
            res = "Server Down. Please try again after some time.";
        long timeInMillis = Calendar.getInstance().getTimeInMillis();
        MessageStructure m = new MessageStructure(res, new SimpleDateFormat("hh:mm").format(timeInMillis), timeInMillis, false, false);
        context.receiveMessage(m);
    }
}
