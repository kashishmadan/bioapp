package com.telhai.spl.crydetector;

import java.util.PriorityQueue;

/**
 * Created by Dima Ruinskiy on 25/09/16.
 * <p>
 * Computes the following pitch-related parameters:
 * vector[0] - median pitch after removing outliers
 * vector[1] - pitch run length
 * vector[2] - median maximum correlation
 * vector[3] - Harmonicity - deviations of main frequency peaks from harmonics (pitch multiples)
 * vector[4] - Number of valid harmonics (including the pitch itself)
 * vector[5] - PHPR (Peak to Harmonic Power Ratio)
 */

public class PitchParametersVectorFeature implements IVectorFeature
{
    static final double WINDOW_LENGTH_MS = 16.0;
    static final int NUM_FFT_PEAKS = 5;
    static final int NUM_HARMONICS = 6;
    static final double EPSILON = 2.22044604925031e-16;
    static final double DEFAULT_PHPR = -2817.64075941486;

    /* Returns the 3-point running median filter (MATLAB's medfilt1(x,3)) */
    public static double[] MedianFilter3Pt(double[] x)
    {
        double[] y;

        if(x.length < 2)
        {
            y = x;
        } else
        {
            y = new double[x.length];
            int n = x.length - 1;
            y[0] = (x[0] < x[1]) ? (x[0] > 0) ? x[0] : (x[1] < 0) ? x[1] : 0 : (x[1] > 0) ? x[1] : (x[0] < 0) ? x[0] : 0;
            y[n] = (x[n] < x[n - 1]) ? (x[n] > 0) ? x[n] : (x[n - 1] < 0) ? x[n - 1] : 0 : (x[n - 1] > 0) ? x[n - 1] : (x[n] <
                    0) ? x[n] : 0;
            for(int i = 1; i < n; i++)
            {
                y[i] = (x[i] < x[i - 1]) ? (x[i] > x[i + 1]) ? x[i] : (x[i - 1] < x[i + 1]) ? x[i - 1] : x[i + 1] : (x[i - 1] >
                        x[i + 1]) ? x[i - 1] : (x[i] < x[i + 1]) ? x[i] : x[i + 1];
            }
        }

        return y;
    }

    /* Returns the nPeaks tallest peaks (local maxima) in array */
    public static IndexValue[] GetTallestPeaks(double[] array, int nPeaks)
    {
        PriorityQueue<IndexValue> queue = new PriorityQueue<>(nPeaks);

        int peaksFound = 0;
        IndexValue currentMin;

        for(int i = 1; i < array.length - 1; i++)
        {
            if(array[i] > array[i - 1] && array[i] > array[i + 1])
            { //  Local maximum found
                ++peaksFound;
                if(peaksFound <= nPeaks)
                { // Queue is not yet full - add unconditionally
                    queue.add(new IndexValue(i, array[i]));
                } else
                {
                    if((currentMin = queue.peek()).value < array[i])
                    { // Test against current minimum
                        queue.remove(currentMin);               // If larger - remove old minimum
                        queue.add(new IndexValue(i, array[i])); // Insert current element
                    }
                }
            }
        }

        /* Transfer priority queue to array by iteratively peeking at the first (minimum) element
         * and removing it from the queue. The array is built from end to start so that the maximum
         * element ends up first. Note that we do not use the built-in toArray() of PriorityQueue,
         * since that one does not guarantee order of elements.
         */
        IndexValue[] tallestPeaks = new IndexValue[queue.size()];
        for(int i = tallestPeaks.length; i > 0; )
        {
            tallestPeaks[--i] = queue.peek();
            queue.remove(tallestPeaks[i]);
        }

        return tallestPeaks;
    }

