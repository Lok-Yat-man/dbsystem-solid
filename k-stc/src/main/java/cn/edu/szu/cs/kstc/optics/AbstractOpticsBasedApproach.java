package cn.edu.szu.cs.kstc.optics;

import cn.edu.szu.cs.entity.KstcQuery;
import cn.edu.szu.cs.entity.OpticsRelevantObject;
import cn.edu.szu.cs.kstc.Context;
import cn.edu.szu.cs.kstc.TopKSpatialTextualClustersRetrieval;
import cn.edu.szu.cs.util.CommonUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 *  OpticsBasedApproach
 * @author Whitence
 * @date 2024/4/5 12:03
 * @version 1.0
 */
public abstract class AbstractOpticsBasedApproach<T extends OpticsRelevantObject> implements TopKSpatialTextualClustersRetrieval<T> {


    private List<Set<T>> doOpticsBasedApproach(KstcQuery query) {

        checkoutQuery(query);
        Context<T> context = initContext(query);
        beforeOpticsBasedApproach(context);

        List<T> resultQueue = generateResultQueue(context);
        afterGenerateResultQueue(context);
        List<Set<T>> clusters = getClusters(resultQueue, context);
        afterOpticsBasedApproach(context);
        return clusters;
    }

    private List<Set<T>> getClusters(List<T> resultQueue,Context<T> context) {
        KstcQuery query = context.getQuery();
        List<Set<T>> clusters = new ArrayList<>();

        Set<T> cluster = new HashSet<>();
        for (T object : resultQueue) {

            // if reachable distance is less than epsilon, add to cluster
            if(Optional.ofNullable(object.getReachableDistance()).orElse(Double.MAX_VALUE) <= query.getEpsilon()){
                cluster.add(object);
                continue;
            }

            // if core distance is less than epsilon, create a new cluster and add the point to cluster
            if(Optional.ofNullable(object.getCoreDistance()).orElse(Double.MAX_VALUE) <= query.getEpsilon()){
                if(!cluster.isEmpty()){
                    clusters.add(cluster);
                    cluster = new HashSet<>();
                }
                cluster.add(object);
            }

        }

        return clusters.stream()
                .sorted(
                        Comparator.comparingDouble(
                                cls -> cls.stream()
                                        .map(
                                                obj -> CommonUtil.calculateDistance(
                                                        query.getCoordinate(), obj.getCoordinate()
                                                )
                                        ).min(Double::compareTo).orElse(0.0)
                        )
                ).limit(query.getK()).collect(Collectors.toList());

    }

    protected abstract Context<T> initContext(KstcQuery query);

    protected abstract void beforeOpticsBasedApproach(Context<T> context);

    protected abstract List<T> generateResultQueue(Context<T> context);

    protected abstract void afterOpticsBasedApproach(Context<T> context);

    protected abstract void afterGenerateResultQueue(Context<T> context);

    @Override
    public List<Set<T>> kstcSearch(KstcQuery query) {
        return doOpticsBasedApproach(query);
    }
}
