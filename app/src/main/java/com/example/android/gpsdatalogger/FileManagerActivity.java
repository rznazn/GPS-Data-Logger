package com.example.android.gpsdatalogger;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by sport on 3/17/2017.
 */

public class FileManagerActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{

    /**
     * create global variables for views
     */
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private static final int FINE_LOCATION_PERMISSION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_file_manager);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_file_directory);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        /**
         * check for location permission, request if not current
         * call to run location services if it is
         */
        requestPermissions();
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

//    /**
//     * on receiving location permissions, begin run location services
//     *
//     * @param requestCode
//     * @param permissions
//     * @param grantResults
//     */
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case FINE_LOCATION_PERMISSION: {
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
//
//                    runLocationServices();
//
//                }
//            }
//
//        }
//    }

}
