package com.edu.szu;

import com.edu.szu.entity.GeoJson;
import com.edu.szu.entity.GeoJsonSkyline;
import com.edu.szu.entity.Marker;
import com.edu.szu.entity.ObjectPoint;
import com.edu.szu.service.BstdService;
import com.edu.szu.service.KstcService;
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
@RequestMapping("/bstd")
@AllArgsConstructor
@Log4j2
public class BSTDEndPoint {
    private BstdService bstdService;

    @GetMapping("/objectPoints")
    public List<ObjectPoint> objectPoints(
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
                .keyword(Arrays.stream(keywords.split(",")).collect(Collectors.toList()))
                .build();
        log.info("objectPoints: " + query.toString());
        return bstdService.loadObjectPoint(query);
    }

    @GetMapping("/geojson")
    public GeoJsonSkyline geoJsonSkyline(
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
                .keyword(Arrays.stream(keywords.split(",")).collect(Collectors.toList()))
                .build();
        log.info("geoJson: " + query.toString());
        return bstdService.loadGeoJsonSkyline(
                query
        );
    }
}
