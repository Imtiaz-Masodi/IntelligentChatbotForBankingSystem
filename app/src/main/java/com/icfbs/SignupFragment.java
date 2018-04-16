package com.icfbs;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignupFragment extends Fragment {

    SignupFragCommunicator fragCommunicator;
    EditText accountNo,phoneNo,email,password,rePassword;
    Button createAccount;
    private final String CUSTOM_FONT="fonts/app_font.ttf";

    public SignupFragment() {
        // Required empty public constructor
    }

    public void setContext(Context context) {
        fragCommunicator = (SignupFragCommunicator) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        accountNo = getActivity().findViewById(R.id.etSignupAccountNo);
        phoneNo = getActivity().findViewById(R.id.etSignupPhoneNo);
        email = getActivity().findViewById(R.id.etSignupEmail);
        password = getActivity().findViewById(R.id.etSignPassword);
        rePassword = getActivity().findViewById(R.id.etSignRePassword);
        createAccount = getActivity().findViewById(R.id.bSignup);

        accountNo.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), CUSTOM_FONT));
        phoneNo.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), CUSTOM_FONT));
        email.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), CUSTOM_FONT));
        password.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), CUSTOM_FONT));
        rePassword.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), CUSTOM_FONT));
        createAccount.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), CUSTOM_FONT));

        createAccount.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                String account = accountNo.getText().toString().trim();
                String phone = phoneNo.getText().toString().trim();
                String emailID = email.getText().toString().trim();
                String pwd = password.getText().toString().trim();
                String rPwd = rePassword.getText().toString().trim();
                clearErrorNotes();
                int error=0;

                if(account.length()==0) {
                    accountNo.setError("Account No can't be Empty");
                    error=1;
                } else if(account.length()<11) {
                    accountNo.setError("Invalid Account No.");
                    error=1;
                } else if(phone.length() !=10) {
                    phoneNo.setError("Invalid Phone Number");
                    error=1;
                } else if(!emailID.contains("@")) {
                    email.setError("Invalid Email ID.");
                    error=1;
                } else if(pwd.length()<4) {
                    password.setError("Password should be of minimum 4 characters.");
                    error=1;
                } else if(!pwd.equals(rPwd)) {
                    rePassword.setError("Re-type Password doesn't match with the password.");
                    error=1;
                }
                if(error==0) {
                    fragCommunicator.signup(account,phone,emailID,pwd);
                }
            }
        });
    }

    private void clearErrorNotes() {
        accountNo.setError(null);
        phoneNo.setError(null);
        email.setError(null);
        password.setError(null);
        rePassword.setError(null);
    }

    public interface SignupFragCommunicator {
        void signup(String accountno, String phone, String email, String password);
    }
}
