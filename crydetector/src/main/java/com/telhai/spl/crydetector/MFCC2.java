package com.telhai.spl.crydetector;

/**
 * Created by Dima Ruinskiy on 08/08/16.
 * <p>
 * Based on MelBankM/MelCepst Matlab VoiceBox implementation by (C) Mike Brookes 1997
 */
public class MFCC2
{

    /* Constants for Freq-to-Mel Conversion */
    private static double FRQ2MEL_K = 1000.0 / Math.log1p(1000.0 / 700.0);    // ~1127.01048
    /* Constants for Freq-to-ERB Conversion */
    private static double FRQ2ERB_A = 11.1726796613307;
    private static double FRQ2ERB_C = -14678.4946168094;
    private static double FRQ2ERB_H = 47.0653791116351;
    private static double FRQ2ERB_K = 676170.419311422;
    double sampleRate;
    int fftSize;
    int numCoeffs;
    int numMelBands;
    int numActiveBins;          // MelBankM B4/K4
    double melMaxFreq;          // MelBankM MelRng
    double melBankStep;         // MelBankM MelInc
    double[] melFilterCenters;  // MelBankM MC
    double[] melFftBins;        // MelBankM PF

    ;
    // MelBankM r,c,v
    int sparseMatrixSize;

    ;
    int[] melBankMatRowsFilters;
    int[] melBankMatColsFftBins;
    double[] melBankMatValues;
    WindowMode windowMode;
    FreqScale freqScale;
    public MFCC2(int _frameSize, int _fftSize, int _numCoeffs, int _melBands, double _sampleRate)
    {
        fftSize = _fftSize;
        numCoeffs = _numCoeffs;
        numMelBands = _melBands;
        sampleRate = _sampleRate;

        /* default windowing and scaling modes */
        windowMode = WindowMode.WIN_HAMMING;
        freqScale = FreqScale.SCALE_MEL;

        if(fftSize <= 0)
        {
            throw new IllegalArgumentException("Bad FFT size");
        }
        if(sampleRate <= 0)
        {
            throw new IllegalArgumentException("Bad sample rate");
        }

        SetupMelBank(numMelBands, fftSize, sampleRate);
    }
    /* Constructor with non-default windowing and scaling modes */
    public MFCC2(int _frameSize, int _fftSize, int _numCoeffs, int _melBands, double _sampleRate, WindowMode _windowMode, FreqScale
            _freqScale)
    {
        fftSize = _fftSize;
        numCoeffs = _numCoeffs;
        numMelBands = _melBands;
        sampleRate = _sampleRate;
        windowMode = _windowMode;
        freqScale = _freqScale;

        windowMode = _windowMode;
        freqScale = _freqScale;

        if(fftSize <= 0)
        {
            throw new IllegalArgumentException("Bad FFT size");
        }
        if(sampleRate <= 0)
        {
            throw new IllegalArgumentException("Bad sample rate");
        }

        SetupMelBank(numMelBands, fftSize, sampleRate);
    }

    private static double fmel2hz(double mel)
    {
        return 700.0 * (Math.pow(10.0, mel / 2595.0) - 1.0);
    }

    private static double fhz2mel(double freq)
    {
        return FRQ2MEL_K * Math.log1p(freq / 700.0);
    }

    private static double fhz2erb(double freq)
    {
        return FRQ2ERB_A * Math.log((FRQ2ERB_H - FRQ2ERB_K / (freq - FRQ2ERB_C)));
    }

    /* Compute cepstrum when the raw DFT is given (as two separate arrays - real + imaginary).
     * This function first computes the power (magnitude) spectrum of the first N/2+1 samples,
     * then calls the cepstrum(powerSpectrum) function.
     */
    public double[] cepstrum(double[] fftReal, double[] fftImag)
    {
        double[] powerSpectrum = new double[fftSize / 2 + 1];
        for(int i = 0; i < powerSpectrum.length; i++)
        {
            powerSpectrum[i] = Math.sqrt(fftReal[i] * fftReal[i] + fftImag[i] * fftImag[i]);   // Compute magnitude
        }
        return cepstrumP(powerSpectrum);
    }

    /* Compute cepstrum when the raw DFT is given (as a single array with interleaved real/imaginary parts)
     * This function first computes the power (magnitude) spectrum of the first N/2+1 samples,
     * then calls the cepstrum(powerSpectrum) function.
     */
    public double[] cepstrum(double[] fftRI)
    {
        double[] powerSpectrum = new double[fftSize / 2 + 1];
        for(int i = 0; i < powerSpectrum.length; i++)
        {
            powerSpectrum[i] = Math.sqrt(fftRI[2 * i] * fftRI[2 * i] + fftRI[2 * i + 1] * fftRI[2 * i + 1]);  // Compute magnitude
        }
        return cepstrumP(powerSpectrum);
    }

