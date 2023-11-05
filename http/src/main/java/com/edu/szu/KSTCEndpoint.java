package com.edu.szu;

import cn.edu.szu.cs.*;
import com.edu.szu.entity.GeoJson;
import com.edu.szu.entity.Marker;
import com.edu.szu.service.KstcService;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/kstc")
@AllArgsConstructor
@Log4j2
public class KSTCEndpoint {

    private KstcService kstcService;

    @GetMapping("/markers")
    public List<Marker> markers(
            @RequestParam("keywords") String keywords,
            @RequestParam("lon") Double lon,
            @RequestParam("lat") Double lat,
            @RequestParam("k") Integer k,
            @RequestParam("epsilon") Double epsilon,
            @RequestParam("minPts") Integer minPts,
            @RequestParam("maxDist") Double maxDist
            ){
        if(maxDist<0){
            maxDist=Double.MAX_VALUE;
        }
        Query query = Query.builder()
                .keyword(
                        Arrays.stream(keywords.split(",")).collect(Collectors.toList())
                )
                .location(
                        Coordinate.create(
                                lon,
                                lat
                        )
                )
                .k(k)
                .epsilon(epsilon)
                .minPts(minPts)
                .maxDistance(maxDist)
                .build();

        log.info("markers: "+query.toString());
        return kstcService.loadMarkers(
                query
        );
    }

    @GetMapping("/geojson")
    public GeoJson geoJson(
            @RequestParam("keywords") String keywords,
            @RequestParam("lon") Double lon,
            @RequestParam("lat") Double lat,
            @RequestParam("k") Integer k,
            @RequestParam("epsilon") Double epsilon,
            @RequestParam("minPts") Integer minPts,
            @RequestParam("maxDist") Double maxDist
    ){
        if(maxDist<0){
            maxDist=Double.MAX_VALUE;
        }
        Query query = Query.builder()
                .keyword(
                        Arrays.stream(keywords.split(",")).collect(Collectors.toList())
                )
                .location(
                        Coordinate.create(
                                lon,
                                lat
                        )
                )
                .k(k)
                .epsilon(epsilon)
                .minPts(minPts)
                .maxDistance(maxDist)
                .build();
        log.info("geoJson: "+query.toString());
        return kstcService.loadGeoJson(
                query
        );
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


    public static void main(String[] args) {

        System.out.println(
                CommonAlgorithm.getDistance(
                        112.112,
                        23.111,
                        112.111,
                        23.112
                )
        );

    }
}
