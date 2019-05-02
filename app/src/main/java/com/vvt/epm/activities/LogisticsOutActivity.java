package com.vvt.epm.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.vvt.epm.MainActivity;
import com.vvt.epm.R;
import com.vvt.epm.api_requests.MaterialOutPallets;
import com.vvt.epm.api_requests.MaterialOutSheets;
import com.vvt.epm.api_responses.currentfifo.CurrentFIFOResponse;
import com.vvt.epm.api_responses.currentfifo.Currentfifo;
import com.vvt.epm.api_responses.genericresponse.GenericResponse;
import com.vvt.epm.utils.OnResponseListener;
import com.vvt.epm.utils.Utilities;
import com.vvt.epm.utils.WebServices;

import java.util.List;

import static com.vvt.epm.utils.Utilities.getIPAddress;
import static com.vvt.epm.utils.Utilities.showSnackBar;
import static com.vvt.epm.utils.Utilities.showToast;
import static com.vvt.epm.utils.Utilities.toggleVisibility;

public class LogisticsOutActivity extends AppCompatActivity implements OnResponseListener,View.OnClickListener {
int SELECTED_ITEM_TYPE;

    Toolbar mToolBar;

    EditText mScannedEditText,mQuantityEditText;
    ProgressBar mProgressBar;
    Toast mToast;
    TextView mSelectedItemTypeTextView,mResultText,mRecentFIFoNumber,mNextItemPureWhitePallets,mNextItemPaleWhitePallets,mNextItemBlackPallets,mNextMallemineItemSheets;
    ImageView mClear,mSuccess,mError;
    LinearLayout mStatusTextLayout;
    TextView mQuantityType;

    String text = "";
    String Fifonumber="";
    Handler mHandler;

