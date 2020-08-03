package com.fabianolibano.fen.model;
import com.fabianolibano.fen.CustomLocation;

import java.util.LinkedList;

public class node
{
    private int uuid;
    private double weight;
    private CustomLocation loc;
    private boolean isVisited;
    public LinkedList<edge> edges;

    public node(String location, double prob, double delay, int i)
    {
        uuid = i;
        weight = prob * (delay/60.0);
        loc = new CustomLocation(Double.valueOf(location.substring(0, 9)), Double.valueOf(location.substring(11)));
        isVisited = false;
        edges = new LinkedList<>();
    }

    public int getUuid() {
        return uuid;
    }

    public double getWeight() {
        return weight;
    }

    public CustomLocation getLoc() {
        return loc;
    }

    public boolean isVisited() {
        return isVisited;
    }

    public void setVisited(boolean visited) {
        isVisited = visited;
    }
}
