package com.telhai.spl.crydetector;

import org.jtransforms.fft.DoubleFFT_1D;

/**
 * Created by Dima Ruinskiy on 11/08/16.
 * <p>
 * Implementation for Levinson-Durbin recursion taken from cettur/JPM
 */
public class LPC
{
    /**
     * Levinson-Durbin recursion
     *
     * @param r
     * @param order
     * @param allow_singularity
     * @return
     * @throws IllegalArgumentException
     */
    public static double[] levinson(double[] r, int order, boolean allow_singularity/* default False */) throws IllegalArgumentException
    {
        double T0 = r[0];
        int M = r.length - 1;
        if(order == -1)
        {
            M = r.length - 1;
        } else if(order > M)
        {
            throw new IllegalArgumentException("Order must be less than size of the input data");
        } else
        {
            M = order;
        }
        double[] A = new double[M + 1];
        A[0] = 1;
        double[] ref = new double[M];
        double P = T0;
        for(int k = 0; k < M; k++)
        {
            double save = r[k + 1];
            double temp;
            if(k == 0)
            {
                temp = -save / P;
            } else
            {
                for(int j = 0; j < k; j++)
                {
                    save += A[j + 1] * r[k - j];
                }
                temp = -save / P;
            }
            P = P * (1. - temp * temp);
            if(P <= 0 && !allow_singularity)
            {
                throw new IllegalArgumentException("ValueError: singular matrix");
            }
            A[k + 1] = temp;
            ref[k] = temp; // save reflection coefficient at each step
            if(k == 0)
            {
                continue;
            }
            int khalf = (k + 1) / 2;
            for(int j = 0; j < khalf; j++)
            {
                int kj = k - j;
                save = A[j + 1];
                A[j + 1] = save + temp * A[kj];
                if(j + 1 != kj)
                {
                    A[kj] += temp * save;
                }
            }
        }
        return A;
    }

    public static double[] lpc(double[] x, int order)
    {
        int n = x.length;      // For auto-correlation - round up length of X to next power of 2 and double
        int M = (1 << (1 + (int) Math.ceil(Math.log(n) / Math.log(2))));

        /* Java initializes these to 0 by default */
        double[] X = new double[2 * M];

        for(int i = 0; i < n; i++)
        {
            X[i] = x[i];
        }

        DoubleFFT_1D fft = new DoubleFFT_1D(M);

        fft.realForwardFull(X);  // Compute double-length FFT

        for(int i = 0; i < M; i++)
        {
            X[2 * i] = (X[2 * i] * X[2 * i] + X[2 * i + 1] * X[2 * i + 1]) / n;  // Absolute value squared normalized by original length
            X[2 * i + 1] = 0;
        }

        fft.complexInverse(X, false); // Compute inverse FFT and throw out the imaginary part (which should be ~0 anyways)

        double[] r = new double[M];
        for(int i = 0; i < M; i++)
        {
            r[i] = X[2 * i];
        }
        return levinson(r, order, false);
    }
}