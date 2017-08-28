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

public class PlotActivity2 extends Activity implements SensorEventListener
{
    private static final int HISTORY_SIZE = 100;
    private SensorManager sensorMgr = null;
    private Sensor accSensor = null;

    private XYPlot accDataPlot = null;

    private SimpleXYSeries xAccSeries;
    private SimpleXYSeries xHistorySeries = null;
    private Redrawer redrawer;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plot);

        xAccSeries = new SimpleXYSeries("Light Intensity");

        // setup the accData plot:
        accDataPlot = findViewById(R.id.accDataPlot);

        xHistorySeries = new SimpleXYSeries("Light Intensity");
        xHistorySeries.useImplicitXVals();

        accDataPlot.setRangeBoundaries(0, 250, BoundaryMode.FIXED);
        accDataPlot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.FIXED);
        accDataPlot.addSeries(xHistorySeries,
                new LineAndPointFormatter(
                        Color.rgb(10, 10, 255), null, null, null));
        accDataPlot.setDomainStepMode(StepMode.INCREMENT_BY_VAL);
        accDataPlot.setDomainStepValue(HISTORY_SIZE/10);
        accDataPlot.setRangeStepMode(StepMode.INCREMENT_BY_VAL);
        accDataPlot.setRangeStepValue(10);
        accDataPlot.setDomainLabel("Sample Index");
        accDataPlot.getDomainTitle().pack();
        accDataPlot.setRangeLabel("Light Intensity (lux)");
        accDataPlot.getRangeTitle().pack();

        accDataPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).
                setFormat(new DecimalFormat("#"));

        accDataPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).
                setFormat(new DecimalFormat("#"));

        // register for acceleration sensor events:
        sensorMgr = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        for (Sensor sensor : sensorMgr.getSensorList(Sensor.TYPE_LIGHT)) {
            if (sensor.getType() == Sensor.TYPE_LIGHT) {
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

        // get rid the oldest sample in history:
        if (xHistorySeries.size() > HISTORY_SIZE) {
            xHistorySeries.removeFirst();
        }

        // add the latest history sample:
        xHistorySeries.addLast(null, sensorEvent.values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Not interested in this event
    }
}

