package com.telhai.spl.crydetector;

import org.jtransforms.fft.DoubleFFT_1D;

import static com.telhai.spl.crydetector.AudioFrameHandle.FrameFilter.FRAME_LOWPASS;
import static com.telhai.spl.crydetector.AudioFrameHandle.FrameFilter.FRAME_PREEMPHASIS;
import static com.telhai.spl.crydetector.AudioFrameHandle.FrameFilter.FRAME_WINDOWED;

/**
 * Created by Dima Ruinskiy on 12/08/16.
 * <p>
 * This class provides a generic interface to an audio frame handle.
 * Feature computation classes should receive this handle instead of raw audio data. The handle will allow access to the raw
 * samples, as well as commonly used artifacts of audio, such as FFT, MFCC, LPC. This way, if multiple features rely on these
 * common artifacts, they only need to be computed once, and then passed through the frame handle.
 * <p>
 * The object follows a "lazy initialization" principle. Frame sample data is provided upon creation. FFT, IFFT, MFCC, LPC, etc.
 * are left uninitialized and are computed the first time they are requested.
 * <p>
 * The static objects and methods of the PrecomputedFilters class are used to compute the aforementioned artifacts.
 */
public class AudioFrameHandle
{

    private int frameLength = 0;
    private int fftLength = 0;
    private double[] frameData = null;
    private double[] frameDataLP = null;
    private double[] frameDataFPE = null;
    private double[] frameDataHamming = null;
    private double[] frameDataLPHamming = null;
    private double[] fft = null;
    private double[] fftHalfMag = null;
    private double[] fbankE = null;
    private double[] mfcc = null;
    private double[] lpc = null;
    private PitchDetector.PitchData pitch = null;
    private DoubleFFT_1D fftComputer;
    private MFCC2 mfccComputer;
    private PitchDetector pitchD;
    public AudioFrameHandle(double[] _frameData, int _frameLength)
    {
        if(_frameData.length != _frameLength)
        {
            throw new IllegalArgumentException("Frame length parameter does not match actual");
        }

        if(_frameLength != PrecomputedFilters.frameLength)
        {
            throw new RuntimeException("Frame length does not match standard length");
        }

        frameData = _frameData;
        frameLength = _frameLength;
        fftLength = (1 << ((int) Math.ceil(Math.log(frameLength) / Math.log(2))));   // FFT length - next power of 2 of frame length

        /* retrieve handles to processing classes */
        fftComputer = PrecomputedFilters.fftComputer;
        mfccComputer = PrecomputedFilters.mfccComputer;
        pitchD = PrecomputedFilters.pitchComputer;
    }

    /* Returns audio data as is */
    public double[] GetAudioFrame()
    {
        return GetAudioFrame(FrameFilter.FRAME_NORMAL);
    }

    /* Returns audio data in one of three modes:
     *
     * 1) As-is
     * 2) Lowpass-filtered (using precomputed filter)
     * 3) Lowpass-filtered and pre-emphasized (using precomputed filter)
     * 4) Windowed with Hamming window
     * 5) Lowpassed and Windowed with Hamming Window
     *
     * The first time a given mode is requested, it is computed and then stored for future queries.
     */
    public double[] GetAudioFrame(FrameFilter mode)
    {
        if(frameData == null)
        {
            throw new RuntimeException("Audio data is null");
        }

        switch(mode)
        {
            case FRAME_NORMAL:
                return frameData;
            case FRAME_LOWPASS:
                if(frameDataLP == null)
                {
                    frameDataLP = PrecomputedFilters.LowPass(frameData, false);
                }
                return frameDataLP;
            case FRAME_PREEMPHASIS:
                if(frameDataFPE == null)
                {
                    frameDataFPE = PrecomputedFilters.PreEmphasis(GetAudioFrame(FRAME_LOWPASS), false);
                }
                return frameDataFPE;
            case FRAME_WINDOWED:
                if(frameDataHamming == null)
                {
                    frameDataHamming = new double[frameLength];
                    for(int i = 0; i < frameLength; i++)
                    {
                        frameDataHamming[i] = frameData[i] * PrecomputedFilters.Hamming[i];
                    }
                    return frameDataHamming;
                }
            case FRAME_LOWPASS_WINDOWED:
                if(frameDataLPHamming == null)
                {
                    frameDataLPHamming = new double[frameLength];
                    GetAudioFrame(FRAME_LOWPASS);
                    for(int i = 0; i < frameLength; i++)
                    {
                        frameDataLPHamming[i] = frameDataLP[i] * PrecomputedFilters.Hamming[i];
                    }
                    return frameDataLPHamming;
                }
            default:    // should not happen
                return frameData;
        }
    }

