package com.sunshine.smart.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.Slider;
import com.sunshine.smart.R;
import com.sunshine.smart.activity.MainActivity;
import com.sunshine.smart.fragment.DevicesFragment;

import org.w3c.dom.Text;

/**
 * Created by zhanyonghui on 16/7/27.
 *
 * 操作界面有3种写入处理  分别是  温度调拖动. 开关被点击  通过修改device_fragment的controldata然后直接提交写入特征
 *
 */
public class DevicesAdapter extends BaseAdapter {

    private String TAG = DevicesAdapter.class.getName();
    private Context context;
    private byte[] data;
    private byte[] controlData;
    //记录开关信息
    private int x ;

    public DevicesAdapter(Context c) {
        this.context = c;
        controlData = new byte[4];
        controlData[0] = 42;
        controlData[1] = 42;
        controlData[2] = 42;
        controlData[3] = 42;
    }

    public void setUpdateData(byte[] data) {
        this.data = data;
        this.x = data[4];
        //默认就是当前状态
        DevicesFragment.controlData[4] = (byte) x;

    }

    public void setControlData(byte[] controlData){
        this.controlData = controlData;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {

        ViewHolder viewHolder ;
        if (view==null && controlData!=null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_ble_devices, null);
            viewHolder = new ViewHolder();
            viewHolder.blename = (TextView) view.findViewById(R.id.blename);
            viewHolder.bleon = (ImageView) view.findViewById(R.id.bleon);
            viewHolder.tmp = (TextView) view.findViewById(R.id.tmp);
            viewHolder.bleswith = (ImageView) view.findViewById(R.id.bleswith);
            viewHolder.ble = (LinearLayout) view.findViewById(R.id.ble);
            viewHolder.slider = (SeekBar) view.findViewById(R.id.slider);
            viewHolder.nowtmp = (TextView)view.findViewById(R.id.nowtmp);
            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) view.getTag();
        }
//        if (data==null)return view;
        //设置文本信息
        viewHolder.blename.setText(R.string.switch1);
        //设置温度滑条
        if (viewHolder.slider.getTag()==null){
            viewHolder.slider.setProgress(0);
            //设置了写入的前四位
            DevicesFragment.controlData[i] = (byte) 42;
            viewHolder.nowtmp.setText(42+"°C");
            viewHolder.slider.setTag("set");
        }

        final ViewHolder finalViewHolder = viewHolder;
        viewHolder.slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int tmp, boolean b) {
                finalViewHolder.nowtmp.setText((tmp+42)+"°C");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if ((data[4] & (0b00000001 << i)) <= 0||data==null){
                    AlertDialog.Builder er = new AlertDialog.Builder(context).setMessage("请先开启温度控制");
                    er.create().show();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //只要有变化 就修改了写入特征
                DevicesFragment.controlData[i] = (byte) (seekBar.getProgress()+42);
                //发送写入 ???  开关信息在这里已经初始化 这里不需要再改
                DevicesFragment.getInstance().updataControlData(false);
            }
        });
        if (data == null){
            data = new byte[7];
            data[4] = 0;
        }

        // 依次判断开关
        if ((data[4] & (0b00000001 << i)) > 0){
            viewHolder.bleon.setImageResource(R.mipmap.bleon);
            viewHolder.tmp.setText(String.format(context.getResources().getString(R.string.currenttmp),data[i]));
            viewHolder.bleswith.setImageResource(R.mipmap.knob);
            viewHolder.ble.setVisibility(View.VISIBLE);
            viewHolder.bleswith.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //这里就是关闭了
                    Log.e(TAG,"关闭");
                    x = x - (1 << i);
                    DevicesFragment.controlData[4] = (byte) x;
                    //发送写入 ???  ???温度调信息呢?  调温信息已经写入 在这里并没有修改  现在不需要处理.
                    DevicesFragment.getInstance().updataControlData(false);
                }
            });
        }else{
            viewHolder.bleon.setImageResource(R.mipmap.bleoff);
            if (data[i]==0){
                viewHolder.tmp.setText(R.string.currenttmpnull);
            }else {
                viewHolder.tmp.setText((data[i]== -1) ? String.format(context.getResources().getString(R.string.sensor_abnormal)) : String.format(context.getResources().getString(R.string.currenttmp), data[i]));
            }
            viewHolder.bleswith.setImageResource(R.mipmap.knob1);
            viewHolder.ble.setVisibility(View.GONE);
            viewHolder.bleswith.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e(TAG,"开启");
                    x = x + (1 << i);
                    DevicesFragment.controlData[4] = (byte) x;
                    //发送写入 ???
                    DevicesFragment.getInstance().updataControlData(false);
                }
            });
        }
        return view;
    }

    public void reset() {
        this.data = null;
        this.controlData = null;
        Log.e(TAG,"清楚数据,刷新listview");
    }

    class ViewHolder{
        TextView blename;   //名称  传感器1
        ImageView bleon;    //开关标示图
        TextView tmp;       //温度显示信息
        ImageView bleswith; //开关
        LinearLayout ble;   //设置温度区域
        SeekBar slider;      //设置温度滑条
        TextView nowtmp;    //当前设置的温度
    }
}
