package com.telhai.spl.crydetector.test;

import org.junit.Test;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import com.telhai.spl.WavFile.WavFile;
import com.telhai.spl.crydetector.AudioFrameHandle;
import com.telhai.spl.crydetector.PitchDetector;
import com.telhai.spl.crydetector.PitchParametersVectorFeature;
import com.telhai.spl.crydetector.PrecomputedFilters;

/**
 * Created by Dima Ruinskiy on 27/09/16.
 */
public class PitchDetectorTest {
    @Test
    public void computePitchVector() throws Exception {

        String wavFileName = "..\\_WAVDATA\\pitchtestwav.wav";
        WavFile wavReader = WavFile.openWavFile(new File(wavFileName));
        wavReader.display();

        double[] frameData = new double[(int) wavReader.getNumFrames()];

        wavReader.readFrames(frameData, (int) wavReader.getNumFrames());

        PitchDetector pitchd = new PitchDetector(44100, 16, 0.85);

        PitchDetector.PitchData pitchData = pitchd.ComputePitchVector(frameData);

        System.out.println(pitchData.length);
        for (int i=0;i<pitchData.length;i++) {
            System.out.print((int)pitchData.pitchPeriod[i]);
            System.out.print(" ");
            if (i%20==19) System.out.println();
        }
    }

    @Test
    public void computePitchVector11K() throws Exception {

        String wavFileName = "..\\_WAVDATA\\pitchtestwav11.wav";
        WavFile wavReader = WavFile.openWavFile(new File(wavFileName));
        wavReader.display();

        double[] frameData = new double[(int) wavReader.getNumFrames()];

        wavReader.readFrames(frameData, (int) wavReader.getNumFrames());

        PitchDetector pitchd = new PitchDetector(11025, 16, 0.75);

        PitchDetector.PitchData pitchData = pitchd.ComputePitchVector(frameData);

        System.out.println(pitchData.length);
        for (int i=0;i<pitchData.length;i++) {
            System.out.print((int)pitchData.pitchPeriod[i]);
            System.out.print(" ");
            if (i%20==19) System.out.println();
        }
    }

    @Test
    public void testPitchParameters() throws Exception {
        String filePath = "app\\src\\test\\res\\FIRST_FRAME.DAT";
        double[] audioData = new double[4096];

        try {
            FileInputStream fid = new FileInputStream(filePath);
            DataInputStream data = new DataInputStream(fid);

            for (int i = 0; i < 4096; i++) {
                audioData[i] = data.readDouble();
            }

            PrecomputedFilters.Initialize(44100, 4096, 4096, null, 12);

            AudioFrameHandle handle = new AudioFrameHandle(audioData, 4096);

            PitchParametersVectorFeature feature = new PitchParametersVectorFeature();

            double[] pitchParam = feature.Compute(handle);

            System.out.print(pitchParam[3]);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPitchZeroFrame() throws Exception {
        double[] audioData = new double[4096];

        try {
            PrecomputedFilters.Initialize(44100, 4096, 4096, null, 12);

            AudioFrameHandle handle = new AudioFrameHandle(audioData, 4096);

            PitchParametersVectorFeature feature = new PitchParametersVectorFeature();

            double[] pitchParam = feature.Compute(handle);

            System.out.println(pitchParam[0]);
            System.out.println(pitchParam[1]);
            System.out.println(pitchParam[2]);
            System.out.println(pitchParam[3]);
            System.out.println(pitchParam[4]);
            System.out.println(pitchParam[5]);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void tallestPeakTest()  throws Exception {
        String filePath = "app\\src\\test\\res\\FIRST_FRAME.DAT";
        double[] audioData = new double[4096];

        try {
            FileInputStream fid = new FileInputStream(filePath);
            DataInputStream data = new DataInputStream(fid);

            for (int i = 0; i < 4096; i++) {
                audioData[i] = data.readDouble();
            }

            PrecomputedFilters.Initialize(44100, 4096, 4096, null, 12);

            AudioFrameHandle handle = new AudioFrameHandle(audioData, 4096);

            PitchParametersVectorFeature feature = new PitchParametersVectorFeature();

            PitchParametersVectorFeature.IndexValue[] peaks = feature.GetTallestPeaks(handle.GetFFTMagnitude(), 13);

            for (int i = 0; i < 13; i++) {
                System.out.println(peaks[i].index + " " + peaks[i].value);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void activePitchTest()  throws Exception {
        String filePath = "app\\src\\test\\res\\FRAME_1890.DAT";
        double[] audioData = new double[4096];

        try {
            FileInputStream fid = new FileInputStream(filePath);
            DataInputStream data = new DataInputStream(fid);

            for (int i = 0; i < 4096; i++) {
                audioData[i] = data.readDouble();
            }

            PrecomputedFilters.Initialize(44100, 4096, 4096, null, 12);

            AudioFrameHandle handle = new AudioFrameHandle(audioData, 4096);

            PitchParametersVectorFeature feature = new PitchParametersVectorFeature();

            double[] pitchparams = feature.Compute(handle);
            for (int i = 0; i < pitchparams.length; i++) {
                System.out.println(pitchparams[i]);
            }


        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void floatModulusTest() throws Exception {
        double[] arr = {0.0,1.1,2.2,3.3,4.4,5.5,6.6,7.7,8.8,9.9,11};

        for (int i = 0; i < 11; i++) {
            System.out.println(arr[i] % 3.4);
        }
        for (int i = 0; i < 11; i++) {
            System.out.println((arr[i] % 3.4) > 1.7 ? 3.4 - (arr[i] % 3.4) : (arr[i] % 3.4));
        }
    }

    @Test
    public void medfiltTest() throws Exception {
        double[] arr = new double[12];
        for (int i = 0; i < 12; i++) {
            arr[i] = Math.sin(i);
        }

        double[] flt = PitchParametersVectorFeature.MedianFilter3Pt(arr);

        for (int i = 0; i < 12; i++) {
            System.out.println(flt[i]);
        }
    }
}