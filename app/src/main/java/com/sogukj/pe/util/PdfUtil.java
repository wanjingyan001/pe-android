package com.sogukj.pe.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.sogukj.pe.BuildConfig;
import com.sogukj.pe.service.DownLoadService;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PdfUtil {

    public static void opendPdf(Context context, File file) {
        Uri path = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setDataAndType(path, "application/pdf");
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", file);
            intent.setDataAndType(contentUri, "application/pdf");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context,
                    "没有找到可以打开pdf类型的应用!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static void loadPdf(final Context context, String urls) {
        URL url = null;

        try {
            url = new URL(urls);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (url == null) {
            return;
        }

        final String filename = FileUtil.getExternalFilesDir(context) + url.getPath().hashCode() + ".pdf";

        if (FileUtil.isFileExist(filename)) {
            opendPdf(context, new File(filename));
        } else {
            Toast.makeText(context, "正在下载文件!", Toast.LENGTH_SHORT).show();
            new DownLoadService("http://" + url.getHost()).getFile(url.getPath()).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.code() == 200) {
                            File file = FileUtil.byte2File(response.body().bytes(), filename);
                            opendPdf(context, file);
                        } else {
                            Toast.makeText(context, "下载失败!", Toast.LENGTH_SHORT).show();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(context, "连接失败!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
