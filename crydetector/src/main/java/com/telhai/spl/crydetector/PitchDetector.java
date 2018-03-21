package com.telhai.spl.crydetector;

import org.jtransforms.fft.DoubleFFT_1D;

import java.util.ArrayList;

/**
 * Created by Dima Ruinskiy on 08/09/16.
 * <p/>
 * This class implements the Pitch detector algorithm as given in PITCHD.M from the MATLAB
 * infant cry voice analysis kit.
 * <p/>
 * The detection uses real cepstrum or pitch period of previous segment for rough estimate
 * of the pitch, and the maximum of the cross-correlation for final pitch value. If the
 * maximum cross-correlation is below the threshold, the frame is considered unvoiced (no pitch).
 */
public class PitchDetector
{

    private int samplingFrequency;  // Sampling rate of the audio data
    private int frameLength;        // Length of frame for initial pitch detection
    private int frameLengthPadded;  // Padded to nearest power-of-2 for FFT-based real cepstrum
    private double threshold;       // Cross-correlation threshold to consider the pitch valid
    private int lowerBound;         // Lower bound for pitch period search
    private int upperBound;         // Upper bound for pitch period search
    private DoubleFFT_1D fftHelper;
    public PitchDetector(int samplingFrequency, double frameLengthMs, double threshold)
    {
        this.samplingFrequency = samplingFrequency;

        if(threshold > 0 && threshold <= 1)
        {
            this.threshold = threshold;
        } else
        {
            this.threshold = 0.85;  // Default x-corr threshold for valid pitch (voiced frame)
        }

        /* Convert frame length from milliseconds to samples, and pad to nearest power of 2
         * for FFT / real cepstrum computation.
         * The lower and upper bound of pitch period search correspond to frequencies of 530Hz
         * and 830Hz, which is the range in which we expect to find the pitch in a baby's cry.
         */
        frameLength = (int) Math.floor(frameLengthMs * samplingFrequency / 1000.0);
        frameLengthPadded = Math.max(512, (1 << ((int) Math.ceil(Math.log(frameLength) / Math.log(2)))));
        lowerBound = (int) Math.floor(samplingFrequency / 600.0);
        upperBound = (int) Math.floor(samplingFrequency / 300.0);

        fftHelper = new DoubleFFT_1D(frameLengthPadded);
    }