    /* Return complex DFT in interleaved (real/imaginary) array */
    public double[] GetFFT()
    {
        if(fft == null)
        { // New arrays will be initialized to 0
            fft = new double[2 * fftLength];
            GetAudioFrame(FRAME_WINDOWED);  // Changes internal array so no need to return it
            for(int i = 0; i < frameLength; i++)
            {
                fft[i] = frameDataHamming[i];
            }
            fftComputer.realForwardFull(fft);
        }
        return fft;
    }

    public double[] GetFFTMagnitude()
    {
        if(fftHalfMag == null)
        {
            fftHalfMag = new double[fftLength / 2 + 1];
            GetFFT();  // This will compute FFT and store it in internal array

            for(int i = 0; i < fftHalfMag.length; i++)
            {
                fftHalfMag[i] = Math.sqrt(fft[2 * i] * fft[2 * i] + fft[2 * i + 1] * fft[2 * i + 1]);   // Compute magnitude
            }
        }
        return fftHalfMag;
    }

    /* Return frame inversed DFT (currently not implemented) */
    public double[] GetIFFT()
    {
        return null;
    }

    /* Return frame Filter Bank Energy (Mel or other) */
    public double[] GetFilterBankEnergy()
    {
        if(fbankE == null)
        {
            fbankE = mfccComputer.filterEn(GetFFTMagnitude());
        }
        return fbankE;
    }

    /* Return frame MFCC (Mel Frequency Cepstrum Coefficients */
    public double[] GetMFCC()
    {
        if(mfcc == null)
        {
            mfcc = mfccComputer.cepstrumE(GetFilterBankEnergy());
        }
        return mfcc;
    }

    /* Return frame LPC (Linear Predictor Coefficients) - computer after lowpass and pre-emphasis */
    public double[] GetLPC()
    {
        if(lpc == null)
        {
            double[] x = GetAudioFrame(FRAME_PREEMPHASIS);

            /* Downsample (44100Hz-->11025Hz) and apply Hamming window */
            if(PrecomputedFilters.sampleRate == 44100)
            {
                x = PrecomputedFilters.Downsample_44K_11K(x);
            } else if(PrecomputedFilters.sampleRate != 11025)
            {
                throw new RuntimeException("Currently supported sample rates are 44100 and 11025 only.");
            }
            for(int i = 0; i < x.length; i++)
            {
                x[i] *= PrecomputedFilters.Hamming1K[i];
            }

            lpc = LPC.lpc(x, PrecomputedFilters.lpcOrder);
        }

        return lpc;
    }

    /* Return frame pitch vector - computed after lowpass on the frame */
    public PitchDetector.PitchData GetPitch()
    {
        if(pitch == null)
        {
            pitch = pitchD.ComputePitchVector(GetAudioFrame(FRAME_LOWPASS));
        }

        return pitch;
    }

    public int FrameLength()
    {
        return frameLength;
    }

    public int FftLength()
    {
        return fftLength;
    }

    public enum FrameFilter
    {
        FRAME_NORMAL,
        FRAME_LOWPASS,
        FRAME_PREEMPHASIS,
        FRAME_WINDOWED,
        FRAME_LOWPASS_WINDOWED
    }
}
