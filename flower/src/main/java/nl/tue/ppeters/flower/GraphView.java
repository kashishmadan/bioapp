/**
 * Here the data received by the Shimmer is send to the filter, received from the filter and translated into the Flower and the Graph
 * graphical depictions.
 * It is an adaptation of the original GraphView of the original Shimmer app
 * The variable 'value' is the actual measured resistance by the Shimmer. In order to determine whether or not the app is working properly
 * one can attach a potentio meter to the Shimmer device, the set resistance should than equal 'value'.
 * <p/>
 * In the end the flower that is draw is based on two values, the Tonic value and the amount of peaks/sec (Phasic value).
 * The tonic value determines the size of the flower and the amount of petals are determined by the peaks/sec
 */

//**HIER WORDT DE GRAFISCHE WEERGAVE VAN DE VERGAARDE DATA AANGEGEVEN
//**NORMAAL WAS DIT EEN GRAFIEK, MAAR DIT KUNNEN WE DUS ZELF AANPASSEN

package nl.tue.ppeters.flower;
/*
  Copyright (c) 2009 Bonifaz Kaufmann.

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/


import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
//import shimmer.ShimmerLogExample.R;


public class GraphView extends View {

    boolean mFirstWrite = true;
    boolean stop = false;
    String myAddress = "Device 1";
    String[] signalNameArray = new String[4];
    double[] dataValues = new double[4];

    Timer mTimer;
    private Bitmap mBitmap;
    private Paint mPaint = new Paint();
    private Paint mPaintText = new Paint();
    private Paint PaintOne = new Paint();
    private Paint PaintTwo = new Paint();
    private Paint PaintThree = new Paint();
    private Canvas mCanvas = new Canvas();
    private float mSpeed = 1.0f;
    private float mLastX;
    private float mScale;
    private float[] mLastValue = new float[3];
    private float mYOffset;
    private int[] mColor = new int[3];
    private float mWidth;
    private float maxValue = 1024f;

    private float[] myValues;
    private ArrayList<Float> myValuesArray; //Array of Tonic values
    private ArrayList<Float> myValuesPhasicArray; //Array of Phasic Values
    private ArrayList<Float> myCirclesArray; //Array of Peak locations
    private ArrayList<Float> myPeaksArray; // Array of amount of peaks

    public float mTempV;
    public float yvalueOld;
    public float ref = 1;
    public int x = 1;
    public boolean graphViewed;
    public String buttonColor;
    public double A = 300;
    public double AA = 300;
    public double B;
    public double diff;
    public float yvalue;
    public float radius = 1;
    public static float getradius;
    public float oldRadius = 1;
    public float newRadius = 1;
    public String text = "200";
    public String text2 = "200";
    public String text3 = "200";
    public String text4 = "200";
    public int alphaLevelLeaves1 = 0;
    public int alphaLevelLeaves1Max;
    public int alphaLevelLeaves2 = 0;
    public int alphaLevelLeaves2Max;
    public int alphaLevelLeaves3 = 0;
    public int counter = 0;

    float valueNoise;
    float valueTonic;
    float valuePhasic;

    public int msg = 1;
    public int y;
    public int z;
    public int inputLength = 1;

    //KYRA DEZE MOET JE AANPASSEN VOOR JE PROTOTYPE
    int kyra = 25;

    public float total = 0;
    public int teller = 1;
    public float average;
    public float rawAverage = 0;

    //Filter variables
    public float tonicOutput = 0;
    public float phasicOutput = 0;
    public float output50Hz = 0;
    public float output = 0;
    public float input = 0;

    public String peakStatus = "up";
    public String peakStatusOud = "up";
    public float circleValue;

    public float peak;
    public int peakCount;

    public float startTime; //these times are used to determing the time passed in order to determin the amount of peaks/sec
    public float endTime;

    public int L = 1;
    //private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;

    //ShimmerGraph shimmergraph = new ShimmerGraph();

    public GraphView(Context context) {
        super(context);
        init();
    }

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        //Determine the colors used

        //mColor = Color.argb(192, 64, 128, 64);
        mColor[0] = Color.argb(255, 100, 255, 100); // g
        mColor[1] = Color.argb(255, 255, 255, 100); // y
        mColor[2] = Color.argb(255, 255, 100, 100); // r
        //mColor[x] = Color.argb(255, 100, 255, 255); // c
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        //graphics.setRenderingHints(Graphics2D.ANTIALIASING,Graphics2D.ANTIALIAS_ON);
        PaintOne.setColor(0xFFFF9900); // orange
        PaintTwo.setColor(0xFFFFFF00); // yellow
        PaintThree.setColor(0xFF99FF00); // green

        PaintOne.setAlpha(90);
        PaintTwo.setAlpha(110);
        PaintThree.setAlpha(130);

        myValuesArray = new ArrayList<Float>();
        myValuesPhasicArray = new ArrayList<Float>();
        myCirclesArray = new ArrayList<Float>();
    }

    //Here a dataPoint is added to the array of datapoints
    public void setData(float value) {
        addDataPoint(value, mColor[0], mLastValue[0], 0);
        invalidate();
    }

    public void setData(int[] values, String deviceID) {
        final int length = values.length;
        final Paint paintText = mPaintText;
        paintText.setColor(Color.argb(255, 0, 255, 255));
        //mCanvas.drawText(deviceID, 5, 10, mPaintText);
        try {
            for (int i = 0; i < length; i++) {
                addDataPoint(values[i], mColor[i % 3], mLastValue[i], i);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            /* mLastValue might run into this in extreme situations */
            // but then we just do not want to support more than 3 values in our graph
        }
        invalidate();
    }

    //Here the width of the graph is determined
    public void setDataWithAdjustment(int[] values, String deviceID, String dataType) {
        final int length = values.length;

        final Paint paintText = mPaintText;
        int offset = 0;
        if (dataType == "u8") {
            setMaxValue(255);
            offset = 0;
        } else if (dataType == "i8") {
            setMaxValue(255);
            offset = 127;
        } //center the graph, so the negative values will be displayed
        else if (dataType == "u12") {
            setMaxValue(4095);
            offset = 0;
        } else if (dataType == "u16") {
            setMaxValue(65535);
            offset = 0;
        } else if (dataType == "i16") {
            setMaxValue(4095);
            offset = 2047;
        }        // it is actually a signed 12bit value for magnetometer

        paintText.setColor(Color.argb(255, 255, 255, 255));
        //mCanvas.drawText(deviceID, 5, 10, mPaintText); // draw title and deviceID
        try {
            for (int i = 0; i < length; i++) {
                addDataPoint(values[i] + offset, mColor[i % 3], mLastValue[i], i);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            /* mLastValue might run into this in extreme situations */
            // but then we just do not want to support more than 3 values in our graph
        }
        invalidate();
    }

    private boolean logOutlier(float value, final int pos) {
        if (teller == 1) rawAverage = value;
        else if (teller < 100) rawAverage = (value + (rawAverage * (teller - 1))) / teller;
        else rawAverage = (value + (rawAverage * 99)) / 100;
        if ((value < (0.5 * rawAverage)) || (value > (1.5 * rawAverage))) {
            Log.d("Outlier", "" + pos + ":" + value + ", " + rawAverage);
            return true;
        }
        return false;
    }

    //float value is the actual resistance measured by the shimmer device
    private void addDataPoint(float value, final int color, final float lastValue, final int pos) {
        Log.d("Data value", "" + value);
        //System.out.println("value: "+value);
        float rawValue = value; //rawValue is saved and later on printed to the txt file as the raw data received by the shimmer device, so don't calculate with rawValue
        // remove outliers
        if (logOutlier(value, pos)) if (FlowerActivity.viewOutlier()) value = rawAverage;
        value = -value; //Reverse value in order to ensure the graph is printed above the mid-line instead of below, this is due to the fact that on the app 0,0 is top left and not bottom left

        final Paint paint = mPaint;
        float newX = mLastX + mSpeed;
        final float v = mYOffset + value * mScale;

        //System.out.println("v: "+v);
        //System.out.println("average: "+average);


        //call the filters
/////FILTERING/////
        if (input == 0) {
            input = value;
        }
        if (output == 0) {
            output = value;
        }
        if (tonicOutput == 0) {
            tonicOutput = value;
        }
        if (phasicOutput == 0) {
            phasicOutput = value;
        }
        if (output50Hz == 0) {
            output50Hz = value;
        }


        Filters filters = new Filters();
        filters.filterData(value, output50Hz, input, output, tonicOutput, phasicOutput);

        //receive filtered data
        input = filters.getInput();
        output = filters.getOutput();
        output50Hz = filters.get50Hz();
        tonicOutput = filters.getTonic();
        phasicOutput = filters.getPhasic();


        valueNoise = output;
        valueTonic = tonicOutput;
        valuePhasic = phasicOutput;

        //shift graph to fit the screen
        valuePhasic = valuePhasic * 350; //was *100 voor meting vingers en 350 voor meting met volwassen sok
        valueTonic = valueTonic * 100; //was *25 voor meting met vingers en 100 voor meting met volwassen sok


        float time = System.nanoTime();

        //System.out.println("Tonic Output: "+tonicOutput);
        //System.out.println("Tonic Output *25: "+valueTonic);
        //System.out.println("Phasic Output: "+phasicOutput);
        //System.out.println("50Hz Output: "+output50Hz);

/////FILTERING/////


        int maxSize = 1000;

        //Phasic data array
        if (myValuesArray.size() == maxSize) {
            myValuesArray.remove(0);
            myValuesPhasicArray.remove(0);


            myValuesArray.add(maxSize - 1, valueTonic);
            myValuesPhasicArray.add(maxSize - 1, valuePhasic);


        } else if (myValuesArray.size() < maxSize) {
            myValuesArray.add(valueTonic);
            myValuesPhasicArray.add(valuePhasic);


        }


        Float[] myValues = new Float[myValuesArray.size()];
        Float[] myValuesPhasic = new Float[myValuesPhasicArray.size()];


        for (int i = 0; i < myValuesArray.size(); i++) {
            myValues[i] = myValuesArray.get(i);
            myValuesPhasic[i] = myValuesPhasicArray.get(i);

        }


        ////PEAK DETECTION/////
        peakCount = 0;
        for (int i = myValuesPhasicArray.size() - 60; i < myValuesPhasicArray.size(); i++) {


            if (i >= myValuesPhasicArray.size() - 59) {

                if (myValuesPhasic[i - 1] <= -5000) {
                    circleValue = myValuesPhasic[i - 1];

                    //System.out.println("----------");
                    //System.out.println("peakfound!");
                    //System.out.println("----------");
                    peakCount++;
                    //System.out.println("peakCount= " +peakCount);


                } else {
                    circleValue = 0;

                }

            }
        }
////PEAK DETECTION/////


////CALCULATE PEAKS PER Sec//////


        float peakPerSec = peakCount;
        //System.out.println("peakPerSec: "+peakPerSec);

////CALCULATE PEAKS PER Sec//////


        if (myCirclesArray.size() == maxSize) {
            myCirclesArray.remove(0);
            myCirclesArray.add(maxSize - 1, circleValue);
        } else if (myCirclesArray.size() < maxSize) {
            myCirclesArray.add(circleValue);

        }

        Float[] myCircles = new Float[myCirclesArray.size()];

        for (int i = 0; i < myCirclesArray.size(); i++) {
            myCircles[i] = myCirclesArray.get(i);
        }


        // smooths
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);


        float xpos = mCanvas.getWidth() / 2;
        float ypos = mCanvas.getHeight() / 2;

        paint.setTextSize(30);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);


        ////AVERAGE////

        //Calcualte the average in order to have a reference point
        total = valueTonic + total;
        average = total / teller;
        teller++;

        ////AVERAGE////


        //radius = Math.abs(average - valueTonic);
        radius = -valueTonic / kyra;
        // System.out.println("radius= " +radius);

        mCanvas.drawRect(new RectF(0, 0, mWidth, mYOffset), mPaint);


        //Make sure the flower stays visible and fit to the screen
        if (radius >= 400) {
            radius = 400;

        }
        if (radius <= 20) {
            radius = 20;

        }

        if (FlowerActivity.viewFlower()) {
            getradius = radius; //getradius is send to BluetoothService in order to send messages to the arduino, see getRadius()

            //Draw the flower
            paint.setStrokeWidth(0);
            if (FlowerActivity.viewGraph()) {
                radius = radius / 2;
                ypos = ypos + mCanvas.getHeight() / 4;
            } else {
                PaintOne.setAlpha(90);
                alphaLevelLeaves1Max = 90;
                PaintTwo.setAlpha(110);
                alphaLevelLeaves2Max = 110;
                PaintThree.setAlpha(130);
            }

            int interval = 1;

            // orange leaves

            //Slowly make the orange leaves grow relative to the amount of peaks/sec
            if (peakPerSec > 2) {
                alphaLevelLeaves1 = alphaLevelLeaves1 + interval;

                if (alphaLevelLeaves1 >= alphaLevelLeaves1Max) {
                    alphaLevelLeaves1 = alphaLevelLeaves1Max;
                }
            }
            //Slowly make the yellow leaves shrink relative to the amount of peaks/sec
            else if (peakPerSec < 2) {
                alphaLevelLeaves1 = alphaLevelLeaves1 - interval;
                if (alphaLevelLeaves1 <= 0) {
                    alphaLevelLeaves1 = 0;
                }
            }

            mCanvas.save();
            PaintOne.setAlpha(alphaLevelLeaves1);

            mCanvas.rotate(54, xpos, ypos);
            RectF Bounds3 = new RectF(xpos - 10 - (5 * radius / 6), ypos - (radius / 4), xpos - 10, ypos + (radius / 4));
            mCanvas.drawOval(Bounds3, PaintOne);

            mCanvas.rotate(72, xpos, ypos);
            mCanvas.drawOval(Bounds3, PaintOne);

            mCanvas.rotate(72, xpos, ypos);
            mCanvas.drawOval(Bounds3, PaintOne);

            mCanvas.rotate(72, xpos, ypos);
            mCanvas.drawOval(Bounds3, PaintOne);

            mCanvas.rotate(72, xpos, ypos);
            mCanvas.drawOval(Bounds3, PaintOne);
            mCanvas.restore();

            // yellow leaves
            if (peakPerSec > 1) {
                alphaLevelLeaves2 = alphaLevelLeaves2 + interval;

                //Slowly make the yellow leaves grow relative to the amount of peaks/sec
                if (alphaLevelLeaves2 >= alphaLevelLeaves2Max) {
                    alphaLevelLeaves2 = alphaLevelLeaves2Max;
                }
            }
            //Slowly make the yellow leaves shrink relative to the amount of peaks/sec
            else if (peakPerSec < 1) {
                alphaLevelLeaves2 = alphaLevelLeaves2 - interval;
                if (alphaLevelLeaves2 <= 0) {
                    alphaLevelLeaves2 = 0;
                }
            }


            mCanvas.save();
            PaintTwo.setAlpha(alphaLevelLeaves2);
            mCanvas.rotate(54, xpos, ypos);

            RectF Bounds2 = new RectF(xpos - 10 - (4 * radius / 5), ypos - (radius / 8), xpos - 10, ypos + (radius / 8));
            mCanvas.drawOval(Bounds2, PaintTwo);

            mCanvas.rotate(72, xpos, ypos);
            mCanvas.drawOval(Bounds2, PaintTwo);

            mCanvas.rotate(72, xpos, ypos);
            mCanvas.drawOval(Bounds2, PaintTwo);

            mCanvas.rotate(72, xpos, ypos);
            mCanvas.drawOval(Bounds2, PaintTwo);

            mCanvas.rotate(72, xpos, ypos);
            mCanvas.drawOval(Bounds2, PaintTwo);

            mCanvas.restore();

            //green leaves
            mCanvas.save();
            mCanvas.rotate(36 / 2, xpos, ypos);
            RectF Bounds = new RectF(xpos - 10 - (3 * radius / 4), ypos - (radius / 5), xpos - 10, ypos + (radius / 5));
            mCanvas.drawOval(Bounds, PaintThree);

            mCanvas.rotate(72, xpos, ypos);
            mCanvas.drawOval(Bounds, PaintThree);

            mCanvas.rotate(72, xpos, ypos);
            mCanvas.drawOval(Bounds, PaintThree);

            mCanvas.rotate(72, xpos, ypos);
            mCanvas.drawOval(Bounds, PaintThree);

            mCanvas.rotate(72, xpos, ypos);
            mCanvas.drawOval(Bounds, PaintThree);

            mCanvas.restore();

            ypos = mCanvas.getHeight() / 2;
//            int length = myValues.length;
//            int size = myValuesArray.size();
//            int val = Math.round(valueTonic);
//            String string = Integer.toString(val);
//            String string2 = Integer.toString(length);
//            String string3 = Integer.toString(size);
            mPaint.setColor(Color.BLACK);


            mTempV = v;
            mLastValue[pos] = v;
            if (pos == 0)
                mLastX += mSpeed;
            mLastX = 0;
        }

        //***HERE THE VARIOUS VARIABLES ARE SAVED TO THE TEXT FILE
        //public void safeData(){
        try {
            Log.d("Downloader", "Write to file");
            File root = Environment.getExternalStorageDirectory();
            //SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String currentDateandTime = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
            String fileName = getContext().getString(R.string.app_name) + "/" + getContext().getString(R.string.app_name_flower) + currentDateandTime + ".txt";
            File outputFile = new File(root, fileName);
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true));

            buttonColor = FlowerActivity.getButtonColor();

            //Draw measurements on the screen
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            mCanvas.drawText(text, 100, 100, mPaint);

            paint.setColor(Color.BLACK);
            text = Float.toString(rawValue);
            mCanvas.drawText(text, 100, 100, mPaint);

            boolean verbose = false;
            //writer = new BufferedWriter(new FileWriter(outputFile,false));
            writer.newLine();
            if (verbose) writer.append("rawValue,");
            writer.append(Float.toString(rawValue));
            if (FlowerActivity.viewOutlier()) {
                writer.append(",");
                if (verbose) writer.append("rawAverage,");
                writer.append(Float.toString(rawAverage));
            }
            writer.append(",");
            if (verbose) writer.append("average,");
            writer.append(Float.toString(average));
            writer.append(",");
            if (verbose) writer.append("output (Noise),");
            writer.append(Float.toString(output));
            writer.append(",");
            if (verbose) writer.append("Tonic Ouput,");
            writer.append(Float.toString(tonicOutput));
            writer.append(",");
            if (verbose) writer.append("Phasic Ouput,");
            writer.append(Float.toString(phasicOutput));
            writer.append(",");
            if (verbose) writer.append("peakCount,");
            writer.append(Float.toString(peakCount));
            writer.append(",");
            //writer.append("elapsedTimeSec,");
            //writer.append(Float.toString(elapsedTimeSec));
            //writer.append(",");
            if (verbose) writer.append("buttonColor,");
            writer.append(buttonColor);
            writer.append(",");
            if (verbose) writer.append("peakPerSec,");
            writer.append(Float.toString(peakPerSec));
            writer.append(",");
            if (verbose) writer.append("radius,");
            writer.append(Float.toString(radius));
            writer.append(",");
            if (verbose) writer.append("Time,");
            writer.append(currentDateandTime);
            //writer.newLine();
            writer.close();
        } catch (Exception e) {

            Log.d("Downloader", e.getMessage());
        }

        //graphView = ShimmerGraph.Access(graphView); ik access deze eerder
        int x = 0;
        int start = Math.round(myValues.length - mWidth); // starting point for drawing the graph
        if (start < 0) {
            start = 10;
        }


        //if graph button is selected, draw the graph
        if (FlowerActivity.viewGraph()) {
            paint.setColor(0xFF99FF00);
            mCanvas.drawLine(0, ypos, mWidth, ypos, mPaint); //middle line graph
            paint.setAlpha(100);

            mCanvas.drawLine(0, ypos - 25, 10, ypos - 25, mPaint); // other graph lines above
            mCanvas.drawLine(0, ypos - 50, mWidth, ypos - 50, mPaint);
            mCanvas.drawLine(0, ypos - 75, 10, ypos - 75, mPaint);
            mCanvas.drawLine(0, ypos - 100, mWidth, ypos - 100, mPaint);
            mCanvas.drawLine(0, ypos - 150, mWidth, ypos - 150, mPaint);
            mCanvas.drawLine(0, ypos - 200, mWidth, ypos - 200, mPaint);

	    	/*mCanvas.drawLine(0, ypos + 25, 10, ypos + 25, mPaint); // other graph lines below
            mCanvas.drawLine(0, ypos + 50, mWidth, ypos + 50, mPaint);
	    	mCanvas.drawLine(0, ypos + 75, 10, ypos + 75, mPaint);
	    	mCanvas.drawLine(0, ypos + 100, mWidth, ypos + 100, mPaint);
	    	mCanvas.drawLine(0, ypos + 150, mWidth, ypos + 150, mPaint);
	    	mCanvas.drawLine(0, ypos + 200, mWidth, ypos + 200, mPaint);*/
            paint.setAlpha(255);


            paint.setColor(0xFFFF9900);

            for (int i = start; i < myValues.length; i++) {
                //Float average1 = (myValues[i-3]+myValues[i-2])/2;
                //Float average2 = (myValues[i-1]+myValues[i])/2;
                int y1 = Math.round(myValues[i] / 50 + (ypos - 0)); //-8900 / -150
                int y2 = Math.round(myValues[i + 1] / 50 + (ypos - 0));

	    		/*
	    		int yT1 = Math.round(myValuesTonic[i]/50 + (ypos - 0)); //-8900 / -150
	    		int yT2 = Math.round(myValuesTonic[i+1]/50 + (ypos - 0));
	    		*/
                int yP1 = Math.round(myValuesPhasic[i] / 50 + (ypos - 0)); //-8900 / -150
                int yP2 = Math.round(myValuesPhasic[i + 1] / 50 + (ypos - 0));
                paint.setColor(0xFFFF9900); //set color to orange
                mCanvas.drawLine(x, y1, x + 1, y2, mPaint); //Draw line between i and i+1 to extend graph

                paint.setColor(0xFF99FF00); //set color to green
                mCanvas.drawLine(x, yP1, x + 1, yP2, mPaint); //Draw line between i and i+1 to extend Phasicgraph

                if (myCircles[i] != 0) {
                    int yC = Math.round(myCircles[i] / 50 + (ypos - 0)); //-8900 / -150
                    mCanvas.drawCircle(x, yC, 10, mPaint);
                }
                x++; //move graph one point on x-axis
            }
        }
    }


    //This instance is called by BluetoothSevice in order to aquire the radius
    public static float getRadius() {
        return getradius;
    }

    public void setMaxValue(int max) {
        maxValue = max;
        mScale = -(mYOffset * (1.0f / maxValue));
    }

    public void setmYoffset(int m) {
        mYOffset = m;
    }

    public void setSpeed(float speed) {
        mSpeed = speed;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        mCanvas.setBitmap(mBitmap);
        mCanvas.drawColor(0xFFFFFFFF);
        mYOffset = h;
        mScale = -(mYOffset * (1.0f / maxValue));
        mWidth = w;
        mLastX = mWidth;
        super.onSizeChanged(w, h, oldw, oldh);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        synchronized (this) {
            if (mBitmap != null) {
                if (mLastX >= mWidth) {
                    mLastX = 0;
                    final Canvas cavas = mCanvas;

                    //DisplayMetrics metrics = new DisplayMetrics();
                    //getMetrics(metrics);
                    //canvas.drawRect(0,0, metrics.widthPixels, metrics.heightPixels, mLoadPaint);

                    Shader shader = new LinearGradient(0, 0, 500, 900, 0xFFFFFF99, Color.WHITE, TileMode.CLAMP);
                    Paint paint = new Paint();
                    paint.setShader(shader);
                    //cavas.drawRect(new RectF(0,0,mWidth,mYOffset), paint);

                    cavas.drawColor(0xFFFFFFFF); //set backgroundcolour
                    //mPaint.setColor(0xFF777777);
                    mPaint.setColor(0xFF444444);
                    cavas.drawLine(0, mYOffset, mWidth, mYOffset, mPaint);


                }
                canvas.drawBitmap(mBitmap, 0, 0, null);
            }
        }
    }
}
