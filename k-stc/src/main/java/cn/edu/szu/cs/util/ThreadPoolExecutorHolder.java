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
    public static ThreadPoolExecutor cpuIntensiveThreadPool = null;
    public static ThreadPoolExecutor ioIntensiveThreadPool = null;

    static {
        int processors = Runtime.getRuntime().availableProcessors();
        cpuIntensiveThreadPool=new ThreadPoolExecutor(
                processors+1,
                2*processors,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(128),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        ioIntensiveThreadPool=new ThreadPoolExecutor(
                2*processors,
                4*processors,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(128),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

    }


    public static ThreadPoolExecutor getCpuIntensiveThreadPool() {
        return cpuIntensiveThreadPool;
    }

    public static ThreadPoolExecutor getIoIntensiveThreadPool() {
        return ioIntensiveThreadPool;
    }
}