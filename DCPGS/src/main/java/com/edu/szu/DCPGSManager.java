package com.edu.szu;

import com.edu.szu.util.CheckInDistanceCalculator;
import com.edu.szu.util.CheckInReader;
import com.github.davidmoten.rtree.RTree;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import com.edu.szu.entity.CheckIn;
import com.edu.szu.entity.CheckInJson;
import com.edu.szu.entity.DCPGSParams;
import com.edu.szu.entity.DCPGSGeoJson;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Log4j2
public class DCPGSManager {
    private final Map<String, CheckInJson> jsonMap;

    private final Map<String, DCPGSGeoJson> geoJsonMap;

    private final Map<String, DCPGSParams> paramsMap;

    private final Map<String, Boolean> cacheMap;

    @Setter Map<String, Map<Long, Set<Long>>> edgeMapSet;

    public String resourcePath;


    ExecutorService pool = new ThreadPoolExecutor(4, 5, 8,TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(6), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

    public void dcpgsRun(String checkInFilePath, String key, String dataSet) throws Exception {
        //获取参数
        var params = getParams(key,dataSet);
        //判断是否已经跑完
        String filePath = String.format("%s/result/%s_%.1f_%.1f_%.1f_%.1f.json",
                dataSet,key,params.getEpsilon(),params.getOmega(),params.getTau(),params.getMaxD());
        log.info("request file path: {}", filePath);
        if(cacheMap.getOrDefault(filePath,false)){
            log.info("DCPGS finished of key: {}", key);
            return;
        }
        //跑DCPGS
        log.info("DCPGS running with params: epsilon: {}, " +
                        "omega: {}, tau: {}, maxD: {}, fileKey: {}",
                params.getEpsilon(),params.getOmega(),params.getTau(),params.getMaxD(), checkInFilePath);
        RTree<String, CheckIn> rTree = RTree.star().maxChildren(30).create();
        var checkIns = CheckInReader.getCheckInFromFile(checkInFilePath);
        log.info("checkIns size: {}", checkIns.size());
        if(edgeMapSet.get(dataSet) == null){
            throw new IllegalArgumentException("edgeMap is null");
        }
        long timeStart = System.currentTimeMillis();
        CheckInDistanceCalculator calculator = new CheckInDistanceCalculator(edgeMapSet.get(dataSet), CheckInReader.getLocationMap(), params);
        for (CheckIn checkIn : checkIns) {
            rTree = rTree.add(checkIn.getName(),checkIn);
        }
        long timeEnd1 = System.currentTimeMillis();
        log.info("RTree build finished, using time: {} s", (timeEnd1 - timeStart) / 1000.0);
        DCPGS<CheckIn> dbscan = new DCPGS<>(checkIns,5, calculator, pool, params);
        var clusters = dbscan.performClustering(rTree);
        AtomicLong sum = new AtomicLong(0);
        clusters.forEach(c -> sum.addAndGet(c.size()));
        log.info("clusters sum: {}", sum.get());
        log.info("noise point size: {}", CheckInReader.getLocationMap().size()-sum.get());
        long timeEnd2 = System.currentTimeMillis();
        log.info("DCPGS finished, using time: {} s", (timeEnd2 - timeEnd1) / 1000.0);
        //排序
        clusters.sort((list1,list2)->Integer.compare(list2.size(),list1.size()));
        long timeEnd3 = System.currentTimeMillis();
        log.info("DCPGS sort finished, using time: {} s", (timeEnd3 - timeEnd2) / 1000.0);
        //输出结果到文件
        CheckInJson checkInJson = CheckInReader.parseJson(clusters);
        CheckInReader.outPutCheckIn(clusters, resourcePath + filePath);
        var geoJsonFilePath = String.format("%s/geojson/%s_%.1f_%.1f_%.1f_%.1f.geojson",
                dataSet,key,params.getEpsilon(),params.getOmega(),params.getTau(),params.getMaxD());
        var geoJson = CheckInReader.parseGeoJson(checkInJson);
        CheckInReader.parseGeoJsonTo(geoJson, resourcePath +
                geoJsonFilePath);
        jsonMap.put(filePath,checkInJson);
        geoJsonMap.put(geoJsonFilePath,geoJson);
        cacheMap.put(filePath,true);
        long timeEnd = System.currentTimeMillis();
        log.info("DCPGS finished with {} clusters of key: {}, using time: {} s",
                clusters.size(), key, (timeEnd - timeStart) / 1000.0);
    }

    public DCPGSManager(){
        this("DCPGS/src/main/resources/");
    }

    public DCPGSManager(String resourcePath) {
        log.info("resourcePath: {}", resourcePath);
        this.resourcePath = resourcePath;
        jsonMap = new HashMap<>();
        geoJsonMap = new HashMap<>();
        paramsMap = new HashMap<>();
        cacheMap = new HashMap<>();
        File directory = new File(resourcePath + "gowalla/result");
        log.info("directory: {}", directory.getAbsolutePath());
        File[] files = directory.listFiles();
        if (files != null) {
            log.info("files: {}", files.length);
            for (File file : files) {
                cacheMap.put("gowalla/result/" + file.getName(),true);
            }
        }
        directory = new File(resourcePath + "brightkite/result");
        files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                cacheMap.put("brightkite/result/" + file.getName(),true);
            }
        }
        cacheMap.keySet().forEach(key -> log.info("cacheMap key: {}", key));
    }

    public DCPGSParams getParams(String key, String dataSet){
        key = dataSet + key;
        paramsMap.computeIfAbsent(key,k -> new DCPGSParams());
        return paramsMap.get(key);
    }

    public CheckInJson getJson(String key, String dataSet) throws IOException {
        paramsMap.computeIfAbsent(key,k -> new DCPGSParams());
        var params = getParams(key,dataSet);
        String filePath = String.format("%s%s/result/%s_%.1f_%.1f_%.1f_%.1f.json",
                resourcePath,dataSet,key,params.getEpsilon(),params.getOmega(),params.getTau(),params.getMaxD());
        log.info("getting json: {}",filePath);
        if(jsonMap.containsKey(filePath)){
            return jsonMap.get(filePath);
        }
        return CheckInReader.parseJson(filePath);
    }

    public DCPGSGeoJson getGeoJson(String key, String dataSet) throws IOException {
        paramsMap.computeIfAbsent(key,k -> new DCPGSParams());
        var params = getParams(key,dataSet);
        String filePath = String.format("%s%s/geojson/%s_%.1f_%.1f_%.1f_%.1f.geojson",
                resourcePath,dataSet,key,params.getEpsilon(),params.getOmega(),params.getTau(),params.getMaxD());
        log.info("getting geoJson: {}",filePath);
        if(geoJsonMap.containsKey(filePath)){
            return geoJsonMap.get(filePath);
        }
        return CheckInReader.readGeoJsonFromFile(filePath);
    }

    public void setAllParams(String key, DCPGSParams dcpgsParams, String dataSet){
        key = dataSet + key;
        paramsMap.put(key,dcpgsParams);
    }


}
