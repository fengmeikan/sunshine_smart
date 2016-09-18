package com.sunshine.smart.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.utils.Utils;
import com.sunshine.smart.R;
import com.sunshine.smart.SmartApplication;
import com.sunshine.smart.utils.Constants;
import com.sunshine.smart.utils.HttpUtils;
import com.sunshine.smart.utils.ScreenUtils;
import com.sunshine.smart.widget.DialogShows;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.request.HttpRequest;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyinfoActivity extends BaseActivity implements View.OnClickListener, DialogShows.ClickOKLintener, SmartApplication.xUtilsHttp, DialogShows.EditInputLintener {


    private ImageView left_navi,right_navi;
    private TextView title;

    private String sex;
    private String age;
    private String height;
    private String weight;
    private String nick;

    private List<String> sexlist;   //性别列表
    private List<String> agelist;   //年龄列表
    private List<String> heightlist;//身高列表
    private List<String> weightlist;//体重列表

    private Uri mOutPutFileUri;
    private final String IMAGE_FILE_NAME = "/faceImage.jpg";

    private String USER_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myinfo);

        USER_ID = shared.getString(Constants.USER_ID, "");
        sex = shared.getString(Constants.SEX, "男");
        age = shared.getString(Constants.AGE, "20");
        height = shared.getString(Constants.HEIGHT, "160");
        weight = shared.getString(Constants.WEIGHT, "45");
        nick = shared.getString(Constants.NICK, "昵称");

        left_navi = (ImageView) findViewById(R.id.left_navi);
        left_navi.setOnClickListener(this);
