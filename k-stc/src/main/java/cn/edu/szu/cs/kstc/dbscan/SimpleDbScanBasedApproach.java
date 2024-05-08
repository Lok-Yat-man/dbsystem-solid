package cn.edu.szu.cs.kstc.dbscan;

import cn.edu.szu.cs.adapter.KstcDataFetchManager;
import cn.edu.szu.cs.constant.DataFetchConstant;
import cn.edu.szu.cs.entity.DataFetchResult;
import cn.edu.szu.cs.entity.DbScanRelevantObject;
import cn.edu.szu.cs.entity.KstcQuery;
import cn.edu.szu.cs.kstc.Context;
import cn.edu.szu.cs.util.CommonUtil;
import cn.edu.szu.cs.util.TimerHolder;
import cn.hutool.core.convert.Convert;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

/**
 *  基于DBSCAN的空间文本聚类检索方法-简约版。这一版的实现没有使用IR-Tree。
 *  <p> A simplified version of the spatial text clustering retrieval method based on DBSCAN. This implementation does not use IR-Tree.
 * @author Whitence
 * @date 2024/4/6 12:40
 * @version 1.0
 */
@SuppressWarnings("all")
public class SimpleDbScanBasedApproach extends AbstractDbScanBasedApproach<DbScanRelevantObject>{

    /**
     * logger
     */
    private static final Log logger = LogFactory.get();

    /**
     * 是否提前准备数据
     * <p> Whether to prepare data in advance
     */
    public static boolean prepareAdvance = true;

    /**
     *  基于DBSCAN的简约版上下文实现类
     *  <p> Simple version of the context implementation based on DBSCAN
     * @author Whitence
     * @date 2024/4/9 19:56
     * @version 1.0
     */
    @NoArgsConstructor
    private static class SimpleDbScanBasedContext extends Context<DbScanRelevantObject> {

        /**
         * 是否是第一次
         * <p> Whether it is the first time
         */
        @Getter
        @Setter
        private boolean isFirst = true;

    }

    /**
     * 在执行基于DBSCAN的扫描方法之前
     * <p> Before executing the DBSCAN-based scanning method
     * @param query
     */
    @Override
    protected void beforeDoDbScanBasedApproach(KstcQuery query) {
        TimerHolder.start("SimpleDbScanBasedApproach");
    }

    /**
     * 初始化上下文
     * <p> Initialize the context
     * @param query
     * @return
     */
    @Override
    protected Context<DbScanRelevantObject> initContext(KstcQuery query) {
        SimpleDbScanBasedContext simpleDbScanBasedContext = new SimpleDbScanBasedContext();
        simpleDbScanBasedContext.setQuery(query);

        // 设置查询命令
        // set the query command
        query.setCommand(DataFetchConstant.LOAD_OPTICS_RTREE_DATA_BY_KEYWORDS);

        // 根据关键字异步加载数据，生成RTree
        // Load data by keyword and generate RTree
        if(prepareAdvance){
            KstcDataFetchManager.generateTask(DataFetchConstant.INFRASTRUCTURE_LAYER,
                    DataFetchConstant.LOAD_OPTICS_RTREE_DATA_BY_KEYWORDS,
                    JSON.toJSONString(query));
        }
        return simpleDbScanBasedContext;
    }

