package com.edu.szu.service.impl;

import cn.edu.szu.cs.entity.Coordinate;
import cn.edu.szu.cs.kstc.KSTC;
import cn.edu.szu.cs.entity.KSTCQuery;
import cn.edu.szu.cs.entity.RelatedObject;
import com.edu.szu.entity.GeoJson;
import com.edu.szu.entity.Marker;
import com.edu.szu.service.KstcService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class KstcServiceImpl implements KstcService {

    private KSTC<RelatedObject> kstc;

    public KstcServiceImpl(KSTC<RelatedObject> kstc) {
        this.kstc = kstc;
    }


    private GeoJson doLoadGeoJson(KSTCQuery KSTCQuery){
        List<Set<RelatedObject>> list = kstc.kstcSearch(KSTCQuery);
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
                        GeoJson.Properties properties = new GeoJson.Properties(clusterId, relatedObject.getName(), relatedObject.getLabels());
                        return new GeoJson.Feature(geometry, properties);
                    }).collect(Collectors.toList());
            geoJson.getFeatures().addAll(features);
        }
        return geoJson;

    }

    @Override
    public GeoJson loadGeoJson(KSTCQuery KSTCQuery) {
        return doLoadGeoJson(KSTCQuery);
    }

    private List<Marker> doLoadMarkers(KSTCQuery KSTCQuery){
        List<Set<RelatedObject>> list = kstc.kstcSearch(KSTCQuery);
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
    public List<Marker> loadMarkers(KSTCQuery KSTCQuery) {
        return doLoadMarkers(KSTCQuery);
    }
}
