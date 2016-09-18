package com.sunshine.smart.activity;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.sunshine.smart.R;
import com.sunshine.smart.Service.BluetoothLeService;
import com.sunshine.smart.Service.UpdateService;
import com.sunshine.smart.SmartApplication;
import com.sunshine.smart.fragment.DevicesFragment;
import com.sunshine.smart.fragment.HealthFragment;
import com.sunshine.smart.fragment.MenuFragment;
import com.sunshine.smart.fragment.NewDeviceFragment;
import com.sunshine.smart.fragment.NewsFragment;
import com.sunshine.smart.fragment.ShopFragment;
import com.sunshine.smart.lib.SlidingMenu;
import com.sunshine.smart.lib.app.SlidingFragmentActivity;
import com.sunshine.smart.utils.Constants;
import com.sunshine.smart.utils.FragmentTabAdapter;
import com.sunshine.smart.utils.HttpUtils;
import com.sunshine.smart.utils.VersionUtils;
import com.sunshine.smart.widget.DialogShows;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

/**
 * Created by huihu on 2016/7/5.
 * 主页面 Fragment
 */
public class MainActivity extends SlidingFragmentActivity implements NewsFragment.OnFragmentInteractionListener,ShopFragment.OnFragmentInteractionListener, SmartApplication.xUtilsHttp, DialogShows.EditInputLintener {

    private String TAG = MainActivity.class.getName();
    private Fragment fragment1;
    private RadioGroup rds;
    private RadioButton device_btn,health_btn,news_btn,shop_btn;
    private ImageView left_navi;
    private Fragment mContent;
    public static ImageView right_navi;
    public static ProgressBar progressBar;
    private static SlidingMenu sm;

    private String updataURL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);//B

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initFragment();
        slidingMenu(savedInstanceState);
        initView();


        HashMap<String,String> param = new HashMap<String,String>();
        param.put("action", "System.autoUpdate");
        param.put("version", VersionUtils.getVersion(this));
        new HttpUtils(this).post(param, 0);

    }

    @Override
    protected void onStart() {
        super.onStart();
//        initBleService();
    }

    private void initView() {
        right_navi = (ImageView)findViewById(R.id.right_navi);
        //动画
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
    }


    private void slidingMenu(Bundle savedInstanceState) {
        // check if the content frame contains the menu frame
        if (findViewById(R.id.menu_frame) == null) {
            setBehindContentView(R.layout.menu_frame);
            getSlidingMenu().setSlidingEnabled(true);
            getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
//            getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        } else {
            // add a dummy view
            View v = new View(this);
            setBehindContentView(v);
            getSlidingMenu().setSlidingEnabled(false);
            getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        }

        // set the Above View Fragment
        if (savedInstanceState != null) {
            mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
        }

        // set the Behind View Fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.menu_frame, new MenuFragment()).commit();
        // customize the SlidingMenu
        sm = getSlidingMenu();
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setFadeEnabled(false);
        sm.setBehindScrollScale(0.25f);
        sm.setFadeDegree(0.25f);
        if (left_navi!=null){
            left_navi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sm.toggle();
                }
            });
        }
        sm.setBackgroundColor(getResources().getColor(R.color.gray));
