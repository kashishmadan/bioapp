package com.telhai.spl.crydetector;

import java.util.Arrays;

/**
 * Created by Dima Ruinskiy on 23/09/16.
 * <p>
 * Auxiliary Math primitives for the audio processing.
 */

public class AuxMathFuncs
{

    /* Array mean (average) value */
    public static double Mean(double[] array)
    {
        if(array == null || array.length == 0)
        {
            return 0;
        }

        double sum = 0.0;
        for(int i = 0; i < array.length; i++)
        {
            sum += array[i];
        }
        return (sum / array.length);
    }

    /* Array variance */
    public static double Variance(double[] array)
    {
        return VarianceFromMean(array, Mean(array));
    }

    /* Array variance when the mean is precomputed and passed as an argument
     * (note that we normalize by Length-1 when Length>1, as in MATLAB)
     * For arrays shorter than 2, the variance is 0 by definition.
     */
    public static double VarianceFromMean(double[] array, double mean)
    {
        if(array == null || array.length < 2)
        {
            return 0;
        }

        double varsum = 0.0;
        for(int i = 0; i < array.length; i++)
        {
            varsum += (array[i] - mean) * (array[i] - mean);
        }
        return (varsum / (array.length - 1));
    }

    /* Array median (sorts and uses SortedMedian internally without affecting original array) */
    public static double Median(double[] array)
    {
        double[] sorted = array.clone();
        Arrays.sort(sorted);
        return SortedMedian(sorted);
    }

    /* Sorted median. Implements the equivalent of MATLAB's median function.
     * Assumes that the array has been pre-sorted.
     */
    public static double SortedMedian(double[] array)
    {
        return SortedMedian(array, 0, array.length);
    }

    /* Sorted median with boundary indices. Note: for simplicity, 'last' should
     * not be the last element to consider, but rather the first to not consider.
     */
    public static double SortedMedian(double[] array, int first, int last)
    {
        if(array == null || last - first <= 0)
        {
            return Double.NaN;
        }

        int middle = (first + last) / 2;
        if((last - first) % 2 == 0)
        {    // even-sized array - median is average between two middle elements
            return 0.5 * (array[middle - 1] + array[middle]);
        } else
        {                        // odd-sized array - median is the middle element
            return array[middle];
        }
    }

    /* Sorted median and quartiles. Implements the equivalent of QUARTILES.M from the Babies Cry
     * Analysis Toolkit. Returns three sample quartiles (middle one being the median).
     * Assumes that the array has been pre-sorted.
     */
    public static double[] SortedMedianAndQuartiles(double[] array)
    {

        /* bad input or empty array */
        if(array == null || array.length == 0)
        {
            return new double[]{Double.NaN, Double.NaN, Double.NaN};
        }

        /* The exact values returned depend on the length of the array modulo 4.
         * The general case works remarkably well also for degenerate arrays of length 1, 2 or 3.
         * The zero-length case is handled previously.
         * Note that the implementation depends heavily on the fact that length / 2 and length / 4
         * are always rounded down to the nearest integer.
         */
        switch(array.length % 4)
        {
            case 0:
                return new double[]{
                        0.5 * (array[array.length / 4 - 1] + array[array.length / 4]),
                        0.5 * (array[array.length / 2 - 1] + array[array.length / 2]),
                        array[array.length / 4 * 3 - 1]
                };
            case 1:
                return new double[]{
                        array[array.length / 4],
                        array[array.length / 2],
                        array[array.length / 4 * 3]
                };
            case 2:
                return new double[]{
                        array[array.length / 4],
                        0.5 * (array[array.length / 2 - 1] + array[array.length / 2]),
                        0.5 * (array[array.length / 4 * 3] + array[array.length / 4 * 3 + 1])
                };
            case 3:
                return new double[]{
                        0.5 * (array[array.length / 4] + array[array.length / 4 + 1]),
                        array[array.length / 2],
                        0.5 * (array[array.length / 4 * 3 + 1] + array[array.length / 4 * 3 + 2])
                };
            default:    // not supposed to happen
                return null;
        }
    }

    /* Removes outliers in the array, equivalent to REM_OUTLIER.M in the Babies Cry Analysis Toolkit.
     * Outliers are defined as values outside the range of [Q1-F*D ; Q3+F*D], where Q1 and Q3 are
     * the first and third quartiles, respectively, D = Q3-Q1 and F is a user-defined factor. In
     * other words, the values that are outside the middle range by more than the factor times the
     * range.
     *
     * Arguments:
     * inplace - when true, performs manipulations on the input array and returns it in the same
     * memory location. Since fixed array sizes cannot be changed, outliers are simply replaced
     * with NaNs.
     * returnSorted - when true, the output array will be sorted (low to high).
     *
     * Note that the algorithm always sorts the data. Therefore, to avoid any array copying,
     * both inplace and returnSorted must be true. If inplace is false, the function guarantees
     * that the original array will be unmodified.
     */
    public static double[] RemoveOutliers(double[] array, double factor, boolean inplace, boolean returnSorted)
    {

        // Clone array for sorting, unless in-place sorting and modification are requested
        double[] sortedArray = (inplace && returnSorted) ? array : array.clone();
        Arrays.sort(sortedArray);

        // Find quartiles and determine range (min;max)
        double[] quartiles = SortedMedianAndQuartiles(sortedArray);
        double range = quartiles[2] - quartiles[0];
        double max = quartiles[2] + factor * range;
        double min = quartiles[0] - factor * range;

        int numOutliers = 0;
        double[] intermediateArray = (returnSorted) ? sortedArray : array;

        // Find the outliers, count them, and (optionally, if inplace=true) mark them
        for(int i = 0; i < intermediateArray.length; i++)
        {
            if(intermediateArray[i] < min || intermediateArray[i] > max)
            {
                ++numOutliers;
                if(inplace)
                {
                    intermediateArray[i] = Double.NaN;
                }
            }
        }

        // If inplace=false, allocate a new (smaller) array and copy all non-outliers to it
        double[] outputArray = (inplace) ? intermediateArray : new double[intermediateArray.length - numOutliers];

        if(!inplace)
        {
            for(int i = 0, j = 0; i < intermediateArray.length; i++)
            {
                if(!(intermediateArray[i] < min || intermediateArray[i] > max))
                {
                    outputArray[j++] = intermediateArray[i];
                }
            }
        }

        return outputArray;
    }

    /* Removes outliers in the array, considering only the elements between 'first' and 'last'
     * ('last' being the first element not to consider).
     */
    public static double[] RemoveOutliers(double[] array, int first, int last, double factor)
    {
        double[] newArray = new double[last - first];
        for(int i = first; i < last; i++)
        {
            newArray[i - first] = array[i];
        }
        return RemoveOutliers(newArray, factor, false, false);
    }

    /* Maximum positive run length
     * Returns the maximum number of consecutive positive numbers in the array.
     */
    public static int MaxPositiveRunLength(double[] array)
    {
        int runLength = 0, maxRunLength = 0;

        for(int i = 0; i < array.length; i++)
        {
            if(array[i] > 0)
            { // accumulate
                ++runLength;
            } else
            {
                if(runLength > maxRunLength)
                { // store if new maximum
                    maxRunLength = runLength;
                }
                runLength = 0;  // reset
            }
        }

        if(runLength > maxRunLength)
        { // check run length at array boundary
            maxRunLength = runLength;
        }

        return maxRunLength;
    }
}
