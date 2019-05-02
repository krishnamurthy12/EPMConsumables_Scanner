package com.vvt.epm.utils;

import com.vvt.epm.api_requests.AdminLogin;
import com.vvt.epm.api_requests.MaterialIn;
import com.vvt.epm.api_requests.MaterialOutPallets;
import com.vvt.epm.api_requests.MaterialOutSheets;
import com.vvt.epm.api_requests.PrintConfirmation;
import com.vvt.epm.api_requests.ReprintDelete;
import com.vvt.epm.api_responses.currentfifo.CurrentFIFOResponse;
import com.vvt.epm.api_responses.genericresponse.GenericResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface EPMConsumablesAPI {

    @Headers("Content-Type: application/json")
    @POST("epm/material-in/")
    Call<GenericResponse> materialIn(@Body MaterialIn materialIn);

    @Headers("Content-Type: application/json")
    @POST("epm/material-out/")
    Call<GenericResponse> materialOutPallets(@Body MaterialOutPallets materialOutPallets);

    @Headers("Content-Type: application/json")
    @POST("epm/material-out-sheet/")
    Call<GenericResponse> materialOutSheets(@Body MaterialOutSheets materialOutSheets);

    @Headers("Content-Type: application/json")
    @GET("present/fifoNumber/")
    Call<CurrentFIFOResponse> currentFIFO();

    @Headers("Content-Type: application/json")
    @POST("fifoprinted/epmprint-first")
    Call<GenericResponse> printConfirmationIn(@Body PrintConfirmation printConfirmation);

    @Headers("Content-Type: application/json")
    @POST("fifoprinted/epmprint-second")
    Call<GenericResponse> printConfirmationOut(@Body PrintConfirmation printConfirmation);

    @Headers("Content-Type: application/json")
    @POST("EPM_admin/login/")
    Call<GenericResponse> adminLogIn(@Body AdminLogin login);

    @Headers("Content-Type: application/json")
    @POST("EPM_admin/reprintfifo/")
    Call<GenericResponse> adminReprint(@Body ReprintDelete reprintDelete);

    @Headers("Content-Type: application/json")
    @POST("EPM_admin/deletefifo/")
    Call<GenericResponse> adminDelete(@Body ReprintDelete reprintDelete);

}
