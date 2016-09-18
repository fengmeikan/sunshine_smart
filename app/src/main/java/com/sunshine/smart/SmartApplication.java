package com.sunshine.smart;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.sunshine.smart.Service.BluetoothLeService;
import com.sunshine.smart.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.image.ImageOptions;
import org.xutils.x;

import java.io.File;

/**
 * Created by mac on 16/7/19.
 */
public class SmartApplication extends Application {

    private static String TAG = SmartApplication.class.getName();
    public static SmartApplication application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        x.Ext.init(this);

        Log.e(TAG,"Sunshine Smart starting ... !!!  ");
    }

    public interface xUtilsHttp{
         void jsonResponse(JSONObject json,int tag) throws JSONException;
         void jsonResponseError();
    }

    public interface xUtilsHttpFile{
        void UpdateProgress(long total, long current, boolean isDownloading);
        void Start();
        void jsonResponse(File file,String Savepath);
        void jsonResponseError();
    }

}
