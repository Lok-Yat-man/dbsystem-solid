package cn.edu.szu.cs.kstc;

import cn.edu.szu.cs.entity.KstcQuery;

import java.util.List;
import java.util.Set;

/**
 * TopKSpatialTextualClustersRetrieval
 * @param <T>
 */
public interface TopKSpatialTextualClustersRetrieval<T> {

    /**
     * kstcSearch
     * @param query
     * @return
     */
    List<Set<T>> kstcSearch(KstcQuery query);

}
