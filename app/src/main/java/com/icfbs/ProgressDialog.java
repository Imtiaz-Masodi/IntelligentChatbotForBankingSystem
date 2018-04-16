package com.icfbs;


import android.app.Activity;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;


public class ProgressDialog extends Dialog {

    ProgressBar progress;
    TextView title,message;
    String[] data =new String[2];

    public ProgressDialog(Activity activity) {
        super(activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fragment_progress_dialog);
        progress=findViewById(R.id.progressBar);
    }

    public void setTitle(String title) {
        data[0]=title;
    }


    public void setMessage(String message) {
        data[1]=message;
    }

    @Override
    public void show() {
        super.show();
        title=findViewById(R.id.progressTitle);
        message=findViewById(R.id.progressMessage);
        title.setTypeface(Typeface.createFromAsset(getContext().getAssets(),IndexActivity.CUSTOM_FONTS[0]));
        message.setTypeface(Typeface.createFromAsset(getContext().getAssets(),IndexActivity.CUSTOM_FONTS[0]));
        title.setText(data[0]);
        message.setText(data[1]);
    }
}
