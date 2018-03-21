package com.telhai.spl.crydetector;

/**
 * Created by Dima Ruinskiy on 09/08/16.
 * <p>
 * This is a direct implementation of DCT/iDCT, and as such it is inefficient compared to FFT,
 * for large sequences. However we only need it for short series (several tens of coefficients),
 * so this is OK.
 */
public class DCT
{
    public static void dct(double[] x, double[] y)
    {

        int N = x.length;

        for(int k = 0; k < N; k++)
        {
            double sum = 0;
            for(int n = 0; n < N; n++)
            {
                sum += x[n] * Math.cos(Math.PI * k * (2.0 * n + 1) / (2 * N));
            }

            double alpha = (k > 0 ? 1 : 1 / Math.sqrt(2));
            y[k] = sum * alpha * Math.sqrt(2.0 / N);
        }
    }

    public static void idct(double[] y, double[] x)
    {

        int N = y.length;

        for(int n = 0; n < N; n++)
        {
            double sum = 0;

            for(int k = 0; k < N; k++)
            {
                double product = y[k] * Math.cos(Math.PI * k * (2.0 * n + 1) / (2 * N));
                double alpha = (k > 0 ? 1 : 1 / Math.sqrt(2));
                sum += alpha * product;
            }
            x[n] = sum * Math.sqrt(2.0 / N);

        }
    }
}
