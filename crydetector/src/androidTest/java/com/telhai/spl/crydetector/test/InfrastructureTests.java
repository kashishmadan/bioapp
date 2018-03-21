package com.telhai.spl.crydetector.test;

import org.jtransforms.fft.DoubleFFT_1D;
import org.junit.Test;

import com.telhai.spl.crydetector.FFT;

import static org.junit.Assert.*;

/**
 * Created by Dima Ruinskiy on 17-Jun-2017.
 */
public class InfrastructureTests {

    @Test
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