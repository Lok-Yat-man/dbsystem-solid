package com.edu.szu;

import cn.edu.szu.cs.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.edu.szu.entity.GeoJson;
import com.edu.szu.util.CheckInReader;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/kstc")
@AllArgsConstructor
@Slf4j
public class KSTCEndpoint {

    private KSTC kstc;

    /**
     * search top-k cluster
     * @param query
     * @return
     */
    @PostMapping("/search")
    public List<KstcCluster> kstcSearch(@RequestBody Query query){
        log.info(query.toString());
        return kstc.kstcSearch(query);
    }


    /**
     * search top-k cluster
     * @return
     */
    @GetMapping("/geojson")
    public GeoJson kstcSearchGeoJson(
            @RequestParam("keywords") String keywords,
            @RequestParam("lon") Double lon,
            @RequestParam("lat") Double lat,
            @RequestParam("k") Integer k,
            @RequestParam("epsilon") Double epsilon,
            @RequestParam("minPts") Integer minPts
    ){

        Query query = Query.create(
                Coordinate.create(
                        lon,
                        lat
                ),
                Arrays.stream(keywords.split(",")).collect(Collectors.toList()),
                k,
                epsilon,
                minPts
        );

        log.info(query.toString());
        List<GeoJson.Feature> features = kstc.kstcSearch(query)
                .stream()
                .map(
                        kstcCluster -> kstcCluster.getMembers()
                                .stream()
                                .map(member -> new GeoJson.Feature(
                                        new GeoJson.Geometry(
                                                member.getCoordinate().getLongitude(),
                                                member.getCoordinate().getLatitude()
                                        ),
                                        new GeoJson.Properties(kstcCluster.getClusterId() + "")
                                )).collect(Collectors.toList())
                ).reduce(new ArrayList<>(), (a, b) -> {
                    a.addAll(b);
                    return a;
                });

        GeoJson geoJson = new GeoJson();
        geoJson.setFeatures(features);

        return geoJson;
    }

    /**
     * search top-k cluster
     * @return
     */
    @GetMapping("/save")
    public String kstcSave(
            @RequestParam("keywords") String keywords,
            @RequestParam("lon") Double lon,
            @RequestParam("lat") Double lat,
            @RequestParam("k") Integer k,
            @RequestParam("epsilon") Double epsilon,
            @RequestParam("minPts") Integer minPts
    ) throws FileNotFoundException {

        Query query = Query.create(
                Coordinate.create(
                        lon,
                        lat
                ),
                Arrays.stream(keywords.split(",")).collect(Collectors.toList()),
                k,
                epsilon,
                minPts
        );


        log.info(query.toString());


        List<GeoJson.Feature> features = kstc.kstcSearch(query)
                .stream()
                .map(
                        kstcCluster -> kstcCluster.getMembers()
                                .stream()
                                .map(member -> new GeoJson.Feature(
                                        new GeoJson.Geometry(
                                                member.getCoordinate().getLongitude(),
                                                member.getCoordinate().getLatitude()
                                        ),
                                        new GeoJson.Properties(kstcCluster.getClusterId() + "")
                                )).collect(Collectors.toList())
                ).reduce(new ArrayList<>(), (a, b) -> {
                    a.addAll(b);
                    return a;
                });

        GeoJson geoJson = new GeoJson();
        geoJson.setFeatures(features);

        String basePath = "E:\\JAVA_Files\\dbsystem-solid\\http\\src\\main\\resources\\";

        String fileName = keywords+"_"+lon+"_"+lat+"_"+k+"_"+epsilon+"_"+minPts+".json";

        PrintWriter printWriter = new PrintWriter(basePath + fileName);

        printWriter.write(JSON.toJSONString(geoJson));

        printWriter.close();
        log.info("ok");
        return "ok";
    }

    @GetMapping("/example01")
    public GeoJson example01()throws Exception{
        String json = IOUtils.resourceToString("Water_-75_39_20_10000_3.json", StandardCharsets.UTF_8, KSTCEndpoint.class.getClassLoader());
        return new Gson().fromJson(json,GeoJson.class);
    }

    @GetMapping("/example02")
    public GeoJson example02()throws Exception{
        String json = IOUtils.resourceToString("Restaurants_-75.1_39.9_20_100.0_10.json", StandardCharsets.UTF_8, KSTCEndpoint.class.getClassLoader());
        return new Gson().fromJson(json,GeoJson.class);
    }

    @GetMapping("/example03")
    public GeoJson example03()throws Exception{
        String json = IOUtils.resourceToString("Drugstores_-75.1_39.9_60_1000.0_4.json", StandardCharsets.UTF_8, KSTCEndpoint.class.getClassLoader());
        return new Gson().fromJson(json,GeoJson.class);
    }

    @GetMapping("/example04")
    public GeoJson example04()throws Exception{
        String json = IOUtils.resourceToString("Food_-75.1_39.9_20_100.0_5.json", StandardCharsets.UTF_8, KSTCEndpoint.class.getClassLoader());
        return new Gson().fromJson(json,GeoJson.class);
    }
}
