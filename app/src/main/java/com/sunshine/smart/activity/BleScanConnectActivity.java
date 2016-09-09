package com.sunshine.smart.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sunshine.smart.R;
import com.sunshine.smart.Service.BluetoothLeService;
import com.sunshine.smart.utils.Constants;
import com.sunshine.smart.utils.DbDao;
import com.sunshine.smart.utils.Device;

import java.util.ArrayList;

/**
 * BLE设备搜索界面
 */
public class BleScanConnectActivity extends BaseActivity implements View.OnClickListener {

    private String TAG = BleScanConnectActivity.class.getName();
    private ListView listView;
    private ImageView right_navi;
    private BluetoothManager bleService;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private ProgressDialog dialog;
    private LeDeviceListAdapter mLeDeviceListAdapter;//更新设备列表的adapter
    private SharedPreferences.Editor editor;
    private String mac;
    private BluetoothDevice de;
    private SharedPreferences sp;
    private ImageView left_navi;
    private ProgressBar progressBar;
    private boolean mScanning;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_scan_connect);
        sp = getSharedPreferences(Constants.SETTING,Context.MODE_PRIVATE);
        mHandler = new mHandler();
        mac = getSharedPreferences(Constants.SETTING, Context.MODE_PRIVATE).getString("mac","");

        init();
        initView();
        update();
        scanLeDevice(true);
    }

    private void init() {
        bleService = (BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bleService.getAdapter();
        if (mBluetoothAdapter==null){
            Toast.makeText(this,R.string.nonsupportble,Toast.LENGTH_SHORT).show();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable();
        }
    }


    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mScanning = false;
//                    progressBar.setVisibility(View.GONE);
//                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
////                    invalidateOptionsMenu();
//                }
//            }, SCAN_PERIOD);
            mScanning = true;
            progressBar.setVisibility(View.VISIBLE);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            progressBar.setVisibility(View.GONE);
            progressBar.setVisibility(View.INVISIBLE);
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
//        invalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mScanning)scanLeDevice(false);
    }

    private void initView() {
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        listView = (ListView) findViewById(R.id.listView);
        left_navi = (ImageView) findViewById(R.id.left_navi);
        left_navi.setImageResource(R.mipmap.back);
        left_navi.setOnClickListener(this);
        right_navi = (ImageView) findViewById(R.id.right_navi);
        right_navi.setVisibility(View.VISIBLE);
        right_navi.setImageResource(android.R.drawable.ic_menu_search);
        right_navi.setOnClickListener(this); //搜索条
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(mLeDeviceListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final BluetoothDevice device = mLeDeviceListAdapter.getDevice(i);
                if (mScanning){
                    scanLeDevice(false);
                }
                AlertDialog.Builder alert = new AlertDialog.Builder(BleScanConnectActivity.this);
                if (sp.getString("mac","").equals(device.getAddress()) && MainActivity.bleState){
                    alert.setMessage("确认断开当前连接吗?");
                    alert.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
//                            editor = sp.edit();
//                            editor.putString("mac","");
//                            editor.putString("name","");
//                            editor.putString("password","");
//                            editor.commit();
                            // 不再删除mac地址 和密码.
                            mLeDeviceListAdapter.clear();
                            mLeDeviceListAdapter.notifyDataSetChanged();
                            setResult(1);
                            BleScanConnectActivity.this.finish();
                        }
                    });
                }else{
                    //当选择的设备 已经连接过 而且密码已经存在    就不输入密码了.直接连接
                    if (sp.getString("mac","").equals(device.getAddress()) &&  !sp.getString("password","").equals("")){
                        alert.setMessage("是否连接设备吗?");
                        alert.setPositiveButton("连接", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent idata = new Intent();
                                idata.putExtra("mac",device.getAddress());
                                setResult(2,idata);
                                BluetoothLeService.mySwitch = false;//取消手动连接开关 断开后重连
                                BleScanConnectActivity.this.finish();
                            }
                        });
                    }else{
                        alert.setMessage(R.string.connectionprompt);
                        final EditText pwd = new EditText(BleScanConnectActivity.this);
                        pwd.setInputType(InputType.TYPE_CLASS_NUMBER);
                        pwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
                        alert.setView(pwd);
                        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String pwds1 = pwd.getText().toString().trim();
                                if (pwds1.equals("")|| pwds1.length()!=4){
                                    Toast.makeText(BleScanConnectActivity.this,R.string.connectionprompt,Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                //如果现在有连接  断开他
                                if (!mac.equals("") && MainActivity.bleState){
                                    MainActivity.mBluetoothLeService.disconnect();
                                    MainActivity.mBluetoothLeService.close();
                                }
                                //新策略   点击设备后  将设备添加进数据库 准备连接
                                Device device1 = new Device(device.getAddress(),device.getName(),pwds1,Integer.parseInt(device.getName().substring(5)));
                                DbDao dbdao = new DbDao(BleScanConnectActivity.this);
                                if (dbdao.addDevice(device1)>0){
                                    Log.e(TAG,"device insert into sqlite success...");
                                }

                                editor = sp.edit();
                                editor.putString("mac",device.getAddress());
                                editor.putString("name",device.getName());
                                editor.putString("password",pwds1);
                                editor.commit();
                                Intent idata = new Intent();
                                idata.putExtra("mac",device.getAddress());
                                setResult(2,idata);
                                BluetoothLeService.mySwitch = false;//取消手动连接开关 断开后重连
                                BleScanConnectActivity.this.finish();
                            }
                        });
                    }

                }
                alert.setNegativeButton(R.string.cancel,null);
                alert.create().show();
            }
        });

    }

    /**+
     * 加入已经连接的设备
     */
    private void update() {
//        if ( !mac.equals("") &&  MainActivity.bleState){
//            Log.e(TAG,"地址:"+mac+"的连接状态:"+MainActivity.bleState+"");
//            de = MainActivity.mBluetoothLeService.getmBluetoothAdapter().getRemoteDevice(mac);
//            mLeDeviceListAdapter.addDevice(de,0);
//            Log.i("device",de.toString());
//            mLeDeviceListAdapter.notifyDataSetChanged();
//        }
    }




    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.left_navi:
                if (mScanning){
                    scanLeDevice(false);
                }
                this.finish();
                break;
            case R.id.right_navi:
                //清除之前结果
                if (mScanning){
                    Log.e(TAG,"stop scanning...");
                    progressBar.setVisibility(View.GONE);
                    scanLeDevice(false);
                }else{
                    Log.e(TAG,"start scanning...");
                    progressBar.setVisibility(View.INVISIBLE);
                    update();
                    scanLeDevice(true);
                }

                break;
        }
    }


    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    Log.e(TAG,device.getName()+"");
                    //验证设备名称 不以HDL开头的取消显示
                    if (device.getName()!=null &&device.getName().startsWith("hdl")) {
                        mLeDeviceListAdapter.addDevice(device, rssi);
                    }
                }
            };



    private class mHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    if (dialog!=null && dialog.isShowing()){
                        dialog.dismiss();
                    }
                    break;

            }
        }
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private final ArrayList<Integer> mRssi;
        private ArrayList<BluetoothDevice> mLeDevices;
        private ArrayList<String> deviceaddress;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mRssi = new ArrayList<Integer>();
            mLeDevices = new ArrayList<BluetoothDevice>();
            deviceaddress = new ArrayList<String>();
            mInflator = BleScanConnectActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device,int i) {
            if (!deviceaddress.contains(device.getAddress())){
                Log.i(TAG,device.getAddress()+"---"+device.getName());
                deviceaddress.add(device.getAddress());
                mLeDevices.add(device);
                mRssi.add(i);
                Log.e(TAG,"notify");
                notifyDataSetChanged();
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            deviceaddress.clear();
            mLeDevices.clear();
            mRssi.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }


        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;

                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);


            BluetoothDevice device = mLeDevices.get(i);
            Log.i(TAG,device.getName()+":---:"+device.getAddress()+"===="+mLeDevices.size());
            final String deviceName = device.getName();

            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknow_device);

            if (mac.equals(device.getAddress()) && MainActivity.bleState){

                viewHolder.deviceAddress.setText(getType(device)+"---"+getResources().getString(R.string.isconnected));
            }else{
                viewHolder.deviceAddress.setText(getType(device)+"   "+getResources().getString(R.string.signal_strength)+":"+mRssi.get(i));
            }

            return view;
        }
    }

    public String getType(BluetoothDevice device){
        int typeName;

        int j = Integer.parseInt(device.getName().substring(5));

        switch (j){
            case 1:
                typeName = R.string.smart_waistcoat;
                break;
            case 2:
                typeName = R.string.smart_suit;
                break;
            case 3:
                typeName = R.string.smart_cheongsam;
                break;
            case 4:
                typeName = R.string.smart_coat;
                break;
            case 5:
                typeName = R.string.smart_trousers;
                break;
            case 6:
                typeName = R.string.smart_gloves;
                break;
            case 7:
                typeName = R.string.smart_neckguard;
                break;
            case 8:
                typeName = R.string.smart_waist_support;
                break;
            case 9:
                typeName = R.string.smart_waist_support;
                break;
            case 10:
                typeName = R.string.smart_knee;
                break;
            case 11:
                typeName = R.string.smart_legguard;
                break;
            default:
                typeName = R.string.smart_cheongsam;
                break;
        }
        return getResources().getString(typeName);
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
