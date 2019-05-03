package com.huawei;

import java.io.*;
import java.util.*;

public class Cross {
	private int id;
	private ArrayList<Integer> roads = new ArrayList<Integer>();
	private int[] sortRoads = null;

	public Cross(String crossStr) {
		crossStr = crossStr.substring(1, crossStr.length() - 1).replace(" ", "");
        String[] str = crossStr.split(",");
        this.id = Integer.parseInt(str[0]);
        for(int i = 1; i < str.length; i++) {
        	this.roads.add(Integer.parseInt(str[i]));
        }
        int[] temp = {Integer.parseInt(str[1]), Integer.parseInt(str[2]), Integer.parseInt(str[3]), Integer.parseInt(str[4])};
        this.sortRoads = temp;
        Arrays.sort(this.sortRoads);
	}

	public int id() {
		return this.id;
	}

	public ArrayList<Integer> roads() {
		return this.roads;
	}

	public int[] sortRoads() {
		return this.sortRoads;
	}

	public String toString() {
		return "Cross [Id=" + id + ", roads=" + roads + ", sortRoads=" + Arrays.toString(sortRoads) + "]";
	}
}