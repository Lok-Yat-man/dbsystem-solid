package cn.edu.szu.cs.kstc.dbscan;

import cn.edu.szu.cs.entity.DbScanRelevantObject;
import cn.edu.szu.cs.entity.KstcQuery;
import cn.edu.szu.cs.kstc.Context;
import cn.edu.szu.cs.kstc.TopKSpatialTextualClustersRetrieval;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * 基于DBSCAN算法实现的Top-K空间文本聚类检索的抽象类
 * <p> Abstract class for Top-K spatial text cluster retrieval based on DBSCAN algorithm
 *
 * @author Whitence
 * @version 1.0
 * @date 2024/3/16 15:47
 */
@Data
@NoArgsConstructor
public abstract class AbstractDbScanBasedApproach<T extends DbScanRelevantObject> implements TopKSpatialTextualClustersRetrieval<T> {

    /**
     * 执行基于DBSCAN的检索方法前置操作
     * <p> before execute the retrieval method based on DBSCAN
     *
     * @param query
     */
    protected abstract void beforeDoDbScanBasedApproach(KstcQuery query);

    /**
     * 初始化上下文
     * <p> initialize the context
     *
     * @param query
     * @return
     */
    protected abstract Context<T> initContext(KstcQuery query);

    /**
     * 获取根据关键词查询出所有的对象并按照距离升序排序所得相关对象集合
     * <p> get the relevant object set obtained by querying all objects according to the keywords and sorting in ascending order of distance
     *
     * @param context@return
     */
    protected abstract SortedSet<T> getRelevantObjects(Context<T> context);

    /**
     * 初始化聚类结果集
     * <p> initialize the cluster result set
     * @param context
     * @return
     */
    protected abstract List<Set<T>> initClusters(Context<T> context);

    /**
     * 从SList和TList中获取下一个对象
     * <p> get the next object from SList and TList
     *  @param sList
     * @param context
     */
    protected abstract T getNextObject(SortedSet<T> sList, Context<T> context);

    /**
     * 根据当前位置以及关键词进行范围查询
     *
     * @param p
     * @param context
     * @return
     */
    protected abstract Queue<T> rangeQuery(T p, Context<T> context);

    /**
     * 判断是否可以跳过当前对象
     * <p> determine whether to skip the current object
     *
     * @param obj
     * @param context
     * @return
     */
    protected abstract boolean canSkip(T obj, Context<T> context);


    /**
     * 从SList和TList中移除对象
     * <p> remove object from SList and TList
     *  @param obj
     * @param sList
     * @param context
     */
    protected abstract void removeObject(T obj, SortedSet<T> sList, Context<T> context);

    /**
     * 判断是否可以提前结束
     * <p> determine whether to finish early
     * @param obj
     * @param cluster
     * @param context
     * @return
     */
    protected abstract boolean canFinishAdvanced(T obj,Set<T> cluster,  Context<T> context);

    protected abstract void afterAddToCluster(T obj, Set<T> cluster, Context<T> context);

    /**
     * 执行基于DBSCAN的检索方法后置操作
     * <p> after execute the retrieval method based on DBSCAN
     *
     * @param context
     */
    protected abstract void afterDoDbScanBasedApproach(Context<T> context);


    /**
     * 执行基于DBSCAN的检索方法
     * <p> execute the retrieval method based on DBSCAN
     *
     * @param query
     * @return
     */
    private List<Set<T>> doDbScanBasedApproach(KstcQuery query) {
        // checkout query
        checkoutQuery(query);
        // before do dbScanBasedApproach
        beforeDoDbScanBasedApproach(query);
        // init context
        Context<T> context = initContext(query);
        // sort objs ascent by distance
        SortedSet<T> sList = getRelevantObjects(context);
        // init clusters
        List<Set<T>> rList = initClusters(context);

        // noises
        Set<T> noises = new HashSet<>();
        // can finish early
        boolean canFinish = false;
        while (!sList.isEmpty() && rList.size() < query.getK() && !canFinish) {
            T obj = getNextObject(sList, context);

            Set<T> cluster = getCluster(obj, sList, noises, context);
            if (!cluster.isEmpty()) {
                rList.add(cluster);
                canFinish = canFinishAdvanced(obj,cluster, context);
            }

        }

        afterDoDbScanBasedApproach(context);

        return rList;
    }


    /**
     * 获取当前对象的聚类
     * <p> get the cluster of the current object
     *
     * @param p
     * @param sList
     * @param noises
     * @param context
     * @return
     */
    private Set<T> getCluster(T p, SortedSet<T> sList, Set<T> noises, Context<T> context) {

        KstcQuery q = context.getQuery();
        Queue<T> neighbors = rangeQuery(p, context);

        if (neighbors.size() < q.getMinPts()) {
            removeObject(p, sList, context);
            // mark p as noise
            noises.add(p);
            return Collections.emptySet();
        }

        Set<T> result = new HashSet<>(neighbors);
        neighbors.forEach(nei -> removeObject(nei, sList, context));
        neighbors.remove(p);

        while (!neighbors.isEmpty()) {

            T neighbor = neighbors.poll();
            // if neighbor can be skipped
            if (canSkip(neighbor, context)) {
                continue;
            }

            Queue<T> neighborsTmp = rangeQuery(neighbor, context);
            if (neighborsTmp.size() >= q.getMinPts()) {

                for (T obj : neighborsTmp) {
                    if (noises.contains(obj)) {
                        result.add(obj);
                    } else if (!result.contains(obj)) {
                        result.add(obj);
                        neighbors.add(obj);
                        removeObject(obj, sList, context);
                        afterAddToCluster(obj, result, context);
                    }
                }
            }
        }
        afterGetCluster(context);
        return result;
    }

    protected abstract void afterGetCluster(Context<T> context);

    @Override
    public List<Set<T>> kstcSearch(KstcQuery query) {
        return doDbScanBasedApproach(query);
    }
}
