package cn.edu.szu.cs.adapter.impl;

import cn.edu.szu.cs.adapter.DataFetchAction;
import cn.edu.szu.cs.common.Cacheable;
import cn.edu.szu.cs.common.DataFetchCommandConstant;
import cn.edu.szu.cs.entity.DbScanRelevantObject;
import cn.edu.szu.cs.entity.KstcQuery;
import cn.edu.szu.cs.kstc.dbscan.AbstractDbScanBasedApproach;
import cn.edu.szu.cs.kstc.dbscan.SimpleDbScanBasedApproach;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson.JSON;

import java.text.MessageFormat;
import java.util.List;

/**
 *  SimpleDbScanBasedApproachDataFetchAction
 * @author Whitence
 * @date 2024/4/6 20:45
 * @version 1.0
 */
@SuppressWarnings("all")
public class SimpleDbScanBasedApproachDataFetchAction implements DataFetchAction<KstcQuery, List>, Cacheable<KstcQuery> {

    private Log log = LogFactory.get();

    private static final String CACHE_KEY_PREFIX = "SIMPLE_DBSCAN_BASED_APPROACH:{0}";

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
        log.info("SimpleDbScanBasedApproachDataFetchAction parseParams:{}",paramsStr);
        return JSON.parseObject(paramsStr, KstcQuery.class);
    }

    @Override
    public boolean checkParams(KstcQuery params) {
        return true;
    }

    @Override
    public List fetchData(KstcQuery params) {
        return dbScanBasedApproach.kstcSearch(params);
    }

    @Override
    public String getCacheKey(KstcQuery params) {
        return MessageFormat.format(CACHE_KEY_PREFIX, JSON.toJSONString(params));
    }
}
