package com.telhai.spl.crydetector.test;

import org.junit.Test;

import java.io.DataInputStream;
import java.io.FileInputStream;

import com.telhai.spl.crydetector.AudioFrameHandle;
import com.telhai.spl.crydetector.BandEnergyRatioFeature;
import com.telhai.spl.crydetector.PrecomputedFilters;

/**
 * Created by Dima Ruinskiy on 04/09/17.
 */
public class BandEnergyRatioFeatureTest {

    @Test
    public void testBER() throws Exception {
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

            BandEnergyRatioFeature feature = new BandEnergyRatioFeature();

            System.out.println("BER: " + feature.Compute(handle));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}