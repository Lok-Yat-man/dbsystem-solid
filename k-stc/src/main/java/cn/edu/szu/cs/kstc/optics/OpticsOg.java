package cn.edu.szu.cs.kstc.optics;

import cn.edu.szu.cs.adapter.KstcDataFetchManager;
import cn.edu.szu.cs.constant.DataFetchConstant;
import cn.edu.szu.cs.entity.DataFetchResult;
import cn.edu.szu.cs.entity.KstcQuery;
import cn.edu.szu.cs.entity.OpticsRelevantObject;
import cn.edu.szu.cs.kstc.Context;
import cn.edu.szu.cs.util.CommonUtil;
import cn.edu.szu.cs.util.TimerHolder;
import cn.hutool.core.convert.Convert;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OpticsOg
 *
 * @author Whitence
 * @version 1.0
 * @date 2024/4/5 11:12
 */
@SuppressWarnings("all")
public class OpticsOg extends AbstractOpticsBasedApproach<OpticsRelevantObject> {

    private Log log = LogFactory.get();

    @EqualsAndHashCode(callSuper = true)
    @Data
    private static class OpticsOgContext extends Context<OpticsRelevantObject> {

        private Map<OpticsRelevantObject, Double> reachabilityDistanceMap;

        private Map<OpticsRelevantObject, Double> coreDistanceMap;
    }


    @Override
    protected Context<OpticsRelevantObject> initContext(KstcQuery query) {
        OpticsOgContext context = new OpticsOgContext();
        context.setQuery(query);
        context.setReachabilityDistanceMap(new ConcurrentHashMap<>());
        context.setCoreDistanceMap(new ConcurrentHashMap<>());
        return context;
    }

    @Override
    protected void beforeOpticsBasedApproach(Context<OpticsRelevantObject> context) {
        TimerHolder.start("OpticsOg");
    }