    /* Returns the harmonic peaks (frequency and magnitude pairs)
     * The implementation matches PEAKHSEARCH.M from the Baby Cry Analysis toolbox.
     * The function goes over the peaks of the FFT magnitude, and finds the closest peaks to
     * the pitch and it's first N harmonics. If the closest peaks fall within a predefined delta
     * range, they are considered valid and stored for later use. The range is 2.5*DELTA for the
     * pitch and 2.5*DELTA*(1.1)^N for the harmonics (starting from N=2).
     */
    public static FreqMagnitude[] GetHarmonicPeaks(double[] fftMagnitude, double delta, double scale, double pitch)
    {
        FreqMagnitude[] freqmags = new FreqMagnitude[NUM_HARMONICS];

        if(pitch != 0)
        {   // Pitch valid
            double freq, lastfreq = 0, peak, lastpeak = 0;  // Frequency (in Hz), Peak value
            double freqdiff, lastdiff = PrecomputedFilters.sampleRate;  // Frequency differences
            double effectiveDelta = delta * 2.5;                        // Range for peak search
            double harmonic = pitch;        // Harmonic frequencies (multiples of pitch)
            int harmonicIndex = 0;

            for(int i = 1; i < fftMagnitude.length - 1; i++)
            {
                if(fftMagnitude[i] > fftMagnitude[i - 1] && fftMagnitude[i] > fftMagnitude[i + 1])
                { //  Local maximum found
                    freq = i * scale;       // Convert FFT bin to Hz
                    peak = fftMagnitude[i];

                    freqdiff = Math.abs(freq - harmonic);   // Distance from pitch/harmonic
                    if(freqdiff < lastdiff)
                    {  // Still moving closer to harmonic
                        lastdiff = freqdiff;
                        lastfreq = freq;
                        lastpeak = peak;
                    } else
                    {    // Passed the harmonic frequency - look at last frequency/peak
                        if(lastdiff < effectiveDelta)
                        {    // Within range
                            freqmags[harmonicIndex] = new FreqMagnitude(lastfreq, lastpeak);
                        } else
                        {    // Outside of range - leave at 0 (redundant)
                            freqmags[harmonicIndex] = new FreqMagnitude(0.0, EPSILON);
                        }

                        ++harmonicIndex;    // next harmonic

                        if(harmonicIndex == NUM_HARMONICS)
                        {   // stop if reached maximum
                            break;
                        } else if(harmonicIndex == 1)
                        {    // An extra factor of 1.1
                            pitch = freqmags[0].freq;
                            harmonic = pitch;
                            effectiveDelta *= 1.1;
                        }

                        effectiveDelta *= 1.1;
                        harmonic += pitch;

                        lastdiff = PrecomputedFilters.sampleRate;   // Reset difference
                    }
                }
            }
        } else
        {    // Pitch not valid
            for(int i = 0; i < NUM_HARMONICS; i++)
            {
                freqmags[i] = new FreqMagnitude(0.0, EPSILON);
            }
        }

        return freqmags;
    }

    /* Returns the PHPR - from MATLABs peak2HhPr.m */
    public static double PeakHarmonicPowerRatio(double[] fftMagnitude, FreqMagnitude[] freqPeaks, double scale)
    {
        int M = 2 * fftMagnitude.length;

        double Py = fftMagnitude[0] * fftMagnitude[0];
        for(int i = 1; i < fftMagnitude.length; i++)
        {
            Py += 2 * fftMagnitude[i] * fftMagnitude[i];
        }
        Py = Py / M / scale / scale;

        double phpr = 10 * Math.log10(freqPeaks[0].mag / Py / scale);
        for(int i = 1; i < freqPeaks.length; i++)
        {
            phpr += 10 * Math.log10(((freqPeaks[i].mag * freqPeaks[i].mag) / M / scale / scale) / Py);
        }

        return phpr;
    }

