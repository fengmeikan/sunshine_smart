package com.sunshine.smart.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;

import com.sunshine.smart.R;
import com.sunshine.smart.Service.BluetoothLeService;
import com.sunshine.smart.utils.Constants;

/**
 * Created by huihu on 2016/7/5.
 * APP欢迎页
 */
public class SplashActivity extends Activity {

    private String TAG = SplashActivity.class.getName();
    private SharedPreferences sharedPreferences,sp;
    private SharedPreferences.Editor editor;
    private boolean first;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        sharedPreferences = getSharedPreferences(Constants.SETTING, Context.MODE_PRIVATE);
        sp = getSharedPreferences(Constants.USER,MODE_PRIVATE);
        editor = sharedPreferences.edit();
        AlphaAnimation animation = new AlphaAnimation(0.3f, 1.0f);
        animation.setDuration(2000);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.splash_root);
        relativeLayout.startAnimation(animation);
        createShortcut();
    }

    /**
     * 创建快捷方式
     */
    private void createShortcut() {
        first = sharedPreferences.getBoolean(Constants.FIRSTTIME, false);
        if (!first){
            Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT") ;//不知道什么常量
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME,getString(R.string.app_name));
            shortcut.putExtra("duplicate",false);
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setClass(this, SplashActivity.class);//设置第一个页面
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
            Intent.ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher);
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
            sendBroadcast(shortcut);
            editor.putBoolean(Constants.FIRSTTIME,true);
            editor.commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (first){

                    if (sp.getBoolean(Constants.IS_LOGIN,false)){
                        startActivity(new Intent(SplashActivity.this,MainActivity.class));
                    }else{
                        startActivity(new Intent(SplashActivity.this,LoginActivity.class));
                    }

                }else{
                    startActivity(new Intent(SplashActivity.this,FirstInstallActivity.class));
                }
                SplashActivity.this.finish();

            }
        }).start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
