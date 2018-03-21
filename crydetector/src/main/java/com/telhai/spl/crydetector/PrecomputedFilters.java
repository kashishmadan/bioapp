package com.telhai.spl.crydetector;

import org.jtransforms.fft.DoubleFFT_1D;

import java.util.InputMismatchException;

/**
 * Created by Dima Ruinskiy on 18/08/16.
 * <p>
 * The precomputed filters class is used to initializes common audio-processing primitives such as:
 * FFT, MFCC, Pitch detector, and filter-by-FFT. It also precomputers a set of pre-defined filters,
 * including: downsampling low-pass FIR filter, standard low-pass FIR filter, and Hamming windows
 * of necessary sizes.
 * <p>
 * This class is static and can be used by AudioFrameHandle to gain access to the needed processing
 * primitives without constructing them for each individual frame.
 */
public final class PrecomputedFilters
{

    public static final double[] Hamming1K = new double[1024];
    public static final double[] Hamming4K = new double[4096];
    private static final double[] DownsampleFilter = new double[]{
            0,
            0,
            0,
            0,
            0,
            -0.000282649394796115,
            -0.000525791965429469,
            -0.000475416983723193,
            0,
            0.000731628527240472,
            0.00125463273086795,
            0.00106307976671378,
            0,
            -0.00148301296094593,
            -0.00244770849279972,
            -0.00200653136531637,
            0,
            0.00265127251920136,
            0.00427878875724403,
            0.00343848976765794,
            0,
            -0.00439479131278936,
            -0.00699581925273953,
            -0.00555497139192494,
            0,
            0.00696672908988022,
            0.0110128710249591,
            0.00869881368759028,
            0,
            -0.0108557716105287,
            -0.0171721337464203,
            -0.0136063727671956,
            0,
            0.0172430846995437,
            0.0276474325182314,
            0.0223208371330115,
            0,
            -0.0300335875386367,
            -0.0504711057057638,
            -0.0434944357428851,
            0,
            0.0741357049010938,
            0.158369021719968,
            0.224908142752552,
            0.250159141272278,
            0.224908142752552,
            0.158369021719968,
            0.0741357049010938,
            0,
            -0.0434944357428851,
            -0.0504711057057638,
            -0.0300335875386367,
            0,
            0.0223208371330115,
            0.0276474325182314,
            0.0172430846995437,
            0,
            -0.0136063727671956,
            -0.0171721337464203,
            -0.0108557716105287,
            0,
            0.00869881368759028,
            0.0110128710249591,
            0.00696672908988022,
            0,
            -0.00555497139192494,
            -0.00699581925273953,
            -0.00439479131278936,
            0,
            0.00343848976765794,
            0.00427878875724403,
            0.00265127251920136,
            0,
            -0.00200653136531637,
            -0.00244770849279972,
            -0.00148301296094593,
            0,
            0.00106307976671378,
            0.00125463273086795,
            0.000731628527240472,
            0,
            -0.000475416983723193,
            -0.000525791965429469,
            -0.000282649394796115,
            0};
    public static int sampleRate, frameLength, fftLength, filterFftLength, nMelBands, nMFCC, lpcOrder;
    public static DoubleFFT_1D fftComputer;
    public static DoubleFFT_1D filterFftComputer;
    public static MFCC2 mfccComputer;
    public static PitchDetector pitchComputer;
    public static double[] Hamming;
    private static boolean isInitialized = false;
    private static double[] DownsampleFilterFFT;
    private static double[] LowpassFilter44K = new double[]{
            0.000509052345140714,
            0.000371467877137805,
            0,
            -0.000421930966377862,
            -0.000653272863359662,
            -0.000512505037477536,
            0,
            0.000647601620055325,
            0.00103703452274013,
            0.000831895989141231,
            0,
            -0.00107042530175746,
            -0.0017136513040701,
            -0.00136872848802055,
            0,
            0.00173304389693948,
            0.00274663379691676,
            0.00217058829095879,
            0,
            -0.00268995280176046,
            -0.00421932472011522,
            -0.00330167095701468,
            0,
            0.00401904640600595,
            0.00625444668786646,
            0.00485938393057683,
            0,
            -0.00584586711159484,
            -0.00905585907055896,
            -0.0070105113702579,
            0,
            0.00839898208811935,
            0.013006613956334,
            0.0100788107604236,
            0,
            -0.0121541773423973,
            -0.018935270880269,
            -0.0147943263440062,
            0,
            0.0182929645236637,
            0.0290206865980503,
            0.0232068289971168,
            0,
            -0.0307317609809232,
            -0.0513133875678244,
            -0.0439812939684705,
            0,
            0.074379480369694,
            0.158501841074448,
            0.224767270549089,
            0.249880485591675,
            0.224767270549089,
            0.158501841074448,
            0.074379480369694,
            0,
            -0.0439812939684705,
            -0.0513133875678244,
            -0.0307317609809232,
            0,
            0.0232068289971168,
            0.0290206865980503,
            0.0182929645236637,
            0,
            -0.0147943263440062,
            -0.018935270880269,
            -0.0121541773423973,
            0,
            0.0100788107604236,
            0.013006613956334,
            0.00839898208811935,
            0,
            -0.0070105113702579,
            -0.00905585907055896,
            -0.00584586711159484,
            0,
            0.00485938393057683,
            0.00625444668786646,
            0.00401904640600595,
            0,
            -0.00330167095701468,
            -0.00421932472011522,
            -0.00268995280176046,
            0,
            0.00217058829095879,
            0.00274663379691676,
            0.00173304389693948,
            0,
            -0.00136872848802055,
            -0.0017136513040701,
            -0.00107042530175746,
            0,
            0.000831895989141231,
            0.00103703452274013,
            0.000647601620055325,
            0,
            -0.000512505037477536,
            -0.000653272863359662,
            -0.000421930966377862,
            0,
            0.000371467877137805,
            0.000509052345140714
    };
    private static double[] LowpassFilter11K = new double[]{
            0,
            0.000525321768513287,
            0,
            -0.000596685568496443,
            0,
            0.000724773444029965,
            0,
            -0.000915824084065622,
            0,
            0.00117644916056271,
            0,
            -0.00151377211109964,
            0,
            0.00193562596981901,
            0,
            -0.00245083287380369,
            0,
            0.0030695986110737,
            0,
            -0.00380407257264361,
            0,
            0.00466915104356242,
            0,
            -0.00568364775443765,
            0,
            0.00687203475025825,
            0,
            -0.00826709774535201,
            0,
            0.00991411224998107,
            0,
            -0.0118776572505769,
            0,
            0.0142532343145577,
            0,
            -0.0171881724619856,
            0,
            0.020921813586893,
            0,
            -0.0258695113800024,
            0,
            0.032818591325538,
            0,
            -0.0434601859854416,
            0,
            0.0621973865063129,
            0,
            -0.10518583859786,
            0,
            0.317860970855762,
            0.499748469597803,
            0.317860970855762,
            0,
            -0.10518583859786,
            0,
            0.0621973865063129,
            0,
            -0.0434601859854416,
            0,
            0.032818591325538,
            0,
            -0.0258695113800024,
            0,
            0.020921813586893,
            0,
            -0.0171881724619856,
            0,
            0.0142532343145577,
            0,
            -0.0118776572505769,
            0,
            0.00991411224998107,
            0,
            -0.00826709774535201,
            0,
            0.00687203475025825,
            0,
            -0.00568364775443765,
            0,
            0.00466915104356242,
            0,
            -0.00380407257264361,
            0,
            0.0030695986110737,
            0,
            -0.00245083287380369,
            0,
            0.00193562596981901,
            0,
            -0.00151377211109964,
            0,
            0.00117644916056271,
            0,
            -0.000915824084065622,
            0,
            0.000724773444029965,
            0,
            -0.000596685568496443,
            0,
            0.000525321768513287,
            0
    };
    private static double[] LowpassFilter;  // Selected at runtime between LowpassFilter11K and LowpassFilter44K
    private static double[] LowpassFilterFFT;
    private PrecomputedFilters()
    {
    }

