package com.telhai.spl.crydetector;

/**
 * Main feature extractor class.
 * Features will be added as needed, depending on the algorithm.
 * Created by Dima Ruinskiy on 10/26/15.
 */
public class FeatureExtractor
{

    private IScalarFeature[] mScalarFeatures;
    private IVectorFeature[] mVectorFeatures;
    private TrainingSet.SpecificTrainingSet mSetSpecifier;

    public FeatureExtractor(TrainingSet.SpecificTrainingSet setSpecifier)
    {
        mSetSpecifier = setSpecifier;
        TrainingSet.FeatureSet featureSet = TrainingSet.FeatureSet(mSetSpecifier);
        mScalarFeatures = featureSet.mScalarFeatures;
        mVectorFeatures = featureSet.mVectorFeatures;
    }

    public double[] ProcessAudioBuffer(AudioFrameHandle handle)
    {

        double[] scalarFeatureValues = new double[mScalarFeatures.length];
        double[][] vectorFeatureValues = new double[mVectorFeatures.length][];
        int i;

        for(i = 0; i < mScalarFeatures.length; i++)
        {
            scalarFeatureValues[i] = mScalarFeatures[i].Compute(handle);
        }

        for(i = 0; i < mVectorFeatures.length; i++)
        {
            vectorFeatureValues[i] = mVectorFeatures[i].Compute(handle);
        }

        return aggregateFeaturesPolicy(scalarFeatureValues, vectorFeatureValues);
    }

    private double[] aggregateFeaturesPolicy(double[] scalarFeatures, double[][] vectorFeatures)
    {
        switch(mSetSpecifier)
        {
            case SET_1607:
                return aggregateFeatures1607Policy(scalarFeatures, vectorFeatures);
            case SET_1709_11K:
                return aggregateFeatures1709Policy(scalarFeatures, vectorFeatures);
            default:
                return aggregateFeaturesDefaultPolicy(scalarFeatures, vectorFeatures);
        }
    }

    /* Aggregates all features into a single array - scalar features first, followed by vector features.
     * Elegant implementation courtesy of G.S.
     */
    private double[] aggregateFeaturesDefaultPolicy(double[] scalarFeatures, double[][] vectorFeatures)
    {
        int totalLength = scalarFeatures.length;

        for(double[] array : vectorFeatures)
        {
            totalLength += array.length;
        }

        double[] featureVector = new double[totalLength];
        int currentPos = 0;
        System.arraycopy(scalarFeatures, 0, featureVector, currentPos, scalarFeatures.length);
        currentPos += scalarFeatures.length;

        for(double[] array : vectorFeatures)
        {
            System.arraycopy(array, 0, featureVector, currentPos, array.length);
            currentPos += array.length;
        }

        return featureVector;
    }

    /* Aggregates all features into a single array according to the order used in the MATLAB
     * algorithm from Infant_Cry_Detection_160719.m
     */
    private double[] aggregateFeatures1607Policy(double[] scalarFeatures, double[][] vectorFeatures)
    {
        double[] featureVector = new double[45];

        System.arraycopy(vectorFeatures[0], 0, featureVector, 0, 38);   // MFCC
        featureVector[38] = scalarFeatures[0];                          // Energy
        featureVector[39] = scalarFeatures[1];                          // ZCR
        featureVector[40] = vectorFeatures[1][0];                       // Pitch
        featureVector[41] = vectorFeatures[1][3];                       // Harmonicity
        featureVector[42] = vectorFeatures[1][5];                       // PHPR
        featureVector[43] = scalarFeatures[2];                          // First formant
        featureVector[44] = vectorFeatures[1][4];                       // nHarmonics

        return featureVector;
    }

    /* Aggregates all features into a single array according to the order used in the MATLAB
     * algorithm from Infant_Cry_Detection_170728_11025.m
     */
    private double[] aggregateFeatures1709Policy(double[] scalarFeatures, double[][] vectorFeatures)
    {
        double[] featureVector = new double[50];

        System.arraycopy(vectorFeatures[0], 0, featureVector, 0, 40);   // MFCC
        featureVector[40] = scalarFeatures[0];                          // Energy
        featureVector[41] = scalarFeatures[1];                          // ZCR
        featureVector[42] = vectorFeatures[1][0];                       // Pitch
        featureVector[43] = vectorFeatures[1][1];                       // Pitch run-length
        featureVector[44] = vectorFeatures[1][3];                       // Harmonicity
        featureVector[45] = vectorFeatures[1][5];                       // PHPR
        featureVector[46] = scalarFeatures[2];                          // First formant
        featureVector[47] = vectorFeatures[1][4];                       // nHarmonics
        featureVector[48] = scalarFeatures[3];                          // Band Energy Ratio
        featureVector[49] = scalarFeatures[4];                          // Spectrum Rolloff

        return featureVector;
    }
}
