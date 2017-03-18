package com.example.android.gpsdatalogger;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;

/**
 * Created by sport on 3/17/2017.
 */

public class DirectoryAdapter extends RecyclerView.Adapter<DirectoryAdapter.directoryAdapterViewHolder> {

    private File[] mFiles;

    @Override
    public directoryAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.directory_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new directoryAdapterViewHolder(view);

    }

    @Override
    public void onBindViewHolder(directoryAdapterViewHolder holder, int position) {
        String filenameAtPosition = mFiles[position].toString();
        holder.mDirectoryListItemTV.setText(filenameAtPosition);

    }

    @Override
    public int getItemCount() {
        if (mFiles != null) {
            return mFiles.length;
        }else {
            return 0;
        }
    }

    public class directoryAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView mDirectoryListItemTV;

        public directoryAdapterViewHolder(View itemView) {
            super(itemView);
            mDirectoryListItemTV = (TextView) itemView.findViewById(R.id.tv_directory_list_item);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {


        }
    }

    public void setmFiles(File[] files){
        mFiles = files;
        notifyDataSetChanged();
    }
}
