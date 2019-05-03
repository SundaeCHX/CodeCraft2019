package com.huawei;

import java.io.*;
import java.util.*;

public class Car {
    private int id;
    private int from;
    private int to;
    private int speed;
    private int planTime;
    private int runTime;
    private int totalTime;
    private int startTime;
    private int priority;
    private int preset;
    private int actualTime;
    private int planRoadIndex           = 0;
    private int carSpeed                = 0;
    private int onRoad                  = 0;
    private int carState                = 0;
    private int direction               = 0;
    private int nextRoad                = 0;
    private int nextRoadSpeed           = 0;
    private int initFlag                = 0;
    private int canChange               = 0;
    private int carPosition             = -1;
    private int carChannel              = -1;
    private boolean realPlanTime        = false;
    private ArrayList<Integer> planRoad = null;

    public Car(String carStr) {
        carStr          = carStr.substring(1, carStr.length() - 1).replace(" ", "");
        String[] str    = carStr.split(",");
        this.id         = Integer.parseInt(str[0]);
        this.from       = Integer.parseInt(str[1]);
        this.to         = Integer.parseInt(str[2]);
        this.speed      = Integer.parseInt(str[3]);
        this.planTime   = Integer.parseInt(str[4]);
        this.priority   = Integer.parseInt(str[5]);
        this.preset     = Integer.parseInt(str[6]);
        this.runTime    = 0;
        this.totalTime  = 0;
        this.canChange  = 0;
        this.startTime  = this.planTime;
        this.actualTime = this.planTime;
    }

    public int canChange() {
        return this.canChange;
    }

    public void setCanChange() {
        this.canChange = 1;
    }

    public void cleanTime() {
        this.startTime  = this.planTime;
        this.actualTime = this.planTime;
    }

    public int id() {
        return this.id;
    }

    public int from() {
        return this.from;
    }

    public int to() {
        return this.to;
    }

    public int speed() {
        return this.speed;
    }

    public int planTime() {
        return this.planTime;
    }

    public int priority() {
        return this.priority;
    }

    public int preset() {
        return this.preset;
    }

    public int runTime() {
        return this.runTime;
    }

    public int totalTime() {
        return this.totalTime;
    }

    public void setTotalTime(int time) {
        this.totalTime = time;
    }

    public int startTime() {
        return this.startTime;
    }

    public void setStartTime(int time) {
        this.startTime = time;
    }

    public void setPlanTime(int time) {
        this.planTime = time;
    }

    public int actualTime() {
        return this.actualTime;
    }

    public void setActualTime(int time) {
        this.actualTime = time;
    }

    public void setPlanRoad(ArrayList<Integer> planRoad) {
        this.planRoad = planRoad;
    }

    public int nextRoad() {
        return this.nextRoad;
    }

    public void setNextRoad(int id) {
        this.nextRoad = id;
    }

    public void setNextRoadSpeed(int speed) {
        this.nextRoadSpeed = speed;
    }

    public int initFlag() {
        return this.initFlag;
    }

    public void setInitFlag(int flag) {
        this.initFlag = flag;
    }

    public ArrayList<Integer> planRoad() {
        return this.planRoad;
    }

    public int carSpeed() {
        return this.carSpeed;
    }

    public void setCarSpeed(int speed) {
        this.carSpeed = speed;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int direction() {
        return this.direction;
    }

    public int carPosition() {
        return this.carPosition;
    }

    public void setCarPosition(int i) {
        this.carPosition = i;
    }

    public int carChannel() {
        return this.carChannel;
    }

    public void setCarChannel(int i) {
        this.carChannel = i;
    }

    public void setOnRoad(int i) {
        this.onRoad = i;
    }

    public void setCarState(int i) {
        this.carState = i;
    }

    public int carState() {
        return this.carState;
    }

    public int planRoadIndex() {
        return this.planRoadIndex;
    }

    public void planRoadIndexAdd() {
        this.planRoadIndex = this.planRoadIndex + 1;
    }

    public void setRealPlanTime(boolean i) {
        this.realPlanTime = i;
    }

    public String toString() {
        //return "Car [Id=" + id + ", PlanTime=" + planTime + ", RunTime =" + runTime + ", speed=" + speed + ", Priority =" + priority + "]";
        return id + " ";
        //return "Car [Id=" + id + ", PlanTime=" + planTime + ", from=" + from + "]";
    }

    public ArrayList<Road> showRoad(MatrixFloyd floyd, CrossMap map, ArrayList<Integer> crosses) {
        return floyd.findRoad(map, this.from, this.to, crosses);
    }

}