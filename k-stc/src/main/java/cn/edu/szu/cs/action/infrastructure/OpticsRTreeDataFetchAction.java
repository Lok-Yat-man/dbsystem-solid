package cn.edu.szu.cs.action.infrastructure;

import cn.edu.szu.cs.action.DataFetchAction;
import cn.edu.szu.cs.adapter.KstcDataFetchManager;
import cn.edu.szu.cs.constant.DataFetchConstant;
import cn.edu.szu.cs.entity.*;
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
public class OpticsRTreeDataFetchAction implements DataFetchAction<KstcQuery, RTree> {

    private Log log = LogFactory.get();

    private static final String CACHE_KEY_PREFIX = "OPTICS_RTREE_DATA_BY_KEYWORDS:{0}";

    private LRUCache<String,RTree> rTreeLRUCache = CacheUtil.newLRUCache(64, 60 * 60 * 1000L);

    @Override
    public String getCommand() {
        return DataFetchConstant.LOAD_OPTICS_RTREE_DATA_BY_KEYWORDS;
    }

    @Override
    public String getCommandType() {
        return DataFetchConstant.INFRASTRUCTURE_LAYER;
    }

    @Override
    public KstcQuery parseParams(String paramsStr) {
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
            return rTreeLRUCache.get(cacheKey);
        }

        DataFetchResult task = KstcDataFetchManager.generateTaskAndGet(DataFetchConstant.INFRASTRUCTURE_LAYER,
                DataFetchConstant.LOAD_OPTICS_DATA_BY_KEYWORDS,
                JSON.toJSONString(params));

        if(!task.isSuccess()){
            throw new RuntimeException("RTreeDataFetchAction.fetchData: task failed. task = " + JSON.toJSONString(task));
        }

        List<OpticsRelevantObject> dbScanRelevantObjects = Convert.toList(OpticsRelevantObject.class, task.getData());

        if(CollUtil.isEmpty(dbScanRelevantObjects)){
            return RTree.create();
        }

        List<Entry<OpticsRelevantObject, GeoPointDouble>> entries = dbScanRelevantObjects
                .stream()
                .map(obj -> Entries.entry(obj, GeoPointDouble.create(obj.getCoordinate()[0], obj.getCoordinate()[1])))
                .collect(Collectors.toList());

        log.info("RTreeDataFetchAction.fetchData: entries.size = {}", entries.size());

        RTree<OpticsRelevantObject, GeoPointDouble> rTree = RTree.create(entries);
        rTreeLRUCache.put(cacheKey, rTree);
        return rTree;
    }
}
