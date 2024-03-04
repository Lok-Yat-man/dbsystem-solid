package cn.edu.szu.cs.kstc;

import cn.edu.szu.cs.config.KSTCConfig;
import cn.edu.szu.cs.entity.KSTCQuery;
import cn.edu.szu.cs.entity.KSTCResult;
import cn.edu.szu.cs.entity.RelatedObject;
import cn.edu.szu.cs.irtree.IRTree;
import cn.edu.szu.cs.ivtidx.InvertedIndex;
import cn.edu.szu.cs.strategy.CacheStrategy;
import cn.edu.szu.cs.util.CommonUtil;
import cn.edu.szu.cs.util.TimerHolder;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DBScanBasedApproach<T extends RelatedObject> implements TopKSpatialTextualClustersRetrieval<T> {

    /**
     * logger
     */
    private static final Log logger = LogFactory.get();

    private CacheStrategy<T> cacheStrategy;

    private InvertedIndex<T> invertedIndex;

    private IRTree<T> irTree;


    @Override
    public KSTCResult<T> kstcSearch(@NonNull KSTCQuery query) {
        checkKSTCQuery(query);
        TimerHolder.start();
        KSTCResult<T> tkstcResult = dbScanBasedApproach(query);
        long stop = TimerHolder.stop();
        logger.info("KSTC search time cost: {}", stop);
        return tkstcResult;
    }


    private KSTCResult<T> dbScanBasedApproach(KSTCQuery query) {

        // check cache
        KSTCResult<T> kstcResult = cacheStrategy.get(query);

        do {
            if (kstcResult == null || KSTCResult.FAIL == kstcResult.getCode() || CollUtil.isEmpty(kstcResult.getClusters())) {
                break;
            }
            if (KSTCResult.RUNNING == kstcResult.getCode() && KSTCConfig.ASYNC) {
                logger.info("Cache is running.");
                return kstcResult;
            }
        } while (false);

        // compute
        List<Set<T>> compute = compute(query);
        kstcResult = KSTCResult.success(compute);

        cacheStrategy.set(query, kstcResult);

        return kstcResult;
    }


    private List<Set<T>> compute(KSTCQuery query) {
        query.setKeywords(
                query.getKeywords().stream().map(String::toLowerCase).collect(Collectors.toList())
        );

        // sList
        Set<T> noises = new HashSet<>();
        // sort objs ascent by distance
        SortedSet<T> sList = invertedIndex.getSList(
                query.getKeywords(),
                query.getCoordinate(),
                query.getMaxDistance(),
                Comparator.comparingDouble(a -> CommonUtil.calculateDistance(query.getCoordinate(), a.getCoordinate()))
        );

        List<Set<T>> rList = new ArrayList<>(query.getK());

        double bound = 0.0;
        double t = Double.MAX_VALUE;

        while (!sList.isEmpty() && rList.size() < query.getK() && bound < t) {
            // get the nearest obj
            T obj = sList.;
            Set<T> cluster = getCluster(obj, query, sList, noises);
            if (!cluster.isEmpty()) {
                rList.add(cluster);
                t = getScore(cluster);
                bound = getBound();
            }

        }

        return rList;
    }

    private double getScore(Set<T> cluster) {
        return Double.MAX_VALUE;
    }

    private double getBound() {
        return 0.0;
    }


    private Set<T> getCluster(T p,
                              KSTCQuery q,
                              SortedSet<T> sList,
                              Set<T> noises) {

        List<T> neighbors = irTree.rangeQuery(q.getKeywords(), p.getCoordinate(), q.getEpsilon());

        if (neighbors.size() < q.getMinPts()) {
            // mark p
            noises.add(p);
            return Collections.emptySet();
        }
        Set<T> result = new HashSet<>(neighbors);
        neighbors.remove(p);
        while (!neighbors.isEmpty()) {
            T neighbor = neighbors.remove(0);
            sList.remove(neighbor);
            List<T> neighborsTmp = irTree.rangeQuery(q.getKeywords(), neighbor.getCoordinate(), q.getEpsilon());
            if (neighborsTmp.size() >= q.getMinPts()) {
                for (T obj : neighborsTmp) {
                    if (noises.contains(obj)) {
                        result.add(obj);
                    } else if (!result.contains(obj)) {
                        result.add(obj);
                        neighbors.add(obj);
                    }
                }
            }
        }
        return result;
    }


    private void checkKSTCQuery(KSTCQuery query) {
        Assert.isTrue(query.getCoordinate() != null && query.getCoordinate().length == 2, "Please enter coordinate correctly.");
        Assert.checkBetween(query.getCoordinate()[0], -180.0, 180.0, "wrong longitude.");
        Assert.checkBetween(query.getCoordinate()[1], -90.0, 90.0, "wrong latitude.");
        Assert.checkBetween(query.getK(), 1, 20, "wrong k.");
        Assert.checkBetween(query.getEpsilon(), 1.0, Double.MAX_VALUE, "wrong epsilon.");
        Assert.checkBetween(query.getMinPts(), 2, Integer.MAX_VALUE, "wrong minPts.");
        Assert.checkBetween(query.getMaxDistance(), -1, Double.MAX_VALUE, "wrong maxDistance.");
        Assert.notNull(query.getKeywords(), "keywords is null.");
        Assert.isFalse(query.getKeywords().isEmpty(), "keywords is empty.");
    }
}
