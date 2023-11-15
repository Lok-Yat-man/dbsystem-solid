package com.edu.szu;

import com.edu.szu.DCPGS;
import com.edu.szu.entity.CheckIn;
import com.edu.szu.entity.DCPGSParams;
import com.edu.szu.util.CheckInDistanceCalculator;
import com.edu.szu.util.CheckInReader;
import com.edu.szu.util.EdgeReader;
import com.github.davidmoten.rtree.RTree;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;

import java.util.List;

@Log4j2
public class DCPGSTest {

    private static final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    @Test
    public void testRTree(){
        RTree<String, CheckIn> rTree = RTree.create();
        var checkIns = CheckInReader.getCheckInFromFile("gowalla/checkIn1.txt");
        System.out.println("checkIn size: " + checkIns.size());
        for (CheckIn checkIn : checkIns) {
            rTree = rTree.add(checkIn.getName(),checkIn);
            System.out.println("rTree size: " + rTree.size());
        }
    }

    @Test
    public void testRTreeDBScan() throws Exception {
        var files = List.of("AustinUS",
                "GothenburgSweden",
                "LondonUK",
                "MalmoSweden",
                "NewcastleUponTyneUk",
                "OsloNorway",
                "StockholmSweden",
                "ZurichSwitzerland"
                );
        var edges = EdgeReader.getEdges("gowalla/loc-gowalla_edges.txt");
        CheckInDistanceCalculator.setEdgeMap(edges);
        for (String file : files) {
            long timeStart = System.currentTimeMillis();
            log.info("start clustering {}",file);
            RTree<String, CheckIn> rTree = RTree.star().maxChildren(4).create();
            var checkIns = CheckInReader.getCheckInFromFile(
                    "gowalla/splittedCheckIn/" + file + ".txt");
            CheckInDistanceCalculator.setLocationMap(CheckInReader.getLocationMap());
            System.out.println("checkIn size: " + checkIns.size());
            for (CheckIn checkIn : checkIns) {
                rTree = rTree.add(checkIn.getName(),checkIn);
            }
            System.out.println("rTree size: " + rTree.size());
            DCPGS<CheckIn> dbscan = new DCPGS<>(checkIns,5);
            var clusters = dbscan.performClustering(rTree);
            System.out.println("cluster nums: " + clusters.size());
            clusters.sort((list1,list2)->Integer.compare(list2.size(),list1.size()));
            CheckInReader.outPutCheckIn(clusters, "src/main/resources/gowalla/result/" + file + ".json");
            long timeEnd = System.currentTimeMillis();
            log.info("finish clustering {}, time: {} s",file, (timeEnd - timeStart)/1000.0);
        }
    }

    @Test
    public void testGson(){
        var checkIns = CheckInReader.getCheckInFromFile("gowalla/checkIn1.txt");
        checkIns = checkIns.subList(0,10);
        checkIns.forEach(checkIn -> System.out.println(gson.toJson(checkIn)));
    }

    @Test
    public void testDCPGSGetNeighbour() throws Exception{
        var edges = EdgeReader.getEdges("gowalla/loc-gowalla_edges.txt");
        CheckInDistanceCalculator.setEdgeMap(edges);
        String file = "AustinUS";
        RTree<String, CheckIn> rTree = RTree.star().maxChildren(4).create();
        var checkIns = CheckInReader.getCheckInFromFile(
                "gowalla/splittedCheckIn/" + file + ".txt");
        CheckInDistanceCalculator.setLocationMap(CheckInReader.getLocationMap());
        DCPGS<CheckIn> dbscan = new DCPGS<>(checkIns,5);
        dbscan.setParams(new DCPGSParams());
        dbscan.getAllNeighbours(rTree);
    }
}
