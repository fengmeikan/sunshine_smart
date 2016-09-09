package com.sunshine.smart.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.TimePickerView;
import com.gc.materialdesign.views.Slider;
import com.gc.materialdesign.views.Switch;
import com.sunshine.smart.R;
import com.sunshine.smart.Service.BluetoothLeService;
import com.sunshine.smart.activity.BleScanConnectActivity;
import com.sunshine.smart.activity.MainActivity;
import com.sunshine.smart.adapter.DevicesAdapter;
import com.sunshine.smart.utils.Constants;

import java.util.Date;

public class DevicesFragment extends Fragment implements OnClickListener {
    private static String TAG = DevicesFragment.class.getName();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RelativeLayout clickconnect;
    private Switch mainswitch;//状态总开关
    private TextView mainswitchtext;
    private ImageView blepasswordset,bletimeset;

    private View rootView;
    private SharedPreferences sp;
    private String mac;
    private boolean havePwd = false;//是否有设置密码
    private boolean ViewVisible = false; //广播监听打开状态  //原用于关闭广播 后来考虑不再关闭本广播 用于处理界面是否需要刷新 为false时不需要处理界面
    private boolean threadRun = true;
    private myThread myThread;
    private int count = 0;//用于计算尝试连接次数
    private TimePickerView pvTime;
    private RelativeLayout timeSetInfo; //用于指示倒计时的外层
    private TextView hour,minute,second; //用于指示倒计时的时分秒
    private Button offtimeset;
    private int myTime;
    private ProgressDialog pdAuto;
    private ListView device_listview;
    private DevicesAdapter adapter;
    public static byte[] controlData = new byte[7];//写入特征
    static DevicesFragment init;
    public DevicesFragment() {
    }

    public static DevicesFragment getInstance(){
        return init;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_devices, container, false);
        sp = getActivity().getSharedPreferences(Constants.SETTING,Context.MODE_PRIVATE);
        mac = sp.getString("mac","");
        initView();
        init = this;
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        controlData[0] = 42;
        controlData[1] = 42;
        controlData[2] = 42;
        controlData[3] = 42;
        //devices fragment初始化后  就尝试连接已经存在的MAC地址
        new Thread(new Runnable() {
            int x1 = 0;
            @Override
            public void run() {
                do {
                    Log.e("x1",x1++ + "");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }while (MainActivity.mBluetoothLeService==null);
                if (!mac.equals("") && !MainActivity.bleState && MainActivity.mBluetoothLeService!=null) {
                    Log.i(TAG,"尝试自动连接");
                    mHandler.sendEmptyMessage(101);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.mBluetoothLeService.connect(mac);
                        }
                    },500);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (pdAuto!=null) pdAuto.dismiss();
                        }
                    },2000);
                }
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.right_navi.setVisibility(View.VISIBLE);
        MainActivity.right_navi.setImageResource(android.R.drawable.ic_media_play);
        MainActivity.right_navi.setOnClickListener(this);
        //注册一个广播监听
        IntentFilter i = new IntentFilter();
        i.addAction(BluetoothLeService.ACTION_WRITE_COMPLATE);  //写入完成
        i.addAction(BluetoothLeService.ACTION_GATT_CONNECTING); //连接中
        i.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);  //已连接
        i.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED); //断开连接
        i.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);  //发现服务
        i.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE); //收到数据
        Log.i(TAG,"receiver register ");
        ViewVisible = true;
        getActivity().registerReceiver(mGattUpdateReceiver,i);
        //恢复刷新线程  条件是BLE已经连接
        if (myThread!=null && myThread.isAlive() && !myThread.getStatus() && MainActivity.bleState){
            myThread.onThreadResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity.right_navi.setVisibility(View.GONE);

        ViewVisible = false;

        //暂停刷新线程
        if (myThread!=null && myThread.isAlive() && myThread.getStatus()){
            myThread.pause();
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG,"devicefragment destory");
        super.onDestroy();
        if (myThread!=null && myThread.isAlive()){
            Log.e(TAG,"refresh ui thread will stop...");
            threadRun = false;
        }
        //销毁广播监听
        Log.i(TAG,"receiver unregister ");
        getActivity().unregisterReceiver(mGattUpdateReceiver);
    }

    //接受BLE服务的广播
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            //收到广播
            switch (action){
                case BluetoothLeService.ACTION_GATT_CONNECTING:
                    Log.i(TAG,"bleservice is connecting...");
                    MainActivity.progressBar.setVisibility(View.VISIBLE);
                    break;
                case BluetoothLeService.ACTION_GATT_CONNECTED:
                    Log.i(TAG,getResources().getString(R.string.connected));
                    MainActivity.progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getActivity(),R.string.connected,Toast.LENGTH_SHORT).show();
                    MainActivity.bleState = true;
                    //打开所有控制选项
                    mainswitch.setChecked(true);
                    mainswitchtext.setText(R.string.connected);

                    //开启刷新线程   -- 2016年07月23日21:02:23  不开启刷新线程了 先把连接 断开连接重连  密码这些做好
                    if (myThread == null){
                        Log.i(TAG,"refresh data thread is null .try new it and start");

                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                threadRun = true;
                                myThread = new myThread();
                                myThread.start();
                            }
                        },500);

                    }else{
                        Log.i(TAG,"refresh data thread is newed and pause , restart it");
                        if (myThread.isAlive() && !myThread.getStatus()){
                            myThread.onThreadResume();
                        }
                    }

                    break;
                case BluetoothLeService.ACTION_GATT_DISCONNECTED:
                    //结束掉刷新线程.
                    threadRun = false;
                    myThread = null;
                    MainActivity.progressBar.setVisibility(View.INVISIBLE);
                    Log.i(TAG,getResources().getString(R.string.unconnected));
