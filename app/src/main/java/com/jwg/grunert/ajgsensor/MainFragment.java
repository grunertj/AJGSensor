package com.jwg.grunert.ajgsensor;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.GZIPOutputStream;

public class MainFragment extends Fragment implements SensorEventListener, LocationListener {
    static final int LONG = 0;
    static final int SHORT = 1;
    static final long MIN_TIME_IN_MILLISECONDS = 1000;
    static final float MIN_DISTANCE_IN_METERS = 1;
    static boolean start_logging = false;

    static double LastLatitude, LastLongitude, TotalDistance, MaxSpeed, AverageSpeed, TotalSpeed;
    static int log_counter;
    float [] Distance = new float[10];

    static final String directory_name = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + "PaddleSensorBis";

    TextView textViewRunning, textViewFile, textViewGPSFile, textViewLocation,textViewLatitude,textViewLongitude;
    TextView textViewDistance, textViewAverageSpeed, textViewMaxSpeed;
    Button buttonStart, buttonStop, buttonDim;
    SimpleDateFormat gpx_simpleDateFormat;


    static BufferedWriter sensor_file = null;
    static BufferedWriter gps_file = null;
    static String sensor_file_name = null;
    static String gps_file_name = null;

    SensorManager sensorManager = null;
    LocationManager locationManager = null;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        textViewFile = (TextView) view.findViewById(R.id.textViewFile);
        textViewGPSFile = (TextView) view.findViewById(R.id.textViewGPSFile);
        textViewRunning = (TextView) view.findViewById(R.id.textViewRunning);
        textViewLocation = (TextView) view.findViewById(R.id.textViewLocation);
        textViewLatitude = (TextView) view.findViewById(R.id.textViewLatitude);
        textViewLongitude = (TextView) view.findViewById(R.id.textViewLongitude);
        textViewDistance = (TextView) view.findViewById(R.id.textViewDistance);
        textViewAverageSpeed = (TextView) view.findViewById(R.id.textViewAverageSpeed);
        textViewMaxSpeed = (TextView) view.findViewById(R.id.textViewMaxSpeed);
        buttonStart = (Button) view.findViewById(R.id.buttonStart);
        buttonStop = (Button) view.findViewById(R.id.buttonStop);
        buttonDim = (Button) view.findViewById(R.id.buttonDim);

        File directory = new File(directory_name);

