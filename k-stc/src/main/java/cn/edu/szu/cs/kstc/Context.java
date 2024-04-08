package cn.edu.szu.cs.kstc;

import cn.edu.szu.cs.entity.KstcQuery;

import java.util.List;
import java.util.Set;

/**
 *  记录算法运行过程中的上下文信息
 *  <p> Record the context information during the operation of the algorithm.
 * @author Whitence
 * @date 2024/4/6 11:01
 * @version 1.0
 */
public interface Context<T> {

    /**
     * 获取查询
     * <p> Get the query
     * @return
     */
    KstcQuery getQuery();

    /**
     * 设置查询
     * <p> Set the query
     * @param query
     */
    void setQuery(KstcQuery query);

    /**
     * 获取聚类
     * <p> Get the clusters
     * @return
     */
    List<Set<T>> getClusters();

    /**
     * 设置聚类
     * <p> Set the clusters
     * @param clusters
     */
    void setClusters(List<Set<T>> clusters);

}
