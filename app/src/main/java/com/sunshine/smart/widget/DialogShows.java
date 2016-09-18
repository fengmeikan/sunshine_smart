package com.sunshine.smart.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sunshine.smart.R;
import com.sunshine.smart.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fengm on 2016/9/7.
 */
public class DialogShows extends Dialog implements View.OnClickListener, PickerView.onSelectListener {
    public DialogShows(Context context) {
        super(context);
    }

    public DialogShows(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected DialogShows(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    private static DialogShows ds;

    public static DialogShows getInstance(Context context){
        if (ds!=null && ds.isShowing()){
            ds.dismiss();
        }
        ds = new DialogShows(context);
        return ds;
    }

    private String unit;

    private List<String> list;

    public DialogShows showPackSelect(Context context,List<String> list,String unit, String defselect){
        this.unit = unit;
        this.list = new ArrayList<String>();
        this.list.addAll(list);
        View view = LayoutInflater.from(context).inflate(R.layout.packview_layout,null,false);
        PickerView pick1 = (PickerView) view.findViewById(R.id.pick_1);
        PickerView pickunit = (PickerView) view.findViewById(R.id.pick_2);
        LinearLayout pick_layout = (LinearLayout) view.findViewById(R.id.pick_layout);
        pick1.setData(this.list);
        if(!TextUtils.isEmpty(unit)){
            List<String> listunit = new ArrayList<String>();
            listunit.add(unit);
            pickunit.setData(listunit);
        }else{
            pickunit.setVisibility(View.GONE);
        }
        pick1.setOnSelectListener(this);
        selected = this.list.get(0);
        pick1.setSelected(defselect);
        WindowManager.LayoutParams wmlp = new WindowManager.LayoutParams();
        wmlp.height = ScreenUtils.getScreenHeight(context)/4;
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        llp.leftMargin = ScreenUtils.getScreenWidth(context)/4;
        llp.rightMargin = ScreenUtils.getScreenWidth(context)/4;
        llp.weight = 1;
        pick_layout.setLayoutParams(llp);
        view.findViewById(R.id.cancel).setOnClickListener(this);
        view.findViewById(R.id.submit).setOnClickListener(this);
        ds.setContentView(view,wmlp);
        ds.show();
        return ds;
    }

    private EditText edittext;

    public DialogShows showEditDialog(Context context, String defselect){
        View view = LayoutInflater.from(context).inflate(R.layout.edittext_layout,null,false);
        LinearLayout pick_layout = (LinearLayout) view.findViewById(R.id.pick_layout);
        edittext = (EditText) view.findViewById(R.id.edittext_dialog);
        edittext.setHint(defselect);
        WindowManager.LayoutParams wmlp = new WindowManager.LayoutParams();
        wmlp.height = ScreenUtils.getScreenHeight(context)/6;
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        llp.leftMargin = ScreenUtils.getScreenWidth(context)/6;
        llp.rightMargin = ScreenUtils.getScreenWidth(context)/6;
        llp.weight = 1;
        pick_layout.setLayoutParams(llp);
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ds.cancel();
            }
        });
        view.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dseil.OnEditInput(ds,edittext.getText().toString().trim());
                ds.dismiss();
            }
        });
        ds.setContentView(view,wmlp);
        ds.show();
        return ds;
    }

    private TextView textview;

    public DialogShows showTextDialog(Context context, String display, String title){
        View view = LayoutInflater.from(context).inflate(R.layout.textview_layout,null,false);
        LinearLayout pick_layout = (LinearLayout) view.findViewById(R.id.pick_layout);
        textview = (TextView) view.findViewById(R.id.textview_dialog);
        textview.setText(display);
        WindowManager.LayoutParams wmlp = new WindowManager.LayoutParams();
        wmlp.height = ScreenUtils.getScreenHeight(context)/6;
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        llp.leftMargin = ScreenUtils.getScreenWidth(context)/6;
        llp.rightMargin = ScreenUtils.getScreenWidth(context)/6;
        llp.weight = 1;
        pick_layout.setLayoutParams(llp);
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ds.cancel();
            }
        });
        view.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dseil.OnEditInput(ds,"");
                ds.dismiss();
            }
        });
        ds.setContentView(view, wmlp);
        ds.setTitle(title);
        ds.show();
        return ds;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cancel:
                this.cancel();
                break;
            case R.id.submit:
                dsokl.ClickOK(ds,selected,unit);
                this.dismiss();
                break;
        }
    }

    private String selected;
    private ClickOKLintener dsokl;
    private EditInputLintener dseil;

    public void setDialogShowsOKLintener(ClickOKLintener dsokl){
        this.dsokl = dsokl;
    }

    public void setEditInputOKLintener(EditInputLintener dseil){
        this.dseil = dseil;
    }

    @Override
    public void onSelect(String text) {
        this.selected = text;
    }


    /**
     * Created by fff on 2016/9/7.
     */
    public interface ClickOKLintener {
        /**
         * ok点击回调
         * @param dialog    对话框对象
         * @param str       对话框类型
         */
        void ClickOK(DialogShows dialog,String selected,String str);
    }

    public interface EditInputLintener{
        void OnEditInput(DialogShows dialog,String input);
    }


}
