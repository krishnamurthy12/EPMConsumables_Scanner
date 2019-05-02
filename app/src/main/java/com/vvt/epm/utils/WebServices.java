package com.vvt.epm.utils;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Adapter;

import com.vvt.epm.api_requests.AdminLogin;
import com.vvt.epm.api_requests.MaterialIn;
import com.vvt.epm.api_requests.MaterialOutPallets;
import com.vvt.epm.api_requests.MaterialOutSheets;
import com.vvt.epm.api_requests.PrintConfirmation;
import com.vvt.epm.api_requests.ReprintDelete;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by Krish on 12-11-2018.
 */

public class WebServices<T> {
    T t;
    Call<T> call=null;
    public T getT() {
        return t;
    }

    public void setT(T t) {

        this.t = t;
    }
    ApiType apiTypeVariable;
    Context context;
    OnResponseListener<T> onResponseListner;
    private static OkHttpClient.Builder builder;

    public enum ApiType {
        materialIn,materialOutPallets,getMaterialOutSheets,printConfirmationIn,printConfirmationOut,currentFIFO,logIn,logout,rePrintSticker,deleteSticker
    }

    public WebServices(OnResponseListener<T> onResponseListner) {
        this.onResponseListner = onResponseListner;

        if (onResponseListner instanceof Activity) {
            this.context = (Context) onResponseListner;
        } else if (onResponseListner instanceof IntentService) {
            this.context = (Context) onResponseListner;
        } else if (onResponseListner instanceof android.app.DialogFragment) {
            android.app.DialogFragment dialogFragment = (android.app.DialogFragment) onResponseListner;
            this.context = dialogFragment.getActivity();
        }else if (onResponseListner instanceof android.app.Fragment) {
            android.app.Fragment fragment = (android.app.Fragment) onResponseListner;
            this.context = fragment.getActivity();
        }
         else if (onResponseListner instanceof Adapter) {

            this.context = (Context) onResponseListner;
        }
        else if (onResponseListner instanceof Adapter) {
            this.context = (Context) onResponseListner;
        }
            else {
            android.support.v4.app.Fragment fragment = (android.support.v4.app.Fragment) onResponseListner;
            this.context = fragment.getActivity();
        }

        builder = getHttpClient();
    }

    public WebServices(Context context, OnResponseListener<T> onResponseListner) {
        this.onResponseListner = onResponseListner;
        this.context = context;
        builder = getHttpClient();
    }


    public OkHttpClient.Builder getHttpClient() {

        if (builder == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient.Builder client = new OkHttpClient.Builder();
            client.connectTimeout(10000, TimeUnit.SECONDS);
            client.readTimeout(10000, TimeUnit.SECONDS).build();
            client.addInterceptor(loggingInterceptor);
            /*to pass header information with request*/
           /* client.addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request request = chain.request().newBuilder().addHeader("Content-Type", "application/json").build();
                    return_to_production chain.proceed(request);
                }
            });*/

            return client;
        }
        return builder;
    }

    private Retrofit getRetrofitClient(String api)
    {
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl(api)
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit;
    }

