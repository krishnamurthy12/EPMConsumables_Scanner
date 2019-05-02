package com.vvt.epm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.vvt.epm.activities.LogisticsInActivity;
import com.vvt.epm.activities.LogisticsOutActivity;

import static com.vvt.epm.utils.Utilities.getIPAddress;
import static com.vvt.epm.utils.Utilities.saveIPAddress;
import static com.vvt.epm.utils.Utilities.showSnackBar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    RadioGroup mRadioGroup;
    int SELECTED_TYPE;
    CardView mCheckIn,mCheckOut;
    LinearLayout mEditIpLayout;
    EditText mPassword,mIPAddressEditText;

    AlertDialog.Builder builder;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("systemcheck","inside on cerate");

        initializeViews();
    }

    private void initializeViews() {
        AssetManager assetManager = getAssets();
        Typeface tfForTitle = Typeface.createFromAsset( assetManager, "Midnight_Drive.otf");
        TextView mApptitle=findViewById(R.id.app_title);
        mApptitle.setTypeface(tfForTitle);

        mCheckIn= (CardView) findViewById(R.id.vR_am_check_in);
        mCheckOut= (CardView) findViewById(R.id.vR_am_check_out);

        mEditIpLayout=findViewById(R.id.vL_edit_ipaddress_layout);

        mRadioGroup=findViewById(R.id.vRG_category_radiogroup);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                switch (i)
                {
                    case R.id.vRB_palewhite_pallets:
                        SELECTED_TYPE=6;
                        break;

                    case R.id.vRB_purewhite_pallets:
                        SELECTED_TYPE=5;
                        break;

                    case R.id.vRB_black_pallets:
                        SELECTED_TYPE=4;
                        break;

                    case R.id.vRB_mallemine_sheets:
                        SELECTED_TYPE=3;
                        break;
                }

            }
        });


            mCheckIn.setOnClickListener(this);
            mCheckOut.setOnClickListener(this);




        mEditIpLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.vR_am_check_in:
                hideKeyBoard();
                gotoNextActivity(LogisticsInActivity.class);
                break;
            case R.id.vR_am_check_out:
                hideKeyBoard();
                gotoNextActivity(LogisticsOutActivity.class);
                break;
            case R.id.vL_edit_ipaddress_layout:
                showPasswordLayout();
                break;
            case R.id.vT_password_ok:
                hideKeyBoard();
                if(mPassword.getText().toString().equalsIgnoreCase("123456"))
                {
                    alertDialog.dismiss();
                    showIPAddressLayout();
                }
                else {
                    showSnackBar(this,"Incorrect password");
                }

                break;

            case R.id.vT_password_cancel:
                hideKeyBoard();
                alertDialog.dismiss();
                break;

            case R.id.vT_ipaddress_cancel:
                hideKeyBoard();
                alertDialog.dismiss();
                break;

            case R.id.vT_ipaddress_ok:
                hideKeyBoard();
                saveIpAddress();
                break;
        }

    }

    public void showPasswordLayout()
    {
        LinearLayout rootlayout=findViewById(R.id.vL_am_root_layout);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.password_layout, rootlayout,false);
        mPassword=dialogView.findViewById(R.id.vE_password);
        TextView cancel = dialogView.findViewById(R.id.vT_password_cancel);
        TextView ok = dialogView.findViewById(R.id.vT_password_ok);

        builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);

        alertDialog = builder.create();
        alertDialog.show();
        cancel.setOnClickListener(MainActivity.this);
        ok.setOnClickListener(MainActivity.this);

    }
    private void showIPAddressLayout()
    {
        String currentIP=getIPAddress(this);

        LinearLayout rootlayout=findViewById(R.id.vL_am_root_layout);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.ipaddress_layout, rootlayout,false);
        mIPAddressEditText=dialogView.findViewById(R.id.vE_ipaddress);
        if(currentIP!=null)
        {
            mIPAddressEditText.setText(currentIP);
        }
        TextView cancel = dialogView.findViewById(R.id.vT_ipaddress_cancel);
        TextView save = dialogView.findViewById(R.id.vT_ipaddress_ok);

        // builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);

        alertDialog = builder.create();
        alertDialog.show();
        cancel.setOnClickListener(MainActivity.this);
        save.setOnClickListener(MainActivity.this);

    }

    private void saveIpAddress() {
        String ip = mIPAddressEditText.getText().toString();
        if (TextUtils.isEmpty(ip) || ip.length() < 12 || !ip.contains(".")) {
            showSnackBar(this,"enter a valid IP address");
            hideKeyBoard();

        } else {
            alertDialog.dismiss();
            saveIPAddress(this,ip);
            hideKeyBoard();

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

    public void gotoNextActivity(Class<?> target)
    {
        if(SELECTED_TYPE!=0)
        {
            Intent intent= new Intent(this,target);
            intent.putExtra("MATERIAL_TYPE",SELECTED_TYPE);
            startActivity(intent);
        }
        else {
            showSnackBar(this,"Please Select one item type");
        }

        //startActivity(new Intent(this,target));

    }
}
