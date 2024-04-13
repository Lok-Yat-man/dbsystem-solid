package cn.edu.szu.cs.adapter;

import cn.edu.szu.cs.entity.DataFetchResult;

import java.util.List;

public interface DataFetchManager {


    void generateTask(String commandType, String command, String paramsStr);

    List<DataFetchResult> listTask();

     DataFetchResult getTask(String actionId);


     DataFetchResult generateTaskAndGet(String commandType, String command, String paramsStr);


}
