package com.huawei;

import java.io.*;
import java.util.*;

public class Road {
    private int id;
    private int from;
    private int to;
    private int isDuplex;
    private double pri;
    private double averageSpeed;
    private ArrayList<Integer> speeds;
    private int[] weight;

    private ArrayList<LinkedList<Car>> fromToRoad = new ArrayList<LinkedList<Car>>();
    private ArrayList<LinkedList<Car>> toFromRoad = new ArrayList<LinkedList<Car>>();
    private ArrayList<Integer> runList            = new ArrayList<Integer>();
    private int[] fromToFirstCar = {-1, -1, -1};
    private int[] toFromFirstCar = {-1, -1, -1};
    public  int[] carsNum        = {0, 0};
    private boolean waitingCar   = false;
    private int volume;

    public Road() {

    }

    public Road(String roadStr) {
        roadStr           = roadStr.substring(1, roadStr.length() - 1).replace(" ", "");
        String[] str      = roadStr.split(",");
        this.id           = Integer.parseInt(str[0]);
        this.from         = Integer.parseInt(str[4]);
        this.to           = Integer.parseInt(str[5]);
        this.isDuplex     = Integer.parseInt(str[6]);
        this.weight       = new int[4];
        this.weight[0]    = Integer.parseInt(str[1]);  //length
        this.weight[1]    = Integer.parseInt(str[2]);  //speed
        this.weight[2]    = Integer.parseInt(str[3]);  //channel
        this.weight[3]    = 0;                         //carNumber
        this.pri          = (double) this.weight[0] / this.weight[1];
        this.averageSpeed = this.weight[1];
        this.speeds       = new ArrayList<Integer>();
        this.volume       = this.weight[0] * this.weight[2];

        this.fromToRoad = new ArrayList<>();
        for(int i = 0; i < this.weight[2]; i++) {
            this.fromToRoad.add(new LinkedList<>());
        }
        if(this.isDuplex == 1) {
            this.toFromRoad = new ArrayList<>();
            for(int i = 0; i < this.weight[2]; i++) {
                this.toFromRoad.add(new LinkedList<>());
            }
        }
    }

    public Road copyRoad() {
        Road road         = new Road();
        road.id           = this.id;
        road.from         = this.to;
        road.to           = this.from;
        road.weight       = new int[4];
        road.weight[0]    = this.weight[0];
        road.weight[1]    = this.weight[1];
        road.weight[2]    = this.weight[2];
        road.weight[3]    = this.weight[3];
        road.pri          = this.pri;
        road.averageSpeed = this.weight[1];
        road.speeds       = new ArrayList<Integer>();
        return road;
    }

    public void setPri(Integer carSpeed) {
        this.pri = (double) this.weight[0] / carSpeed;
        this.averageSpeed = carSpeed;
    }

    public int from() {
        return this.from;
    }

    public int to() {
        return this.to;
    }

    public int id() {
        return this.id;
    }

    public int isDuplex() {
        return this.isDuplex;
    }

    public double pri() {
        return this.pri;
    }

    public int speed() {
        return this.weight[1];
    }

    public int length() {
        return this.weight[0];
    }

    public int channel() {
        return this.weight[2];
    }

    public ArrayList<Integer> runList() {
        return this.runList;
    }

    public boolean hasWaitingCar() {
        return this.waitingCar;
    }

    public void setWaitingCarState(boolean state) {
        this.waitingCar = state;
    }

    public String toString() {
        //return "Road [ID=" + id + ", From=" + from + ", To=" + to + ", Length=" + weight[0] + ", Speed=" + weight[1] + ", Channel=" + weight[2] + "]";
        return id + " ";
    }

