package com.sunshine.smart.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ProviderInfo;
import android.media.Image;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sunshine.smart.R;
import com.sunshine.smart.Service.BluetoothLeService;
import com.sunshine.smart.utils.Constants;
import com.sunshine.smart.utils.DbDao;
import com.sunshine.smart.utils.Device;

/**+
 * 蓝牙设备连接界面
 */
public class DeviceActivity extends AppCompatActivity implements View.OnClickListener{

    private static String TAG = DeviceActivity.class.getName();
    TextView title,nowtmp,settmp;
    ImageView left_navi;
    ImageView open,close,tmpplus,tmpsub,battery;
    private DbDao dbDao;
    private Device device;
    private boolean threadRun = true;
    public static byte[] controlData = new byte[7];//写入特征
    private ProgressDialog connect;
    private boolean ifRun = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initView();
        initBle();

    }

    public static BluetoothLeService mBluetoothLeService; //蓝牙BLE服务进程


    private void initView() {
        //标题
        title = (TextView) findViewById(R.id.textView);
        title.setText(getIntent().getIntExtra("name",0));
        left_navi = (ImageView) findViewById(R.id.left_navi);
        left_navi.setOnClickListener(this);
        nowtmp = (TextView) findViewById(R.id.textView16);
        settmp = (TextView) findViewById(R.id.textView3);
        tmpplus = (ImageView) findViewById(R.id.imageView8);
        tmpsub = (ImageView) findViewById(R.id.imageView9);
        open = (ImageView) findViewById(R.id.imageView4);
        close = (ImageView) findViewById(R.id.imageView5);
        //电池
        battery = (ImageView) findViewById(R.id.imageView3);
        close.setImageAlpha(50);
        open.setOnClickListener(this);
        close.setOnClickListener(this);
        tmpplus.setOnClickListener(this);
        tmpsub.setOnClickListener(this);

        connect = new ProgressDialog(this);
        connect.setMessage(getResources().getString(R.string.connectting));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.left_navi:
                DeviceActivity.this.finish();
                break;
            case R.id.imageView4:
                if (device==null){
                    Toast.makeText(this,R.string.noconnect,Toast.LENGTH_SHORT).show();
                    break;
                }
                if (!mBluetoothLeService.bleState) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (!mBluetoothLeService.connect(device.getAddress())){
                                try {
                                    Log.e(TAG,"尝试登陆");
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
                }else{
                    Toast.makeText(this,R.string.connected,Toast.LENGTH_SHORT).show();
                    Log.e(TAG,"设备已连接");
                }
                break;
            case R.id.imageView5:
                if (mBluetoothLeService.bleState){
                    AlertDialog.Builder al = new AlertDialog.Builder(this).setMessage(R.string.askdisconnect).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mBluetoothLeService.disconnect();
                        }
                    }).setNegativeButton(R.string.cancel,null);
