package com.example.android.gpsdatalogger;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextClock;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener, ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * View and string variable for basic UI function
     */
    private TextClock mTextClock;
    private TextView mAzimuthTV;
    private TextView mLocationTV;
    private TextView mLogDisplayTV;
    private TextView mLogButtonTV;
    private String mEventLog = "Event Log:\n";

    /**
     * Variables for compass and location episode
     */
    private SensorManager mSensorManager;

    private LocationListener mLocationListener;
    private float[] gravity = new float[3];
    // magnetic data
    private float[] geomagnetic = new float[3];
    // Rotation data
    private float[] rotation = new float[9];
    // orientation (azimuth, pitch, roll)
    private float[] orientation = new float[3];
    // smoothed values
    private float[] smoothed = new float[3];

    private LocationManager mLocationManager;

    private static final int FINE_LOCATION_PERMISSION = 0;

    private Sensor mSensorGravity;
    private Sensor mSensorMagnetic;
    private Location currentLocation;
    private GeomagneticField geomagneticField;
    private double bearing = 0;

    private String FILENAME = "filename";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mTextClock = (TextClock) findViewById(R.id.tc_event_time);
        mTextClock.setFormat24Hour("yyyyMMdd hh:mm:ss");
        mAzimuthTV = (TextView) findViewById(R.id.tv_azimuth);
        mLocationTV = (TextView) findViewById(R.id.tv_location);
        mLogDisplayTV = (TextView) findViewById(R.id.tv_log_display);
        mLogButtonTV = (TextView) findViewById(R.id.tv_log_button);

        mLogButtonTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String eventTime = mTextClock.getText().toString();
                String eventAzimuth = mAzimuthTV.getText().toString();
                String eventLocation = mLocationTV.getText().toString();
                mEventLog = mEventLog.concat(eventTime + "\n" + eventAzimuth +
                        "\n" + eventLocation + "\n\n");
                mLogDisplayTV.setText(mEventLog);

            }
        });

        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        mSensorGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mSensorManager.registerListener(this, mSensorMagnetic,
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorGravity,
                SensorManager.SENSOR_DELAY_GAME);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        }else {
            runLocationServices();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.logger_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_share:
                ShareCompat.IntentBuilder.from(this)
                        .setChooserTitle("share weather")
                        .setType("text/plain")
                        .setText(mEventLog)
                        .startChooser();
                break;
        }
        return true;
    }

    private void requestLocationPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    FINE_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case FINE_LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

                    runLocationServices();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        final float alpha = 0.97f;

        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                gravity[0] = alpha * gravity[0] + (1 - alpha)
                        * event.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha)
                        * event.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha)
                        * event.values[2];

                // mGravity = event.values;

                // Log.e(TAG, Float.toString(mGravity[0]));
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                // mGeomagnetic = event.values;

                geomagnetic[0] = alpha * geomagnetic[0] + (1 - alpha)
                        * event.values[0];
                geomagnetic[1] = alpha * geomagnetic[1] + (1 - alpha)
                        * event.values[1];
                geomagnetic[2] = alpha * geomagnetic[2] + (1 - alpha)
                        * event.values[2];
                // Log.e(TAG, Float.toString(event.values[0]));

            }

            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, gravity,
                    geomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                // Log.d(TAG, "azimuth (rad): " + azimuth);
                bearing = (float) Math.toDegrees(orientation[0]); // orientation
                bearing = (bearing + 360) % 360;
                bearing = Math.round(bearing);
                // Log.d(TAG, "azimuth (deg): " + azimuth);
            }
        }
        mAzimuthTV.setText(String.valueOf(bearing));

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void runLocationServices() {
        mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double lattDouble = location.getLatitude();
                String lattString = String.valueOf(lattDouble);
                double longDouble = location.getLongitude();
                String longString = String.valueOf(longDouble);
                String locationString = "latt: " + lattString +
                        "  long: " + longString;
                mLocationTV.setText(locationString);
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
        };


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500,
                1, mLocationListener);
    }
}
