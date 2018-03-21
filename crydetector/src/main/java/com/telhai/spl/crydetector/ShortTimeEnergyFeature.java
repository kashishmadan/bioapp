package com.telhai.spl.crydetector;

/**
 * Implements ScalarFeature interface.
 * Computes short-time energy (sum of squares divided by frame length)
 * Created by Dima Ruinskiy on 10/21/15.
 */
public class ShortTimeEnergyFeature implements IScalarFeature
{
    public double Compute(AudioFrameHandle handle)
    {
        double[] buffer = handle.GetAudioFrame(AudioFrameHandle.FrameFilter.FRAME_LOWPASS_WINDOWED);
        int len = buffer.length;
        double energy = 0;
        for(int i = 0; i < len; i++)
        {
            energy += buffer[i] * buffer[i];
        }

        return energy;  // Do not normalize by buffer length here
    }
}
