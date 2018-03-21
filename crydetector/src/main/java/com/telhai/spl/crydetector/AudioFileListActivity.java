package com.telhai.spl.crydetector;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Dima Ruinskiy on 09-11-17.
 */

public class AudioFileListActivity extends ListActivity
{
    //initialize view's
    ListView simpleListView;
    String[] fileNames;
    String[] fileTimes;
    File[] files;
    MediaPlayer mPlayer = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        simpleListView = getListView();

        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                getString(R.string.app_name) + "/" + getString(R.string.title_appmain));

        if(!folder.isDirectory())
        {
            return;
        }
        files = folder.listFiles();
        fileNames = new String[files.length];
        fileTimes = new String[files.length];
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        int totalSize = 0;
        for(int i = 0; i < files.length; i++)
        {
            fileNames[i] = files[i].getName();
            fileTimes[i] = sdf.format(files[i].lastModified());
            totalSize += files[i].length();
        }

        ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
        for(int i = 0; i < fileNames.length; i++)
        {
            HashMap<String, String> hashMap = new HashMap<>();//create a hashmap to store the data in key value pair
            hashMap.put("name", fileNames[i]);
            hashMap.put("time", fileTimes[i] + "");
            arrayList.add(hashMap);//add the hashmap into arrayList
        }
        String[] from = {"name", "time"};//string array
        int[] to = {R.id.textViewFileName, R.id.textViewFileTimestamp};//int array of views id's
        SimpleAdapter simpleAdapter =
                new SimpleAdapter(this, arrayList, R.layout.activity_list_view, from, to);//Create object and set the parameters for
        // simpleAdapter
        simpleListView.setAdapter(simpleAdapter);//sets the adapter for listView

        //perform listView item click event
        simpleListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l)
            {
                AlertDialog alertDialog = new AlertDialog.Builder(AudioFileListActivity.this).create();
                alertDialog.setTitle("Action");
                alertDialog.setMessage(fileTimes[i]);
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Play", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        try
                        {
                            if(mPlayer.isPlaying())
                            {
                                mPlayer.stop();
                            }
                            mPlayer.release();
                            mPlayer = new MediaPlayer();
                            mPlayer.setDataSource(files[i].getPath());
                            mPlayer.prepare();
                            mPlayer.start();
                        } catch(IOException e)
                        {
                            Toast.makeText(getApplicationContext(), "Error playing file!", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                });
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Delete", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        AlertDialog confirmDialog = new AlertDialog.Builder(AudioFileListActivity.this).create();
                        confirmDialog.setTitle("Delete");
                        confirmDialog.setMessage("Are you sure?");
                        confirmDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Yes", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                File file = files[i];
                                file.delete();
                                recreate();
                            }
                        });
                        confirmDialog.setButton(DialogInterface.BUTTON_POSITIVE, "No", (DialogInterface.OnClickListener) null);
                        confirmDialog.show();
                    }
                });
                alertDialog.show();
            }
        });

        ((TextView) (findViewById(R.id.textViewFileInfo)))
                .setText(files.length + " files. Total size: " + String.format("%.2f", ((double) totalSize / 1024 / 1024)) + " MB");

        (findViewById(R.id.btnDelete)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog alertDialog = new AlertDialog.Builder(AudioFileListActivity.this).create();
                alertDialog.setTitle("Delete");
                alertDialog.setMessage("Delete all files???");
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Delete", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        for(File file : files)
                        {
                            file.delete();
                        }
                        recreate();
                    }
                });
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", (DialogInterface.OnClickListener) null);
                alertDialog.show();
            }
        });

        (findViewById(R.id.btnUpload)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final String serverUrl =
                        PreferenceManager.getDefaultSharedPreferences(AudioFileListActivity.this).getString("server_url_main", "");
                AlertDialog alertDialog = new AlertDialog.Builder(AudioFileListActivity.this).create();
                alertDialog.setTitle("Upload");
                alertDialog.setMessage("Upload to " + serverUrl + '?');
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Upload", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Runnable r = new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                UploadManager.uploadAllFiles(getApplicationContext(), serverUrl, files, false, false);
                            }
                        };
                        new Thread(r).start();
                    }
                });
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", (DialogInterface.OnClickListener) null);
                alertDialog.show();

            }
        });
    }

    @Override
    protected void onDestroy()
    {
        mPlayer.release();
        super.onDestroy();
    }
}
