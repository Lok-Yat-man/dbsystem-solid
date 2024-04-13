package cn.edu.szu.cs.kstc.dbscan;

import cn.edu.szu.cs.adapter.KstcDataFetchManager;
import cn.edu.szu.cs.constant.DataFetchConstant;
import cn.edu.szu.cs.entity.DataFetchResult;
import cn.edu.szu.cs.entity.DbScanRelevantObject;
import cn.edu.szu.cs.entity.GeoPointDouble;
import cn.edu.szu.cs.entity.KstcQuery;
import cn.edu.szu.cs.kstc.Context;
import cn.edu.szu.cs.util.CommonUtil;
import cn.edu.szu.cs.util.TimerHolder;
import cn.hutool.core.convert.Convert;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson.JSON;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.internal.NonLeafDefault;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * DBScanBasedApproach
 *
 * @author Whitence
 * @version 1.0
 * @date 2024/3/16 15:47
 */
@SuppressWarnings("all")
public class DbScanBasedApproach extends AbstractDbScanBasedApproach<DbScanRelevantObject> {

    /**
     * logger
     */
    private static final Log logger = LogFactory.get();


    private double alpha = 0.5;

    protected void removeObject(DbScanRelevantObject obj, SortedSet<DbScanRelevantObject> sList, SortedSet<DbScanRelevantObject> tList) {
        sList.remove(obj);
        tList.remove(obj);
    }

    protected DbScanRelevantObject getNextObject(int times, SortedSet<DbScanRelevantObject> sList, SortedSet<DbScanRelevantObject> tList) {
        DbScanRelevantObject obj = null;
        if (times % 2 == 0) {
            obj = sList.first();
        } else {
            obj = tList.first();
        }
        ++times;
        return obj;
    }

    private static class DbScanBasedApproachContext extends Context<DbScanRelevantObject>{

        @Getter
        @Setter
        private double maxDistance = Double.MAX_VALUE;

        @Getter
        @Setter
        private SortedSet<DbScanRelevantObject> tList;

        @Getter
        @Setter
        private long times = 0;

        @Getter
        @Setter
        private boolean isFirst;

    }

    @Override
    protected void beforeDoDbScanBasedApproach(KstcQuery query) {
        TimerHolder.start("DBScanBasedApproach");
    }

    @Override
    protected Context<DbScanRelevantObject> initContext(KstcQuery query) {

        DbScanBasedApproachContext context = new DbScanBasedApproachContext();
        context.setQuery(query);

        DataFetchResult dataFetchResult = KstcDataFetchManager.generateTaskAndGet(
                DataFetchConstant.INFRASTRUCTURE_LAYER,
                DataFetchConstant.LOAD_OPTICS_RTREE_DATA_BY_KEYWORDS,
                JSON.toJSONString(query)
        );

        if(!dataFetchResult.isSuccess()){
            throw new RuntimeException("Failed to load data from the database. task: " + JSON.toJSONString(dataFetchResult));
        }

        RTree<DbScanRelevantObject, GeoPointDouble> rTree = (RTree<DbScanRelevantObject, GeoPointDouble>) dataFetchResult.getData();

        if(rTree.root().isPresent()){
            NonLeafDefault<DbScanRelevantObject, GeoPointDouble> rootNode = (NonLeafDefault<DbScanRelevantObject, GeoPointDouble>) rTree.root().get();
            context.setMaxDistance(
                    CommonUtil.getDistance(rootNode.geometry().mbr().x1(), rootNode.geometry().mbr().y1(),
                            rootNode.geometry().mbr().x2(), rootNode.geometry().mbr().y2())
            );
        }

        return context;
    }


    @Override
    protected SortedSet<DbScanRelevantObject> getRelevantObjects(Context<DbScanRelevantObject> context) {
        DbScanBasedApproachContext dbScanBasedApproachContext = (DbScanBasedApproachContext) context;
        KstcQuery query = dbScanBasedApproachContext.getQuery();
        // 从数据集中加载数据
        // Load data from the dataset
        DataFetchResult task = KstcDataFetchManager.generateTaskAndGet(DataFetchConstant.INFRASTRUCTURE_LAYER,
                DataFetchConstant.LOAD_DBSCAN_DATA_BY_KEYWORDS,
                JSON.toJSONString(query));

        if(!task.isSuccess()){
            throw new RuntimeException("Failed to load data from the database. task: " + JSON.toJSONString(task));
        }

        // 过滤掉距离大于最大距离的数据
        // Filter out data with distances greater than the maximum distance
        List<DbScanRelevantObject> dbScanRelevantObjects = Convert.toList(DbScanRelevantObject.class, task.getData()).stream()
                .filter(obj-> CommonUtil.calculateDistance(obj.getCoordinate(), query.getCoordinate()) <= query.getMaxDistance())
                .collect(Collectors.toList());


        // 将数据集中的数据按照权重排序
        // Sort the data in the dataset by weight
        SortedSet<DbScanRelevantObject> tList = new TreeSet<>(
                Comparator.comparingDouble(obj->obj.getWeight(query.getKeywords()))
        );
        tList.addAll(dbScanRelevantObjects);
        dbScanBasedApproachContext.setTList(tList);

        // 将数据集中的数据按照距离排序
        // Sort the data in the dataset by distance
        SortedSet<DbScanRelevantObject> sList = new TreeSet<>(
                Comparator.comparingDouble(obj -> CommonUtil.calculateDistance(obj.getCoordinate(), query.getCoordinate())
        ));
        sList.addAll(dbScanRelevantObjects);
        return sList;
    }

    @Override
    protected List<Set<DbScanRelevantObject>> initClusters(Context<DbScanRelevantObject> context) {
        return new ArrayList<>();
    }

