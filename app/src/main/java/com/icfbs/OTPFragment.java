package com.icfbs;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class OTPFragment extends Fragment {

    String email,account;
    TextView tvOtp;
    EditText otp;
    Button validateEmail;
    private IndexActivity context;
    private final String CUSTOM_FONT="fonts/app_font.ttf";
    public OTPFragment() {
        // Required empty public constructor
    }

    public void setContext(Context context) {
        this.context = (IndexActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        email = getArguments().getString("EMAIL");
        account = getArguments().getString("ACCOUNT");
        return inflater.inflate(R.layout.fragment_otp, container, false);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tvOtp = getActivity().findViewById(R.id.tvOTP);
        otp = getActivity().findViewById(R.id.etOTP);
        validateEmail = getActivity().findViewById(R.id.bValidateAccount);
        tvOtp.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),CUSTOM_FONT));
        otp.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),CUSTOM_FONT));
        validateEmail.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),CUSTOM_FONT));

        String s1 = "OTP was sent to ";
        String s2=" address. Type in the OTP to confirm your email.";

        Spannable emailSpanText = new SpannableString(email);
        emailSpanText.setSpan(new StyleSpan(Typeface.BOLD),0, email.length(), 0);

        tvOtp.setText(s1);
        tvOtp.append(emailSpanText);
        tvOtp.append(s2);

        validateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String typedOTP = otp.getText().toString().trim();
                if(typedOTP != null) {
                    context.validateOTP(account, email, typedOTP);
                } else {
                    otp.setError("OTP can't be empty.");
                }
            }
        });
    }

    interface OTPFragCommunicator {
        void validateOTP(String account, String email, String otp);
    }
}
