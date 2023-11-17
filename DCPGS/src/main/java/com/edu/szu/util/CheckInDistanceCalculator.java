package com.edu.szu.util;

import com.edu.szu.api.PointDistanceCalculator;
import com.edu.szu.entity.CheckIn;
import com.edu.szu.entity.DCPGSParams;
import lombok.extern.log4j.Log4j2;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Log4j2
public class CheckInDistanceCalculator implements PointDistanceCalculator<CheckIn> {
    private static final double INF = 1.5;

    /**
     * key = userId, value = set of users who is friend of key
     */
    private final Map<Long, Set<Long>> edgeMap;

    /**
     * key = locationId, value=Set of users who visit this location
     */
    private final Map<String, Set<Long>> locationMap;

    private final DCPGSParams params;

    public CheckInDistanceCalculator(Map<Long, Set<Long>> edgeMap, Map<String, Set<Long>> locationMap,
                                     DCPGSParams params){
        this.edgeMap = edgeMap;
        this.locationMap = locationMap;
        this.params = params;
    }

    /**
     * 计算地理社交距离 D_{gs}
     */
    public double calculateDistance(CheckIn val1, CheckIn val2) {
        double dp = getDp(val1,val2);
        if(dp >= 1)
            return INF;
        double ds = getDs(val1,val2);
        if(ds >= params.getTau())
            return INF;
        return params.getOmega() * dp + (1-params.getOmega()) * ds;
    }

    /**
     * 计算欧氏距离 E(p_i,p_j)
     */
    private double getE(CheckIn val1, CheckIn val2){
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
    private double getDp(CheckIn val1, CheckIn val2){
        return getE(val1,val2) / params.getMaxD();
    }

    /**
     * 计算社交距离 D_s
     * @param val1
     * @param val2
     * @return
     */
    private double getDs(CheckIn val1, CheckIn val2){
        if(val1.equals(val2)){
            return 0.0;
        }
        var upi = new HashSet<>(locationMap.get(val1.getLocationId()));
        var upj = new HashSet<>(locationMap.get(val2.getLocationId()));
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

    private boolean isContributeUser(long user, Set<Long> targetLocation){
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