    public void materialsIn(@NonNull String api, ApiType apiTypes, MaterialIn materialIn)
    {
        apiTypeVariable = apiTypes;
        Retrofit retrofit=getRetrofitClient(api);

        EPMConsumablesAPI digitalFIFOAPI=retrofit.create(EPMConsumablesAPI.class);
        call=(Call<T>)digitalFIFOAPI.materialIn(materialIn);
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                t=(T)response.body();
                onResponseListner.onResponse(t, apiTypeVariable, true,response.code());
                //Toast.makeText(context, "Success"+response.code(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                //Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show();
                onResponseListner.onResponse(null, apiTypeVariable, false,0);

            }
        });
    }

    public void materialsOutPallets(String api, ApiType apiTypes, MaterialOutPallets materialOut)
    {
        apiTypeVariable = apiTypes;
        Retrofit retrofit=getRetrofitClient(api);

        EPMConsumablesAPI digitalFIFOAPI=retrofit.create(EPMConsumablesAPI.class);
        call=(Call<T>)digitalFIFOAPI.materialOutPallets(materialOut);
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                t=(T)response.body();
                onResponseListner.onResponse(t, apiTypeVariable, true,response.code());
                //Toast.makeText(context, "Success"+response.code(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                //Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show();
                onResponseListner.onResponse(null, apiTypeVariable, false,0);
            }
        });
    }

    public void materialsOutSheets(String api, ApiType apiTypes, MaterialOutSheets materialOut)
    {
        apiTypeVariable = apiTypes;
        Retrofit retrofit=getRetrofitClient(api);

        EPMConsumablesAPI digitalFIFOAPI=retrofit.create(EPMConsumablesAPI.class);
        call=(Call<T>)digitalFIFOAPI.materialOutSheets(materialOut);
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                t=(T)response.body();
                onResponseListner.onResponse(t, apiTypeVariable, true,response.code());
                //Toast.makeText(context, "Success"+response.code(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                //Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show();
                onResponseListner.onResponse(null, apiTypeVariable, false,0);
            }
        });
    }

    public void getCurrentFIFO(String api, ApiType apiTypes)
    {
        apiTypeVariable = apiTypes;
        Retrofit retrofit=getRetrofitClient(api);

        EPMConsumablesAPI digitalFIFOAPI=retrofit.create(EPMConsumablesAPI.class);
        call=(Call<T>)digitalFIFOAPI.currentFIFO();
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                t=(T)response.body();
                onResponseListner.onResponse(t, apiTypeVariable, true,response.code());
                //Toast.makeText(context, "Success"+response.code(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                //Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show();
                onResponseListner.onResponse(null, apiTypeVariable, false,0);
            }
        });
    }
    public void printConfirmationIn(String api, ApiType apiTypes, PrintConfirmation printConfirmation)
    {
        apiTypeVariable = apiTypes;
        Retrofit retrofit=getRetrofitClient(api);

        EPMConsumablesAPI digitalFIFOAPI=retrofit.create(EPMConsumablesAPI.class);
        call=(Call<T>)digitalFIFOAPI.printConfirmationIn(printConfirmation);
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                t=(T)response.body();
                onResponseListner.onResponse(t, apiTypeVariable, true,response.code());
                //Toast.makeText(context, "Success"+response.code(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                //Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show();
                onResponseListner.onResponse(null, apiTypeVariable, false,0);
            }
        });
    }
    public void printConfirmationOut(String api, ApiType apiTypes, PrintConfirmation printConfirmation)
    {
        apiTypeVariable = apiTypes;
        Retrofit retrofit=getRetrofitClient(api);

        EPMConsumablesAPI digitalFIFOAPI=retrofit.create(EPMConsumablesAPI.class);
        call=(Call<T>)digitalFIFOAPI.printConfirmationOut(printConfirmation);
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                t=(T)response.body();
                onResponseListner.onResponse(t, apiTypeVariable, true,response.code());
                //Toast.makeText(context, "Success"+response.code(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                //Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show();
                onResponseListner.onResponse(null, apiTypeVariable, false,0);
            }
        });
    }

    public void adminLogIn(String api, ApiType apiTypes, AdminLogin logIn)
    {
        apiTypeVariable = apiTypes;
        Retrofit retrofit=getRetrofitClient(api);

        EPMConsumablesAPI digitalFIFOAPI=retrofit.create(EPMConsumablesAPI.class);
        call=(Call<T>)digitalFIFOAPI.adminLogIn(logIn);
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                t=(T)response.body();
                onResponseListner.onResponse(t, apiTypeVariable, true,response.code());
                //Toast.makeText(context, "Success"+response.code(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                //Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show();
                onResponseListner.onResponse(null, apiTypeVariable, false, 0);

            }
        });
    }

    public void reprintSticker(String api, ApiType apiTypes, ReprintDelete reprintDelete)
    {
        apiTypeVariable = apiTypes;
        Retrofit retrofit=getRetrofitClient(api);

        EPMConsumablesAPI digitalFIFOAPI=retrofit.create(EPMConsumablesAPI.class);
        call=(Call<T>)digitalFIFOAPI.adminReprint(reprintDelete);
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                t=(T)response.body();
                onResponseListner.onResponse(t, apiTypeVariable, true,response.code());
                //Toast.makeText(context, "Success"+response.code(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                //Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show();
                onResponseListner.onResponse(null, apiTypeVariable, false,0);

            }
        });
    }

    public void deleteSticker(String api, ApiType apiTypes, ReprintDelete reprintDelete)
    {
        apiTypeVariable = apiTypes;
        Retrofit retrofit=getRetrofitClient(api);

        EPMConsumablesAPI digitalFIFOAPI=retrofit.create(EPMConsumablesAPI.class);
        call=(Call<T>)digitalFIFOAPI.adminDelete(reprintDelete);
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                t=(T)response.body();
                onResponseListner.onResponse(t, apiTypeVariable, true,response.code());
                //Toast.makeText(context, "Success"+response.code(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                //Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show();
                onResponseListner.onResponse(null, apiTypeVariable, false,0);

            }
        });
    }


}
