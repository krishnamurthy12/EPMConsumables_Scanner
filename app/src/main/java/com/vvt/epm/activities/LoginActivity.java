package com.vvt.epm.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vvt.epm.MainActivity;
import com.vvt.epm.R;
import com.vvt.epm.api_requests.AdminLogin;
import com.vvt.epm.api_responses.genericresponse.GenericResponse;
import com.vvt.epm.utils.OnResponseListener;
import com.vvt.epm.utils.Utilities;
import com.vvt.epm.utils.WebServices;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.vvt.epm.utils.Utilities.showSnackBar;
import static com.vvt.epm.utils.Utilities.showToast;
import static com.vvt.epm.utils.Utilities.toggleVisibility;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, OnResponseListener {

    Toolbar mToolBar;

    EditText mUserName,mPassword;
    Button mLogin;

    Toast mToast;
    Snackbar snackbar;
    ProgressBar mProgressBar;

    public static String IpAddress = "";
    public String url = "";

    @Override
    protected void onStart() {
        IpAddress = Utilities.getIPAddress(this);
        if (IpAddress == null) {
            showToast(this,"IP address is empty");
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeViews();
    }

    private void initializeViews() {
        mToolBar= (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);

        mUserName= (EditText) findViewById(R.id.vE_username);
        mPassword= (EditText) findViewById(R.id.vE_password);
        
        mLogin= (Button) findViewById(R.id.vB_btn_login);

        mProgressBar= (ProgressBar) findViewById(R.id.vP_login_progress_bar);
        mProgressBar.setVisibility(View.GONE);
        
        mLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        
        switch (v.getId())
        {
            case R.id.vB_btn_login:
                hideKeyBoard();
                validateFields();
                break;
        }
        
    }

    private void validateFields() {

        if(!validateUserName()) {
            return;
        }

        else if (!validatePassword()) {
            return;
        }

        validationSuccess();
    }



    private boolean validateUserName() {

        String userName=mUserName.getText().toString().trim();

        if (userName.isEmpty() || userName.length()<3 || !isValidUserName(userName)) {

            showSnackBar(this,getResources().getString(R.string.err_msg_name));
            return false;
        }

        return true;
    }

    private boolean validatePassword() {
        if (mPassword.getText().toString().trim().isEmpty()) {

            showSnackBar(this,getResources().getString(R.string.err_msg_password));
            return false;
        }
        else  if(mPassword.getText().toString().trim().length()<5){

            showSnackBar(this,getResources().getString(R.string.err_msg_password_length));
            return false;
        }

        return true;
    }

    private boolean isValidUserName(String name)
    {
        String regexUserName = "^[A-Za-z\\s]+$";
        Pattern p = Pattern.compile(regexUserName, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(name);
        return m.matches();
    }

    private void validationSuccess() {

        if(!mProgressBar.isShown())
        {
            if(Utilities.isConnectedToInternet(this))
            {
                String userName=mUserName.getText().toString();
                String password=mUserName.getText().toString();
                callAdminLogInAPI(userName,password);

            }
            else
            {
                showSnackBar(this,getResources().getString(R.string.err_msg_nointernet));
            }
        }


    }

    public void callAdminLogInAPI(String userName, String password) {
        AdminLogin adminLogin = new AdminLogin(userName, password);
        if (Utilities.isConnectedToInternet(this)) {
            toggleVisibility(true,mProgressBar);
            WebServices<GenericResponse> webServices = new WebServices<GenericResponse>(LoginActivity.this);
            webServices.adminLogIn(Utilities.getBaseURL(LoginActivity.this), WebServices.ApiType.logIn, adminLogin);

        } else {
            showToast(LoginActivity.this, getResources().getString(R.string.err_msg_nointernet));

        }

    }
    @Override
    public void onResponse(Object response, WebServices.ApiType URL, boolean isSucces, int code) {
        switch (URL) {
            case logIn:
                toggleVisibility(false,mProgressBar);
                if (isSucces) {
                    GenericResponse genericResponse = (GenericResponse) response;
                    if (genericResponse != null) {
                        if (genericResponse.getMessage() != null && genericResponse.getStatus() != null) {
                            if (genericResponse.getStatus().equalsIgnoreCase("success")) {
                                //Success case
                                //showSuccessLayout(genericResponse.getMessage());
                                mUserName.setText("");
                                mPassword.setText("");
                                startActivity(new Intent(LoginActivity.this,AdminHomeActivity.class));
                                finish();
                            } else {
                                //Failure case
                                showSnackBar(this,genericResponse.getMessage());
                            }

                        } else {
                            showSnackBar(LoginActivity.this, "Something went wrong please try again");
                        }

                    } else {
                        showSnackBar(LoginActivity.this, "Server is busy");
                    }

                } else {
                    //API call failed
                    showSnackBar(LoginActivity.this, "Server Timeout");
                }
                break;
        }
    }


    private void hideKeyBoard() {
        try {
            //InputMethodManager is used to hide the virtual keyboard from the user after finishing the user input
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            if (imm.isAcceptingText()) {
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
            }
        } catch (NullPointerException e) {
            Log.e("Exception", e.getMessage() + ">>");
        }

    }

    @Override
    protected void onResume() {
        mUserName.clearFocus();
        mPassword.clearFocus();
        super.onResume();

    }

    @Override
    protected void onPause() {
        mUserName.clearFocus();
        mPassword.clearFocus();
        super.onPause();
    }
}
