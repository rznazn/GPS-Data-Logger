package com.example.android.gpsdatalogger;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by sport on 3/17/2017.
 */

public class DirectoryAdapter extends RecyclerView.Adapter<DirectoryAdapter.directoryAdapterViewHolder> {



    @Override
    public directoryAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(directoryAdapterViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class directoryAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public directoryAdapterViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
