package com.edu.szu.util;

import java.lang.Math;

public class EuclideanDistanceCalculator {

    // 地球半径（单位：米）
    private static final double EARTH_RADIUS = 6371000.0;

    // 计算两个经纬度点之间的欧氏距离
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 将经纬度从度数转换为弧度
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // 计算差值
        double latDiff = lat2Rad - lat1Rad;
        double lonDiff = lon2Rad - lon1Rad;

        // 使用Haversine公式计算距离
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(lonDiff / 2) * Math.sin(lonDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    public static void main(String[] args) {
        double lat1 = 52.5200; // 第一个点的纬度
        double lon1 = 13.4050; // 第一个点的经度
        double lat2 = 48.8566; // 第二个点的纬度
        double lon2 = 2.3522;  // 第二个点的经度

        double distance = calculateDistance(lat1, lon1, lat2, lon2);

        System.out.println("两点之间的距离（米）: " + distance);
    }
}

