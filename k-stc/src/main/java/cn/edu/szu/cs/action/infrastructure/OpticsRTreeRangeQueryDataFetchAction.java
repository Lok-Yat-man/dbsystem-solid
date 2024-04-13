package cn.edu.szu.cs.action.infrastructure;

import cn.edu.szu.cs.action.DataFetchAction;
import cn.edu.szu.cs.adapter.KstcDataFetchManager;
import cn.edu.szu.cs.constant.DataFetchConstant;
import cn.edu.szu.cs.entity.*;
import cn.edu.szu.cs.util.CommonUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson.JSON;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import rx.observables.BlockingObservable;

import java.util.*;

/**
 *  RTreeRangeQueryDataFetchAction
 * @author Whitence
 * @date 2024/4/7 15:06
 * @version 1.0
 */
@SuppressWarnings("all")
public class OpticsRTreeRangeQueryDataFetchAction implements DataFetchAction<KstcQuery, List> {


    private Log log = LogFactory.get();

    private static final String CACHE_KEY_PREFIX = "OPTICS_RTREE_RANGE_QUERY:{0}:{1}:{2}";

    @Override
    public String getCommand() {
        return DataFetchConstant.OPTICS_RTREE_RANGE_QUERY;
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

        if(StrUtil.isBlank(params.getCommand())) {
            return false;
        }

        if(CollUtil.isEmpty(params.getKeywords()) || params.getKeywords().stream().anyMatch(StrUtil::isBlank)) {
            return false;
        }


        return true;
    }

    @Override
    public List fetchData(KstcQuery params) {

        DataFetchResult task = KstcDataFetchManager.generateTaskAndGet(DataFetchConstant.INFRASTRUCTURE_LAYER,
                DataFetchConstant.LOAD_OPTICS_RTREE_DATA_BY_KEYWORDS,
                JSON.toJSONString(params));

        if(!task.isSuccess()) {
            throw new RuntimeException("RTreeRangeQueryDataFetchAction fetchData failed");
        }

        RTree<OpticsRelevantObject, GeoPointDouble> rTree = (RTree<OpticsRelevantObject, GeoPointDouble>) task.getData();

        BlockingObservable<Entry<OpticsRelevantObject, GeoPointDouble>> entryBlockingObservable = rTree
                .nearest(
                        GeoPointDouble.create(params.getCoordinate()[0], params.getCoordinate()[1]), params.getEpsilon(), Integer.MAX_VALUE)
                .toBlocking();


        List<OpticsRelevantObject> list = new ArrayList<>();
        entryBlockingObservable.forEach(entry -> {
            list.add(entry.value());
        });

        list.sort(Comparator.comparingDouble(obj-> CommonUtil.calculateDistance(obj.getCoordinate(), params.getCoordinate())));

        return list;
    }

}
