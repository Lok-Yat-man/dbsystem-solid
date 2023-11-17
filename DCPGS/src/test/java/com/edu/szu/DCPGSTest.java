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
    public void testGson(){
        var checkIns = CheckInReader.getCheckInFromFile("gowalla/checkIn1.txt");
        checkIns = checkIns.subList(0,10);
        checkIns.forEach(checkIn -> System.out.println(gson.toJson(checkIn)));
    }
}
