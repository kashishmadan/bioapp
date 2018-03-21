package com.telhai.spl.crydetector.test;

import org.junit.Test;

import com.telhai.spl.crydetector.DirectFilter;

import static org.junit.Assert.*;

/**
 * Created by Dima Ruinskiy on 01/09/16.
 */
public class DirectFilterTest {

    @Test
    public void testFilter() throws Exception {

        double[] x = new double[50];

        for (int i=0;i<50;i++)
            x[i] = Math.sin(i);             // x = sin[n] for n=0..49

        for (int i=0;i<50;i++)
            System.out.println(x[i]);

        DirectFilter iirFilter = new DirectFilter(new double[]{.4, .3, .2, .1}, new double[]{1, -1, -.5, .5});

        double[] y = iirFilter.filter(x);    // Test IIR filter

        System.out.println();

        for (int i=0;i<50;i++)
            System.out.println(y[i]);

        DirectFilter firFilter = new DirectFilter(new double[]{.2,.2,.2,.2,.2});

        double[] z = firFilter.filter(x);   // Test FIR filter (single vector of coefficients; no denominator)

        System.out.println();

        for (int i=0;i<50;i++)
            System.out.println(z[i]);

        DirectFilter abnormal = new DirectFilter(new double[]{.4, .3, .2, .1}, new double[]{10, -10, -5, 5});

        double[] w = abnormal.filter(x);    // Test non-normalized filter (a[0] != 1)

        System.out.println();

        for (int i=0;i<50;i++)
            System.out.println(w[i]);
    }
}