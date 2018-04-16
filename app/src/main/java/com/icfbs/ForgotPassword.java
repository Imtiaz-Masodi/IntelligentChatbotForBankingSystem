package com.icfbs;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ForgotPassword extends Fragment {

    ForgotPasswordCommunicator context;
    LinearLayout layoutSendOTP, layoutVerifyOTP, layoutChangePwd, root;
    EditText email, otp, pwd, rPwd;
    Button sendOTP, valiadteOTP, changePwd;
    boolean isStep1Done, isStep2Done;
    String emailID;


    public ForgotPassword() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        isStep1Done = false;
        isStep2Done = false;
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        layoutSendOTP = getActivity().findViewById(R.id.llSendOTP);
        layoutVerifyOTP = getActivity().findViewById(R.id.llValidateOTP);
        layoutChangePwd = getActivity().findViewById(R.id.llChangePassword);
        root = getActivity().findViewById(R.id.llFPRoot);
        layoutVerifyOTP.setVisibility(View.GONE);
        layoutChangePwd.setVisibility(View.GONE);

        email = getActivity().findViewById(R.id.etFP_Email);
        otp = getActivity().findViewById(R.id.etFP_OTP);
        pwd = getActivity().findViewById(R.id.etFP_Password);
        rPwd = getActivity().findViewById(R.id.etFP_RePassword);

        ((TextView)getActivity().findViewById(R.id.tvFPS1)).setTypeface(Typeface.createFromAsset(getActivity().getAssets(),IndexActivity.CUSTOM_FONTS[0]));
        ((TextView)getActivity().findViewById(R.id.tvFPS2)).setTypeface(Typeface.createFromAsset(getActivity().getAssets(),IndexActivity.CUSTOM_FONTS[0]));
        email.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), IndexActivity.CUSTOM_FONTS[0]));
        otp.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), IndexActivity.CUSTOM_FONTS[0]));
        pwd.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), IndexActivity.CUSTOM_FONTS[0]));
        rPwd.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), IndexActivity.CUSTOM_FONTS[0]));

        sendOTP = getActivity().findViewById(R.id.bFP_SendOTP);
        sendOTP.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), IndexActivity.CUSTOM_FONTS[0]));
        sendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int error = 0;
                emailID = email.getText().toString().trim();

                if (!TextUtils.isEmpty(emailID)) {
                    if (!emailID.contains("@")) {
                        email.setError("Invalid Email Address");
                        error = 1;
                    }
                } else {
                    email.setError("Please Typein your Registered Email Address");
                    error = 1;
                }

                if (error == 0) {
                    context.sendOTPToChangePassword(emailID);
                }
            }
        });

        valiadteOTP = getActivity().findViewById(R.id.bFP_ValidateOTP);
        valiadteOTP.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), IndexActivity.CUSTOM_FONTS[0]));
        valiadteOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String t = otp.getText().toString().trim();
                if(TextUtils.isEmpty(t)) {
                    otp.setError("Type received OTP here");
                } else {
                    try {
                        int typedOTP = Integer.parseInt(t);
                        context.verifyOTPTOChangePassword(emailID,typedOTP);
                    } catch (Exception e) {
                        otp.setError("Invalid OTP");
                    }
                }
            }
        });

        changePwd = getActivity().findViewById(R.id.bFP_ChangePassword);
        changePwd.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), IndexActivity.CUSTOM_FONTS[0]));
        changePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int error=0;
                String passwd = pwd.getText().toString().trim();
                String rpasswd = rPwd.getText().toString().trim();
                if(TextUtils.isEmpty(passwd)) {
                    error=1;
                    pwd.setError("This field Can't be empty");
                } else if(!passwd.equals(rpasswd)) {
                    error=1;
                    rPwd.setError("Re-Type password not matching with the above typed password");
                }

                if(error==0) {
                    context.changePassword(emailID, passwd);
                }
            }
        });
    }

    public void setContext(Context context) {
        this.context = (ForgotPasswordCommunicator) context;
    }

    void initPhase2() {
        if(isStep1Done) {
            ((TextView)getActivity().findViewById(R.id.tvFPS2)).setText("OTP was sent to "+email.getText().toString()+" email address.");
            layoutSendOTP.setVisibility(View.GONE);
            layoutChangePwd.setVisibility(View.GONE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                Slide slide = new Slide();
                slide.setDuration(500);
                TransitionManager.beginDelayedTransition(root, slide);
                layoutVerifyOTP.setVisibility(View.VISIBLE);
            } else {
                layoutVerifyOTP.setVisibility(View.VISIBLE);
            }
        }
    }

    void initPhase3() {
        if(isStep2Done) {
            layoutSendOTP.setVisibility(View.GONE);
            layoutVerifyOTP.setVisibility(View.GONE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                Slide slide = new Slide();
                slide.setDuration(500);
                TransitionManager.beginDelayedTransition(root, slide);
                layoutChangePwd.setVisibility(View.VISIBLE);
            } else {
                layoutChangePwd.setVisibility(View.VISIBLE);
            }
        }
    }

    interface ForgotPasswordCommunicator {
        void sendOTPToChangePassword(String email);
        void verifyOTPTOChangePassword(String email, int otp);
        void changePassword(String email, String pwd);
    }
}
