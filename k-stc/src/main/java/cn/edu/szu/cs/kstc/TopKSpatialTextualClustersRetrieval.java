package cn.edu.szu.cs.kstc;

import cn.edu.szu.cs.entity.KstcQuery;
import cn.hutool.core.lang.Assert;

import java.util.List;
import java.util.Set;

/**
 * TopKSpatialTextualClustersRetrieval
 * @param <T>
 */
public interface TopKSpatialTextualClustersRetrieval<T> {


    default void checkoutQuery(KstcQuery query) {
        Assert.isTrue(query.getCoordinate() != null && query.getCoordinate().length == 2, "Please enter coordinate correctly.");
        Assert.checkBetween(query.getCoordinate()[0], -180.0, 180.0, "wrong longitude.");
        Assert.checkBetween(query.getCoordinate()[1], -90.0, 90.0, "wrong latitude.");
        Assert.checkBetween(query.getK(), 1, 200, "wrong k.");
        Assert.checkBetween(query.getEpsilon(), 0.0, Double.MAX_VALUE, "wrong epsilon.");
        Assert.checkBetween(query.getMinPts(), 2, Integer.MAX_VALUE, "wrong minPts.");
        Assert.checkBetween(query.getMaxDistance(), 0.0, Double.MAX_VALUE, "wrong maxDistance.");
        Assert.notNull(query.getKeywords(), "keywords is null.");
        Assert.isFalse(query.getKeywords().isEmpty(), "keywords is empty.");
    }

    /**
     * kstcSearch
     * @param query
     * @return
     */
    List<Set<T>> kstcSearch(KstcQuery query);

}