    public static void Initialize(int _sampleRate, int _frameLength, int _fftLength, MFCC2 _mfccComputer, int _lpcOrder)
    {
        if(isInitialized)
        {
            return;
        }
        InitializeForced(_sampleRate, _frameLength, _fftLength, _mfccComputer, _lpcOrder);
    }

    public static void InitializeForced(int _sampleRate, int _frameLength, int _fftLength, MFCC2 _mfccComputer, int _lpcOrder)
    {
        if(_sampleRate != 44100 && _sampleRate != 11025)
        {
            throw new InputMismatchException("Supported sampling rates are 11025 and 44100 Hz only");
        }

        sampleRate = _sampleRate;
        frameLength = _frameLength;
        fftLength = _fftLength;
        filterFftLength = 2 * _fftLength;
        lpcOrder = _lpcOrder;

        mfccComputer = _mfccComputer;
        InitializeComponents();

        PrecomputeFilterFFTs();

        PrecomputeHammingWindows();

        isInitialized = true;
    }

    /* Initializes all components for internal and external use */
    private static void InitializeComponents()
    {

        /* initialize main processing classes for AudioFrameHandle usage */
        fftComputer = new DoubleFFT_1D(fftLength);
        pitchComputer = new PitchDetector(sampleRate, 16.0, 0.85);

        LowpassFilter = (sampleRate == 44100) ? LowpassFilter44K : LowpassFilter11K;

        /* initialize filtering-by-FFT class and data placeholders (with double the FFT size) */
        filterFftComputer = new DoubleFFT_1D(filterFftLength);
        DownsampleFilterFFT = new double[2 * filterFftLength];
        LowpassFilterFFT = new double[2 * filterFftLength];
    }

