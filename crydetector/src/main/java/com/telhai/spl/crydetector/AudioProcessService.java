package com.telhai.spl.crydetector;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.net.HttpURLConnection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import com.telhai.spl.WavFile.WavFile;
import com.telhai.spl.WavFile.WavFileException;

/**
 * Created by Dima Ruinskiy on 12-10-17.
 */

public class AudioProcessService extends IntentService
{

    /* Detected segment wave file creation parameters */
    public static final int MAX_DURATION_SEC = 10;
    private static final double MIN_STORAGE_SPACE_THRESHOLD = 0.10;
    private static final int NOTIFICATION_ID = 999983;  // Largest prime under 1M :)
    private static final String TAG = "AUDRECPROC::APS";
    File folder = null;
    private String serverUrl = null;
    private boolean autoUp, autoDel;
    private IAudioDataProvider audioDataProvider = null;
    private FrameProcessor frameProcessor = null;
    private double[] frameData = null;
    private double[][] audioDataBuffers = null;
    private boolean[] audioDataBufferLocked = null;
    private boolean serviceRunning = false;
    private String imei, path;

    public AudioProcessService()
    {
        super("com.telhai.spl.crydetector.AudioProcessService");
    }

    // We are not supposed to override the functions below - do it for logging only (temporary)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i(TAG, "onStartCommand called.");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.i(TAG, "onCreate called.");

        audioDataBuffers = new double[2][MAX_DURATION_SEC * AudioRecordActivity.RECORDER_SAMPLERATE];
        audioDataBufferLocked = new boolean[]{false, false};
        imei = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        Log.d(TAG, "IMEI: " + imei);