//        right_navi = (ImageView) findViewById(R.id.right_navi);
//        right_navi.setOnClickListener(this);
        title = (TextView) findViewById(R.id.textView);
        title.setText(R.string.profile);

        InitView();

    }

    private LinearLayout linear_avatar,linear_nickname,linear_sex,linear_age,linear_height,linear_weight;
    private TextView text_sex,text_age,text_height,text_weight,edit_nicknage;
    private ImageView avatar;

    private void InitView() {
        linear_avatar = (LinearLayout) this.findViewById(R.id.linear_avatar);
        linear_nickname = (LinearLayout) this.findViewById(R.id.linear_nickname);
        linear_sex = (LinearLayout) this.findViewById(R.id.linear_sex);
        linear_age = (LinearLayout) this.findViewById(R.id.linear_age);
        linear_height = (LinearLayout) this.findViewById(R.id.linear_height);
        linear_weight = (LinearLayout) this.findViewById(R.id.linear_weight);

        linear_avatar.setOnClickListener(this);
        linear_nickname.setOnClickListener(this);
        linear_sex.setOnClickListener(this);
        linear_age.setOnClickListener(this);
        linear_height.setOnClickListener(this);
        linear_weight.setOnClickListener(this);

        text_sex = (TextView) this.findViewById(R.id.sex);
        text_age = (TextView) this.findViewById(R.id.age);
        text_height = (TextView) this.findViewById(R.id.height);
        text_weight = (TextView) this.findViewById(R.id.weight);

        avatar = (ImageView) this.findViewById(R.id.avatar);

        edit_nicknage = (TextView) this.findViewById(R.id.nickname);

        text_sex.setText("1".endsWith(sex)?"男":"女");
        text_age.setText(age+"岁");
        text_height.setText(height+"cm");
        text_weight.setText(weight+"kg");
        edit_nicknage.setText(nick);
        //+++++++++++建立显示数据初始+++++++++++++
        sexlist = new ArrayList<String>();
        sexlist.add("男");
        sexlist.add("女");

        agelist = new ArrayList<String>();
        for (int i=12;i<100;i++){
            agelist.add(i+"");
        }
        heightlist = new ArrayList<String>();
        for (int i=100;i<200;i++){
            heightlist.add(i+"");
        }
        weightlist = new ArrayList<String>();
        for (int i=10;i<=200;i++){
            weightlist.add(i+"");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.left_navi:
                MyinfoActivity.this.finish();
                break;
            case R.id.linear_avatar:
                ShowPhotoSelect();
                break;
            case R.id.linear_nickname:
                DialogShows.getInstance(this).showEditDialog(this,"昵称").setEditInputOKLintener(this);
                break;
            case R.id.linear_sex:
                DialogShows.getInstance(this).showPackSelect(this,sexlist,"","男").setDialogShowsOKLintener(this);
                break;
            case R.id.linear_age:
                age = text_age.getText().toString().substring(0,text_age.getText().toString().length()-1);
                DialogShows.getInstance(this).showPackSelect(this,agelist, "岁",age).setDialogShowsOKLintener(this);
                break;
            case R.id.linear_height:
                height = text_height.getText().toString().substring(0,text_height.getText().toString().length()-2);
                DialogShows.getInstance(this).showPackSelect(this,heightlist, "cm",height).setDialogShowsOKLintener(this);
                break;
            case R.id.linear_weight:
                weight = text_weight.getText().toString().substring(0,text_weight.getText().toString().length()-2);
                DialogShows.getInstance(this).showPackSelect(this,weightlist, "kg",weight).setDialogShowsOKLintener(this);
                break;
        }
    }

    @Override
    public void ClickOK(DialogShows dialog,String selected,String str) {
        HashMap<String,String> param = new HashMap<String,String>();
        param.put("action","User.updateUser");
        param.put("is",USER_ID);
        switch (str){
            case "":
                if ("男".equals(selected)){
                    sex = "1";
                }else{
                    sex = "2";
                }
                text_sex.setText(selected);
                param.put("sex", sex + "");
                break;
            case "岁":
                age =  selected;
                text_age.setText(selected+"岁");
                param.put("age", age);
                break;
            case "cm":
                height =  selected;
                text_height.setText(selected+"cm");
                param.put("height", height);
                break;
            case "kg":
                weight =  selected;
                text_weight.setText(selected+"kg");
                param.put("weight", weight);
                break;
        }
        new HttpUtils(this).post(param,0);
    }

    @Override
    public void jsonResponse(JSONObject json, int tag) {

    }

    @Override
    public void jsonResponseError() {

    }


    public void ShowPhotoSelect() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(5, 5, 5, 5);

        TextView textView1 = new TextView(this);
        textView1.setPadding(10, 15, 10, 15);
        textView1.setText("相册");
        textView1.setClickable(true);
        textView1.setGravity(Gravity.CENTER);
        textView1.setBackgroundResource(R.drawable.blue_bg);
        textView1.setTextSize(24);
        textView1.setTextColor(getResources().getColor(R.color.colorBrown));

        TextView textView2 = new TextView(this);
        textView2.setPadding(10, 15, 10, 15);
        textView2.setText("拍照");
        textView2.setClickable(true);
        textView2.setGravity(Gravity.CENTER);
        textView2.setBackgroundResource(R.drawable.blue_bg);
        textView2.setTextSize(24);
        textView2.setTextColor(getResources().getColor(R.color.colorBrown));

        TextView textView3 = new TextView(this);
        textView3.setPadding(10, 15, 10, 15);
        textView3.setText("取消");
        textView3.setClickable(true);
        textView3.setGravity(Gravity.CENTER);
        textView3.setBackgroundResource(R.drawable.blue_bg);

        textView3.setTextSize(24);
        textView3.setTextColor(getResources().getColor(R.color.thingrey));

        linearLayout.addView(textView1);
        linearLayout.addView(textView2);
        linearLayout.addView(textView3);
        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ll.weight = 1;
        textView1.setLayoutParams(ll);
        textView2.setLayoutParams(ll);

        dialog.create();
        final AlertDialog alert = dialog.show();
        alert.setContentView(linearLayout);

        Window window = alert.getWindow();
        window.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams wmlp = window.getAttributes();
        wmlp.width = ScreenUtils.getScreenWidth(this);
        window.setAttributes(wmlp);


        textView1.setOnClickListener(new View.OnClickListener() {//相册
            @Override
            public void onClick(View v) {
                Intent openAlbumIntent = new Intent(Intent.ACTION_PICK);
                openAlbumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(openAlbumIntent, 201);
                alert.dismiss();
            }
        });
        textView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//拍照
                //文件夹aaaa
                String path = Environment.getExternalStorageDirectory()+ "/sunshine/image";
                File path1 = new File(path);
                if (!path1.exists()) {
                    path1.mkdirs();
                }
                File Avatarfile = new File(path1, IMAGE_FILE_NAME);
                mOutPutFileUri = Uri.fromFile(Avatarfile);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//action is capture
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mOutPutFileUri);
                startActivityForResult(intent, 204);//头像拍照
                alert.dismiss();
            }
        });
        textView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode ==201)){
            startPhotoZoom(data.getData());
        }else if(requestCode ==204){
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                    File tempFile = new File(Constant.IMAGEPATH);
                Log.i("faceimage", Environment.getExternalStorageDirectory()+ "/sunshine/image" + IMAGE_FILE_NAME);
                startPhotoZoom(mOutPutFileUri);
            } else {
                Toast.makeText(this, "未找到存储卡，无法存储照片！", Toast.LENGTH_LONG).show();
            }
        }else if(requestCode ==2){
            getImageToView(data);
        }
    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 320);
        intent.putExtra("outputY", 320);
        intent.putExtra("return-data", true);
        Log.i("裁剪图片", "12541231231231");
        startActivityForResult(intent, 2);
    }


    /**
     * 保存裁剪之后的图片数据
     *
     * @param picdata
     */
    Bitmap photo;
    private void getImageToView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            photo = extras.getParcelable("data");
            Drawable drawable = new BitmapDrawable(photo);
            avatar.setImageDrawable(drawable);
            String path = Environment.getExternalStorageDirectory()+ "/sunshine";
            File dirFile = new File(path);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            try {
                File myCaptureFile = new File(Environment.getExternalStorageDirectory()+ "/sunshine/icon.png");
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
                photo.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                bos.flush();
                bos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            HashMap<String,String> param = new HashMap<String,String>();
            param.put("action","User.updateUser");
            param.put("id",USER_ID);
            param.put("icon", Environment.getExternalStorageDirectory() + "/sunshine/icon.png");
            new HttpUtils(this).post(param, 0);
        }
    }

    @Override
    public void OnEditInput(DialogShows dialog, String input) {
        edit_nicknage.setText(input);
        HashMap<String,String> param = new HashMap<String,String>();
        param.put("action","User.updateUser");
        param.put("id",USER_ID);
        param.put("nick", input);
        new HttpUtils(this).post(param, 0);
    }
}
