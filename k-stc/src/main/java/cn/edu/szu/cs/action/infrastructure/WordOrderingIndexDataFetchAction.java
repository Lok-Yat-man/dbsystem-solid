package cn.edu.szu.cs.action.infrastructure;

import cn.edu.szu.cs.action.DataFetchAction;
import cn.edu.szu.cs.adapter.KstcDataFetchManager;
import cn.edu.szu.cs.constant.DataFetchConstant;
import cn.edu.szu.cs.dto.WordOrderingIndexQueryDTO;
import cn.edu.szu.cs.entity.DefaultRelevantObject;
import cn.edu.szu.cs.entity.OpticsRelevantObject;
import cn.edu.szu.cs.entity.OpticsRelevantObjectExtend;
import cn.edu.szu.cs.infrastructure.dataloader.IRelevantObjectDataLoader;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WordOrderingIndexDataFetchAction implements DataFetchAction<WordOrderingIndexQueryDTO, Queue> {


    private static final String CACHE_KEY_PREFIX = "WORD_ORDERING_INDEX:{0}";


    @Override
    public String getCommand() {
        return DataFetchConstant.WORD_ORDERING_INDEX;
    }

    @Override
    public String getCommandType() {
        return DataFetchConstant.INFRASTRUCTURE_LAYER;
    }

    @Override
    public WordOrderingIndexQueryDTO parseParams(String paramsStr) {
        return JSON.parseObject(paramsStr, WordOrderingIndexQueryDTO.class);
    }

    @Override
    public boolean checkParams(WordOrderingIndexQueryDTO params) {
        return StrUtil.isNotBlank(params.getKeyword());
    }

    @Override
    public Queue fetchData(WordOrderingIndexQueryDTO params) {

        List<OpticsRelevantObjectExtend> orderingData = KstcDataFetchManager.getWordOrderingIndex().getOrderingData(params.getKeyword());

        if(CollUtil.isEmpty(orderingData)){
            return new ArrayDeque<>();
        }

        IRelevantObjectDataLoader<DefaultRelevantObject> dataLoader = KstcDataFetchManager.getDataLoader();

        List<String> objIds = orderingData.stream().map(OpticsRelevantObjectExtend::getObjectId).collect(Collectors.toList());

        Map<String, DefaultRelevantObject> relevantObjectMap = dataLoader.getByIds(objIds)
                .stream()
                .collect(Collectors.toMap(
                        DefaultRelevantObject::getObjectId,
                        Function.identity()
                ));

        Queue<OpticsRelevantObject> arrayDeque = orderingData.stream()
                .map(
                        opticsRelevantObjectExtend -> {
                            DefaultRelevantObject defaultRelevantObject = relevantObjectMap.get(opticsRelevantObjectExtend.getObjectId());
                            OpticsRelevantObject opticsRelevantObject = new OpticsRelevantObject();
                            opticsRelevantObject.setObjectId(defaultRelevantObject.getObjectId());
                            opticsRelevantObject.setName(defaultRelevantObject.getName());
                            opticsRelevantObject.setLabels(defaultRelevantObject.getLabels());
                            opticsRelevantObject.setCoordinate(defaultRelevantObject.getCoordinate());
                            opticsRelevantObject.setReachableDistance(opticsRelevantObjectExtend.getReachableDistance() == null ? Double.MAX_VALUE : opticsRelevantObjectExtend.getReachableDistance());
                            opticsRelevantObject.setCoreDistance(opticsRelevantObjectExtend.getCoreDistance());
                            return opticsRelevantObject;
                        }
                ).collect(Collectors.toCollection(ArrayDeque::new));

        return new ArrayDeque<>(arrayDeque);
    }
}
