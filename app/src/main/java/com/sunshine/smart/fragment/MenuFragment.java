package com.sunshine.smart.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunshine.smart.R;
import com.sunshine.smart.activity.BleScanConnectActivity;
import com.sunshine.smart.activity.FeedbackActivity;
import com.sunshine.smart.activity.HelpActivity;
import com.sunshine.smart.activity.LoginActivity;
import com.sunshine.smart.activity.MainActivity;
import com.sunshine.smart.activity.MyinfoActivity;
import com.sunshine.smart.activity.SecurityActivity;
import com.sunshine.smart.activity.SettingActivity;
import com.sunshine.smart.activity.WebviewActivity;
import com.sunshine.smart.utils.Constants;

import org.xutils.common.Callback;
import org.xutils.image.ImageOptions;
import org.xutils.x;

import de.hdodenhof.circleimageview.CircleImageView;

public class MenuFragment extends Fragment implements View.OnClickListener {

    private CircleImageView usericon;
    private TextView username,help;
    private SharedPreferences sp;
    private RelativeLayout index,devices,myinfo,securitycenter,setting,aboutapp,backfeed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getActivity().getSharedPreferences(Constants.USER, Context.MODE_PRIVATE);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_menu, null);
        initView(rootView);
        return rootView;
    }

    private void initView(View rootView) {
        usericon = (CircleImageView) rootView.findViewById(R.id.usericon);
        username = (TextView) rootView.findViewById(R.id.username);
        usericon.setOnClickListener(this);
        username.setOnClickListener(this);
        help = (TextView) rootView.findViewById(R.id.help);
        index = (RelativeLayout) rootView.findViewById(R.id.index);
        devices = (RelativeLayout) rootView.findViewById(R.id.devices);
        myinfo = (RelativeLayout) rootView.findViewById(R.id.myinfo);
        securitycenter = (RelativeLayout) rootView.findViewById(R.id.securitycenter);
        setting = (RelativeLayout) rootView.findViewById(R.id.setting);
        aboutapp = (RelativeLayout) rootView.findViewById(R.id.aboutapp);
        backfeed = (RelativeLayout) rootView.findViewById(R.id.backfeed);

        usericon.setOnClickListener(this);
        index.setOnClickListener(this);
        devices.setOnClickListener(this);
        myinfo.setOnClickListener(this);
        securitycenter.setOnClickListener(this);
        setting.setOnClickListener(this);
        aboutapp.setOnClickListener(this);
        backfeed.setOnClickListener(this);
        help.setOnClickListener(this);

        if (sp.getBoolean(Constants.IS_LOGIN,false)) {

            x.image().loadDrawable(sp.getString(Constants.ICON,""),ImageOptions.DEFAULT,new Callback.CommonCallback<Drawable>(){

                @Override
                public void onSuccess(Drawable result) {
                    usericon.setImageDrawable(result);
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {

                }

                @Override
                public void onCancelled(CancelledException cex) {

                }

                @Override
                public void onFinished() {

                }
            });
            username.setText(sp.getString(Constants.NICK,""));
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.index:
                //Main不能在这里被结束...
                MainActivity.smToggle();
                break;
            case R.id.devices:
                startActivity(new Intent(getActivity(), BleScanConnectActivity.class));
                break;
            case R.id.myinfo:
                startActivity(new Intent(getActivity(),MyinfoActivity.class));
                break;
            case R.id.securitycenter:
                startActivity(new Intent(getActivity(), SecurityActivity.class));
                break;
            case R.id.setting:
                startActivity(new Intent(getActivity(), SettingActivity.class));
                break;
            case R.id.aboutapp:
                Intent intent = new Intent(getActivity(), WebviewActivity.class);
                intent.putExtra(Constants.URL,Constants.About_Site);
                intent.putExtra("type","about");
                startActivity(intent);
                break;
            case R.id.backfeed:
                startActivity(new Intent(getActivity(), FeedbackActivity.class));
                break;
            case R.id.help:
                startActivity(new Intent(getActivity(), HelpActivity.class));
                break;
            case R.id.username:
                if (!sp.getBoolean(Constants.IS_LOGIN,false)){
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                }
                break;
            case R.id.usericon:
                if (!sp.getBoolean(Constants.IS_LOGIN,false)){
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                }
                break;
        }
    }
}