    public double[] Compute(AudioFrameHandle handle)
    {
        PitchDetector.PitchData pitchData = handle.GetPitch();  // Compute pitch over entire frame

        double[] outputVector = new double[]{0, 0, 0, 0, 0, 0};  // Outputs default to 0 (invalid)

        double pitch = 0, pitchRunLength = 0, maxCor = 0;

        double thr = (PrecomputedFilters.sampleRate == 11025) ? 0.75 : 0.85;

        int frameLength = handle.FrameLength();
        int windowLength = (int) Math.floor(PrecomputedFilters.sampleRate * WINDOW_LENGTH_MS / 1000);
        int stepLength = windowLength / 2;  // Round down

        double freqScaleFactor = (double) PrecomputedFilters.sampleRate / handle.FftLength();

        int nWindows = 2 * (frameLength / windowLength);    // Number of windows given step size
        double[] pitchw = new double[nWindows];             // One pitch sample per window
        double[] corrw = new double[nWindows];

        /* Process window-by-window */
        for(int i = 0; i < nWindows; i++)
        {
            int firstOffset = -1, lastOffset = pitchData.length;
            for(int j = 0; j < pitchData.length; j++)
            {    // find all offsets within the current window
                if(pitchData.periodOffsets[j] >= i * stepLength && firstOffset == -1)
                {
                    firstOffset = j;
                }
                if(pitchData.periodOffsets[j] > i * stepLength + windowLength)
                {    // As soon as we found the last offset - stop
                    lastOffset = j;
                    break;
                }
            }
            if(firstOffset == -1)
            {    // No pitch samples in this window - can stop looking
                break;
            }
            double[] pitchVector =
                    AuxMathFuncs.RemoveOutliers(pitchData.pitchPeriod, firstOffset, lastOffset, 0.5);    // Pitch vector without outliers
            double[] corrVector = new double[lastOffset - firstOffset];     // Maximum correlation vector within the window
            for(int j = firstOffset; j < lastOffset; j++)
            {
                corrVector[j - firstOffset] = pitchData.maxCor[j];
            }

            double meanc = AuxMathFuncs.Mean(corrVector);
            double stdc = Math.sqrt(AuxMathFuncs.VarianceFromMean(corrVector, meanc));

            if(meanc > thr && stdc < 0.3)
            {   // Pitch is considered valid
                pitchw[i] = AuxMathFuncs.Median(pitchVector);
                if(pitchw[i] != 0)
                {
                    pitchw[i] = PrecomputedFilters.sampleRate / pitchw[i];
                }
                corrw[i] = AuxMathFuncs.Median(corrVector);
            }
        }

        pitchw = MedianFilter3Pt(pitchw);
        pitch = AuxMathFuncs.Median(pitchw);
        pitchRunLength = AuxMathFuncs.MaxPositiveRunLength(pitchw);
        maxCor = AuxMathFuncs.Median(corrw);

        double[] fftMagnitude = handle.GetFFTMagnitude();

        IndexValue[] fftPeaks = GetTallestPeaks(fftMagnitude, NUM_FFT_PEAKS);

        double[] freqPeaks = new double[fftPeaks.length];
        double sumDeviations = 0;

        for(int i = 0; i < freqPeaks.length; i++)
        {
            freqPeaks[i] = fftPeaks[i].index * freqScaleFactor;
            if(pitch != 0)
            {
                freqPeaks[i] %= pitch;
            }
            if(freqPeaks[i] > pitch / 2)
            {
                freqPeaks[i] = pitch - freqPeaks[i];
            }
            sumDeviations += freqPeaks[i];
        }

        FreqMagnitude[] freqMag = GetHarmonicPeaks(fftMagnitude, freqScaleFactor, freqScaleFactor, pitch);

        double phpr = DEFAULT_PHPR;
        int nHarmonics = 0;
        for(int i = 0; i < freqMag.length; i++)
        {  // Number of valid harmonics
            if(freqMag[i].freq > 0)
            {
                ++nHarmonics;
            }
        }

        /* Only compute PHPR if pitch is valid and there at least 2 additional harmonics */
        if(freqMag[0].freq > 0 && nHarmonics > 2)
        {
            double[] fftMagnitudeWithoutOutliers = AuxMathFuncs.RemoveOutliers(fftMagnitude, 1, false, false);
            phpr = PeakHarmonicPowerRatio(fftMagnitudeWithoutOutliers, freqMag, 0.5 * handle.FftLength());
        }

        outputVector[0] = pitch;
        outputVector[1] = pitchRunLength;
        outputVector[2] = maxCor;
        outputVector[3] = (freqPeaks.length > 0) ? sumDeviations / freqPeaks.length : 0.0;
        outputVector[4] = nHarmonics;
        outputVector[5] = phpr;

        return outputVector;
    }

    /* An (index,value) pair which can be compared by the value */
    public static class IndexValue implements Comparable<IndexValue>
    {
        public int index;
        public double value;

        public IndexValue(int i, double v)
        {
            index = i;
            value = v;
        }

        public int compareTo(IndexValue other)
        {
            if(value < other.value)
            {
                return -1;
            } else if(value > other.value)
            {
                return +1;
            }
            return 0;
        }
    }

    /* A (frequency,magnitude) pair */
    public static class FreqMagnitude
    {
        public double freq;
        public double mag;

        public FreqMagnitude(double f, double m)
        {
            freq = f;
            mag = m;
        }
    }
}
