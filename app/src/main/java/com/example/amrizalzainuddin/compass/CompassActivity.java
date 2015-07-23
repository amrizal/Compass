package com.example.amrizalzainuddin.compass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.WindowManager;


public class CompassActivity extends ActionBarActivity {

    private float[] aValues = new float[3];
    private float[] mValues = new float[3];
    private CompassView compassView;
    private SensorManager sensorManager;
    private int rotation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        compassView = (CompassView)findViewById(R.id.compassView);
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        String windoSrvc = Context.WINDOW_SERVICE;
        WindowManager wm = (WindowManager)getSystemService(windoSrvc);
        Display display = wm.getDefaultDisplay();
        rotation = display.getRotation();

        updateOrientation(new float[]{0,0,0});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_compass, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateOrientation(float[] values){
        if(compassView != null){
            compassView.setBearing(values[0]);
            compassView.setPitch(values[1]);
            compassView.setRoll(-values[2]);
            compassView.invalidate();
        }
    }

    private float[] calculateOrientation(){
        float[] values = new float[3];
        float[] inR = new float[9];
        float[] outR = new float[9];

        //determinte the rotation matrix
        SensorManager.getRotationMatrix(inR, null, aValues, mValues);

        //remap the coordinates based on the natural device orientation
        int x_axis = SensorManager.AXIS_X;
        int y_axis = SensorManager.AXIS_Y;

        switch (rotation){
            case (Surface.ROTATION_90):
                x_axis = SensorManager.AXIS_Y;
                y_axis = SensorManager.AXIS_MINUS_X;
                break;
            case (Surface.ROTATION_180):
                y_axis = SensorManager.AXIS_MINUS_Y;
                break;
            case (Surface.ROTATION_270):
                x_axis = SensorManager.AXIS_MINUS_Y;
                y_axis = SensorManager.AXIS_X;
                break;
            default:break;
        }

        SensorManager.remapCoordinateSystem(inR, x_axis, y_axis, outR);

        //obtain the current, corrected orientation
        SensorManager.getOrientation(outR, values);

        //convert from radians to degrees
        values[0] = (float)Math.toDegrees(values[0]);
        values[1] = (float)Math.toDegrees(values[1]);
        values[2] = (float)Math.toDegrees(values[2]);

        return values;
    }

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                aValues = event.values;
            if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                mValues = event.values;

            updateOrientation(calculateOrientation());
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(sensorEventListener, magField, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(sensorEventListener);
        super.onPause();
    }
}
