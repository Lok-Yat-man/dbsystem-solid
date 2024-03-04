package cn.edu.szu.cs.strategy.impl;

import cn.edu.szu.cs.entity.KSTCQuery;
import cn.edu.szu.cs.entity.KSTCResult;
import cn.edu.szu.cs.entity.RelatedObject;
import cn.edu.szu.cs.strategy.CacheStrategy;
import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.LFUCache;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.ref.SoftReference;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("all")
@Data
@AllArgsConstructor
public class DefaultCacheStrategy<T extends RelatedObject> implements CacheStrategy<T> {

    /**
     * cache key
     */
    private static String cacheKey = "TopKSpatialTextualClustersRetrievalQueryCacheKey:{0}:{1}:{2}:{3}:{4}:{5}";

    /**
     * logger
     */
    private static Log logger = LogFactory.get();


    private final int capacity;
    private final int timeout;
    private final boolean deepCopy;

    public DefaultCacheStrategy() {
        // 128
        capacity = 128;
        // 30 minutes
        timeout = 30 * 60 * 1000;

        deepCopy = false;
    }

    /**
     * The main cache is used to cache results. Using soft references ensures that there will be no out-of-memory errors.
     */
    private static volatile SoftReference<LFUCache<String, Object>> mainCache = null;

    private LFUCache<String, Object> mainCache() {

        try {
            if (mainCache == null || mainCache.get() == null) {
                synchronized (DefaultCacheStrategy.class) {
                    if (mainCache == null) {
                        mainCache = new SoftReference<>(CacheUtil.newLFUCache(capacity, timeout));
                        logger.info("Main cache initialized successfully.");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Main cache initialization failed.", e);
        }

        return Optional.ofNullable(mainCache.get()).orElse(null);
    }


    public String getCacheKey(KSTCQuery query) {
        double lon = Math.round(query.getCoordinate()[0] * 1000.0) / 1000.0;
        double lat = Math.round(query.getCoordinate()[1] * 1000.0) / 1000.0;
        long epsilon = Math.round(query.getEpsilon());
        int minPts = query.getMinPts();
        long maxDist = Math.round(query.getMaxDistance());
        return MessageFormat.format(cacheKey, lon, lat, query.getKeywords(), epsilon, minPts, maxDist);
    }

    @Override
    public void set(KSTCQuery query, KSTCResult<T> kstcResult) {

        String cacheKey = getCacheKey(query);

        LFUCache<String, Object> cache = mainCache();

        if (cache == null) {
            return;
        }

        cache.put(cacheKey, kstcResult.getClusters());
    }

    @Override
    public KSTCResult<T> get(KSTCQuery query) {

        String cacheKey = getCacheKey(query);

        LFUCache<String, Object> cache = mainCache();

        if (cache == null) {
            return KSTCResult.fail("Main cache is not initialized.");
        }

        List<Set<T>> clusters = (List<Set<T>>) cache.get(cacheKey);

        if (CollUtil.isEmpty(clusters)) {
            return KSTCResult.fail("No data found in the cache.");
        }

        if (!deepCopy) {
            return KSTCResult.success(clusters.subList(0, query.getK()));
        }

        List<Set<T>> result = new ArrayList<>(query.getK());

        Iterator<Set<T>> iterator = clusters.iterator();
        int idx = 0;

        while (iterator.hasNext() && idx < query.getK()) {
            Set<T> cluster = iterator.next();
            result.add(cluster.stream().map(obj -> (T) obj.clone()).collect(Collectors.toSet()));
            idx++;
        }

        return KSTCResult.success(clusters);
    }


}
