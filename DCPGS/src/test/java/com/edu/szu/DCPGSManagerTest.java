package com.edu.szu;

import com.edu.szu.api.Pair;
import com.edu.szu.util.CheckInDistanceCalculator;
import com.edu.szu.util.CheckInReader;
import com.edu.szu.util.EdgeReader;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
        dcpgsManager.dcpgsRun("gowalla/splittedCheckIn/OsloNorway.txt","OsloNorway","gowalla");
    }

    @Test
    public void culDCPGSDistance() throws Exception {
        String[] checkInFilePath = {
                "gowalla/splittedCheckIn/AustinUS.txt",
                "gowalla/splittedCheckIn/GothenburgSweden.txt",
                "gowalla/splittedCheckIn/LondonUK.txt",
                "gowalla/splittedCheckIn/MalmoSweden.txt",
                "gowalla/splittedCheckIn/NewcastleUponTyneUk.txt",
                "gowalla/splittedCheckIn/StockholmSweden.txt",
                "gowalla/splittedCheckIn/OsloNorway.txt",
                "gowalla/splittedCheckIn/ZurichSwitzerland.txt",
        };
        var edgeMap = EdgeReader.getEdges("gowalla/loc-gowalla_edges.txt");
        CheckInDistanceCalculator.setEdgeMap(edgeMap);
        for (String file : checkInFilePath) {
            var checkIns = CheckInReader.getCheckInFromFile(file);
            CheckInDistanceCalculator.setLocationMap(CheckInReader.getLocationMap());
            log.info("checkIns size: {}", checkIns.size());
            var eos = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.split("/")[2].split("\\.")[0] + ".distance")));
            eos.write("start,end,e,ds\n");
            eos.flush();
            for (int i = 0; i < checkIns.size(); i++) {
                for (int j = i+1; j < checkIns.size(); j++) {
                    var checkIn1 = checkIns.get(i);
                    var checkIn2 = checkIns.get(j);
                    var pair = Pair.of(checkIn1.getName(),checkIn2.getName());
                    var e = CheckInDistanceCalculator.getE(checkIn1,checkIn2);
                    var ds = CheckInDistanceCalculator.getDs(checkIn1,checkIn2);
                    eos.write(String.format("%s,%s,%f,%f\n",pair.getStart(),pair.getEnd(),e,ds));
                    eos.flush();
                }
            }
            log.info("DCPGS distance pre run end with file:{}",file);
        }
    }
}
