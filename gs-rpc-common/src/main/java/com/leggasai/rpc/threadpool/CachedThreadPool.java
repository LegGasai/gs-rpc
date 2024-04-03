package com.leggasai.rpc.threadpool;

import java.util.concurrent.*;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-01-20:59
 * @Description: Cached线程池
 */
public class CachedThreadPool {
    public static Executor getExecutor(String name,int cores,int threads,int alive){
        return new ThreadPoolExecutor(cores,threads,alive, TimeUnit.MILLISECONDS,new SynchronousQueue<>(),r -> new Thread(r,"gs-rpc-" + name + "-" + r.hashCode()),new ThreadPoolExecutor.AbortPolicy());
    }
}
