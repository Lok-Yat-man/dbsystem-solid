package cn.edu.szu.cs.action.infrastructure;

import cn.edu.szu.cs.action.DataFetchAction;
import cn.edu.szu.cs.adapter.KstcDataFetchManager;
import cn.edu.szu.cs.constant.DataFetchConstant;
import cn.edu.szu.cs.dto.WordOrderingIndexQueryDTO;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;

import java.util.List;

public class WordOrderingIndexDataFetchAction implements DataFetchAction<WordOrderingIndexQueryDTO, List> {

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
    public List fetchData(WordOrderingIndexQueryDTO params) {
        return KstcDataFetchManager.getWordOrderingIndex().getOrderingData(params.getKeyword());
    }
}
