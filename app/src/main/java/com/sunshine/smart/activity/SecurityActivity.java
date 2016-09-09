package com.sunshine.smart.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunshine.smart.R;

public class SecurityActivity extends BaseActivity implements View.OnClickListener {


    private ImageView left_navi;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);

        left_navi = (ImageView) findViewById(R.id.left_navi);
        left_navi.setOnClickListener(this);
        title = (TextView) findViewById(R.id.textView);
        title.setText(R.string.security);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.left_navi:
                SecurityActivity.this.finish();
                break;
        }
    }

}
