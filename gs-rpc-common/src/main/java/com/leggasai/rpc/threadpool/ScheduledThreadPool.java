package com.leggasai.rpc.threadpool;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-05-10:49
 * @Description: Scheduled线程池
 */
public class ScheduledThreadPool{
    public static Executor getExecutor(String name, int cores) {
        return new ScheduledThreadPoolExecutor(cores,r -> new Thread(r,"gs-rpc-" + name + "-" + r.hashCode()));
    }
}
