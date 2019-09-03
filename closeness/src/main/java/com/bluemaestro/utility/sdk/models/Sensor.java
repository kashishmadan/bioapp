package com.bluemaestro.utility.sdk.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

import static com.bluemaestro.utility.sdk.database.Database.TIMESTAMP_DEVICE_FIELD;

@Parcel
public class Sensor
{
    @Expose
    private Float temperature;

//    @SerializedName(LATITUDE_FIELD)
    @Expose
    private double latitude;

//    @SerializedName(LATITUDE_FIELD)
    @Expose
    private double longitude;

    @SerializedName(TIMESTAMP_DEVICE_FIELD)
    @Expose
    private String timestamp;

    @ParcelConstructor
    public Sensor()
    {
    }

    public Sensor(Float temperature, String timestamp, double latitude, double longitude)
    {
        this.temperature = temperature;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Float getTemperature()
    {
        return temperature;
    }

    public void setTemperature(Float temperature)
    {
        this.temperature = temperature;
    }

    public String getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(String timestamp)
    {
        this.timestamp = timestamp;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

    @Override
    public String toString()
    {
        return "Sensor{" +
                "temperature=" + temperature +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
