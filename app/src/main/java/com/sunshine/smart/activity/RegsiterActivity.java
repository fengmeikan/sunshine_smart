package com.sunshine.smart.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sunshine.smart.R;
import com.sunshine.smart.SmartApplication;
import com.sunshine.smart.utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Random;

public class RegsiterActivity extends AppCompatActivity implements View.OnClickListener,SmartApplication.xUtilsHttp {

    private String TAG = RegsiterActivity.class.getName();
    private ImageView left_navi,right_navi;
    private TextView title;
    private EditText mobile,verifycode,password;
    private Button verify,ok;
    private Boolean isVerify = false;
    private HttpUtils httpUtil;
    private String verifyC;//生成的验证码
    private HashMap<String,String> map;
    private String phonenum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regsiter);

        initView();
        initData();
    }

    private void initData() {
        httpUtil = new HttpUtils(this);
    }

    private void initView() {
        left_navi = (ImageView) findViewById(R.id.left_navi);
        right_navi = (ImageView) findViewById(R.id.right_navi);
        title = (TextView) findViewById(R.id.textView);
        title.setText(R.string.register);
        left_navi.setOnClickListener(this);
        mobile = (EditText) findViewById(R.id.mobile);
        verifycode = (EditText) findViewById(R.id.verifycode);
        password = (EditText) findViewById(R.id.password);
        verify = (Button) findViewById(R.id.verify);
        ok = (Button) findViewById(R.id.ok);
        verify.setOnClickListener(this);
        ok.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.left_navi:
                this.finish();
                break;
            case R.id.verify:
                phonenum = mobile.getText().toString();
                if (phonenum.length()!=11 || !phonenum.startsWith("1")){
                    Toast.makeText(RegsiterActivity.this, "请输入正确的手机号码", Toast.LENGTH_SHORT).show();
                    return ;
                }
                int x;//定义两变量
                Random ne=new Random();//实例化一个random的对象ne
                x=ne.nextInt(9999-1000+1)+1000;//为变量赋随机值1000-9999
                verifyC = x + "";
                map = new HashMap<String, String>();
                map.put("action","User.sendVerifyCode");
                map.put("mobile",phonenum);
                map.put("code",verifyC);
                httpUtil.post(map,100);
                break;
            case R.id.ok:
                String mycode = verifycode.getText().toString();
                Log.e(TAG,"mycode:"+mycode);
                Log.e(TAG,"verifyC:"+verifyC);
                //对比验证码
                if (password.getText().toString().length()<6){
                    Toast.makeText(RegsiterActivity.this,"请输入最少6位密码",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!mycode.equals(verifyC)){
                    Toast.makeText(RegsiterActivity.this,R.string.verifynote,Toast.LENGTH_SHORT).show();
                    return;
                }
                map = new HashMap<String, String>();
                map.put("action","User.register");
                map.put("mobile",phonenum);
                map.put("password",password.getText().toString());
                httpUtil.post(map,101);
                break;
        }
    }

    @Override
    public void jsonResponse(JSONObject json,int tag) {
        switch (tag){
            case 100:
                try {
                    if (json.getInt("error")==0){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int time = 60;
                                while (time>=0){
                                    Message msg = new Message();
                                    msg.what = 100;
                                    msg.obj = time;
                                    handler.sendMessage(msg);
                                    time -- ;
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                handler.sendEmptyMessage(99);
                            }
                        }).start();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case 101:
                //注册成功
                try {
                    if (json.getInt("success")==1) {
                        Toast.makeText(this, "注册成功,请登录", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MobileLoginActivity.class));
                        this.finish();
                    }else{
                        Toast.makeText(this, json.getString("error")+":"+json.getString("error_desc"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void jsonResponseError() {

    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 99:
                    verify.setEnabled(true);
                    verify.setText("GO");
                    break;
                case 100:
                    verify.setEnabled(false);
                    verify.setText((int)msg.obj+"");
                    break;
            }
        }
    };
}
