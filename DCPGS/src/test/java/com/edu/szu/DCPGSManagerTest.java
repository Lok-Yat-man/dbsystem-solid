package com.edu.szu;

import com.edu.szu.api.Pair;
import com.edu.szu.entity.CheckIn;
import com.edu.szu.entity.DCPGSParams;
import com.edu.szu.util.CheckInDistanceCalculator;
import com.edu.szu.util.CheckInReader;
import com.edu.szu.util.EdgeReader;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import rx.Observable;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Log4j2
public class DCPGSManagerTest {
    private final DCPGSManager dcpgsManager = new DCPGSManager();

    @Test
    public void testDCPGSManager() throws Exception {
        log.info("DCPGSManagerTest start");
        Map<String, Map<Long, Set<Long>>> edgeMapSet = new HashMap<>();
        var edgeMap = EdgeReader.getEdges("gowalla/loc-gowalla_edges.txt");
        edgeMapSet.put("gowalla",edgeMap);
        edgeMap = EdgeReader.getEdges("brightkite/loc-brightkite_edges.txt");
        edgeMapSet.put("brightkite",edgeMap);
        dcpgsManager.setEdgeMapSet(edgeMapSet);
        dcpgsManager.dcpgsRun("gowalla/splittedCheckIn/StockholmSweden.txt","StockholmSweden","gowalla");
    }

}
