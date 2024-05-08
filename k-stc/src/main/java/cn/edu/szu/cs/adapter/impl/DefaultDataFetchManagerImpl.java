package cn.edu.szu.cs.adapter.impl;

import cn.edu.szu.cs.action.DataFetchAction;
import cn.edu.szu.cs.adapter.DataFetchManager;
import cn.edu.szu.cs.constant.DataFetchConstant;
import cn.edu.szu.cs.entity.BaseDataFetchActionParams;
import cn.edu.szu.cs.entity.DataFetchResult;
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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@SuppressWarnings("all")
public class DefaultDataFetchManagerImpl implements DataFetchManager {

    private static final Log logger = LogFactory.get();

    private Map<String, Map<String,DataFetchAction>> dataFetchActionMap = null;
    private LRUCache<String, Tuple> taskMapCache = null;

    private static final String CACHE_KEY_PREFIX = "PARAMS_ACTION_ID:{0}:{1}:{2}";
    private LRUCache<String, String> paramsActionIdMap = null;

    private Map<String,ThreadPoolExecutor> threadPoolExecutorMap = null;

    public DefaultDataFetchManagerImpl() {

        logger.info("DataFetchManager init");

        List<String> layers = Arrays.asList(
                DataFetchConstant.OPERATIONAL_LAYER,
                DataFetchConstant.INFRASTRUCTURE_LAYER
                );

        threadPoolExecutorMap = new ConcurrentHashMap<>();
        threadPoolExecutorMap.put(DataFetchConstant.OPERATIONAL_LAYER, ThreadPoolExecutorHolder.getCpuIntensiveThreadPool());
        threadPoolExecutorMap.put(DataFetchConstant.INFRASTRUCTURE_LAYER, ThreadPoolExecutorHolder.getIoIntensiveThreadPool());


        dataFetchActionMap = new ConcurrentHashMap<>();

        taskMapCache = new LRUCache<>(256, 30 * 60 * 1000);
        paramsActionIdMap = new LRUCache<>(256, 30 * 60 * 1000);

        List<DataFetchAction> dataFetchActions = ServiceLoaderUtil.loadList(DataFetchAction.class);
        if (CollUtil.isNotEmpty(dataFetchActions)) {
            for (DataFetchAction dataFetchAction : dataFetchActions) {

                String commandType = dataFetchAction.getCommandType();
                if (StrUtil.isBlank(commandType)) {
                    throw new RuntimeException("CommandType is blank.: " + dataFetchAction.getClass().getName());
                }
                if(!layers.contains(commandType)){
                    throw new RuntimeException("CommandType is not supported.: " + dataFetchAction.getClass().getName());
                }
                dataFetchActionMap.putIfAbsent(commandType, new ConcurrentHashMap<>());

                String command = dataFetchAction.getCommand();
                if (StrUtil.isBlank(command)) {
                    throw new RuntimeException("Command is blank.: " + dataFetchAction.getClass().getName());
                }

                if (dataFetchActionMap.containsKey(command)) {
                    throw new RuntimeException("Duplicate command: " + command + " " + dataFetchAction.getClass().getName());
                }
                dataFetchActionMap.get(commandType).put(command, dataFetchAction);
            }
        }

        logger.debug("DataFetchActionMap:{}", dataFetchActionMap);
        logger.info("DataFetchManager init success");
    }


