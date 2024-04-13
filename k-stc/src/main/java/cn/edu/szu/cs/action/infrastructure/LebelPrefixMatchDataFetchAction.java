package cn.edu.szu.cs.action.infrastructure;

import cn.edu.szu.cs.action.DataFetchAction;
import cn.edu.szu.cs.constant.DataFetchConstant;
import cn.edu.szu.cs.dto.WordOrderingIndexQueryDTO;
import com.alibaba.fastjson.JSON;

import java.util.List;

public class LebelPrefixMatchDataFetchAction implements DataFetchAction<WordOrderingIndexQueryDTO, List> {
    @Override
    public String getCommand() {
        return DataFetchConstant.PREFIX_MATCH_KEYWORDS;
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
        return true;
    }

    @Override
    public List fetchData(WordOrderingIndexQueryDTO params) {



        return null;
    }
}
