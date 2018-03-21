package com.telhai.spl.crydetector.test;

import org.junit.Test;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import com.telhai.spl.WavFile.WavFile;
import com.telhai.spl.crydetector.FrameProcessor;
import com.telhai.spl.crydetector.FrameResult;
import com.telhai.spl.crydetector.TrainingSet;

import static com.telhai.spl.crydetector.TrainingSet.SpecificTrainingSet.SET_1607;
import static org.junit.Assert.*;

/**
 * Created by Dima Ruinskiy on 15-Jan-2017.
 */
public class FrameProcessorTest {

    static final int CONST_DEFAULT_FREQ = 44100;
    static final int CONST_DEFAULT_FRAMESIZE = 4096;
    static final int CONST_DEFAULT_DFTSIZE = 4096;

    @Test
    public void testProcessFrame() throws Exception {

        String filePath = "app\\src\\test\\res\\FRAME_1890.DAT";
        double[] audioData = new double[4096];

        try {
            FileInputStream fid = new FileInputStream(filePath);
            DataInputStream data = new DataInputStream(fid);

            for (int i = 0; i < 4096; i++) {
                audioData[i] = data.readDouble();
            }

            FrameProcessor frameProcessor = new FrameProcessor(CONST_DEFAULT_FREQ, CONST_DEFAULT_FRAMESIZE, CONST_DEFAULT_DFTSIZE, SET_1607);
            FrameResult result = frameProcessor.ProcessFrame(audioData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFullFileProcess() throws Exception {

        try {
            // Open the wav file specified as the first argument
            WavFile wavFile = WavFile.openWavFile(
                    new File("..\\_WAVDATA\\amber_0_0_0000_1_0_0000_1h.wav"));

            // Display information about the wav file
            wavFile.display();

            // Get the number of audio channels in the wav file
            int numChannels = wavFile.getNumChannels();

            // Create a buffer for numChannels channels
            double[][] audioData = new double[numChannels][CONST_DEFAULT_FRAMESIZE];

            FrameProcessor frameProcessor =
                    new FrameProcessor(CONST_DEFAULT_FREQ, CONST_DEFAULT_FRAMESIZE, CONST_DEFAULT_DFTSIZE, SET_1607);

            FrameResult result;
            int framesRead;
            int i = 0;
            do {
                framesRead = wavFile.readFrames(audioData, CONST_DEFAULT_FRAMESIZE);
//                System.out.println(i);

                result = frameProcessor.ProcessFrame(audioData[0]);     // Left channel only

                if (result == FrameResult.SAVE) {
                    System.out.println("Detected frame " + i);
                }
                ++i;
            }
            while (framesRead != 0);

            wavFile.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}