    //this.weight[0]    length
    //this.weight[1]    speed
    //this.weight[2]    channel
    //this.weight[3]    carNumber
    public void addCarNum(int carSpeed, int priType) {
        if(priType == 0) {
            int speed = this.weight[1] < carSpeed ? this.weight[1] : carSpeed;
            if(this.weight[3] == 0)
                this.averageSpeed = (double) speed;
            else
                this.averageSpeed = (this.averageSpeed * this.weight[3] + speed) / (this.weight[3] + 1);
            this.weight[3] = this.weight[3] + 1;
            int tmp = speed * this.weight[2];
            
            if(this.weight[3] < tmp) 
                this.pri = (double) (this.weight[0] / this.averageSpeed);
            else
                this.pri = this.pri + (double) this.weight[3] / tmp + 1;
                
            //this.pri = (int) (this.weight[0] / this.averageSpeed) + this.weight[3] / tmp;
            this.speeds.add(speed);
        } else if(priType == 1) {
            this.weight[3] = this.weight[3] + 1;
            int speed = this.weight[1] < carSpeed ? this.weight[1] : carSpeed;
            int tmp = speed * this.weight[2];
            
            if(this.weight[3] < tmp) 
                this.pri = this.pri;
            else
                this.pri = this.pri + (double) this.weight[3] / tmp + 1;
            
            //this.pri = this.weight[0] / speed + this.weight[3] / tmp;
            this.speeds.add(this.weight[1]);
        } else {
            this.weight[3] = this.weight[3] + 1;
            int speed = this.weight[1] < carSpeed ? this.weight[1] : carSpeed;
            this.pri = (double) this.weight[0] / speed + (double) this.weight[3] / this.weight[2];
            this.speeds.add(speed);       
        }
    }

    public int getDirection(Car car, HashMap<Integer, Road> preRoads, HashMap<Integer, Cross> preCrosses) {
        int nextRoadId = car.nextRoad();
        Road nextRoad = preRoads.get(nextRoadId);
        int crossId;
        if(this.from() == nextRoad.from() || this.from() == nextRoad.to())
            crossId = this.from();
        else
            crossId = this.to();
        Cross cross = preCrosses.get(crossId);
        int roadIndex = cross.roads().indexOf(this.id());
        int nextRoadIndex = cross.roads().indexOf(nextRoadId);

        if(nextRoadIndex == (roadIndex + 1) % 4)
            return 1;
        else if(nextRoadIndex == (roadIndex + 2) % 4)
            return 0;
        else
            return 2;
    }

    public ArrayList<LinkedList<Car>> getRoadMatrix(int direction) {
        if(direction == 0)
            return this.fromToRoad;
        else
            return this.toFromRoad;
    }

    public boolean runToRoad(Car car, HashMap<Integer, Road> preRoads, HashMap<Integer, Cross> preCrosses, HashMap<String, Integer> statisticsInfo) {
        ArrayList<LinkedList<Car>> roadMatrix;
        if(car.from() == this.from())
            roadMatrix = this.fromToRoad;
        else
            roadMatrix = this.toFromRoad;
        int nextRoadId;
        if(car.initFlag() == 0) {
            car.setCarSpeed(Math.min(this.weight[1], car.speed()));
            if(car.planRoad().size() > 1) {
                nextRoadId = car.planRoad().get(1);
                car.setNextRoad(nextRoadId);
                car.setNextRoadSpeed(preRoads.get(nextRoadId).speed());
                car.setDirection(this.getDirection(car, preRoads, preCrosses));
            } else {
                car.setNextRoad(-1);
                car.setNextRoadSpeed(-1);
                car.setDirection(0);
            }
            car.setInitFlag(1);
        }
        int roadChannel;
        for(roadChannel = 0; roadChannel < this.channel(); roadChannel++) {
            if(roadMatrix.get(roadChannel).size() == 0) {
                roadMatrix.get(roadChannel).add(car);
                car.setCarPosition(car.carSpeed() - 1 );
                car.setCarChannel(roadChannel);
                car.setOnRoad(1);
                car.setCarState(2);
                statisticsInfo.put("RunningCarNum", statisticsInfo.get("RunningCarNum") + 1);
                statisticsInfo.put("DepartCarNum", statisticsInfo.get("DepartCarNum") + 1);
                return true;
            }
            Car frontCar = roadMatrix.get(roadChannel).get(0);
            if(frontCar.carPosition() == 0) {
                if(frontCar.carState() == 1)
                    return false;
                else {
                    if(roadChannel == this.channel() - 1) {
                        return false;
                    } else {
                        continue;
                    }
                }
            } else if(car.carSpeed() > frontCar.carPosition()) {
                if(frontCar.carState() == 1)
                    return false;
                else {
                    roadMatrix.get(roadChannel).add(0, car);
                    car.setCarPosition(frontCar.carPosition() - 1);
                    car.setCarChannel(roadChannel);
                    car.setOnRoad(1);
                    car.setCarState(2);
                    statisticsInfo.put("RunningCarNum", statisticsInfo.get("RunningCarNum") + 1);
                    statisticsInfo.put("DepartCarNum", statisticsInfo.get("DepartCarNum") + 1);
                    return true;
                }
            } else {
                roadMatrix.get(roadChannel).add(0, car);
                car.setCarPosition(car.carSpeed() - 1 );
                car.setCarChannel(roadChannel);
                car.setOnRoad(1);
                car.setCarState(2);
                statisticsInfo.put("RunningCarNum", statisticsInfo.get("RunningCarNum") + 1);
                statisticsInfo.put("DepartCarNum", statisticsInfo.get("DepartCarNum") + 1);
                return true;
            }
        }
        System.out.println("Error!");
        return false;
    }

