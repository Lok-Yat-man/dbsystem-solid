package cn.edu.szu.cs.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolExecutorHolder{
    public static ThreadPoolExecutor threadPool = null;
    static {
        int processors = Runtime.getRuntime().availableProcessors();
        threadPool=new ThreadPoolExecutor(
                processors+1,
                2*processors,
                30,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

    }

    public static ThreadPoolExecutor getThreadPool() {
        return threadPool;
    }
}