package com.telhai.spl.crydetector.test;

import org.junit.Test;

import java.io.DataInputStream;
import java.io.FileInputStream;

import com.telhai.spl.crydetector.AudioFrameHandle;
import com.telhai.spl.crydetector.PrecomputedFilters;
import com.telhai.spl.crydetector.SpectrumRolloffFeature;

/**
 * Created by Dima Ruinskiy on 04/09/17.
 */
public class SpectrumRolloffTest {

    @Test
    public void testRolloff() throws Exception {
        String filePath = "app\\src\\test\\res\\FIRST_FRAME.DAT";
        double[] audioData = new double[4096];

        try {
            FileInputStream fid = new FileInputStream(filePath);
            DataInputStream data = new DataInputStream(fid);

            for (int i = 0; i < 4096; i++) {
                audioData[i] = data.readDouble();
            }

            PrecomputedFilters.Initialize(44100, 4096, 4096, null, 12);

            AudioFrameHandle handle = new AudioFrameHandle(audioData, 4096);

            SpectrumRolloffFeature feature = new SpectrumRolloffFeature();

            System.out.println("ROLLOFF: " + feature.Compute(handle));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRolloff11K() throws Exception {
        String filePath = "app\\src\\test\\res\\FRAME_36_11K.DAT";
        double[] audioData = new double[1024];

        try {
            FileInputStream fid = new FileInputStream(filePath);
            DataInputStream data = new DataInputStream(fid);

            for (int i = 0; i < 1024; i++) {
                audioData[i] = data.readDouble();
            }

            PrecomputedFilters.Initialize(11025, 1024, 1024, null, 12);

            AudioFrameHandle handle = new AudioFrameHandle(audioData, 1024);

            SpectrumRolloffFeature feature = new SpectrumRolloffFeature();

            System.out.println("ROLLOFF: " + feature.Compute(handle));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}