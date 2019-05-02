package com.vvt.epm.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.vvt.epm.MainActivity;
import com.vvt.epm.R;
import com.vvt.epm.api_requests.MaterialIn;
import com.vvt.epm.api_requests.PrintConfirmation;
import com.vvt.epm.api_responses.currentfifo.CurrentFIFOResponse;
import com.vvt.epm.api_responses.currentfifo.Currentfifo;
import com.vvt.epm.api_responses.genericresponse.GenericResponse;
import hardware.print.printer;
import com.vvt.epm.utils.OnResponseListener;
import com.vvt.epm.utils.Utilities;
import com.vvt.epm.utils.WebServices;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static com.vvt.epm.utils.Utilities.showSnackBar;
import static com.vvt.epm.utils.Utilities.showToast;

import com.google.zxing.MultiFormatWriter;

import static com.vvt.epm.utils.Utilities.toggleVisibility;

public class LogisticsInActivity extends AppCompatActivity implements View.OnClickListener, OnResponseListener {

    int SELECTED_ITEM_TYPE;
    Handler mHandler;

    String[] materialTypeNames = {"no item", "Leaded", "Lead-free", "Mallemine Sheets", "Black Pallets", "Pure-white pallets", "Pale-white Pallets"};

    Toolbar toolbar;
    int count = 0;

    Button mPrint, mStep, mReprint, mBack, mNext;
    EditText mScannedEditText, mWeightEditText;
    TextView mSelectedItemTypeTextView,mQuantityTypeTextView, mCurrentFifoNumberTextView;
    printer m_printer = new printer();
    Paint m_pat = new Paint();
    String text = "";
    Bitmap bitmap;
    LinearLayout mPrintLayout, mReprintLayout;
    ProgressBar mProgressBar;
    ImageView mBitmapImage, mClear;

    Toast mToast;

    public static String IpAddress = "";
    public String refregeratorNumber = "";
    public String rackNumber = "";
    public int materialID = 1;
    public String url = "";

    String scannedRackNumber, currentFIFONumber;

