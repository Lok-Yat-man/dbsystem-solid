package cn.edu.szu.cs.kstc.dbscan;

import cn.edu.szu.cs.entity.DbScanRelevantObject;
import cn.edu.szu.cs.entity.KstcQuery;
import cn.edu.szu.cs.kstc.TopKSpatialTextualClustersRetrieval;
import cn.edu.szu.cs.util.CommonUtil;
import cn.edu.szu.cs.util.TimerHolder;
import cn.hutool.core.lang.Assert;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.Data;
import lombok.NonNull;

import java.util.*;

/**
 * DBScanBasedApproach
 *
 * @author Whitence
 * @version 1.0
 * @date 2024/3/16 15:47
 */
@Data
public class DbScanBasedApproach<T extends DbScanRelevantObject> implements TopKSpatialTextualClustersRetrieval<T> {

    /**
     * logger
     */
    private static final Log logger = LogFactory.get();


    /**
     * InvertedIndex
     *
     * @author Whitence
     * @version 1.0
     * @date 2024/4/3 23:58
     */
    public interface InvertedIndex<U> {
        /**
         * load data by keywords and coordinate and maxDistance and sort by distance ASC
         *
         * @param keywords
         * @param coordinate
         * @param maxDistance
         * @return
         */
        SortedSet<U> loadDataSortByDistanceAsc(List<String> keywords, double[] coordinate, double maxDistance);

        /**
         * load data by keywords and sort by weight DESC
         *
         * @param keywords
         * @return
         */
        SortedSet<U> loadDataSortByWeightDesc(List<String> keywords);

    }

    /**
     * IRTree
     *
     * @param <U>
     */
    public interface IRTree<U> {

        /**
         * range query
         *
         * @param keywords
         * @param coordinate
         * @param epsilon
         * @return
         */
        Queue<U> rangeQuery(List<String> keywords, double[] coordinate, double epsilon);


        Double getMaxDistance();
    }

    /**
     * invertedIndex
     */
    private InvertedIndex<T> invertedIndex;
    /**
     * irTree
     */
    private IRTree<T> irTree;

    private double alpha = 0.5;
    private double maxDistance = 0.0;

    public DbScanBasedApproach(@NonNull InvertedIndex<T> invertedIndex, @NonNull IRTree<T> irTree, double alpha) {
        this.invertedIndex = invertedIndex;
        this.irTree = irTree;
        this.alpha = alpha;
        this.maxDistance = irTree.getMaxDistance();
    }

    /**
     * compute the cluster's score
     *
     * @param cluster
     * @param query
     * @return
     */
    protected double getScore(Set<T> cluster, KstcQuery query) {

        double minDistance = 0.0;
        double maxWeight = 0.0;

        for (T t : cluster) {

            double distance = CommonUtil.calculateDistance(t.getCoordinate(), t.getCoordinate());
            if (distance < minDistance) {
                minDistance = distance;
            }
            double weight = t.getWeight(query.getKeywords());
            if (weight > maxWeight) {
                maxWeight = weight;
            }

        }

        return alpha * (1 - minDistance / maxDistance) + (1 - maxWeight) * (1 - alpha);
    }

    protected void removeObject(T obj, SortedSet<T> sList, SortedSet<T> tList) {
        sList.remove(obj);
        tList.remove(obj);
    }

    protected double getBound(T obj, KstcQuery query) {

        double distance = CommonUtil.calculateDistance(obj.getCoordinate(), query.getCoordinate());
        double distanceWeight = 1 - distance / maxDistance;

        double weight = obj.getWeight(query.getKeywords());

        return alpha * distanceWeight + (1 - weight) * alpha;
    }


    protected T getNextObject(int times, SortedSet<T> sList, SortedSet<T> tList) {
        T obj = null;
        if (times % 2 == 0) {
            obj = sList.first();
        } else {
            obj = tList.first();
        }
        ++times;
        return obj;
    }

    /**
     * get SList
     * @param keywords
     * @param coordinate
     * @param maxDistance
     * @return
     */
    protected SortedSet<T> getSList(List<String> keywords, double[] coordinate, double maxDistance) {
        return invertedIndex.loadDataSortByDistanceAsc(keywords, coordinate, maxDistance);
    }

