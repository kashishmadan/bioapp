package com.telhai.spl.crydetector;

import java.util.InputMismatchException;

/**
 * Receives an audio frame and processes it.
 * Created by Dima Ruinskiy on 11/15/15.
 * <p/>
 * TODO: Refactor this and all child classes to use a single pre-allocated double[] for the feature vector,
 * instead of reallocating for every frame!
 * TODO: Use MasterAllocator class to allocate memory for ALL data and reuse for every frame
 */
public class FrameProcessor
{

    static final int CONST_DEFAULT_MELBANDS = 39;
    static final int CONST_DEFAULT_MFCC = 39;
    static final int CONST_DEFAULT_LPC_ORDER = 12;
    private int sampleRate;
    private int frameSize;
    private int fftSize;
    private TrainingSet trainingSet;
    private TrainingSet.SpecificTrainingSet setSpecifier;
    private double threshold;
    private double exponent;
    private FeatureExtractor featureExtractor;

    public FrameProcessor(int _sampleRate, int _frameSize, int _fftSize, TrainingSet.SpecificTrainingSet _setSpecifier)
    {
        sampleRate = _sampleRate;
        frameSize = _frameSize;
        fftSize = _fftSize;
        setSpecifier = _setSpecifier;

        if(frameSize > fftSize)
        {
            throw new InputMismatchException("FFT size cannot be smaller than frame size");
        }

        trainingSet = new TrainingSet(setSpecifier);
        MFCC2 mfcc = TrainingSet.getMFCCComputer(setSpecifier);
        featureExtractor = new FeatureExtractor(setSpecifier);
        PrecomputedFilters.Initialize(sampleRate, frameSize, fftSize, mfcc, CONST_DEFAULT_LPC_ORDER);

        switch(setSpecifier)
        {
            case SET_1607:
                threshold = 0.8;
                exponent = 1.5;
                break;
            case SET_1709_11K:
            default:
                threshold = 0.82;
                exponent = 2;
                break;
        }
    }

    /* Processes a frame. Returns decision (SAVE or DISCARD). */
    public FrameResult ProcessFrame(double[] frame)
    {

        if(frame.length != frameSize)
        {
            throw new InputMismatchException("Invalid frame size");
        }

        AudioFrameHandle handle = new AudioFrameHandle(frame, frameSize);

        double[] featureVector;

        featureVector = featureExtractor.ProcessAudioBuffer(handle); // Process buffer and extract features

        return LogisticRegressionClassifier(featureVector);
    }

    private FrameResult LogisticRegressionClassifier(double[] featureVector)
    {

        int len = featureVector.length;
        int len2 = 2 * len;
        double[] augmentedNormalizedVector = new double[len2];

        /* Normalize feature vector by range and offset by minimum value as seen in the training set.
         * Values <0 (outside of training set range) are truncated to 0 (to avoid complex roots in the next step)
         * Feature vector X is doubled in length to [X ; X√X] or [X ; X²] (depending on training set)
         */
        for(int i = 0; i < len; i++)
        {
            augmentedNormalizedVector[i] = (featureVector[i] - trainingSet.minValue[i]) / trainingSet.range[i];
            if(augmentedNormalizedVector[i] < 0)
            {
                augmentedNormalizedVector[i] = 0;
            }
            augmentedNormalizedVector[i + len] = Math.pow(augmentedNormalizedVector[i], exponent);
        }

        double scalarProduct = 0;

        for(int i = 0; i < len2; i++)
        {    // Scalar product between augmented feature vector and precomputed theta from training
            scalarProduct += augmentedNormalizedVector[i] * trainingSet.theta[i];
        }

        double sigmoidValue = 1 / (1 + Math.exp(-scalarProduct));   // Sigmoid value Z --> 1/(1+e^-Z)

        return (sigmoidValue >
                threshold) ? FrameResult.SAVE : FrameResult.DISCARD; // Return 'save' (detected) if higher than threshold ; 'discard' otherwise
    }
}
