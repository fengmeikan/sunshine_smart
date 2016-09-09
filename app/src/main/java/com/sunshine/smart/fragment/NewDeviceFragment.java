package com.sunshine.smart.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sunshine.smart.R;
import com.sunshine.smart.activity.DeviceCateSelActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewDeviceFragment extends Fragment implements View.OnClickListener {


    public NewDeviceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_new_device, container, false);
        ImageView imageView6 = (ImageView) root.findViewById(R.id.imageView6);
        imageView6.setOnClickListener(this);
        return root;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imageView6:
                startActivity(new Intent(getActivity(), DeviceCateSelActivity.class));
                break;
        }
    }
}
