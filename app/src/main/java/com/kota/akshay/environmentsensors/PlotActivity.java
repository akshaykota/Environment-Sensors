package com.kota.akshay.environmentsensors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import com.androidplot.Plot;
import com.androidplot.util.Redrawer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;

import java.text.DecimalFormat;
import java.util.Arrays;

public class PlotActivity extends Activity implements SensorEventListener
{
    private static final int HISTORY_SIZE = 500;
    private SensorManager sensorMgr = null;
    private Sensor accSensor = null;

    private XYPlot accDataPlot = null;

    private SimpleXYSeries xAccSeries;
    private SimpleXYSeries yAccSeries;
    private SimpleXYSeries zAccSeries;
    private SimpleXYSeries xHistorySeries = null;
    private SimpleXYSeries yHistorySeries = null;
    private SimpleXYSeries zHistorySeries = null;

    private Redrawer redrawer;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plot);

        xAccSeries = new SimpleXYSeries("X");
        yAccSeries = new SimpleXYSeries("Y");
        zAccSeries = new SimpleXYSeries("Z");

        // setup the accData plot:
        accDataPlot = findViewById(R.id.accDataPlot);

        xHistorySeries = new SimpleXYSeries("X");
        xHistorySeries.useImplicitXVals();
        yHistorySeries = new SimpleXYSeries("Y");
        yHistorySeries.useImplicitXVals();
        zHistorySeries = new SimpleXYSeries("Z");
        zHistorySeries.useImplicitXVals();

        accDataPlot.setRangeBoundaries(-30, 30, BoundaryMode.FIXED);
        accDataPlot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.FIXED);
        accDataPlot.addSeries(xHistorySeries,
                new LineAndPointFormatter(
                        Color.rgb(10, 10, 255), null, null, null));
        accDataPlot.addSeries(yHistorySeries,
                new LineAndPointFormatter(
                        Color.rgb(10, 255, 10), null, null, null));
        accDataPlot.addSeries(zHistorySeries,
                new LineAndPointFormatter(
                        Color.rgb(255, 10, 10), null, null, null));
        accDataPlot.setDomainStepMode(StepMode.INCREMENT_BY_VAL);
        accDataPlot.setDomainStepValue(HISTORY_SIZE/10);
        accDataPlot.setRangeStepMode(StepMode.INCREMENT_BY_VAL);
        accDataPlot.setRangeStepValue(10);
        accDataPlot.setDomainLabel("Sample Index");
        accDataPlot.getDomainTitle().pack();
        accDataPlot.setRangeLabel("Acceleration (m/s2)");
        accDataPlot.getRangeTitle().pack();

        accDataPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).
                setFormat(new DecimalFormat("#"));

        accDataPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).
                setFormat(new DecimalFormat("#"));

        // register for acceleration sensor events:
        sensorMgr = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        for (Sensor sensor : sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER)) {
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accSensor = sensor;
            }
        }

        // if we can't access the acceleration sensor then exit:
        if (accSensor == null) {
            System.out.println("Failed to attach to Accelerometer.");
            cleanup();
        }

        sensorMgr.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_UI);

        redrawer = new Redrawer(
                Arrays.asList(new Plot[]{accDataPlot}),
                100, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        redrawer.start();
    }

    @Override
    public void onPause() {
        redrawer.pause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        redrawer.finish();
        super.onDestroy();
    }

    private void cleanup() {
        sensorMgr.unregisterListener(this);
        finish();
    }


    // Called whenever a new accSensor reading is taken.
    @Override
    public synchronized void onSensorChanged(SensorEvent sensorEvent) {

        // update level data:
        xAccSeries.setModel(Arrays.asList(
                new Number[]{sensorEvent.values[0]}),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);

        yAccSeries.setModel(Arrays.asList(
                new Number[]{sensorEvent.values[1]}),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);

        zAccSeries.setModel(Arrays.asList(
                new Number[]{sensorEvent.values[2]}),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);

        // get rid the oldest sample in history:
        if (zHistorySeries.size() > HISTORY_SIZE) {
            zHistorySeries.removeFirst();
            yHistorySeries.removeFirst();
            xHistorySeries.removeFirst();
        }

        // add the latest history sample:
        xHistorySeries.addLast(null, sensorEvent.values[0]);
        yHistorySeries.addLast(null, sensorEvent.values[1]);
        zHistorySeries.addLast(null, sensorEvent.values[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Not interested in this event
    }
}

