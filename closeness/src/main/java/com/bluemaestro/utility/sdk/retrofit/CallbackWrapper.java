package com.bluemaestro.utility.sdk.retrofit;

import android.content.Context;
import android.util.Log;
import android.view.View;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;

import io.reactivex.observers.DisposableObserver;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;

public abstract class CallbackWrapper<T extends Response> extends DisposableObserver<T>
{

    private static final String TAG = "CallbackWrapper";
    private final Context context;
    //BaseView is just a reference of a View in MVP
    private WeakReference<View> weakReference;

    //    public CallbackWrapper(View view) {
    //        this.weakReference = new WeakReference<>(view);
    //    }
    public CallbackWrapper(Context context)
    {
        this.context = context;
//        ((BaseActivity) context).showProgressDialog();
    }

    protected abstract void onSuccess(T t);

    @Override
    public void onNext(T t)
    {
        //You can return StatusCodes of different cases from your API and handle it here. I usually include these cases on BaseResponse
        // and iherit it from every Response
//        switch(t.raw().code()) {
        //            case HttpURLConnection.HTTP_OK:
        //                onSuccess(t);
        //            break;
        //            default:
        //                Log.e(TAG, t.toString());
        //                error();
        //        }
                onSuccess(t);
    }

    @Override
    public void onError(Throwable e)
    {
        //        View view = weakReference.get();
        if(e instanceof HttpException)
        {
            ResponseBody responseBody = ((HttpException) e).response().errorBody();
            //            view.onUnknownError(getErrorMessage(responseBody));
        } else if(e instanceof SocketTimeoutException)
        {
            //            view.onTimeout();
        } else if(e instanceof IOException)
        {
            //            view.onNetworkError();
        } else
        {
            //            view.onUnknownError(e.getMessage());
        }
        e.printStackTrace();
        error();
    }

    private void error()
    {
//        ((BaseActivity) context).hideProgressDialog();
//        ((BaseActivity) context).webServiceConnectionFailed();
        Log.d(TAG, "error");
    }

    @Override
    public void onComplete()
    {
//        ((BaseActivity) context).hideProgressDialog();
    }

    private String getErrorMessage(ResponseBody responseBody)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(responseBody.string());
            return jsonObject.getString("message");
        } catch(Exception e)
        {
            return e.getMessage();
        }
    }
}