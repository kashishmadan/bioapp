package com.example.stress.retrofit;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okio.Buffer;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient
{

    private static Retrofit retrofit = null;
    private static Retrofit retrofitWithToken = null;

    public static GsonBuilder createBasicGsonBuilder()
    {
        final GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();

        return builder;
    }

    public static Gson createBasicGson()
    {
        final GsonBuilder builder = new GsonBuilder();

        builder.excludeFieldsWithoutExposeAnnotation();
        builder.serializeNulls();

        return builder.create();
    }

//    public static Gson createGson()
//    {
//        final GsonBuilder builder = new GsonBuilder();
////        builder.registerTypeAdapter(AppointmentAddress.class, new AppointmentAddressTypeAdapter());
////        builder.registerTypeAdapter(MyAddress.class, new MyAddressTypeAdapter());
//        builder.registerTypeAdapter(MyDate.class, new MyDateTypeAdapter());
//
//        builder.excludeFieldsWithoutExposeAnnotation();
//        builder.serializeNulls();
//        return builder.create();
//    }

    public static Gson createFullGson()
    {
        final GsonBuilder builder = new GsonBuilder();

//        builder.registerTypeAdapter(User.class, new UserTypeAdapter());
//        builder.registerTypeAdapter(AppointmentAddress.class, new AppointmentAddressTypeAdapter());
//        builder.registerTypeAdapter(MyAddress.class, new MyAddressTypeAdapter());
//        builder.registerTypeAdapter(MyDate.class, new MyDateTypeAdapter());
//        builder.registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter());
//        builder.registerTypeAdapter(MyLocalDate.class, new MyLocalDateTypeAdapter());
//        builder.registerTypeAdapter(Appointment.class, new AppointmentTypeAdapter());

//        builder.serializeNulls();
        builder.excludeFieldsWithoutExposeAnnotation();

        return builder.create();
    }

    private static OkHttpClient.Builder getOkHttpClientBuilder(Context context){
        return new OkHttpClient.Builder();
//                .addInterceptor(new ChuckInterceptor(context));
//                .addInterceptor(chain -> {
//                    Request request = chain.request();
//                    if(request.body().getClass() == FormBody.class)
//                    {
//
//                        FormBody.Builder bodyBuilder = new FormBody.Builder();
//                        FormBody b = (FormBody) request.body();
//                        for(int i = 0; i < b.size(); i++)
//                        {
//                            bodyBuilder.addEncoded(b.name(i), b.value(i));
//                        }
//                        bodyBuilder.addEncoded(SOURCE_TYPE_FIELD, Constants.SOURCE_TYPE);
//                        request = request.newBuilder().post(bodyBuilder.build()).build();
//
//                    } else
//                    {
//
//                        Request.Builder requestBuilder = request.newBuilder();
//                        String postBodyString = bodyToString(request.body());
//                        if(postBodyString.length() > 0)
//                        {
//                            postBodyString =
//                                    postBodyString.substring(0, postBodyString.length() - 1) +
//                                            fieldToString(SOURCE_TYPE_FIELD, Constants.SOURCE_TYPE) +
//                                            postBodyString.substring(postBodyString.length() - 1);
//                        }
//                        request = requestBuilder
//                                .post(RequestBody
//                                        .create(MediaType.parse("application/json"), postBodyString))
//                                .build();
//                    }
//                    return chain.proceed(request);
//                });
    }

    public static Retrofit getClient(Context context, String baseUrl)
    {
        if(retrofit == null)
        {

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(getOkHttpClientBuilder(context).build())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(createFullGson()))
                    .build();
        }
        return retrofit;
    }

    public static String fieldToString(String name, String value)
    {
        return ",\"" + name + "\":\"" + value + "\"";
    }

    public static String bodyToString(final RequestBody request)
    {
        try
        {
            final RequestBody copy = request;
            final Buffer buffer = new Buffer();
            if(copy != null)
            {
                copy.writeTo(buffer);
            } else
            {
                return "";
            }
            return buffer.readUtf8();
        } catch(final IOException e)
        {
            return "did not work";
        }
    }
}