    String[] materialTypeNames = {"no item", "Leaded", "Lead-free", "Mallemine Sheets", "Black Pallets", "Pure-white pallets", "Pale-white Pallets"};
    @Override
    protected void onStart() {

        String IpAddress=getIPAddress(this);

        if(IpAddress==null)
        {
            showToast(this,"IP address is empty");
            startActivity(new Intent(this,MainActivity.class));
        }

       // m_Runnable.run();
        callgetCurrentFifoAPI();
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logistics_out);
        receiveIntentData();
        initializeViews();
        mHandler=new Handler();
    }

    private void initializeViews() {

        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);

        mProgressBar=findViewById(R.id.vP_progress_bar);
        mRecentFIFoNumber=findViewById(R.id.vT_alo_recent_fifo_number);

        mClear=findViewById(R.id.vI_clear);
        mClear.setVisibility(View.GONE);
        mClear.setOnClickListener(this);

        mNextItemPaleWhitePallets=findViewById(R.id.vT_next_item_palewhite);
        mNextItemPureWhitePallets=findViewById(R.id.vT_next_item_purewhite);
        mNextItemBlackPallets=findViewById(R.id.vT_next_item_black);
        mNextMallemineItemSheets=findViewById(R.id.vT_next_item_sheets);

        mScannedEditText=findViewById(R.id.vE_scanned_edit_text);
        mQuantityEditText=findViewById(R.id.vE_weight);
        mQuantityType=findViewById(R.id.vT_quantity_type);

        if(SELECTED_ITEM_TYPE==3)
        {
            mQuantityType.setText(R.string.pcs);
            mQuantityEditText.setText("1");
        }
        else {
            mQuantityType.setText(R.string.kgs);
            mQuantityEditText.setText("5.00");
        }

        mSuccess= (ImageView) findViewById(R.id.vI_success);
        mError= (ImageView) findViewById(R.id.vI_error);

        mStatusTextLayout= (LinearLayout) findViewById(R.id.vL_status_text_layout);

        mStatusTextLayout.setVisibility(View.VISIBLE);

        mError.setOnClickListener(this);
        mSuccess.setOnClickListener(this);

        mSuccess.setVisibility(View.GONE);
        mError.setVisibility(View.GONE);

        mScannedEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mClear.setVisibility(View.GONE);

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if(s.toString().length()>0)
                {
                    mClear.setVisibility(View.VISIBLE);

                }
                else {
                    mClear.setVisibility(View.GONE);
                }
                if(mProgressBar!=null)
                {
                    if(!mProgressBar.isShown())
                    {
                       /* mScannedEditText.clearFocus();
                        mScannedEditText.setFocusable(false);
                        mScannedEditText.setFocusable(false);*/
                        // mScannedEditText.setEnabled(false);

                        hideKeyBoard();

                        validateCheckOut();

                    }
                    else {
                       showToast(LogisticsOutActivity.this,"please wait a while");
                    }
                }



            }
        });
    }

    private void receiveIntentData() {
        mSelectedItemTypeTextView=findViewById(R.id.vT_alo_selected_item_type);
        Bundle bundle=getIntent().getExtras();
        if(bundle!=null) {
            SELECTED_ITEM_TYPE=bundle.getInt("MATERIAL_TYPE",1);
            mSelectedItemTypeTextView.setText(materialTypeNames[SELECTED_ITEM_TYPE]);
        }
        else {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
    }
    private final Runnable m_Runnable = new Runnable()
    {
        public void run()

        {
            //Toast.makeText(Main2Activity.this,"in runnable",Toast.LENGTH_SHORT).show();

            if(Utilities.isConnectedToInternet(getApplicationContext()))
            {

                //new NextFIFOToBeScan().execute();
                callgetCurrentFifoAPI();
            }

            LogisticsOutActivity.this.mHandler.postDelayed(m_Runnable,10000);
        }

    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.toolbar_items, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.menu_admin_login)
        {
            startActivity(new Intent(this,LoginActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.vI_clear:
                skip();
                break;

            case R.id.vI_success:
                hideStatusTextLayout();
                break;

            case R.id.vI_error:
                hideStatusTextLayout();
                break;
           /* case R.id.btn_check:
                validateCheckOut();
                break;*/
        }

    }

    private void hideStatusTextLayout() {
        mStatusTextLayout.setVisibility(View.GONE);
        mScannedEditText.setFocusable(true);
        mScannedEditText.requestFocus();

    }
    private void showStatusTextLayout()
    {
        mStatusTextLayout.setVisibility(View.VISIBLE);
    }
    private void skip() {
        hideKeyBoard();
        mScannedEditText.setText("");

    }

    private void validateCheckOut() {
        String refregeratorNumber, rackNumber, fifoNumber;
        int materialType;

        if(!TextUtils.isEmpty(mScannedEditText.getText()))
        {
            text = mScannedEditText.getText().toString();
            String[] parts = text.split("-");
            try {
                refregeratorNumber = parts[0];
                rackNumber = parts[1];
                fifoNumber = parts[3];
                materialType= Integer.parseInt(parts[2]);
                if(!TextUtils.isEmpty(mQuantityEditText.getText()))
                {
                    double weight= Double.parseDouble(mQuantityEditText.getText().toString().trim());
                    String weightString=String.valueOf(weight);
                    if(SELECTED_ITEM_TYPE==3)
                    {
                        //Sheets
                        MaterialOutSheets materialOutSheets = new MaterialOutSheets(refregeratorNumber, rackNumber, materialType,fifoNumber);
                        callSheetsOutAPI(materialOutSheets);
                    }
                    else {
                        //pallets
                        MaterialOutPallets materialOutPallets = new MaterialOutPallets(refregeratorNumber, rackNumber, weightString,materialType,fifoNumber);
                        callPalletsOutAPI(materialOutPallets);
                    }

                }
                else {

                }



            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }

        }





    }
    public void callgetCurrentFifoAPI() {
        if (Utilities.isConnectedToInternet(this)) {
            toggleVisibility(true,mProgressBar);
            WebServices<CurrentFIFOResponse> webServices = new WebServices<CurrentFIFOResponse>(LogisticsOutActivity.this);
            webServices.getCurrentFIFO(Utilities.getBaseURL(LogisticsOutActivity.this), WebServices.ApiType.currentFIFO);

        } else {
            showToast(LogisticsOutActivity.this, getResources().getString(R.string.err_msg_nointernet));

        }
    }

    public void callPalletsOutAPI(MaterialOutPallets materialOutPallets) {

        if (Utilities.isConnectedToInternet(this)) {
            toggleVisibility(true,mProgressBar);
            WebServices<GenericResponse> webServices = new WebServices<GenericResponse>(LogisticsOutActivity.this);
            webServices.materialsOutPallets(Utilities.getBaseURL(LogisticsOutActivity.this), WebServices.ApiType.materialOutPallets, materialOutPallets);

        } else {
            showToast(LogisticsOutActivity.this, getResources().getString(R.string.err_msg_nointernet));

        }

    }

    public void callSheetsOutAPI(MaterialOutSheets materialOutSheets) {
        if (Utilities.isConnectedToInternet(this)) {
            toggleVisibility(true,mProgressBar);
            WebServices<GenericResponse> webServices = new WebServices<GenericResponse>(LogisticsOutActivity.this);
            webServices.materialsOutSheets(Utilities.getBaseURL(LogisticsOutActivity.this), WebServices.ApiType.getMaterialOutSheets, materialOutSheets);

        } else {
            showToast(LogisticsOutActivity.this, getResources().getString(R.string.err_msg_nointernet));

        }

    }



    @Override
    public void onResponse(Object response, WebServices.ApiType URL, boolean isSucces, int code) {
        switch (URL) {
            case currentFIFO:
                toggleVisibility(false, mProgressBar);
                if (isSucces) {
                    CurrentFIFOResponse currentFIFOResponse = (CurrentFIFOResponse) response;
                    if (currentFIFOResponse != null) {
                        if (currentFIFOResponse.getCurrentfifo() != null) {
                            List<Currentfifo> mList = currentFIFOResponse.getCurrentfifo();

                            if (mList != null) {
                                setCurrentFIFONumbersToViews(mList);
                            } else {
                                showSnackBar(LogisticsOutActivity.this, "FIFO numbers are empty");
                            }

                           /* if (genericResponse.getStatus().equalsIgnoreCase("success")) {
                                //Success case
                                showSuccessLayout(genericResponse.getMessage());

                            } else {
                                //Failure case
                                showFailureLayout(genericResponse.getMessage());
                            }*/

                        } else {
                            showSnackBar(LogisticsOutActivity.this, "Something went wrong please try again");
                        }

                    } else {
                        showSnackBar(LogisticsOutActivity.this, "Server is busy");
                    }

                } else {
                    //API call failed
                    showSnackBar(LogisticsOutActivity.this, "Server Timeout");
                }
                break;

            case materialOutPallets:
                toggleVisibility(false, mProgressBar);
                if(isSucces)
                {
                    GenericResponse genericResponse= (GenericResponse) response;
                    if(genericResponse!=null)
                    {
                        if (genericResponse.getMessage() != null && genericResponse.getStatus() != null) {
                            if (genericResponse.getStatus().equalsIgnoreCase("success")) {
                                //Success case
                                String currentNumber=mScannedEditText.getText().toString();
                                mScannedEditText.setText("");
                                mRecentFIFoNumber.setText(currentNumber);
                                setStetusText(genericResponse.getMessage(),1);
                                //showSuccessLayout(genericResponse.getMessage());

                            } else {
                                //Failure case
                                String currentNumber=mScannedEditText.getText().toString();
                                mScannedEditText.setText("");
                                mRecentFIFoNumber.setText(currentNumber);

                                 setStetusText(genericResponse.getMessage(),2);

                            }

                        } else {

                            hideStatusTextLayout();
                            showSnackBar(LogisticsOutActivity.this, "Something went wrong please try again");
                        }

                    }else {
                        hideStatusTextLayout();
                        showSnackBar(LogisticsOutActivity.this, "Server is busy");
                    }

                }
                else {
                    //API call failed
                    showSnackBar(LogisticsOutActivity.this, "Server Timeout");
                }
                break;

            case getMaterialOutSheets:
                toggleVisibility(false, mProgressBar);
                if(isSucces)
                {
                    GenericResponse genericResponse= (GenericResponse) response;
                    if(genericResponse!=null)
                    {
                        if (genericResponse.getMessage() != null && genericResponse.getStatus() != null) {
                            if (genericResponse.getStatus().equalsIgnoreCase("success")) {
                                //Success case
                                String currentNumber=mScannedEditText.getText().toString();
                                mScannedEditText.setText("");
                                mRecentFIFoNumber.setText(currentNumber);

                                setStetusText(genericResponse.getMessage(),1);

                            } else {
                                //Failure case
                                String currentNumber=mScannedEditText.getText().toString();
                                mScannedEditText.setText("");
                                mRecentFIFoNumber.setText(currentNumber);

                                setStetusText(genericResponse.getMessage(),2);
                            }

                        } else {

                            hideStatusTextLayout();
                            showSnackBar(LogisticsOutActivity.this, "Something went wrong please try again");
                        }

                    }else {
                        hideStatusTextLayout();
                        showSnackBar(LogisticsOutActivity.this, "Server is busy");
                    }

                }
                else {
                    //API call failed
                    showSnackBar(LogisticsOutActivity.this, "Server Timeout");
                }
                break;

        }
    }

    private void setCurrentFIFONumbersToViews(List<Currentfifo> mList) {

        for (Currentfifo currentfifo:mList)
        {
            if(currentfifo.getMaterialId()==3)
            {
                //Mallemine Sheets
                mNextMallemineItemSheets.setText(currentfifo.getPresentfifonumber());

            }
            else if(currentfifo.getMaterialId()==4)
            {
                //Black Pallets
                mNextItemBlackPallets.setText(currentfifo.getPresentfifonumber());

            }
            else if(currentfifo.getMaterialId()==5)
            {
                //Pure-white pallets

                mNextItemPureWhitePallets.setText(currentfifo.getPresentfifonumber());

            }
            else if(currentfifo.getMaterialId()==6)
            {
                //Pale-white Pallets
                mNextItemPaleWhitePallets.setText(currentfifo.getPresentfifonumber());

            }
        }
    }

    private void setStetusText(String message,int status)
    {
        if(status==1)
        {
            //Correct FIFO number

            mSuccess.setVisibility(View.VISIBLE);
            mError.setVisibility(View.GONE);

            mResultText.setTextColor(getResources().getColor(R.color.green));
            mResultText.setText(message);

        }else if(status==2)
        {

            //No item at Specified location

            mSuccess.setVisibility(View.GONE);
            mError.setVisibility(View.VISIBLE);

            mResultText.setTextColor(getResources().getColor(R.color.error_red));
            mResultText.setText(message);

        }

        else {

            mSuccess.setVisibility(View.GONE);
            mError.setVisibility(View.GONE);
            mResultText.setTextColor(getResources().getColor(R.color.error_red));
            mResultText.setText("");
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
    public void onBackPressed() {

        hideStatusTextLayout();
        hideKeyBoard();
        super.onBackPressed();
    }
}
