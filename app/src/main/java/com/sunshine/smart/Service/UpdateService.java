package com.sunshine.smart.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.RemoteViews;

import com.sunshine.smart.R;
import com.sunshine.smart.SmartApplication;
import com.sunshine.smart.activity.MainActivity;
import com.sunshine.smart.utils.HttpUtils;

import org.xutils.common.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Created by fengm on 2016/9/16.
 */
public class UpdateService extends Service implements SmartApplication.xUtilsHttpFile {
    private static String down_url; // = "http://192.168.1.112:8080/360.apk";
    private static final int DOWN_OK = 1; // 下载完成
    private static final int DOWN_ERROR = 0;

    private String app_name;

    private NotificationManager notificationManager;
    private Notification notification;

    private Intent updateIntent;
    private PendingIntent pendingIntent;
    private String updateFile;

    private int notification_id = 1;
    long totalSize = 0;// 文件总大小
    /***
     * 更新UI
     */
    final Handler handler = new Handler() {
        @SuppressWarnings("deprecation")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_OK:
                    // 下载完成，点击安装
                    Intent installApkIntent = getFileIntent(new File(updateFile));
                    pendingIntent = PendingIntent.getActivity(UpdateService.this, 0, installApkIntent, 0);
                    builder.setContentTitle(app_name);
                    builder.setContentText("下载完成");
                    builder.setSmallIcon(R.mipmap.ic_launcher);
                    builder.setDeleteIntent(pendingIntent);
                    notification = builder.getNotification();
                    notification.flags |= Notification.FLAG_AUTO_CANCEL;
                    notificationManager.notify(notification_id, notification);
                    stopService(updateIntent);
                    break;
                case DOWN_ERROR:
                    builder.setContentTitle(app_name);
                    builder.setContentText("下载失败");
                    builder.setSmallIcon(R.mipmap.ic_launcher);
                    builder.setDeleteIntent(pendingIntent);
                    notification = builder.getNotification();
                    notification.flags |= Notification.FLAG_AUTO_CANCEL;
                    notificationManager.notify(notification_id, notification);
                    break;
                default:
                    stopService(updateIntent);
                    break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            try {
                app_name = intent.getStringExtra("app_name");
                down_url = intent.getStringExtra("downurl");
                // 创建通知
                createNotification();
                // 开始下载
                downloadUpdateFile(down_url, "haodeli.apk");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /***
     * 创建通知栏
     */
    RemoteViews contentView;
    Notification.Builder builder;

    @SuppressWarnings("deprecation")
    public void createNotification() {

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new Notification.Builder(this);
        builder.setContentTitle(app_name);
        builder.setContentText("准备下载");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        notification = builder.getNotification();

        /***
         * 在这里我们用自定的view来显示Notification
         */
        contentView = new RemoteViews(getPackageName(), R.layout.notification_item);
//        contentView.setTextViewText(R.id.notificationTitle, app_name);
        contentView.setTextViewText(R.id.notificationPercent, "0%");
        contentView.setProgressBar(R.id.notificationProgress, 100, 0, false);
        contentView.setImageViewResource(R.id.notificationImage, R.mipmap.ic_launcher);
        notification.contentView = contentView;

        updateIntent = new Intent(this, MainActivity.class);
        updateIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0, updateIntent, 0);

        notification.contentIntent = pendingIntent;
        notificationManager.notify(notification_id, notification);
    }

    /***
     * 下载文件
     */
    public void downloadUpdateFile(String down_url, String file){
        updateFile = file;
        HttpUtils HttpUtils = new HttpUtils(this,0);
        HttpUtils.DownloadAPK(down_url, file);
    }
    // 下载完成后打开安装apk界面
    public static void installApk(File file, Context context) {
        //L.i("msg", "版本更新获取sd卡的安装包的路径=" + file.getAbsolutePath());
        Intent openFile = getFileIntent(file);
        context.startActivity(openFile);

    }

    public static Intent getFileIntent(File file) {
        Uri uri = Uri.fromFile(file);
        String type = getMIMEType(file);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, type);
        return intent;
    }


    public static String getMIMEType(File f) {
        String type = "";
        String fName = f.getName();
        // 取得扩展名
        String end = fName
                .substring(fName.lastIndexOf(".") + 1, fName.length());
        if (end.equals("apk")) {
            type = "application/vnd.android.package-archive";
        } else {
            // /*如果无法直接打开，就跳出软件列表给用户选择 */
            type = "*/*";
        }
        return type;
    }

    @Override
    public void UpdateProgress(long total, long current, boolean isDownloading) {
        double x_double = current * 1.0;
        double tempresult = x_double / total;
        DecimalFormat df1 = new DecimalFormat("0.00"); // ##.00%
        // 百分比格式，后面不足2位的用0补齐
        String result = df1.format(tempresult);
        contentView.setTextViewText(R.id.notificationPercent, (int) (Float.parseFloat(result) * 100) + "%");
        contentView.setProgressBar(R.id.notificationProgress, 100, (int) (Float.parseFloat(result) * 100), false);
        notificationManager.notify(notification_id, notification);
    }

    @Override
    public void Start() {

    }

    @Override
    public void jsonResponse(File file,String SavePath) {
        updateFile = SavePath;
        handler.sendEmptyMessage(1);
        installApk(file, this);
    }

    @Override
    public void jsonResponseError() {
        handler.sendEmptyMessage(0);
    }
}