    /**
     * 加载相关对象数据
     * <p> Load related object data
     * @param context@return
     * @return
     */
    @Override
    protected SortedSet<DbScanRelevantObject> getRelevantObjects(Context<DbScanRelevantObject> context) {

        KstcQuery query = context.getQuery();
        // 从数据集中加载数据
        // Load data from the dataset
        DataFetchResult task = KstcDataFetchManager.generateTaskAndGet(DataFetchConstant.INFRASTRUCTURE_LAYER,
                DataFetchConstant.LOAD_DBSCAN_DATA_BY_KEYWORDS,
                JSON.toJSONString(query));

        if(!task.isSuccess()){
            throw new RuntimeException("Failed to load data from the database. task: " + JSON.toJSONString(task));
        }

        List<DbScanRelevantObject> dbScanRelevantObjects = Convert.toList(DbScanRelevantObject.class, task.getData());

        // 将数据集中的数据按照距离排序
        // Sort the data in the dataset by distance
        return dbScanRelevantObjects.stream()
                .filter(obj-> CommonUtil.calculateDistance(obj.getCoordinate(), query.getCoordinate()) <= query.getMaxDistance())
                .collect(Collectors.toCollection(()->new TreeSet<>(
                        new Comparator<DbScanRelevantObject>() {
                            @Override
                            public int compare(DbScanRelevantObject o1, DbScanRelevantObject o2) {
                                int compare = Double.compare(CommonUtil.calculateDistance(o1.getCoordinate(), query.getCoordinate()),
                                        CommonUtil.calculateDistance(o2.getCoordinate(), query.getCoordinate()));
                                if(compare == 0){
                                    return o1.compareTo(o2);
                                }
                                return compare;
                            }
                        }
                )));
    }

    /**
     * 初始化聚类集合
     * <p> Initialize the cluster set
     * @param context
     * @return
     */
    @Override
    protected List<Set<DbScanRelevantObject>> initClusters(Context<DbScanRelevantObject> context) {
        return new ArrayList<>();
    }


    /**
     * 获取下一个对象
     * <p> Get the next object
     * @param sList
     * @param context
     * @return
     */
    @Override
    protected DbScanRelevantObject getNextObject(SortedSet<DbScanRelevantObject> sList, Context<DbScanRelevantObject> context) {
        return sList.first();
    }

    /**
     * 根据当前对象的坐标进行范围查询
     * @param p
     * @param context
     * @return
     */
    @Override
    protected Queue<DbScanRelevantObject> rangeQuery(DbScanRelevantObject p, Context<DbScanRelevantObject> context) {
        SimpleDbScanBasedContext simpleDbScanBasedContext = (SimpleDbScanBasedContext) context;

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
        if(queue.size() >= query.getMinPts() && simpleDbScanBasedContext.isFirst() && prepareAdvance){
            for (DbScanRelevantObject dbScanRelevantObject : queue) {
                kstcQuery.setCoordinate(dbScanRelevantObject.getCoordinate());
                KstcDataFetchManager.generateTask(DataFetchConstant.INFRASTRUCTURE_LAYER,
                        DataFetchConstant.DBSCAN_RTREE_RANGE_QUERY,
                        JSON.toJSONString(kstcQuery));
            }
            simpleDbScanBasedContext.setFirst(false);
        }

        return queue;
    }

    /**
     * 判断是否可以跳过
     * <p> Determine if it can be skipped
     * @param obj
     * @param context
     * @return
     */
    @Override
    protected boolean canSkip(DbScanRelevantObject obj, Context<DbScanRelevantObject> context) {
        return false;
    }

    /**
     * 从集合中移除对象
     * <p> Remove objects from the collection
     * @param obj
     * @param sList
     * @param context
     */
    @Override
    protected void removeObject(DbScanRelevantObject obj, SortedSet<DbScanRelevantObject> sList, Context<DbScanRelevantObject> context) {
        sList.remove(obj);
    }

    /**
     * 判断是否可以提前结束
     * <p> Determine if advanced clustering can be completed
     * @param obj
     * @param cluster
     * @param context
     * @return
     */
    @Override
    protected boolean canFinishAdvanced(DbScanRelevantObject obj, Set<DbScanRelevantObject> cluster, Context<DbScanRelevantObject> context) {
        return false;
    }

    /**
     * 找到一个点之后的操作
     * <p> Operation after finding a point
     * @param obj
     * @param cluster
     * @param context
     */
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
        long timestamp = TimerHolder.stop("SimpleDbScanBasedApproach");
        logger.info("SimpleDbScanBasedApproach finished in {} ms", timestamp);
    }

    @Override
    protected void afterGetCluster(Context<DbScanRelevantObject> context) {
        SimpleDbScanBasedContext simpleDbScanBasedContext = (SimpleDbScanBasedContext) context;
        simpleDbScanBasedContext.setFirst(true);
    }
}
