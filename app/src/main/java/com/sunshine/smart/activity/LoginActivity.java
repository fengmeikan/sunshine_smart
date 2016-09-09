package com.sunshine.smart.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mob.commons.SHARESDK;
import com.sunshine.smart.R;
import com.sunshine.smart.SmartApplication;
import com.sunshine.smart.utils.Constants;
import com.sunshine.smart.utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.PlatformDb;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

import static cn.sharesdk.sina.weibo.SinaWeibo.*;

public class LoginActivity extends BaseActivity implements View.OnClickListener, PlatformActionListener,SmartApplication.xUtilsHttp {

    private String TAG = LoginActivity.class.getName();
    private Button qqlogin,wxlogin,wblogin,phonelogin;
    private TextView suibian,register;
    private SharedPreferences sp;
    private ProgressDialog pd;
    private Platform platform;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sp = getSharedPreferences(Constants.USER,MODE_PRIVATE);

        ShareSDK.initSDK(this);
        ShareSDK.closeDebug();
        initView();
    }

    private void initView() {
        qqlogin = (Button) findViewById(R.id.qqlogin);
        wxlogin = (Button) findViewById(R.id.wxlogin);
        wblogin = (Button) findViewById(R.id.wblogin);
        suibian = (TextView) findViewById(R.id.suibian);
        register = (TextView) findViewById(R.id.textView13);
        phonelogin = (Button) findViewById(R.id.phonelogin);
        qqlogin.setOnClickListener(this);
        wxlogin.setOnClickListener(this);
        wblogin.setOnClickListener(this);
        suibian.setOnClickListener(this);
        register.setOnClickListener(this);
        phonelogin.setOnClickListener(this);

        pd = new ProgressDialog(this);
        pd.setCancelable(false);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.qqlogin:
                Platform qq = ShareSDK.getPlatform(QQ.NAME);
                if (qq.isAuthValid()){
                    String userId = qq.getDb().getUserId();
                    Log.e(TAG,"当前已经授权");
                    qq.removeAccount(true);
                    return ;
                }
                pd.setMessage("请稍后...");
                pd.show();
                qq.SSOSetting(false);
                qq.setPlatformActionListener(this);
                qq.showUser(null);
                break;
            case R.id.wxlogin:
                Platform weixin = ShareSDK.getPlatform(Wechat.NAME);
                if (weixin.isAuthValid()){
                    String userId = weixin.getDb().getUserId();
                    Log.e(TAG,"当前已经授权");
                    weixin.removeAccount(true);
                    return ;
                }
                pd.setMessage("请稍后...");
                pd.show();
                weixin.SSOSetting(false);
                weixin.setPlatformActionListener(this);
                weixin.showUser(null);
//                AlertDialog.Builder wx = new AlertDialog.Builder(this).setMessage("暂未开放").setPositiveButton("确定",null);
//                wx.create().show();
                break;
            case R.id.wblogin:
                AlertDialog.Builder wb = new AlertDialog.Builder(this).setMessage("暂未开放").setPositiveButton("确定",null);
                wb.create().show();
                break;
            case R.id.suibian:
                startActivity(new Intent(this,MainActivity.class));
                finish();
                break;
            case R.id.phonelogin:
                startActivity(new Intent(this,MobileLoginActivity.class));
                break;
            case R.id.textView13:
                startActivity(new Intent(this,RegsiterActivity.class));
                break;
        }
    }

    @Override
    public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
        //登录成功后 先提交服务器  更新数据
        Log.e(TAG,"login success");
        this.platform = platform;
        HashMap map = null;
        HttpUtils httpUtils = new HttpUtils(this);
        PlatformDb db = platform.getDb();
        Log.e(TAG,"login type:"+db.getPlatformNname()+"");
        //QQ登录
        if (db.getPlatformNname().equals(QQ.NAME)){
            map = new HashMap();
            map.put("qqopenid",db.getToken());
            //微信
        }else if (db.getPlatformNname().equals(Wechat.NAME)) {
            map = new HashMap();
            map.put("wxopenid",db.getToken());
            //微博
        } else if (db.getPlatformNname().equals(SinaWeibo.NAME)) {
            map = new HashMap();
            map.put("wbopenid",db.getToken());
        }
        if (map!=null){
            Log.e(TAG,map.toString());
            map.put("action","User.updateUser");
            Log.e(TAG,map.toString());
            httpUtils.post(map,100);
        }
    }

    @Override
    public void onError(Platform platform, int i, Throwable throwable) {
        Toast.makeText(LoginActivity.this,"登陆失败",Toast.LENGTH_SHORT).show();
        pd.dismiss();
        Log.e(TAG,"2222");
    }

    @Override
    public void onCancel(Platform platform, int i) {
        Toast.makeText(LoginActivity.this,"登陆取消",Toast.LENGTH_SHORT).show();
        pd.dismiss();
        Log.e(TAG,"3333");
    }

    @Override
    public void jsonResponse(JSONObject json, int tag) {

        try {
            sp.edit().putBoolean(Constants.IS_LOGIN,true)
                    .putString(Constants.USER_ID,platform.getDb().getUserId())
                    .putString(Constants.ICON,platform.getDb().getUserIcon())
                    .putString(Constants.NICK,platform.getDb().getUserName())
                    .putString(Constants.USER_TOKEN,platform.getDb().getToken())
                    .putString(Constants.USER_ID,json.getJSONObject("data").getString("id"))
                    .commit();
            //手机号码登录的
            if (json.getJSONObject("data").has("mobile")){
                sp.edit().putString(Constants.ICON,json.getJSONObject("data").getString("icon"))
                        .putString(Constants.NICK,json.getJSONObject("data").getString("nick"))
                        .putString(Constants.SEX,json.getJSONObject("data").getString("sex"))
                        .putString(Constants.AGE,json.getJSONObject("data").getString("age"))
                        .putString(Constants.HEIGHT,json.getJSONObject("data").getString("height"))
                        .putString(Constants.WEIGHT,json.getJSONObject("data").getString("weight"))
                        .commit();
            }
            startActivity(new Intent(this,MainActivity.class));
            if (pd.isShowing()){
                pd.dismiss();
            }
            finish();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void jsonResponseError() {

    }
}
