package cn.edu.szu.cs.adapter;

import cn.edu.szu.cs.entity.BaseDataFetchActionParams;

public interface DataFetchAction <P extends BaseDataFetchActionParams,R>{

    String getCommand();

    P parseParams(String paramsStr);

    boolean checkParams(P params);

    R fetchData(P params);


}
