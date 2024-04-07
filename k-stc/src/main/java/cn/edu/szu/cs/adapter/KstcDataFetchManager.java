package cn.edu.szu.cs.adapter;

import cn.edu.szu.cs.entity.DataFetchTask;
import cn.hutool.core.util.ServiceLoaderUtil;
import lombok.NonNull;

import java.util.List;

/**
 * 用于接受并处理请求的管理器
 * <p> Implement the manager to accept and process requests
 * @author Whitence
 * @date 2024/4/6 19:30
 * @version 1.0
 */
@SuppressWarnings("all")
public class KstcDataFetchManager {

    private static DataFetchManager dataFetchManager = null;

    static {
        dataFetchManager = ServiceLoaderUtil.loadFirst(DataFetchManager.class);

        if(dataFetchManager == null){
            throw new RuntimeException("No implementation of DataFetchManager found");
        }
    }

    @NonNull
    public static String generateTask(String command, String paramsStr){
        return dataFetchManager.generateTask(command, paramsStr);
    }

    @NonNull
    public static List<DataFetchTask> listTask(){
        return dataFetchManager.listTask();
    }

    @NonNull
    public static DataFetchTask getTask(String actionId){
        return dataFetchManager.getTask(actionId);
    }

}
