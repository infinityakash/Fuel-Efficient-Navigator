package com.fabianolibano.fen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import com.fabianolibano.fen.model.node;
import com.fabianolibano.fen.model.edge;


public class mapGraph
{
    Set<node> vertices;
    private double fuelConsumed, tripTimeMin;

    mapGraph()
    {
        vertices = new HashSet<>();
        fuelConsumed = -1;
        tripTimeMin = -1;
    }

    public void addNode(String location, double prob, double delay, int i)
    {
        vertices.add(new node(location, prob, delay, i));
    }

    public void addEdge(node u, node v, double dist, double time, int i)
    {
        u.edges.add(new edge(u, v, dist, time, i));
        v.edges.add(new edge(v, u, dist, time, i));
    }

    public boolean hasEdge(node src, node dst)
    {
        LinkedList<edge> edgeList = src.edges;

        for(edge e : edgeList)
        {
            if(e.getDst() == dst)
                return true;
        }
        return false;
    }

    public double getRouteTime()
    {
        return tripTimeMin;
    }

    public double getRouteConsumption()
    {
        return fuelConsumed;
    }

    public node getNode(int uuid)
    {
        for(node n: vertices)
        {
            if(n.getUuid() == uuid)
                return n;
        }
        return null;
    }

    public edge getEdge(int u, int v)
    {
        for(node n: vertices)
        {
            if(n.getUuid() == u)
            {
                for (edge e : n.edges)
                {
                    if(e.getDst().getUuid() == v)
                        return e;
                }
            }
        }

        return null;
    }

    /*
        Referenced the StackAbuse implementation of Dijkstra's algorithm:
        https://stackabuse.com/graphs-in-java-dijkstras-algorithm/
     */
    public LinkedList<node> runDijkstra(node start, node end, double idleGPM, double cityGPM)
    {
        LinkedList<node> res = new LinkedList<>();
        HashMap<node, node> parentMap = new HashMap<>();
        HashMap<node, Double> path = new HashMap<>();

        // Initialize the local weights
        for(node n : vertices)
        {
            if(n == start)
                path.put(start, 0.0);
            else
                path.put(n, Double.POSITIVE_INFINITY);
        }

        for(edge e : start.edges)
        {
            path.put(e.getDst(), (e.getWeight() * cityGPM) + (e.getDst().getWeight() * idleGPM));
            parentMap.put(e.getDst(), start);
        }

        start.setVisited(true);

        // Run dijksttras until all nodes have been visited
        while(true)
        {
            node curr = getClosestUnvisitedNode(path);

            // If a path does not exist
            if(curr == null)
                return null;

            // If the destination has been reached
            if(curr == end)
                break;

            // Visit this next node
            curr.setVisited(true);

            // Traverse the connected edges and attempt to find a better path
            for(edge e: curr.edges)
            {
                if(!e.getDst().isVisited())
                {
                    // Calculate weight
                    double weight = (e.getWeight()*cityGPM);
                    if(e.getDst() != end)
                        weight += (e.getDst().getWeight() * idleGPM);

                    if((path.get(curr) + weight) < path.get(e.getDst()))
                    {
                        path.put(e.getDst(), (path.get(curr) + weight));
                        parentMap.put(e.getDst(), curr);
                    }
                }
            }


        }

        // Build up a path of nodes;
        node child = end;
        res.add(end);
        tripTimeMin = end.getWeight();

        while(true)
        {
            node parent = parentMap.get(child);
            if(parent == null) {
                break;
            }
            else
            {
                tripTimeMin += getEdge(child.getUuid(), parent.getUuid()).getWeightTime();
                tripTimeMin += parent.getWeight();

                res.add(parent);
                child = parent;
            }
        }

        fuelConsumed = path.get(end);
        return res;
    }

    private node getClosestUnvisitedNode(HashMap<node, Double> path)
    {
        double sDist = Double.POSITIVE_INFINITY;
        node res = null;

        for(node n : vertices)
        {
            if(!n.isVisited())
            {
                double cDist = path.get(n);

                if(cDist < sDist)
                {
                    sDist = cDist;
                    res = n;
                }
            }
        }

        return res;
    }



}
