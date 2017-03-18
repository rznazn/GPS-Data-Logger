package com.example.android.gpsdatalogger;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Created by sport on 3/17/2017.
 * this class is to facilitate reading and writing to external storage
 */

public class StorageManager {

    private StorageManager(){}

    /**
     * create a document for future use
     */

    public static void createFileInexternalStorage (Context context, String directoryname, String filename) {
        String storageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(storageState)) {
            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            File dir = new File(root.getAbsolutePath() + directoryname);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File log = new File(dir, filename + ".txt");
            Log.v("Storage Manager", log.toString());
            try {
                FileOutputStream fos = new FileOutputStream(log, true);
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * get directory list
     */
    public static File[] getFilesInDirectory(Context context, String directoryname) {
            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            File dir = new File(root.getAbsolutePath() + directoryname);
            File[] files = dir.listFiles();
        return files;
    }


    /**
     * write current readings to file
     */
    public static void writeToExternalStorage(Context context, String directoryname, String filename,
                                       String content, boolean appendContent) {
        if (appendContent) {
            String storageState = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(storageState)) {
                File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                File dir = new File(root.getAbsolutePath() + directoryname);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File log = new File(dir, filename + ".txt");

                try {
                    FileOutputStream fos = new FileOutputStream(log, true);
                    PrintWriter pw = new PrintWriter(fos);
                    pw.write(content);
                    pw.flush();
                    pw.close();
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(context, "External Storage no available", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * read data file from file and show in display
     */
    public static String readFromExternalStorage(Context context, String directoryname, String filename) {
        File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File dir = new File(root.getAbsolutePath() + directoryname);
        File log = new File(dir, filename + ".txt");

        StringBuffer sb = new StringBuffer();

        try {
            FileInputStream fis = new FileInputStream(log);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String logForDisplay;

            while ((logForDisplay = br.readLine()) != null) {
                sb.append(logForDisplay + "\n");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (sb != null) {
            return sb.toString();
        }else {
            return null;
        }
    }
}
