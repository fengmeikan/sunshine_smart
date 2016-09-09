package com.sunshine.smart.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunshine.smart.R;
import com.sunshine.smart.utils.Constants;
import com.sunshine.smart.utils.webChromeClient;

public class WebviewActivity extends BaseActivity implements View.OnClickListener {

    private WebView webview;
    private TextView titile;
    private ImageView left_navi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        initView();
    }

    private void initView() {
        Intent intent = getIntent();
        String url = intent.getExtras().getString(Constants.URL, "");
        String type = intent.getExtras().getString(Constants.TYPE,"");
        left_navi = (ImageView) findViewById(R.id.left_navi);
        left_navi.setOnClickListener(this);
        webview = (WebView) findViewById(R.id.webView);
        titile = (TextView) findViewById(R.id.textView);
        switch (type){
            case "about":
                titile.setText(R.string.about);
                break;
        }

        WebSettings setting = webview.getSettings();
        setting.setJavaScriptEnabled(true);
        webview.setWebViewClient(new WebViewClient());
        webview.setWebChromeClient(new webChromeClient(this,webview));
        if (!url.equals("")) {
            webview.loadUrl(url);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.left_navi:
                WebviewActivity.this.finish();
                break;

        }
    }
}