    /* Precompute FFT of lowpass filter which is later used for downsampling */
    private static void PrecomputeFilterFFTs()
    {
        if(isInitialized)    // Already configured
        {
            return;
        }

        for(int i = 0; i < DownsampleFilter.length; i++)
        {
            DownsampleFilterFFT[i] = DownsampleFilter[i];
        }
        filterFftComputer.realForwardFull(DownsampleFilterFFT);

        for(int i = 0; i < LowpassFilter.length; i++)
        {
            LowpassFilterFFT[i] = LowpassFilter[i];
        }
        filterFftComputer.realForwardFull(LowpassFilterFFT);
    }

    /* Precompute 1024-sample and 4096-sample Hamming windows, which are used in various stages of the algorithm */
    private static void PrecomputeHammingWindows()
    {
        for(int i = 0; i < 512; i++)     // 1024 samples
        {
            Hamming1K[i] = Hamming1K[1023 - i] = (0.54 - 0.46 * Math.cos(2 * Math.PI * i / 1023));
        }

        for(int i = 0; i < 2048; i++)    // 4096 samples
        {
            Hamming4K[i] = Hamming4K[4095 - i] = (0.54 - 0.46 * Math.cos(2 * Math.PI * i / 4095));
        }

        Hamming = (sampleRate == 44100) ? Hamming4K : Hamming1K; // Choose window based on sampling rate
    }

    /*
    * Auxiliary function - resample 44100Hz to 11025Hz as pre-processing for LPC analysis
    *
    * Filters with the appropriate pre-computed lowpass filter prior to downsampling.
    * For performance, filtering is done through FFT and not raw convolution.
    * The downsampling is performed by taking every 4th sample of the non-zero (initial) part of the output,
    * after rejecting the first few samples (accouting for filter delay).
    */
    public static double[] Downsample_44K_11K(double[] frame)
    {

        if(!isInitialized)
        {
            throw new RuntimeException("Precomputed filters not initialized");
        }

        int filterSize = DownsampleFilter.length;
        int fftSize = DownsampleFilterFFT.length / 2;
        int frameSize = frame.length;

        if(fftSize < frameSize + filterSize)
        {
            throw new InputMismatchException("Insufficient FFT size");
        }

        double[] realFFT = FilterByFFT(frame, DownsampleFilterFFT);

        int outLength = frameSize / 4;
        int start = filterSize / 8 + 1;     // samples to throw from the start
        int end = outLength + start;    // samples to throw away from the end
        double[] outFrame = new double[outLength];

        for(int i = start; i < end; i++)
        {
            outFrame[i - start] = realFFT[i * 4];
        }

        return outFrame;
    }

