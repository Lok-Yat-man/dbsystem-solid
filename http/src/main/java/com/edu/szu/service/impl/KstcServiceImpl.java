package com.edu.szu.service.impl;

import cn.edu.szu.cs.Coordinate;
import cn.edu.szu.cs.KSTC;
import cn.edu.szu.cs.Query;
import cn.edu.szu.cs.RelatedObject;
import com.edu.szu.entity.GeoJson;
import com.edu.szu.entity.Marker;
import com.edu.szu.service.KstcService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class KstcServiceImpl implements KstcService {

    private KSTC<RelatedObject> kstc;

    public KstcServiceImpl(KSTC<RelatedObject> kstc) {
        this.kstc = kstc;
    }


    private GeoJson doLoadGeoJson(Query query){
        List<Set<RelatedObject>> list = kstc.kstcSearch(query);
        GeoJson geoJson = new GeoJson();
        for (int i = 0; i < list.size(); i++) {
            Set<RelatedObject> relatedObjects = list.get(i);
            String clusterId = i+"";
            List<GeoJson.Feature> features = relatedObjects.stream()
                    .map(relatedObject -> {
                        GeoJson.Geometry geometry = new GeoJson.Geometry(
                                relatedObject.getCoordinate().getLongitude(),
                                relatedObject.getCoordinate().getLatitude()
                        );
                        GeoJson.Properties properties = new GeoJson.Properties(clusterId, relatedObject.getLabels());
                        return new GeoJson.Feature(geometry, properties);
                    }).collect(Collectors.toList());
            geoJson.getFeatures().addAll(features);
        }
        return geoJson;

    }

    @Override
    public GeoJson loadGeoJson(Query query) {
        return doLoadGeoJson(query);
    }

    private List<Marker> doLoadMarkers(Query query){
        List<Set<RelatedObject>> list = kstc.kstcSearch(query);
        List<Marker> res = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            int size = list.get(i).size();
            Marker marker = new Marker();
            marker.setClusterId(i+"");
            marker.setPointNum(size);
            marker.setDescription("");
            Coordinate sum = list.get(i).stream().map(RelatedObject::getCoordinate).reduce(
                    new Coordinate(),
                    (a, b) -> {
                        a.setLongitude(a.getLongitude() + b.getLongitude());
                        a.setLatitude(a.getLatitude() + b.getLatitude());
                        return a;
                    }
            );
            marker.setCoordinate(Coordinate.create(
                    sum.getLongitude()/size,
                    sum.getLatitude()/size
            ));
            res.add(marker);
        }
        return res;
    }

    @Override
    public List<Marker> loadMarkers(Query query) {
        return doLoadMarkers(query);
    }
}
