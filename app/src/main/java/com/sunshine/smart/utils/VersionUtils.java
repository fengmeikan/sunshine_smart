package com.sunshine.smart.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created by fengm on 2016/9/13.
 */
public class VersionUtils {

    /**
     * 返回当前程序版本名
     */
    public static String getVersion(Context context){
        String versionName = "";
        int versioncode=0;
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            versioncode = pi.versionCode;
            if (versionName == null || versionName.length() <= 0) {
                return "0";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }

}
