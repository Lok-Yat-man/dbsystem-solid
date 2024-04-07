package cn.edu.szu.cs.adapter.impl;

import cn.edu.szu.cs.adapter.DataFetchAction;
import cn.edu.szu.cs.adapter.KstcDataFetchManager;
import cn.edu.szu.cs.common.DataFetchCommandConstant;
import cn.edu.szu.cs.entity.DataFetchTask;
import cn.edu.szu.cs.entity.DbScanRelevantObject;
import cn.edu.szu.cs.entity.GeoPointDouble;
import cn.edu.szu.cs.entity.KstcQuery;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson.JSON;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import rx.observables.BlockingObservable;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 *  RTreeRangeQueryDataFetchAction
 * @author Whitence
 * @date 2024/4/7 15:06
 * @version 1.0
 */
@SuppressWarnings("all")
public class RTreeRangeQueryDataFetchAction implements DataFetchAction<KstcQuery, Queue>{


    private Log log = LogFactory.get();

    private static final String CACHE_KEY_PREFIX = "RTREE_RANGE_QUERY:{0}:{1}:{2}";

    @Override
    public String getCommand() {
        return DataFetchCommandConstant.RTREE_RANGE_QUERY;
    }

    @Override
    public KstcQuery parseParams(String paramsStr) {
        log.info("RTreeRangeQueryDataFetchAction parseParams:{}",paramsStr);
        return JSON.parseObject(paramsStr, KstcQuery.class);
    }

    @Override
    public boolean checkParams(KstcQuery params) {
        if(params == null) {
            return false;
        }

        if(StrUtil.isBlank(params.getCommand())) {
            return false;
        }

        if(CollUtil.isEmpty(params.getKeywords()) || params.getKeywords().stream().anyMatch(StrUtil::isBlank)) {
            return false;
        }


        return true;
    }

    @Override
    public Queue fetchData(KstcQuery params) {

        String actionId = KstcDataFetchManager.generateTask(
                DataFetchCommandConstant.LOAD_RTREE_DATA_BY_KEYWORDS,
                JSON.toJSONString(params)
        );

        DataFetchTask task = KstcDataFetchManager.getTask(actionId);

        if(!task.isSuccess()) {
            throw new RuntimeException("RTreeRangeQueryDataFetchAction fetchData failed");
        }

        RTree<DbScanRelevantObject, GeoPointDouble> rTree = (RTree<DbScanRelevantObject, GeoPointDouble>) task.getData();

        BlockingObservable<Entry<DbScanRelevantObject, GeoPointDouble>> entryBlockingObservable = rTree
                .nearest(
                        GeoPointDouble.create(params.getCoordinate()[0], params.getCoordinate()[1]), params.getEpsilon(), Integer.MAX_VALUE)
                .toBlocking();

        Queue<DbScanRelevantObject> queue = new ArrayDeque<>();
        entryBlockingObservable.forEach(entry -> {
            queue.add(entry.value());
        });

        return queue;
    }

}
