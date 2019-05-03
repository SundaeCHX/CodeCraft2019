package com.huawei;

import org.apache.log4j.Logger;
import java.io.*;
import java.util.*;

public class Main {
    private static int carMaxSpeed           = 0;    //所有车辆最大车速
    private static int vipCarNum             = 0;    //优先级车车辆数（不含预置优先级车）
    private static int carNum                = 0;    //普通车车辆数（不含预置车）
    private static int v                     = 0;    //路口数量
    private static int changeBusNum          = 0;    //可改变路径的预置车辆数
    private static int maxBusesTime          = 0;    //预置车辆最晚发车时间
    private static int maxChangeVipBusesTime = 0;    //可改变预置车辆最晚发车时间
    private static ArrayList<Integer> crosses                   = new ArrayList<>();                        //所有路口ID集合
    private static HashMap<Integer, Cross> preCrosses           = new HashMap<Integer, Cross>();            //所有路口哈希表，Key值为ID
    private static HashMap<Integer, ArrayList<Car>> fromVipCar  = new HashMap<Integer, ArrayList<Car>>();   //所有优先级车（不含预置优先级车）哈希表，Key值为起点路口ID
    private static HashMap<Integer, ArrayList<Car>> fromCar     = new HashMap<Integer, ArrayList<Car>>();   //所有普通车（不含预置级车）哈希表，Key值为起点路口ID
    private static ArrayList<Car> cars                          = new ArrayList<>();                        //所有普通车（不含预置车）集合
    private static ArrayList<Car> vipCars                       = new ArrayList<>();                        //所有优先级车（不含预置优先级车）集合
    private static ArrayList<Car> vipBuses                      = new ArrayList<>();                        //所有预置优先级车集合
    private static ArrayList<Car> changeVipBuses                = new ArrayList<>();                        //所有可改变预置优先级车集合
    private static ArrayList<Integer> buses                     = new ArrayList<>();                        //所有预置车ID集合
    private static ArrayList<Integer> vips                      = new ArrayList<>();                        //所有优先级车ID集合
    private static HashMap<Integer, Car> busesId                = new HashMap<Integer, Car>();              //所有预置车哈希表，Key值为ID
    private static HashMap<Integer, Car> preCars                = new HashMap<Integer, Car>();              //所有车辆哈希表，Key值为ID
    private static ArrayList<Road> roads                        = new ArrayList<>();                        //所有道路集合
    private static HashMap<Integer, Road> preRoads              = new HashMap<Integer, Road>();             //所有道路哈希表，Key值为道路ID
    private static HashMap<Integer, ArrayList<Road>> busesRoads = new HashMap<Integer, ArrayList<Road>>();  //所有预置车路径哈希表，Key值为预置车ID，Value值为路径集合
    private static HashMap<Integer, ArrayList<Car>> busesTime   = new HashMap<Integer, ArrayList<Car>>();   //所有预置车发车哈希表，Key值为出发时间，Value值为预置车集合
    private static ArrayList<String> answer                     = new ArrayList<String>();                  //路径规划结果
    private static CrossMap map;
    private static MatrixFloyd floyd;

    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args)
    {
        long start = System.currentTimeMillis();
        if (args.length != 5) {
            logger.error("please input args: inputFilePath, resultFilePath");
            return;
        }

        logger.info("Start...");

        String carPath = args[0];
        String roadPath = args[1];
        String crossPath = args[2];
        String presetAnswerPath = args[3];
        String answerPath = args[4];
        logger.info("carPath = " + carPath + " roadPath = " + roadPath + " crossPath = " + crossPath + " presetAnswerPath = " + presetAnswerPath + " and answerPath = " + answerPath);

        // TODO:read input files
        logger.info("start read input files");

        //showCarSort(vipCars);
        // TODO: calc

        int totalCarsNums;      //每秒发车数
        int onlyCarsNums;       //预置车发完后每4秒发车数
        int score;              //分数

        totalCarsNums = 150;
        onlyCarsNums  = totalCarsNums * 4;

        //调度前准备，读取文件信息，车辆排序，更改部分预置优先级车辆出发时间等
        ready(carPath, roadPath, crossPath, presetAnswerPath);
        
        System.out.println("----------------------------------------------------------------------------------");
        System.out.println("启动调度器！！！！！！！！！");
        System.out.println("totalCarsNums: " + totalCarsNums + "  onlyCarsNums: " +  onlyCarsNums);

        //调度方法，发车策略，路径规划
        startRunMap(totalCarsNums, onlyCarsNums);
        
        //启动判题器，返回判读成绩，若发生死锁则返回-1
        score = startDispatch();

        //自动调参，若发生死锁，则减少每秒发车辆重新开始调度
        while(score == -1) {
            totalCarsNums = totalCarsNums - 5;
            onlyCarsNums  = totalCarsNums * 4;
            reboot(carPath, roadPath, crossPath, presetAnswerPath);
            System.out.println("----------------------------------------------------------------------------------");
            System.out.println("启动调度器！！！！！！！！！");
            System.out.println("totalCarsNums: " + totalCarsNums + "  onlyCarsNums: " +  onlyCarsNums);
            startRunMap(totalCarsNums, onlyCarsNums);
            score = startDispatch();
        }
        
        if(totalCarsNums >= 110)
            totalCarsNums = totalCarsNums - 5;
        else
            totalCarsNums = totalCarsNums;
        
        onlyCarsNums  = totalCarsNums * 4;
        reboot(carPath, roadPath, crossPath, presetAnswerPath);
        System.out.println("----------------------------------------------------------------------------------");
        System.out.println("启动调度器！！！！！！！！！");
        System.out.println("totalCarsNums: " + totalCarsNums + "  onlyCarsNums: " +  onlyCarsNums);
        startRunMap(totalCarsNums, onlyCarsNums);
        score = startDispatch();
        while(score == -1) {
            totalCarsNums = totalCarsNums;
            onlyCarsNums  = onlyCarsNums - 10;
            reboot(carPath, roadPath, crossPath, presetAnswerPath);
            System.out.println("----------------------------------------------------------------------------------");
            System.out.println("启动调度器！！！！！！！！！");
            System.out.println("totalCarsNums: " + totalCarsNums + "  onlyCarsNums: " +  onlyCarsNums);
            startRunMap(totalCarsNums, onlyCarsNums);
            score = startDispatch();
        }
        
        // TODO: write answer.txt
        logger.info("Start write output file");
        System.out.println("totalCarsNums: " + totalCarsNums + "  onlyCarsNums: " +  onlyCarsNums);

        //输出最终路径规划结果
        showAnswer(answerPath);

        //showBusesTime();
        //changeBusTime();
        //showBusesTime();
        //showCarSort(vipCars);
        logger.info("End...");
        long end = System.currentTimeMillis();
        System.out.println("The running time of the program is " + (end - start) + "ms");
    }

    public static void startRunMap(int totalCarsNums, int onlyCarsNums) {
        int temp       = 0;     //普通车每秒实际发车数
        int vipTemp    = 0;     //优先级车每秒实际发车数
        int carsNum    = 0;     //普通车每秒理论发车数
        int vipCarsNum = 0;     //优先级车每秒理论发车数
        int time       = 1;     //时间轴
        int timeFlag   = 0;     //优先级车截止时间标志
        int m          = 0;     //预置车辆影响时间区间
        int avgCarsNum = 0;     //有预置车辆影响情况下每秒发车总数
        int vipCarFlag = 0;     //上一优先级车发车时间
        int carFlag    = 0;     //上一普通车发车时间
        int sortFlag   = 0;     //预置车截止标志
        int priType    = 2;     //权重函数选择

        while(true) {
            if(busesTime.containsKey(time)) {
                for(Car bus : busesTime.get(time)) {
                    //复赛时更改部分预置优先级车辆路径采用以下方法，决赛时更改车辆出发时间无法更改路径，因此无需使用下述方法
                    /*
                    if(bus.canChange() == 1) {
                        ArrayList<Road> busRoads = new ArrayList<Road>();
                        busRoads = bus.showRoad(floyd, map, crosses);
                        ArrayList<Integer> busAnswer = new ArrayList<>();                            
                        for(Road way : busRoads) {
                            way.addCarNum(bus.speed(), priType);
                            busAnswer.add(way.id());
                        }
                        busAnswer.add(0, bus.id());
                        busAnswer.add(1, bus.startTime());
                        String str = busAnswer.toString().replace("[", "(").replace("]", ")");
                        answer.add(str);
                    } else {
                        ArrayList<Road> busRoads = busesRoads.get(bus.id()); 
                        for(Road busWay : busRoads) {
                            busWay.addCarNum(bus.speed(), priType);
                        }
                    }
                    */

                    //改变预置车辆路径经过道路的权重
                    if(busesTime.get(time).size() != 0) {
                        ArrayList<Road> busRoads = busesRoads.get(bus.id()); 
                        for(Road busWay : busRoads) {
                            busWay.addCarNum(bus.speed(), priType);
                        }
                    }
                }

                //引入预置车辆发车数对其他车辆发车数的影响
                m = time + busesTime.get(time).size() / 20;
                avgCarsNum = totalCarsNums - 20;
                int n = busesTime.get(time).size() % 80 == 0 ? busesTime.get(time).size() / 80 : busesTime.get(time).size() / 80 + 1;
                if(n > 1) {
                    time = time + n;
                    continue;
                }
            }

            //计算该时刻优先级车辆发车数
            if(vipCars.size() != 0) {
                if(busesTime.containsKey(time)) {
                    if(totalCarsNums > busesTime.get(time).size())
                        vipCarsNum = totalCarsNums - busesTime.get(time).size();
                    else {
                        vipCarsNum = 0;
                        break;
                    }
                } else {
                    if(time <= m)
                        vipCarsNum = avgCarsNum;
                    else
                        vipCarsNum = totalCarsNums;
                }
            }

            //优先级车辆路径规划
            while (vipCars.size() != 0) {
                int i = 0;
                ArrayList<Road> vipCarRoads = new ArrayList<Road>();                  
                if(time < vipCars.get(i).startTime()) {
                    vipCarsNum = vipTemp;
                    vipTemp = 0;
                    break;
                } else {
                    if(vipCars.get(i).startTime() <= vipCarFlag) {
                        vipTemp++;
                        vipCars.get(i).setStartTime(time);
                        if(vipTemp == vipCarsNum + 1) {
                            vipCarsNum = vipTemp - 1;
                            vipTemp = 0;
                            break;
                        }
                    } else {
                        if(vipCars.get(i).startTime() <= time) {
                            vipTemp++;
                            vipCars.get(i).setStartTime(time);
                        } else {
                            vipCarsNum = vipTemp;
                            vipTemp = 0;
                            break;
                        }
                    }     
                }
                vipCarRoads = vipCars.get(i).showRoad(floyd, map, crosses);
                ArrayList<Integer> vipCarAnswer = new ArrayList<>();                            
                for(Road way : vipCarRoads) {
                    way.addCarNum(vipCars.get(i).speed(), priType);
                    vipCarAnswer.add(way.id());
                }

                vipCarAnswer.add(0, vipCars.get(i).id());
                vipCarAnswer.add(1, vipCars.get(i).startTime());
                String str = vipCarAnswer.toString().replace("[", "(").replace("]", ")");
                answer.add(str);
                vipCarFlag = vipCars.get(i).startTime();
                vipCars.remove(i);
            }

            //计算该时刻普通车辆发车数
            if(vipCars.size() > 0) {
                if(busesTime.containsKey(time)) {
                    if(totalCarsNums > busesTime.get(time).size())
                        carsNum = totalCarsNums - busesTime.get(time).size() - vipCarsNum;
                    else {
                        carsNum = 0;
                    }
                } else {
                    if(time <= m)
                        carsNum = avgCarsNum - vipCarsNum;
                    else
                        carsNum = totalCarsNums - vipCarsNum;
                }
            }  
            if(timeFlag == 0 && vipCars.size() == 0) {
                timeFlag = time;
                carsNum = 0;
            } 
            if(time > timeFlag && vipCars.size() == 0) {
                vipCarsNum = 0;
                if(busesTime.containsKey(time)) {
                    if(totalCarsNums > busesTime.get(time).size())
                        carsNum = totalCarsNums - busesTime.get(time).size();
                    else {
                        carsNum = 0;
                    }
                } else {
                    if(time <= m) 
                        carsNum = avgCarsNum;
                    else
                        carsNum = totalCarsNums;

                    if(time > maxBusesTime && sortFlag == 0) {
                        sortFlag = 1;
                        time = time + 4;
                    }
                    if(time > maxBusesTime && sortFlag == 1) {
                        carsNum = onlyCarsNums;                       
                    }
                }
            }
            //answer.add("# time: " + time + " vipCarsNum: " + vipCarsNum + " needs carsNum: " + carsNum);

            //普通车辆路径规划
            while(cars.size() != 0) {
                if(carsNum == 0)
                    break;
                int j = 0;
                ArrayList<Road> carRoads = new ArrayList<Road>();
                if(time < cars.get(j).startTime()) {
                    break;
                } else {
                    if(cars.get(j).startTime() <= carFlag) {
                        temp++;
                        cars.get(j).setStartTime(time);
                        if(temp == carsNum + 1) {
                            carsNum = temp - 1;
                            temp = 0;
                            break;
                        }
                    } else {
                        if(cars.get(j).startTime() <= time) {
                            temp++;
                            cars.get(j).setStartTime(time);
                        } else {
                            carsNum = temp;
                            temp = 0;
                            break;
                        }
                    }     
                }
                carRoads = cars.get(j).showRoad(floyd, map, crosses);
                ArrayList<Integer> carAnswer = new ArrayList<>();                            
                for(Road way : carRoads) {
                    way.addCarNum(cars.get(j).speed(), priType);
                    carAnswer.add(way.id());
                }

                carAnswer.add(0, cars.get(j).id());
                carAnswer.add(1, cars.get(j).startTime());
                String str = carAnswer.toString().replace("[", "(").replace("]", ")");
                answer.add(str);
                carFlag = cars.get(j).startTime();
                cars.remove(j);
            }
            //answer.add("# carsNum: " + carsNum);

            //由于道路权重改变，更新Floyd
            floyd.update(roads, crosses);
            if(vipCars.size() == 0 && cars.size() == 0) {
                break;
            }
            if(sortFlag == 0)
                time++;
            else
                time = time + 4;
        }
    }

    //启动判题器，对调度方法输出的路径规划进行判读，返回系统成绩，若发生死锁则返回-1
    public static int startDispatch() {
        int score;
        Dispatch dispatch;
        try {
            readAnswer();
            dispatch = new Dispatch(Main.preCars, Main.preRoads, Main.preCrosses, Main.vips, Main.buses);
            score = dispatch.dispatch();
        } catch(Exception e) {
            e.printStackTrace();
            score = -1;
        }
        return score;
    }

    //系统重启方法，若系统调度发生死锁，重新启动调度
    public static void reboot(String carPath, String roadPath, String crossPath, String presetAnswerPath) {
        Main.carMaxSpeed  = 0;
        Main.vipCarNum    = 0;
        Main.carNum       = 0;
        Main.v            = 0;
        Main.changeBusNum = 0;
        Main.maxBusesTime = 0;
        Main.maxChangeVipBusesTime = 0;
        Main.crosses.clear();
        Main.preCrosses.clear();
        Main.fromVipCar.clear();
        Main.fromCar.clear();
        Main.cars.clear();
        Main.vipCars.clear();
        Main.vipBuses.clear();
        Main.changeVipBuses.clear();
        Main.buses.clear();
        Main.vips.clear();
        Main.busesId.clear();
        Main.preCars.clear();
        Main.roads.clear();
        Main.preRoads.clear();
        Main.busesRoads.clear();
        Main.busesTime.clear();
        Main.answer.clear();
        ready(carPath, roadPath, crossPath, presetAnswerPath);
    }

    //调度前准备工作，包括读取文件，生成Floyd，车辆排序，改变部分预置优先级车辆出发时间
    public static void ready(String carPath, String roadPath, String crossPath, String presetAnswerPath) {
        readCross(crossPath);
        readCar(carPath);
        readRoad(roadPath);   
        readPresetAnswer(presetAnswerPath);
        v = crosses.size();
        changeBusNum = buses.size() / 10;
        maxBusesTime = maxBusesTime();

        while(vipCars.size() != vipCarNum) {
            for(Map.Entry<Integer, ArrayList<Car>> entry : fromVipCar.entrySet()) {
                if(entry.getValue().size() > 0) {
                    vipCars.add(entry.getValue().get(0));
                    entry.getValue().remove(0);
                }
            }
        }

        while(cars.size() != carNum) {
            for(Map.Entry<Integer, ArrayList<Car>> entry : fromCar.entrySet()) {
                if(entry.getValue().size() > 0) {
                    cars.add(entry.getValue().get(0));
                    entry.getValue().remove(0);
                }
            }
        }

        map = new CrossMap(roads);
        for(Road road : roads) {
            map.addRoad(road);
        }

        floyd = new MatrixFloyd(v, roads, crosses);
        carsSort(vipCars, 1);
        carsSort(cars, 1);
        setChangeBus();
        changeVipBusTime();
    }

    //确定更改哪些预置优先级车辆的出发时间
    public static void setChangeBus() {
        for(Car bus : vipBuses) {
            ArrayList<Road> busRoads = busesRoads.get(bus.id());
            int busTotalTime = 0;
            for(Road busWay : busRoads) {
                if(busWay.speed() < bus.speed()) {
                    busTotalTime = busTotalTime + (busWay.length() % busWay.speed() == 0 ? busWay.length() / busWay.speed() : busWay.length() / busWay.speed() + 1);
                } else {
                    busTotalTime = busTotalTime + (busWay.length() % bus.speed() == 0 ? busWay.length() / bus.speed() : busWay.length() / bus.speed() + 1);
                }
            }
            bus.setTotalTime(busTotalTime);
        }
        carsSort(vipBuses, 2);
        if(vipBuses.size() > changeBusNum) {
            for(int i = 0; i < changeBusNum; i++) {
                vipBuses.get(i).setCanChange();
                changeVipBuses.add(vipBuses.get(i));
                if(vipBuses.get(i).startTime() > maxChangeVipBusesTime)
                    maxChangeVipBusesTime = vipBuses.get(i).startTime();
            }
        } else {
            for(int i = 0; i < vipBuses.size(); i++) {
                vipBuses.get(i).setCanChange();
                changeVipBuses.add(vipBuses.get(i));
                if(vipBuses.get(i).startTime() > maxChangeVipBusesTime)
                    maxChangeVipBusesTime = vipBuses.get(i).startTime();
            }
        }
    }

    //对于确定改变的预置优先级车辆，更改其出发时间
    public static void changeVipBusTime() {
        for(Map.Entry<Integer, ArrayList<Car>> entry : busesTime.entrySet()) {
            ArrayList<Car> temp = entry.getValue();
            for(int i = temp.size() - 1; i >= 0; i--) {
                if(temp.get(i).canChange() == 1) {
                    temp.remove(i);
                }
            }
            busesTime.put(entry.getKey(), temp);
        }
        int i = 0;
        int j = maxChangeVipBusesTime;
        for(Car car : changeVipBuses) {
            car.setPlanTime(j);
            car.setStartTime(j);
            car.setActualTime(j);
            i++;
            if(i == 35) {
                i = 0;
                j++;
            }
        }
        for(Car car : changeVipBuses) {
            if(!busesTime.containsKey(car.startTime())) {
                ArrayList<Car> tempBuses = new ArrayList<Car>();
                tempBuses.add(car);
                busesTime.put(car.startTime(), tempBuses);
            } else {
                ArrayList<Car> tempBuses = busesTime.get(car.startTime());
                tempBuses.add(car);
                busesTime.put(car.startTime(), tempBuses);
            }
        }
    }

    //读取路口信息文件
    public static void readCross(String crossPath) {
        try {
            FileInputStream inputCross = new FileInputStream(crossPath);
            BufferedReader crossReader = new BufferedReader(new InputStreamReader(inputCross));
            String crossStr = null;
            //crossReader.readLine();
            while ((crossStr = crossReader.readLine()) != null) {
                if(crossStr.contains("#"))
                    continue;
                Cross cross = new Cross(crossStr);
                crosses.add(cross.id());
                preCrosses.put(cross.id(), cross);
                fromVipCar.put(cross.id(), new ArrayList<>());
                fromCar.put(cross.id(), new ArrayList<>());
            }
            crossReader.close();
            inputCross.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //读取车辆信息文件
    public static void readCar(String carPath) {
        try {
            FileInputStream inputCar = new FileInputStream(carPath);
            BufferedReader carReader = new BufferedReader(new InputStreamReader(inputCar));
            String carStr = null;
            //carReader.readLine();
            while ((carStr = carReader.readLine()) != null) {
                if(carStr.contains("#"))
                    continue;
                Car car= new Car(carStr);
                preCars.put(car.id(), car);
                if(carMaxSpeed < car.speed())
                    carMaxSpeed = car.speed();
                if(car.preset() == 1) {
                    busesId.put(car.id(), car);
                    buses.add(car.id());
                    if(car.priority() == 1) {
                        vips.add(car.id());
                        vipBuses.add(car);
                    }
                } else if(car.priority() == 1){
                    //vipCars.add(car);
                    vips.add(car.id());
                    vipCarNum++;
                    ArrayList<Car> fromcars = fromVipCar.get(car.from());
                    fromcars.add(car);
                    fromVipCar.put(car.from(), fromcars);
                } else {
                    //cars.add(car);
                    carNum++;
                    ArrayList<Car> fromcars = fromCar.get(car.from());
                    fromcars.add(car);
                    fromCar.put(car.from(), fromcars);
                }
            }
            carReader.close();
            inputCar.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //读取道路信息文件
    public static void readRoad(String roadPath) {
        try {
            FileInputStream inputRoad = new FileInputStream(roadPath);
            BufferedReader roadReader = new BufferedReader(new InputStreamReader(inputRoad));
            String roadStr = null;
            //roadReader.readLine();
            while ((roadStr = roadReader.readLine()) != null) {
                if(roadStr.contains("#"))
                    continue;
                Road road = new Road(roadStr);
                if(road.speed() > carMaxSpeed)
                    road.setPri(carMaxSpeed);
                roads.add(road);
                preRoads.put(road.id(), road);
                if(road.isDuplex() == 1) {
                    Road copy = road.copyRoad();
                    roads.add(copy);
                }
            }
            roadReader.close();
            inputRoad.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //读取预置车辆信息文件
    public static void readPresetAnswer(String presetAnswerPath) {
        try {
            FileInputStream inputPreset = new FileInputStream(presetAnswerPath);
            BufferedReader presetReader = new BufferedReader(new InputStreamReader(inputPreset));
            String presetStr = null;
            //presetReader.readLine();
            while ((presetStr = presetReader.readLine()) != null) {
                if(presetStr.contains("#"))
                    continue;
                presetStr = presetStr.substring(1, presetStr.length() - 1).replace(" ", "");
                String[] str = presetStr.split(",");
                int carId = Integer.parseInt(str[0]);
                int carStartTime = Integer.parseInt(str[1]);
                ArrayList<Integer> planRoad = new ArrayList<Integer>();
                for(int i = 2; i < str.length; i++) {
                    planRoad.add(Integer.parseInt(str[i]));
                }
                if(busesId.containsKey(carId)) {
                    busesId.get(carId).setStartTime(carStartTime);
                    busesId.get(carId).setPlanTime(carStartTime);
                    busesId.get(carId).setActualTime(carStartTime);
                    busesId.get(carId).setPlanRoad(planRoad);
                }
            
                if(!busesTime.containsKey(carStartTime)) {
                    ArrayList<Car> tempBuses = new ArrayList<Car>();
                    tempBuses.add(busesId.get(carId));
                    busesTime.put(carStartTime, tempBuses);
                } else {
                    ArrayList<Car> tempBuses = busesTime.get(carStartTime);
                    tempBuses.add(busesId.get(carId));
                    busesTime.put(carStartTime, tempBuses);
                }

                ArrayList<Road> preAnswers = new ArrayList<Road>();
                for(int i = 2; i < str.length; i++) {
                    preAnswers.add(preRoads.get(Integer.parseInt(str[i])));
                }
                busesRoads.put(carId, preAnswers);
            }
            presetReader.close();
            inputPreset.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //读取路径规划结果信息文件（未使用）
    public static void readAnswerTxt(String answerPath) {
        try {
            FileInputStream inputAnswer = new FileInputStream(answerPath);
            BufferedReader answerReader = new BufferedReader(new InputStreamReader(inputAnswer));
            String answerStr = null;
            while((answerStr = answerReader.readLine()) != null) {
                if(answerStr.contains("#"))
                    continue;
                answerStr = answerStr.trim();
                answerStr = answerStr.substring(1, answerStr.length() - 1).replace(" ", "");
                String[] str = answerStr.split(",");
                int carId = Integer.parseInt(str[0]);
                int planTime = Integer.parseInt(str[1]);
                Car car = preCars.get(carId);
                car.setActualTime(planTime);
                ArrayList<Integer> planRoad = new ArrayList<Integer>();
                for(int i = 2; i < str.length; i++) {
                    planRoad.add(Integer.parseInt(str[i]));
                }
                car.setPlanRoad(planRoad);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //读取路径规划结果
    public static void readAnswer() {
        try {
            for(String answerStr : answer) {
                if(answerStr.contains("#"))
                    continue;
                answerStr = answerStr.trim();
                answerStr = answerStr.substring(1, answerStr.length() - 1).replace(" ", "");
                String[] str = answerStr.split(",");
                int carId = Integer.parseInt(str[0]);
                int planTime = Integer.parseInt(str[1]);
                Car car = preCars.get(carId);
                car.setActualTime(planTime);
                ArrayList<Integer> planRoad = new ArrayList<Integer>();
                for(int i = 2; i < str.length; i++) {
                    planRoad.add(Integer.parseInt(str[i]));
                }
                car.setPlanRoad(planRoad);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //生成最终路径规划结果到文件
    public static void showAnswer(String answerPath) {
        try {
            FileWriter writer = new FileWriter(answerPath, true);
            for(String result : answer) {
                writer.write(result + "\t\n");
            }
            writer.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    //输出车辆排序结果（数据分析及调试用）
    public static void showCarSort(ArrayList<Car> cars) {
        try {
            FileWriter writer = new FileWriter("config/carSort.txt", true);
            for(Car car : cars) {
                String str = car.toString();
                writer.write(str + "\t\n");
            }
            writer.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    //预置车辆最大出发时间
    public static int maxBusesTime() {
        int i = 0;
        for(Map.Entry<Integer, ArrayList<Car>> entry : busesTime.entrySet()) {
            if(i < entry.getKey())
                i = entry.getKey();
        }
        return i;
    }

    //输出预置车辆信息（出发时间信息）
    public static void showBusesTime() {
        for(Map.Entry<Integer, ArrayList<Car>> entry : busesTime.entrySet()) {
            System.out.println("StartTime: " + entry.getKey() + " Buses: " + entry.getValue());
        }
    }

    //输出预置车辆信息（路径信息）
    public static void showBusesRoads() {
        for(Map.Entry<Integer, ArrayList<Road>> entry : busesRoads.entrySet()) {
            System.out.println("Bus: " + entry.getKey() + " Roads: " + entry.getValue());
        }
    }
    
    //车辆排序方法，对不同车辆集合采用不同优先级排序
    public static void carsSort(ArrayList<Car> cars, int type) {
        Collections.sort(cars, new Comparator<Car>() {
            @Override
            public int compare(Car car1, Car car2) {
                if(type == 0) {
                    if(car1.runTime() != car2.runTime()) {
                        return car1.runTime() - car2.runTime();
                    } else {
                        if(car1.speed() != car2.speed()) {
                        return car2.speed() - car1.speed();
                        } else {
                            if(car1.planTime() != car2.planTime())
                                return car1.planTime() - car2.planTime();                      
                            else {
                                if(car1.to() != car2.to())
                                    return car1.to() - car2.to();
                                else
                                    return car1.id() - car2.id();
                            }
                        }
                    }
                } else if(type == 1) {
                    if(car1.planTime() != car2.planTime())
                        return car1.planTime() - car2.planTime();
                    else
                        return car1.to() - car2.to();
                    //return car1.planTime() - car2.planTime();
                } else if(type == 2) {
                    if(car1.planTime() != car2.planTime())
                        return car1.planTime() - car2.planTime();
                    else
                        return car2.totalTime() - car1.totalTime();
                } else {
                    if(car1.planTime() != car2.planTime()) {
                        return car1.planTime() - car2.planTime();
                    } else {
                        if(car1.runTime() != car2.runTime())
                            return car1.runTime() - car2.runTime();                      
                        else {
                            return car1.to() - car2.to();
                        }
                    }
                }             
            }
        });
    }

}