    /* This is the main function which computes the pitch information of the audio signal.
     * The pitch information is returned in the PitchData structure, which contains the pitch
     * values, the corresponding maximum cross-correlation values, and the offsets of the frames
     * where said pitch values were found.
     */
    public PitchData ComputePitchVector(double[] audioData)
    {

        /* Use ArrayLists to store the value initially, so we can add to the end;
         * then convert to static arrays at the end of the function.
         */
        ArrayList<Integer> pp = new ArrayList<Integer>();
        ArrayList<Double> cc = new ArrayList<Double>();
        ArrayList<Integer> oo = new ArrayList<Integer>();
        int length = 0; // Length of pitch vectors

        boolean lastVoiced = false;     // Whether previous frame was voiced
        boolean cepstrumUsed = false;   // Whether cepstrum was used on this frame

        int i;

        double cepstrumPeak;    // Cepstrum peak
        int pitchEstimate;      // Cepstrum peak location (corresponds to initial pitch guess)

        double correlation, maxCorrelation; // Cross-correlation used to fine-tune the pitch
        double amplitude, maxAmplitude;     // Frame start offsets determined by maximum amplitude

        int startOffset = 0;
        int endOffset = startOffset + frameLength;

        int maxSearchStart, maxSearchEnd, peakOffset;   // Boundaries for frame maximum search

        int pmin, pmax, pitch;  // Boundaries for pitch refinement

        double[] frameCepstrum = new double[2 * frameLengthPadded];   // Padded to power-of-2 for cepstrum
        // Doubled for complex array
        while(endOffset <= audioData.length)
        {
            maxCorrelation = -1.0;

            if(!lastVoiced)
            {  // Previous frame was unvoiced - use cepstrum for initial pitch guess
                for(i = 0; i < frameLength; i++)
                {
                    frameCepstrum[i] = audioData[startOffset + i];  // Copy frameLength of audio
                }
                for(i = frameLength; i < frameCepstrum.length; i++)
                {
                    frameCepstrum[i] = 0;                           // Zero-pad to full length
                }
                if(RealCepstrum(frameCepstrum))
                {  // returns false if cepstrum cannot be computed
                    cepstrumUsed = true;
                    pitchEstimate = lowerBound;
                    cepstrumPeak = frameCepstrum[2 * lowerBound];     // Real part of bin i is index 2*i
                    for(i = lowerBound; i <= upperBound; i++)
                    {
                        if(frameCepstrum[2 * i] > cepstrumPeak)
                        {
                            cepstrumPeak = frameCepstrum[2 * i];      // Find cepstrum peak in relevant area
                            pitchEstimate = i;
                        }
                    }
                } else
                {        // Cepstrum failed, no previous voiced frame - assume garbage
                    pp.add(0);
                    cc.add(maxCorrelation);
                    oo.add(startOffset);    // Do not look for maximum, just store starting offset
                    lastVoiced = false;
                    startOffset += Math.floor(samplingFrequency / 100.0);   // Advance 10ms
                    endOffset = startOffset + frameLength;
                    ++length;
                    continue;
                }
            } else
            {    // Previous frame was voiced - used last pitch value for initial guess
                cepstrumUsed = false;
                pitchEstimate = pp.get(length - 1);
            }

            // Increment pitch estimate by 1 if cepstrum was used
            pitch = (cepstrumUsed) ? ++pitchEstimate : pitchEstimate;

            /* This loop searches for the pitch within the vicinity of the initial pitch estimate.
             * The pitch is defined as the period between 0.8*EST and 1.2*EST, which gives the
             * maximum cross-correlation (assuming it is above the threshold).
             *
             * If the initial pitch estimated was done via real cepstrum, it is possible that the
             * maximum would be centered around the double pitch. In this case, once the pitch
             * is found, we repeat the process using half the pitch as the new estimate.
             */
            do
            {
                pmin = (int) Math.floor(0.8 * pitchEstimate);
                pmax = (int) Math.ceil(1.2 * pitchEstimate);

                for(i = pmin; i <= pmax; i++)
                {
                    if(startOffset + 2 * i > audioData.length) // Stop if we go out of bounds
                    {
                        break;
                    }
                    /* Compute cross-correlation of two segments of length i */
                    correlation = CrossCorrelation(audioData, startOffset, startOffset + i, i);
                    if(correlation > maxCorrelation)
                    {
                        maxCorrelation = correlation;
                        pitch = i;
                    }
                }

                pitchEstimate = pitch / 2;
                cepstrumUsed = !cepstrumUsed;
                /* If cepstrum was used, now cepstrumUsed=FALSE, and so !cepstrumUsed=TRUE and
                 * we try again. The next time cepstrumUsed will be set to TRUE, and we will exit
                 * the loop. This logic guarantees the loop will run only once OR twice, depending
                 * on the initial value of cepstrumUsed.
                 *
                 * However, if the maximum correlation is under the threshold, then whatever we
                 * found is not assumed to be a pitch period anyways, so there is no need to run
                 * the loop again.
                 */
            } while(!cepstrumUsed && maxCorrelation >= threshold);

            cc.add(maxCorrelation); // Store maximum correlation

            /* The pitch is considered valid if the maximum correlation is over the threshold,
             * and the pitch period is in the expected range for infant cry.
             *
             * If it is valid, we also want to search for the main excitation of the voiced frame.
             * This is likely to correspond to a local maximum of the audio waveform.
             * If the previous frame was voiced, we expect to be already locked on the main
             * excitation, and only search within 10% before and after the frame start.
             * If the previous frame was unvoiced, we search over the entire pitch period.
             */
            if(maxCorrelation >= threshold && IsPitchInExpectedRange(pitch))
            { // Valid pitch
                pp.add(pitch);  // Store pitch
                maxSearchStart = startOffset - (int) Math.floor(0.1 * pitch);
                if(maxSearchStart < 0)
                {
                    maxSearchStart = 0;
                }
                if(lastVoiced)
                {   // Determine search end boundaries
                    maxSearchEnd = maxSearchStart + (int) Math.floor(0.2 * pitch);
                } else
                {
                    maxSearchEnd = maxSearchStart + pitch;
                }
                maxAmplitude = audioData[maxSearchStart];
                peakOffset = maxSearchStart;
                for(i = maxSearchStart; i < maxSearchEnd; i++)
                {
                    amplitude = audioData[i];
                    if(amplitude > maxAmplitude)
                    {
                        maxAmplitude = amplitude;
                        peakOffset = i;
                    }
                }
                oo.add(peakOffset); // Store frame maximum
                lastVoiced = true;  // Mark frame as voiced
                startOffset = peakOffset + pitch;   // Advance one pitch period
                endOffset = startOffset + 4 * pitch;  // Take 4 pitch periods for the next frame
            } else
            {        // Invalid pitch - mark frame as unvoiced
                pp.add(0);
                lastVoiced = false;
                oo.add(startOffset);    // Do not look for maximum, just store starting offset
                startOffset += Math.floor(samplingFrequency / 100.0);   // Advance 10ms
                endOffset = startOffset + frameLength;
            }

            ++length;
        }

        /* Pitch analysis complete - convert ArrayLists to static arrays and return PitchData */
        double[] p = new double[length];
        double[] c = new double[length];
        int[] o = new int[length];

        for(i = 0; i < length; i++)
        {
            p[i] = pp.get(i).doubleValue();
            c[i] = cc.get(i);
            o[i] = oo.get(i);
        }

        return new PitchData(length, p, c, o);
    }

