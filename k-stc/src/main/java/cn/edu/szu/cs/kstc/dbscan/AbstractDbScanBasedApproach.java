package cn.edu.szu.cs.kstc.dbscan;

import cn.edu.szu.cs.entity.DbScanRelevantObject;
import cn.edu.szu.cs.entity.KstcQuery;
import cn.edu.szu.cs.kstc.Context;
import cn.edu.szu.cs.kstc.TopKSpatialTextualClustersRetrieval;
import cn.hutool.core.lang.Assert;
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
    protected abstract SortedSet<T> getSList(Context<T> context);

    /**
     * 获取根据关键词查询出所有的对象并按照权重降序排序所得相关对象集合
     * <p> get the relevant object set obtained by querying all objects according to the keywords and sorting in descending order of weight
     *
     * @param context@return
     */
    protected abstract SortedSet<T> getTList(Context<T> context);

    /**
     * 从SList和TList中获取下一个对象
     * <p> get the next object from SList and TList
     *
     * @param sList
     * @param tList
     * @param context
     */
    protected abstract T getNextObject(SortedSet<T> sList, SortedSet<T> tList, Context<T> context);

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
     * 计算聚类的分数
     * <p> compute the cluster's score
     *
     * @param cluster
     * @param context
     * @return
     */
    protected abstract double getScore(Set<T> cluster, Context<T> context);

    /**
     * 计算当前对象的分数边界
     * <p> compute the score boundary of the current object
     *
     * @param obj
     * @param context
     * @return
     */
    protected abstract double getBound(T obj, Context<T> context);

    /**
     * 从SList和TList中移除对象
     * <p> remove object from SList and TList
     *
     * @param obj
     * @param sList
     * @param tList
     * @param context
     */
    protected abstract void removeObject(T obj, SortedSet<T> sList, SortedSet<T> tList, Context<T> context);


    /**
     * 执行基于DBSCAN的检索方法后置操作
     * <p> after execute the retrieval method based on DBSCAN
     *
     * @param context
     */
    protected abstract void afterDoDbScanBasedApproach(Context<T> context);

    private void checkoutQuery(KstcQuery query) {
        Assert.isTrue(query.getCoordinate() != null && query.getCoordinate().length == 2, "Please enter coordinate correctly.");
        Assert.checkBetween(query.getCoordinate()[0], -180.0, 180.0, "wrong longitude.");
        Assert.checkBetween(query.getCoordinate()[1], -90.0, 90.0, "wrong latitude.");
        Assert.checkBetween(query.getK(), 1, 20, "wrong k.");
        Assert.checkBetween(query.getEpsilon(), 0.0, Double.MAX_VALUE, "wrong epsilon.");
        Assert.checkBetween(query.getMinPts(), 2, Integer.MAX_VALUE, "wrong minPts.");
        Assert.checkBetween(query.getMaxDistance(), 0.0, Double.MAX_VALUE, "wrong maxDistance.");
        Assert.notNull(query.getKeywords(), "keywords is null.");
        Assert.isFalse(query.getKeywords().isEmpty(), "keywords is empty.");
    }

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
        SortedSet<T> sList = getSList(context);
        // sort objs descent by weight
        SortedSet<T> tList = getTList(context);

        List<Set<T>> rList = new ArrayList<>(query.getK());
        double bound = 0.0;
        double t = Double.MAX_VALUE;
        Set<T> noises = new HashSet<>();

        do {
            T obj = getNextObject(sList, tList, context);

            Set<T> cluster = getCluster(obj, sList, tList, noises, context);
            if (!cluster.isEmpty()) {
                rList.add(cluster);
                t = getScore(cluster, context);
                bound = getBound(obj, context);
            }

        } while (!sList.isEmpty() && rList.size() < query.getK() && bound < t);

        afterDoDbScanBasedApproach(context);

        return rList;
    }


    /**
     * 获取当前对象的聚类
     * <p> get the cluster of the current object
     *
     * @param p
     * @param sList
     * @param tList
     * @param noises
     * @param context
     * @return
     */
    private Set<T> getCluster(T p, SortedSet<T> sList, SortedSet<T> tList, Set<T> noises, Context<T> context) {

        KstcQuery q = context.getQuery();
        Queue<T> neighbors = rangeQuery(p, context);

        if (neighbors.size() < q.getMinPts()) {
            removeObject(p, sList, tList, context);
            // mark p as noise
            noises.add(p);
            return Collections.emptySet();
        }

        Set<T> result = new HashSet<>(neighbors);
        neighbors.forEach(nei -> removeObject(nei, sList, tList, context));
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
                        removeObject(obj, sList, tList, context);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public List<Set<T>> kstcSearch(KstcQuery query) {
        return doDbScanBasedApproach(query);
    }
}
