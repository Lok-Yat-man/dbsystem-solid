package cn.edu.szu.cs.action.infrastructure;

import cn.edu.szu.cs.action.DataFetchAction;
import cn.edu.szu.cs.adapter.KstcDataFetchManager;
import cn.edu.szu.cs.constant.DataFetchConstant;
import cn.edu.szu.cs.entity.DefaultRelevantObject;
import cn.edu.szu.cs.entity.KstcQuery;
import cn.edu.szu.cs.entity.OpticsRelevantObject;
import cn.edu.szu.cs.infrastructure.dataloader.IRelevantObjectDataLoader;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson.JSON;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("all")
public class OpticsRelevantObjectDataFetchAction implements DataFetchAction<KstcQuery, List> {

    private IRelevantObjectDataLoader<DefaultRelevantObject> relevantObjectDataLoader = null;

    private static final String CACHE_KEY_PREFIX = "OPTICS_DATA_BY_KEYWORDS:{0}";

    private final Log log = LogFactory.get();

    public OpticsRelevantObjectDataFetchAction() {

        relevantObjectDataLoader = KstcDataFetchManager.getDataLoader();

    }

    @Override
    public String getCommand() {
        return DataFetchConstant.LOAD_OPTICS_DATA_BY_KEYWORDS;
    }

    @Override
    public String getCommandType() {
        return DataFetchConstant.INFRASTRUCTURE_LAYER;
    }

    @Override
    public KstcQuery parseParams(String paramsStr) {
        log.info("parseParams: {}", paramsStr);
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

        log.info("OpticsRelevantObject fetchData: {}", params);

        return relevantObjectDataLoader.getObjectsByKeywords(params.getKeywords())
                .stream().map(
                    defaultRelevantObject -> new OpticsRelevantObject(
                            defaultRelevantObject.getObjectId(),
                            defaultRelevantObject.getCoordinate(),
                            defaultRelevantObject.getName(),
                            defaultRelevantObject.getLabels(),
                            Double.MAX_VALUE,
                            Double.MAX_VALUE)
        ).collect(Collectors.toList());
    }

}
