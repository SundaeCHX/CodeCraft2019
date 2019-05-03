package com.huawei;

import java.io.*;
import java.util.*;

public class Dispatch {
	private HashMap<Integer, Car> preCars      = null;
	private HashMap<Integer, Road> preRoads    = null;
	private HashMap<Integer, Cross> preCrosses = null;
	private ArrayList<Integer> vipCars         = null;
	private ArrayList<Integer> buses           = null;
	private int allCarsNum;
	private int allVipCarsNum;
	private int allNorCarsNum;
	private int finishedVipCarsNum             = 0;
	private int finishedNorCarsNum             = 0;
	private int Tvip                           = -1;
	private int finishedNorCarsTime            = -1;
	private int T                              = -1;
	private int time                           = 0;
	private int[] sortCrossId                  = null;
	private HashMap<String, Integer> statisticsInfo = new HashMap<String, Integer>();

	public Dispatch(HashMap<Integer, Car> preCars, HashMap<Integer, Road> preRoads, HashMap<Integer, Cross> preCrosses,
		ArrayList<Integer> vipCars, ArrayList<Integer> buses) {
		this.preCars    = preCars;
		this.preRoads   = preRoads;
		this.preCrosses = preCrosses;
		this.vipCars    = vipCars;
		this.buses      = buses;
		this.buses.sort(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				Car car1 = preCars.get(o1);
				Car car2 = preCars.get(o2);
				if(car1.priority() != car2.priority())
					return car2.priority() - car1.priority();
				else {
					if(car1.actualTime() != car2.actualTime())
						return car1.actualTime() - car2.actualTime();
					else
						return car1.id() - car2.id();
				}
			}
		});
		this.allCarsNum = this.preCars.size();
		this.allVipCarsNum = this.vipCars.size();
		this.allNorCarsNum = this.allCarsNum - this.allVipCarsNum;

		this.setStatisticInfo();
		this.sortCrossRoad();
		this.sortCars();
	}

	private void setStatisticInfo() {
		String info0 = "WaitingCarNum";
		String info1 = "FinishCarNum";
		String info2 = "PreFinishCarNum";
		String info3 = "CurFinishCarNum";
		String info4 = "RunningCarNum";
		String info5 = "DepartCarNum";

		this.statisticsInfo.put(info0, 0);
		this.statisticsInfo.put(info1, 0);
		this.statisticsInfo.put(info2, 0);
		this.statisticsInfo.put(info3, 0);
		this.statisticsInfo.put(info4, 0);
		this.statisticsInfo.put(info5, 0);
	}

	private void sortCrossRoad() {
		ArrayList<Integer> sortCrossId = new ArrayList<Integer>(this.preCrosses.keySet());
		Collections.sort(sortCrossId);
		this.sortCrossId  = new int[sortCrossId.size()];
		for(int i = 0; i < sortCrossId.size(); i++) {
			this.sortCrossId[i] = sortCrossId.get(i);
		}
	}

	private void sortCars() {
		for(Map.Entry<Integer, Car> entry : preCars.entrySet()) {
			int carId = entry.getKey();
			Car car = entry.getValue();
			int roadId = car.planRoad().get(0);
			Road road = this.preRoads.get(roadId);
			road.runList().add(carId);
		}

		for(Map.Entry<Integer, Road> entry : preRoads.entrySet()) {
			Road road = entry.getValue();
			Collections.sort(road.runList(), new Comparator<Integer>() {
				@Override
				public int compare(Integer o1, Integer o2) {
					Car car1 = preCars.get(o1);
					Car car2 = preCars.get(o2);
					if(car1.priority() != car2.priority())
						return car2.priority() - car1.priority();
					else {
						if(car1.actualTime() != car2.actualTime())
							return car1.actualTime() - car2.actualTime();
						else
							return car1.id() - car2.id();
					}
				}
			});
		}
	}

	public void createCarSequeue(Road road, int direction) {
		if(road == null) {
			for(Map.Entry<Integer, Road> entry : preRoads.entrySet()) {
				Road curRoad = entry.getValue();
				if(curRoad.hasWaitingCar()) {
					curRoad.creatSequeue(0);
					if(curRoad.isDuplex() == 1)
						curRoad.creatSequeue(1);
				}
			}
		} else {
			road.creatSequeue(direction);
		}
	}

	public int getCarDirection(int crossId, Road road) {
		if(crossId == road.to())
			return 0;
		else
			return 1;
	}

	public ArrayList<LinkedList<Car>> getNextRoadMatrix(Road road, Road nextRoad) {
		int crossId;
		if(road.from() == nextRoad.from() || road.from() == road.to())
			crossId = road.from();
		else
			crossId = road.to();
		if(nextRoad.from() == crossId)
			return nextRoad.getRoadMatrix(0);
		else
			return nextRoad.getRoadMatrix(1);
	}

	public int getDir(Car car) {
		int roadId = car.planRoad().get(car.planRoadIndex());
		int nextRoadId = car.nextRoad();
		int crossId;

		Road road = this.preRoads.get(roadId);
		Road nextRoad = this.preRoads.get(nextRoadId);
		if(road.from() == nextRoad.from() || road.from() == nextRoad.to())
			crossId = road.from();
		else
			crossId = road.to();
		Cross cross = this.preCrosses.get(crossId);
		int roadIndex = cross.roads().indexOf(roadId);
		int nextRoadIndex = cross.roads().indexOf(nextRoadId);
		if(nextRoadIndex == (roadIndex + 1) % 4)
			return 1;
		else if(nextRoadIndex == (roadIndex + 2) % 4)
			return 0;
		else
			return 2;
	}

	public int getNextRoadDirection(Road road, Road nextRoad) {
		if(nextRoad.from() == road.from() || nextRoad.from() == road.to())
			return 0;
		else
			return 1;
	}

	public boolean runToNextRoad(Car car, Road road, int direction, int channel, int position) {
		ArrayList<LinkedList<Car>> roadMatrix;
		ArrayList<LinkedList<Car>> nextRoadMatrix;
		int nextRoadId;
		Road nextRoad;
		int nextRoadSpeed;

		roadMatrix = road.getRoadMatrix(direction);
		if(car.direction() == 0 && car.planRoadIndex() == car.planRoad().size() - 1) {
			roadMatrix.get(channel).pollLast();
			car.setCarState(3);
			/*
			* "WaitingCarNum"
			* "FinishCarNum"
			* "PreFinishCarNum"
			* "CurFinishCarNum"
			* "RunningCarNum"
			* "DepartCarNum"
			*/
			this.statisticsInfo.put("FinishCarNum", this.statisticsInfo.get("FinishCarNum") + 1);
			this.statisticsInfo.put("CurFinishCarNum", this.statisticsInfo.get("CurFinishCarNum") + 1);
			this.statisticsInfo.put("WaitingCarNum", this.statisticsInfo.get("WaitingCarNum") - 1);
			this.statisticsInfo.put("RunningCarNum", this.statisticsInfo.get("RunningCarNum") - 1);
			road.carsNum[direction]--;
			if(car.priority() == 1)
				this.finishedVipCarsNum = this.finishedVipCarsNum + 1;
			else
				this.finishedNorCarsNum = this.finishedNorCarsNum + 1;
			return true;
		}
		nextRoadId = car.nextRoad();
		nextRoad = this.preRoads.get(nextRoadId);
		nextRoadMatrix = this.getNextRoadMatrix(road, nextRoad);
		nextRoadSpeed = Math.min(nextRoad.speed(), car.speed());
		if(nextRoadSpeed - (road.length() - car.carPosition() - 1) <= 0) {
			car.setCarPosition(road.length() - 1);
			car.setCarState(2);
			this.statisticsInfo.put("WaitingCarNum", this.statisticsInfo.get("WaitingCarNum") - 1);
			return true; 
		} else {
			int nextChannel = nextRoad.channel();
			for(int i = 0; i < nextChannel; i++) {
				if(nextRoadMatrix.get(i).size() == 0) {
					car = roadMatrix.get(channel).pollLast();
					nextRoadMatrix.get(i).add(0, car);
					car.setCarPosition(nextRoadSpeed - road.length() + car.carPosition());
					car.setCarChannel(i);
					car.setCarState(2);
					this.statisticsInfo.put("WaitingCarNum", this.statisticsInfo.get("WaitingCarNum") - 1);
					car.setCarSpeed(nextRoadSpeed);
					car.planRoadIndexAdd();
					if(car.planRoadIndex() == (car.planRoad().size() - 1)) {
						car.setDirection(0);
						car.setNextRoad(-1);
						car.setNextRoadSpeed(-1);
					} else {
						car.setNextRoad(car.planRoad().get(car.planRoadIndex() + 1));
						car.setNextRoadSpeed(this.preRoads.get(car.nextRoad()).speed());
						car.setDirection(this.getDir(car));
					}
					road.carsNum[direction]--;
					int nextRoadDirection = this.getNextRoadDirection(road, nextRoad);
					nextRoad.carsNum[nextRoadDirection]++;
					car.setRealPlanTime(false);
					return true;
				} else {
					Car frontCar = nextRoadMatrix.get(i).get(0);
					if(frontCar.carPosition() == 0 && frontCar.carState() == 2) {
						if(i == nextChannel - 1) {
							car.setCarPosition(road.length() - 1);
							car.setCarState(2);
							this.statisticsInfo.put("WaitingCarNum", this.statisticsInfo.get("WaitingCarNum") - 1);
							return true;
						} else {
							continue;
						}
					} else if(frontCar.carPosition() == 0 && frontCar.carState() == 1) {
						return false;
					} else if(nextRoadSpeed - (road.length() - car.carPosition() - 1) <= frontCar.carPosition()) {
						car = roadMatrix.get(channel).pollLast();
						nextRoadMatrix.get(i).add(0, car);
						car.setCarPosition(nextRoadSpeed - road.length() + car.carPosition());
						car.setCarChannel(i);
						car.setCarState(2);
						this.statisticsInfo.put("WaitingCarNum", this.statisticsInfo.get("WaitingCarNum") - 1);
						car.setCarSpeed(nextRoadSpeed);
						car.planRoadIndexAdd();
						if(car.planRoadIndex() == (car.planRoad().size() - 1)) {
							car.setDirection(0);
							car.setNextRoad(-1);
							car.setNextRoadSpeed(-1);
						} else {
							car.setNextRoad(car.planRoad().get(car.planRoadIndex() + 1));
							car.setNextRoadSpeed(this.preRoads.get(car.nextRoad()).speed());
							car.setDirection(this.getDir(car));
						}
						road.carsNum[direction]--;
						int nextRoadDirection = this.getNextRoadDirection(road, nextRoad);
						nextRoad.carsNum[nextRoadDirection]++;
						car.setRealPlanTime(false);
						return true;
					} else if(frontCar.carState() == 2) {
						car = roadMatrix.get(channel).pollLast();
						nextRoadMatrix.get(i).add(0, car);
						car.setCarPosition(frontCar.carPosition() - 1);
						car.setCarChannel(i);
						car.setCarState(2);
						this.statisticsInfo.put("WaitingCarNum", this.statisticsInfo.get("WaitingCarNum") - 1);
						car.setCarSpeed(nextRoadSpeed);
						car.planRoadIndexAdd();
						if(car.planRoadIndex() == (car.planRoad().size() - 1)) {
							car.setDirection(0);
							car.setNextRoad(-1);
							car.setNextRoadSpeed(-1);
						} else {
							car.setNextRoad(car.planRoad().get(car.planRoadIndex() + 1));
							car.setNextRoadSpeed(this.preRoads.get(car.nextRoad()).speed());
							car.setDirection(this.getDir(car));
						}
						road.carsNum[direction]--;
						int nextRoadDirection = this.getNextRoadDirection(road, nextRoad);
						nextRoad.carsNum[nextRoadDirection]++;
						car.setRealPlanTime(false);
						car.setRealPlanTime(false);
						return true;
					} else {
						return false;
					}
				}
			}
		}
		System.out.println("error!");
		return false;
	}

	public void channelUpdate(ArrayList<LinkedList<Car>> roadMatrix, int channel, int length) {
		LinkedList<Car> channelMatrix = roadMatrix.get(channel);
		if(channelMatrix.size() == 0)
			return;
		Car car;
		for(int i = channelMatrix.size() - 1; i >= 0; i--) {
			car = channelMatrix.get(i);
			if(car.carState() == 2)
				continue;
			else {
				if(i == channelMatrix.size() - 1) {
					if(length - car.carPosition() - 1 < car.carSpeed()) {
						car.setCarState(1);
						return;
					} else {
						car.setCarPosition(car.carPosition() + car.carSpeed());
						car.setCarState(2);
						this.statisticsInfo.put("WaitingCarNum", this.statisticsInfo.get("WaitingCarNum") - 1);
						continue;
					}
				} else {
					Car frontCar = channelMatrix.get(i + 1);
					if(frontCar.carPosition() - car.carPosition() > car.carSpeed()) {
						car.setCarPosition(car.carPosition() + car.carSpeed());
						car.setCarState(2);
						this.statisticsInfo.put("WaitingCarNum", this.statisticsInfo.get("WaitingCarNum") - 1);
						continue;
					} else {
						if(frontCar.carState() == 2) {
							car.setCarPosition(frontCar.carPosition() - 1);
							car.setCarState(2);
							this.statisticsInfo.put("WaitingCarNum", this.statisticsInfo.get("WaitingCarNum") - 1);
							continue;
						} else {
							return;
						}
					}
				}
			}
		}
	}

	public int getConflictDirection(Road road, int crossId) {
		if(road.to() == crossId)
			return 0;
		else
			return 1;
	}

	public boolean conflict(Car car, Road road, Cross cross) {
		int conflictRoadId;
		Road conflictRoad;
		int direction;
		int conflictCarId;
		Car conflictCar;
		int[] conflictRoadsCars;

		if(car.priority() == 1) {
			if(car.direction() == 0)
				return false;
			else {
				if(car.direction() == 1) {
					conflictRoadId = cross.roads().get((cross.roads().indexOf(road.id()) + 3) % 4);
					if(conflictRoadId == -1)
						return false;
					conflictRoad = this.preRoads.get(conflictRoadId);
					direction = this.getConflictDirection(conflictRoad, cross.id());
					if(direction == 1 && conflictRoad.isDuplex() == 0)
						conflictRoadsCars = new int[] {-1, -1, -1, -1};
					else
						conflictRoadsCars = conflictRoad.getCarSequeue(direction);
					if(conflictRoadsCars[0] == -1)
						return false;
					else {
						conflictCarId = conflictRoadsCars[0];
						conflictCar = this.preCars.get(conflictCarId);
						if(conflictCar.priority() == 1 && conflictCar.direction() == 0)
							return true;
						else
							return false;
					}
				} else {
					conflictRoadId = cross.roads().get((cross.roads().indexOf(road.id()) + 1) % 4);
					if(conflictRoadId != -1) {
						conflictRoad = this.preRoads.get(conflictRoadId);
						direction = this.getConflictDirection(conflictRoad, cross.id());
						if(direction == 1 && conflictRoad.isDuplex() == 0)
							conflictRoadsCars = new int[] {-1, -1, -1, -1};
						else
							conflictRoadsCars = conflictRoad.getCarSequeue(direction);
						if(conflictRoadsCars[0] != -1) {
							conflictCarId = conflictRoadsCars[0];
							conflictCar = this.preCars.get(conflictCarId);
							if(conflictCar.priority() == 1 && conflictCar.direction() == 0)
								return true;
						}
					}
					conflictRoadId = cross.roads().get((cross.roads().indexOf(road.id()) + 2) % 4);
					if(conflictRoadId == -1)
						return false;
					conflictRoad = this.preRoads.get(conflictRoadId);
					direction = this.getConflictDirection(conflictRoad, cross.id());
					if(direction == 1 && conflictRoad.isDuplex() == 0)
						conflictRoadsCars = new int[] {-1, -1, -1, -1};
					else
						conflictRoadsCars = conflictRoad.getCarSequeue(direction);
					if(conflictRoadsCars[0] == -1)
						return false;
					else {
						conflictCarId = conflictRoadsCars[0];
						conflictCar = this.preCars.get(conflictCarId);
						if(conflictCar.priority() == 1 && conflictCar.direction() == 1)
							return true;
						else
							return false;
					}
				}
			}
		} else {
			if(car.direction() == 0) {
				conflictRoadId = cross.roads().get((cross.roads().indexOf(road.id()) + 1) % 4);
				if(conflictRoadId != -1) {
					conflictRoad = this.preRoads.get(conflictRoadId);
					direction = this.getConflictDirection(conflictRoad, cross.id());
					if(direction == 1 && conflictRoad.isDuplex() == 0)
						conflictRoadsCars = new int[] {-1, -1, -1, -1};
					else
						conflictRoadsCars = conflictRoad.getCarSequeue(direction);
					if(conflictRoadsCars[0] != -1) {
						conflictCarId = conflictRoadsCars[0];
						conflictCar = this.preCars.get(conflictCarId);
						if(conflictCar.priority() == 1 && conflictCar.direction() == 1)
							return true;
					}
				}
				conflictRoadId = cross.roads().get((cross.roads().indexOf(road.id()) + 3) % 4);
				if(conflictRoadId == -1)
					return false;
				conflictRoad = this.preRoads.get(conflictRoadId);
				direction = this.getConflictDirection(conflictRoad, cross.id());
				if(direction == 1 && conflictRoad.isDuplex() == 0)
					conflictRoadsCars = new int[] {-1, -1, -1, -1};
				else
					conflictRoadsCars = conflictRoad.getCarSequeue(direction);
				if(conflictRoadsCars[0] == -1)
					return false;
				else {
					conflictCarId = conflictRoadsCars[0];
					conflictCar = this.preCars.get(conflictCarId);
					if(conflictCar.priority() == 1 && conflictCar.direction() == 2)
						return true;
					else
						return false;
				}
			} else if(car.direction() == 1) {
				conflictRoadId = cross.roads().get((cross.roads().indexOf(road.id()) + 3) % 4);
				if(conflictRoadId != -1) {
					conflictRoad = this.preRoads.get(conflictRoadId);
					direction = this.getConflictDirection(conflictRoad, cross.id());
					if(direction == 1 && conflictRoad.isDuplex() == 0)
						conflictRoadsCars = new int[] {-1, -1, -1, -1};
					else
						conflictRoadsCars = conflictRoad.getCarSequeue(direction);
					if(conflictRoadsCars[0] != -1) {
						conflictCarId = conflictRoadsCars[0];
						conflictCar = this.preCars.get(conflictCarId);
						if(conflictCar.direction() == 0)
							return true;
					}
				}
				conflictRoadId = cross.roads().get((cross.roads().indexOf(road.id()) + 2) % 4);
				if(conflictRoadId == -1)
					return false;
				conflictRoad = this.preRoads.get(conflictRoadId);
				direction = this.getConflictDirection(conflictRoad, cross.id());
				if(direction == 1 && conflictRoad.isDuplex() == 0)
					conflictRoadsCars = new int[] {-1, -1, -1, -1};
				else
					conflictRoadsCars = conflictRoad.getCarSequeue(direction);
				if(conflictRoadsCars[0] == -1)
					return false;
				else {
					conflictCarId = conflictRoadsCars[0];
					conflictCar = this.preCars.get(conflictCarId);
					if(conflictCar.priority() == 1 && conflictCar.direction() == 2)
						return true;
					else
						return false;
				}
			} else {
				conflictRoadId = cross.roads().get((cross.roads().indexOf(road.id()) + 1) % 4);
				if(conflictRoadId != -1) {
					conflictRoad = this.preRoads.get(conflictRoadId);
					direction = this.getConflictDirection(conflictRoad, cross.id());
					if(direction == 1 && conflictRoad.isDuplex() == 0)
						conflictRoadsCars = new int[] {-1, -1, -1, -1};
					else
						conflictRoadsCars = conflictRoad.getCarSequeue(direction);
					if(conflictRoadsCars[0] != -1) {
						conflictCarId = conflictRoadsCars[0];
						conflictCar = this.preCars.get(conflictCarId);
						if(conflictCar.direction() == 0)
							return true;
					}
				}
				conflictRoadId = cross.roads().get((cross.roads().indexOf(road.id()) + 2) % 4);
				if(conflictRoadId == -1)
					return false;
				conflictRoad = this.preRoads.get(conflictRoadId);
				direction = this.getConflictDirection(conflictRoad, cross.id());
				if(direction == 1 && conflictRoad.isDuplex() == 0)
					conflictRoadsCars = new int[] {-1, -1, -1, -1};
				else
					conflictRoadsCars = conflictRoad.getCarSequeue(direction);
				if(conflictRoadsCars[0] == -1)
					return false;
				else {
					conflictCarId = conflictRoadsCars[0];
					conflictCar = this.preCars.get(conflictCarId);
					if(conflictCar.direction() == 1)
						return true;
					else
						return false;
				}
			}
		}
	}

	public boolean driveCarWaitState() {
		while(this.statisticsInfo.get("WaitingCarNum") != 0) {
			Cross cross;
			Road road;
			Car car;
			int direction;
			int channel;
			int position;
			int[] firstCar;
			int deadLock = 0;
			int preWaitingCarNum = this.statisticsInfo.get("WaitingCarNum");

			for(int crossId : this.sortCrossId) {
				cross = this.preCrosses.get(crossId);
				for(int roadId : cross.sortRoads()) {
					if(roadId == -1)
						continue;
					road = this.preRoads.get(roadId);
					direction = this.getCarDirection(crossId, road);
					if(direction == 1 && road.isDuplex() == 0)
						continue;
					firstCar = road.getCarSequeue(direction);
					label : {
						while(firstCar[0] != -1) {
							car = this.preCars.get(firstCar[0]);
							channel = firstCar[1];
							position = firstCar[2];
							if(this.conflict(car, road, cross))
								break label;
							if(this.runToNextRoad(car, road, direction, channel, position)) {
								this.channelUpdate(road.getRoadMatrix(direction), channel, road.length());
								this.createCarSequeue(road, direction);
								road.runCarInRunList(this.preCars, this.preRoads, this.preCrosses, 
									this.time, true, this.statisticsInfo, direction);
							} else {
								break label;
							}
							firstCar = road.getCarSequeue(direction);
						}
						road.setWaitingCarState(false);
					}
				}
			}
			if(preWaitingCarNum == this.statisticsInfo.get("WaitingCarNum")) {
				deadLock = 1;
			}
			if(deadLock == 1)
				return false;
		}
		return true;
	}

	public int score() {
		// 1、车速
		int max_speed_all_car = -1;
		int max_speed_priority_car = -1;
		int min_speed_all_car = 1000;
		int min_speed_priority_car = 1000;
		
		// 2、出发时间
		int latest_time_all_car = -1;
		int latest_time_priority_car = -1;
		int earlist_time_all_car = 1000;
		int earlist_time_priority_car = 1000;
		
		// 3、分布
		List<Integer> list_from_dis_all_car = new ArrayList<>();
		List<Integer> list_from_dis_priority_car = new ArrayList<>();
		List<Integer> list_to_dis_all_car = new ArrayList<>();
		List<Integer> list_to_dis_priority_car = new ArrayList<>();
		
		Iterator<Map.Entry<Integer, Car>> iter = this.preCars.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<Integer, Car> entry = iter.next();
			Car car = entry.getValue();
			
			// 1、所有车最大速度
			if(car.speed() > max_speed_all_car)
				max_speed_all_car = car.speed();
			// 2、所有车最小速度
			if(car.speed() < min_speed_all_car)
				min_speed_all_car = car.speed();
			// 3、所有车最晚出发时间
			if(car.planTime() > latest_time_all_car)
				latest_time_all_car = car.planTime();
			// 4、所有车最早出发时间
			if(car.planTime() < earlist_time_all_car)
				earlist_time_all_car = car.planTime();
			// 5、所有车出发地分布
			if(!list_from_dis_all_car.contains(car.from())) {
				list_from_dis_all_car.add(car.from());
			}
			// 6、所有车目的地分布
			if(!list_to_dis_all_car.contains(car.from())) {
				list_to_dis_all_car.add(car.from());
			}
			
			if(car.priority() == 1) {
				if(car.speed() > max_speed_priority_car)
					max_speed_priority_car = car.speed();
				if(car.speed() < min_speed_priority_car)
					min_speed_priority_car = car.speed();
				if(car.planTime() > latest_time_priority_car)
					latest_time_priority_car = car.planTime();
				if(car.planTime() < earlist_time_priority_car)
					earlist_time_priority_car = car.planTime();
				if(!list_from_dis_priority_car.contains(car.from())) {
					list_from_dis_priority_car.add(car.from());
				}
				if(!list_to_dis_priority_car.contains(car.from())) {
					list_to_dis_priority_car.add(car.from());
				}
			}
		}
		this.Tvip -= earlist_time_priority_car;
		
		double a1 = (this.allCarsNum + 0.0) / this.allVipCarsNum * 0.05;
		double a2 = ((max_speed_all_car + 0.0)/min_speed_all_car) / ((max_speed_priority_car+0.0)/min_speed_priority_car) * 0.2375;
		double a3 = ((latest_time_all_car + 0.0)/earlist_time_all_car) / ((latest_time_priority_car+0.0)/earlist_time_priority_car) * 0.2375;
		double a4 = (list_from_dis_all_car.size()+0.0)/list_from_dis_priority_car.size() * 0.2375;
		double a5 = (list_to_dis_all_car.size()+0.0)/list_to_dis_priority_car.size() * 0.2375;
		double a = a1 + a2 + a3 + a4 + a5;
		int final_score = (int)(a*this.Tvip + this.T);
		return final_score;
	}

	public void runningCarChannelUpdate(LinkedList<Car> roadChannel, int length, Road road) {
		int i =0;
		Car car;
		for(i = roadChannel.size() - 1; i >= 0; i--) {
			car = roadChannel.get(i);
			if(i == roadChannel.size() - 1) {
				if(length - car.carPosition() - 1 < car.carSpeed()) {
					car.setCarState(1);
					this.statisticsInfo.put("WaitingCarNum", this.statisticsInfo.get("WaitingCarNum") + 1);
					road.setWaitingCarState(true);
					continue;
				} else {
					car.setCarPosition(car.carPosition() + car.carSpeed());
					car.setCarState(2);
				}
			} else {
				Car frontCar = roadChannel.get(i + 1);
				if(frontCar.carPosition() - car.carPosition() > car.carSpeed()) {
					car.setCarPosition(car.carPosition() + car.carSpeed());
					car.setCarState(2);
				} else {
					if(frontCar.carState() == 2) {
						car.setCarPosition(frontCar.carPosition() - 1);
						car.setCarState(2);
					} else {
						car.setCarState(1);
						this.statisticsInfo.put("WaitingCarNum", this.statisticsInfo.get("WaitingCarNum") + 1);
						road.setWaitingCarState(true);
						continue;
					}
				}
			}
		}
	}

	public int dispatch() {
		while(true) {
			this.time++;
			this.statisticsInfo.put("CurFinishCarNum", 0);
			this.statisticsInfo.put("DepartCarNum", 0);

			for(Map.Entry<Integer, Road> entry : preRoads.entrySet()) {
				Road road = entry.getValue();
				int channel = road.channel();
				int length = road.length();
				for(int i = 0; i < channel; i++) {
					if(road.getRoadMatrix(0).get(i).size() != 0)
						this.runningCarChannelUpdate(road.getRoadMatrix(0).get(i), length, road);
					if(road.isDuplex() == 1) {
						if(road.getRoadMatrix(1).get(i).size() != 0)
							this.runningCarChannelUpdate(road.getRoadMatrix(1).get(i), length, road);
					}
				}
			}

			for(Map.Entry<Integer, Road> entry : preRoads.entrySet()) {
				Road road = entry.getValue();
				road.runCarInRunList(this.preCars, this.preRoads, this.preCrosses, this.time,
					true, this.statisticsInfo, -1);
			}

			this.createCarSequeue(null, -1);
			if(!this.driveCarWaitState()) {
				System.out.println("发生死锁！！！！！！！！！！！！！！！！！！");
				return -1;
			}
			for(Map.Entry<Integer, Road> entry : preRoads.entrySet()) {
				Road road = entry.getValue();
				road.runCarInRunList(this.preCars, this.preRoads, this.preCrosses, this.time,
					false, this.statisticsInfo, -1);
			}
			if(this.Tvip == -1 && this.finishedVipCarsNum == this.allVipCarsNum)
				this.Tvip = this.time;
			if(this.finishedNorCarsTime == -1 && this.finishedNorCarsNum == this.allNorCarsNum)
				this.finishedNorCarsTime = this.time;
			/*
			* "WaitingCarNum"
			* "FinishCarNum"
			* "PreFinishCarNum"
			* "CurFinishCarNum"
			* "RunningCarNum"
			* "DepartCarNum"
			*/
			System.out.println("----------------------------------------------------------------------------------");
            System.out.println("当前系统调度时间为                     ：" + this.time);
            System.out.println("当前时刻已经完成的车数量为       ：" + this.statisticsInfo.get("FinishCarNum"));
            System.out.println("该时间片内完成的车辆数目为       ：" + this.statisticsInfo.get("CurFinishCarNum"));
            System.out.println("当前时间片系统中运行的车辆数目：" + this.statisticsInfo.get("RunningCarNum"));
            System.out.println("当前时刻的发车数量为                 ：" + this.statisticsInfo.get("DepartCarNum"));
            
            if(this.statisticsInfo.get("FinishCarNum") == this.allCarsNum) {
            	this.T = this.time;
            	break;
            }
		}
		int score = this.score();
		System.out.println("----------------------------------------------------------------------------------");
		System.out.println("系统调度完成！！！！");
		System.out.println("根据公式，整个系统得分为 ：" + score);
		System.out.println("优先级车辆的完成时间为    ：" + this.Tvip);
		System.out.println("整个系统的完成时间为        ：" + this.T);
		return score;
	}
}