    /* Low-pass-filter the signal using the pre-computed 100-coefficient FIR low-pass filter
     * and the filter-by-FFT method.
     * The low-pass filter is designed to have a cut-off frequency of 0.25 Nyquist.
     * When 'inplace' is true, output overwrites input array; otherwise a new one is allocated.
     */
    public static double[] LowPass(double[] frame, boolean inplace)
    {
        if(!isInitialized)
        {
            throw new RuntimeException("Precomputed filters not initialized");
        }

        int filterSize = LowpassFilter.length;
        int fftSize = LowpassFilterFFT.length / 2;
        int frameSize = frame.length;

        if(fftSize < frameSize + filterSize)
        {
            throw new InputMismatchException("Insufficient FFT size");
        }

        double[] realFFT = FilterByFFT(frame, LowpassFilterFFT);

        double[] outFrame = (inplace) ? frame : new double[frameSize];

        for(int i = 0; i < frameSize; i++)
        {
            outFrame[i] = realFFT[i];
        }

        return outFrame;
    }

    /* Apply digital "pre-emphasis" filter:
     *  Y[i] = X[i] - 0.95*X[i-1]
     * Setting 'inplace' flag to TRUE will perform in-place filtering (modifying the input array without copying it over)
     */
    public static double[] PreEmphasis(double[] frame, boolean inplace)
    {
        double[] outFrame;

        outFrame = (inplace) ? frame : new double[frame.length];

        /* Filtering backwards spares us from having to store a temporary with the computation result,
         * in case in-place filtering is desired, and output overwrites input.
         */
        for(int i = frame.length - 1; i > 0; i--)
        {
            outFrame[i] = frame[i] - 0.95 * frame[i - 1];
        }
        outFrame[0] = frame[0];

        return outFrame;
    }

    /* Implementation of FIR filtering via FFT is as follows:
    * 1) Compute double-length FFT of input (double-length to avoid boundary mismatches)
    * 2) Multiply by (precomputed) double-length FFT of filter
    * 3) Compute inverse FFT
    */
    private static double[] FilterByFFT(double[] frame, double[] filterFFT)
    {
        int fftSize = filterFFT.length / 2;
        int frameSize = frame.length;

        if(fftSize < 2 * frameSize)  // We assume here that the filter is shorter than the frame and FFT size is a power of 2
        {
            throw new InputMismatchException("Insufficient FFT size");
        }

        /* Initialized to zero */
        double[] FFT = new double[2 * fftSize];

        for(int i = 0; i < frameSize; i++)
        {
            FFT[i] = frame[i];          // Copy frame over up to length
        }

        filterFftComputer.realForwardFull(FFT);  // Compute FFT

        /* Multiplication in frequency space as a subsititue to convolution (filtering) */
        double temp;
        for(int i = 0; i < fftSize; i++)
        {
            temp = FFT[2 * i] * filterFFT[2 * i] - FFT[2 * i + 1] * filterFFT[2 * i + 1];         // Real = Real*Real - Imag*Imag
            FFT[2 * i + 1] = FFT[2 * i] * filterFFT[2 * i + 1] + FFT[2 * i + 1] * filterFFT[2 * i];   // Imag = Real*Imag + Imag*Real
            FFT[2 * i] = temp;
        }

        filterFftComputer.complexInverse(FFT, true); // Filtered frame will be in realFFT; imagFFT will be ~0 since input is real

        for(int i = 0; i < fftSize; i++)
        {
            FFT[i] = FFT[2 * i];          // Squash real part to first half of array (second half will be ignored)
        }
        return FFT; // Return (real part of) output as is; caller will truncate it to desired frame size
    }
}
