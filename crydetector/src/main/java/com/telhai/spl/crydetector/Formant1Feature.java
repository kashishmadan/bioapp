package com.telhai.spl.crydetector;

import java.util.Arrays;

import Jama.Matrix;
import Jama.EigenvalueDecomposition;

/**
 * Created by Dima Ruinskiy on 03/09/16.
 * <p>
 * Implements ScalarFeature interface.
 * Computes first formant (based on LPC and LSF representations of the signal)
 * <p>
 * The implementation follows MATLAB's function POLY2LSF, which, in turn, uses
 * ROOTS and ANGLE to compute the complex roots of the LPC polynomial and their phases.
 * <p>
 * The roots are computed via eigenvalue decomposition of a specially crafted matrix.
 * The MATLAB implementation is in ROOTS.M. This code uses the JAMA library, specifically
 * the classes Matrix and EigenvalueDecomposition to compute the same. Angle is computed,
 * like in MATLAB, via the four-quadrant inverse tangent of a complex variable (Math.atan2).
 * <p>
 * Note that unlike MATLAB, we do not bother with throwing out the unit roots / conjugate roots
 * in the LSF computation stage, as these will be naturally filtered out in the subsequent
 * processing stages. Keeping the unit roots adds two unnecessary dimensions to the eigenvalue
 * problem, but simplifies the code and avoids the deconvolution/IIR filter.
 * <p>
 * Once the angles are found and sorted, the first formant is obtained the follows:
 * Consecutive positive angles are split into pairs and the difference in each pair is calculated.
 * The pair with the smallest difference corresponds to the first formant frequency, and the
 * formant value is estimated as the average of the two frequencies in the pair.
 */
public class Formant1Feature implements IScalarFeature
{
    /*
     * The compute function takes the LPC directly from the audio handle. Therefore, we assume
     * that it is a valid LPC (only doing a single sanity check for first coefficient value).
     */
    public double Compute(AudioFrameHandle handle)
    {

        double[] lpc = handle.GetLPC();
        int L = lpc.length;
        int i;

        /* Return 0.0 if LPC series is malformed (not normalized) or has NaNs present.
         * The latter can happen if recorder suddenly returns a sequence of all zeros.
         */
        if(lpc[0] != 1.0)
        {
            return 0.0;
        }
        for(i = 1; i < L; i++)
        {
            if(Double.isNaN(lpc[i]))
            {
                return 0.0;
            }
        }

        /* Sum and difference polynomials of LPC and reciprocal sequence (note the trailing 0) */
        double[] P = new double[L + 1];
        double[] Q = new double[L + 1];

        P[0] = 1;
        Q[0] = 1;
        for(i = 1; i < L; i++)
        {
            P[i] = lpc[i] - lpc[L - i];
            Q[i] = lpc[i] + lpc[L - i];
        }
        P[L] = -1;
        Q[L] = 1;

        /* Compute roots of both polynomials */
        ComplexArray rootsP = Roots(P);
        ComplexArray rootsQ = Roots(Q);

        /* In our case LP=LQ always, but just for clarity... */
        int LP = rootsP.realArray.length;
        int LQ = rootsQ.realArray.length;

        double[] angles = new double[LP + LQ];

        /* Compute angles using ATAN2 */
        for(i = 0; i < LP; i++)
        {
            angles[i] = Math.atan2(rootsP.imagArray[i], rootsP.realArray[i]);
        }
        for(i = 0; i < LQ; i++)
        {
            angles[i + LP] = Math.atan2(rootsQ.imagArray[i], rootsQ.realArray[i]);
        }

        /*
         * After sorting, we expect the negative angles, followed by 0, followed by all the
         * positive angles (conjugates of the negative ones), followed by PI.
         */
        Arrays.sort(angles);

        i = 0;
        while(angles[i] <= 0.0)    // Skip all the negative angles and 0
        {
            ++i;
        }

        double minWidth = 2 * Math.PI;    // Store the minimum width between consecutive angles
        int minWidthIndex = -1;         // Store the location of the minimum

        while(i < LP + LQ - 2)
        {
            if(angles[i + 1] - angles[i] < minWidth)
            {
                minWidth = angles[i + 1] - angles[i];
                minWidthIndex = i;
            }
            i += 2;
        }

        return ((angles[minWidthIndex] + angles[minWidthIndex + 1]) / 2);
    }

    /*
     * This is a private function to compute the roots of a polynomial via the eigenvalue
     * decomposition method.
     *
     * Since it is only use internally, on specially crafted polynomials based on LPC series,
     * we cut certain corners: the function does not check for leading or trailing zeros,
     * and does not normalize by the first coefficient (since it is guaranteed to be 1.0).
     */
    private ComplexArray Roots(double[] poly)
    {
        int L = poly.length - 1;

        double[][] matrixArray = new double[L][L];

        /*
         * Matrix with ones located one below the main diagonal, and -poly in the first row
         * The eigenvalues of this matrix will be the roots of poly.
         */
        matrixArray[0][0] = -poly[1];
        for(int i = 1; i < L; i++)
        {
            matrixArray[i][i - 1] = 1;
            matrixArray[0][i] = -poly[i + 1];
        }

        Matrix almostDiagonal = new Matrix(matrixArray, L, L);
        EigenvalueDecomposition eig = new EigenvalueDecomposition(almostDiagonal);

        /* Return arrays of real and imaginary parts of eigenvalues in a single structure */
        return new ComplexArray(eig.getRealEigenvalues(), eig.getImagEigenvalues());
    }

    /*
     * Simple wrapper class to pass real/imaginary arrays corresponding to a complex array
     * Used to avoid cumbersome passing of return values in arguments and extra array copying
     */
    private class ComplexArray
    {
        public double[] realArray;
        public double[] imagArray;

        ComplexArray(double[] real, double[] imag)
        {
            realArray = real;
            imagArray = imag;
        }
    }
}
