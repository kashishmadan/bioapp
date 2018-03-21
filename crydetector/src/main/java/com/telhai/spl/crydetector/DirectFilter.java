package com.telhai.spl.crydetector;

/**
 * Created by Dima Ruinskiy on 31/08/16.
 * <p>
 * This class implements a straightforward digital filter (FIR/IIR).
 * Implementation is based on Direct Form II.
 */
public class DirectFilter
{

    private double[] a; // Feedback (IIR) coefficients a[n]
    private double[] b; // Feedforward (FIR) coefficients b[n]

    public DirectFilter(double[] _b, double[] _a)
    {    // feedforward coefficients (x[n]) first, feedback coefficients (y[n]) second
        a = _a.clone();
        b = _b.clone();
        normalizeFilter();  // normalize by a[0] if needed
    }

    public DirectFilter(double[] _b)
    { // feedforward coefficients only (FIR filter)
        a = new double[]{1.0};
        b = _b.clone();
    }

    /* Filter implementation based on Direct Form II
     *
     * Assuming a0 = 1 (filter has been normalized).
     * nb = length of numerator polynomial
     * na = length of denominator polynomial
     *
     * Intermediate difference equation:
     * w[n] = x[n] - a1*w[n-1] - a2*w[n-2] - ... - a_na*w[n-na]
     *
     * Final output difference equation:
     * y[n] = b0*w[n] + b1*w[n-1] + b2*w[n-2] + ... + b_nb*w[n-nb]
     *
     * The filtering function assumes no initial conditions, that is,
     * nonexistent samples at the beginning of the signal (before filter is saturated)
     * are assumed to be 0.
     */
    public double[] filter(double[] x)
    {
        double[] y = new double[x.length];
        double[] w = new double[x.length];
        int i, j, n;
        int na = a.length - 1, nb = b.length - 1;

        for(i = 0; i < x.length; i++)
        {
            w[i] = x[i];
            n = (i < na) ? i : na;  // If i<na, only take i past samples
            for(j = 1; j <= n; j++)
            {
                w[i] -= a[j] * w[i - j];
            }
            y[i] = b[0] * w[i];
            n = (i < nb) ? i : nb;  // If i<nb, only take i past samples
            for(j = 1; j <= n; j++)
            {
                y[i] += b[j] * w[i - j];
            }
        }

        return y;
    }

    private void normalizeFilter()
    {    // Normalizes all filter coefficients by a[0] if a[0] is not 1
        if(a[0] != 1.0)
        {
            for(int i = 0; i < b.length; i++)
            {
                b[i] /= a[0];
            }
            for(int i = 1; i < a.length; i++)
            {
                a[i] /= a[0];
            }
            a[0] = 1.0;
        }
    }
}
