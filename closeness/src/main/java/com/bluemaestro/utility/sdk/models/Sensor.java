package com.bluemaestro.utility.sdk.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

import static com.bluemaestro.utility.sdk.database.Database.IS_PARTNER_CLOSE_FIELD;
import static com.bluemaestro.utility.sdk.database.Database.TIMESTAMP_DEVICE_FIELD;

@Parcel
public class Sensor
{
    @Expose
    private Float temperature;

    @SerializedName(IS_PARTNER_CLOSE_FIELD)
    @Expose
    private boolean isPartnerClose;

    @SerializedName(TIMESTAMP_DEVICE_FIELD)
    @Expose
    private String timestamp;

    @ParcelConstructor
    public Sensor()
    {
    }

    public Sensor(Float temperature, boolean isPartnerClose, String timestamp)
    {
        this.temperature = temperature;
        this.isPartnerClose = isPartnerClose;
        this.timestamp = timestamp;
    }

    public Float getTemperature()
    {
        return temperature;
    }

    public void setTemperature(Float temperature)
    {
        this.temperature = temperature;
    }

    public boolean isPartnerClose()
    {
        return isPartnerClose;
    }

    public void setPartnerClose(boolean partnerClose)
    {
        isPartnerClose = partnerClose;
    }

    public String getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(String timestamp)
    {
        this.timestamp = timestamp;
    }

    @Override
    public String toString()
    {
        return "Sensor{" +
                "temperature=" + temperature +
                ", isPartnerClose=" + isPartnerClose +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