//                    Toast.makeText(getActivity(),R.string.unconnected,Toast.LENGTH_SHORT).show();
                    MainActivity.bleState = false;
                    mainswitch.setChecked(false);
                    mainswitchtext.setText(R.string.unconnected);
                    //关闭所有控制
                    timeSetInfo.setVisibility(View.GONE);
                    //断开后复位界面
                    adapter.reset();
                    adapter.notifyDataSetChanged();

                    //设置主开关样式
                    mainswitch.setChecked(false);
                    mainswitchtext.setText(R.string.unconnected);
                    //重连逻辑
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(100);

                                if (BluetoothLeService.mySwitch) {
                                    MainActivity.mBluetoothLeService.close();
                                }else {
                                    //myswitch 为假 表示本次断开连接不是人工控制   需要尝试重连.
                                    MainActivity.mBluetoothLeService.connect(sp.getString("mac", ""));
                                }

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                    break;
                case BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED:
                    //发现了蓝牙服务
                    Log.i(TAG,"services discovered...");
                    //读取密码和发送本地保存的密码
                    sendPwdVeryfy();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.mBluetoothLeService.queueRequestCharacteristicValue(3);
                        }
                    },500);
                    break;
                case BluetoothLeService.ACTION_WRITE_COMPLATE:
                    //写入完成后 重新开启读
                    Log.e(TAG,"write complate...");
                    myThread.onThreadResume();
                    break;
                case BluetoothLeService.ACTION_DATA_AVAILABLE:
                    byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    StringBuilder stringBuilder = new StringBuilder(data.length);
                    for (byte b:data){
                        stringBuilder.append(String.format("%02X ", b));
                    }
                    Log.e(TAG,"type:"+intent.getStringExtra("type")+" length:"+data.length+" bytes:contents:"+stringBuilder.toString());
                    switch (intent.getStringExtra("type")){
                        case "status":
                            //刷新列表信息
                            adapter.setUpdateData(data);
                            adapter.notifyDataSetChanged();

                            //定时信息
                            if (data[5]== -1 && data[6] == -1){
                                timeSetInfo.setVisibility(View.GONE);
                                return;
                            }
                            timeSetInfo.setVisibility(View.VISIBLE);
                            //刘总给的计算方法
                            myTime = ((((int) data[6])&0xff) << 8) + (( (int)data[5]) & 0xff);
                            //将秒转倒计时
                            Log.e(TAG,"定时时间是:"+myTime+"秒");
                            hour.setText(myTime/3600+"");
                            int m = myTime/60%60;
                            minute.setText(m+"");
                            second.setText(myTime%60+"");

                            timerSet = myTime; //把更新的时间再放到设置的时间中  再界面变更时  设置新的时间.
                            break;
                        case "control":
                            adapter.setControlData(data);
                            adapter.notifyDataSetChanged();
                            break;
                        case "passwd":
                            //重置密码段
//                            passwordData[0] = (byte) 0xff;
//                            passwordData[1] = (byte) 0xff;
//                            passwordData[2] = (byte) 0xff;
//                            passwordData[3] = (byte) 0xff;
//                            mHandler.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    MainActivity.mBluetoothLeService.queueWriteDataToCharacteristic(passwordData,3);
//                                }
//                            },300);

                            printHexString("device passwd is : ",data);
                            if (new String(data).equals(sp.getString("password",""))) return;
                            Log.e("密码错误","设备密码是:"+new String(data)+"");
                            MainActivity.mBluetoothLeService.mySwitch = true;//标记不再尝试重连
                            //因为密码错误会被终端连接 所以如果发现密码不对 需要立即尝试主动断开连接 并且引导用户重新连接设备
                            AlertDialog.Builder pswAlert = new AlertDialog.Builder(getActivity()).setTitle("安全验证失败").setMessage("密码错误,设备已断开.请重新搜索设备并输入正确密码.如忘记密码,请尝试重置设备").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    sp.edit().putString("mac","").commit();
                                    MainActivity.mBluetoothLeService.disconnect();
                                    MainActivity.mBluetoothLeService.close();
                                }
                            });
                            pswAlert.create().show();
                            break;
                    }
                    break;
                default:
                    Log.e(TAG,action);
                    break;
            }
        }
    };
    /**
     * 全局handler
     */

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 100:
                    break;
                case 101:
                    pdAuto = new ProgressDialog(getActivity());
                    pdAuto.setMessage("正在尝试自动连接,请稍后...");
                    pdAuto.show();
                    break;
                default:
                    break;
            }
        }
    };

    /**+
     * 读取状态刷新线程
     */
    private class myThread extends Thread{

        public boolean getStatus() {
            return status;
        }

        private boolean status = true;

        //刷新线程暂停
        public synchronized void pause(){
            Log.e(TAG,"refresh thread pause.");
            this.status = false;
        }

        //刷新线程恢复
        public synchronized void onThreadResume(){
            Log.e(TAG,"refresh thread resume.");
            this.status = true;
        }


        @Override
        public void run() {
            super.run();
            while (threadRun){
                try {

                    if (status) {
                        Thread.sleep(500);
                        //先刷状态
                        if (MainActivity.mBluetoothLeService!=null) {
                            MainActivity.mBluetoothLeService.queueRequestCharacteristicValue(1);
                        }
//                        Thread.sleep(700);
//                        if (MainActivity.mBluetoothLeService!=null) {
//                            MainActivity.mBluetoothLeService.readCharacteristic(2);
//                        }
                        Thread.sleep(500);
                    } else {
                        Thread.sleep(1000);
                        Log.i(TAG, "刷新线程空状态...");
                    }
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            Log.e("TAG","刷新线程终止");
        }
    }


    private void initView() {
        //初始化的时候让动画旋转
        if (mac.equals("")) MainActivity.progressBar.setVisibility(View.INVISIBLE);
        mainswitch = (Switch) rootView.findViewById(R.id.mainswitch);
        mainswitch.setEnabled(false);
        mainswitchtext = (TextView) rootView.findViewById(R.id.mainswitchtext);
        //初始化listview
        device_listview = (ListView)rootView.findViewById(R.id.device_listview);
        adapter = new DevicesAdapter(getActivity());
        device_listview.setAdapter(adapter);

        //上方文字区域  用于显示设备连接状态 并且快速连接设备
        clickconnect = (RelativeLayout) rootView.findViewById(R.id.clickconnect);
        clickconnect.setOnClickListener(this);
        //密码设置图标
        blepasswordset = (ImageView) rootView.findViewById(R.id.blepasswordset);
        blepasswordset.setOnClickListener(this);
        //定时设置图标
        bletimeset = (ImageView) rootView.findViewById(R.id.bletimeset);
        bletimeset.setOnClickListener(this);
        //定时指示器
        timeSetInfo = (RelativeLayout) rootView.findViewById(R.id.timeSetInfo);
        hour = (TextView) rootView.findViewById(R.id.hour);
        minute = (TextView) rootView.findViewById(R.id.minute);
        second = (TextView) rootView.findViewById(R.id.second);
        offtimeset = (Button) rootView.findViewById(R.id.offtimeset);
        offtimeset.setOnClickListener(this);
    }


    //连接成功后发送密码验证信息
    private void sendPwdVeryfy(){
        String password = sp.getString("password","");
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
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity.mBluetoothLeService.queueWriteDataToCharacteristic(controlData,2);
            }
        },300);
    }
    //设置密码
    private byte[] passwordData = new byte[4];
    private void setPassword(){
        if (!MainActivity.bleState){
            Toast.makeText(getActivity(),"请连接设备",Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder pwdAlert = new AlertDialog.Builder(getActivity());
        pwdAlert.setTitle("安全设置").setMessage("请输入密码");
        final EditText pwdSet = new EditText(getActivity());
        pwdSet.setInputType(InputType.TYPE_CLASS_NUMBER);
        pwdSet.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        pwdAlert.setView(pwdSet);
        pwdAlert.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String pwds = pwdSet.getText().toString().trim();
                if (pwds.length()!=4){
                    Toast.makeText(getActivity(),"输入有误,密码设置失败",Toast.LENGTH_SHORT).show();
                    return;
                }
                //存储新密码  以便以后再次连接
                sp.edit().putString("password",pwds).commit();
                passwordData[0] = (byte) pwds.charAt(0);
                passwordData[1] = (byte) pwds.charAt(1);
                passwordData[2] = (byte) pwds.charAt(2);
                passwordData[3] = (byte) pwds.charAt(3);
                MainActivity.mBluetoothLeService.queueWriteDataToCharacteristic(passwordData,3);
            }
        });
        pwdAlert.setNegativeButton("取消",null);
        pwdAlert.create().show();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.right_navi:
                startActivityForResult(new Intent(getActivity(),BleScanConnectActivity.class),101);
                break;

            case R.id.clickconnect:
                startActivityForResult(new Intent(getActivity(),BleScanConnectActivity.class),101);
                break;
            case R.id.blepasswordset:
                //设置当前设备的连接密码
                setPassword();
                break;
            case R.id.bletimeset:
                //设置定时信息
                setTime(view);
                break;
            case R.id.offtimeset:
                AlertDialog.Builder alertOfftime = new AlertDialog.Builder(getActivity()).setMessage("确定立即关闭定时吗?如需要再次开启,请重新设置定时时间.").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        updataControlData(true);
                    }
                }).setNegativeButton("取消",null);
                alertOfftime.create().show();
                break;
            default:
                Log.e(TAG,view.getId()+"");
                break;
        }
    }

    /**
     * 设置定时信息
     */
    private int timerSet  = 0xFFFF;
    private void setTime(View view) {
        //时间选择器
        pvTime = new TimePickerView(getActivity(), TimePickerView.Type.HOURS_MINS);
        //控制时间范围
        pvTime.setMaxHour(9);
        pvTime.setCyclic(true);
        pvTime.setCancelable(true);
        //时间选择后回调
        pvTime.setOnTimeSelectListener(new TimePickerView.OnTimeSelectListener() {

            @Override
            public void onTimeSelect(Date date) {
                int rs = (date.getHours() * 60 + date.getMinutes()) * 60;
                timerSet = rs;
                updataControlData(false);
            }
        });
        //弹出时间选择器
        pvTime.show();


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode){
            case 1:
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.mBluetoothLeService.disconnect();
                        BluetoothLeService.mySwitch=true;//标记手动连接  连接断开后将不会尝试重新连接
                    }
                },1000);
                break;
            case 2:
                if (!data.getExtras().getString("mac","").equals("")){
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.mBluetoothLeService.connect(data.getExtras().getString("mac",""));
                        }
                    },1000);

                }
                break;
        }

    }

    /**
     * 写入控制
     * @param offTime
     */
    public void updataControlData(boolean offTime){
        //0 1 2 3 4由外部写入
        if (offTime){
            controlData[5] = (byte) 0xff;
            controlData[6] = (byte) 0xff;
        }else{
            //定时信息
            controlData[5] = (byte)  (timerSet & 0xff);
            controlData[6] = (byte) ((timerSet >> 8) & 0xff);
        }
        MainActivity.mBluetoothLeService.queueWriteDataToCharacteristic(controlData,2);
    }



    public static void printHexString(String hint, byte[] b)
    {
        System.out.print(hint);
        for (int i = 0; i < b.length; i++)
        {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1)
            {
                hex = '0' + hex;
            }
            System.out.print(hex.toUpperCase() + " ");
        }
        System.out.println("========");
    }
}
