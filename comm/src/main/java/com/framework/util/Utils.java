package com.framework.util;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 获取系统信息
 *
 * @author Mars
 */
public class Utils {

    public static final String TAG = Utils.class.getSimpleName();

    /**
     * 获取版本号(内部识别号)
     *
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取版本号名称(内部识别号)
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        String versionName = "";
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return versionName;
        }
        return versionName;
    }

    /**
     * @param context
     * @return
     */
    public static String getIMEI(Context context) {
        TelephonyManager telephonemanage = (TelephonyManager) context.getSystemService(Activity.TELEPHONY_SERVICE);
        return telephonemanage.getDeviceId();
    }

    /**
     * @param context
     * @return
     */
    public static String getIMSI(Context context) {
        TelephonyManager telephonemanage = (TelephonyManager) context.getSystemService(Activity.TELEPHONY_SERVICE);
        return telephonemanage.getSubscriberId();
    }

    static public String getUDID(Context context) {
        String uuid = null;

        final String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        try {
            if (!"9774d56d682e549c".equals(androidId)) {
                uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8")).toString();
            } else {
                final String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                uuid = deviceId != null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")).toString() : UUID.randomUUID().toString();
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return MD5.get32MD5(uuid);
    }

    /**
     * 实现文本复制功能
     */
    public static void copy(Context context, String content) {
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content.trim());
    }

    /**
     * 实现粘贴功能
     */
    public static String paste(Context context) {
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        return cmb.getText().toString().trim();
    }

    public static boolean isMobile(final CharSequence str) {
        Pattern p = Pattern.compile("^[1][0-9]{10}$");
        Matcher m = p.matcher(str);
        return m.matches();
    }
}