//                    Toast.makeText(this,R.string.unconnected,Toast.LENGTH_SHORT).show();
                    al.create().show();
                }else{
                    Toast.makeText(this,R.string.noconnect,Toast.LENGTH_SHORT).show();
                    Log.e(TAG,"未连接设备");
                }
                break;
            case R.id.imageView8:
                if (!mBluetoothLeService.bleState){
                    Toast.makeText(this,R.string.noconnect,Toast.LENGTH_SHORT).show();
                    break;
                }
                int t = Integer.parseInt(settmp.getText().toString().trim());
                int tmp = t + 1;
                if (tmp>=42 && tmp<=60) {
                    settmp.setText(tmp + "");
                    sendTmp(tmp);
                }
                break;
            case R.id.imageView9:
                if (!mBluetoothLeService.bleState){
                    Toast.makeText(this,R.string.noconnect,Toast.LENGTH_SHORT).show();
                    break;
                }
                int t1 = Integer.parseInt(settmp.getText().toString().trim());
                int tmp1 = t1 - 1;
                if (tmp1>=42 && tmp1<=60) {
                    settmp.setText(tmp1 + "");
                    sendTmp(tmp1);
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        dbDao = new DbDao(this);
        //现在做法有一个BUG  当从当前界面跳转到搜索界面 然后再回来的时候, 因为传入的type并没有携带 所以仍然会提示 没有设备.
        int type = getIntent().getIntExtra("type",0);
        Log.e(TAG,"device type:"+type);
        device = dbDao.getDevice(type);
        //sqlite里没有当前类型的设备  提示用户添加设备 再次进入
        if (device == null){
            AlertDialog.Builder noDevice = new AlertDialog.Builder(this).setMessage(R.string.nodevice).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivityForResult(new Intent(DeviceActivity.this,BleScanConnectActivity.class),101);
                }
            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    DeviceActivity.this.finish();
                }
            });
            noDevice.create().show();
        }

        //注册一个广播监听
        IntentFilter i = new IntentFilter();
        i.addAction(BluetoothLeService.ACTION_WRITE_COMPLATE);  //写入完成
        i.addAction(BluetoothLeService.ACTION_GATT_CONNECTING); //连接中
        i.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);  //已连接
        i.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED); //断开连接
        i.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);  //发现服务
        i.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE); //收到数据
        Log.i(TAG,"receiver register ");
        registerReceiver(mGattUpdateReceiver,i);
        if (mBluetoothLeService != null && mBluetoothLeService.bleState){
            //恢复刷新线程
            ifRun = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ifRun = false;
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (threadRun) threadRun = false;
        mBluetoothLeService.disconnect();
        if (mBluetoothLeService!=null) {
            unbindService(mServiceConnection);
            mBluetoothLeService = null;
        }
    }


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            //收到广播
            switch (action){
                case BluetoothLeService.ACTION_GATT_CONNECTING:
                    connect.show();
                    Log.i(TAG,"bleservice is connecting...");
                    break;
                case BluetoothLeService.ACTION_GATT_CONNECTED:
                    if (connect.isShowing())connect.dismiss();
                    settmp.setText("45");
                    Toast.makeText(DeviceActivity.this,R.string.connected,Toast.LENGTH_SHORT).show();
                    Log.i(TAG,getResources().getString(R.string.connected));
                    break;
                case BluetoothLeService.ACTION_GATT_DISCONNECTED:
                    if (connect.isShowing())connect.dismiss();
                    nowtmp.setText("--°C");
                    settmp.setText("--");
                    close.setImageAlpha(50);
                    open.setImageAlpha(255);
                    threadRun = false;
                    Toast.makeText(DeviceActivity.this,R.string.unconnected,Toast.LENGTH_SHORT).show();
                    Log.e(TAG,getResources().getString(R.string.unconnected));
                    break;
                case BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED:
                    if (connect.isShowing())connect.dismiss();
                    close.setImageAlpha(255);
                    open.setImageAlpha(50);
                    //发现了蓝牙服务   开始刷新线程  并发送本地密码  读取设备密码  再读一次控制的字段  取回设备上现在的设置温度
                    Log.i(TAG,"services discovered...working start.. ");
                    threadRun = true;
                    //刷新线程开始
                    new Thread(new runnable()).start();
                    //读取密码和发送本地保存的密码

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBluetoothLeService.queueRequestCharacteristicValue(3);
                            sendPwdVeryfy();
                            mBluetoothLeService.queueRequestCharacteristicValue(2);
                        }
                    },500);

