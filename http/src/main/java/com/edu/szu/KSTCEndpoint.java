package com.edu.szu;

import cn.edu.szu.cs.entity.KstcQuery;
import com.edu.szu.api.Result;
import com.edu.szu.dto.DataCoordinateRangeDTO;
import com.edu.szu.entity.GeoJson;
import com.edu.szu.service.KstcService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/kstc")
@AllArgsConstructor
@Log4j2
public class KSTCEndpoint {

    private KstcService kstcService;


    @GetMapping("/keywords")
    public List<String> getKeyWords(@RequestParam("keywords") String keywords){
        return kstcService.getKeyWords(keywords);
    }


    @GetMapping("/dataCoordinateRange")
    public Result<DataCoordinateRangeDTO> dataCoordinateRangeDTO(@RequestParam("keywords") String keywords,
                                                                @RequestParam("lon") Double lon,
                                                                @RequestParam("lat") Double lat,
                                                                @RequestParam("k") Integer k,
                                                                @RequestParam("epsilon") Double epsilon,
                                                                @RequestParam("minPts") Integer minPts,
                                                                @RequestParam("maxDist") Double maxDist,
                                                                @RequestParam("command") String command){
        KstcQuery kstcQuery = KstcQuery.builder()
                .keywords(
                        Arrays.stream(keywords.split(" ")).collect(Collectors.toList())
                )
                .coordinate(new double[]{lon, lat})
                .k(k)
                .epsilon(epsilon)
                .minPts(minPts)
                .maxDistance(maxDist<=0?Double.MAX_VALUE:maxDist)
                .command(command)
                .build();


        return kstcService.getDataCoordinateRange(kstcQuery);
    }

    @GetMapping("/geojson")
    public GeoJson geoJson(
            @RequestParam("keywords") String keywords,
            @RequestParam("lon") Double lon,
            @RequestParam("lat") Double lat,
            @RequestParam("k") Integer k,
            @RequestParam("epsilon") Double epsilon,
            @RequestParam("minPts") Integer minPts,
            @RequestParam("maxDist") Double maxDist,
            @RequestParam("command") String command){
        KstcQuery kstcQuery = KstcQuery.builder()
                .keywords(
                        Arrays.stream(keywords.split(" ")).collect(Collectors.toList())
                )
                .coordinate(new double[]{lon, lat})
                .k(k)
                .epsilon(epsilon)
                .minPts(minPts)
                .maxDistance(maxDist<=0?Double.MAX_VALUE:maxDist)
                .command(command)
                .build();
        log.info("geoJson: "+ kstcQuery.toString());

        GeoJson geoJson = kstcService.loadGeoJson(
                kstcQuery
        );
        log.info("geoJson: "+ geoJson.getFeatures().size());
        return geoJson;
    }
}
