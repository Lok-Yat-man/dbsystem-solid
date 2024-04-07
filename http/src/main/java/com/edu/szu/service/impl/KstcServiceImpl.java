package com.edu.szu.service.impl;

import cn.edu.szu.cs.adapter.KstcDataFetchManager;
import cn.edu.szu.cs.common.DataFetchCommandConstant;
import cn.edu.szu.cs.entity.DataFetchTask;
import cn.edu.szu.cs.entity.DbScanRelevantObject;
import cn.edu.szu.cs.entity.KstcQuery;
import cn.edu.szu.cs.entity.RelevantObject;
import com.alibaba.fastjson.JSON;
import com.edu.szu.entity.Coordinate;
import com.edu.szu.entity.GeoJson;
import com.edu.szu.entity.Marker;
import com.edu.szu.service.KstcService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@Service
public class KstcServiceImpl implements KstcService {



    private GeoJson doLoadGeoJson(KstcQuery query) {

        String actionId = KstcDataFetchManager.generateTask(DataFetchCommandConstant.SIMPLE_DBSCAN_BASED_APPROACH, JSON.toJSONString(query));
        DataFetchTask task = KstcDataFetchManager.getTask(actionId);

        List<Set<DbScanRelevantObject>> list = (List<Set<DbScanRelevantObject>>) task.getData();

        GeoJson geoJson = new GeoJson();
        for (int i = 0; i < list.size(); i++) {
            Set<DbScanRelevantObject> relatedObjects = list.get(i);
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

        String actionId = KstcDataFetchManager.generateTask(DataFetchCommandConstant.SIMPLE_DBSCAN_BASED_APPROACH, JSON.toJSONString(query));
        DataFetchTask task = KstcDataFetchManager.getTask(actionId);

        List<Set<DbScanRelevantObject>> list = (List<Set<DbScanRelevantObject>>) task.getData();
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
            Coordinate coordinate = Coordinate.create(
                    sum[0] / size,
                    sum[1] / size
            );
            marker.setCoordinate(coordinate);
            res.add(marker);
        }
        return res;
    }

    @Override
    public List<Marker> loadMarkers(KstcQuery query) {
        return doLoadMarkers(query);
    }
}