    AlertDialog.Builder builder;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logistics_in);

        receiveIntentData();
        initializeViews();
    }

    private void initializeViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mBitmapImage = (ImageView) findViewById(R.id.bitmap_img);


        mPrint = (Button) findViewById(R.id.btn_print);
        mStep = (Button) findViewById(R.id.btn_Step);
        mScannedEditText = (EditText) findViewById(R.id.vE_edittext);
        mWeightEditText = findViewById(R.id.vE_weight);

        mQuantityTypeTextView = (TextView) findViewById(R.id.vT_quantity_type);
        mCurrentFifoNumberTextView = findViewById(R.id.vT_display_text);

        //disableInput(mScannedEditText);

        mPrintLayout = (LinearLayout) findViewById(R.id.vL_sticker_print_layout);
        mReprintLayout = (LinearLayout) findViewById(R.id.vL_sticker_reprint_layout);

        mReprint = (Button) findViewById(R.id.btn_reprint);
        mNext = (Button) findViewById(R.id.btn_next);
        mBack = (Button) findViewById(R.id.btn_back);

        mProgressBar = (ProgressBar) findViewById(R.id.vP_al_progress_bar);

        mClear = (ImageView) findViewById(R.id.vI_cancel);
        mClear.setVisibility(View.GONE);

        mProgressBar.setVisibility(View.GONE);


        m_printer.Open();

        mPrint.setOnClickListener(this);
        mStep.setOnClickListener(this);

        mReprint.setOnClickListener(this);
        mNext.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mClear.setOnClickListener(this);

        hidePrintLayout();

        if (SELECTED_ITEM_TYPE == 3) {
            mQuantityTypeTextView.setText(R.string.pcs);
            mWeightEditText.setText("5");
        } else {
            mQuantityTypeTextView.setText(R.string.kgs);
            mWeightEditText.setText("15.00");

        }


        mScannedEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mClear.setVisibility(View.GONE);
                Log.d("enteredtext ", "beforeTextChanged=>" + s.toString());
                Log.d("enteredtext", "beforeTextChanged=>" + mScannedEditText.getText().toString());

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("enteredtext", "onTextChanged=>" + s.toString());
                Log.d("enteredtext", "onTextChanged=>" + mScannedEditText.getText().toString());

               /* mScannedEditText.clearFocus();

                hideKeyBoard();
                generateSticker();*/
            }

            @Override
            public void afterTextChanged(Editable s) {
                hideKeyBoard();
                Log.d("enteredtext", "afterTextChanged=>" + s.toString());
                Log.d("enteredtext", "afterTextChanged=>" + mScannedEditText.getText().toString());

                if (s.toString().length() > 0) {
                    mClear.setVisibility(View.VISIBLE);

                } else {
                    mClear.setVisibility(View.GONE);
                }


               /* mScannedEditText.clearFocus();
                mScannedEditText.setFocusable(false);
                mScannedEditText.setFocusable(false);*/
                // mScannedEditText.setEnabled(false);

                hideKeyBoard();
                generateSticker();

            }
        });

    }


    private void receiveIntentData() {
        mSelectedItemTypeTextView=findViewById(R.id.vT_ali_selected_item_type);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            SELECTED_ITEM_TYPE = bundle.getInt("MATERIAL_TYPE", 1);
            mSelectedItemTypeTextView.setText(materialTypeNames[SELECTED_ITEM_TYPE]);
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.toolbar_items, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_admin_login) {
            startActivity(new Intent(this, LoginActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_print:
                printSticker();
                break;

            case R.id.btn_Step:
                stepUp();
                break;

            case R.id.btn_reprint:
                printSticker();
                break;

            case R.id.btn_next:
                //next();
                generateSticker();
                break;

            case R.id.btn_back:
                stepUp();
                break;

            case R.id.vI_cancel:
                skip();
                break;
        }

    }

    private void generateSticker() {
        text = mScannedEditText.getText().toString();
        if (TextUtils.isEmpty(text)) {
            if (text.length() < 15) {
                String[] parts = text.split("/");
                if (parts != null) {
                    try {
                        if (parts.length > 2) {

                            refregeratorNumber = parts[0];
                            rackNumber = parts[1];
                            materialID = Integer.parseInt(parts[2]);

                            if (refregeratorNumber != null && rackNumber != null && materialID != 1) {
                                if (!TextUtils.isEmpty(mWeightEditText.getText())) {
                                    double weight = Double.parseDouble(mWeightEditText.getText().toString());
                                    String weightString = String.valueOf(weight);
                                    callMaterialsInAPI(refregeratorNumber, rackNumber, weightString, materialID);
                                } else {
                                    showToast(this, "Weight is empty!!!");
                                }

                            } else {

                                showToast(this, "Invalid Parameters");
                            }
                            //callGenerateFIFONumber();
                        } else {
                            showToast(this, "Invalid QR code");
                        }

                    } catch (ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                        showToast(this, "Invalid Parameters");
                    }


                }

            } else {
                mScannedEditText.setText("");
                mScannedEditText.requestFocus();
                mScannedEditText.isFocusable();
                mScannedEditText.isActivated();
            }

        }
    }

    private void printSticker() {
        //m_printer.PrintString(str, 20);

        String currentlyDisplayedNumber = mCurrentFifoNumberTextView.getText().toString().trim();
        //RF01-L01-3-00001

        String[] stringArray = currentlyDisplayedNumber.split("-");
        String refNumber = "";
        String rack = "";
        String fifoNumber = "";
        int type = 3;
        try {
            refNumber = stringArray[0];
            rack = stringArray[1];
            type = Integer.parseInt(stringArray[2]);
            fifoNumber = stringArray[3];


        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        if (count == 10) {
            Log.d("countvalue", "count=>" + count);
            count = 0;
            //m_printer.Step((byte)0xA0); //160
            m_printer.Step((byte) 0x50); //80

            m_printer.PrintLineInit(30);
            m_printer.PrintLineString("\r\n", 40, printer.PrintType.Centering, true);//160
            m_printer.PrintLineEnd();
        }
        count++;

        if (bitmap != null) {
            m_printer.PrintBitmap(bitmap);
            m_printer.PrintLineEnd();
        }

        m_printer.PrintLineInit(25);
        m_printer.PrintLineString(currentlyDisplayedNumber, 32, printer.PrintType.Centering, true);//160
        m_printer.PrintLineEnd();

        //String currentDateTime = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
        //jun 2,2018 4:11:17 PM

        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
        String datetime = dateformat.format(c.getTime());

        //Log.d("dateandtime","SimpleDateFormat=>"+datetime);

        m_printer.PrintLineInit(20);
        m_printer.PrintLineString(datetime, 22, printer.PrintType.Centering, true);//160
        m_printer.PrintLineEnd();

        m_printer.PrintLineInit(125);
        m_printer.PrintLineString("\r\n", 40, printer.PrintType.Centering, true);//160
        m_printer.PrintLineEnd();
       /* m_printer.PrintLineInit(50);
        m_printer.PrintLineString(" ", 20, PrintType.Centering, true);//160
        m_printer.PrintLineEnd();*/

        if (Utilities.isConnectedToInternet(this)) {
            if (!mProgressBar.isShown()) {
                hideKeyBoard();

                callPrintConfirmationInAPI(refNumber, rack, fifoNumber, type);

                //callPrintConfirmationInAPI();
            } else {
                showToast(this, "Please wait a while");
            }

        } else {
            showToast(this, getResources().getString(R.string.err_msg_nointernet));
        }

        /*hidePrintLayout();
        showReprintLayout();*/
    }

    private void stepUp() {
        m_printer.Step((byte) 0x3C);

    }

    private void skip() {
        hideKeyBoard();
        mScannedEditText.setText("");
        mScannedEditText.requestFocus();
        mScannedEditText.isFocusable();
        mScannedEditText.isActivated();
        mScannedEditText.setFocusable(true);
        //mBitmapImage.setImageBitmap(null);
        /*  mClear.setVisibility(View.GONE);*/

    }


    private void showReprintLayout() {
        mPrintLayout.setVisibility(View.GONE);
        mReprintLayout.setVisibility(View.VISIBLE);
    }

    private void hideReprintLayout() {
        mPrintLayout.setVisibility(View.GONE);
        mReprintLayout.setVisibility(View.GONE);
    }

    private void showPrintLayout() {

        mPrintLayout.setVisibility(View.VISIBLE);
        mReprintLayout.setVisibility(View.GONE);
    }

    private void hidePrintLayout() {
        // mScannedEditText.setText("");
        mPrintLayout.setVisibility(View.GONE);
        mReprintLayout.setVisibility(View.GONE);
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


    /************************************
     Beginning of API call section
     *****************************************************************************************************************************/

    public void callMaterialsInAPI(String refrigeraterNumber, String rackNumber, String quantity, int materialId) {
        MaterialIn materialIn = new MaterialIn(refrigeraterNumber, rackNumber, quantity, materialId);
        if (Utilities.isConnectedToInternet(this)) {
            toggleVisibility(true, mProgressBar);
            WebServices<GenericResponse> webServices = new WebServices<GenericResponse>(LogisticsInActivity.this);
            webServices.materialsIn(Utilities.getBaseURL(LogisticsInActivity.this), WebServices.ApiType.materialIn, materialIn);

        } else {
            showToast(LogisticsInActivity.this, getResources().getString(R.string.err_msg_nointernet));

        }

    }

    public void callgetCurrentFifoAPI() {
        if (Utilities.isConnectedToInternet(this)) {
            toggleVisibility(true, mProgressBar);
            WebServices<CurrentFIFOResponse> webServices = new WebServices<CurrentFIFOResponse>(LogisticsInActivity.this);
            webServices.getCurrentFIFO(Utilities.getBaseURL(LogisticsInActivity.this), WebServices.ApiType.currentFIFO);

        } else {
            showToast(LogisticsInActivity.this, getResources().getString(R.string.err_msg_nointernet));

        }

    }

    public void callPrintConfirmationInAPI(String refrigeraterNumber, String rackNumber, String fifoNumber, int materialId) {
        PrintConfirmation printConfirmation = new PrintConfirmation(refrigeraterNumber, rackNumber, materialId, fifoNumber);
        if (Utilities.isConnectedToInternet(this)) {
            toggleVisibility(true, mProgressBar);
            WebServices<GenericResponse> webServices = new WebServices<GenericResponse>(LogisticsInActivity.this);
            webServices.printConfirmationIn(Utilities.getBaseURL(LogisticsInActivity.this), WebServices.ApiType.printConfirmationIn, printConfirmation);

        } else {
            showToast(LogisticsInActivity.this, getResources().getString(R.string.err_msg_nointernet));

        }

    }


    /************************************
     END of API calling Section
     ******************************************************************************************************************************/

    /*****************************
     API Response handling
     *******************************************************************************************************************************/
    @Override
    public void onResponse(Object response, WebServices.ApiType URL, boolean isSucces, int code) {
        switch (URL) {
            case materialIn:
                toggleVisibility(false, mProgressBar);
                if (isSucces) {
                    GenericResponse genericResponse = (GenericResponse) response;
                    if (genericResponse != null) {
                        if (genericResponse.getMessage() != null && genericResponse.getStatus() != null) {
                            if (genericResponse.getStatus().equalsIgnoreCase("success")) {
                                //Success case

                                mCurrentFifoNumberTextView.setText(genericResponse.getMessage());

                                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();

                                BitMatrix bitMatrix = null;
                                try {

                                    bitMatrix = multiFormatWriter.encode(genericResponse.getMessage(), BarcodeFormat.QR_CODE, 1100, 310);
                                    bitmap = barcodeEncoder.createBitmap(bitMatrix);

                                } catch (WriterException e) {
                                    e.printStackTrace();
                                }

                                mBitmapImage.setImageBitmap(bitmap);

                                showPrintLayout();

                                //showSuccessLayout(genericResponse.getMessage());

                            } else {
                                //Failure case

                                bitmap = null;
                                mBitmapImage.setImageBitmap(bitmap);
                                mCurrentFifoNumberTextView.setText("");

                                hidePrintLayout();
                                //showFailureLayout(genericResponse.getMessage());
                                showSnackBar(LogisticsInActivity.this, genericResponse.getMessage());
                            }

                        } else {
                            bitmap = null;
                            mBitmapImage.setImageBitmap(bitmap);
                            mCurrentFifoNumberTextView.setText("");

                            hidePrintLayout();
                            showSnackBar(LogisticsInActivity.this, "Something went wrong please try again");
                        }

                    } else {
                        bitmap = null;
                        mBitmapImage.setImageBitmap(bitmap);
                        mCurrentFifoNumberTextView.setText("");

                        hidePrintLayout();
                        showSnackBar(LogisticsInActivity.this, "Server is busy");
                    }

                } else {
                    //API call failed
                    bitmap = null;
                    mBitmapImage.setImageBitmap(bitmap);
                    mCurrentFifoNumberTextView.setText("");

                    hidePrintLayout();
                    showSnackBar(LogisticsInActivity.this, "Server Timeout");
                }
                break;
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
                                showSnackBar(LogisticsInActivity.this, "FIFO numbers are empty");
                            }

                           /* if (genericResponse.getStatus().equalsIgnoreCase("success")) {
                                //Success case
                                showSuccessLayout(genericResponse.getMessage());

                            } else {
                                //Failure case
                                showFailureLayout(genericResponse.getMessage());
                            }*/

                        } else {
                            showSnackBar(LogisticsInActivity.this, "Something went wrong please try again");
                        }

                    } else {
                        showSnackBar(LogisticsInActivity.this, "Server is busy");
                    }

                } else {
                    //API call failed
                    showSnackBar(LogisticsInActivity.this, "Server Timeout");
                }
                break;
            case printConfirmationIn:
                toggleVisibility(false, mProgressBar);
                if (isSucces) {
                    GenericResponse genericResponse = (GenericResponse) response;
                    if (genericResponse != null) {
                        if (genericResponse.getMessage() != null && genericResponse.getStatus() != null) {
                            if (genericResponse.getStatus().equalsIgnoreCase("success")) {
                                //Success case
                                // showSuccessLayout(genericResponse.getMessage());
                                // showToast(LogisticsInActivity.this,"print success");
                                showReprintLayout();

                            } else {
                                //Failure case
                                //showFailureLayout(genericResponse.getMessage());
                                showToast(LogisticsInActivity.this, genericResponse.getMessage());
                            }

                        } else {
                            showSnackBar(LogisticsInActivity.this, "Something went wrong please try again");
                        }

                    } else {
                        showSnackBar(LogisticsInActivity.this, "Server is busy");
                    }

                } else {
                    //API call failed
                    showSnackBar(LogisticsInActivity.this, "Server Timeout");
                }
                break;


        }

    }

    /*******************************
     END of API Response handling
     *******************************************************************************************************************************/

    private void setCurrentFIFONumbersToViews(List<Currentfifo> mList) {
        for (Currentfifo currentfifo : mList) {
            int matID = currentfifo.getMaterialId();

            String materialName = materialTypeNames[matID];
            String currentFIFO = currentfifo.getPresentfifonumber();
        }
    }

    private void showSuccessLayout(String successMessage) {
       /* toggleVisibility(false, mProgressBar);
        toggleVisibility(true, mSuccessLayout);
        mSuccessMessage.setText(successMessage.trim());

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideSuccessAndfailureLayouts();
                mHandler.removeCallbacksAndMessages(null);
            }
        }, 60000);*/

    }

    private void showFailureLayout(String failureMessage) {
       /* toggleVisibility(false, mProgressBar);
        toggleVisibility(true, mFailureLayout);
        mFailureMessage.setText(failureMessage.trim());

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideSuccessAndfailureLayouts();
                mHandler.removeCallbacksAndMessages(null);
            }
        }, 60000);*/

    }

    private void hideSuccessAndfailureLayouts() {
       /* toggleVisibility(false, mProgressBar);
        mHandler.removeCallbacksAndMessages(null);
        toggleVisibility(false, mSuccessLayout, mFailureLayout);*/
    }

    @Override
    public void onBackPressed() {

        hidePrintLayout();
        hideKeyBoard();
        super.onBackPressed();
    }
}