    /* Compute filter bank energy sequence when the power spectrum is given.
     * The power spectrum is assumed to be the magnitude of the first half of the DFT sequence
     * (actually the first N/2+1 samples, discarding the last N/2-1 samples)
     */
    public double[] filterEn(double[] powerSpectrum)
    {
        if(powerSpectrum.length != fftSize / 2 + 1)
        {
            throw new IllegalArgumentException("Magnitude FFT size does not match expected");
        }

        double powerThreshold = 0;                              // MelCepst PTH/ATH

        /* Assume here that FFT active bins always start at the second element - implementation simplification */
        for(int i = 1; i <= numActiveBins; i++)
        {
            if(powerSpectrum[i] > powerThreshold)
            {
                powerThreshold = powerSpectrum[i];
            }
        }
        powerThreshold = powerThreshold * 1e-10;       // MelCepst ATH

        double[] filterEnergy = new double[numMelBands];

        /* Calculate cepstrum by multiplying the root power spectrum by the pre-computed Mel banks */

/*
        for (int i = 0; i < numMelBands; i++) {
            filterEnergy[i] = 0;
            for (int j = 0; j < numActiveBins ; j++) {
                filterEnergy[i] += melBankMatValues[i][j] * powerSpectrum[j+1];
            }
        }
*/

        for(int i = 0; i < numMelBands; i++)
        {
            filterEnergy[i] = 0;
        }

        for(int i = 0; i < sparseMatrixSize; i++)
        {
            filterEnergy[melBankMatRowsFilters[i]] += (melBankMatValues[i] * powerSpectrum[melBankMatColsFftBins[i] + 1]);
        }

        for(int i = 0; i < numMelBands; i++)
        {
            filterEnergy[i] = (filterEnergy[i] > powerThreshold) ? Math.log(filterEnergy[i]) : Math.log(powerThreshold);
        }

        return filterEnergy;
    }

    /* Compute cepstrum when the power spectrum is given.
     * The cepstrum is computed as the DCT (Direct Cosine Transform) of the filter energy sequence
     * The power spectrum is assumed to be the magnitude of the first half of the DFT sequence
     * (actually the first N/2+1 samples, discarding the last N/2-1 samples).
     */
    public double[] cepstrumP(double[] powerSpectrum)
    {

        return cepstrumE(filterEn(powerSpectrum));
    }

    /* Compute cepstrum when the filter energy sequence is already given (precomputed)
     * The cepstrum is computed as the DCT (Direct Cosine Transform) of the filter energy sequence
     * The power spectrum is assumed to be the magnitude of the first half of the DFT sequence
     * (actually the first N/2+1 samples, discarding the last N/2-1 samples).
     */
    public double[] cepstrumE(double[] filterEnergy)
    {

        double[] dctResults = new double[numMelBands];  // To hold temporary DCT output (before truncating/expanding to numCoeffs)
        double[] mfcc = new double[numCoeffs];          // To hold final output

        DCT.dct(filterEnergy, dctResults);

        for(int i = 0; i < numCoeffs; i++)
        {
            if(i + 1 < numMelBands)
            {
                mfcc[i] = dctResults[i + 1];
            } else
            {
                mfcc[i] = 0;
            }
        }

        return mfcc;
    }

    /* For this simplified implementation we always assume the frequency range to be [0..Fs/2] and do not allow custom ranges */
    private void SetupMelBank(int numMelBands, int fftSize, double sampleRate)
    {
        double scaler = sampleRate / fftSize;
        numActiveBins = fftSize / 2 - 1;              // MelBankM B4/K4

        int melUpperLowerFirst = numActiveBins + 1;   // MelBankM K2
        int melUpperLowerLast = 0;                  // MelBankM K3

        melMaxFreq = scaleFrequency(sampleRate / 2);     // MelBankM MelRng
        melBankStep = melMaxFreq / (numMelBands + 1);    // MelBankM MelInc

        melFilterCenters = new double[numMelBands];    // MelBankM MC
        for(int i = 0; i < numMelBands; )
        {
            melFilterCenters[i] = melBankStep * (++i);
        }

        melFftBins = new double[numActiveBins];         // MelBankM PF
        for(int i = 0; i < numActiveBins; i++)
        {
            melFftBins[i] = scaleFrequency((i + 1) * scaler) / melBankStep;
        }
        for(int i = 0; i < numActiveBins; i++)
        {
            if(melFftBins[i] >= 1.0)
            {
                melUpperLowerFirst = i;     // MelBankM K2
                break;
            }
        }

        for(int i = numActiveBins - 1; i >= 0; i--)
        {
            if(melFftBins[i] < numMelBands)
            {
                melUpperLowerLast = i;      // MelBankM K3
                break;
            }
        }

        sparseMatrixSize = melUpperLowerLast - melUpperLowerFirst + numActiveBins + 1; // Total number of non-zero filter values
        melBankMatRowsFilters = new int[sparseMatrixSize];
        melBankMatColsFftBins = new int[sparseMatrixSize];
        melBankMatValues = new double[sparseMatrixSize];

        for(int i = 0; i <= melUpperLowerLast; i++)
        {
            melBankMatColsFftBins[i] = i;
            melBankMatRowsFilters[i] = (int) Math.floor(melFftBins[i]);
            melBankMatValues[i] = melFftBins[i] - melBankMatRowsFilters[i];
        }

        for(int i = melUpperLowerLast + 1, v = melUpperLowerFirst; i < sparseMatrixSize; i++, v++)
        {
            melBankMatColsFftBins[i] = v;
            melBankMatRowsFilters[i] = (int) Math.floor(melFftBins[v]) - 1;
            melBankMatValues[i] = 2 - melFftBins[v] + melBankMatRowsFilters[i];
        }

        /* Convert triangular filters into Hamming-shaped filters (if requested) and double */
        for(int i = 0; i < sparseMatrixSize; i++)
        {
            if(windowMode == WindowMode.WIN_HAMMING)
            {
                melBankMatValues[i] = 0.5 - 0.46 / 1.08 * Math.cos(Math.PI * melBankMatValues[i]);
            }
            melBankMatValues[i] = 2 * melBankMatValues[i];
        }
    }

    private double scaleFrequency(double freq)
    {
        switch(freqScale)
        {
            case SCALE_MEL:
                return fhz2mel(freq);
            case SCALE_ERB:
                return fhz2erb(freq);
            default:
                return freq;    // No conversion - should not happen
        }
    }

    public enum WindowMode
    {
        WIN_HAMMING,
        WIN_TRIANG
    }

    public enum FreqScale
    {
        SCALE_MEL,
        SCALE_ERB
    }

}
