package com.kota.akshay.environmentsensors;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnClickListener;


    public class SensorActivity extends Activity implements SensorEventListener {
        private SensorManager mSensorManager;
        private Sensor mLight;
        private Sensor mAccelerometer;
        private Sensor mGyro;
        private Sensor mMag;

        TextView lightVal;
        TextView accMag;
        TextView gyroX;
        TextView magX;
        TextView warning;

        Button plotAcc;
        Button plotOrient;
        Button plotLight;

        @Override
        public final void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            // Get an instance of the sensor service, and use that to get an instance of
            // a particular sensor.
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            mMag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            if ((mGyro == null) || (mAccelerometer == null) || (mLight == null) || (mMag == null))
            {
                warning = (TextView) findViewById(R.id.warning);
                warning.setText("One or many of the sensors not available !!");
            }

            mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_NORMAL);

            lightVal = (TextView) findViewById(R.id.textView2);
            accMag = (TextView) findViewById(R.id.textView19);
            gyroX = (TextView) findViewById(R.id.textView20);
            magX = (TextView) findViewById(R.id.textView21);

            plotAcc = (Button) findViewById(R.id.button);
            plotOrient = (Button) findViewById(R.id.button2);
            plotLight = (Button) findViewById(R.id.button3);

            plotAcc.setOnClickListener(new OnClickListener(){
                public void onClick(View v){
                    Intent intent = new Intent(SensorActivity.this, PlotActivity.class);
                    startActivity(intent);
                }
            });

            plotOrient.setOnClickListener(new OnClickListener(){
                public void onClick(View v){
                    Intent intent = new Intent(SensorActivity.this, PlotActivity1.class);
                    startActivity(intent);
                }
            });

            plotLight.setOnClickListener(new OnClickListener(){
                public void onClick(View v){
                    Intent intent = new Intent(SensorActivity.this, PlotActivity2.class);
                    startActivity(intent);
                }
            });
        }



        @Override
        public final void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Do something here if sensor accuracy changes.
        }

        @Override
        public final void onSensorChanged(SensorEvent event) {
            Sensor sensor = event.sensor;

            float lux = 0;
            float accelX = 0, accelY = 0, accelZ = 0;
            float gyrX = 0, gyrY = 0, gyrZ = 0;
            float magnetX = 0;

            if (sensor.getType() == Sensor.TYPE_LIGHT) {
                lux = event.values[0];
            }
            else if(sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                accelX = event.values[0];
                accelY = event.values[1];
                accelZ = event.values[2];
            }
            else if(sensor.getType() == Sensor.TYPE_GYROSCOPE){
                gyrX = event.values[0];
                gyrY = event.values[1];
                gyrZ = event.values[2];
            }
            else if(sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            {
                magnetX = event.values[0];
            }

            double magnitude = Math.sqrt(accelX*accelX+accelY*accelY+accelZ*accelZ);

            lightVal.setText(String.valueOf(lux) + "lux");
            accMag.setText(String.valueOf(magnitude));
            gyroX.setText(String.valueOf(gyrX));
            magX.setText(String.valueOf(magnetX));
        }

        @Override
        protected void onResume() {
            // Register a listener for the sensor.
            super.onResume();
            mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        protected void onPause() {
            // Be sure to unregister the sensor when the activity pauses.
            super.onPause();
            mSensorManager.unregisterListener(this);
        }
    }
