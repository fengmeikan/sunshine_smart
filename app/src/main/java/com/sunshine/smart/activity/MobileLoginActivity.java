package com.sunshine.smart.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.sunshine.smart.R;
import com.sunshine.smart.SmartApplication;
import com.sunshine.smart.utils.Constants;
import com.sunshine.smart.utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MobileLoginActivity extends AppCompatActivity implements View.OnClickListener,SmartApplication.xUtilsHttp {

    private EditText mobile,password;
    private Button ok;
    private ImageView left_navi;
    private HttpUtils httpUtil;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_login);
        initView();
        editor = getSharedPreferences(Constants.USER,MODE_PRIVATE).edit();
    }

    private void initView() {
        mobile = (EditText) findViewById(R.id.mobile);
        password = (EditText) findViewById(R.id.password);
        ok = (Button) findViewById(R.id.ok);
        left_navi = (ImageView) findViewById(R.id.left_navi);
        left_navi.setOnClickListener(this);
        ok.setOnClickListener(this);
        httpUtil = new HttpUtils(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.ok){
            String mobiles = mobile.getText().toString();
            String passwords = password.getText().toString();
            HashMap map = new HashMap();
            map.put("action","User.login");
            map.put("mobile",mobiles);
            map.put("password",passwords);
            httpUtil.post(map,100);
        }
        if (view.getId()==R.id.left_navi){
            this.finish();
        }
    }

    @Override
    public void jsonResponse(JSONObject json, int tag) {
        try {
            if (json.getInt("success")==1){
                //登录成功
                editor.putBoolean(Constants.IS_LOGIN,true);
                editor.putString("mobile",json.getJSONObject("data").getString("mobile"));
                editor.putString("icon",json.getJSONObject("data").getString("icon"));
                editor.commit();
                startActivity(new Intent(this,MainActivity.class));
                this.finish();
            }else{
                Toast.makeText(this,"账号或密码错误",Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void jsonResponseError() {

    }
}
