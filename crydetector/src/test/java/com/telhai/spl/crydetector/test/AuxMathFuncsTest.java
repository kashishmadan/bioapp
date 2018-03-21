package com.telhai.spl.crydetector.test;

import org.junit.Test;

import com.telhai.spl.crydetector.AuxMathFuncs;
import com.telhai.spl.crydetector.Matrix;

import static org.junit.Assert.*;

/**
 * Created by Dima Ruinskiy on 23/09/16.
 */
public class AuxMathFuncsTest {
    @Test
    public void testSortedMedianAndQuartiles() throws Exception {
        double[] x, y;
        for (int i = 1; i < 13; i++) {
            x = new double[i];
            for (int j = 0; j < i; j++) {
                x[j] = j + 1;
            }
            y = AuxMathFuncs.SortedMedianAndQuartiles(x);
            System.out.print(i);
            System.out.print(' ');
            System.out.print(y[0]);
            System.out.print(' ');
            System.out.print(y[1]);
            System.out.print(' ');
            System.out.print(y[2]);
            System.out.println();
            System.out.println(x[x.length / 3] + " " + x[x.length / 3 * 2] + " " + AuxMathFuncs.SortedMedian(x, x.length / 3, x.length / 3 * 2));
        }

    }

    @Test
    public void testRemOutliers() throws Exception {
        double[] arrayTT = {1, 2, 3, 4, 5, -30, 5, 4, 3, 2, 1, -30};
        double[] arrayTF = {1, 2, 3, 4, 5, -30, 5, 4, 3, 2, 1, -30};
        double[] arrayFT = {1, 2, 3, 4, 5, -30, 5, 4, 3, 2, 1, -30};
        double[] arrayFF = {1, 2, 3, 4, 5, -30, 5, 4, 3, 2, 1, -30};

        double[] resultTT = AuxMathFuncs.RemoveOutliers(arrayTT, 1.5, true, true);
        double[] resultTF = AuxMathFuncs.RemoveOutliers(arrayTF, 1.5, true, false);
        double[] resultFT = AuxMathFuncs.RemoveOutliers(arrayFT, 1.5, false, true);
        double[] resultFF = AuxMathFuncs.RemoveOutliers(arrayFF, 1.5, false, false);

        double[] arrayPartial = {-100, 1, 2, 3, 4, 5, -30, 5, 4, 3, 2, 1, -30, 100};
        double[] resurtPartial = AuxMathFuncs.RemoveOutliers(arrayPartial, 1, 13, 1.5);

        System.out.println(resultTT.length);
        System.out.println(resultTF.length);
        System.out.println(resultFT.length);
        System.out.println(resultFF.length);
        System.out.println(resurtPartial.length);
    }

    @Test
    public void testMeanAndVariance() throws Exception {
        double[] array = new double[10];

        for (int i = 0; i < 10; i++) {
            array[i] = Math.exp(i);
        }

        System.out.println(AuxMathFuncs.Mean(array) + " " + AuxMathFuncs.Variance(array) + " " + AuxMathFuncs.VarianceFromMean(array, AuxMathFuncs.Mean(array)));
    }

    @Test
    public void testRunLength() throws Exception {
        double[] sin = new double[200];

        for (int i=0;i<200;i++) {
            sin[i] = Math.sin(i);
        }
        System.out.println(AuxMathFuncs.MaxPositiveRunLength(sin));

        for (int i=0;i<200;i++) {
            sin[i] = Math.sin(i*i);
        }
        System.out.println(AuxMathFuncs.MaxPositiveRunLength(sin));
    }

}