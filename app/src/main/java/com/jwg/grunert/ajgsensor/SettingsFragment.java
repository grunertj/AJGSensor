package com.jwg.grunert.ajgsensor;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SettingsFragment extends Fragment {
    boolean dim_state = false;
    CheckBox checkBox;
    ArrayList<CheckBox> checkBoxes;
    SensorManager sensorManager = null;
    RadioGroup radioGroup;


    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        LinearLayout linearLayout = (LinearLayout)view.findViewById(R.id.linearLayout);
        radioGroup = (RadioGroup)view.findViewById(R.id.radioGroup);

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    dim_state = MainActivity.dim_screen(dim_state, getActivity());
                    MainActivity.dim_screen(false, getActivity());

                }
                return false;
            }
        });


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
             switch (checkedId) {
                    case R.id.radioButtonOverride:
                        MainActivity.file_mode = MainActivity.OVERRIDE;
                        break;
                    case R.id.radioButtonAppend:
                        MainActivity.file_mode = MainActivity.APPEND;
                        break;
                    case R.id.radioButtonNew:
                        MainActivity.file_mode = MainActivity.NEW;
                        break;
                    default:
                        MainActivity.file_mode = MainActivity.NEW;
                        break;
                }
            }
        });

        sensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> list = sensorManager.getSensorList(Sensor.TYPE_ALL);
        ArrayList<Sensor> sensors = new ArrayList<Sensor>();
        sensors.addAll(list);

        Collections.sort(sensors, new Comparator<Sensor>() {
            @Override
            public int compare(Sensor lhs, Sensor rhs) {
                return lhs.getType() - rhs.getType();
            }
        });


        checkBoxes = new ArrayList<CheckBox>();

        checkBox = new CheckBox(view.getContext());
        checkBox.setText("GPS Location");
        checkBox.setChecked(false);
        checkBox.setId(MainActivity.TYPE_GPS);
        checkBoxes.add(checkBox);
        linearLayout.addView(checkBox);

        for (Sensor sensor: sensors) {
            switch (sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    checkBox = new CheckBox(view.getContext());
                    checkBox.setText(sensor.getType() + " " + sensor.getName() + " " + sensor.getVendor());
                    checkBox.setChecked(true);
                    checkBox.setId(sensor.getType());
                    checkBoxes.add(checkBox);
                    linearLayout.addView(checkBox);
                    break;
                case Sensor.TYPE_STEP_DETECTOR:
                case Sensor.TYPE_STEP_COUNTER:
                case Sensor.TYPE_GYROSCOPE:
                case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                case Sensor.TYPE_ROTATION_VECTOR:
                    checkBox = new CheckBox(view.getContext());
                    checkBox.setText(sensor.getType() + " " + sensor.getName() + " " + sensor.getVendor());
                    checkBox.setChecked(false);
                    checkBox.setId(sensor.getType());
                    checkBoxes.add(checkBox);
                    linearLayout.addView(checkBox);
                    break;
                default:
                    break;
            }
        }

        sensorManager = null;

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        for (CheckBox checkBox: checkBoxes) {
            MainActivity.log[checkBox.getId()] = checkBox.isChecked();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && isResumed()) {

        } else if (isResumed()) {
            for (CheckBox checkBox: checkBoxes) {
                MainActivity.log[checkBox.getId()] = checkBox.isChecked();
            }
        }
    }
}