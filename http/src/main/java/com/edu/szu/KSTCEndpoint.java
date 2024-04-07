package com.edu.szu;

import cn.edu.szu.cs.entity.KstcQuery;
import com.edu.szu.entity.GeoJson;
import com.edu.szu.entity.Marker;
import com.edu.szu.service.KstcService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

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
        KstcQuery kstcQuery = KstcQuery.builder()
                .keywords(
                        Arrays.stream(keywords.split(",")).collect(Collectors.toList())
                )
                .coordinate(new double[]{lon, lat})
                .k(k)
                .epsilon(epsilon)
                .minPts(minPts)
                .maxDistance(maxDist)
                .build();

        log.info("markers: "+ kstcQuery.toString());
        return kstcService.loadMarkers(
                kstcQuery
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
        KstcQuery kstcQuery = KstcQuery.builder()
                .keywords(
                        Arrays.stream(keywords.split(",")).collect(Collectors.toList())
                )
                .coordinate(new double[]{lon, lat})
                .k(k)
                .epsilon(epsilon)
                .minPts(minPts)
                .maxDistance(maxDist)
                .build();
        log.info("geoJson: "+ kstcQuery.toString());
        return kstcService.loadGeoJson(
                kstcQuery
        );
    }
}
