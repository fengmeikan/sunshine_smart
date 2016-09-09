package com.sunshine.smart.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.sunshine.smart.R;

import java.util.ArrayList;
import java.util.List;

public class DeviceSelectActivity extends AppCompatActivity {

    List<String> list ;
    int[] namelist;
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_select);
        listView = (ListView) findViewById(R.id.listView2);
        list = new ArrayList<String>();
        final int cate = getIntent().getIntExtra("cate",0);
        //2个分类下面的小类
        final int[] a = new int[]{R.string.smart_waistcoat,R.string.smart_suit,R.string.smart_cheongsam,R.string.smart_coat,R.string.smart_trousers};
        int[] b = new int[]{R.string.smart_gloves,R.string.smart_neckguard,R.string.smart_waist_support,R.string.smart_shoulder,R.string.smart_knee,R.string.smart_legguard};

        if (cate!=0){
            if (cate == 1){ //时尚服饰
                namelist = a;
            }
            if (cate == 2){ //健康养生
                namelist = b;
            }
            Adapter adapter = new Adapter(namelist);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(DeviceSelectActivity.this,DeviceActivity.class);
                    intent.putExtra("name",namelist[i]);
                    //当分类为2的时候  类型ID堆积
                    intent.putExtra("type",cate==1?i+1:i+1+a.length);
                    startActivity(intent);
                }
            });
        }

    }

    class Adapter extends BaseAdapter{

        int data[];

        public Adapter(int[] d) {
            this.data = d;
        }

        @Override
        public int getCount() {
            return data.length;
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
        public View getView(int i, View view, ViewGroup viewGroup) {
            View root = LayoutInflater.from(DeviceSelectActivity.this).inflate(R.layout.list_selectdevece,null);
            TextView name = (TextView) root.findViewById(R.id.catename);
            name.setText(data[i]);
            return root;
        }
    }
}
