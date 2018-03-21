package com.telhai.spl.crydetector;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

// TODO: Control recording threshold / maybe show files from menu (big app only)?
// TODO: Communicate from service to activity when service is quitting (for correct button states)
// TODO: Indicate when recorder is running
// TODO: Check POST response - can get OK even when upload failed???
// TODO: Implement scheduled upload all / storage-driven upload all

/* Main class running the app */
public class AudioRecordActivity extends AppCompatActivity
{

    public static final int RECORDER_SAMPLERATE = 11025;
    public static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    public static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final int ALG_WINDOWSIZE = 1024;

    private static final String TAG = "AUDRECPROC::MAIN";
    private static final String serviceName = "com.telhai.spl.crydetector.AudioProcessService";

    /* Interfaces to audio data service */

    private Intent apServiceIntent = null;

    private boolean isRecording = false;
    private boolean processingFile = false;

    private SharedPreferences preferences;
    private String serverUrl;

    private Thread serviceAlivenessThread = null;
    private AudioSrc lastKnownSource = AudioSrc.AUDIOSRC_NONE;

    ;
    private View.OnClickListener btnClick = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            loadCurrentPreferenceValues();
            int id = v.getId();
            if(id == R.id.btnStart)
            {
                isRecording = true;
                startRecording();
            } else if(id == R.id.btnStop)
            {
                isRecording = false;
                stopRecording();
            } else if(id == R.id.btnFileStart)
            {
                processingFile = true;
                startProcessWav();
            } else if(id == R.id.btnFileStop)
            {
                processingFile = false;
                stopProcessWav();
            } else if(id == R.id.btnFileList)
            {
                Intent intent = new Intent(getApplicationContext(), AudioFileListActivity.class);
                startActivity(intent);
            }
            setButtons();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        serverUrl = preferences.getString("server_url_main", "");
        Log.i(TAG, "Server URL: " + serverUrl);

        loadState();

        apServiceIntent = new Intent(this, AudioProcessService.class);
        apServiceIntent.putExtra("samplerate", RECORDER_SAMPLERATE);
        apServiceIntent.putExtra("windowsize", ALG_WINDOWSIZE);
        apServiceIntent.putExtra("trainingset", TrainingSet.SpecificTrainingSet.SET_1709_11K);

        ((CheckBox) findViewById(R.id.chkRecorderStatus)).setClickable(false);
        setButtonHandlers();
        setButtons();

        spawnServiceAlivenessThread();
    }

    private void spawnServiceAlivenessThread()
    {
        serviceAlivenessThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while(!Thread.currentThread().isInterrupted())
                {
                    isRecording = processingFile = false;
                    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                    for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
                    {
                        if(serviceName.equals(service.service.getClassName()))
                        {
                            switch(lastKnownSource)
                            {
                                case AUDIOSRC_MIC:
                                    isRecording = true;
                                    break;
                                case AUDIOSRC_FILE:
                                    processingFile = true;
                                    break;
                                default:
                            }
                            break;
                        }
                    }
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            setButtons();
                        }
                    });
                    try
                    {
                        Thread.sleep(3000);
                    } catch(InterruptedException e)
                    {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        serviceAlivenessThread.start();
    }

    @Override
    public void onDestroy()
    {
        serviceAlivenessThread.interrupt();
        saveState();
        Log.i(TAG, "AudioRecordActivity: destroyed");
        super.onDestroy();
    }

    private void setButtonHandlers()
    {
        (findViewById(R.id.btnStart)).setOnClickListener(btnClick);
        (findViewById(R.id.btnStop)).setOnClickListener(btnClick);
        //        (findViewById(R.id.btnFileStart)).setOnClickListener(btnClick);
        //        (findViewById(R.id.btnFileStop)).setOnClickListener(btnClick);
        (findViewById(R.id.btnFileList)).setOnClickListener(btnClick);
    }

    private void enableButton(int id, boolean isEnable)
    {
        (findViewById(id)).setEnabled(isEnable);
    }

    /* start button is enabled if and only if not recording; stop button is enabled if and only if recording */
    private void setButtons()
    {
        enableButton(R.id.btnStart, !isRecording && !processingFile);
        enableButton(R.id.btnStop, isRecording);
        //        enableButton(R.id.btnFileStart, !isRecording && !processingFile);
        enableButton(R.id.btnFileStop, processingFile);
        enableButton(R.id.btnFileList, true);
        ((CheckBox) findViewById(R.id.chkRecorderStatus)).setChecked((isRecording || processingFile));
    }

    private void startRecording()
    {
        lastKnownSource = AudioSrc.AUDIOSRC_MIC;
        apServiceIntent.putExtra("source", lastKnownSource);
        startService(apServiceIntent);
    }

    private void stopRecording()
    {
        stopService(apServiceIntent);
    }

    private void loadCurrentPreferenceValues()
    {
        serverUrl = preferences.getString("server_url_main", "");
        apServiceIntent.putExtra("url", serverUrl);
        apServiceIntent.putExtra("auto_upload", preferences.getBoolean("checkboxAutoUp", false));
        apServiceIntent.putExtra("auto_delete", preferences.getBoolean("checkboxDeleteUploaded", false));
    }

    private void startProcessWav()
    {
        lastKnownSource = AudioSrc.AUDIOSRC_FILE;
        apServiceIntent.putExtra("source", lastKnownSource);
        apServiceIntent.putExtra("filename", "/sdcard/media/waves/amber/amber_11k_4_0_0000_5_0_0000_1h.wav");
        startService(apServiceIntent);
    }

    private void stopProcessWav()
    {
        stopService(apServiceIntent);
    }

    private void saveState()
    {
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putString("lastKnownSource", lastKnownSource.name());
        editor.commit();
    }

    private void loadState()
    {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        lastKnownSource = AudioSrc.valueOf(sharedPreferences.getString("lastKnownSource", "AUDIOSRC_NONE"));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_audio_record, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == R.id.preferences)
        {
            Intent openPrefs = new Intent(AudioRecordActivity.this,
                    AudioPreferencesActivity.class);
            startActivity(openPrefs);
        } else if(item.getItemId() == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public enum AudioSrc
    {
        AUDIOSRC_NONE,
        AUDIOSRC_MIC,
        AUDIOSRC_FILE
    }
}
