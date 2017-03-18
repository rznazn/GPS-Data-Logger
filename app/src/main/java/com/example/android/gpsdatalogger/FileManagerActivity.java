package com.example.android.gpsdatalogger;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.File;

/**
 * Created by sport on 3/17/2017.
 */

public class FileManagerActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback,
            DirectoryAdapter.directoryAdapterOnClickHandler{

    /**
     * create global variables for views
     */
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private DirectoryAdapter mAdapter;
    private static final int FINE_LOCATION_PERMISSION = 0;
    public static final String directoryName = "/GPSLog";
    private File[] mFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_file_manager);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_file_directory);

        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new DirectoryAdapter(this);
        mRecyclerView.setAdapter(mAdapter);


        /**
         * check for location permission, request if not current
         * call to run location services if it is
         */
        requestPermissions();

        updateRecyclerView();
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
     * Inflate option menu for creating new file
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.directory_menu, menu);
        return true;

    }

    /**
     * create new file in directory
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_create_file:
                AlertDialog.Builder builder = new AlertDialog.Builder(FileManagerActivity.this);
                LayoutInflater layoutInflater = getLayoutInflater();
                final View inputView = layoutInflater.inflate(R.layout.alert_dialog_layout, null);
                builder.setView(inputView);
                builder.setMessage("enter new file name");
                builder.setPositiveButton("create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText fileNameET = (EditText) inputView.findViewById(R.id.et_new_file);
                        String fileNameString = fileNameET.getText().toString() + ".txt";
                        StorageManager.createFileInExternalStorage(FileManagerActivity.this, directoryName, fileNameString);
                        updateRecyclerView();
                    }
                });
                AlertDialog ad = builder.create();
                ad.show();
        }
        return true;
    }

    /**
     * get list of files in directory and update recyclerview
     */
    private void updateRecyclerView(){
        mFiles = StorageManager.getFilesInDirectory(this, directoryName);
        mAdapter.setmFiles(mFiles);

    }

    /**
     * interface with the on click method in the recycler view adapter
     * @param file is passed from the viewholder in the adapter
     */
    @Override
    public void onClick(String file) {

        Intent intentToStartMainActivity = new Intent(this, LoggingActivity.class);
        intentToStartMainActivity.putExtra(Intent.EXTRA_TEXT, file);
        startActivity(intentToStartMainActivity);
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
                   updateRecyclerView();

                }
            }

        }
    }

}
