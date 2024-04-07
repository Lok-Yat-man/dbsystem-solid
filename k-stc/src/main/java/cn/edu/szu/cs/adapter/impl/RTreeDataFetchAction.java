package cn.edu.szu.cs.adapter.impl;

import cn.edu.szu.cs.adapter.DataFetchAction;
import cn.edu.szu.cs.adapter.KstcDataFetchManager;
import cn.edu.szu.cs.common.DataFetchCommandConstant;
import cn.edu.szu.cs.entity.DataFetchTask;
import cn.edu.szu.cs.entity.DbScanRelevantObject;
import cn.edu.szu.cs.entity.GeoPointDouble;
import cn.edu.szu.cs.entity.KstcQuery;
import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.LRUCache;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson.JSON;
import com.github.davidmoten.rtree.Entries;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("all")
public class RTreeDataFetchAction implements DataFetchAction<KstcQuery, RTree> {

    private Log log = LogFactory.get();

    private static final String CACHE_KEY_PREFIX = "RTREE_DATA_BY_KEYWORDS:{0}";

    private LRUCache<String,RTree> rTreeLRUCache = CacheUtil.newLRUCache(64, 60 * 60 * 1000L);

    @Override
    public String getCommand() {
        return DataFetchCommandConstant.LOAD_RTREE_DATA_BY_KEYWORDS;
    }

    @Override
    public KstcQuery parseParams(String paramsStr) {
        log.info("RTreeDataFetchAction.parseParams: paramsStr = {}", paramsStr);
        return JSON.parseObject(paramsStr, KstcQuery.class);
    }

    @Override
    public boolean checkParams(KstcQuery params) {

        if(params == null) {
            return false;
        }

        if(StrUtil.isBlank(params.getCommand())){
            return false;
        }

        if(CollUtil.isEmpty(params.getKeywords())){
            return false;
        }

        return true;
    }

    @Override
    public RTree fetchData(KstcQuery params) {

        String cacheKey = MessageFormat.format(CACHE_KEY_PREFIX, params.getKeywords().toString());
        if(rTreeLRUCache.containsKey(cacheKey)){
            log.info("RTreeDataFetchAction.fetchData: hit cache. cacheKey = {}", cacheKey);
            return rTreeLRUCache.get(cacheKey);
        }

        String actionId = KstcDataFetchManager.generateTask(
                DataFetchCommandConstant.LOAD_DBSCAN_DATA_BY_KEYWORDS,
                JSON.toJSONString(params)
        );

        log.info("RTreeDataFetchAction.fetchData: actionId = {}", actionId);

        DataFetchTask task = KstcDataFetchManager.getTask(actionId);

        if(!task.isSuccess()){
            throw new RuntimeException("RTreeDataFetchAction.fetchData: task failed. actionId = " + actionId + ", task = " + task);
        }

        List<DbScanRelevantObject> dbScanRelevantObjects = Convert.toList(DbScanRelevantObject.class, task.getData());

        if(CollUtil.isEmpty(dbScanRelevantObjects)){
            return RTree.create();
        }

        List<Entry<DbScanRelevantObject, GeoPointDouble>> entries = dbScanRelevantObjects
                .stream()
                .map(obj -> Entries.entry(obj, GeoPointDouble.create(obj.getCoordinate()[0], obj.getCoordinate()[1])))
                .collect(Collectors.toList());

        log.info("RTreeDataFetchAction.fetchData: entries.size = {}", entries.size());

        RTree<DbScanRelevantObject, GeoPointDouble> rTree = RTree.create(entries);
        rTreeLRUCache.put(cacheKey, rTree);
        return rTree;
    }
}
