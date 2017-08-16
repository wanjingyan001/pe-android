package com.sogukj.pe.util;

import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.TypedValue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class Utils {

    public static final String TAG = Utils.class.getSimpleName();

    public static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static float pxToDp(Context context, int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dp, context.getResources().getDisplayMetrics());
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

    public static boolean isMobile(final CharSequence str) {
        Pattern p = Pattern.compile("^[1][0-9]{10}$");
        Matcher m = p.matcher(str);
        return m.matches();
    }
}
