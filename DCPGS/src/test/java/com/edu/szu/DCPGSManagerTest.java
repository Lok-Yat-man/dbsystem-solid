package com.edu.szu;

import com.edu.szu.util.EdgeReader;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
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
        dcpgsManager.dcpgsRun("gowalla/splittedCheckIn/LondonUK.txt","LondonUK","gowalla");
    }
}
