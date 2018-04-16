package com.icfbs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class IndexActivity extends AppCompatActivity implements LoginFragment.LoginFragCommunicator, SignupFragment.SignupFragCommunicator, OTPFragment.OTPFragCommunicator, ForgotPassword.ForgotPasswordCommunicator {

    TextView tvLogo, signup, login, changeEmail, backToLogin;
    public static final String[] CUSTOM_FONTS = {"fonts/app_font.ttf", "fonts/logo_font.ttf"};
    public static final String USER_INFO = "user.info";
    FragmentManager manager;
    ForgotPassword forgotPasswordFragment;
    ViewGroup root;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_index);

        context = this;
        login = findViewById(R.id.tvLogin);
        tvLogo = findViewById(R.id.tvLogo);
        signup = findViewById(R.id.tvSignup);
        changeEmail = findViewById(R.id.tvChangeEmail);
        backToLogin = findViewById(R.id.tvBackToLogin);
        root = findViewById(R.id.indexAnimLayout);

        tvLogo.setTypeface(Typeface.createFromAsset(getAssets(), CUSTOM_FONTS[1]));
        signup.setTypeface(Typeface.createFromAsset(getAssets(), CUSTOM_FONTS[0]));
        login.setTypeface(Typeface.createFromAsset(getAssets(), CUSTOM_FONTS[0]));
        changeEmail.setTypeface(Typeface.createFromAsset(getAssets(), CUSTOM_FONTS[0]));
        backToLogin.setTypeface(Typeface.createFromAsset(getAssets(), CUSTOM_FONTS[0]));
        manager = getSupportFragmentManager();

        login.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                android.support.v4.app.FragmentTransaction transaction = manager.beginTransaction();
                transaction.setCustomAnimations(R.animator.slide_from_right, R.animator.slide_to_right, R.animator.slide_from_right, R.animator.slide_to_right);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    Slide slide = new Slide();
                    slide.setDuration(500);
                    login.setVisibility(View.GONE);
                    changeEmail.setVisibility(View.GONE);
                    backToLogin.setVisibility(View.GONE);
                    TransitionManager.beginDelayedTransition(root, slide);
                    signup.setVisibility(View.VISIBLE);
                } else {
                    login.setVisibility(View.GONE);
                    changeEmail.setVisibility(View.GONE);
                    backToLogin.setVisibility(View.GONE);
                    signup.setVisibility(View.VISIBLE);
                }
                LoginFragment loginn = new LoginFragment();
                loginn.setContext(context);
                transaction.replace(R.id.fragmentLayout, loginn, "login");
                transaction.commit();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.setCustomAnimations(R.animator.slide_from_right, R.animator.slide_to_right, R.animator.slide_from_right, R.animator.slide_to_right);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    Slide slide = new Slide();
                    slide.setDuration(500);
                    signup.setVisibility(View.GONE);
                    changeEmail.setVisibility(View.GONE);
                    backToLogin.setVisibility(View.GONE);
                    TransitionManager.beginDelayedTransition(root, slide);
                    login.setVisibility(View.VISIBLE);
                } else {
                    changeEmail.setVisibility(View.GONE);
                    signup.setVisibility(View.GONE);
                    backToLogin.setVisibility(View.GONE);
                    login.setVisibility(View.VISIBLE);
                }
                SignupFragment sign = new SignupFragment();
                sign.setContext(context);
                transaction.replace(R.id.fragmentLayout, sign, "signup");
                transaction.commit();
            }
        });

        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.setCustomAnimations(R.animator.slide_from_right, R.animator.slide_to_right, R.animator.slide_from_right, R.animator.slide_to_right);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    Slide slide = new Slide();
                    slide.setDuration(500);
                    signup.setVisibility(View.GONE);
                    changeEmail.setVisibility(View.GONE);
                    backToLogin.setVisibility(View.GONE);
                    TransitionManager.beginDelayedTransition(root, slide);
                    login.setVisibility(View.VISIBLE);
                } else {
                    changeEmail.setVisibility(View.GONE);
                    backToLogin.setVisibility(View.GONE);
                    signup.setVisibility(View.GONE);
                    login.setVisibility(View.VISIBLE);
                }
                SignupFragment sign = new SignupFragment();
                sign.setContext(context);
                transaction.replace(R.id.fragmentLayout, sign, "signup");
                transaction.commit();
            }
        });

        backToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login.performClick();
            }
        });

        FragmentTransaction transaction = manager.beginTransaction();
        LoginFragment login = new LoginFragment();
        login.setContext(context);
        transaction.add(R.id.fragmentLayout, login, "login");
        transaction.commit();

    }

    @Override
    public void login(String userId, String password) {
        new LoginTask().execute(userId,password);
        //startActivity(new Intent(this, HomeActivity.class));
    }

    @Override
    public void forgotPassword() {
        showForgotPasswordFragment();
    }

    private void showForgotPasswordFragment() {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.animator.slide_from_right, R.animator.slide_to_right, R.animator.slide_from_right, R.animator.slide_to_right);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Slide slide = new Slide();
            slide.setDuration(500);
            signup.setVisibility(View.GONE);
            login.setVisibility(View.GONE);
            changeEmail.setVisibility(View.GONE);
            TransitionManager.beginDelayedTransition(root, slide);
            backToLogin.setVisibility(View.VISIBLE);
        } else {
            backToLogin.setVisibility(View.VISIBLE);
            changeEmail.setVisibility(View.GONE);
            signup.setVisibility(View.GONE);
            login.setVisibility(View.GONE);

        }
        ForgotPassword forgotPassword = new ForgotPassword();
        forgotPassword.setContext(context);
        forgotPasswordFragment = forgotPassword;
        transaction.replace(R.id.fragmentLayout, forgotPassword);
        transaction.commit();
    }

    @Override
    public void signup(String accountno, String phone, String email, String password) {
        new SignupUserTask().execute(accountno, phone, email, password);
    }

    @Override
    public void validateOTP(String account, String email, String otp) {
        new OTPValidateTask().execute(account, email, otp);
    }

    @Override
    public void sendOTPToChangePassword(String email) {
        new SendOTPTask().execute(email);
    }

    @Override
    public void verifyOTPTOChangePassword(String email, int otp) {
        new VerifyOTPToChangePasswordTask().execute(email, otp + "");
    }

    @Override
    public void changePassword(String email, String pwd) {
        new ChangePassword().execute(email, pwd);
    }

    class SignupUserTask extends AsyncTask<String, Integer, String> {
        String account, phone, email, pwd;
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(IndexActivity.this);
            dialog.setTitle("Creating Account");
            dialog.setMessage("Please wait...\nYour account is being created");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... userInfo) {
            URL url;
            HttpURLConnection connection;
            account = userInfo[0];
            phone = userInfo[1];
            email = userInfo[2];
            pwd = userInfo[3];
            try {
                url = new URL("http://" + HomeActivity.IP_ADDRESS + ":8080/SBIBot/Signup");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setConnectTimeout(3000);
                OutputStream os = connection.getOutputStream();
                os.write(("account=" + account + "&phone="+phone+"&email=" + email + "&pwd=" + pwd).getBytes());
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                    StringBuilder res = new StringBuilder();
                    int c = reader.read();
                    while (c != -1) {
                        res.append((char) c);
                        c = reader.read();
                    }
                    return res.toString().trim();
                } else {
                    return "SERVER_DOWN";
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "SERVER_DOWN";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialog.dismiss();
            if (s==null || s.equals("SERVER_DOWN") || s.contains("failed to connect to /192.168.")) {
                Snackbar.make(root, "Server Down. Please try again after sometime.", Snackbar.LENGTH_LONG).show();
            } else if (s.equals("OTP_SENT_SUCCESSFULL")) {
                showOTPFragment();
            } else if (s.equals("ACCOUNT_DOESNOT_EXIST")) {
                Snackbar.make(root, "There is no Account with "+account+" account number.", Snackbar.LENGTH_LONG).show();
            } else if (s.equals("EMAIL_IS_REGISTERED")) {
                Snackbar.make(root, "Email address is already been used with another account.", Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(root, "Something went wrong while creating account. Try again.", Snackbar.LENGTH_LONG).show();
            }
        }

        private void showOTPFragment() {
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.setCustomAnimations(R.animator.slide_from_right, R.animator.slide_to_right, R.animator.slide_from_right, R.animator.slide_to_right);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                Slide slide = new Slide();
                slide.setDuration(500);
                signup.setVisibility(View.GONE);
                login.setVisibility(View.GONE);
                TransitionManager.beginDelayedTransition(root, slide);
                changeEmail.setVisibility(View.VISIBLE);
            } else {
                changeEmail.setVisibility(View.VISIBLE);
                signup.setVisibility(View.GONE);
                login.setVisibility(View.GONE);
            }
            OTPFragment otpFragment = new OTPFragment();
            otpFragment.setContext(context);
            Bundle bundle = new Bundle();
            bundle.putString("EMAIL", email);
            bundle.putString("ACCOUNT", account);
            otpFragment.setArguments(bundle);
            transaction.replace(R.id.fragmentLayout, otpFragment, "otp");
            transaction.commit();
        }
    }

    class OTPValidateTask extends AsyncTask<String, Integer, String> {
        String account, email, otp;
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(IndexActivity.this);
            dialog.setTitle("Validating OTP");
            dialog.setMessage("Please wait...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            URL url;
            HttpURLConnection connection;
            account = strings[0];
            email = strings[1];
            otp = strings[2];
            try {
                url = new URL("http://" + HomeActivity.IP_ADDRESS + ":8080/SBIBot/ValidateOTP");
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                OutputStream os = connection.getOutputStream();
                os.write(("account=" + account + "&email=" + email + "&otp=" + otp).getBytes());
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                    StringBuilder res = new StringBuilder();
                    int c = reader.read();
                    while (c != -1) {
                        res.append((char) c);
                        c = reader.read();
                    }
                    return res.toString().trim();
                } else {
                    return "SERVER_DOWN";
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialog.dismiss();
            if (s.equals("SERVER_DOWN") || s.contains("failed to connect to /192.168.")) {
                Snackbar.make(root, "Server Down. Please try again after sometime.", Snackbar.LENGTH_LONG).show();
            } else if (s.equals("ACCOUNT_CREATED_SUCCESSFULLY")) {
                SharedPreferences preferences = getSharedPreferences(USER_INFO, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("ACCOUNTNO", account);
                editor.putString("EMAIL", email);
                editor.commit();
                finish();
                startActivity(new Intent(context, HomeActivity.class));
                Toast.makeText(context, "Account Created", Toast.LENGTH_SHORT).show();
            } else if (s.equals("INVALID_OTP")) {
                Snackbar.make(root, "Invalid OTP.", Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(root, "Something went wrong while creating account. Try again.", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private class LoginTask extends AsyncTask<String, Integer, String> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(IndexActivity.this);
            dialog.setTitle("Processing");
            dialog.setMessage("Validating your credentials.\nPlease wait...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... data) {
            URL url;
            HttpURLConnection connection;
            String account, pwd;
            account = data[0];
            pwd = data[1];
            try {
                url = new URL("http://" + HomeActivity.IP_ADDRESS + ":8080/SBIBot/Login");
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                OutputStream os = connection.getOutputStream();
                os.write(("account=" + account + "&pwd=" + pwd).getBytes());
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                    StringBuilder res = new StringBuilder();
                    int c = reader.read();
                    while (c != -1) {
                        res.append((char) c);
                        c = reader.read();
                    }
                    return res.toString().trim();
                } else {
                    return "SERVER_DOWN";
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("MYTAG", "Login Res : " + result);
            dialog.dismiss();
            if (result == null || TextUtils.isEmpty(result) || result.equals("SERVER_DOWN") || result.contains("failed to connect to /192.168.")) {
                Snackbar.make(root, "Server Down. Please try again after sometime.", Snackbar.LENGTH_LONG).show();
            } else if (result.contains(" : ")) {
                String cred[] = result.split(" : ");
                SharedPreferences preferences = getSharedPreferences(USER_INFO, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("ACCOUNTNO", cred[0]);
                editor.putString("EMAIL", cred[1]);
                editor.commit();
                finish();
                startActivity(new Intent(context, HomeActivity.class));
            } else {
                Snackbar.make(root, "Invalid Username or Password.", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private class SendOTPTask extends AsyncTask<String, Integer, String> {
        ProgressDialog dialog;
        String email;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(IndexActivity.this);
            dialog.setTitle("Sending OTP");
            dialog.setMessage("Please wait...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... data) {
            URL url;
            HttpURLConnection connection;
            email = data[0];
            try {
                url = new URL("http://" + HomeActivity.IP_ADDRESS + ":8080/SBIBot/SendOTP");
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                OutputStream os = connection.getOutputStream();
                os.write(("email=" + email).getBytes());
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                    StringBuilder res = new StringBuilder();
                    int c = reader.read();
                    while (c != -1) {
                        res.append((char) c);
                        c = reader.read();
                    }
                    return res.toString().trim();
                } else {
                    return "SERVER_DOWN";
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if (result==null || result.equals("SERVER_DOWN") || result.contains("failed to connect to /192.168.")) {
                Snackbar.make(root, "Server Down. Please try again after sometime.", Snackbar.LENGTH_LONG).show();
            } else if (result.equals("OTP_SENT_SUCCESSFULL")) {
                forgotPasswordFragment.isStep1Done = true;
                forgotPasswordFragment.initPhase2();
            } else if(result.equals("NO_ACCOUNT_EXIST")) {
                Snackbar.make(root, "There is no account with "+email+" email Address in our Database.", Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(root, "There was an Error Sending OTP", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private class VerifyOTPToChangePasswordTask extends AsyncTask<String, Integer, String> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(IndexActivity.this);
            dialog.setTitle("Processing");
            dialog.setMessage("Validating the OTP.\nPlease wait...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... data) {
            URL url;
            HttpURLConnection connection;
            String email, otp;
            email = data[0];
            otp = data[1];
            try {
                url = new URL("http://" + HomeActivity.IP_ADDRESS + ":8080/SBIBot/ValidateOTPToChangePassword");
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                OutputStream os = connection.getOutputStream();
                os.write(("email=" + email + "&otp=" + otp).getBytes());
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                    StringBuilder res = new StringBuilder();
                    int c = reader.read();
                    while (c != -1) {
                        res.append((char) c);
                        c = reader.read();
                    }
                    return res.toString().trim();
                } else {
                    return "SERVER_DOWN";
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if (result.equals("SERVER_DOWN") || result.contains("failed to connect to /192.168.")) {
                Snackbar.make(root, "Server Down. Please try again after sometime.", Snackbar.LENGTH_LONG).show();
            } else if (result.equals("OTP_MATCHED")) {
                forgotPasswordFragment.isStep2Done = true;
                forgotPasswordFragment.initPhase3();
            } else {
                Snackbar.make(root, "There was an Error Validating OTP", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private class ChangePassword extends AsyncTask<String, Integer, String> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(IndexActivity.this);
            dialog.setTitle("Processing");
            dialog.setMessage("Updating your password.\nPlease wait...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... data) {
            URL url;
            HttpURLConnection connection;
            String email, pwd;
            email = data[0];
            pwd = data[1];
            try {
                url = new URL("http://" + HomeActivity.IP_ADDRESS + ":8080/SBIBot/ChangePassword");
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                OutputStream os = connection.getOutputStream();
                os.write(("email=" + email + "&pwd=" + pwd).getBytes());
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                    StringBuilder res = new StringBuilder();
                    int c = reader.read();
                    while (c != -1) {
                        res.append((char) c);
                        c = reader.read();
                    }
                    return res.toString().trim();
                } else {
                    return "SERVER_DOWN";
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if (result.equals("SERVER_DOWN") || result.contains("failed to connect to /192.168.")) {
                Snackbar.make(root, "Server Down. Please try again after sometime.", Snackbar.LENGTH_LONG).show();
            } else if (result.equals("PASSWORD_CHANGED")) {
                login.performClick();
                Snackbar.make(root, "Password Changed Sucessfully", Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(root, "Something went wrong while updating password", Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
