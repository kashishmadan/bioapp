package com.telhai.spl.crydetector;

import com.telhai.spl.WavFile.WavFile;
import com.telhai.spl.WavFile.WavFileException;

import java.io.File;

/**
 * Provides audio data frame-by-frame from an existing WAV file
 * Note: currently assume only mono WAV files
 * Created by Dima Ruinskiy on 12/24/15.
 */
public class AudioDataWaveFileReader implements IAudioDataProvider
{

    private static final String TAG = "AUDRECPROC::ADWR";
    boolean initialized = false;
    double[][] audioBuffer = null;
    private String wavFileName;
    private WavFile wavReader;
    private int frequency;
    private int frameSize;
    private int numChannels;

    AudioDataWaveFileReader(String filename, int frequency)
    {
        wavFileName = filename;
        this.frequency = frequency;
    }

    /* returns true if initialization is successful */
    public boolean Init(int frameSize)
    {
        try
        {
            wavReader = WavFile.openWavFile(new File(wavFileName));
            wavReader.display();
            numChannels = wavReader.getNumChannels();
            if(wavReader.getSampleRate() != frequency)
            {
                wavReader.close();
                throw new WavFileException("WAV file sampling rate does not match.");
            }
            // Create a buffer for numChannels channels
            audioBuffer = new double[numChannels][frameSize];
        } catch(WavFileException e)
        {
            e.printStackTrace();
            return false;
        } catch(java.io.IOException e)
        {
            e.printStackTrace();
            return false;
        }

        // If we are here, it means that WAV file was opened successfully
        initialized = true;

        this.frameSize = frameSize;

        return true;
    }

    /* returns true/false if audio data was provided */
    public boolean GetNextFrame(double[] frameData)
    {

        int framesRead;

        try
        {
            framesRead = wavReader.readFrames(audioBuffer, frameSize);  // Read all channels from WAV
            System.arraycopy(audioBuffer[0], 0, frameData, 0, frameSize);   // Take only first channel
            return (framesRead == frameSize);   // return true only if there were enough samples to read
        } catch(WavFileException e)
        {
            e.printStackTrace();
        } catch(java.io.IOException e)
        {
            e.printStackTrace();
        }

        return false;   // If we got here, something went wrong
    }

    @Override
    public void Release()
    {
        if(wavReader != null)
        {
            try
            {
                wavReader.close();
            } catch(java.io.IOException e)
            {
                e.printStackTrace();
            }
            wavReader = null;
        }
    }

}
