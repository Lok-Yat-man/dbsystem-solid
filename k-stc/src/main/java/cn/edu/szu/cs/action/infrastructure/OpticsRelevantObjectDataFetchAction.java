package cn.edu.szu.cs.action.infrastructure;

import cn.edu.szu.cs.action.DataFetchAction;
import cn.edu.szu.cs.constant.DataFetchConstant;
import cn.edu.szu.cs.entity.KstcQuery;
import cn.edu.szu.cs.entity.OpticsRelevantObject;
import cn.edu.szu.cs.infrastructure.dataloader.IRelevantObjectDataLoader;
import cn.edu.szu.cs.infrastructure.dataloader.RelevantObjectDataLoaderImpl;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson.JSON;

import java.util.List;

@SuppressWarnings("all")
public class OpticsRelevantObjectDataFetchAction implements DataFetchAction<KstcQuery, List> {

    private IRelevantObjectDataLoader<OpticsRelevantObject> relevantObjectDataLoader = null;

    private static final String CACHE_KEY_PREFIX = "OPTICS_DATA_BY_KEYWORDS:{0}";

    private final Log log = LogFactory.get();

    public OpticsRelevantObjectDataFetchAction() {

        ClassPathResource classPathResource = new ClassPathResource("objs.txt");

        relevantObjectDataLoader = new RelevantObjectDataLoaderImpl<>(classPathResource.getStream(), OpticsRelevantObject.class);

        List<OpticsRelevantObject> scanRelevantObjects = relevantObjectDataLoader.getAll();

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

        return relevantObjectDataLoader.getObjectsByKeywords(params.getKeywords());
    }

}
