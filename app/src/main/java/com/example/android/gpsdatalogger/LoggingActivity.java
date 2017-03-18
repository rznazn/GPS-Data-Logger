package com.example.android.gpsdatalogger;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextClock;
import android.widget.TextView;

import static com.example.android.gpsdatalogger.StorageManager.readFromExternalStorage;

public class LoggingActivity extends AppCompatActivity implements SensorEventListener{

    /**
     * View and string variable for basic UI function
     */
    private TextClock mTextClock;
    private TextView mAzimuthTV;
    private TextView mLocationTV;
    private TextView mLogDisplayTV;
    private TextView mLogButtonTV;
    private ScrollView mDisplaySV;
    /**
     * Strings for handling file name
     * TODO add extra activity to create multiple files 
     */
    private String directoryName = "/GPSLog";
    private String fileName = "";

    /**
     * Variables for compass and location use
     */
    private SensorManager mSensorManager;

    private LocationListener mLocationListener;
    private float[] gravity = new float[3];
    private float[] geomagnetic = new float[3];

    private LocationManager mLocationManager;


    private Sensor mSensorGravity;
    private Sensor mSensorMagnetic;
    private double bearing = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_logging);
        /**
         * AlertDialog to give use guidance with compass
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(LoggingActivity.this);
        LayoutInflater layoutInflater = getLayoutInflater();
//        final View inputView = layoutInflater.inflate(R.layout.alert_dialog_layout, null);
//        builder.setView(inputView);
        builder.setMessage("For best results with the compass: \n" +
                "1. Use parallel to the ground. \n" +
                "2. Use outside and away from metal objects. \n" +
                "3. Move horizontally in a figure 8 pattern to calibrate. \n" +
                "4. Hold steady for 10-20 secs, and allow azimuth to settle before logging. ");

        AlertDialog ad = builder.create();
        ad.show();
        /**
         * set filename to equal that from the view clicked to open this activity
         */
        Intent intent = getIntent();
        fileName = intent.getStringExtra(Intent.EXTRA_TEXT);

/**
 * assign views to local variables
 */
        mTextClock = (TextClock) findViewById(R.id.tc_event_time);
        mAzimuthTV = (TextView) findViewById(R.id.tv_azimuth);
        mLocationTV = (TextView) findViewById(R.id.tv_location);
        mLogDisplayTV = (TextView) findViewById(R.id.tv_log_display);
        mLogButtonTV = (TextView) findViewById(R.id.tv_log_button);
        mDisplaySV = (ScrollView) findViewById(R.id.sv_display);


        /**
         * set log button to collect the data from the displays and add them to the event log
         */
        mLogButtonTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String eventTime = mTextClock.getText().toString();
            String eventAzimuth = mAzimuthTV.getText().toString();
            String eventLocation = mLocationTV.getText().toString();
            String eventToLog = "";
            eventToLog = eventToLog.concat(eventTime + "\n" + eventAzimuth +
                    "\n" + eventLocation + "\n\n");
                StorageManager.writeToExternalStorage(LoggingActivity.this, directoryName, fileName, eventToLog, true);
                String currentLog = StorageManager.readFromExternalStorage(LoggingActivity.this, directoryName, fileName);
                updateDisplayLog(currentLog);

            }
        });

        /**
         * set the sensors and sensor manager
         */
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        mSensorGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mSensorManager.registerListener(this, mSensorMagnetic,
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorGravity,
                SensorManager.SENSOR_DELAY_GAME);

        /**
         * call to run location services if it is
         */
        runLocationServices();

        /**
         * load data to display if a log already exists
         */
        String currentLog = readFromExternalStorage(LoggingActivity.this, directoryName, fileName);
        updateDisplayLog(currentLog);

    }

    /**
     * inflate the menu in main activity
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.logger_menu, menu);
        return true;
    }

    /**
     * Pull log from the display and share to other app for sharing
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:

                /**
                 * share the text of the event log to send via another app
                 */
                ShareCompat.IntentBuilder.from(this)
                        .setChooserTitle("share weather")
                        .setType("text/plain")
                        .setText(mLogDisplayTV.getText())
                        .startChooser();
                break;
            case R.id.home:
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

        }
        return true;
    }



    /**
     * get data from the sensor to get compass azimuth
     *
     * @param event
     */
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

            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                // mGeomagnetic = event.values;

                geomagnetic[0] = alpha * geomagnetic[0] + (1 - alpha)
                        * event.values[0];
                geomagnetic[1] = alpha * geomagnetic[1] + (1 - alpha)
                        * event.values[1];
                geomagnetic[2] = alpha * geomagnetic[2] + (1 - alpha)
                        * event.values[2];

            }

            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, gravity,
                    geomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                bearing = (float) Math.toDegrees(orientation[0]);
                bearing = (bearing + 360) % 360;
                bearing = Math.round(bearing);
            }
        }
        mAzimuthTV.setText(String.valueOf(bearing));

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * this method contains all the steps to get latt and long for location
     */
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
                        "\nlong: " + longString;
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
    /**
     * method to update the log display
     */
    private void updateDisplayLog(String currentLog){
        mLogDisplayTV.setText(currentLog);
        mDisplaySV.fullScroll(View.FOCUS_DOWN);
    }

}
