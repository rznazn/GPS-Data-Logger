package com.example.android.gpsdatalogger;

import android.Manifest;
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
import android.os.Environment;
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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity implements SensorEventListener, ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * View and string variable for basic UI function
     */
    private TextClock mTextClock;
    private TextView mAzimuthTV;
    private TextView mLocationTV;
    private TextView mLogDisplayTV;
    private TextView mLogButtonTV;
    private String GPSLog = "/" + "GPSLog";

    /**
     * Variables for compass and location use
     */
    private SensorManager mSensorManager;

    private LocationListener mLocationListener;
    private float[] gravity = new float[3];
    private float[] geomagnetic = new float[3];

    private LocationManager mLocationManager;

    private static final int FINE_LOCATION_PERMISSION = 0;

    private Sensor mSensorGravity;
    private Sensor mSensorMagnetic;
    private double bearing = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

/**
 * assign views to local variables
 */
        mTextClock = (TextClock) findViewById(R.id.tc_event_time);
        mAzimuthTV = (TextView) findViewById(R.id.tv_azimuth);
        mLocationTV = (TextView) findViewById(R.id.tv_location);
        mLogDisplayTV = (TextView) findViewById(R.id.tv_log_display);
        mLogButtonTV = (TextView) findViewById(R.id.tv_log_button);


        /**
         * set log button to collect the data from the displays and add them to the event log
         */
        mLogButtonTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                writeToExternalStorage();
                readFromExternalStorage();

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
         * check for location permission, request if not current
         * call to run location services if it is
         */
        requestPermissions();
        runLocationServices();

        /**
         * load data to display if a log already exists
         */
        readFromExternalStorage();

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
        }
        return true;
    }


    /**
     * request permission if not already granted
     */
    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    FINE_LOCATION_PERMISSION);
        }
    }

    /**
     * on receiving location permissions, begin run location services
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case FINE_LOCATION_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

                    runLocationServices();

                }
            }

        }
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
     * write current readings to file
     */
    public void writeToExternalStorage(){
        String storageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(storageState)){
            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            File dir = new File(root.getAbsolutePath() + GPSLog);
            if (!dir.exists()){
                dir.mkdirs();
            }
            File log = new File(dir, GPSLog + ".txt");

            String eventTime = mTextClock.getText().toString();
            String eventAzimuth = mAzimuthTV.getText().toString();
            String eventLocation = mLocationTV.getText().toString();
            String eventToLog = "";
            eventToLog = eventToLog.concat(eventTime + "\n" + eventAzimuth +
                    "\n" + eventLocation + "\n\n");
            try {
                FileOutputStream fos = new FileOutputStream(log, true);
                PrintWriter pw = new PrintWriter(fos);
                pw.write(eventToLog);
                pw.flush();
                pw.close();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else {
            Toast.makeText(this, "External Storage no available", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * read data file from file and show in display
     */
    public void readFromExternalStorage(){
        mLogDisplayTV.setText("");
        File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File dir = new File(root.getAbsolutePath() + GPSLog);
        File log = new File(dir, GPSLog + ".txt");

        try {
            FileInputStream fis = new FileInputStream(log);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String  logForDisplay;
            StringBuffer sb = new StringBuffer();

            while ((logForDisplay = br.readLine()) != null){
                sb.append(logForDisplay + "\n" );
            }

            mLogDisplayTV.setText(sb.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
