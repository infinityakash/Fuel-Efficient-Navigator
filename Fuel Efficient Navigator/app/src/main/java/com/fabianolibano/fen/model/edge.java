package com.fabianolibano.fen.model;

import android.support.annotation.NonNull;

public class edge implements Comparable<edge>
{
    private node src, dst;
    private double weightDist, weightTime;
    private int uuid;

    public edge(node s, node d, double dist, double time, int i)
    {
        src = s;
        dst = d;
        weightDist = dist;
        weightTime = time;
        uuid = i;
    }

    @Override
    public int compareTo(@NonNull edge edge)
    {
        if(this.weightDist > edge.getWeight())
            return 1;
        else
            return -1;
    }

    public node getSrc() {
        return src;
    }

    public node getDst() {
        return dst;
    }

    public double getWeight() {
        return weightDist;
    }

    public double getWeightTime() {
        return weightTime;
    }

    public int getUuid() {
        return uuid;
    }
}
