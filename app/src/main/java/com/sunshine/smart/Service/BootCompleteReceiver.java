package com.sunshine.smart.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * 开机完成广播
 *
 */
public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, StepService.class);
        context.startService(i);
        Log.e("BootCompleteReceiver:","广播接收");
    }
}
