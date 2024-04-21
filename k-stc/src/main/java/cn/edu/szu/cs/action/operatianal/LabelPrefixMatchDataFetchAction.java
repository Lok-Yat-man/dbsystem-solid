package cn.edu.szu.cs.action.operatianal;

import cn.edu.szu.cs.action.DataFetchAction;
import cn.edu.szu.cs.adapter.KstcDataFetchManager;
import cn.edu.szu.cs.constant.DataFetchConstant;
import cn.edu.szu.cs.dto.LabelPrefixMatchQueryDTO;
import cn.hutool.cache.impl.LRUCache;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LabelPrefixMatchDataFetchAction implements DataFetchAction<LabelPrefixMatchQueryDTO, List> {

    private LRUCache<String,List<String>> cache = new LRUCache<>(32, 60 * 1000);

    @Override
    public String getCommand() {
        return DataFetchConstant.PREFIX_MATCH_KEYWORDS;
    }

    @Override
    public String getCommandType() {
        return DataFetchConstant.OPERATIONAL_LAYER;
    }

    @Override
    public LabelPrefixMatchQueryDTO parseParams(String paramsStr) {
        return JSON.parseObject(paramsStr, LabelPrefixMatchQueryDTO.class);
    }

    @Override
    public boolean checkParams(LabelPrefixMatchQueryDTO params) {
        return true;
    }

    @Override
    public List fetchData(LabelPrefixMatchQueryDTO params) {

        String keyword = params.getKeyword();
        if(StrUtil.isBlank(keyword)){
            return Collections.emptyList();
        }

        if(cache.containsKey(keyword)){
            return cache.get(keyword);
        }

        List<String> result = KstcDataFetchManager.getDataLoader().getAllLabels()
                .stream()
                .filter(label -> label.startsWith(keyword))
                .collect(Collectors.toList());

        cache.put(keyword,result);
        return result;
    }
}
