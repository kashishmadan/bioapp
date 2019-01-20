package com.bluemaestro.utility.sdk.retrofit;

public class WebService
{
    // web services
    public static final String ADD_SENSOR_URL = "/sensor/add";


    private int status;
    private String response;

    public WebService(int status, String response) {
        this.status = status;
        this.response = response;
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    public String getResponse()
    {
        return response;
    }

    public void setResponse(String response)
    {
        this.response = response;
    }

    public String toString() {
        return this.status + ": " + this.response;
    }
}
