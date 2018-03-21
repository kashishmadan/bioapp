package com.telhai.spl.crydetector.test;

import org.junit.Test;

import java.io.DataInputStream;
import java.io.FileInputStream;

import com.telhai.spl.crydetector.AudioFrameHandle;
import com.telhai.spl.crydetector.Formant1Feature;
import com.telhai.spl.crydetector.PrecomputedFilters;

/**
 * Created by Dima Ruinskiy on 03/09/16.
 */
public class Formant1FeatureTest {

    @Test
    public void testFormat1Feature() throws Exception {
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

            Formant1Feature feature = new Formant1Feature();

            System.out.println("Formant 1: " + feature.Compute(handle));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}