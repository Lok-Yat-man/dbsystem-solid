//package cn.edu.szu.cs.kstc.optics;
//
//import cn.edu.szu.cs.entity.OpticsRelevantObject;
//import cn.edu.szu.cs.kstc.RelevantObject;
//import cn.edu.szu.cs.infrastructure.dataloader.IRelevantObjectDataLoader;
//import cn.hutool.core.collection.CollUtil;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * DefaultOpticsInvertedIndex
// *
// * @author Whitence
// * @version 1.0
// * @date 2024/4/5 15:59
// */
//public class DefaultOpticsInvertedIndex<T extends OpticsRelevantObject> implements OpticsOg.InvertedIndex<T> {
//
//    /**
//     * inverted index
//     * key: label
//     * value: objIds
//     */
//    private final Map<String, Set<String>> map = new HashMap<>();
//
//    private final IRelevantObjectDataLoader<T> relevantLoader;
//
//    public DefaultOpticsInvertedIndex(IRelevantObjectDataLoader<T> relevantLoader) {
//        this.relevantLoader = relevantLoader;
//
//        List<T> relatedObjects = relevantLoader.getAll();
//
//        if (CollUtil.isEmpty(relatedObjects)) {
//            throw new IllegalArgumentException("relatedObjects is empty");
//        }
//
//        for (RelevantObject relatedObject : relatedObjects) {
//            List<String> labels = relatedObject.getLabels();
//            if (CollUtil.isNotEmpty(labels)) {
//                for (String label : labels) {
//                    map.putIfAbsent(label, new HashSet<>());
//                    map.get(label).add(relatedObject.getObjectId());
//                }
//            }
//        }
//    }
//
//    @Override
//    public List<T> getRelevantObjects(List<String> keywords) {
//
//        if (CollUtil.isEmpty(keywords)) {
//            return Collections.emptyList();
//        }
//        List<String> objIds = getObjIds(keywords);
//
//        return relevantLoader.getByIds(objIds);
//
//    }
//
//
//    /**
//     * get objIds by keywords
//     *
//     * @param keywords
//     * @return
//     */
//    private List<String> getObjIds(List<String> keywords) {
//
//        return keywords.stream().map(map::get).filter(Objects::nonNull)
//                .flatMap(Set::stream).distinct().collect(Collectors.toList());
//
//    }
//}