    @SuppressWarnings("unchecked")
    public void runCarInRunList(HashMap<Integer, Car> preCars, HashMap<Integer, Road> preRoads, HashMap<Integer, Cross> preCrosses,
        int time, boolean priority, HashMap<String, Integer> statisticsInfo, int direction) {
        ArrayList<Integer> runList = this.runList;
        ArrayList<Integer> runListCopy = (ArrayList<Integer>)runList.clone();
        if(runList.size() == 0)
            return;
        int carId;
        Car car;
        for(int i = 0; i < runListCopy.size(); i++) {
            carId = runListCopy.get(i);
            car = preCars.get(carId);
            if(priority == true && car.priority() == 0)
                break;
            if(direction != -1) {
                if(direction == 0) {
                    if(car.from() != this.from)
                        continue;
                } else {
                    if(car.from() != this.to)
                        continue;
                }
            }
            if(time < car.actualTime())
                continue;
            if(this.runToRoad(car, preRoads, preCrosses, statisticsInfo)) {
                int dir;
                if(direction == -1) {
                    if(car.from() == this.from)
                        dir = 0;
                    else
                        dir = 1;
                } else {
                    dir = direction;
                }
                this.carsNum[dir]++;
                runList.remove((Integer)carId);

            }
        }
    }

    public void creatSequeue(int direction) {
        ArrayList<LinkedList<Car>> roadMatrix;
        if(direction == 0)
            roadMatrix = this.fromToRoad;
        else
            roadMatrix = this.toFromRoad;
        ArrayList<Car> channelFirstCarList = new ArrayList<Car>();
        Car car;
        for(int i = 0; i < this.channel(); i++) {
            if(roadMatrix.get(i).size() == 0)
                continue;
            else {
                int index = roadMatrix.get(i).size() - 1;
                car = roadMatrix.get(i).get(index);
                if(car.carState() == 1 && car.priority() == 1) {
                    if(direction == 0)
                        this.fromToFirstCar = new int[] {car.id(), car.carChannel(), car.carPosition()};
                    else
                        this.toFromFirstCar = new int[] {car.id(), car.carChannel(), car.carPosition()};
                    return;
                }
                channelFirstCarList.add(car);
            }
        }
        if(channelFirstCarList.size() == 0) {
            if(direction == 0)
                this.fromToFirstCar = new int[] {-1, -1, -1};
            else
                this.toFromFirstCar = new int[] {-1, -1, -1};
            return;
        }
        Car firstCar = null;
        for(int i = 0; i < channelFirstCarList.size(); i++) {
            car = channelFirstCarList.get(i);
            if(car.carState() == 1) {
                if(firstCar == null)
                    firstCar = car;
                else {
                    if(car.carPosition() > firstCar.carPosition())
                        firstCar = car;
                }
            }
        }
        if(firstCar == null) {
            if(direction == 0)
                this.fromToFirstCar = new int[] {-1, -1, -1};
            else
                this.toFromFirstCar = new int[] {-1, -1, -1};
            return;
        } else {
            car = firstCar;
            if(direction == 0)
                this.fromToFirstCar = new int[] {car.id(), car.carChannel(), car.carPosition()};
            else
                this.toFromFirstCar = new int[] {car.id(), car.carChannel(), car.carPosition()};
            return;
        }
    }

    public int[] getCarSequeue(int direction) {
        if(direction == 0)
            return this.fromToFirstCar;
        else
            return this.toFromFirstCar;
    }

    public ArrayList<Integer> getRunList() {
        return this.runList;
    }

}