package com.telhai.spl.crydetector.test;

import org.jtransforms.fft.DoubleFFT_1D;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.FileInputStream;

import com.telhai.spl.crydetector.FFT;
import com.telhai.spl.crydetector.MFCC;
import com.telhai.spl.crydetector.MFCC2;
import com.telhai.spl.crydetector.PrecomputedFilters;

import static org.junit.Assert.*;

/**
 * Created by Dima Ruinskiy on 07/08/16.
 */
public class MFCCTest {

    @Test
    public void testCepstrumOld() throws Exception {
        String filePath = "app\\src\\test\\res\\FIRST_FRAME.DAT";
        double[] audioData = new double[4096];
        double[] imagData = new double[4096];
        double[] cepstrum = new double[40];

        try
        {
            FileInputStream fid = new FileInputStream(filePath);
            DataInputStream data = new DataInputStream(fid);

            for (int i=0;i<4096;i++) {
                audioData[i] = data.readDouble();
            }

            FFT mFFT = new FFT(4096);
            mFFT.fft(audioData, imagData);

            MFCC mfcc = new MFCC(4096,40,40,44100);

            cepstrum = mfcc.cepstrum(audioData,imagData);

            for (int i=0;i<40;i++) {
                System.out.println(cepstrum[i]);
            }



        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Test
    public void testMfcc2MelBank() throws Exception
    {
        String filePath = "app\\src\\test\\res\\FIRST_FRAME.DAT";
        double[] audioData = new double[8192];

        double[] expected = new double[]{-12.165977148008505,
                3.9413299227991634,
                -2.15565707537635,
                -0.4839460776225918,
                -0.6161416234822431,
                1.6222973140230386,
                -2.0071876709622853,
                0.44560462104432885,
                0.19747774338801102,
                -0.08799305583603827,
                -0.4311386446748871,
                -0.23416566677933423,
                0.6801261840234343,
                -0.3446570726112313,
                0.10031662322791876,
                0.23896380878014475,
                -0.030962934795459262,
                -0.23250943763916268,
                -0.27015440429922283,
                0.2746741442515726,
                -0.14648673788205194,
                0.00670471560899519,
                -0.016915286360866397,
                0.013790943917033198,
                0.019607469834706688,
                -0.34924615814595156,
                -0.11739380489936771,
                -0.15572466943076801,
                -0.08034872921730787,
                -0.05438162594702899,
                -0.08344512441695637,
                -0.05658978386556168,
                0.0022884542820011946,
                0.09455688065457407,
                -0.02029348636219809,
                0.014862559337084299,
                0.148747791031698,
                0.11588880336279399,
                0.0};

        try
        {
            FileInputStream fid = new FileInputStream(filePath);
            DataInputStream data = new DataInputStream(fid);
            PrecomputedFilters.Initialize(44100,4096,4096,null,12);

            for (int i=0;i<4096;i++) {
                audioData[i] = data.readDouble();
                audioData[i] *= PrecomputedFilters.Hamming4K[i];
            }

            DoubleFFT_1D mFFT = new DoubleFFT_1D(4096);
            mFFT.realForwardFull(audioData);

            MFCC2 mfcc = new MFCC2(4096,4096,39,39,44100);

            double[] cepstrum = mfcc.cepstrum(audioData);

            assertArrayEquals(expected, cepstrum, 1e-6);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Test
    public void testMfcc2MelBank11K() throws Exception
    {
        String filePath = "app\\src\\test\\res\\FIRST_FRAME_11K.DAT";
        double[] audioData = new double[2048];

        double[] expected = new double[]{
                -2.32348953838391,
                1.61376503420021,
                0.339115657036865,
                1.02396653694932,
                -1.37080293928433,
                0.674340313979154,
                0.643951168287081,
                -0.936209607882974,
                -0.43838319060575,
                0.578615945202413,
                0.33712635299202,
                0.032622066415205,
                0.261437417316937,
                -0.25154375768762,
                -0.145400860264857,
                0.162425199486412,
                0.0272804158207602,
                -0.095762827595388,
                0.249507928761548,
                0.320795130236199,
                0.430396116911829,
                0.0860716209363606,
                0.367444697687089,
                0.102623577752802,
                0.105768393228415,
                -0.290626152464339,
                0.213967218635879,
                -0.468333740548211,
                -0.425931795686821,
                -0.277250413643514,
                -0.00633707991499456,
                -0.1126892387218,
                -0.0639986074249109,
                0.0286012595655832,
                -0.147957623220973,
                -0.255101334620177,
                0.218474734527578,
                -0.00291451940961424,
                -0.296861931409715,
                0
        };

        try
        {
            FileInputStream fid = new FileInputStream(filePath);
            DataInputStream data = new DataInputStream(fid);
            PrecomputedFilters.Initialize(11025,1024,1024,null,12);

            for (int i=0;i<1024;i++) {
                audioData[i] = data.readDouble();
                audioData[i] *= PrecomputedFilters.Hamming1K[i];
            }

            DoubleFFT_1D mFFT = new DoubleFFT_1D(1024);
            mFFT.realForwardFull(audioData);

            MFCC2 mfcc = new MFCC2(1024,1024,40,40,11025);

            double[] cepstrum = mfcc.cepstrum(audioData);

            assertArrayEquals(expected, cepstrum, 1e-6);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Test
    public void testMfcc2MelBank11K_ERBTriang() throws Exception
    {
        String filePath = "app\\src\\test\\res\\FIRST_FRAME_11K.DAT";
        double[] audioData = new double[2048];

        double[] expected = new double[]{
                -3.25383947512309,
                1.99850160273608,
                -0.25270826982222,
                -0.0609446278205521,
                -0.459655674945947,
                1.05930248381552,
                -1.17876365256555,
                1.0906752575247,
                0.627454835563332,
                -0.599397489612465,
                0.15979451895245,
                0.463082864311256,
                0.223959073276097,
                0.563820894094563,
                0.0758213688676933,
                -0.000585275073785204,
                -0.227795226632285,
                -0.645215131972512,
                -0.181443895968335,
                -0.028525357958476,
                0.0432144759805272,
                0.360850011285691,
                0.494501011300531,
                0.454536115055385,
                0.216657514835005,
                0.143228129180267,
                -0.0825758443394171,
                0.207407394765031,
                0.296043179774053,
                -0.151990274590821,
                -0.179515384631654,
                0.078339414413699,
                -0.155064501658493,
                -0.176919242608367,
                0.103825066126754,
                0.0437689044525382,
                0.087716577280785,
                0.00736899262723323,
                0.0478567315058712,
                0
        };

        try
        {
            FileInputStream fid = new FileInputStream(filePath);
            DataInputStream data = new DataInputStream(fid);
            PrecomputedFilters.Initialize(11025,1024,1024,null,12);

            for (int i=0;i<1024;i++) {
                audioData[i] = data.readDouble();
                audioData[i] *= PrecomputedFilters.Hamming1K[i];
            }

            DoubleFFT_1D mFFT = new DoubleFFT_1D(1024);
            mFFT.realForwardFull(audioData);

            MFCC2 mfcc = new MFCC2(1024, 1024, 40, 40, 11025, MFCC2.WindowMode.WIN_TRIANG, MFCC2.FreqScale.SCALE_ERB);

            double[] cepstrum = mfcc.cepstrum(audioData);

            assertArrayEquals(expected, cepstrum, 1e-6);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}