package com.sunshine.smart.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.sunshine.smart.SmartApplication;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

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

    public void post(HashMap<String,String> map, final int mytag){
        if (!map.containsKey("action")){
            Log.e(TAG,"action is null...");
            Toast.makeText(context,"参数错误",Toast.LENGTH_SHORT).show();
            return;
        }
        RequestParams params = new RequestParams(Constants.API_URL);

        for (String name:map.keySet()){
            params.addBodyParameter(name,map.get(name));
        }
        x.http().post(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                Log.e(TAG,"success:"+result.toString());
                http.jsonResponse(result,mytag);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.e(TAG,"error");
                Toast.makeText(context,"请求失败,请检查网络设定",Toast.LENGTH_SHORT).show();
                http.jsonResponseError();
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Log.e(TAG,"canceled");

            }

            @Override
            public void onFinished() {
                Log.e(TAG,"finished");

            }
        });
    }
}
