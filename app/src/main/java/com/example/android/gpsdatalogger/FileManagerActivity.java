package com.example.android.gpsdatalogger;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

/**
 * Created by sport on 3/17/2017.
 */

public class FileManagerActivity extends Activity {

    /**
     * create global variables for views
     */
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
