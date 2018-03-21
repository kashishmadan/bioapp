package com.telhai.spl.crydetector;

/**
 * Created by Dima Ruinskiy on 07/01/17.
 */

public class MFCCVectorFeature implements IVectorFeature
{

    public double[] Compute(AudioFrameHandle handle)
    {
        return handle.GetMFCC();
    }
}