    protected SortedSet<T> getTList(List<String> keywords) {
        return invertedIndex.loadDataSortByWeightDesc(keywords);
    }

    protected Queue<T> rangeQuery(List<String> keywords, double[] coordinate, double epsilon) {
        return irTree.rangeQuery(keywords, coordinate, epsilon);
    }

    protected void beforeDoDbScanBasedApproach(KstcQuery query) {
        Assert.isTrue(query.getCoordinate() != null && query.getCoordinate().length == 2, "Please enter coordinate correctly.");
        Assert.checkBetween(query.getCoordinate()[0], -180.0, 180.0, "wrong longitude.");
        Assert.checkBetween(query.getCoordinate()[1], -90.0, 90.0, "wrong latitude.");
        Assert.checkBetween(query.getK(), 1, 20, "wrong k.");
        Assert.checkBetween(query.getEpsilon(), 1.0, Double.MAX_VALUE, "wrong epsilon.");
        Assert.checkBetween(query.getMinPts(), 2, Integer.MAX_VALUE, "wrong minPts.");
        Assert.checkBetween(query.getMaxDistance(), -1, Double.MAX_VALUE, "wrong maxDistance.");
        Assert.notNull(query.getKeywords(), "keywords is null.");
        Assert.isFalse(query.getKeywords().isEmpty(), "keywords is empty.");

        TimerHolder.start("DBScanBasedApproach");
    }

    protected void afterDoDbScanBasedApproach(KstcQuery query,List<Set<T>> rList) {
        long stop = TimerHolder.stop("DBScanBasedApproach");
        logger.info("DBScanBasedApproach cost time: " + stop + " ms.");
    }

    private List<Set<T>> doDbScanBasedApproach(KstcQuery query){
        // before do dbScanBasedApproach
        beforeDoDbScanBasedApproach(query);

        // sort objs ascent by distance
        SortedSet<T> sList = getSList(query.getKeywords(), query.getCoordinate(), maxDistance);
        // sort objs descent by weight
        SortedSet<T> tList = getTList(query.getKeywords());

        List<Set<T>> rList = new ArrayList<>(query.getK());

        double bound = 0.0;
        double t = Double.MAX_VALUE;
        int times = 0;
        Set<T> noises = new HashSet<>();

        do {
            T obj = getNextObject(times, sList, tList);

            Set<T> cluster = getCluster(obj, query, sList, tList, noises);
            if (!cluster.isEmpty()) {
                rList.add(cluster);
                t = getScore(cluster, query);
                bound = getBound(obj, query);
            }

        } while (!sList.isEmpty() && rList.size() < query.getK() && bound < t);

        afterDoDbScanBasedApproach(query,rList);

        return rList;
    }


    @Override
    public List<Set<T>> kstcSearch(KstcQuery query) {
        return doDbScanBasedApproach(query);
    }

    /**
     * get cluster
     *
     * @param p
     * @param q
     * @param sList
     * @param tList
     * @param noises
     * @return
     */
    private Set<T> getCluster(T p,
                              KstcQuery q,
                              SortedSet<T> sList,
                              SortedSet<T> tList,
                              Set<T> noises) {

        Queue<T> neighbors = rangeQuery(q.getKeywords(), p.getCoordinate(), q.getEpsilon());

        if (neighbors.size() < q.getMinPts()) {
            removeObject(p, sList, tList);
            // mark p as noise
            noises.add(p);
            return Collections.emptySet();
        }

        Set<T> result = new HashSet<>(neighbors);
        removeObject(p, sList, tList);
        neighbors.remove(p);

        while (!neighbors.isEmpty()) {
            T neighbor = neighbors.poll();

            Queue<T> neighborsTmp = rangeQuery(q.getKeywords(), neighbor.getCoordinate(), q.getEpsilon());

            if (neighborsTmp.size() >= q.getMinPts()) {

                for (T obj : neighborsTmp) {
                    if (noises.contains(obj)) {
                        result.add(obj);
                    } else if (!result.contains(obj)) {
                        result.add(obj);
                        neighbors.add(obj);
                        removeObject(p, sList, tList);
                    }
                }
            }
        }
        return result;
    }

}
