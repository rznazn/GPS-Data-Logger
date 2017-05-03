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

public class DirectoryAdapter extends RecyclerView.Adapter<DirectoryAdapter.directoryAdapterViewHolder>{
    /**
     * global class variables
     */
    private final directoryAdapterOnClickHandler mClickHandler;
    private File[] mFiles;

    /**
     * Creates a DirectoryAdapter.
     *
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public DirectoryAdapter(directoryAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    /**
     * create view holder object to be filled with assigned values
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public directoryAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.directory_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new directoryAdapterViewHolder(view);

    }

    /**
     * assign data set to appropriate view
     * @param holder the holder object that will display the desired data
     * @param position in the array of data being displayed
     */
    @Override
    public void onBindViewHolder(directoryAdapterViewHolder holder, int position) {
        String filenameAtPosition = mFiles[position].getName().replace(".txt","");
        holder.mDirectoryListItemTV.setText(filenameAtPosition);

    }

    /**
     *
     * @return the number of items to be dispalyed in the recyclerview
     */
    @Override
    public int getItemCount() {
        if (mFiles != null) {
            return mFiles.length;
        }else {
            return 0;
        }
    }

    /**
     * viewHolder object class. the actual view object that gets displayed in the recyclerview
     */
    public class directoryAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView mDirectoryListItemTV;

        public directoryAdapterViewHolder(View itemView) {
            super(itemView);
            mDirectoryListItemTV = (TextView) itemView.findViewById(R.id.tv_directory_list_item);
            itemView.setOnClickListener(this);
        }

        /**
         * get the information needed from the clicked view to pass into the clickhandler
         * @param v
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            String file = mFiles[adapterPosition].getName().replace(".txt", "");
            mClickHandler.onClick(file);

        }
    }

    /**
     * public method to send new data for the recycler view
     * @param files
     */
    public void setmFiles(File[] files){
        mFiles = files;
        notifyDataSetChanged();
    }


    /**
     * The interface that receives onClick messages.
     * onClick argument passes objects to the activity where this method is overridden
     */
    public interface directoryAdapterOnClickHandler {
        void onClick(String file);
    }

}
