package com.example.android.gpsdatalogger;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
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
    private TextView mLatitudeTV;
    private TextView mLongitudeTV;
    private TextView mLogDisplayTV;
    private TextView mLogButtonTV;
    private ScrollView mDisplaySV;
    /**
     * Strings for handling file name
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
    private float[] rotation = new float[9];
    private float[] orientation = new float[3];
    private long lastUpdateToAzimuthTv = 0;

    private LocationManager mLocationManager;
    private GeomagneticField mGeoMagField;


    private Sensor mSensorGravity;
    private Sensor mSensorMagnetic;
    private double bearing = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        /**
         * disable screen TimeOut
         */
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_logging);
        /**
         * AlertDialog to give use guidance with compass
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(LoggingActivity.this);
        builder.setMessage(R.string.compassUseGuide);
        AlertDialog ad = builder.create();
        ad.show();
        /**
         * set filename to equal that from the view clicked to open this activity
         */
        Intent intent = getIntent();
        fileName = intent.getStringExtra(Intent.EXTRA_TEXT);
        getSupportActionBar().setTitle(fileName);

/**
 * assign views to local variables
 */
        mTextClock = (TextClock) findViewById(R.id.tc_event_time);
        mAzimuthTV = (TextView) findViewById(R.id.tv_azimuth_logact);
        mLatitudeTV = (TextView) findViewById(R.id.tv_lat_logact);
        mLongitudeTV = (TextView) findViewById(R.id.tv_long_logact) ;
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
                String eventLatitude = mLatitudeTV.getText().toString();
                String eventLongitude= mLongitudeTV.getText().toString();
                String eventToLog = "";
                eventToLog = eventToLog.concat(eventTime + "\n" + eventAzimuth +
                        "\n" + eventLatitude + "\n" + eventLongitude + "\n\n");
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
         * call to run location services
         */
        runLocationServices();

        /**
         * load data to display if a log already exists
         */
        String currentLog = readFromExternalStorage(LoggingActivity.this, directoryName, fileName);
        updateDisplayLog(currentLog);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
//          TODO delete this portion, only for evaluating battery life.
//        String eventTime = mTextClock.getText().toString();
//        String eventAzimuth = mAzimuthTV.getText().toString();
//        String eventLocation = mLocationTV.getText().toString();
//        String eventToLog = "";
//        eventToLog = eventToLog.concat("Activity Paused at: \n" + eventTime + "\n" + eventAzimuth +
//                "\n" + eventLocation + "\n\n");
//        StorageManager.writeToExternalStorage(LoggingActivity.this, directoryName, fileName, eventToLog, true);
//        String currentLog = StorageManager.readFromExternalStorage(LoggingActivity.this, directoryName, fileName);
//        updateDisplayLog(currentLog);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorMagnetic,
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorGravity,
                SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * inflate the menu in main activity
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logger_menu, menu);
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
                        .setChooserTitle("share log")
                        .setType("text/plain")
                        .setText(mLogDisplayTV.getText())
                        .startChooser();
                break;
            case R.id.action_enter_note:
                final String openNoteTime = mTextClock.getText().toString();
                final String azimuth = mAzimuthTV.getText().toString();
                final String latitude = mLatitudeTV.getText().toString();
                final String longitude = mLongitudeTV.getText().toString();

                final View adLayout = getLayoutInflater().inflate(R.layout.alert_dialog_layout,null);

                final TextView timeTV = (TextView) adLayout.findViewById(R.id.tv_time);
                timeTV.setText(openNoteTime);

                final TextView azimuthTV = (TextView) adLayout.findViewById(R.id.tv_azimuth);
                azimuthTV.setText(azimuth);

                final TextView latitudeTV = (TextView) adLayout.findViewById(R.id.tv_lat);
                latitudeTV.setText(latitude);

                final TextView longitudeTV = (TextView) adLayout.findViewById(R.id.tv_long);
                longitudeTV.setText(longitude);

                final EditText noteET = (EditText) adLayout.findViewById(R.id.et_new_file);

                AlertDialog.Builder builder = new AlertDialog.Builder(LoggingActivity.this);
                builder.setView(adLayout);
                builder.setMessage(R.string.enterNoteAlertDialogMessage);
                builder.setPositiveButton(R.string.enter_note, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String noteEntryString =  timeTV.getText() + "\n"
                                + azimuthTV.getText() + "\n"
                                + latitudeTV. getText() +"\n"
                                + longitudeTV.getText() +"\n"
                                +"<<<" + noteET.getText().toString() + ">>>\n\n" ;
                        StorageManager.writeToExternalStorage(LoggingActivity.this, directoryName,
                                fileName, noteEntryString, true);
                        updateDisplayLog(readFromExternalStorage(LoggingActivity.this, directoryName, fileName));
                    }
                });
                AlertDialog ad = builder.create();
                ad.show();
                break;
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

        boolean accelOrMagnetic = false;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values;
            accelOrMagnetic = true;

        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values;
            accelOrMagnetic = true;

        }

        SensorManager.getRotationMatrix(rotation, null, gravity, geomagnetic);
        SensorManager.getOrientation(rotation, orientation);
        bearing = orientation[0];
        bearing = Math.toDegrees(bearing);
        bearing = Math.round(bearing);

        /**
         * adjust for declination
         */
        if (mGeoMagField != null) {
            bearing += mGeoMagField.getDeclination();
        }

        /**
         * set bearing to be 0 - 360
         */
        if (bearing < 0) {
            bearing += 360;
        }

        if (System.currentTimeMillis() > lastUpdateToAzimuthTv + 250) {
            /**
             * set bearing to int value to remove unneeded  ".0"
             */
            int bearingAsInt = (int) bearing;
            lastUpdateToAzimuthTv = System.currentTimeMillis();
            mAzimuthTV.setText(String.valueOf(bearingAsInt));
        }
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
                long systemTime = System.currentTimeMillis();
                long gpsTime = location.getTime();
                long timeDifference = gpsTime - systemTime;
                int secondsDiff = (int) timeDifference/1000;
                double lattDouble = location.getLatitude();
                String lattString = Location.convert(lattDouble, Location.FORMAT_MINUTES);
                double longDouble = location.getLongitude();
                String longString = Location.convert(longDouble, Location.FORMAT_MINUTES);
                mLatitudeTV.setText(lattString);
                mLongitudeTV.setText(longString);

                mGeoMagField = new GeomagneticField(Double.valueOf(lattDouble).floatValue(),
                        Double.valueOf(longDouble).floatValue(),
                        Double.valueOf(location.getAltitude()).floatValue(),
                        System.currentTimeMillis());
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
