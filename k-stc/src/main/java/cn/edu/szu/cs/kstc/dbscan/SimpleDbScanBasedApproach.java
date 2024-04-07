package cn.edu.szu.cs.kstc.dbscan;

import cn.edu.szu.cs.adapter.KstcDataFetchManager;
import cn.edu.szu.cs.common.DataFetchCommandConstant;
import cn.edu.szu.cs.entity.DataFetchTask;
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


    @NoArgsConstructor
    private static class SimpleDbScanBasedContext implements Context<DbScanRelevantObject> {

        private KstcQuery query;

        private List<Set<DbScanRelevantObject>> clusterSet;

        @Getter
        @Setter
        private String rTreeActionId;

        @Override
        public KstcQuery getQuery() {
            return query;
        }

        @Override
        public void setQuery(KstcQuery query) {
            this.query = query;
        }

        @Override
        public List<Set<DbScanRelevantObject>> getClusters() {
            return clusterSet;
        }

        @Override
        public void setClusters(List<Set<DbScanRelevantObject>> clusters) {
            this.clusterSet = clusters;
        }
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
        query.setCommand(DataFetchCommandConstant.LOAD_RTREE_DATA_BY_KEYWORDS);

        String actionId = KstcDataFetchManager.generateTask(
                DataFetchCommandConstant.LOAD_RTREE_DATA_BY_KEYWORDS,
                JSON.toJSONString(query)
        );
        simpleDbScanBasedContext.setRTreeActionId(actionId);

        return simpleDbScanBasedContext;
    }

    @Override
    protected SortedSet<DbScanRelevantObject> getSList(Context<DbScanRelevantObject> context) {

        KstcQuery query = context.getQuery();
        String actionId = KstcDataFetchManager.generateTask(
                DataFetchCommandConstant.LOAD_DBSCAN_DATA_BY_KEYWORDS,
                JSON.toJSONString(query)
        );

        DataFetchTask task = KstcDataFetchManager.getTask(actionId);

        if(!task.isSuccess()){
            throw new RuntimeException("Failed to load data from the database. actionId: " + actionId + " task: " + task);
        }

        List<DbScanRelevantObject> dbScanRelevantObjects = Convert.toList(DbScanRelevantObject.class, task.getData());

        SortedSet<DbScanRelevantObject> sList = new TreeSet<>(Comparator.comparingDouble(obj-> CommonUtil.calculateDistance(obj.getCoordinate(), query.getCoordinate())));
        sList.addAll(dbScanRelevantObjects);
        return sList;
    }

    @Override
    protected SortedSet<DbScanRelevantObject> getTList(Context<DbScanRelevantObject> context) {
        return Collections.emptySortedSet();
    }

    @Override
    protected DbScanRelevantObject getNextObject(SortedSet<DbScanRelevantObject> sList, SortedSet<DbScanRelevantObject> tList, Context<DbScanRelevantObject> context) {
        return sList.first();
    }

    @Override
    protected Queue<DbScanRelevantObject> rangeQuery(DbScanRelevantObject p, Context<DbScanRelevantObject> context) {
        SimpleDbScanBasedContext simpleDbScanBasedContext = (SimpleDbScanBasedContext) context;

        KstcQuery query = context.getQuery();

        KstcQuery kstcQuery = new KstcQuery();
        kstcQuery.setCoordinate(p.getCoordinate());
        kstcQuery.setEpsilon(query.getEpsilon());
        kstcQuery.setCommand(DataFetchCommandConstant.RTREE_RANGE_QUERY);
        kstcQuery.setKeywords(query.getKeywords());

        String actionId = KstcDataFetchManager.generateTask(
                DataFetchCommandConstant.RTREE_RANGE_QUERY,
                JSON.toJSONString(kstcQuery)
        );

        DataFetchTask task = KstcDataFetchManager.getTask(actionId);

        if(!task.isSuccess()){
            throw new RuntimeException("Failed to load data from the database. actionId: " + actionId + " task: " + task);
        }
        Queue<DbScanRelevantObject> queue = (Queue<DbScanRelevantObject>) task.getData();

        // 生成RTree的范围查询任务 TODO--
        // Generate RTree range query tasks


        return queue;
    }

    @Override
    protected boolean canSkip(DbScanRelevantObject obj, Context<DbScanRelevantObject> context) {
        return false;
    }

    @Override
    protected double getScore(Set<DbScanRelevantObject> cluster, Context<DbScanRelevantObject> context) {
        return Double.MAX_VALUE;
    }

    @Override
    protected double getBound(DbScanRelevantObject obj, Context<DbScanRelevantObject> context) {
        return 0;
    }

    @Override
    protected void removeObject(DbScanRelevantObject obj, SortedSet<DbScanRelevantObject> sList, SortedSet<DbScanRelevantObject> tList, Context<DbScanRelevantObject> context) {
        sList.remove(obj);
    }

    @Override
    protected void afterDoDbScanBasedApproach(Context<DbScanRelevantObject> context) {
        long timestamp = TimerHolder.stop("SimpleDbScanBasedApproach");
        logger.info("SimpleDbScanBasedApproach finished in {} ms", timestamp);
    }
}
