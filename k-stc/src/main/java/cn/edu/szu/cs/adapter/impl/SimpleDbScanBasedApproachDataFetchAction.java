package cn.edu.szu.cs.adapter.impl;

import cn.edu.szu.cs.adapter.DataFetchAction;
import cn.edu.szu.cs.common.DataFetchCommandConstant;
import cn.edu.szu.cs.entity.DbScanRelevantObject;
import cn.edu.szu.cs.entity.KstcQuery;
import cn.edu.szu.cs.kstc.dbscan.AbstractDbScanBasedApproach;
import cn.edu.szu.cs.kstc.dbscan.SimpleDbScanBasedApproach;
import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.LRUCache;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson.JSON;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

/**
 *  SimpleDbScanBasedApproachDataFetchAction
 * @author Whitence
 * @date 2024/4/6 20:45
 * @version 1.0
 */
@SuppressWarnings("all")
public class SimpleDbScanBasedApproachDataFetchAction implements DataFetchAction<KstcQuery, List> {

    private Log log = LogFactory.get();

    private static final String CACHE_KEY_PREFIX = "SIMPLE_DBSCAN_BASED_APPROACH:{0}";
    private LRUCache<String,List> cache = CacheUtil.newLRUCache(128, 3600 * 1000L);

    private AbstractDbScanBasedApproach<DbScanRelevantObject> dbScanBasedApproach;

    public SimpleDbScanBasedApproachDataFetchAction() {
        this.dbScanBasedApproach = new SimpleDbScanBasedApproach();
    }


    @Override
    public String getCommand() {
        return DataFetchCommandConstant.SIMPLE_DBSCAN_BASED_APPROACH;
    }

    @Override
    public KstcQuery parseParams(String paramsStr) {

        return JSON.parseObject(paramsStr, KstcQuery.class);
    }

    @Override
    public boolean checkParams(KstcQuery params) {
        return true;
    }

    @Override
    public List fetchData(KstcQuery params) {

        String cacheKey = MessageFormat.format(CACHE_KEY_PREFIX, JSON.toJSONString(params));
        if(cache.containsKey(cacheKey)){
            return cache.get(cacheKey);
        }

        List<Set<DbScanRelevantObject>> sets = dbScanBasedApproach.kstcSearch(params);

        cache.put(cacheKey, sets);

        return sets;
    }

}
