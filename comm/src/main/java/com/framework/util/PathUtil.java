package com.framework.util;

import android.content.Context;

import java.io.File;

/**
 * Created by root on 15-6-16.
 */
public class PathUtil {

    private static final String TAG = "PathUtil";

    /**
     * 获取项目目录
     */
    public static File getExternalFilesDir(Context context) {
        return context.getExternalFilesDir(null);
    }

    /**
     * 获取项目子目录
     */
    public static File getExternalFilesDir(Context context, String name) {
        return context.getExternalFilesDir(name);
    }

    public static String getLogPath(Context context) {
        return getExternalFilesDir(context, "log") + "/";
    }

    public static String getDBPath(Context context) {
        return getExternalFilesDir(context, "db") + "/";
    }

    public static String getDownLoadPath(Context context) {
        return getExternalFilesDir(context, "download") + "/";
    }

    public static String getLocationPath(Context context) {
        return getExternalFilesDir(context, "location") + "/";
    }

    public static String getPhotoPath(Context context) {
        return getExternalFilesDir(context, "photo") + "/";
    }

    public static String getWebCachePath(Context context) {
        return getExternalFilesDir(context, "webcache") + "/";
    }

    public static String getCachePath(Context context) {
        return context.getExternalCacheDir() + "/";
    }
}
