package cn.edu.szu.cs.util;

import cn.hutool.core.date.TimeInterval;

public class TimerHolder {

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
        long intervalMs = getTimer().intervalMs(id);
        releaseTimer();
        return intervalMs;
    }

}
