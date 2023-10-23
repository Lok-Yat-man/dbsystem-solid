package com.edu.szu;

import com.edu.szu.entity.CheckIn;
import com.edu.szu.entity.CheckInJson;
import com.edu.szu.entity.DCPGSParams;
import com.edu.szu.entity.GeoJson;
import com.edu.szu.util.CheckInDistanceCalculator;
import com.edu.szu.util.CheckInReader;
import com.github.davidmoten.rtree.RTree;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;

@Slf4j
public class DCPGSManager {

    @Setter
    private Map<Long, Set<Long>> edgeMap;

    @Getter
    private CheckInJson checkInJson;

    @Getter
    private GeoJson geoJson;

    public void dcpgsRun(String checkInFilePath) throws Exception {
        log.info("DCPGS running with params: epsilon: {}, " +
                        "omega: {}, tau: {}, maxD: {}, fileKey: {}",
                DCPGSParams.epsilon, DCPGSParams.omega, DCPGSParams.tau,
                DCPGSParams.maxD, checkInFilePath);
        RTree<String, CheckIn> rTree = RTree.star().maxChildren(4).create();
        var checkIns = CheckInReader.getCheckInFromFile(checkInFilePath);
        if(edgeMap == null){
            throw new IllegalArgumentException("edgeMap is null");
        }
        CheckInDistanceCalculator.setEdgeMap(edgeMap);
        CheckInDistanceCalculator.setLocationMap(CheckInReader.getLocationMap());
        for (CheckIn checkIn : checkIns) {
            rTree = rTree.add(checkIn.getName(),checkIn);
        }
        DCPGS<CheckIn> dbscan = new DCPGS<>(checkIns,5);
        var clusters = dbscan.performClustering(rTree);
        clusters.sort((list1,list2)->Integer.compare(list2.size(),list1.size()));
        this.checkInJson = CheckInReader.parseJson(clusters);
        this.geoJson = CheckInReader.parseGeoJson(checkInJson);
        log.info("DCPGS finished with {} clusters by filePath: {}", clusters.size(), checkInFilePath);
    }

    public DCPGSManager() {
    }

    public DCPGSManager(double epsilon, double omega, double tau, double maxD) {
        DCPGSParams.epsilon = epsilon;
        DCPGSParams.omega = omega;
        DCPGSParams.tau = tau;
        DCPGSParams.maxD = maxD;
    }

    public void setAllParams(double epsilon, double omega, double tau, double maxD) {
        DCPGSParams.epsilon = epsilon;
        DCPGSParams.omega = omega;
        DCPGSParams.tau = tau;
        DCPGSParams.maxD = maxD;
    }

    public void setEpsilon(double epsilon) {
        DCPGSParams.epsilon = epsilon;
    }

    public void setOmega(double omega) {
        DCPGSParams.omega = omega;
    }

    public void setTau(double tau) {
        DCPGSParams.tau = tau;
    }

    public void setMaxD(double maxD){
        DCPGSParams.maxD = maxD;
    }
}
