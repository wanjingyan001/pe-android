package com.sogukj.pe.service;

import android.util.Log;

import com.sogukj.pe.util.Trace;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Url;

public class DownLoadService {

    private static final String TAG = DownLoadService.class.getSimpleName();

    ApiService apiService;

    public interface ApiService {
        @GET()
        Call<ResponseBody> getFile(@Url String path);
    }

    private class LoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request newRequest = chain.request();
//            Log.d(TAG, "url = " + newRequest.url().toString());
            //            if (response != null)
//                Log.d(TAG, "code = " + response.code());
            return chain.proceed(newRequest);
        }
    }

    public DownLoadService(String host) {
//        Log.d(TAG, "host = " + host);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new LoggingInterceptor())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(host)
                .client(client)
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public Call<ResponseBody> getFile(String path) {
//        Trace.INSTANCE.e(TAG, "path = " + path.substring(1));
        return apiService.getFile(path.substring(1));
    }
}
