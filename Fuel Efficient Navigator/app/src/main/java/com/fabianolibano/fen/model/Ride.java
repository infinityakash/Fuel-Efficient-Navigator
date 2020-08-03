package com.fabianolibano.fen.model;

import com.fabianolibano.fen.CustomLocation;

import java.io.Serializable;
import java.util.LinkedList;

public class Ride implements Serializable
{
    public String origin;
    public String destination;
    public LinkedList<CustomLocation> route;
    public Double fuelConsumed;
    public Double estimatedTime;

    public Ride(String origin, String destination, LinkedList<CustomLocation> route, Double fuelConsumed, Double estimatedTime)
    {
        this.origin = origin;
        this.destination = destination;
        this.route = route;
        this.fuelConsumed = fuelConsumed;
        this.estimatedTime = estimatedTime;
    }

    public Ride()
    {
        this.origin = "";
        this.destination = "";
        this.route = new LinkedList<>();
        this.fuelConsumed = 0.0;
        this.estimatedTime = 0.0;
    }
}
