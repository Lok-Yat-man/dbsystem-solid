package cn.edu.szu.cs.util;

import cn.hutool.core.date.TimeInterval;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

/**
 *  TimerHolder
 * @author Whitence
 * @date 2024/3/16 14:52
 * @version 1.0
 */
public class TimerHolder {
    private static final Log logger = LogFactory.get();
    private static final InheritableThreadLocal<TimeInterval> inheritableThreadLocal = new InheritableThreadLocal<>();

    /**
     * every thread has its own timer.
     * @return
     */
    private static TimeInterval getTimer(){
        if(inheritableThreadLocal.get() == null){
            inheritableThreadLocal.set(new TimeInterval());
        }
        return inheritableThreadLocal.get();
    }

    /**
     * release memory.
     */
    private static void releaseTimer(){
        inheritableThreadLocal.remove();
    }

    public static String start(){
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        String id = stackTraceElement.getClassName()+"."+stackTraceElement.getMethodName();
        getTimer().start(id);
        return id;
    }

    public static String start(String id){
        getTimer().start(id);
        return id;
    }


    public static long stop(){
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        String id = stackTraceElement.getClassName()+"."+stackTraceElement.getMethodName();
        long intervalMs = getTimer().intervalMs(id);
        releaseTimer();
        return intervalMs;
    }

    public static long stop(String id){
        return getTimer().intervalMs(id);
    }

    public static long stopAndRelease(String id){
        long intervalMs = getTimer().intervalMs(id);
        releaseTimer();
        return intervalMs;
    }


    public static void release(){
        releaseTimer();
    }

}
