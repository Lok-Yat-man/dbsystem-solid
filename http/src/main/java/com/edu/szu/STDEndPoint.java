package com.edu.szu;

import com.edu.szu.entity.GeoJsonSkyline;
import com.edu.szu.entity.ObjectPoint;
import com.edu.szu.service.STDService;
import entity.Coordinate;
import entity.Query;
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
@RequestMapping("/std")
@AllArgsConstructor
@Log4j2
public class STDEndPoint {
    private STDService stdService;

    @GetMapping("/bstd/objectPoints")
    public List<ObjectPoint> bstdObjectPoints(
            @RequestParam("longitude") Double longitude,
            @RequestParam("latitude") Double latitude,
            @RequestParam("keywords") String keywords
    ) {
        Query query = Query.builder()
                .location(
                        Coordinate.create(
                                longitude,
                                latitude
                        )
                )
                .keyword(Arrays.stream(keywords.split("\\s+")).collect(Collectors.toList()))
                .build();
        log.info("objectPoints: " + query.toString());
        return stdService.loadBstdObjectPoint(query);
    }

    @GetMapping("/bstd/geojson")
    public GeoJsonSkyline bstdGeoJsonSkyline(
            @RequestParam("longitude") Double longitude,
            @RequestParam("latitude") Double latitude,
            @RequestParam("keywords") String keywords
    ) {
        Query query = Query.builder()
                .location(
                        Coordinate.create(
                                longitude,
                                latitude
                        )
                )
                .keyword(Arrays.stream(keywords.split("\\s+")).collect(Collectors.toList()))
                .build();
        log.info("geoJson: " + query.toString());
        return stdService.loadBstdGeoJsonSkyline(query);
    }

    @GetMapping("/astd/objectPoints")
    public List<ObjectPoint> astdObjectPoints(
            @RequestParam("longitude") Double longitude,
            @RequestParam("latitude") Double latitude,
            @RequestParam("keywords") String keywords
    ) {
        Query query = Query.builder()
                .location(
                        Coordinate.create(
                                longitude,
                                latitude
                        )
                )
                .keyword(Arrays.stream(keywords.split("\\s+")).collect(Collectors.toList()))
                .build();
        log.info("objectPoints: " + query.toString());
        return stdService.loadAstdObjectPoint(query);
    }

    @GetMapping("/astd/geojson")
    public GeoJsonSkyline astdGeoJsonSkyline(
            @RequestParam("longitude") Double longitude,
            @RequestParam("latitude") Double latitude,
            @RequestParam("keywords") String keywords
    ) {
        Query query = Query.builder()
                .location(
                        Coordinate.create(
                                longitude,
                                latitude
                        )
                )
                .keyword(Arrays.stream(keywords.split("\\s+")).collect(Collectors.toList()))
                .build();
        log.info("geoJson: " + query.toString());
        return stdService.loadAstdGeoJsonSkyline(query);
    }
}
