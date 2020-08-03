package com.fabianolibano.fen;

import java.io.Serializable;

public class CustomLocation implements Serializable
{
    private Double latitude;
    private Double longitude;

    public CustomLocation()
    {
        this.latitude = null;
        this.longitude = null;
    }

    public CustomLocation(Double latitude, Double longitude)
    {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude()
    {
        return this.latitude;
    }

    public Double getLongitude()
    {
        return this.longitude;
    }

    public void setLatitude(Double latitude)
    {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude)
    {
        this.longitude = longitude;
    }
}
