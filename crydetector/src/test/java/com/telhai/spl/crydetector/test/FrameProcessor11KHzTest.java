package com.telhai.spl.crydetector.test;

import org.junit.Test;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import com.telhai.spl.WavFile.WavFile;
import com.telhai.spl.crydetector.AuxMathFuncs;
import com.telhai.spl.crydetector.FrameProcessor;
import com.telhai.spl.crydetector.FrameResult;
import com.telhai.spl.crydetector.TrainingSet;

import static com.telhai.spl.crydetector.TrainingSet.SpecificTrainingSet.SET_1709_11K;
import static org.junit.Assert.assertEquals;

/**
 * Created by Dima Ruinskiy on 05-Oct-2017.
 */
public class FrameProcessor11KHzTest {

    static final int CONST_DEFAULT_FREQ = 11025;
    static final int CONST_DEFAULT_FRAMESIZE = 1024;
    static final int CONST_DEFAULT_DFTSIZE = 1024;

    @Test
    public void testProcessFrame() throws Exception {

        String filePath = "app\\src\\test\\res\\FRAME_36_11K.DAT";
        double[] audioData = new double[CONST_DEFAULT_FRAMESIZE];

        try {
            FileInputStream fid = new FileInputStream(filePath);
            DataInputStream data = new DataInputStream(fid);

            for (int i = 0; i < CONST_DEFAULT_FRAMESIZE; i++) {
                audioData[i] = data.readDouble();
            }

            FrameProcessor frameProcessor = new FrameProcessor(CONST_DEFAULT_FREQ, CONST_DEFAULT_FRAMESIZE, CONST_DEFAULT_DFTSIZE, SET_1709_11K);
            FrameResult result = frameProcessor.ProcessFrame(audioData);
            System.out.println(result == FrameResult.SAVE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFullFileProcess() throws Exception {

        try {
            // Open the wav file specified as the first argument
            WavFile wavFile = WavFile.openWavFile(
                    new File("..\\_WAVDATA\\amber_11k_4_0_0000_5_0_0000_1h.wav"));

            // Display information about the wav file
            wavFile.display();

            // Get the number of audio channels in the wav file
            int numChannels = wavFile.getNumChannels();

            // Create a buffer for numChannels channels
            double[][] audioData = new double[numChannels][CONST_DEFAULT_FRAMESIZE];

            FrameProcessor frameProcessor =
                    new FrameProcessor(CONST_DEFAULT_FREQ, CONST_DEFAULT_FRAMESIZE, CONST_DEFAULT_DFTSIZE, SET_1709_11K);

            FrameResult result;
            int framesRead;
            int i = 0;
            double mean = 0.0;
            int tot = 0, hsh = 0;
            do {
                framesRead = wavFile.readFrames(audioData, CONST_DEFAULT_FRAMESIZE);
                mean = AuxMathFuncs.Mean(audioData[0]);
                for (int j = 0; j < CONST_DEFAULT_FRAMESIZE; j++) {
                    audioData[0][j] -= mean;
                }

                result = frameProcessor.ProcessFrame(audioData[0]);     // Left channel only

                if (result == FrameResult.SAVE) {
                    System.out.println("Detected frame " + i);
                    tot++;
                    hsh = (hsh + i * i) % 9973;
                }
                ++i;
            }
            while (framesRead != 0);

            wavFile.close();
            System.out.println("Total detected: " + tot);
            System.out.println("Hash: " + hsh);

            assertEquals(tot, 3315);
            assertEquals(hsh, 1107);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}