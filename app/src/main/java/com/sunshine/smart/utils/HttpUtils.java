package com.sunshine.smart.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.sunshine.smart.SmartApplication;
import com.sunshine.smart.activity.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.common.task.AbsTask;
import org.xutils.http.HttpMethod;
import org.xutils.http.HttpTask;
import org.xutils.http.ProgressHandler;
import org.xutils.http.RequestParams;
import org.xutils.http.loader.FileLoader;
import org.xutils.http.request.UriRequest;
import org.xutils.x;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * Created by mac on 16/8/23.
 */
public class HttpUtils {

    private String TAG = HttpUtils.class.getName();
    private SmartApplication.xUtilsHttp http;
    private Context context;
    public HttpUtils(SmartApplication.xUtilsHttp h) {
        http = h;
        context = (Context)h;
    }
    private SmartApplication.xUtilsHttpFile httpfile;
    public HttpUtils(SmartApplication.xUtilsHttpFile h,int i) {
        httpfile = h;
        context = (Context)h;
    }

    public void post(HashMap<String,String> map, final int mytag){
        if (!map.containsKey("action")){
            Log.e(TAG,"action is null...");
            Toast.makeText(context,"参数错误",Toast.LENGTH_SHORT).show();
            return;
        }
        RequestParams params = new RequestParams(Constants.API_URL);

        for (String name:map.keySet()){
            if ("icon".equals(name)){
                params.addBodyParameter(name,new File(map.get(name)));
            }else{
                params.addBodyParameter(name,map.get(name));
            }
        }
        x.http().post(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                Log.e(TAG, "success:" + result.toString());
                try {
                    http.jsonResponse(result, mytag);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.e(TAG, "error");
                Toast.makeText(context, "请求失败,请检查网络设定", Toast.LENGTH_SHORT).show();
                http.jsonResponseError();
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Log.e(TAG, "canceled");

            }

            @Override
            public void onFinished() {
                Log.e(TAG, "finished");

            }
        });
    }


    public void DownloadAPK(final String Url,final String app_name){
        RequestParams rp = new RequestParams(Url);
        rp.setLoadingUpdateMaxTimeSpan(100);
        File file = new File(Environment.getExternalStorageDirectory()+"/apk/");
        if (!file.exists()){
            file.mkdirs();
        }
        rp.setSaveFilePath(Environment.getExternalStorageDirectory()+"/apk/"+app_name);

        x.http().get(rp, new Callback.ProgressCallback<File>() {
            @Override
            public void onWaiting() {
            }
            @Override
            public void onStarted() {
                httpfile.Start();
            }
            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                httpfile.UpdateProgress(total,current,isDownloading);
            }
            @Override
            public void onSuccess(File result) {
                httpfile.jsonResponse(result,Environment.getExternalStorageDirectory()+"/apk/"+app_name);
            }
            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                httpfile.jsonResponseError();
            }
            @Override
            public void onCancelled(CancelledException cex) {
            }
            @Override
            public void onFinished() {
            }
        });
    }



}
