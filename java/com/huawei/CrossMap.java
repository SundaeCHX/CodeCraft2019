package com.huawei;

import java.io.*;
import java.util.*;

public class CrossMap {
    private HashMap<Integer, Bag<Road>> adj;

    public CrossMap(ArrayList<Road> roads) {
        adj = new HashMap<Integer, Bag<Road>>();
        for(Road road : roads) {
            adj.put(road.from(), new Bag<Road>());
        }
    }

    public void addRoad(Road road) {
        Bag<Road> bag = new Bag<Road>();
        bag = adj.get(road.from());
        bag.add(road);
        adj.put(road.from(), bag);
    }

    public Road find(int i, int j) {
        for(Road road : adj.get(i)) {
            if(road.to() == j) {
                return road;
            }
        }
        System.out.println("No way from " + i + "to " + j + "!");
        return null;
    }
}