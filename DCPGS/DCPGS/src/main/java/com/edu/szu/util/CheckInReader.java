package com.edu.szu.util;

import com.edu.szu.entity.AreaJson;
import com.edu.szu.entity.CheckIn;
import com.edu.szu.entity.CheckInClustering;
import com.edu.szu.entity.CheckInJson;
import com.edu.szu.entity.GeoJson;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Log4j2
public class CheckInReader {

    @Getter
    private static Map<Long, Set<Long>> locationMap;

    private static final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    public static List<CheckIn> getCheckInFromFile(String... filePaths) {
        locationMap = new HashMap<>();
        Set<CheckIn> ans = new HashSet<>();
        for (String filePath : filePaths) {
            System.out.println("reading file: " + filePath);
            try (var bf = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(IOUtils.resourceToByteArray(filePath, CheckInReader.class.getClassLoader()))))) {
                String line;
                while ((line = bf.readLine()) != null) {
                    var checkIn = stringToCheckIn(line);
                    locationMap.computeIfAbsent(checkIn.getLocationId(), key -> new HashSet<>());
                    locationMap.get(checkIn.getLocationId()).add(checkIn.getUserId());
                    ans.add(checkIn);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.println("reading work of file:( " + filePath + " ) finished");
        }
        return new ArrayList<>(ans);
    }

    private static CheckIn stringToCheckIn(String line) throws ParseException {
        var params = line.split("\t");
        if (params.length != 5) {
            throw new IllegalArgumentException("check in file format error, size should be 5 ,but: " + params.length + " \n" + line);
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        Instant date = df.parse(params[1]).toInstant();
        return new CheckIn(Long.parseLong(params[0]), params[1], Double.parseDouble(params[2]),
                Double.parseDouble(params[3]), Long.parseLong(params[4]));
    }

    public static List<CheckIn> splitArea(String areaFileName, List<CheckIn> checkIns) throws IOException {
        String areaJson = IOUtils.resourceToString("./area/" + areaFileName, StandardCharsets.UTF_8, CheckInReader.class.getClassLoader());
        AreaJson area = new Gson().fromJson(areaJson, AreaJson.class);
        var split = new ArrayList<CheckIn>();
        checkIns.forEach(checkIn -> {
            var longitude = checkIn.getLongitude();
            var latitude = checkIn.getLatitude();
            if (longitude >= area.getLeft() && longitude <= area.getRight()
                    && latitude >= area.getDown() && latitude <= area.getUp()) {
                split.add(checkIn);
            }
        });
        return split;
    }

    public static void splitAreaTo(String areaFileName, List<CheckIn> checkIns, String target) throws IOException {
        System.out.println("splitting started");
        var split = splitArea(areaFileName, checkIns);
        try (var bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("src/main/resources/" + target)))) {
            for (CheckIn checkIn : split) {
                bw.write(checkIn.toString() + "\r\n");
                bw.flush();
            }
        }
        System.out.println("splitting finished");
    }

    public static void outPutCheckIn(List<ArrayList<CheckIn>> checkIns, String target) throws Exception {
        try (var bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(target)))) {
            bw.write("{\"data\": [\r\n");
            bw.flush();
            for (int i = 0; i < checkIns.size(); i++) {
                List<CheckIn> checkInList = checkIns.get(i);
                var checkInClustering = new CheckInClustering(i, checkInList);
                bw.write(gson.toJson(checkInClustering));
                if (i < checkIns.size() - 1) {
                    bw.write(",");
                }
                bw.write("\r\n");
                bw.flush();
            }
            bw.write("]}\r\n");
            bw.flush();
        }
    }

    /**
     * @param fileName path refer to DCPGS module resources/
     */
    public static CheckInJson parseJson(String fileName) throws IOException {
        String str = IOUtils.resourceToString(fileName, StandardCharsets.UTF_8, CheckInReader.class.getClassLoader());
        return gson.fromJson(str, CheckInJson.class);
    }

    public static CheckInJson parseJson(List<ArrayList<CheckIn>> checkIns) {
        var checkInJson = new CheckInJson();
        var clusters = new ArrayList<CheckInJson.Cluster>();
        for (int i = 0; i < checkIns.size(); i++) {
            clusters.add(new CheckInJson.Cluster(i,
                    checkIns.get(i).stream().map(CheckIn::getPair).collect(Collectors.toList())));
        }
        checkInJson.setData(clusters);
        return checkInJson;
    }

    public static GeoJson readGeoJsonFromFile(String path) throws IOException {
        String json = IOUtils.resourceToString(path, StandardCharsets.UTF_8, CheckInReader.class.getClassLoader());
        return new Gson().fromJson(json,GeoJson.class);
    }

    public static GeoJson parseGeoJson(CheckInJson checkInJson){
        var clusters = checkInJson.getData();
        var geoJson = new GeoJson();
        var feature = new ArrayList<GeoJson.Feature>();
        for (CheckInJson.Cluster cluster : clusters) {
            List<CheckInJson.GeoPair> checkIns = cluster.getCheckIns();
            for (CheckInJson.GeoPair checkIn : checkIns) {
                feature.add(new GeoJson.Feature(new GeoJson.Geometry(checkIn.getLongitude()
                , checkIn.getLatitude()),new GeoJson.Properties(String.valueOf(cluster.getClusterId()))));
            }
        }
        geoJson.setFeatures(feature);
        return geoJson;
    }

    public static void parseGeoJsonTo(String origin, String target) throws IOException {
        String json = IOUtils.resourceToString(origin, StandardCharsets.UTF_8, CheckInReader.class.getClassLoader());
        CheckInJson checkInJson = new Gson().fromJson(json,CheckInJson.class);
        GeoJson geoJson = parseGeoJson(checkInJson);
        try(var bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                "src/main/resources/gowalla/" + target)))){
            bw.write(new Gson().toJson(geoJson));
            bw.flush();
        }
    }

    /**
     * split gowalla file to smaller
     */
//    public static void main(String[] args) {
//        String filePath = "loc-gowalla_totalCheckins.txt";
//        String basePath = "checkIn";
//        try(var bf = new BufferedReader(new InputStreamReader(
//                new ByteArrayInputStream(IOUtils.resourceToByteArray(filePath, CheckInReader.class.getClassLoader()))))) {
//            String line;
//            int size = 0;
//            int index = 1;
//            String outputName = basePath + index + ".txt";
//            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputName)));
//            while((line = bf.readLine()) != null){
//                ++size;
//                bw.write(line);
//                bw.write("\r\n");
//                if(size - 1200000 >= 0){
//                    ++index;
//                    size -= 1200000;
//                    outputName = basePath + index + ".txt";
//                    bw.flush();
//                    bw.close();
//                    bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputName)));
//                }
//            }
//            bw.flush();
//            bw.close();
//        }catch (Exception e){
//            throw new RuntimeException(e);
//        }
//    }
}
