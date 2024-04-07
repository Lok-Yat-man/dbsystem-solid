package com.edu.szu.service.impl;

import cn.edu.szu.cs.entity.KstcQuery;
import cn.edu.szu.cs.entity.RelevantObject;
import cn.edu.szu.cs.kstc.TopKSpatialTextualClustersRetrieval;
import com.edu.szu.entity.GeoJson;
import com.edu.szu.entity.Marker;
import com.edu.szu.service.KstcService;
import entity.Coordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class KstcServiceImpl<T extends RelevantObject> implements KstcService<T> {

    private TopKSpatialTextualClustersRetrieval<T> kstc;

    public KstcServiceImpl(TopKSpatialTextualClustersRetrieval<T> kstc) {
        this.kstc = kstc;
    }


    private GeoJson doLoadGeoJson(KstcQuery query) {
        KSTCResult<T> kstcResult = kstc.kstcSearch(query);
        List<Set<T>> list = kstcResult.getClusters();
        GeoJson geoJson = new GeoJson();
        for (int i = 0; i < list.size(); i++) {
            Set<T> relatedObjects = list.get(i);
            String clusterId = i + "";
            List<GeoJson.Feature> features = relatedObjects.stream()
                    .map(relatedObject -> {
                        GeoJson.Geometry geometry = new GeoJson.Geometry(
                                relatedObject.getCoordinate()[0],
                                relatedObject.getCoordinate()[1]
                        );
                        GeoJson.Properties properties = new GeoJson.Properties(clusterId, relatedObject.getName(), relatedObject.getLabels());
                        return new GeoJson.Feature(geometry, properties);
                    }).collect(Collectors.toList());
            geoJson.getFeatures().addAll(features);
        }
        return geoJson;

    }

    @Override
    public GeoJson loadGeoJson(KstcQuery query) {
        return doLoadGeoJson(query);
    }

    private List<Marker> doLoadMarkers(KstcQuery query) {
        KSTCResult<T> kstcResult = kstc.kstcSearch(query);
        List<Set<T>> list = kstcResult.getClusters();
        List<Marker> res = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            int size = list.get(i).size();
            Marker marker = new Marker();
            marker.setClusterId(i + "");
            marker.setPointNum(size);
            marker.setDescription("");
            double[] sum = list.get(i).stream().map(RelevantObject::getCoordinate).reduce(
                    new double[]{0, 0},
                    (a, b) -> {
                        a[0] += b[0];
                        a[1] += b[1];
                        return a;
                    }
            );
            marker.setCoordinate(Coordinate.create(
                    sum[0] / size,
                    sum[1] / size
            ));
            res.add(marker);
        }
        return res;
    }

    @Override
    public List<Marker> loadMarkers(KstcQuery query) {
        return doLoadMarkers(query);
    }
}
