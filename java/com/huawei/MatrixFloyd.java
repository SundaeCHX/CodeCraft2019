package com.huawei;

import java.io.*;
import java.util.*;

public class MatrixFloyd {
    private final int v;
    private double[][] dist;
    private int[][] path;
    private static final double INF = Double.MAX_VALUE;

    public MatrixFloyd(int v, ArrayList<Road> roads, ArrayList<Integer> crosses) {
        this.v = v;
        create(roads, crosses);
    }

    public void update(ArrayList<Road> roads, ArrayList<Integer> crosses) {
        create(roads, crosses);
    }

    private void create(ArrayList<Road> roads, ArrayList<Integer> crosses) {
        this.dist = new double[v+1][v+1];
        this.path = new int[v+1][v+1];
        double weight;
        
        for(int i = 0; i < crosses.size(); i++) {
            path[0][i+1] = crosses.get(i);
            path[i+1][0] = crosses.get(i);
        }
        for (int x = 1; x <= v; x++) {
            for (int y = 1; y <= v; y++) {
                for(Road road : roads) {
                    if(path[x][0] == road.from() && path[0][y] == road.to()) {
                        weight = road.pri();
                        dist[x][y] = weight;
                        path[x][y] = path[0][y];
                    }
                }
            }
        }

        for (int x = 1; x <= v; x++) {
            for (int y = 1; y <= v; y++) {
                if(dist[x][y] == 0)
                    dist[x][y] = INF;
                if(path[x][y] == 0)
                    path[x][y] = path[0][y];
            }
        }

        for(int k = 1; k <= v; k++) {
            for(int p = 1; p <= v; p++) {
                for(int q = 1; q <= v; q++) {
                    double tmp = (dist[p][k] == INF || dist[k][q] == INF) ? INF : (dist[p][k] + dist[k][q]);
                    if(dist[p][q] > tmp) {
                        dist[p][q] = tmp;
                        path[p][q] = path[p][k];
                    }
                }
            }
        }
    }

    public ArrayList<Road> findRoad(CrossMap map, int i, int j, ArrayList<Integer> crosses) {
        ArrayList<Road> cross = new ArrayList<>();
        if (dist[crosses.indexOf(i)+1][crosses.indexOf(j)+1] == INF) {
            //System.out.printf("顶点 %2d ----> %2d 不可达\n", i, j);
        } else if(i != j) {
            //System.out.printf("顶点 %2d ----> %2d 最短路径为: %2d 路径规划为: %2d %2d ", i, j, dist[i][j], i, path[i][j]);
            int k = path[crosses.indexOf(i)+1][crosses.indexOf(j)+1];
            cross.add(map.find(i, k));
            while(k != j) {
                int tmp = k;
                k = path[crosses.indexOf(k)+1][crosses.indexOf(j)+1];
                cross.add(map.find(tmp, k));
                //System.out.printf("%2d ", k);
            }
            //System.out.println();
        }
        return cross;
    }

    public void showFloyd(ArrayList<Integer> crosses) {
        for(int i = 0; i < v; i++) {
            int k = 0;
            for(int j = 0; j < v; j++) {
                if(dist[i+1][j+1] == INF) {
                    System.out.printf("顶点 %2d ----> %2d 不可达\n", crosses.get(i), crosses.get(j));
                } else if(i != j) {
                    System.out.printf("顶点 %2d ----> %2d 最短路径为: %2d 路径规划为: %2d %2d ", crosses.get(i), crosses.get(j), dist[i+1][j+1], crosses.get(i), path[i+1][j+1]);
                    k = path[i+1][j+1];
                    while(k != crosses.get(j)) {
                        k = path[crosses.indexOf(k)+1][j+1];
                        System.out.printf("%2d ", k);
                    }
                }
                System.out.println();
            }
        }
    }
}