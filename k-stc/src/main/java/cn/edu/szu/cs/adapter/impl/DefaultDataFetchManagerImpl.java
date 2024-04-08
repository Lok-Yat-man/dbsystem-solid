package cn.edu.szu.cs.adapter.impl;

import cn.edu.szu.cs.adapter.DataFetchAction;
import cn.edu.szu.cs.adapter.DataFetchManager;
import cn.edu.szu.cs.entity.BaseDataFetchActionParams;
import cn.edu.szu.cs.entity.DataFetchTask;
import cn.edu.szu.cs.util.ThreadPoolExecutorHolder;
import cn.hutool.cache.impl.LRUCache;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Tuple;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ServiceLoaderUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("all")
public class DefaultDataFetchManagerImpl implements DataFetchManager {

    private Map<String, DataFetchAction> dataFetchActionMap = null;

    private final Log logger = LogFactory.get();


    private LRUCache<String, Tuple> taskMapCache = null;
    private LRUCache<String, String> paramsActionIdMap = null;

    public DefaultDataFetchManagerImpl() {

        logger.info("DataFetchManager init");
        List<DataFetchAction> dataFetchActions = ServiceLoaderUtil.loadList(DataFetchAction.class);

        if (CollUtil.isNotEmpty(dataFetchActions)) {

            dataFetchActionMap = new ConcurrentHashMap<>(dataFetchActions.size());

            taskMapCache = new LRUCache<>(128, 30 * 60 * 1000);
            paramsActionIdMap = new LRUCache<>(128, 30 * 60 * 1000);

            for (DataFetchAction dataFetchAction : dataFetchActions) {

                String command = dataFetchAction.getCommand();
                if (StrUtil.isBlank(command)) {
                    throw new RuntimeException("Command is blank.: " + dataFetchAction.getClass().getName());
                }
                if (dataFetchActionMap.containsKey(command)) {
                    throw new RuntimeException("Duplicate command: " + command + " " + dataFetchAction.getClass().getName());
                }
                dataFetchActionMap.put(command, dataFetchAction);
            }
        }
        logger.debug("DataFetchActionMap:{}", dataFetchActionMap);
        logger.info("DataFetchManager init success");
    }

    @Override
    @NonNull
    public String generateTask(String command, String paramStr) {
        DataFetchAction<BaseDataFetchActionParams, Object> dataFetchAction = dataFetchActionMap.get(command);

        if (dataFetchAction == null) {
            logger.error("DataFetchAction not found:{}", command);
            throw new RuntimeException("DataFetchAction not found: " + command);
        }
        BaseDataFetchActionParams dataFetchActionParams = dataFetchAction.parseParams(paramStr);

        boolean checkParams = dataFetchAction.checkParams(dataFetchActionParams);

        if (!checkParams) {
            throw new RuntimeException("DataFetchAction checkParams failed: " + command + " " + paramStr);
        }

        synchronized (DefaultDataFetchManagerImpl.class) {

            String actionId = IdUtil.objectId();

            CompletableFuture<Object> future = CompletableFuture.supplyAsync(
                    () -> dataFetchAction.fetchData(dataFetchActionParams),
                    ThreadPoolExecutorHolder.getThreadPool());

            Tuple tuple = new Tuple(dataFetchAction.getCommand(), future);
            taskMapCache.put(actionId, tuple);

            return actionId;
        }
    }

    @Override
    @NonNull
    public List<DataFetchTask> listTask() {
        return null;
    }

    @Override
    @NonNull
    public DataFetchTask getTask(String actionId) {

        Tuple tuple = taskMapCache.get(actionId);

        if (tuple == null) {
            return DataFetchTask.fail(actionId, "", "Task not found");
        }

        String command = tuple.get(0);

        CompletableFuture<Object> future = tuple.get(1);
        Object value = null;
        try {
            value = future.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Task get failed.", e);
            return DataFetchTask.fail(actionId, command, e.getMessage());
        }
        return DataFetchTask.success(actionId, command, value);
    }

}
