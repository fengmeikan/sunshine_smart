package com.sunshine.smart.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sunshine.smart.R;

import java.util.HashMap;

/**
 * Created by huihu on 2016/7/8.
 */
public class FirstInstallActivity extends BaseActivity {

    private static String TAG = FirstInstallActivity.class.getName();
    private ViewPager viewPager;
    //用作轮播的图片们
    private static int[] images = {R.mipmap.welcome1,R.mipmap.weicome};
    private LinearLayout slideractivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.activity_slider);
        slideractivity = (LinearLayout) findViewById(R.id.slideractivity);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),this);
        viewPager.setAdapter(sectionsPagerAdapter);
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter{

        Context context;

        public SectionsPagerAdapter(FragmentManager fm, Context c) {
            super(fm);
            context = c;
        }

        public SectionsPagerAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return ImageFragment.instance(position,context);
        }

        @Override
        public int getCount() {
            return images.length;
        }
    }

    @SuppressLint("ValidFragment")
    public static class ImageFragment extends Fragment{
        private static Context context;

        public static ImageFragment instance(int postion,Context c){
            context = c;
            ImageFragment imgf = new ImageFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("img",images[postion]);
            bundle.putInt("postion",postion);
            imgf.setArguments(bundle);
            return imgf;


        }
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//            return super.onCreateView(inflater, container, savedInstanceState);
            View rootview =inflater.inflate(R.layout.layout_images_fragment,container,false);
            ImageView img = (ImageView) rootview.findViewById(R.id.image_viewpager);
            img.setImageResource(getArguments().getInt("img"));
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if (getArguments().getInt("postion") == images.length -1){
                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(context,LoginActivity.class));
                        getActivity().finish();
                    }
                });
            }

            return rootview;
        }
    }

}
