package com.telhai.spl.crydetector;

/**
 * Represents an interface to compute a vector feature from an audio frame.
 * Created by Dima Ruinskiy on 10/21/15.
 */
public interface IVectorFeature
{
    public double[] Compute(AudioFrameHandle handle);
}
