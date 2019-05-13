package com.example.stress.retrofit;

import android.content.Context;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class ApiUtils
{

    //    public static final String BASE_URL = Utils.DATA_BASE_URL;
    public static String TOKEN = null;

    public static Webservices create(Context context)
    {
        //        ((BaseActivity) context).showProgressDialog();
        String url = PreferenceManager.getDefaultSharedPreferences(context).getString("server_url_main", "https://ws.socialthermo.ovh");
        if(url.equals("")) {
            Toast.makeText(context, "Enter a valid url", Toast.LENGTH_LONG).show();
            return null;
        }
        return RetrofitClient.getClient(context, url).create(Webservices.class);
    }

    //    public static Webservices createWithToken(Context context, String token)
    //    {
    //        return RetrofitClient.getClientWithToken(context, BASE_URL, token).create(Webservices.class);
    //    }


}