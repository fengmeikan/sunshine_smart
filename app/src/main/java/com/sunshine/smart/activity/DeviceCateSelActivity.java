package com.sunshine.smart.activity;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunshine.smart.R;

public class DeviceCateSelActivity extends AppCompatActivity implements View.OnClickListener{

    Button btn1 ,btn2;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_cate_sel);
        btn1 = (Button) findViewById(R.id.button12);
        btn2 = (Button) findViewById(R.id.button13);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        intent = new Intent(this,DeviceSelectActivity.class);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button12:
                intent.putExtra("cate",1);
                startActivity(intent);
                DeviceCateSelActivity.this.finish();
                break;
            case R.id.button13:
                intent.putExtra("cate",2);
                startActivity(intent);
                DeviceCateSelActivity.this.finish();
                break;
        }
    }
}
