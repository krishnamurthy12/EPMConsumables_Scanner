package com.vvt.epm.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.vvt.epm.MainActivity;
import com.vvt.epm.R;
import com.vvt.epm.api_requests.ReprintDelete;
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
import static com.vvt.epm.utils.Utilities.toggleVisibility;

public class AdminHomeActivity extends AppCompatActivity implements View.OnClickListener, OnResponseListener {

    Button mDeleteSticker,mRegenerateSticker,mStep;

    LinearLayout mDeleteLayout,mReprintLayout;
    TextView mConfirmDelete,mConfirmReprint,mCancelDelete,mCancelReprint;
    EditText mStickerNumberToDelete,mStickerNumberToRegenerate;

    ImageView mClearDeleteText,mClearRegenerateText;
    ProgressBar mProgressBar;

    Toolbar toolbar;
    Toast mToast;
    Snackbar snackbar;

    public static String IpAddress = "";
    public String url = "";

    printer m_printer = new printer();
    Bitmap bitmap;

    ImageView mBitmapImage;

    String eneteredStickerNumber="";

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
        setContentView(R.layout.activity_admin_home);
        initializeViews();
    }

    private void initializeViews()
    {
        toolbar= (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mProgressBar= (ProgressBar) findViewById(R.id.vP_aah_progress_bar);
        mProgressBar.setVisibility(View.GONE);

        mDeleteSticker= (Button) findViewById(R.id.btn_delete_sticker);
        mRegenerateSticker= (Button) findViewById(R.id.btn_regenerete_sticker);
        mStep= (Button) findViewById(R.id.btn_step);

        mDeleteLayout= (LinearLayout) findViewById(R.id.vL_delete_layout);
        mReprintLayout= (LinearLayout) findViewById(R.id.vL_regenerate_layout);

        mDeleteLayout.setVisibility(View.GONE);
        mReprintLayout.setVisibility(View.GONE);

        mStickerNumberToDelete= (EditText) findViewById(R.id.vE_sticker_to_delete);
        mStickerNumberToRegenerate= (EditText) findViewById(R.id.vE_sticker_to_reprint);

        mConfirmDelete= (TextView) findViewById(R.id.vT_delete_confirm);
        mCancelDelete= (TextView) findViewById(R.id.vT_delete_cancel);

        mCancelReprint= (TextView) findViewById(R.id.vT_regenerate_cancel);
        mConfirmReprint= (TextView) findViewById(R.id.vT_regenerate_confirm);

        mBitmapImage = (ImageView) findViewById(R.id.vI_bitmap_img);

        mClearDeleteText= (ImageView) findViewById(R.id.vI_clear_image1);
        mClearRegenerateText= (ImageView) findViewById(R.id.vI_clear_image2);

        mClearRegenerateText.setOnClickListener(this);
        mClearDeleteText.setOnClickListener(this);

        mConfirmReprint.setOnClickListener(this);
        mCancelReprint.setOnClickListener(this);
        mConfirmDelete.setOnClickListener(this);
        mCancelDelete.setOnClickListener(this);


        mDeleteSticker.setOnClickListener(this);
        mRegenerateSticker.setOnClickListener(this);
        mStep.setOnClickListener(this);

        hideLayouts();

    }

    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.btn_delete_sticker:
                showDeleteLayout();
                break;

            case R.id.btn_regenerete_sticker:
                showRegenerateLayout();
                break;

            case R.id.btn_step:
                m_printer.Step((byte) 0x3C);
                break;

            case R.id.vT_delete_confirm:
                hideKeyBoard();
                delete();
                break;

            case R.id.vT_delete_cancel:
                hideKeyBoard();
                hideLayouts();
                break;

            case R.id.vT_regenerate_confirm:
                hideKeyBoard();
                regenerate();
                break;

            case R.id.vT_regenerate_cancel:
                hideKeyBoard();
                hideLayouts();
                break;

            case R.id.vI_clear_image1:
                hideKeyBoard();
                mStickerNumberToDelete.setText("");
                break;

            case R.id.vI_clear_image2:
                hideKeyBoard();
                mStickerNumberToRegenerate.setText("");
                break;
        }

    }

    private void delete() {

        if(!TextUtils.isEmpty(mStickerNumberToDelete.getText().toString()) || mStickerNumberToDelete.getText().toString().length()<5)
        {
            String[] stringArray=mStickerNumberToDelete.getText().toString().split("-");
            String refNumber="";
            String rack="";
            String fifoNumber="";
            int type=3;
            try
            {
                refNumber=stringArray[0];
                rack=stringArray[1];
                type= Integer.parseInt(stringArray[2]);
                fifoNumber=stringArray[3];


            }catch (ArrayIndexOutOfBoundsException e)
            {
                e.printStackTrace();
            }
            ReprintDelete reprintDelete = new ReprintDelete(refNumber, rack, type, fifoNumber);
            if(!mProgressBar.isShown())
            {
                if(Utilities.isConnectedToInternet(this))
                {
                    callDeleteStickerAPI(reprintDelete);
                }
                else
                {
                    showSnackBar(this,getResources().getString(R.string.err_msg_nointernet));
                }
            }

        }
        else {
            showSnackBar(this,"Invalid input");
        }

    }

    private void regenerate() {
        if(!TextUtils.isEmpty(mStickerNumberToDelete.getText().toString()) || mStickerNumberToDelete.getText().toString().length()<5)
        {
            String[] stringArray=mStickerNumberToDelete.getText().toString().split("-");
            String refNumber="";
            String rack="";
            String fifoNumber="";
            int type=3;
            try
            {
                refNumber=stringArray[0];
                rack=stringArray[1];
                type= Integer.parseInt(stringArray[2]);
                fifoNumber=stringArray[3];


            }catch (ArrayIndexOutOfBoundsException e)
            {
                e.printStackTrace();
            }
            ReprintDelete reprintDelete = new ReprintDelete(refNumber, rack, type, fifoNumber);
            if(!mProgressBar.isShown())
            {
                if(Utilities.isConnectedToInternet(this))
                {
                    callRegenerateStickerAPI(reprintDelete);
                }
                else
                {
                    showSnackBar(this,getResources().getString(R.string.err_msg_nointernet));
                }
            }

        }
        else {
            showSnackBar(this,"Invalid input");
        }
    }

    public void callRegenerateStickerAPI(ReprintDelete reprintDelete) {

        if (Utilities.isConnectedToInternet(this)) {
            toggleVisibility(true,mProgressBar);
            WebServices<GenericResponse> webServices = new WebServices<GenericResponse>(AdminHomeActivity.this);
            webServices.reprintSticker(Utilities.getBaseURL(AdminHomeActivity.this), WebServices.ApiType.rePrintSticker, reprintDelete);

        } else {
            showToast(AdminHomeActivity.this, getResources().getString(R.string.err_msg_nointernet));

        }

    }

    public void callDeleteStickerAPI(ReprintDelete reprintDelete) {
        if (Utilities.isConnectedToInternet(this)) {
            toggleVisibility(true,mProgressBar);
            WebServices<GenericResponse> webServices = new WebServices<GenericResponse>(AdminHomeActivity.this);
            webServices.deleteSticker(Utilities.getBaseURL(AdminHomeActivity.this), WebServices.ApiType.deleteSticker, reprintDelete);

        } else {
            showToast(AdminHomeActivity.this, getResources().getString(R.string.err_msg_nointernet));

        }

    }

    private void printSticker(String responseText) {

        if (bitmap != null) {
            m_printer.PrintBitmap(bitmap);
            m_printer.PrintLineEnd();
        }
        m_printer.PrintLineInit(25);
        m_printer.PrintLineString(responseText, 32, printer.PrintType.Centering, true);//160
        m_printer.PrintLineEnd();

        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
        String datetime = dateformat.format(c.getTime());

        //Log.d("dateandtime","SimpleDateFormat=>"+datetime);

        m_printer.PrintLineInit(20);
        m_printer.PrintLineString(datetime+"(Re)", 22, printer.PrintType.Centering, true);//160
        m_printer.PrintLineEnd();

        m_printer.PrintLineInit(125);
        m_printer.PrintLineString("\r\n", 40, printer.PrintType.Centering, true);//160
        m_printer.PrintLineEnd();


    }
    private void hideLayouts() {
        mDeleteLayout.setVisibility(View.GONE);
        mReprintLayout.setVisibility(View.GONE);
    }

    private void showRegenerateLayout() {

        mStickerNumberToRegenerate.requestFocus();
        mStickerNumberToRegenerate.isFocusable();
        mDeleteLayout.setVisibility(View.GONE);
        mReprintLayout.setVisibility(View.VISIBLE);
    }

    private void showDeleteLayout() {

        mStickerNumberToDelete.requestFocus();
        mStickerNumberToDelete.isFocusable();

        mDeleteLayout.setVisibility(View.VISIBLE);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.logout, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_admin_logout) {

            Intent intent=new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    /*****************************
     API Response handling
     *******************************************************************************************************************************/
    @Override
    public void onResponse(Object response, WebServices.ApiType URL, boolean isSucces, int code) {
        switch (URL) {

            case printConfirmationIn:
                toggleVisibility(false,mProgressBar);
                if (isSucces) {
                    GenericResponse genericResponse = (GenericResponse) response;
                    if (genericResponse != null) {
                        if (genericResponse.getMessage() != null && genericResponse.getStatus() != null) {
                            if (genericResponse.getStatus().equalsIgnoreCase("success")) {
                                //Success case
                                showToast(this,genericResponse.getMessage());
                            } else {
                                //Failure case
                                showToast(this,genericResponse.getMessage());

                            }

                        } else {
                            showSnackBar(AdminHomeActivity.this, "Something went wrong please try again");
                        }

                    } else {
                        showSnackBar(AdminHomeActivity.this, "Server is busy");
                    }

                } else {
                    //API call failed
                    showSnackBar(AdminHomeActivity.this, "Server Timeout");
                }
                break;
            case rePrintSticker:
                 toggleVisibility(false,mProgressBar);
                if (isSucces) {
                    GenericResponse genericResponse = (GenericResponse) response;
                    if (genericResponse != null) {
                        if (genericResponse.getMessage() != null && genericResponse.getStatus() != null) {
                            if (genericResponse.getStatus().equalsIgnoreCase("success")) {
                                //Success case
                                if(genericResponse.getMessage().contains("-") || genericResponse.getMessage().startsWith("RF"))
                                {
                                    hideLayouts();

                                    MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                                    try {

                                        //BitMatrix bitMatrix = multiFormatWriter.encode(enteredStickerNumber, BarcodeFormat.QR_CODE, 1200, 400);
                                        BitMatrix bitMatrix = multiFormatWriter.encode(genericResponse.getMessage(), BarcodeFormat.QR_CODE, 1100, 310);
                                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                                        bitmap = barcodeEncoder.createBitmap(bitMatrix);

                                        mBitmapImage.setImageBitmap(bitmap);
                                        printSticker(genericResponse.getMessage());

                                        //  qrCode.setImageBitmap(bitmap);
                                    } catch (WriterException e) {
                                        e.printStackTrace();
                                    }

                                }


                            } else {
                                //Failure case
                                showToast(AdminHomeActivity.this,genericResponse.getMessage());
                            }

                        } else {
                            showSnackBar(AdminHomeActivity.this, "Something went wrong please try again");
                        }

                    } else {
                        showSnackBar(AdminHomeActivity.this, "Server is busy");
                    }

                } else {
                    //API call failed
                    showSnackBar(AdminHomeActivity.this, "Server Timeout");
                }
                break;
            case deleteSticker:
                // toggleVisibility(false,mProgressBar);
                if (isSucces) {
                    GenericResponse genericResponse = (GenericResponse) response;
                    if (genericResponse != null) {
                        if (genericResponse.getMessage() != null && genericResponse.getStatus() != null) {
                            if (genericResponse.getStatus().equalsIgnoreCase("success")) {
                                //Success case
                                hideLayouts();
                                showToast(AdminHomeActivity.this,genericResponse.getMessage());


                            } else {
                                //Failure case
                                showToast(AdminHomeActivity.this,genericResponse.getMessage());
                            }

                        } else {
                            showSnackBar(AdminHomeActivity.this, "Something went wrong please try again");
                        }

                    } else {
                        showSnackBar(AdminHomeActivity.this, "Server is busy");
                    }

                } else {
                    //API call failed
                    showSnackBar(AdminHomeActivity.this, "Server Timeout");
                }
                break;

        }

    }

    /*******************************
     END of API Response handling
     *******************************************************************************************************************************/
    @Override
    public void onBackPressed() {

        Intent intent=new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        super.onBackPressed();
    }
}
