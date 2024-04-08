package cn.edu.szu.cs.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *  线程池管理类
 *  <p> It is used to manage the thread pool.
 * @author Whitence
 * @date 2024/4/7 10:20
 * @version 1.0
 */
public class ThreadPoolExecutorHolder{
    public static ThreadPoolExecutor threadPool = null;
    static {
        int processors = Runtime.getRuntime().availableProcessors();
        threadPool=new ThreadPoolExecutor(
                processors+1,
                2*processors,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

    }

    public static ThreadPoolExecutor getThreadPool() {
        return threadPool;
    }
}