    @Override
    protected List<OpticsRelevantObject> generateResultQueue(Context<OpticsRelevantObject> context) {

        OpticsOgContext opticsOgContext = (OpticsOgContext) context;
        KstcQuery query = opticsOgContext.getQuery();

        DataFetchResult dataFetchResult = KstcDataFetchManager.generateTaskAndGet(
                DataFetchConstant.INFRASTRUCTURE_LAYER,
                DataFetchConstant.LOAD_OPTICS_DATA_BY_KEYWORDS,
                JSON.toJSONString(query)
        );
        if (!dataFetchResult.isSuccess()) {
            throw new RuntimeException("OpticsOg Data Fetch Error");
        }

        List<OpticsRelevantObject> opticsRelevantObjects = Convert.toList(OpticsRelevantObject.class, dataFetchResult.getData());

        opticsRelevantObjects.sort(Comparator.comparing(obj->CommonUtil.calculateDistance(query.getCoordinate(), obj.getCoordinate())));

        //for (OpticsRelevantObject opticsRelevantObject : opticsRelevantObjects) {
        //
        //    query.setCoordinate(opticsRelevantObject.getCoordinate());
        //
        //    KstcDataFetchManager.generateTask(
        //            DataFetchConstant.INFRASTRUCTURE_LAYER,
        //            DataFetchConstant.OPTICS_RTREE_RANGE_QUERY,
        //            JSON.toJSONString(query)
        //    );
        //
        //}

        Set<OpticsRelevantObject> resultQueue = new LinkedHashSet<>();


        for (OpticsRelevantObject opticsRelevantObject : opticsRelevantObjects) {

            if (resultQueue.contains(opticsRelevantObject)) {
                continue;
            }

            resultQueue.add(opticsRelevantObject);


            query.setCoordinate(opticsRelevantObject.getCoordinate());
            DataFetchResult rangeQueryDataFetchResult = KstcDataFetchManager.generateTaskAndGet(
                    DataFetchConstant.INFRASTRUCTURE_LAYER,
                    DataFetchConstant.OPTICS_RTREE_RANGE_QUERY,
                    JSON.toJSONString(query)
            );
            if (!rangeQueryDataFetchResult.isSuccess()) {
                throw new RuntimeException("OpticsOg Range Query Error");
            }

            List<OpticsRelevantObject> rangeQueryResult = Convert.toList(OpticsRelevantObject.class, rangeQueryDataFetchResult.getData());

            // 如果范围查询结果小于MinPts，则跳过
            if (rangeQueryResult.size() < query.getMinPts()) {
                continue;
            }

            // 计算核心距离
            OpticsRelevantObject theMinPtsObject = rangeQueryResult.get(query.getMinPts() - 1);
            Double coreDistance = CommonUtil.calculateDistance(opticsRelevantObject.getCoordinate(), theMinPtsObject.getCoordinate());
            opticsOgContext.getCoreDistanceMap().put(opticsRelevantObject, coreDistance);

            Map<OpticsRelevantObject, Double> reachabilityDistanceMap = opticsOgContext.getReachabilityDistanceMap();
            SortedSet<OpticsRelevantObject> sortedSet = new TreeSet<>(
                    new Comparator<OpticsRelevantObject>() {
                        @Override
                        public int compare(OpticsRelevantObject o1, OpticsRelevantObject o2) {
                            int compare = Double.compare(reachabilityDistanceMap.getOrDefault(o1, Double.MAX_VALUE), reachabilityDistanceMap.getOrDefault(o2, Double.MAX_VALUE));
                            if (compare == 0) {
                                return o1.compareTo(o2);
                            }
                            return compare;
                        }
                    }
            );
            // 计算每一个对象的可达距离
            for (OpticsRelevantObject relevantObject : rangeQueryResult) {

                if (resultQueue.contains(relevantObject)) {
                    continue;
                }

                Double oriDist = reachabilityDistanceMap.getOrDefault(relevantObject, Double.MAX_VALUE);
                Double rDist = Math.max(coreDistance, CommonUtil.calculateDistance(opticsRelevantObject.getCoordinate(), relevantObject.getCoordinate()));

                reachabilityDistanceMap.put(relevantObject, Math.min(oriDist, rDist));
                sortedSet.add(relevantObject);
            }


            while (!sortedSet.isEmpty()) {
                OpticsRelevantObject nextObject = sortedSet.first();
                sortedSet.remove(nextObject);

                resultQueue.add(nextObject);

                query.setCoordinate(nextObject.getCoordinate());
                DataFetchResult nextRangeQueryDataFetchResult = KstcDataFetchManager.generateTaskAndGet(
                        DataFetchConstant.INFRASTRUCTURE_LAYER,
                        DataFetchConstant.OPTICS_RTREE_RANGE_QUERY,
                        JSON.toJSONString(query)
                );
                if (!nextRangeQueryDataFetchResult.isSuccess()) {
                    throw new RuntimeException("OpticsOg Range Query Error");
                }

                List<OpticsRelevantObject> nextRangeQueryResult = Convert.toList(OpticsRelevantObject.class, nextRangeQueryDataFetchResult.getData());

                // 如果范围查询结果小于MinPts，则跳过
                if (nextRangeQueryResult.size() < query.getMinPts()) {
                    continue;
                }

                // 计算核心距离
                OpticsRelevantObject nextTheMinPtsObject = nextRangeQueryResult.get(query.getMinPts() - 1);
                Double nextCoreDistance = CommonUtil.calculateDistance(nextObject.getCoordinate(), nextTheMinPtsObject.getCoordinate());
                opticsOgContext.getCoreDistanceMap().put(nextObject, nextCoreDistance);

                // 计算每一个对象的可达距离
                for (OpticsRelevantObject relevantObject : nextRangeQueryResult) {

                    if (resultQueue.contains(relevantObject)) {
                        continue;
                    }
                    if (sortedSet.contains(relevantObject)) {
                        sortedSet.remove(relevantObject);
                    }

                    Double oriDist = reachabilityDistanceMap.getOrDefault(relevantObject, Double.MAX_VALUE);
                    Double rDist = Math.max(nextCoreDistance, CommonUtil.calculateDistance(nextObject.getCoordinate(), relevantObject.getCoordinate()));

                    reachabilityDistanceMap.put(relevantObject, Math.min(oriDist, rDist));
                    sortedSet.add(relevantObject);
                }

            }


        }

        return new ArrayList<>(resultQueue);
    }

    @Override
    protected void afterOpticsBasedApproach(Context<OpticsRelevantObject> context) {
        long opticsOg = TimerHolder.stop("OpticsOg");
        log.info("OpticsOg Time Cost: {}", opticsOg);
    }
}