    /* Implement real cepstrum (MATLAB's RCEPS.M) as real(ifft(log(abs(fft(x))))
     * The transform is in-place (input sequence is replaced). The input X is assumed to be real.
     * The implementation uses the JTransforms FFT which produces an interleaved (real/imaginary)
     * complex array.
     */
    private boolean RealCepstrum(double[] x)
    {
        fftHelper.realForwardFull(x);

        for(int i = 0; i < frameLengthPadded; i++)
        {
            x[i] = Math.sqrt(x[2 * i] * x[2 * i] + x[2 * i + 1] * x[2 * i + 1]);    // Absolute value
            if(x[i] == 0)
            {
                return false;   // Zeros in DFT, cannot compute cepstrum
            }
            x[i] = Math.log(x[i]);  // Log(x) must be defined
        }

        fftHelper.realInverseFull(x, true);

        return true;
    }

    /* Cross-correlation computation between two segments of the same array.
     *
     * Computes the cross correlation computation between X1 = array[index1..index1+length-1]
     * and X2 = array[index2..index2+length-1]. The cross correlation is defined as:
     * XCORR = X1-dot-X2 / SQRT ( X1-dot-X1 times X2-dot-X2), where -dot- is the scalar product.
     */
    private double CrossCorrelation(double[] array, int index1, int index2, int length)
    {
        double x1x1 = 0, x2x2 = 0, x1x2 = 0;
        double x1, x2;

        for(int i = 0; i < length; i++)
        {
            x1 = array[index1 + i];
            x2 = array[index2 + i];
            x1x1 += x1 * x1;
            x2x2 += x2 * x2;
            x1x2 += x1 * x2;
        }

        return (x1x2) / Math.sqrt(x1x1 * x2x2);
    }

    private boolean IsPitchInExpectedRange(int pitch)
    {
        return (pitch <= upperBound + 1 && pitch >= lowerBound + 1);
    }

    /* This inner class is used to pass pitch data vectors back to the caller.
     *
     * The pitch data consists of three vectors: the pitch period (0 for unvoiced frames),
     * the maximum correlation found in each frame, and the offset for the global maximum in the
     * corresponding frame (which in voiced frames correlates with the main excitation in the
     * audio signal).
     */
    public class PitchData
    {
        public double[] pitchPeriod;       // Pitch period
        public double[] maxCor;         // Maximum correlation
        public int[] periodOffsets;     // Frame offset
        public int length;              // Length of each vector

        public PitchData(int l, double[] p, double[] c, int[] o)
        {
            length = l;
            pitchPeriod = p;
            maxCor = c;
            periodOffsets = o;
        }
    }
}
