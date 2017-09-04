package com.sogukj.pe.service;

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

/**
 * Created by Mars on 2016/2/25.
 */
public class DownLoadService {

    private static final String TAG = DownLoadService.class.getSimpleName();

    ApiService apiService;

    public interface ApiService {
        @GET("{path}")
        Call<ResponseBody> getFile(@Path("path") String path);
    }

    private class LoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request newRequest = chain.request();
            newRequest = chain.request().newBuilder().url(newRequest.url().toString().replaceAll("%2F", "/")).build();
//            Trace.e(TAG, "url = " + newRequest.url().toString());
            Response response = chain.proceed(newRequest);
//            if (response != null)
//                Trace.e(TAG, "code = " + response.code());
            return response;
        }
    }

    public DownLoadService(String host) {
//        Trace.e(TAG, "host = " + host);
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
        Trace.INSTANCE.e(TAG, "path = " + path.substring(1));
        return apiService.getFile(path.substring(1));
    }
}
