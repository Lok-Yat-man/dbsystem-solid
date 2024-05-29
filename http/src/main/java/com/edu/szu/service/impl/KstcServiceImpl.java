package com.edu.szu.service.impl;

import cn.edu.szu.cs.adapter.KstcDataFetchManager;
import cn.edu.szu.cs.constant.DataFetchConstant;
import cn.edu.szu.cs.dto.LabelPrefixMatchQueryDTO;
import cn.edu.szu.cs.entity.DataFetchResult;
import cn.edu.szu.cs.entity.KstcQuery;
import cn.edu.szu.cs.kstc.RelevantObject;
import cn.edu.szu.cs.util.CommonUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.edu.szu.api.Result;
import com.edu.szu.dto.DataCoordinateRangeDTO;
import com.edu.szu.entity.Coordinate;
import com.edu.szu.entity.GeoJson;
import com.edu.szu.entity.Marker;
import com.edu.szu.service.KstcService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("all")
@Service
@Log4j2
public class KstcServiceImpl implements KstcService {


    public KstcServiceImpl() {



    }

    private List<Set<RelevantObject>> kstcSearch(KstcQuery query) {

        DataFetchResult task = KstcDataFetchManager.generateTaskAndGet(
                DataFetchConstant.OPERATIONAL_LAYER,
                query.getCommand(), JSON.toJSONString(query));

        if (!task.isSuccess()) {
            throw new RuntimeException("Task failed: " + task.getMsg());
        }
        return (List<Set<RelevantObject>>) task.getData();
    }

    private GeoJson doLoadGeoJson(KstcQuery query) {

        List<Set<RelevantObject>> list = kstcSearch(query);
        log.info("query:{}, list.size:{} ",JSON.toJSONString(query), list.size());
        GeoJson geoJson = new GeoJson();
        for (int i = 0; i < list.size(); i++) {
            Set<RelevantObject> relatedObjects = list.get(i);
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

        List<Set<RelevantObject>> list = kstcSearch(query);
        log.info("query:{}, list.size:{} ",JSON.toJSONString(query), list.size());
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

    @Override
    public Result<DataCoordinateRangeDTO> getDataCoordinateRange(KstcQuery query) {

        List<Set<RelevantObject>> list = null;
        try {
            list = kstcSearch(query);
        } catch (Exception e) {
            log.error("getDataCoordinateRange error", e);
            return Result.failed(e.getMessage());
        }

        double[] min = new double[]{180, 90};
        double[] max = new double[]{-180, -90};

        if(list.isEmpty()){
            return Result.success(new DataCoordinateRangeDTO(0.0,0,query.getCoordinate()[0],query.getCoordinate()[1]));
        }

        for (Set<RelevantObject> set : list) {
            for (RelevantObject relevantObject : set) {
                double[] coordinate = relevantObject.getCoordinate();
                min[0] = Math.min(min[0], coordinate[0]);
                min[1] = Math.min(min[1], coordinate[1]);
                max[0] = Math.max(max[0], coordinate[0]);
                max[1] = Math.max(max[1], coordinate[1]);
            }
        }
        log.info("min:{}, max:{}", min, max);
        Double distance = CommonUtil.calculateDistance(new double[]{min[0],max[1]}, new double[]{max[0],min[1]});
        log.info("distance:{}", distance);
        double[] center = new double[]{(min[0] + max[0]) / 2, (min[1] + max[1]) / 2};

        return Result.success(new DataCoordinateRangeDTO(distance,list.size(),center[0],center[1]));
    }

    @Override
    public List<String> getKeyWords(String keywords) {

        if(StrUtil.isBlank(keywords)){
            return Collections.emptyList();
        }
        String[] split = keywords.split(" ");
        String keyword = split[split.length - 1];

        LabelPrefixMatchQueryDTO labelPrefixMatchQueryDTO = new LabelPrefixMatchQueryDTO();
        labelPrefixMatchQueryDTO.setKeyword(keyword);

        DataFetchResult dataFetchResult = KstcDataFetchManager
                .generateTaskAndGet(
                        DataFetchConstant.OPERATIONAL_LAYER,
                        DataFetchConstant.PREFIX_MATCH_KEYWORDS,
                        JSON.toJSONString(labelPrefixMatchQueryDTO)
                );

        return (List<String>) dataFetchResult.getData();
    }
}
