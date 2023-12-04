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
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.concurrent.*;

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
    public void testGson(){
        var checkIns = CheckInReader.getCheckInFromFile("gowalla/checkIn1.txt");
        checkIns = checkIns.subList(0,10);
        checkIns.forEach(checkIn -> System.out.println(gson.toJson(checkIn)));
    }

    @Test
    @SneakyThrows
    public void testLevelOrder(){
        ExecutorService pool = new ThreadPoolExecutor(4, 5, 8, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(6), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
        var edgeMap = EdgeReader.getEdges("gowalla/loc-gowalla_edges.txt");
        RTree<String, CheckIn> rTree = RTree.star().maxChildren(30).create();
        var params = new DCPGSParams();
        String checkInFilePath = "gowalla/splittedCheckIn/AustinUS.txt";
        var checkIns = CheckInReader.getCheckInFromFile(checkInFilePath);
        CheckInDistanceCalculator calculator = new CheckInDistanceCalculator(edgeMap, CheckInReader.getLocationMap(), params);
        for (CheckIn checkIn : checkIns) {
            rTree = rTree.add(checkIn.getName(),checkIn);
        }
        DCPGS<CheckIn> dbscan = new DCPGS<>(checkIns,5, calculator, pool, params);
        for (CheckIn c : checkIns) {
            var order = dbscan.getNeighboursByLevelOrder(c,rTree);
            var search = dbscan.getNeighbours(c,rTree);
            Assertions.assertEquals(order.size(), search.size());
        }
        log.info("level order & search get same result");
        long t = System.currentTimeMillis();
        for (CheckIn c : checkIns) {
            var order = dbscan.getNeighboursByLevelOrder(c,rTree);
        }
        log.info("level order time: {}", System.currentTimeMillis() - t);
        t = System.currentTimeMillis();
        for (CheckIn c : checkIns) {
            var search = dbscan.getNeighbours(c,rTree);
        }
        log.info("search time: {}", System.currentTimeMillis() - t);
    }
}