        gpx_simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        gpx_simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

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
                    MainActivity.dim_state = MainActivity.dim_screen(false, getActivity());
                }
                return false;
            }
        });

        buttonDim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.dim_state = MainActivity.dim_screen(MainActivity.dim_state, getActivity());
            }
        });

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String timestamp = getCurrentTimeStamp(SHORT);
                sensor_logging(true, timestamp);
                sensor(true);
                location_logging(true, timestamp);
                gpx_header();
                location(true);
                buttonStart.setEnabled(false);
                textViewGPSFile.setEnabled(false);
                textViewFile.setEnabled(false);

            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensor(false);
                sensor_logging(false, null);
                location(false);
                gpx_footer();
                location_logging(false, null);
                buttonStart.setEnabled(true);
                textViewGPSFile.setEnabled(true);
                textViewFile.setEnabled(true);
                Toast.makeText(getActivity().getApplicationContext(), "Click on file name to share.", Toast.LENGTH_SHORT).show();
            }
        });

        textViewFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = ((TextView) v);
                String string = textView.getText().toString();
                Uri uri = Uri.fromFile(new File(getAbsoluteFileName(string)));
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                sendIntent.setType("text/plain");
                // sendIntent.setType("application/zip");
                Toast.makeText(getActivity().getApplicationContext(), "Sharing file: " + string, Toast.LENGTH_SHORT).show();

                startActivity(sendIntent);
            }
        });

        textViewGPSFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.log[MainActivity.TYPE_GPS]) {
                    TextView textView = ((TextView) v);
                    String string = textView.getText().toString();
                    Uri uri = Uri.fromFile(new File(getAbsoluteFileName(string)));
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    sendIntent.setType("text/plain");
                    // sendIntent.setType("application/zip");
                    Toast.makeText(getActivity().getApplicationContext(), "Sharing file: " + string, Toast.LENGTH_SHORT).show();
                    startActivity(sendIntent);
                }
            }
        });

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        sensor(false);
        sensor_logging(false, null);
        location(false);
        location_logging(false, null);
        start_logging = false;
    }

    public String getCurrentTimeStamp(int which) {
        if (which == SHORT) {
            return new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
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

    void sensor (boolean on) {
        if (on == true) {
            if (sensorManager == null) {
                sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            }

            if (MainActivity.log[Sensor.TYPE_ACCELEROMETER]) {
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
            }
            if (MainActivity.log[Sensor.TYPE_LINEAR_ACCELERATION]) {
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);
            }
            if (MainActivity.log[Sensor.TYPE_STEP_DETECTOR]) {
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR), SensorManager.SENSOR_DELAY_NORMAL);
            }
            if (MainActivity.log[Sensor.TYPE_STEP_COUNTER]) {
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER), SensorManager.SENSOR_DELAY_NORMAL);
            }
            if (MainActivity.log[Sensor.TYPE_GYROSCOPE]) {
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
            }
            if (MainActivity.log[Sensor.TYPE_GYROSCOPE_UNCALIBRATED]) {
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED), SensorManager.SENSOR_DELAY_NORMAL);
            }
            if (MainActivity.log[Sensor.TYPE_ROTATION_VECTOR]) {
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
            }
        } else {
            if (sensorManager != null) {
                sensorManager.unregisterListener(this);
            }
        }
    }

    void location (boolean on) {
        if (on && MainActivity.log[MainActivity.TYPE_GPS]) {
            if (locationManager == null) {
                locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
                Toast.makeText(getActivity().getApplicationContext(), "Please enable Location.", Toast.LENGTH_LONG).show();
            }
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_IN_MILLISECONDS, MIN_DISTANCE_IN_METERS, this);
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, MIN_TIME_IN_MILLISECONDS, MIN_DISTANCE_IN_METERS, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_IN_MILLISECONDS, MIN_DISTANCE_IN_METERS, this);
        } else {
            if (locationManager != null) {
                locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                locationManager.removeUpdates(this);
                locationManager = null;
            }
        }
    }

    /*
    void sensor_logging (boolean on, String timestamp) {
        FileWriter file;
        if (on == true) {
            if (sensor_file_name == null || MainActivity.file_mode == MainActivity.NEW) {
                sensor_file_name = String.format("%s-%s.txt","sensor", timestamp);
            }
            try {
                file = new FileWriter(getAbsoluteFileName(sensor_file_name));
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
    */

    void sensor_logging (boolean on, String timestamp) {
        if (on == true) {
            if (sensor_file_name == null || MainActivity.file_mode == MainActivity.NEW) {
                if (MainActivity.COMPRESS) {
                    sensor_file_name = String.format("%s-%s.txt.gz", "sensor", timestamp);
                } else {
                    sensor_file_name = String.format("%s-%s.txt", "sensor", timestamp);
                }
            }
            try {
                if (MainActivity.COMPRESS) {
                    sensor_file = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(getAbsoluteFileName(sensor_file_name)))));
                } else {
                    sensor_file = new BufferedWriter(new FileWriter(getAbsoluteFileName(sensor_file_name)));
                }
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

    void location_logging (boolean on, String timestamp) {
        FileWriter file;
        if (on && MainActivity.log[MainActivity.TYPE_GPS]) {
            if (gps_file_name == null || MainActivity.file_mode == MainActivity.NEW) {
                gps_file_name = String.format("%s-%s.gpx","location", timestamp);
            }
            try {
                file = new FileWriter(getAbsoluteFileName(gps_file_name));
                gps_file = new BufferedWriter(file);
            } catch (IOException e) {
                Toast.makeText(getActivity().getApplicationContext(), "Could not open file: "+ "" + e + " " + getAbsoluteFileName(gps_file_name), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            textViewGPSFile.setText(gps_file_name);
        } else {
            if (gps_file != null) {
                try {
                    gps_file.flush();
                    gps_file.close();
                    gps_file = null;
                } catch (IOException e) {
                    Toast.makeText(getActivity().getApplicationContext(), "Could not close file: "+ "" + e + " " + getAbsoluteFileName(gps_file_name), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        float value[];
        long timestamp;
        String event_type;

        event_type=null;
        timestamp = event.timestamp;
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                value = event.values.clone();
                event_type = String.format(Locale.US,"%d %d %.8f %.8f %.8f\n", timestamp, event.sensor.getType(), value[0], value[1], value[2]);
                textViewRunning.setText("Sensor.TYPE_LINEAR_ACCELERATION");
                break;
            case Sensor.TYPE_ACCELEROMETER:
                value = event.values.clone();
                event_type = String.format(Locale.US,"%d %d %.8f %.8f %.8f\n", timestamp, event.sensor.getType(), value[0], value[1], value[2]);
                textViewRunning.setText("Sensor.TYPE_ACCELEROMETER");
                break;
            case Sensor.TYPE_STEP_DETECTOR:
                value = event.values.clone();
                event_type = String.format(Locale.US,"%d %d %.8f %.8f %.8f\n", timestamp, event.sensor.getType(), value[0], value[0], value[0]);
                textViewRunning.setText("Sensor.TYPE_STEP_DETECTOR");
                break;
            case Sensor.TYPE_STEP_COUNTER:
                value = event.values.clone();
                event_type = String.format(Locale.US,"%d %d %.8f %.8f %.8f\n", timestamp, event.sensor.getType(), value[0], value[0], value[0]);
                textViewRunning.setText("Sensor.TYPE_STEP_COUNTER");
                break;
            case Sensor.TYPE_GYROSCOPE:
                value = event.values.clone();
                event_type = String.format(Locale.US,"%d %d %.8f %.8f %.8f\n", timestamp, event.sensor.getType(), value[0], value[1], value[2]);
                textViewRunning.setText("Sensor.TYPE_GYROSCOPE");
                break;
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                value = event.values.clone();
                event_type = String.format(Locale.US,"%d %d %.8f %.8f %.8f\n", timestamp, event.sensor.getType(), value[0], value[1], value[2]);
                textViewRunning.setText("Sensor.TYPE_GYROSCOPE_UNCALIBRATED");
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                value = event.values.clone();
                event_type = String.format(Locale.US,"%d %d %.8f %.8f %.8f\n", timestamp, event.sensor.getType(), value[0], value[1], value[2]);
                textViewRunning.setText("Sensor.TYPE_ROTATION_VECTOR");
                break;
        }
        try {
            if (event_type!=null) {
                if (start_logging && MainActivity.log[MainActivity.TYPE_GPS]
                        || MainActivity.log[MainActivity.TYPE_GPS] == false
                        || MainActivity.DELAYED == false) {
                    sensor_file.append(event_type);
                }
            }
        } catch (IOException e) {
            Toast.makeText(getActivity().getApplicationContext(), "Could append to file: "+ "" + e + " " + getAbsoluteFileName(sensor_file_name), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void gpx_header() {
        if (gps_file != null) {
            try {
                gps_file.append("<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>\n");
                gps_file.append("<gpx version=\"1.1\" creator=\"OsmAnd+\" " +
                        "xmlns=\"http://www.topografix.com/GPX/1/1\" " +
                        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                        "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 " +
                        "http://www.topografix.com/GPX/1/1/gpx.xsd\">\n");
                gps_file.append("  <trk>\n");
                gps_file.append("    <trkseg>\n");
            } catch (IOException e) {
                Toast.makeText(getActivity().getApplicationContext(), "Could append to file: " + "" + e + " " + getAbsoluteFileName(gps_file_name), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    public void gpx_footer() {
        if (gps_file != null) {
            try {
                gps_file.append("    </trkseg>\n");
                gps_file.append("  </trk>\n");
                gps_file.append("</gpx>\n");
            } catch (IOException e) {
                Toast.makeText(getActivity().getApplicationContext(), "Could append to file: " + "" + e + " " + getAbsoluteFileName(gps_file_name), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    // http://stackoverflow.com/questions/6993322/how-do-i-get-the-hdop-or-vdop-values-from-the-gps-locationmanager
    @Override
    public void onLocationChanged(Location location) {
        String time_string,trkpt_start,trkpt_end,speed,hdop;
        long timestamp;
        boolean valid_data = false;

        // time from from last location fix :
        // timestamp = location.getTime();

        // current time:
        timestamp = System.currentTimeMillis();

        gpx_simpleDateFormat.format(timestamp);

        textViewLatitude.setText(String.format(Locale.US, "%.8f", location.getLatitude()));
        textViewLongitude.setText(String.format(Locale.US, "%.8f", location.getLongitude()));
        textViewLocation.setText(location.getProvider());

        trkpt_start = String.format(Locale.US,"     <trkpt lat=\"%.6f\" lon=\"%.6f\">\n", location.getLatitude(), location.getLongitude());
        time_string = String.format(Locale.US,"        <time>%s</time>\n",gpx_simpleDateFormat.format(System.currentTimeMillis()));
        trkpt_end = String.format("     </trkpt>\n");

        if (location.hasAccuracy()) {
            hdop = String.format(Locale.US,"       <hdop>%.1f</hdop>\n", location.getAccuracy()/5.0);
        } else {
            hdop = null;
        }

        if ((location.hasSpeed() && location.getAccuracy() < 20 && location.hasBearing() && location.hasAccuracy())
                || (location.hasSpeed() && location.getAccuracy() < 10 && location.hasAccuracy())) {

            if ((start_logging == false && (location.getSpeed() > 0.3) || MainActivity.DELAYED == false)) {
                start_logging = true;
                LastLatitude = location.getLatitude();
                LastLongitude = location.getLongitude();
                TotalDistance = 0.0f;
                MaxSpeed = 0.0f;
                AverageSpeed = 0.0f;
                TotalSpeed = 0.0f;
                log_counter = 1;
            }

            if (start_logging) {
                valid_data = true;
            }

            Location.distanceBetween(location.getLatitude(), location.getLongitude(), LastLatitude, LastLongitude, Distance);
            TotalDistance  = TotalDistance + Distance[0];
            LastLatitude = location.getLatitude();
            LastLongitude = location.getLongitude();

            if (MaxSpeed < location.getSpeed()) {
                MaxSpeed = location.getSpeed();
            }

            TotalSpeed = TotalSpeed + location.getSpeed();
            AverageSpeed = TotalSpeed / log_counter;
            log_counter++;

            textViewDistance.setText(String.format(Locale.US,"Distance: %.2f km",TotalDistance / 1000.0f));
            textViewAverageSpeed.setText(String.format(Locale.US,"Average Speed: %d km/h",(int)Math.round(AverageSpeed * 3.6)));
            textViewMaxSpeed.setText(String.format(Locale.US,"Max Speed: %d km/h",(int)Math.round(MaxSpeed * 3.6)));

            speed = String.format(Locale.US,"        <extensions>\n          <speed>%.12f</speed>\n        </extensions>\n", location.getSpeed());
        } else {
            valid_data = false;
            speed = null;
        }

        if (gps_file != null && valid_data) {
            try {
                gps_file.append(trkpt_start);
                gps_file.append(time_string);
                if (hdop != null) {
                    gps_file.append(hdop);
                }
                if (speed != null) {
                    gps_file.append(speed);
                }
                gps_file.append(trkpt_end);
            } catch (IOException e) {
                Toast.makeText(getActivity().getApplicationContext(), "Could append to file: " + "" + e + " " + getAbsoluteFileName(gps_file_name), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (MainActivity.log[MainActivity.TYPE_GPS]) {
            if (isVisibleToUser && isResumed()) {
                location(true);
            } else if (isResumed()) {
                location(false);
            }
        } else {
            if (textViewGPSFile != null) {
                textViewGPSFile.setText("GPS file name (if GPS is enabled)");
            }
        }
    }
}