    @Override
    protected DbScanRelevantObject getNextObject(SortedSet<DbScanRelevantObject> sList, Context<DbScanRelevantObject> context) {
        DbScanBasedApproachContext dbScanBasedApproachContext = (DbScanBasedApproachContext) context;
        long times = dbScanBasedApproachContext.getTimes();
        SortedSet<DbScanRelevantObject> tList = dbScanBasedApproachContext.getTList();
        DbScanRelevantObject obj = null;
        if (times % 2 == 0) {
            obj = sList.first();
        } else {
            obj = tList.first();
        }
        dbScanBasedApproachContext.setTimes(times + 1);
        return obj;
    }

    @Override
    protected Queue<DbScanRelevantObject> rangeQuery(DbScanRelevantObject p, Context<DbScanRelevantObject> context) {
        DbScanBasedApproachContext dbScanBasedApproachContext = (DbScanBasedApproachContext) context;

        KstcQuery query = context.getQuery();
        // 准备查询参数
        // Prepare query parameters
        KstcQuery kstcQuery = new KstcQuery();
        kstcQuery.setCoordinate(p.getCoordinate());
        kstcQuery.setEpsilon(query.getEpsilon());
        kstcQuery.setCommand(DataFetchConstant.DBSCAN_RTREE_RANGE_QUERY);
        kstcQuery.setKeywords(query.getKeywords());

        // 进行范围查询
        // Perform range query
        DataFetchResult task = KstcDataFetchManager.generateTaskAndGet(DataFetchConstant.INFRASTRUCTURE_LAYER,
                DataFetchConstant.DBSCAN_RTREE_RANGE_QUERY,
                JSON.toJSONString(kstcQuery));

        if(!task.isSuccess()){
            throw new RuntimeException("Failed to load data from the database.task: " + JSON.toJSONString(task));
        }
        Queue<DbScanRelevantObject> queue = (Queue<DbScanRelevantObject>) task.getData();

        // 提前为下一次查询准备数据
        // Prepare data for the next query in advance
        if(queue.size() >= query.getMinPts() && dbScanBasedApproachContext.isFirst()){
            for (DbScanRelevantObject dbScanRelevantObject : queue) {
                kstcQuery.setCoordinate(dbScanRelevantObject.getCoordinate());
                KstcDataFetchManager.generateTask(DataFetchConstant.INFRASTRUCTURE_LAYER,
                        DataFetchConstant.DBSCAN_RTREE_RANGE_QUERY,
                        JSON.toJSONString(kstcQuery));
            }
            dbScanBasedApproachContext.setFirst(false);
        }

        return queue;
    }

    @Override
    protected boolean canSkip(DbScanRelevantObject obj, Context<DbScanRelevantObject> context) {
        return false;
    }

    @Override
    protected void removeObject(DbScanRelevantObject obj, SortedSet<DbScanRelevantObject> sList, Context<DbScanRelevantObject> context) {
        DbScanBasedApproachContext dbScanBasedApproachContext = (DbScanBasedApproachContext) context;
        SortedSet<DbScanRelevantObject> tList = dbScanBasedApproachContext.getTList();
        sList.remove(obj);
        tList.remove(obj);
    }

    @Override
    protected boolean canFinishAdvanced(DbScanRelevantObject obj, Set<DbScanRelevantObject> cluster, Context<DbScanRelevantObject> context) {

        DbScanBasedApproachContext dbScanBasedApproachContext = (DbScanBasedApproachContext) context;
        // 数据集中最大距离
        // Maximum distance in the dataset
        double maxDistance = dbScanBasedApproachContext.getMaxDistance();
        KstcQuery query = context.getQuery();

        double minDistance = 0.0;
        double maxWeight = 0.0;
        for (DbScanRelevantObject t : cluster) {

            double distance = CommonUtil.calculateDistance(t.getCoordinate(), t.getCoordinate());
            if (distance < minDistance) {
                minDistance = distance;
            }
            double weight = t.getWeight(query.getKeywords());
            if (weight > maxWeight) {
                maxWeight = weight;
            }

        }
        double tau = alpha * (1 - minDistance / maxDistance) + (1 - maxWeight) * (1 - alpha);


        double distance = CommonUtil.calculateDistance(obj.getCoordinate(), query.getCoordinate());
        double distanceWeight = 1 - distance / maxDistance;

        double weight = obj.getWeight(query.getKeywords());

        double bound = alpha * distanceWeight + (1 - weight) * alpha;

        return bound >= tau;
    }

    @Override
    protected void afterAddToCluster(DbScanRelevantObject obj, Set<DbScanRelevantObject> cluster, Context<DbScanRelevantObject> context) {
        KstcQuery query = context.getQuery();
        // 准备查询参数
        // Prepare query parameters
        KstcQuery kstcQuery = new KstcQuery();
        kstcQuery.setCoordinate(obj.getCoordinate());
        kstcQuery.setEpsilon(query.getEpsilon());
        kstcQuery.setCommand(DataFetchConstant.DBSCAN_RTREE_RANGE_QUERY);
        kstcQuery.setKeywords(query.getKeywords());
        // 为下一次查询准备数据
        // Prepare data for the next query
        KstcDataFetchManager.generateTask(DataFetchConstant.INFRASTRUCTURE_LAYER,
                DataFetchConstant.DBSCAN_RTREE_RANGE_QUERY,
                JSON.toJSONString(kstcQuery));
    }

    @Override
    protected void afterDoDbScanBasedApproach(Context<DbScanRelevantObject> context) {
        long dbScanBasedApproach = TimerHolder.stop("DBScanBasedApproach");
        System.out.println("DBScanBasedApproach: " + dbScanBasedApproach);
    }


}
