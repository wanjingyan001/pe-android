package com.sogukj.pe.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.sogukj.pe.ui.main.MainMsgFragment;

import java.util.ArrayList;

/**
 * Created by sogubaby on 2018/4/17.
 */

public class PermissionUtils {
    /**
     * 检查单个权限
     *
     * @param activity
     * @param permission
     * @param code
     * @return
     */
    public static boolean checkPermission(Activity activity, String permission, int code) {
        return checkPermission(activity, new String[]{permission}, code);
    }

    /**
     * 申请没有的权限,已申请的会被过滤,
     * 如果没有过滤,会导致已申请的权限再次申请
     * 返回申请失败,不执行相应的方法
     *
     * @param activity
     * @param permission
     * @param code
     * @return
     */
    public static boolean checkPermission(Activity activity, String[] permission, int code) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<>();
            for (String per : permission) {
                if (ContextCompat.checkSelfPermission(activity, per)
                        != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(per);
                }
            }
            if (permissions.size() > 0) {
                activity.requestPermissions(permissions.toArray(new String[permissions.size()]), code);
                return false;
            }
        }
        return true;
    }

    public static boolean checkPermission(Fragment fragment, String permission, int code) {
        return checkPermission(fragment, new String[]{permission}, code);
    }

    public static boolean checkPermission(Fragment fragment, String[] permission, int code) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<>();
            for (String per : permission) {
                if (ContextCompat.checkSelfPermission(fragment.getContext(), per) != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(per);
                }
            }
            if (permissions.size() > 0) {
                fragment.requestPermissions(permissions.toArray(new String[permissions.size()]), code);
                return false;
            }
        }
        return true;
    }

}
