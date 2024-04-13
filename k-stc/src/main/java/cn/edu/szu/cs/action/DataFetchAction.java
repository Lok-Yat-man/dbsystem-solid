package cn.edu.szu.cs.action;

import cn.edu.szu.cs.entity.BaseDataFetchActionParams;

public interface DataFetchAction <P extends BaseDataFetchActionParams,R>{

    String getCommand();

    String getCommandType();

    P parseParams(String paramsStr);

    boolean checkParams(P params);

    R fetchData(P params);

}
