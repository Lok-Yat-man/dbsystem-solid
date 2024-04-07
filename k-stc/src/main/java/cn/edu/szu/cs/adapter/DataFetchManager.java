package cn.edu.szu.cs.adapter;

import cn.edu.szu.cs.entity.DataFetchTask;

import java.util.List;

public interface DataFetchManager {


    String generateTask(String command, String paramsStr);

    List<DataFetchTask> listTask();

     DataFetchTask getTask(String actionId);
}
