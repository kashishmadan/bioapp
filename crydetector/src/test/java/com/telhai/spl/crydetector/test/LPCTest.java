package com.telhai.spl.crydetector.test;

import org.junit.Test;

import java.io.DataInputStream;
import java.io.FileInputStream;

import com.telhai.spl.crydetector.AudioFrameHandle;
import com.telhai.spl.crydetector.LPC;
import com.telhai.spl.crydetector.PrecomputedFilters;

import static org.junit.Assert.*;

/**
 * Created by Dima Ruinskiy on 11/08/16.
 */
public class LPCTest {

    @Test
    public void testLevinson() throws Exception {
        double[] r = new double[200];
        for (int i = 1; i < 100; i++) {
            r[i] = 99.5 - 0.5 * i;
            r[99 + i] = r[i];
        }
        r[0] = 100;
        r[199] = 1;

        double[] A = LPC.levinson(r, 12, false);

        for (int i = 0; i < 13; i++)
            System.out.println(A[i]);
    }

    @Test
    public void testLPC() throws Exception {
        double[] r = new double[200];
        for (int i = 1; i < 100; i++) {
            r[i] = 99.5 - 0.5 * i;
            r[99 + i] = r[i];
        }
        r[0] = 100;
        r[199] = 1;

        double[] res = new double[]{1.0,
                -0.9949596475790896,
                -7.225540220245753E-5,
                2.810749923587205E-5,
                2.8175228212771374E-5,
                2.8233792126932768E-5,
                2.8293304228151224E-5,
                2.8353767201715637E-5,
                2.841518345093708E-5,
                2.847755463124338E-5,
                2.8540896670734778E-5,
                2.873747127877255E-5,
                0.0013408899527723422};

        System.out.println("LPC");

        double[] A = LPC.lpc(r, 12);

        assertArrayEquals(A, res, 1e-6);
    }

    @Test
    public void testLPCOfResampledAudio() throws Exception {
        String filePath = "app\\src\\test\\res\\FIRST_FRAME.DAT";
        double[] audioData = new double[4096];
        double[] res = new double[]{1.0,
                -0.2065557935524393,
                -0.1126407704420232,
                0.4200802098361617,
                -0.3525521387826726,
                0.12325303662979493,
                0.16218568818779042,
                -0.22455769394687047,
                0.15375261977283836,
                0.020437414199594942,
                -0.10416551321121671,
                0.07591855290578924,
                0.017358720706329926};

        System.out.println("LPC+resample");

        try {
            FileInputStream fid = new FileInputStream(filePath);
            DataInputStream data = new DataInputStream(fid);

            for (int i = 0; i < 4096; i++) {
                audioData[i] = data.readDouble();
            }

            PrecomputedFilters.Initialize(44100, 4096, 4096, null, 12);

            double[] downsampled = PrecomputedFilters.Downsample_44K_11K(audioData);

            double[] coeffs = LPC.lpc(downsampled, 12);

            assertArrayEquals(coeffs, res, 1e-6);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testLPCOfFirstFrame() throws Exception {
        String filePath = "app\\src\\test\\res\\FIRST_FRAME.DAT";
        double[] audioData = new double[4096];

        System.out.println("FPE");

        try {
            FileInputStream fid = new FileInputStream(filePath);
            DataInputStream data = new DataInputStream(fid);

            for (int i = 0; i < 4096; i++) {
                audioData[i] = data.readDouble();
            }

            PrecomputedFilters.Initialize(44100, 4096, 4096, null, 12);

            AudioFrameHandle handle = new AudioFrameHandle(audioData, 4096);

            double[] lpc = handle.GetLPC();

            System.out.println("LPC-LPC");

            for (int i = 0; i < 13; i++)
                System.out.println(lpc[i]);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}