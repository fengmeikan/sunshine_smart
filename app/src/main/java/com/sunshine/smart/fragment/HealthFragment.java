package com.sunshine.smart.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunshine.smart.R;
import com.sunshine.smart.activity.BleScanConnectActivity;
import com.sunshine.smart.activity.MainActivity;
import com.sunshine.smart.widget.RotateCircle;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HealthFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HealthFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HealthFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = HealthFragment.class.getName();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private RotateCircle rotatecircle;
    private TextView steps;
    private Button runsport;
    //分别是 距离 热量  时长   体重  身高   年龄
    private TextView distance,heat,duration,weight,height,age;
    private SensorManager mSensorManager;

    public HealthFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HealthFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HealthFragment newInstance(String param1, String param2) {
        HealthFragment fragment = new HealthFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_health, container, false);


        int h = 480;
        mYOffset = h * 0.5f;
        mScale[0] = -(h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
        mScale[1] = -(h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));



        initView(rootView);

        return rootView;
    }

    private void initView(View rootView) {

        rotatecircle = (RotateCircle) rootView.findViewById(R.id.rotatecircle);
        steps = (TextView)rootView.findViewById(R.id.steps);
        runsport = (Button)rootView.findViewById(R.id.runsport);
        distance = (TextView) rootView.findViewById(R.id.distance);
        heat = (TextView) rootView.findViewById(R.id.heat);
        duration = (TextView) rootView.findViewById(R.id.duration);
        weight = (TextView) rootView.findViewById(R.id.weight);
        height = (TextView) rootView.findViewById(R.id.weight);
        age = (TextView) rootView.findViewById(R.id.age);
        rotatecircle.setOnClickListener(this);
        runsport.setOnClickListener(this);



    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //关闭
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.runsport:
                if (!rotatecircle.isT()){
                    Log.e("ttt","开始转");
                    rotatecircle.start(true);
                    runsport.setText("结束运动");
                    runStart();

                }else{
                    Log.e("ttt","停止转");
                    rotatecircle.start(false);
                    runsport.setText("开始运动");
                    resetRun();
                }
                break;
            case R.id.right_navi:
                startActivity(new Intent(getActivity(), BleScanConnectActivity.class));
                break;
        }
    }

    private void resetRun(){
        steps.setText("0");
        duration.setText("00:00:00");
        if (mSensorManager!=null){
            mSensorManager.unregisterListener(sensorEventListener);
        }
    }

    /**
     * 开始计步
     */
    private void runStart(){
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(sensorEventListener,mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_FASTEST);
    }

    public static int CURRENT_SETP = 0;
    public static float SENSITIVITY = 0;
    private float mLastValues[] = new float[3 * 2];
    private float mScale[] = new float[2];
    private float mYOffset;
    private static long end = 0;
    private static long start = 0;
    private float mLastDirections[] = new float[3 * 2];
    private float mLastExtremes[][] = { new float[3 * 2], new float[3 * 2] };
    private float mLastDiff[] = new float[3 * 2];
    private int mLastMatch = -1;

    private SensorEventListener sensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            Sensor sensor = sensorEvent.sensor;
            synchronized (this) {
                if (sensor.getType() == Sensor.TYPE_ORIENTATION) {
                } else {
                    int j = (sensor.getType() == Sensor.TYPE_ACCELEROMETER) ? 1 : 0;
                    if (j == 1) {
                        float vSum = 0;
                        for (int i = 0; i < 3; i++) {
                            final float v = mYOffset + sensorEvent.values[i] * mScale[j];
                            vSum += v;
                        }
                        int k = 0;
                        float v = vSum / 3;


                        float direction = (v > mLastValues[k] ? 1: (v < mLastValues[k] ? -1 : 0));
                        if (direction == -mLastDirections[k]) {
                            // Direction changed
                            int extType = (direction > 0 ? 0 : 1); // minumum or
                            // maximum?
                            mLastExtremes[extType][k] = mLastValues[k];
                            float diff = Math.abs(mLastExtremes[extType][k]- mLastExtremes[1 - extType][k]);

                            if (diff > SENSITIVITY) {
                                boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff[k] * 2 / 3);
                                boolean isPreviousLargeEnough = mLastDiff[k] > (diff / 3);
                                boolean isNotContra = (mLastMatch != 1 - extType);

                                if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra) {
                                    end = System.currentTimeMillis();
                                    if (end - start > 500) {
                                        Log.i("StepDetector", "CURRENT_SETP:"+ CURRENT_SETP);
                                        CURRENT_SETP++;
                                        mLastMatch = extType;
                                        start = end;
                                    }
                                } else {
                                    mLastMatch = -1;
                                }
                            }
                            mLastDiff[k] = diff;
                        }
                        mLastDirections[k] = direction;
                        mLastValues[k] = v;
                    }
                }
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
