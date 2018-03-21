package com.telhai.spl.crydetector;
// EXTERNAL CODE!

/**
 * This class is part of MEAPsoft.
 */
public class FFT
{

    int n, m;

    // Lookup tables. Only need to recompute when size of FFT changes.
    double[] cos;
    double[] sin;

    public FFT(int n)
    {
        this.n = n;
        this.m = (int) (Math.log(n) / Math.log(2));

        // Make sure n is a power of 2
        if(n != (1 << m))
        {
            throw new RuntimeException("FFT length must be power of 2");
        }

        // precompute tables
        cos = new double[n / 2];
        sin = new double[n / 2];

        for(int i = 0; i < n / 2; i++)
        {
            cos[i] = Math.cos(-2 * Math.PI * i / n);
            sin[i] = Math.sin(-2 * Math.PI * i / n);
        }

    }

    public void fft(double[] x, double[] y)
    {
        int i, j, k, n1, n2, a;
        double c, s, t1, t2;

        if(x.length != n || y.length != n)
        {
            throw new RuntimeException("Sequence length does not match predefined FFT length");
        }

        // Bit-reverse
        j = 0;
        n2 = n / 2;
        for(i = 1; i < n - 1; i++)
        {
            n1 = n2;
            while(j >= n1)
            {
                j = j - n1;
                n1 = n1 / 2;
            }
            j = j + n1;

            if(i < j)
            {
                t1 = x[i];
                x[i] = x[j];
                x[j] = t1;
                t1 = y[i];
                y[i] = y[j];
                y[j] = t1;
            }
        }

        n1 = 0;
        n2 = 1;

        for(i = 0; i < m; i++)
        {
            n1 = n2;
            n2 = n2 + n2;
            a = 0;

            for(j = 0; j < n1; j++)
            {
                c = cos[a];
                s = sin[a];
                a += 1 << (m - i - 1);

                for(k = j; k < n; k = k + n2)
                {
                    t1 = c * x[k + n1] - s * y[k + n1];
                    t2 = s * x[k + n1] + c * y[k + n1];
                    x[k + n1] = x[k] - t1;
                    y[k + n1] = y[k] - t2;
                    x[k] = x[k] + t1;
                    y[k] = y[k] + t2;
                }
            }
        }
    }

    /* Implement IFFT using FFT as follows:
     *
     * Swap real and imaginary parts of input
     * Divide output by n (series length)
     * Swap real and imaginary parts of output
     *
     * Because fft() does an in-place transformation, calling fft(y,x) effectively achieves
     * input and output swaps at the same time.
     */
    public void ifft(double[] x, double[] y)
    {
        fft(y, x);

        for(int i = 0; i < n; i++)
        {
            y[i] /= n;
            x[i] /= n;
        }

    }

    /* Implement real cepstrum (MATLAB's RCEPS.M) as real(ifft(log(abs(fft(x))))
     * The implementation is in-place (input sequence is replaced).
     * The imaginary part is discarded. The input X is assumed to be real.
     */
    public void rceps(double[] x)
    {
        double[] y = new double[x.length];  // Imaginary part (initializes to 0)

        fft(x, y);  // In-place DFT

        for(int i = 0; i < n; i++)
        {
            x[i] = Math.sqrt(x[i] * x[i] + y[i] * y[i]);    // Absolute value
            if(x[i] == 0)
            {
                throw new RuntimeException("Zeros in DFT, cannot compute real cepstrum");
            }
            x[i] = Math.log(x[i]);  // Log(x) must be defined
            y[i] = 0;   // Imaginary part is again zero
        }

        ifft(x, y); // X will hold the real part, imaginary part is discarded
    }
}