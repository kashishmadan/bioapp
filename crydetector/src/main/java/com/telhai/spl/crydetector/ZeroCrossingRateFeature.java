package com.telhai.spl.crydetector;

/**
 * Implements ScalarFeature interface.
 * Computes zero-crossing rate (percentage of samples that change sign)
 * Created by Dima Ruinskiy on 10/21/15.
 */
public class ZeroCrossingRateFeature implements IScalarFeature
{
    public double Compute(AudioFrameHandle handle)
    {
        double[] buffer = handle.GetAudioFrame();
        int len = buffer.length;
        int zc = 0;

        double mean = 0.0;
        for(int i = 0; i < len; i++)
        {
            mean += buffer[i];
        }

        for(int i = 0; i < len - 1; i++)
        {
            if((buffer[i] - mean) * (buffer[i + 1] - mean) < 0)
            {
                ++zc;
            }
        }

        return (double) zc / len;
    }
}
