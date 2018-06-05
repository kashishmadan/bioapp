package com.bluemaestro.utility.sdk;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Downloader
{
    private int lastPointer;
    private int recordsNeeded;
    private int globalLogCount;
    private int sizePerRecord;
    private byte mode;

    private int thresholdNumber;
    private int[] thresholdTypes;
    private short[] thresholds;

    private int downloadCount;

    private volatile boolean downloading;
    private volatile boolean connected;
    private volatile boolean downloadFailure = true;
    private volatile boolean waitingForBluetooth = true;

    private int type;
    private int index;
    public double[][] data;

    /**
     * Convert two bytes to unsigned int 16
     *
     * @param first
     * @param second
     * @return
     */
    protected static final int convertToInt16(byte first, byte second)
    {
        int value = (int) first & 0xFF;
        value *= 256;
        value += (int) second & 0xFF;
        return value;
    }

    public boolean parseData(byte[] txValue)
    {
        if(txValue.length < 20 && txValue[txValue.length - 1] == ':')
        {
            this.lastPointer = convertToInt16(txValue[0], txValue[1]);
            this.recordsNeeded = convertToInt16(txValue[2], txValue[3]);
            this.globalLogCount = convertToInt16(txValue[4], txValue[5]);
            this.sizePerRecord = convertToInt16(txValue[6], txValue[7]);
            this.mode = txValue[8];

            this.thresholdNumber = (txValue[9] % 10 != 0 ? 1 : 0)
                    + ((txValue[9] / 10) % 10 != 0 ? 1 : 0);
            this.thresholdTypes = new int[2];
            thresholdTypes[0] = txValue[9] % 10;
            thresholdTypes[1] = (txValue[9] / 10) % 10;
            this.thresholds = new short[2];
            thresholds[0] = ByteBuffer.wrap(Arrays.copyOfRange(txValue, 10, 12))
                    .order(ByteOrder.BIG_ENDIAN).getShort();
            thresholds[1] = ByteBuffer.wrap(Arrays.copyOfRange(txValue, 12, 14))
                    .order(ByteOrder.BIG_ENDIAN).getShort();

            Log.d("BMDownloader", "Threshold No.: " + thresholdNumber);
            Log.d("BMDownloader", "Threshold Type: " + thresholdTypes[0] + " | " + thresholdTypes[1]);
            Log.d("BMDownloader", "Threshold: " + thresholds[0] + " | Threshold: " + thresholds[1]);

            Log.d("BMDownloader", "Last Pointer: " + lastPointer +
                    " | Records needed: " + recordsNeeded +
                    " | Global Log Count: " + globalLogCount +
                    " | Size per Record: " + sizePerRecord);
            this.downloadFailure = true;
            this.downloading = true;

            this.data = new double[10][recordsNeeded];
        } else
        {
            for(int i = 0; i < txValue.length; i += sizePerRecord)
            {

                if(txValue[i] == '.')
                {
                    this.downloadFailure = false;
                    this.downloading = false;
//                    mBMDatabase.completedDownload();
                    Log.d("BMDownloader", "Came across terminator . ");
                    return true;
                } else if(((txValue[i] == ',') && (txValue[i + 1] == ',')) || (this.index == recordsNeeded))
                {
                    //Log.d("BMDownloader", "Came across separator , now changing type.  Type value is : " + this.type + " and new type
                    // value will be : " + (this.type + 1));
                    i = txValue.length;
                    this.index = 0;
                    this.type += 1;
                    continue;
                }
                byte[] record = Arrays.copyOfRange(txValue, i, i + sizePerRecord);
                switch(sizePerRecord)
                {
                    case 1:
                        // Byte
                        data[type][index] = record[0];
                        index++;
//                        progessToUpdate.setProgress(index);
                        break;
                    case 2:
                        // Int (as Short)
                        data[type][index] = ByteBuffer.wrap(record).order(ByteOrder.BIG_ENDIAN).getShort();
                        //Log.d("BMDownloader", "Index value is : " + index);
                        index++;
//                        progessToUpdate.setProgress(index);
                        break;
                    case 4:
                        // Float
                        data[type][index] = ByteBuffer.wrap(record).order(ByteOrder.BIG_ENDIAN).getFloat();
                        index++;
//                        progessToUpdate.setProgress(index);
                        break;
                    default:
                        return true;

                }
            }
        }
        return false;
    }
}
