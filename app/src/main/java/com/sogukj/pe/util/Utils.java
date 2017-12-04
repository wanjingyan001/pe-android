package com.sogukj.pe.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class Utils {

    public static final String TAG = Utils.class.getSimpleName();

    public static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static float pxToDp(Context context, int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dp, context.getResources().getDisplayMetrics());
    }

    public static String stringFilter(String str) throws PatternSyntaxException {
        // 只允许字母、数字和汉字
        String regEx = "[^a-zA-Z0-9\u4E00-\u9FA5]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    public static void closeInput(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();
        if (isOpen) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static void showInput(Context context, View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInputFromInputMethod(view.getWindowToken(), InputMethodManager.SHOW_FORCED);
    }

    public static void toggleSoftInput(Context context, View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
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

    public static int[] getAndroiodScreenProperty(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        float density = dm.density;
        int densityDpi = dm.densityDpi;
        // 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
        int screenWidth = (int) (width / density);
        int screenHeight = (int) (height / density);
        int[] ints = new int[2];
        ints[0] = screenWidth;
        ints[1] = screenHeight;
        return ints;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static void setWindowStatusBarColor(Activity activity, int colorResId) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(activity.getResources().getColor(colorResId));
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 正则：手机号（精确）
     * <p>移动：134(0-8)、135、136、137、138、139、147、150、151、152、157、158、159、178、182、183、184、187、188</p>
     * <p>联通：130、131、132、145、155、156、171、175、176、185、186</p>
     * <p>电信：133、153、173、177、180、181、189</p>
     * <p>全球星：1349</p>
     * <p>虚拟运营商：170</p>
     */
    public static final String REGEX_MOBILE_EXACT = "^((13[0-9])|(14[5,7])|(15[0-3,5-9])|(17[0,1,3,5-8])|(18[0-9])|(147))\\d{8}$";
    /**
     * 正则：身份证号码18位
     */
    public static final String REGEX_ID_CARD18 = "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9Xx])$";

    /**
     * 验证手机号（精确）
     *
     * @param input 待验证文本
     * @return {@code true}: 匹配<br>{@code false}: 不匹配
     */
    public static boolean isMobileExact(final CharSequence input) {
        return isMatch(REGEX_MOBILE_EXACT, input);
    }

    /**
     * 验证身份证号码 18 位
     *
     * @param input 待验证文本
     * @return {@code true}: 匹配<br>{@code false}: 不匹配
     */
    public static boolean isIDCard18(final CharSequence input) {
        return isMatch(REGEX_ID_CARD18, input);
    }

    /**
     * 判断是否匹配正则
     *
     * @param regex 正则表达式
     * @param input 要匹配的字符串
     * @return {@code true}: 匹配<br>{@code false}: 不匹配
     */
    public static boolean isMatch(final String regex, final CharSequence input) {
        return input != null && input.length() > 0 && Pattern.matches(regex, input);
    }


}
