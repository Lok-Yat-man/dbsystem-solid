package com.edu.szu;

import com.edu.szu.util.EdgeReader;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class DCPGSManagerTest {
    private final DCPGSManager dcpgsManager = new DCPGSManager();

    @Test
    public void testDCPGSManager() throws Exception {
        log.info("DCPGSManagerTest start");
        var edgeMap = EdgeReader.getEdges("gowalla/loc-gowalla_edges.txt");
        dcpgsManager.setEdgeMap(edgeMap);
        dcpgsManager.dcpgsRun("gowalla/splittedCheckIn/SoutheastAsiaCheckIn.txt");
    }
}
