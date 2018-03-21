package com.telhai.spl.crydetector;

/**
 * Represents an interface to provide a frame of audio data for further processing.
 * Created by Dima Ruinskiy on 11/27/15.
 */
public interface IAudioDataProvider
{
    /* returns true if initialization is successful */
    public boolean Init(int frameSize);

    /* returns true/false if audio data was provided */
    public boolean GetNextFrame(double[] frameData);

    /* shut down and free all resources */
    public void Release();
}
