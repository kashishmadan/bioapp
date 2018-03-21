package com.telhai.spl.crydetector;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.AndroidRuntimeException;
import android.util.Log;

import java.sql.Timestamp;

/**
 * Provides audio data frame-by-frame from the microphone
 * Created by Dima Ruinskiy on 11/27/15.
 */
public class AudioDataMicRecorder implements IAudioDataProvider
{

    private static final String TAG = "AUDRECPROC::ADMC";
    boolean initialized = false;
    boolean recording = false;
    /* Basic parameters */
    private int frequency;
    private int channelConfiguration;
    private int audioEncoding;
    private int bufferSize; // Buffer for recording object
    private int frameSize; // Audio Frame size
    private AudioRecord recorder = null;    // Actual recording object
    /* Buffer to hold raw audio data (recorded from microphone) */
    private short sData[] = null;

    AudioDataMicRecorder(int frequency, int channelConfiguration, int audioEncoding)
    {
        this.frequency = frequency;
        this.channelConfiguration = channelConfiguration;
        this.audioEncoding = audioEncoding;
        this.bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
    }

    public boolean Init(int frameSize)
    {
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,   // Initialize internal android recording object
                frequency, channelConfiguration,
                audioEncoding, bufferSize * 4);

        initialized = (recorder.getState() == AudioRecord.STATE_INITIALIZED);

        if(initialized)
        {
            this.frameSize = frameSize;
            sData = new short[frameSize];
        }

        return initialized;
    }

    public void Start()
    {
        if(recorder.getState() != AudioRecord.STATE_INITIALIZED)
        {
            Log.e(TAG, "Error initializing Audio recorder!\n");
            throw (new AndroidRuntimeException("AudioRecord object failed to initialize."));
        }

        recorder.startRecording();
        recording = true;
    }

    public void Stop()
    {
        recording = false;
        recorder.stop();
    }

    @Override
    public void Release()
    {
        if(recorder != null)
        {
            Stop();
            recorder.release();
            recorder = null;
            sData = null;
        }
    }

    public boolean GetNextFrame(double[] frameData)
    {
        if(!recording)
        {
            return false;
        }
        // Gets one frame of audio from the microphone
        int res = recorder.read(sData, 0, frameSize);
        if(res < frameSize)    // not enough data available
        {
            return false;
        }
        Log.i(TAG, (new Timestamp(System.currentTimeMillis()) + " Read " + res + " samples."));
        pcmShortToDouble(sData, frameData, frameSize);
        return true;
    }

    /* convert short to double assuming 16-bit PCM wave format */
    private void pcmShortToDouble(short[] input, double[] output, int windowSize)
    {
        for(int i = 0; i < windowSize; i++)
        {
            output[i] = input[i] / 32768.0;
        }
    }
}
