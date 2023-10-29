package com.edu.szu;

import cn.edu.szu.cs.KSTC;
import cn.edu.szu.cs.KstcCluster;
import cn.edu.szu.cs.Query;
import cn.edu.szu.cs.SimpleKSTC;
import com.edu.szu.entity.GeoJson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/kstc")
@Slf4j
public class KSTCEndpoint {

    private KSTC kstc = new SimpleKSTC();

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
     * @param query
     * @return
     */
    @PostMapping("/geojson")
    public GeoJson kstcSearchGeoJson(@RequestBody Query query){
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

}
