package cn.edu.szu.cs.ivtidx;


import cn.edu.szu.cs.service.IRelatedObjectService;
import cn.edu.szu.cs.entity.RelatedObject;
import cn.edu.szu.cs.util.CommonUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.lang.Tuple;
import com.alibaba.fastjson.JSONObject;
import lombok.NonNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * DefaultLeafInvertedIndex
 *
 * @author Whitence
 * @version 1.0
 * @date 2023/10/17 21:26
 */
public class DefaultInvertedIndex implements InvertedIndex<RelatedObject> {

    private Map<String, Set<String>> map;

    private IRelatedObjectService relatedObjectService;


    public DefaultInvertedIndex(@NonNull IRelatedObjectService relatedObjectService) {
        this.relatedObjectService = relatedObjectService;
        map = new ConcurrentHashMap<>();
        List<RelatedObject> relatedObjects = relatedObjectService.getAll();


        for (RelatedObject relatedObject : relatedObjects) {
            List<String> labels = relatedObject.getLabels();
            if (CollUtil.isNotEmpty(labels)) {
                for (String label : labels) {
                    map.putIfAbsent(label, new HashSet<>());
                    map.get(label).add(relatedObject.getObjectId());
                }
            }
        }

        for (RelatedObject relatedObject : relatedObjects) {

            List<String> labels = relatedObject.getLabels();
            Map<String,Double> labelCnt = new HashMap<>(labels.size());
            for (String label : labels) {
                labelCnt.put(label,labelCnt.getOrDefault(label,0.0)+1);



            }



        }

    }

    @Override
    public synchronized SortedSet<RelatedObject> getSList(
            @NonNull List<String> keywords,
            double[] coordinate,
            double maxDistance,
            @NonNull Comparator<RelatedObject> comparator) {
        SortedSet<RelatedObject> relatedObjects = new TreeSet<>(comparator);
        if (CollUtil.isEmpty(keywords) || coordinate == null || coordinate.length != 2 || maxDistance <= 0) {
            return relatedObjects;
        }

        Set<String> objIds = null;
        for (int i = 0; i < keywords.size(); i++) {
            Set<String> set = map.get(keywords.get(i));
            if (i == 0) {
                objIds = set;
                continue;
            }
            if (CollUtil.isEmpty(set)) {
                break;
            }
            if (CollUtil.isEmpty(objIds)) {
                objIds = set;
                continue;
            }
            objIds.retainAll(set);
        }

        if (CollUtil.isEmpty(objIds)) {
            return relatedObjects;
        }

        relatedObjects.addAll(
                objIds.stream()
                        .filter(Objects::nonNull)
                        .map(relatedObjectService::getById)
                        .filter(Objects::nonNull)
                        .filter(relatedObject -> CommonUtil.calculateDistance(relatedObject.getCoordinate(), coordinate) < maxDistance)
                        .collect(Collectors.toList())
        );
        return relatedObjects;
    }

    @Override
    public SortedSet<RelatedObject> getTList(List<String> keywords) {



        return null;
    }

    @Override
    public Map<String, Set<String>> getAll() {
        return map;
    }

}
