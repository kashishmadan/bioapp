package com.telhai.spl.crydetector.test;

import org.junit.Test;

import java.io.DataInputStream;
import java.io.FileInputStream;

import com.telhai.spl.crydetector.AudioFrameHandle;
import com.telhai.spl.crydetector.FeatureExtractor;
import com.telhai.spl.crydetector.PrecomputedFilters;
import com.telhai.spl.crydetector.TrainingSet;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created by Dima Ruinskiy on 07/01/17.
 */
public class FeatureExtractorTest {
    @Test
    public void processAudioBuffer() throws Exception {
        String filePath = "app\\src\\test\\res\\FRAME_1890.DAT";
        double[] audioData = new double[4096];

        try {
            FileInputStream fid = new FileInputStream(filePath);
            DataInputStream data = new DataInputStream(fid);

            for (int i = 0; i < 4096; i++) {
                audioData[i] = data.readDouble();
            }

            PrecomputedFilters.Initialize(44100, 4096, 4096, TrainingSet.getMFCCComputer(TrainingSet.SpecificTrainingSet.SET_1607), 12);

            AudioFrameHandle handle = new AudioFrameHandle(audioData, 4096);

            FeatureExtractor extractor = new FeatureExtractor(TrainingSet.SpecificTrainingSet.SET_1607);

            double[] featureVector = extractor.ProcessAudioBuffer(handle);

            System.out.println(featureVector.length);

            for (int i = 0; i < featureVector.length; i++) {
                System.out.println(featureVector[i]);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void processAudioBuffer11K() throws Exception {
        String filePath = "app\\src\\test\\res\\FRAME_720_11K.DAT";
        double[] audioData = new double[1024];

        try {
            FileInputStream fid = new FileInputStream(filePath);
            DataInputStream data = new DataInputStream(fid);

            for (int i = 0; i < 1024; i++) {
                audioData[i] = data.readDouble();
            }

            PrecomputedFilters.Initialize(11025, 1024, 1024, TrainingSet.getMFCCComputer(TrainingSet.SpecificTrainingSet.SET_1709_11K), 12);

            AudioFrameHandle handle = new AudioFrameHandle(audioData, 1024);

            FeatureExtractor extractor = new FeatureExtractor(TrainingSet.SpecificTrainingSet.SET_1709_11K);

            double[] featureVector = extractor.ProcessAudioBuffer(handle);

            System.out.println(featureVector.length);

            for (int i = 0; i < featureVector.length; i++) {
                System.out.println(featureVector[i]);
            }
            double[] expected = new double[]{
                    -3.35471756982825,
                    -3.13497113676885,
                    -3.65604663006406,
                    -3.7184190455312,
                    -4.5196049355292,
                    -4.02619747992883,
                    -3.68074534874071,
                    -3.13255703229981,
                    -3.84636713538574,
                    -4.8055635911457,
                    -4.03662620257352,
                    -3.22539440488711,
                    -2.97731801828671,
                    -3.72610626574213,
                    -3.53587629737092,
                    -3.37766147402548,
                    -2.71614012592586,
                    -3.33771829971085,
                    -2.58315584997671,
                    -2.52193526655115,
                    -1.41819034662211,
                    -1.2682037816671,
                    -0.941860400658778,
                    -0.297038103851087,
                    -0.699453193575994,
                    -0.748584749007263,
                    -0.816992984953655,
                    -1.16971818664663,
                    -2.01601061044951,
                    -2.5728894474987,
                    -2.93542675526934,
                    -3.32290705151245,
                    -3.35023676131918,
                    -3.36066342793318,
                    -3.21427762191263,
                    -2.94754502709927,
                    -2.44043113717309,
                    -1.9011991197251,
                    -1.6591258541953,
                    -1.53465034223756,
                    0.000112941542763067,
                    0.248046875,
                    0,
                    0,
                    -1365.205078125,
                    -2817.64075941486,
                    0.735414181609921,
                    0,
                    -16.3245007977302,
                    1776.4892578125
            };

            assertArrayEquals(expected, featureVector, 1e-6);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}