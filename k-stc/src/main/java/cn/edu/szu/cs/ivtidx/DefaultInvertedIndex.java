package cn.edu.szu.cs.ivtidx;


import cn.edu.szu.cs.entity.DefaultRelatedObject;
import cn.edu.szu.cs.service.IRelatedObjectService;
import cn.edu.szu.cs.entity.RelatedObject;
import cn.hutool.core.collection.CollUtil;
import lombok.NonNull;

import java.util.*;

/**
 * DefaultLeafInvertedIndex
 *
 * @author Whitence
 * @version 1.0
 * @date 2023/10/17 21:26
 */
public class DefaultInvertedIndex implements InvertedIndex<DefaultRelatedObject> {

    private final Map<String, Set<String>> map = new HashMap<>();

    private final IRelatedObjectService<DefaultRelatedObject> relatedObjectService;

    public DefaultInvertedIndex(@NonNull IRelatedObjectService<DefaultRelatedObject> relatedObjectService) {
        this.relatedObjectService = relatedObjectService;

        List<DefaultRelatedObject> relatedObjects = relatedObjectService.getAll();

        if (CollUtil.isEmpty(relatedObjects)) {
            throw new IllegalArgumentException("relatedObjects is empty");
        }

        for (RelatedObject relatedObject : relatedObjects) {
            List<String> labels = relatedObject.getLabels();
            if (CollUtil.isNotEmpty(labels)) {
                for (String label : labels) {
                    map.putIfAbsent(label, new HashSet<>());
                    map.get(label).add(relatedObject.getObjectId());
                }
            }
        }
    }

    @Override
    public synchronized SortedMap<DefaultRelatedObject, Boolean> getSList(
            @NonNull List<String> keywords,
            double[] coordinate,
            double maxDistance,
            @NonNull Comparator<DefaultRelatedObject> comparator) {

        SortedMap<DefaultRelatedObject, Boolean> relatedObjects = new TreeMap<>(comparator);
        if (CollUtil.isEmpty(keywords) || coordinate == null || coordinate.length > 2 || maxDistance <= 0) {
            return relatedObjects;
        }

        getObjIds(keywords).stream()
                .map(relatedObjectService::getById)
                .filter(Objects::nonNull)
                .forEach(obj -> relatedObjects.put(obj, Boolean.TRUE));
        return relatedObjects;
    }

    private Set<String> getObjIds(List<String> keywords) {
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
        return objIds;
    }

    @Override
    public SortedMap<DefaultRelatedObject, Boolean> getTList(List<String> keywords) {
        SortedMap<DefaultRelatedObject, Boolean> relatedObjects = new TreeMap<>(new Comparator<DefaultRelatedObject>() {
            @Override
            public int compare(DefaultRelatedObject o1, DefaultRelatedObject o2) {
                return o2.getWeight(keywords).compareTo(o1.getWeight(keywords));
            }
        });
        getObjIds(keywords).stream()
                .map(relatedObjectService::getById)
                .filter(Objects::nonNull)
                .forEach(obj -> relatedObjects.put(obj, Boolean.TRUE));
        return relatedObjects;
    }

    @Override
    public Map<String, Set<String>> getAll() {
        return map;
    }

}
