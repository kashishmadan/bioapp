package com.telhai.spl.crydetector.test;

import org.junit.Test;

import java.io.DataInputStream;
import java.io.FileInputStream;

import com.telhai.spl.crydetector.FFT;
import org.jtransforms.fft.DoubleFFT_1D;

import static org.junit.Assert.*;

/**
 * Created by Dima Ruinskiy on 06/08/16.
 */
public class FFTTest {

    @Test
    public void testFft() throws Exception {

        String filePath = "app\\src\\test\\res\\FIRST_FRAME.DAT";
        double[] audioData = new double[4096];
        double[] imagData = new double[4096];

        System.out.println("FFT!");

        try {
            FileInputStream fid = new FileInputStream(filePath);
            DataInputStream data = new DataInputStream(fid);

            for (int i = 0; i < 4096; i++) {
                audioData[i] = data.readDouble();
            }

            FFT mFFT = new FFT(4096);
            mFFT.fft(audioData, imagData);

            for (int i = 2040; i < 2055; i++)
                System.out.println(audioData[i] + " " + imagData[i]);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testIfft() throws Exception {

        String filePath = "app\\src\\test\\res\\FIRST_FRAME.DAT";
        double[] audioData = new double[4096];
        double[] imagData = new double[4096];

        System.out.println("IFFT!");

        try {
            FileInputStream fid = new FileInputStream(filePath);
            DataInputStream data = new DataInputStream(fid);

            for (int i = 0; i < 4096; i++) {
                audioData[i] = data.readDouble();
            }

            FFT mFFT = new FFT(4096);
            mFFT.ifft(audioData, imagData);

            for (int i = 2040; i < 2055; i++)
                System.out.println(audioData[i] + " " + imagData[i]);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testRceps() throws Exception {
        double[] x = new double[1024];

        for (int i = 0; i < 1024; i++)
            x[i] = Math.cos(i * i);

        FFT fft = new FFT(1024);

        fft.rceps(x);

        for (int i = 0; i < 1024; i += 16)
            System.out.println(x[i]);
    }

    @Test
    public void testFFTCompare() throws Exception {

        String filePath = "app\\src\\test\\res\\FIRST_FRAME.DAT";
        double[] audioData = new double[4096];
        double[] imagData = new double[4096];
        double[] jointData = new double[8192];

        System.out.println("fft-compare!");

        try {
            FileInputStream fid = new FileInputStream(filePath);
            DataInputStream data = new DataInputStream(fid);

            for (int i = 0; i < 4096; i++) {
                jointData[i] = audioData[i] = data.readDouble();
            }

            FFT mFFT = new FFT(4096);
            mFFT.fft(audioData, imagData);

            DoubleFFT_1D mFFT1 = new DoubleFFT_1D(4096);
            mFFT1.realForwardFull(jointData);

            for (int i = 0; i < 4096; i++) {
                assertEquals(audioData[i], jointData[2 * i], 1e-6);
                assertEquals(imagData[i], jointData[2 * i + 1], 1e-6);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void testFFTCompareImplementationsLong() throws Exception {

        int size = 4096;
        int iterations = 50000;

        double[] real = new double[size];
        double[] imag = new double[size];
        double[] comp = new double[2 * size];

        long pre, post;

        FFT fft = new FFT(size);
        DoubleFFT_1D fftj = new DoubleFFT_1D(size);

        for (int i = 0; i < size; i++) {
            real[i] = comp[2 * i] = Math.random();
            imag[i] = comp[2 * i + 1] = Math.random();
        }

        pre = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            fft.fft(real, imag);
        }
        post = System.nanoTime();

        System.out.println("FFT Time = " + (post - pre) + " tics");

        pre = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            fftj.complexForward(comp);
        }
        post = System.nanoTime();
        System.out.println("FFTJ Time = " + (post - pre) + " tics");
        pre = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            fft.fft(real, imag);
        }
        post = System.nanoTime();

        System.out.println("FFT Time = " + (post - pre) + " tics");

        pre = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            fftj.complexForward(comp);
        }
        post = System.nanoTime();
        System.out.println("FFTJ Time = " + (post - pre) + " tics");

        pre = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            fftj.realForward(comp);
        }
        post = System.nanoTime();
        System.out.println("RealForward Time = " + (post - pre) + " tics");

        pre = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            fftj.realForwardFull(comp);
        }
        post = System.nanoTime();
        System.out.println("RealForwardFull Time = " + (post - pre) + " tics");

        pre = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            fftj.realForward(comp);
        }
        post = System.nanoTime();
        System.out.println("RealForward Time = " + (post - pre) + " tics");

        pre = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            fftj.realForwardFull(comp);
        }
        post = System.nanoTime();
        System.out.println("RealForwardFull Time = " + (post - pre) + " tics");
    }
}