package com.telhai.spl.crydetector.test;

import org.junit.Test;

import java.io.DataInputStream;
import java.io.FileInputStream;

import com.telhai.spl.crydetector.PrecomputedFilters;

import static org.junit.Assert.*;

/**
 * Created by Dima Ruinskiy on 21/08/16.
 */
public class PrecomputedFiltersTest {

    @Test
    public void testLowPass() throws Exception {

        String filePath = "app\\src\\test\\res\\FIRST_FRAME.DAT";
        double[] audioData = new double[4096];

        double[] expect = new double[]{
                -1.5542630183594117E-8,
                1.9720668689855246E-8,
                -8.410610275888845E-9,
                2.12669672195108E-8,
                -1.4210905305371134E-8,
                9.928451125353017E-9,
                -2.5547787782036542E-8,
                5.795980595982799E-9,
                -1.7694206814968097E-8,
                2.3913011618843936E-8,
                1.4205165837442314E-9,
                3.120829913167545E-8,
                -1.1575085437727265E-8,
                1.0983214063198262E-9,
                -4.279875937309407E-8,
                -1.0045526913968333E-8,
                -2.090951821286996E-8,
                3.839302345797455E-8,
                4.32547662285901E-8,
                1.9014019735862232E-8,
                2.053583896015498E-8,
                -3.4418617448263017E-8,
                -8.00818993681154E-8,
                -2.2700217526907067E-8,
                -3.995004006701891E-8,
                6.538914219558581E-8,
                9.296713064195E-8,
                6.29888034011492E-8,
                3.0036852586936855E-8,
                -8.618000295722971E-8,
                -1.1120996312363732E-7,
                -1.2622497241443436E-7
        };

        System.out.println("Lowpass");

        try {
            FileInputStream fid = new FileInputStream(filePath);
            DataInputStream data = new DataInputStream(fid);

            for (int i = 0; i < 4096; i++) {
                audioData[i] = data.readDouble();
            }

            PrecomputedFilters.Initialize(44100, 4096, 4096, null, 1);  // only care about FFT length for this test

            double[] filtered = PrecomputedFilters.LowPass(audioData, true);

            for (int i = 0; i < 32; i++) {
                assertEquals(expect[i], filtered[i], 1e-15);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testLowPassAndPreEmphasis() throws Exception {

        String filePath = "app\\src\\test\\res\\FIRST_FRAME.DAT";
        double[] audioData = new double[4096];

        double[] expect = new double[]{
                -1.5542630183594117E-8,
                3.4486167364269655E-8,
                -2.7145245531251327E-8,
                2.92570469816052E-8,
                -3.4414524163906395E-8,
                2.3428811165455595E-8,
                -3.497981635112191E-8,
                3.006637898891751E-8,
                -2.3200388381151757E-8,
                4.072250809306363E-8,
                -2.1296844454157505E-8,
                2.985880837711843E-8,
                -4.1222969612818945E-8,
                1.2094652572160727E-8,
                -4.38421647090979E-8,
                3.061329449047103E-8,
                -1.1366267644600043E-8,
                5.825706576020101E-8,
                6.781393943514284E-9,
                -2.2078008181298366E-8,
                2.472520211085859E-9,
                -5.392766446041024E-8,
                -4.738421279226554E-8,
                5.337758687280256E-8,
                -1.83848334164572E-8,
                1.0334168025925379E-7,
                3.084744555614349E-8,
                -2.5329970708703295E-8,
                -2.9802510644154886E-8,
                -1.1471501291481972E-7,
                -2.9338960314269098E-8,
                -2.0575507446978912E-8
        };

        System.out.println("FPE");

        try {
            FileInputStream fid = new FileInputStream(filePath);
            DataInputStream data = new DataInputStream(fid);

            for (int i = 0; i < 4096; i++) {
                audioData[i] = data.readDouble();
            }

            PrecomputedFilters.Initialize(44100, 4096, 4096, null, 1);  // only care about FFT length for this test

            double[] filtered = PrecomputedFilters.PreEmphasis(PrecomputedFilters.LowPass(audioData,true),true);

            for (int i = 0; i < 32; i++) {
                assertEquals(expect[i], filtered[i], 1e-15);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}