package cn.edu.szu.cs.ivtidx;


import cn.edu.szu.cs.entity.DbScanRelevantObject;
import cn.edu.szu.cs.entity.RelevantObject;
import cn.edu.szu.cs.infrastructure.IRelevantObjectDataLoader;
import cn.edu.szu.cs.kstc.dbscan.DbScanBasedApproach;
import cn.edu.szu.cs.util.CommonUtil;
import cn.hutool.core.collection.CollUtil;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * DefaultLeafInvertedIndex
 *
 * @author Whitence
 * @version 1.0
 * @date 2023/10/17 21:26
 */
public class DefaultDBScanInvertedIndex implements DbScanBasedApproach.InvertedIndex<DbScanRelevantObject> {

    /**
     * inverted index
     * key: label
     * value: objIds
     */
    private final Map<String, Set<String>> map = new HashMap<>();

    private final IRelevantObjectDataLoader<DbScanRelevantObject> relevantLoader;

    public DefaultDBScanInvertedIndex(@NonNull IRelevantObjectDataLoader<DbScanRelevantObject> relevantLoader) {
        this.relevantLoader = relevantLoader;

        List<DbScanRelevantObject> relatedObjects = relevantLoader.getAll();

        if (CollUtil.isEmpty(relatedObjects)) {
            throw new IllegalArgumentException("relatedObjects is empty");
        }

        for (RelevantObject relatedObject : relatedObjects) {
            List<String> labels = relatedObject.getLabels();
            if (CollUtil.isNotEmpty(labels)) {
                for (String label : labels) {
                    map.putIfAbsent(label, new HashSet<>());
                    map.get(label).add(relatedObject.getObjectId());
                }
            }
        }
    }

    public Map<String, Set<String>> getAll() {
        return map;
    }

    /**
     * load data by keywords and coordinate and maxDistance and sort by distance ASC
     * @param keywords
     * @param coordinate
     * @param maxDistance
     * @return
     */
    @Override
    public SortedSet<DbScanRelevantObject> loadDataSortByDistanceAsc(List<String> keywords, double[] coordinate, double maxDistance) {

        if (CollUtil.isEmpty(keywords) || coordinate == null || coordinate.length > 2 || maxDistance <= 0) {
            return Collections.emptySortedSet();
        }

        List<String> objIds = getObjIds(keywords);
        if (CollUtil.isEmpty(objIds)) {
            return Collections.emptySortedSet();
        }

        List<DbScanRelevantObject> objList = relevantLoader.getByIds(objIds);

        SortedSet<DbScanRelevantObject> objs = new TreeSet<>(Comparator.comparingDouble(obj-> CommonUtil.calculateDistance(obj.getCoordinate(), coordinate)));
        objs.addAll(objList);
        return objs;
    }

    /**
     * get objIds by keywords
     * @param keywords
     * @return
     */
    private List<String> getObjIds(List<String> keywords) {

        return keywords.stream().map(map::get).filter(Objects::nonNull)
                .flatMap(Set::stream).distinct().collect(Collectors.toList());

    }

    /**
     * load data by keywords and sort by weight DESC
     * @param keywords
     * @return
     */
    @Override
    public SortedSet<DbScanRelevantObject> loadDataSortByWeightDesc(List<String> keywords) {
        SortedSet<DbScanRelevantObject> objs = new TreeSet<>(new Comparator<DbScanRelevantObject>() {
            @Override
            public int compare(DbScanRelevantObject o1, DbScanRelevantObject o2) {
                return o2.getWeight(keywords).compareTo(o1.getWeight(keywords));
            }
        });
        getObjIds(keywords).stream()
                .map(relevantLoader::getById)
                .filter(Objects::nonNull)
                .forEach(objs::add);
        return objs;
    }
}
