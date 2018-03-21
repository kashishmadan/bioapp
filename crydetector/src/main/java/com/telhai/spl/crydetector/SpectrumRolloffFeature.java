package com.telhai.spl.crydetector;

/**
 * Implements ScalarFeature interface.
 * Computes spectrum roll-off point
 * Created by Dima Ruinskiy on 03/09/17.
 */

public class SpectrumRolloffFeature implements IScalarFeature
{
    static final double ROLLOFF_PT = 0.75;

    public double Compute(AudioFrameHandle handle)
    {

        double[] fftMag = handle.GetFFTMagnitude();
        double partialEn = 0, totalEn = 0;
        double freq = 0;
        for(int i = 0; i < fftMag.length - 1; i++)
        {
            totalEn += fftMag[i];
        }
        double rolloffEn = ROLLOFF_PT * totalEn;
        for(int i = 0; i < fftMag.length; i++)
        {
            partialEn += fftMag[i];
            if(partialEn > rolloffEn)
            {
                freq = (double) PrecomputedFilters.sampleRate * i / PrecomputedFilters.fftLength;
                break;
            }
        }
        return freq;
    }
}
