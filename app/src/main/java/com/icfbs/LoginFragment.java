package com.icfbs;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends android.support.v4.app.Fragment {


    LoginFragCommunicator fragCommunicator;
    EditText userId,pwd;
    TextView forgotPassword, guestLogin;
    Button login;
    private final String CUSTOM_FONT="fonts/app_font.ttf";

    public LoginFragment() {
        // Required empty public constructor
    }

    public void setContext(Context context) {
        fragCommunicator= (LoginFragCommunicator) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        userId = getActivity().findViewById(R.id.etLoginID);
        pwd = getActivity().findViewById(R.id.etLoginPassword);
        login = getActivity().findViewById(R.id.bLogin);
        forgotPassword = getActivity().findViewById(R.id.tvForgotPassword);
        guestLogin = getActivity().findViewById(R.id.tvGuest);

        userId.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), CUSTOM_FONT));
        pwd.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), CUSTOM_FONT));
        login.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), CUSTOM_FONT));
        forgotPassword.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), CUSTOM_FONT));
        guestLogin.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), CUSTOM_FONT));

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userID = userId.getText().toString().trim();
                String passwd = pwd.getText().toString().trim();
                clearErrorNotes();
                int error=0;
                if(userID.length()==0) {
                    userId.setError("Account No can't be empty");
                    error=1;
                } else if(!userID.contains("@") && !userID.matches("[a-zA-Z]")) {
                    userId.setError("Invalid Account No");
                    error=1;
                } else if(passwd.length()<4) {
                    pwd.setError("Password should be of minimum 4 characters.");
                    error=1;
                }
                if(error==0) {
                    fragCommunicator.login(userID,passwd);
                }
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragCommunicator.forgotPassword();
            }
        });

        guestLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  new Intent(getActivity().getApplicationContext(), HomeActivity.class);
                intent.putExtra("USER","GUEST");
                startActivity(intent);
            }
        });
    }

    private void clearErrorNotes() {
        userId.setError(null);
        pwd.setError(null);
    }

    public interface LoginFragCommunicator {
        void login(String userId, String password);
        void forgotPassword();
    }
}