        // Get the directory for the app's audio recordings.
        folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                getString(R.string.app_name) + "/" + getString(R.string.title_appmain));
        if(folder.exists())
        {
            Log.d(TAG, "Directory exists.");
        } else if(!folder.mkdirs())
        {
            Log.e(TAG, "Directory not created!");
        }
        path = folder.toString();
        Log.d(TAG, "PATH: " + path);
    }

    @Override
    public void onDestroy()
    {
        Log.i(TAG, "onDestroy called.");
        serviceRunning = false;
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent)
    {
        Log.i(TAG, "onHandleIntent called.");

        if(intent == null)
        {
            Log.e(TAG, "Intent is null!\n");
            return;
        }

        Intent activityReopenIntent = new Intent(this, AudioRecordActivity.class);
        PendingIntent notificationPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        activityReopenIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        Notification.Builder builder = new Notification.Builder(getBaseContext())
                .setSmallIcon(R.drawable.ic_stat_name)
                .setTicker("Infant Cry Detector Recording Service")
                .setContentTitle("Infant Cry Detector")
                .setContentText("Listening...")
                .setContentIntent(notificationPendingIntent);

        startForeground(NOTIFICATION_ID, builder.build());

        try
        {
            processIntentAudioLoop(intent);
        } finally
        {
            stopForeground(true);
        }
    }

    private void processIntentAudioLoop(@Nullable Intent intent)
    {
        AudioRecordActivity.AudioSrc audioSrc = (AudioRecordActivity.AudioSrc) intent.getSerializableExtra("source");
        TrainingSet.SpecificTrainingSet trainingSet = (TrainingSet.SpecificTrainingSet) intent.getSerializableExtra("trainingset");
        int sampleRate = intent.getIntExtra("samplerate", AudioRecordActivity.RECORDER_SAMPLERATE);
        int windowSize = intent.getIntExtra("windowsize", AudioRecordActivity.ALG_WINDOWSIZE);

        boolean savingRecorded = false;

        int framesPerBuffer = (MAX_DURATION_SEC * AudioRecordActivity.RECORDER_SAMPLERATE) / windowSize;

        FrameResult result;
        int totalDetected = 0;
        int activeBuffer = 0;
        int activeFrame = 0;

        Arrays.fill(audioDataBuffers[0], 0.0);
        Arrays.fill(audioDataBuffers[1], 0.0);

        switch(audioSrc)
        {
            case AUDIOSRC_MIC:
                AudioDataMicRecorder recorder = new AudioDataMicRecorder(sampleRate,
                        AudioRecordActivity.RECORDER_CHANNELS,
                        AudioRecordActivity.RECORDER_AUDIO_ENCODING);
                if(!recorder.Init(windowSize))
                {
                    Log.e(TAG, "Error initializing Audio recorder!\n");
                    return;
                }
                recorder.Start();
                audioDataProvider = recorder;
                break;
            case AUDIOSRC_FILE:
                String filename = intent.getStringExtra("filename");
                AudioDataWaveFileReader reader = new AudioDataWaveFileReader(filename, sampleRate);
                if(!reader.Init(windowSize))
                {
                    Log.e(TAG, "Error initializing Audio file reader!\n");
                    return;
                }
                audioDataProvider = reader;
                break;
            default:
                Log.e(TAG, "Bad audio source!\n");
                return;
        }

        frameProcessor = new FrameProcessor(sampleRate, windowSize, windowSize, trainingSet);
        frameData = new double[windowSize];

        serverUrl = intent.getStringExtra("url");
        autoUp = intent.getBooleanExtra("auto_upload", false);
        autoDel = intent.getBooleanExtra("auto_delete", false);

        serviceRunning = true;
        while(audioDataProvider.GetNextFrame(frameData) && serviceRunning)
        {
            result = frameProcessor.ProcessFrame(frameData);  // Process data frame
            //Log.i(TAG, (new Timestamp(System.currentTimeMillis()) +  " Frame processed."));

            if(result == FrameResult.SAVE && !isLocked(activeBuffer))
            {
                savingRecorded = true;
                totalDetected++;
            }

            if(savingRecorded)
            {
                saveFrameData(frameData, activeBuffer, activeFrame, windowSize);
                activeFrame++;
                if(activeFrame == framesPerBuffer)
                {
                    Log.i(TAG, (new Timestamp(System.currentTimeMillis()) + " Local buffer full. Total detected frames in buffer = " +
                            totalDetected));
                    if((double) totalDetected / framesPerBuffer > 0.25)
                    {   // High enough percentage of frames detected - save WAV file
                        if(isFreeStorageLessThan(MIN_STORAGE_SPACE_THRESHOLD))
                        {
                            Log.i(TAG, "Storage space too low. Not saving detected frames.");
                            new Handler(Looper.getMainLooper()).post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    Toast.makeText(getApplicationContext(), "Storage space too low. Not saving detected frames.", Toast
                                            .LENGTH_LONG)
                                            .show();
                                }
                            });
                        } else
                        {
                            setBufferLock(activeBuffer, true);
                            if(isFreeStorageLessThan(MIN_STORAGE_SPACE_THRESHOLD + 0.05))
                            {
                                final File[] files = folder.listFiles();
                                if(files.length > 0)
                                {
                                    Log.i(TAG, "Storage space is getting low. Attempting to upload and delete all files.");
                                    new Thread(new Runnable()
                                    {
                                        public void run()
                                        {
                                            UploadManager.uploadAllFiles(getApplicationContext(), serverUrl, files, true, true);
                                        }
                                    }).start();
                                }
                            }
                            saveBufferAsWav(activeBuffer);
                            activeBuffer = (1 - activeBuffer);
                            Log.i(TAG, ("Setting active buffer to " + activeBuffer));
                        }
                    }
                    activeFrame = 0;
                    savingRecorded = false;
                    totalDetected = 0;
                }
                //Log.i(TAG, (new Timestamp(System.currentTimeMillis()) +  " Frame saved in local buffer."));
            }
        }
        serviceRunning = false;
        audioDataProvider.Release();
    }

    private void saveBufferAsWav(final int activeBuffer)
    {
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                final String filename = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
                Log.i(TAG, filename);
                String pathname = (path + "/" + imei + "_" + filename + ".wav");
                try
                {
                    final File newFile = new File(pathname);
                    WavFile wavFile =
                            WavFile.newWavFile(newFile, 1, audioDataBuffers[activeBuffer].length, 16, AudioRecordActivity
                                    .RECORDER_SAMPLERATE);
                    wavFile.writeFrames(audioDataBuffers[activeBuffer], audioDataBuffers[activeBuffer].length);
                    wavFile.close();
                    if(autoUp)
                    {
                        Runnable r = new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if(UploadManager.uploadFile(newFile, serverUrl) == HttpURLConnection.HTTP_OK)
                                {
                                    Log.i(TAG, filename + " uploaded");
                                    if(autoDel)
                                    {
                                        newFile.delete();
                                        Log.i(TAG, filename + " deleted");
                                    }
                                } else
                                {
                                    Log.i(TAG, filename + " upload failed");
                                }
                            }
                        };
                        new Thread(r).start();
                    }
                } catch(WavFileException e)
                {
                    e.printStackTrace();
                    Log.e(TAG, "WAV file creation failed.");
                } catch(java.io.IOException e)
                {
                    e.printStackTrace();
                    Log.e(TAG, "WAV file creation failed.");
                }
                setBufferLock(activeBuffer, false);
            }
        };
        new Thread(r).start();
    }

    private void setBufferLock(int activeBuffer, boolean b)
    {
        audioDataBufferLocked[activeBuffer] = b;
        Log.i(TAG, ((b ? "Locking buffer " : "Unlocking buffer ") + activeBuffer));
    }

    private boolean isLocked(int activeBuffer)
    {
        return audioDataBufferLocked[activeBuffer];
    }

    private void saveFrameData(double[] frameData, int activeBuffer, int activeFrame, int windowSize)
    {
        System.arraycopy(frameData, 0, audioDataBuffers[activeBuffer], activeFrame * windowSize, windowSize);
    }

    private boolean isFreeStorageLessThan(double threshold)
    {
        // Is the amount of free space <= min_free_storagespace% of the total space, return false. Otherwise return true;
        double freespace = (double) Environment.getExternalStorageDirectory().getFreeSpace();
        double totalspace = (double) Environment.getExternalStorageDirectory().getTotalSpace();

        if(freespace / totalspace < threshold)
        {
            return true;
        } else
        {
            return false;
        }
    }

    public synchronized void setAudioDataProvider(IAudioDataProvider provider)
    {
        audioDataProvider = provider;
    }

    public synchronized void setFrameProcessor(FrameProcessor processor)
    {
        frameProcessor = processor;
    }
}
