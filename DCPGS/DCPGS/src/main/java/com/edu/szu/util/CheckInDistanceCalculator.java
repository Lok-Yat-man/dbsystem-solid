package com.edu.szu.util;

import com.edu.szu.entity.CheckIn;
import com.edu.szu.entity.DCPGSParams;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CheckInDistanceCalculator {
    private static final double INF = 1.5;

    private static long computeTime = 0;

    private static long mNum = 0;

    /**
     * key = userId, value = set of users who is friend of key
     */
    private static Map<Long, Set<Long>> edgeMap;

    /**
     * key = locationId, value=Set of users who visit this location
     */
    private static Map<Long, Set<Long>> locationMap;


    public static void setEdgeMap(Map<Long, Set<Long>> edgeMap) {
        CheckInDistanceCalculator.edgeMap = edgeMap;
    }

    public static void setLocationMap(Map<Long, Set<Long>> locationMap) {
        CheckInDistanceCalculator.locationMap = locationMap;
    }

    /**
     * 计算地理社交距离 D_{gs}
     */
    public static double calculateDistance(CheckIn val1, CheckIn val2) {
        ++computeTime;
        if((computeTime & 1048575) == 0){
            System.out.println(++mNum + " M times");
        }
        double dp = getDp(val1,val2);
        if(dp >= 1)
            return INF;
        double ds = getDs(val1,val2);
        if(ds >= DCPGSParams.tau)
            return INF;
        return DCPGSParams.omega * dp + (1-DCPGSParams.omega) * ds;
    }

    /**
     * 计算欧氏距离 E(p_i,p_j)
     */
    public static double getE(CheckIn val1, CheckIn val2){
        return EuclideanDistanceCalculator
                .calculateDistance(val1.getLatitude(),val1.getLongitude(),
                        val2.getLatitude(),val2.getLongitude());
    }

    /**
     * 计算空间距离 D_p
     * @param val1
     * @param val2
     * @return
     */
    public static double getDp(CheckIn val1, CheckIn val2){
        return getE(val1,val2) / DCPGSParams.maxD;
    }

    /**
     * 计算社交距离 D_s
     * @param val1
     * @param val2
     * @return
     */
    public static double getDs(CheckIn val1, CheckIn val2){
        if(val1.equals(val2)){
            return 0.0;
        }
        var upi = locationMap.get(val1.getLocationId());
        var upj = locationMap.get(val2.getLocationId());
        Set<Long> cuij = new HashSet<>();
        upi.forEach(user -> {
            if (isContributeUser(user, upj)) {
                cuij.add(user);
                upj.remove(user);
            }
        });
        upj.forEach(user -> {
            if (isContributeUser(user, upi)) {
                cuij.add(user);
            }
        });
        return 1.0 - ((double) cuij.size() / (upi.size() + upj.size()));
    }

    private static boolean isContributeUser(long user, Set<Long> targetLocation){
        if(targetLocation.contains(user)){
            return true;
        }else{
            Set<Long> friends = edgeMap.get(user);
            for (Long friend : friends) {
                if(targetLocation.contains(friend)){
                    return true;
                }
            }
        }
        return false;
    }
}
