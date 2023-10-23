package com.edu.szu;

import com.edu.szu.entity.CheckIn;
import com.edu.szu.entity.CheckInJson;
import com.edu.szu.util.CheckInReader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReaderTest {

    private static final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    @Test
    public void testReading(){
        String checkInFile = "loc-gowalla_totalCheckins.txt";
        var checkIns = CheckInReader.getCheckInFromFile(checkInFile);
        System.out.println(checkIns.size());
    }

    @Test
    public void testComputeIfAbsent(){
        Map<String, String> map = new HashMap<>();
        map.computeIfAbsent("key",k -> k + "=value");
        System.out.println(map.get("key"));
    }

    @Test
    public void testFromJson() throws IOException {
        CheckInJson checkInJson = CheckInReader.parseJson("gowalla/gowallaTop100.json");
        System.out.println(checkInJson);
        CheckInJson json = new CheckInJson();
        var pairsList = List.of(new CheckIn(0, "now",40.0498503951,-82.9151378351,1),
                new CheckIn(1,"now",3.1177151833,101.6357821,2),
                new CheckIn(2,"now",49.453724849,11.0763946275,3));
//        json.setData(List.of(new CheckInJson.Cluster(0,pairsList),
//                new CheckInJson.Cluster(1,pairsList),
//                new CheckInJson.Cluster(2,pairsList)));
        System.out.println(gson.toJson(json));
        System.out.println(gson.fromJson(gson.toJson(json), CheckInJson.class));
    }

    @Test
    public void testGeoJson() throws IOException {
        var files = List.of("AustinUS",
                "GothenburgSweden",
                "LondonUK",
                "MalmoSweden",
                "NewcastleUponTyneUk",
                "OsloNorway",
                "StockholmSweden",
                "ZurichSwitzerland"
        );
        for (String file : files) {
            CheckInReader.parseGeoJsonTo("gowalla/result/" + file + ".json","geojson/" + file + ".geojson");
        }
    }

    @Test
    public void testSplit() throws IOException {
        var checkIns = CheckInReader.getCheckInFromFile(
                "gowalla/checkIn1.txt",
                "gowalla/checkIn2.txt",
                "gowalla/checkIn3.txt",
                "gowalla/checkIn4.txt",
                "gowalla/checkIn5.txt",
                "gowalla/checkIn6.txt");
        CheckInReader.splitAreaTo("AustinUS.json",checkIns, "gowalla/splittedCheckIn/AustinUS.txt");
        CheckInReader.splitAreaTo("GothenburgSweden.json",checkIns, "gowalla/splittedCheckIn/GothenburgSweden.txt");
        CheckInReader.splitAreaTo("LondonUK.json",checkIns, "gowalla/splittedCheckIn/LondonUK.txt");
        CheckInReader.splitAreaTo("MalmoSweden.json",checkIns, "gowalla/splittedCheckIn/MalmoSweden.txt");
        CheckInReader.splitAreaTo("NewcastleUponTyneUk.json",checkIns, "gowalla/splittedCheckIn/NewcastleUponTyneUk.txt");
        CheckInReader.splitAreaTo("OsloNorway.json",checkIns, "gowalla/splittedCheckIn/OsloNorway.txt");
        CheckInReader.splitAreaTo("StockholmSweden.json",checkIns, "gowalla/splittedCheckIn/StockholmSweden.txt");
        CheckInReader.splitAreaTo("ZurichSwitzerland.json",checkIns, "gowalla/splittedCheckIn/ZurichSwitzerland.txt");
    }
}