//                    byte[] d = new byte[4];
//                    d[0] = (byte) 0xff;
//                    d[1] = (byte) 0xff;
//                    d[2] = (byte) 0xff;
//                    d[3] = (byte) 0xff;
//                    mBluetoothLeService.queueWriteDataToCharacteristic(d,3);
                    break;
                case BluetoothLeService.ACTION_WRITE_COMPLATE:
                    if (connect.isShowing())connect.dismiss();
                    //写入完成
                    Log.e(TAG,"write complate...");
                    break;
                case BluetoothLeService.ACTION_DATA_AVAILABLE:
                    if (connect.isShowing())connect.dismiss();
                    byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    StringBuilder stringBuilder = new StringBuilder(data.length);
                    for (byte b:data){
                        stringBuilder.append(String.format("%02X ", b));
                    }
                    Log.e(TAG,"type:"+intent.getStringExtra("type")+" length:"+data.length+" bytes:contents:"+stringBuilder.toString());
                    switch (intent.getStringExtra("type")){
                        case "status":
                            //刷新界面
                            if (data[0]!=0xff){
                                nowtmp.setText(data[0]+"°C");
                            }else{
                                nowtmp.setText("--°C");
                            }
                            if (data.length==7){
                                break;
                            }
                            if (data[7]!=0xff){
                                if (data[7]==0){
                                    battery.setImageResource(R.mipmap.battery0);
                                }else if (data[7]>0  && data[7]<=30){
                                    battery.setImageResource(R.mipmap.battery1);
                                }else if (data[7]>30 && data[7]<=50){
                                    battery.setImageResource(R.mipmap.battery2);
                                }else if (data[7]>50 && data[7]<=70){
                                    battery.setImageResource(R.mipmap.battery3);
                                }else if (data[7]>70 && data[7]<=95){
                                    battery.setImageResource(R.mipmap.battery4);
                                }else if (data[7]>95){
                                    battery.setImageResource(R.mipmap.battery5);
                                }
                            }

                            break;
                        case "control":
                            if (data[0]<42){
                                settmp.setText(42+"");
                            }else{
                                settmp.setText(data[0]+"");
                            }
                            break;
                        case "passwd":
                            if (!new String(data).equals(device.getPassword())){
                                Log.e(TAG,"password wrong:device pwd: "+new String(data)+" ,your pwd :"+device.getPassword());
                            }
                            break;
                    }
                    break;
                default:
                    Log.e(TAG,action);
                    break;
            }
        }
    };

    class runnable implements Runnable{

        @Override
        public void run() {
            try {
                Thread.sleep(500);
                while (threadRun) {
                    if (ifRun) {
                        mBluetoothLeService.queueRequestCharacteristicValue(1);
                        mBluetoothLeService.queueRequestCharacteristicValue(2);
                        Thread.sleep(1000);
                    } else {
                        Log.e(TAG, "refresh thread is free...");
                        Thread.sleep(1000);
                    }
                }
                Log.e(TAG, "refresh thread finished....");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendTmp(int tmp){
        controlData[0] = (byte) tmp;
        controlData[1] = (byte) 0 ;
        controlData[2] = (byte) 0 ;
        controlData[3] = (byte) 0 ;
        controlData[4] = (byte) 0x0f ;
        controlData[5] = (byte) 0xff ;
        controlData[6] = (byte) 0xff ;
        mBluetoothLeService.queueWriteDataToCharacteristic(controlData,2);
    }
    private void sendPwdVeryfy(){
        String password = device.getPassword();
        if (password.equals("")||password.length()!=4){
            Log.e(TAG,"password format error.");
            return;
        }
        Log.i(" my password is",password+"");
        controlData[0] = (byte) password.charAt(0) ;
        controlData[1] = (byte) password.charAt(1) ;
        controlData[2] = (byte) password.charAt(2) ;
        controlData[3] = (byte) password.charAt(3) ;
        controlData[4] = (byte) 0xa0;
        controlData[5] = (byte) 0xff;
        controlData[6] = (byte) 0xff;
        mBluetoothLeService.queueWriteDataToCharacteristic(controlData,2);
    }

    private void initBle() {
        if(mBluetoothLeService==null){
            Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
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
            //这个界面不再需要自动连接
//            String mac = device.getAddress();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==2 ){
            Log.e(TAG,"scan return");
            ifRun = true;
        }
    }
}
