package com.sunshine.smart.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunshine.smart.R;
import com.sunshine.smart.utils.Constants;
import com.sunshine.smart.utils.DataCleanManager;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qq.QQ;

public class SettingActivity extends BaseActivity implements View.OnClickListener {

    private RelativeLayout clearcacke;
    private ImageView left_navi;
    private TextView title;
    private Button logout;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        sp = getSharedPreferences(Constants.USER,MODE_PRIVATE);
        ShareSDK.initSDK(this);
        ShareSDK.closeDebug();
        clearcacke = (RelativeLayout) findViewById(R.id.clearcacke);
        clearcacke.setOnClickListener(this);
        left_navi = (ImageView) findViewById(R.id.left_navi);
        left_navi.setOnClickListener(this);
        title = (TextView) findViewById(R.id.textView);
        title.setText(R.string.settings);
        logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.clearcacke:
                DataCleanManager.cleanExternalCache(this);
                ShareSDK.deleteCache();
                AlertDialog.Builder alert = new AlertDialog.Builder(this).setMessage("清理完成").setPositiveButton("完成",null);
                alert.create().show();
                break;
            case R.id.left_navi:
                SettingActivity.this.finish();
                break;
            case R.id.logout:
                AlertDialog.Builder lout = new AlertDialog.Builder(this).setTitle("退出登陆").setMessage("您确定要退出账户吗?").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sp.edit().putBoolean(Constants.IS_LOGIN,false)
                                .putString(Constants.ICON,"")
                                .putString(Constants.NICK,"")
                                .putString(Constants.USER_ID,"")
                                .putString(Constants.USER_TOKEN,"")
                                .commit();
                        //退出QQ  这里应该根据用户类型响应退出 因为目前只有QQ
                        Platform qq = ShareSDK.getPlatform(QQ.NAME);
                        qq.removeAccount(true);
                        startActivity(new Intent(SettingActivity.this,LoginActivity.class));
                        SettingActivity.this.finish();
                    }
                }).setNegativeButton("取消",null);
                lout.create().show();
                break;
        }
    }
}
