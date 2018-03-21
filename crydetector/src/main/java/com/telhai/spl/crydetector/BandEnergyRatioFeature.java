package com.telhai.spl.crydetector;

/**
 * Implements ScalarFeature interface.
 * Computes band energy ratio.s
 * Created by Dima Ruinskiy on 03/09/17.
 */

public class BandEnergyRatioFeature implements IScalarFeature
{
    static final double BOUNDARY_FREQ_HZ = 2500;

    public double Compute(AudioFrameHandle handle)
    {

        double[] fftMag = handle.GetFFTMagnitude();

        int B1L, B1H, B2L, B2H;
        double N = 2 * (double) fftMag.length / (double) PrecomputedFilters.sampleRate;

        B1L = (int) Math.floor(N * BOUNDARY_FREQ_HZ);
        B1H = fftMag.length - 1;
        B2L = 0;
        B2H = (int) Math.floor(N * BOUNDARY_FREQ_HZ);

        double B1E = 0, B2E = 0;

        for(int i = B1L; i < B1H; i++)
        {
            B1E += fftMag[i] * fftMag[i];
        }

        for(int i = B2L; i < B2H; i++)
        {
            B2E += fftMag[i] * fftMag[i];
        }

        return 10 * Math.log10(B1E) - 10 * Math.log10(B2E);
    }
}
