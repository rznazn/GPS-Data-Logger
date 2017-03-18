package com.example.android.gpsdatalogger;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by sport on 3/17/2017.
 */

public class FileManagerActivity extends AppCompatActivity {

    /**
     * create global variables for views
     */
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_file_manager);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_file_directory);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    }
}