//        sm.setBackgroundImage(R.mipmap.img_frame_background);
        sm.setBehindCanvasTransformer(new SlidingMenu.CanvasTransformer() {
            @Override
            public void transformCanvas(Canvas canvas, float percentOpen) {
                float scale = (float) (percentOpen * 0.25 + 0.75);
                canvas.scale(scale, scale, -canvas.getWidth() / 2,
                        canvas.getHeight() / 2);
            }
        });

        sm.setAboveCanvasTransformer(new SlidingMenu.CanvasTransformer() {
            @Override
            public void transformCanvas(Canvas canvas, float percentOpen) {
                float scale = (float) (1 - percentOpen * 0.25);
                canvas.scale(scale, scale, 0, canvas.getHeight() / 2);
            }
        });

    }

    public static void smToggle(){
        sm.toggle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    /**
     * 初始化4个fragment
     */
    private void initFragment() {
        rds = (RadioGroup)findViewById(R.id.radiogroup);

        //左侧导航
        left_navi = (ImageView) findViewById(R.id.left_navi);
        device_btn = (RadioButton) findViewById(R.id.deveces_btn);
        health_btn = (RadioButton) findViewById(R.id.health_btn);
        news_btn = (RadioButton) findViewById(R.id.news_btn);
        shop_btn = (RadioButton) findViewById(R.id.shop_btn);


        List<Fragment> list = new ArrayList<>();
        list.add(new NewDeviceFragment());
//        list.add(new DevicesFragment());
        list.add(new HealthFragment());
        list.add(new NewsFragment());
        list.add(new ShopFragment());

        FragmentTabAdapter fragmentTabAdapter = new FragmentTabAdapter(this,list,R.id.content,rds);
        fragmentTabAdapter.setOnRgsExtraCheckedChangedListener(new FragmentTabAdapter.OnRgsExtraCheckedChangedListener(){
            @Override
            public void OnRgsExtraCheckedChanged(RadioGroup radioGroup, int checkedId, int index) {
//                super.OnRgsExtraCheckedChanged(radioGroup, checkedId, index);
                Drawable image;
                switch (index){
                    case 0:
                        //在设备页面屏蔽SLIDINGMENU的手势
                        MainActivity.this.getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
                        image = getResources().getDrawable(R.mipmap.devices_en);
                        image.setBounds(0,0,image.getIntrinsicWidth(),image.getIntrinsicHeight());
                        device_btn.setCompoundDrawables(null,image,null,null);
                        break;
                    case 1:
                        image = getResources().getDrawable(R.mipmap.health_en);
                        image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
                        health_btn.setCompoundDrawables(null, image, null, null);
                        break;
                    case 2:
                        image = getResources().getDrawable(R.mipmap.news_en);
                        image.setBounds(0,0,image.getIntrinsicWidth(),image.getIntrinsicHeight());
                        news_btn.setCompoundDrawables(null,image,null,null);
                        break;
                    case 3:
                        image = getResources().getDrawable(R.mipmap.shop_en);
                        image.setBounds(0,0,image.getIntrinsicWidth(),image.getIntrinsicHeight());
                        shop_btn.setCompoundDrawables(null,image,null,null);
                        break;
                }
                HideOther(index);
            }
        });

    }

    private void HideOther(int index) {
        Drawable image;
        if (index != 0 ){
            image = getResources().getDrawable(R.mipmap.devices);
            image.setBounds(0,0,image.getIntrinsicWidth(),image.getIntrinsicHeight());
            device_btn.setCompoundDrawables(null,image,null,null);
        }
        if (index != 1 ){
            image = getResources().getDrawable(R.mipmap.health);
            image.setBounds(0,0,image.getIntrinsicWidth(),image.getIntrinsicHeight());
            health_btn.setCompoundDrawables(null,image,null,null);
        }
        if (index != 2 ){
            image = getResources().getDrawable(R.mipmap.news);
            image.setBounds(0,0,image.getIntrinsicWidth(),image.getIntrinsicHeight());
            news_btn.setCompoundDrawables(null,image,null,null);
        }
        if (index != 3 ){
            image = getResources().getDrawable(R.mipmap.shop);
            image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
            shop_btn.setCompoundDrawables(null,image,null,null);
        }
    }


    public static BluetoothLeService mBluetoothLeService; //蓝牙BLE服务进程
    //BLE连接状态  默认未连接
    public static boolean bleState = false;

    public void initBleService() {
        if (mBluetoothLeService==null) {
            Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void unBindBleService(){
        if (mBluetoothLeService!=null){
            Log.i(TAG,"unbind ble service...");
            bleState = false;
            unbindService(mServiceConnection);
            mBluetoothLeService = null;
        }
    }

    // BLE服务的生命周期
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }
            // Automatically connects to the device upon successful start-up initialization.
            String mac = getSharedPreferences(Constants.SETTING,MODE_PRIVATE).getString("mac","");

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e(TAG,"BLE服务建立失败");
            mBluetoothLeService = null;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"mainactivity is destory");
        unBindBleService();
    }

    /**
     * DEVICES onFragmentInteraction Interface
     * @param uri
     */
    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.e(TAG,uri.toString()+"----");
    }

    private long mExitTime;

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {

                Toast.makeText(this, R.string.exit_again, Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();

            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void jsonResponse(JSONObject json, int tag) throws JSONException {
        if (json.getInt("success")==1){
            updataURL = json.getJSONObject("data").getString("apk_url");
            DialogShows.getInstance(this).showTextDialog(this, getResources().getString(R.string.update_content), getResources().getString(R.string.update_title)).setEditInputOKLintener(this);
        }
    }

    @Override
    public void jsonResponseError() {
        Log.e("MainActivity","错误");
    }


    @Override
    public void OnEditInput(DialogShows dialog, String input) {
        Intent updateIntent =new Intent(MainActivity.this, UpdateService.class);
        updateIntent.putExtra("app_name",getResources().getString(R.string.app_name));
        updateIntent.putExtra("downurl", updataURL);
        startService(updateIntent);
    }
    //开启更新服务UpdateService
    //这里为了把update更好模块化，可以传一些updateService依赖的值
    //如布局ID，资源ID，动态获取的标题,这里以app_name为例
//    Intent updateIntent =new Intent(MainActivity.this, UpdateService.class);
//    updateIntent.putExtra("app_name",R.string.app_name);
//    updateIntent.putExtra("downurl", "http://img4.imgtn.bdimg.com/it/u=306400967,4194172527&fm=21&gp=0.jpg");
//    startService(updateIntent);
}
