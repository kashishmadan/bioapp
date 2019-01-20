package com.telhai.spl.coregulation;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bluemaestro.utility.sdk.ClosenessMainActivity;
import com.bluemaestro.utility.sdk.database.TemperatureDatabaseHelper;
import com.telhai.spl.crydetector.AudioRecordActivity;
import com.telhai.spl.crydetector.UploadManager;
import com.experiencesampler.experiencesampler.MainActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nl.tue.ppeters.flower.FlowerActivity;

public class CoRegulationMainActivity extends AppCompatActivity
{

    private static final String TAG = "CoRegulationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_co_regulation_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_co_regulation_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.preferences:
                Intent openPrefs = new Intent(CoRegulationMainActivity.this,
                        CoRegulationPreferencesActivity.class);
                startActivity(openPrefs);
                break;
            case R.id.upload_all:
                final String serverUrl = PreferenceManager.getDefaultSharedPreferences(this).getString("server_url_main", "");
                File audioFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                        getString(com.telhai.spl.crydetector.R.string.app_name) + "/" +
                                getString(com.telhai.spl.crydetector.R.string.title_appmain));
                File flowerFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                        getString(nl.tue.ppeters.flower.R.string.app_name) + "/" +
                                getString(nl.tue.ppeters.flower.R.string.app_name_flower));
                List<File> allFiles = new ArrayList<>();
                int numFiles = 0;
                if(audioFolder.isDirectory())
                {
                    for(File file : audioFolder.listFiles())
                    {
                        allFiles.add(file);
                        ++numFiles;
                    }
                }
                if(flowerFolder.isDirectory())
                {
                    for(File file : flowerFolder.listFiles())
                    {
                        allFiles.add(file);
                        ++numFiles;
                    }
                }
                File closenessDbFile = new File(TemperatureDatabaseHelper.getDbFileName());
                if(closenessDbFile.exists())
                {
                    allFiles.add(closenessDbFile);
                    ++numFiles;
                }
                if(!allFiles.isEmpty())
                {
                    final File[] fileArray = new File[numFiles];
                    allFiles.toArray(fileArray);
                    AlertDialog alertDialog = new AlertDialog.Builder(CoRegulationMainActivity.this).create();
                    alertDialog.setTitle("Upload and Delete");
                    alertDialog.setMessage("Upload to " + serverUrl + " and delete files?");
                    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "OK", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            Runnable r = new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    UploadManager.uploadAllFiles(getApplicationContext(), serverUrl, fileArray, true, false);
                                }
                            };
                            new Thread(r).start();
                        }
                    });
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", (DialogInterface.OnClickListener) null);
                    alertDialog.show();
                } else
                {
                    Toast.makeText(getApplicationContext(), "No files to upload", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.action_settings:
                return true;
            default:
                Log.e(TAG, "case not handled");
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Called when the user taps the Open Flower Activity button
     */
    public void openFlowerActivity(View view)
    {
        Intent intent = new Intent(this, FlowerActivity.class);
        startActivity(intent);
    }

    /**
     * Called when the user taps the Open Closeness Activity button
     */
    public void openClosenessActivity(View view)
    {
        Intent intent = new Intent(this, ClosenessMainActivity.class);
        startActivity(intent);
    }

    /**
     * Called when the user taps the Open Cry Detector Activity button
     */
    public void openCryDetectorActivity(View view)
    {
        Intent intent = new Intent(this, AudioRecordActivity.class);
        startActivity(intent);
    }

    /**
     * Called when the user taps the Open experience sampler Activity button
     */
    public void openExperienceSamplerActivity(View view)
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