    @Override
    public void generateTask(String commandType, String command, String paramStr) {

        Map<String, DataFetchAction> fetchActionMap = dataFetchActionMap.get(commandType);
        if (fetchActionMap == null) {
            logger.error("DataFetchAction not found:{}", commandType);
            throw new RuntimeException("DataFetchAction not found: " + commandType);
        }

        DataFetchAction dataFetchAction = fetchActionMap.get(command);
        if (dataFetchAction == null) {
            logger.error("DataFetchAction not found:{}", command);
            throw new RuntimeException("DataFetchAction not found: " + command);
        }

        BaseDataFetchActionParams dataFetchActionParams = dataFetchAction.parseParams(paramStr);

        boolean checkParams = dataFetchAction.checkParams(dataFetchActionParams);

        if (!checkParams) {
            throw new RuntimeException("DataFetchAction checkParams failed: " + command + " " + paramStr);
        }

        String cacheKey = MessageFormat.format(CACHE_KEY_PREFIX, commandType, command, paramStr);

        if(paramsActionIdMap.containsKey(cacheKey)){
            return;
        }

        synchronized (DefaultDataFetchManagerImpl.class) {

            if(paramsActionIdMap.containsKey(cacheKey)){
                return;
            }

            String actionId = IdUtil.objectId();
            CompletableFuture<Object> future = CompletableFuture.supplyAsync(
                    () -> dataFetchAction.fetchData(dataFetchActionParams),
                    threadPoolExecutorMap.get(commandType));

            Tuple tuple = new Tuple(dataFetchAction.getCommand(), future);
            //
            //if(taskMapCache.isFull()){
            //    taskMapCache.clear();
            //    paramsActionIdMap.clear();
            //}

            taskMapCache.put(actionId, tuple);
            paramsActionIdMap.put(cacheKey, actionId);

        }
    }

    @Override
    @NonNull
    public List<DataFetchResult> listTask() {
        return null;
    }

    @Override
    @NonNull
    public DataFetchResult getTask(String actionId) {

        Tuple tuple = taskMapCache.get(actionId);

        if (tuple == null) {
            return DataFetchResult.fail(actionId, "", "Task not found");
        }

        String command = tuple.get(0);

        CompletableFuture<Object> future = tuple.get(1);
        Object value = null;
        try {
            value = future.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Task get failed.", e);
            return DataFetchResult.fail(actionId, command, e.getMessage());
        }
        return DataFetchResult.success(actionId, command, value);
    }

    @Override
    public DataFetchResult generateTaskAndGet(String commandType, String command, String paramsStr) {

        String cacheKey = MessageFormat.format(CACHE_KEY_PREFIX, commandType, command, paramsStr);
        if(paramsActionIdMap.containsKey(cacheKey)){
            synchronized (DefaultDataFetchManagerImpl.class){
                // double check
                if(paramsActionIdMap.containsKey(cacheKey)){
                    String actionId = paramsActionIdMap.get(cacheKey);
                    Tuple tuple = taskMapCache.get(actionId);

                    if (tuple != null) {
                        CompletableFuture<Object> future = tuple.get(1);
                        Object value = null;
                        try {
                            value = future.get();
                            return DataFetchResult.success(actionId, command, value);
                        } catch (InterruptedException | ExecutionException e) {
                            logger.error("Task get failed.", e);
                        }
                    }
                }
            }
        }

        Map<String, DataFetchAction> fetchActionMap = dataFetchActionMap.get(commandType);
        if (fetchActionMap == null) {
            logger.error("DataFetchAction not found:{}", commandType);
            throw new RuntimeException("DataFetchAction not found: " + commandType);
        }

        DataFetchAction dataFetchAction = fetchActionMap.get(command);
        if (dataFetchAction == null) {
            logger.error("DataFetchAction not found:{}", command);
            throw new RuntimeException("DataFetchAction not found: " + command);
        }

        BaseDataFetchActionParams dataFetchActionParams = dataFetchAction.parseParams(paramsStr);

        boolean checkParams = dataFetchAction.checkParams(dataFetchActionParams);

        if (!checkParams) {
            throw new RuntimeException("DataFetchAction checkParams failed: " + command + " " + paramsStr);
        }

        String actionId = IdUtil.objectId();
        Object value = null;
        try {
            value = dataFetchAction.fetchData(dataFetchActionParams);
        } catch (Exception e) {
            logger.error("Task get failed.", e);
            return DataFetchResult.fail(actionId, command, e.getMessage());
        }
        return DataFetchResult.success(actionId, command, value);

    }

}
