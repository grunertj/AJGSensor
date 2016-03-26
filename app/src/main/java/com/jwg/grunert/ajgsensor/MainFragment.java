package com.jwg.grunert.ajgsensor;


import android.content.Context;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainFragment extends Fragment {
    static final int LONG = 0;
    static final int SHORT = 1;

    static final String directory_name = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "PaddleSensorBis";

    boolean dim_state = false;
    TextView textViewRunning, textViewFile;
    Button buttonStart, buttonStop;


    static BufferedWriter sensor_file = null;
    static BufferedWriter gps_file = null;
    static String sensor_file_name = null;
    static String gps_file_name = null;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        textViewFile = (TextView) view.findViewById(R.id.textViewFile);
        textViewRunning = (TextView) view.findViewById(R.id.textViewRunning);
        buttonStart = (Button) view.findViewById(R.id.buttonStart);

        File directory = new File(directory_name);

        if (directory.exists() == false) {
            boolean success = directory.mkdir();
            if (!success) {
                Toast.makeText(getActivity().getApplicationContext(), "Could not create directory: "+ directory_name, Toast.LENGTH_LONG).show();
            }
        }

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    dim_state = MainActivity.dim_screen(dim_state, getActivity());
                    MainActivity.dim_screen(false,getActivity());
                }
                return false;
            }
        });

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String timestamp = getCurrentTimeStamp(SHORT);
                sensor_logging(true, timestamp);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        textViewRunning.setText("" + MainActivity.log[Sensor.TYPE_ROTATION_VECTOR]);
    }

    @Override
    public void onPause() {
        super.onPause();
        textViewRunning.setText("" + MainActivity.log[Sensor.TYPE_ROTATION_VECTOR]);

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            textViewRunning.setText("" + MainActivity.log[Sensor.TYPE_ROTATION_VECTOR]);
        } else if (isResumed()) {

        }
    }

    @Override
    public void onStop() {
        super.onStop();
        sensor_logging(false,null);
    }

    public String getCurrentTimeStamp(int which) {
        if (which == SHORT) {
            return new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date());
        } else {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        }
    }

    public String getAbsoluteFileName(String filename) {
        File path = new File(directory_name);
        if (filename.matches(path.toString())) {
            return filename;
        } else {
            return (path.toString() + File.separator + filename);
        }
    }

    void sensor_logging (boolean on, String timestamp) {
        FileWriter file;
        if (on == true) {
            if (sensor_file_name == null || MainActivity.file_mode == MainActivity.NEW) {
                sensor_file_name = String.format("%s-%s.txt","sensor", timestamp);
            }
            try {
                if (MainActivity.file_mode == MainActivity.APPEND) {
                    file = new FileWriter(getAbsoluteFileName(sensor_file_name), true);
                } else {
                    file = new FileWriter(getAbsoluteFileName(sensor_file_name));
                }
                sensor_file = new BufferedWriter(file);
            } catch (IOException e) {
                Toast.makeText(getActivity().getApplicationContext(), "Could not open file: "+ "" + e + " " + getAbsoluteFileName(sensor_file_name), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            textViewFile.setText(sensor_file_name);
        } else {
            if (sensor_file != null) {
                try {
                    sensor_file.flush();
                    sensor_file.close();
                    sensor_file = null;
                } catch (IOException e) {
                    Toast.makeText(getActivity().getApplicationContext(), "Could not close file: "+ "" + e + " " + getAbsoluteFileName(sensor_file_name), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }
    }
}
