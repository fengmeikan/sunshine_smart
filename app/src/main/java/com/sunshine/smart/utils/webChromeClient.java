package com.sunshine.smart.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * Created by mac on 16/7/13.
 */
public class webChromeClient extends WebChromeClient {

    private WebView webView;
    private Context context;
    private ProgressDialog progressDialog;

    public webChromeClient(Context c,WebView w) {
        webView = w;
        context = c;
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("正在加载...");
        progressDialog.setCancelable(false);
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        if (newProgress == 100){
            webView.setVisibility(View.VISIBLE);
            progressDialog.dismiss();
        }else{
            if (!progressDialog.isShowing())progressDialog.show();
        }
    }


}
