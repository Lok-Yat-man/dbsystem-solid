package com.edu.szu;

import com.edu.szu.entity.CheckIn;
import com.edu.szu.util.CheckInDistanceCalculator;
import com.edu.szu.util.CheckInReader;
import com.edu.szu.util.EdgeReader;
import com.github.davidmoten.rtree.RTree;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

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
        RTree<String, CheckIn> rTree = RTree.star().maxChildren(4).create();
//        RTree<String, CheckIn> rTree = RTree.maxChildren(4).create();
        var checkIns = CheckInReader.getCheckInFromFile(
                "gowalla/splittedCheckIn/SouthAfricaCheckIn.txt");
        CheckInDistanceCalculator.setEdgeMap(EdgeReader.getEdges("gowalla/loc-gowalla_edges.txt"));
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
        CheckInReader.outPutCheckIn(clusters, "src/main/resources/gowalla/result/SouthAfrica.json");
    }

    @Test
    public void testGson(){
        var checkIns = CheckInReader.getCheckInFromFile("gowalla/checkIn1.txt");
        checkIns = checkIns.subList(0,10);
        checkIns.forEach(checkIn -> System.out.println(gson.toJson(checkIn)));
    }
}
