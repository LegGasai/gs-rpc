package com.leggasai.rpc.threadpool;

import java.util.concurrent.*;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-01-20:59
 * @Description: Cached线程池
 */
public class CachedThreadPool {
    public static Executor getExecutor(String name, int cores, int threads, int alive,int queues){
        BlockingQueue<Runnable> blockingQueue;
        if(queues == 0){
            blockingQueue = new SynchronousQueue<>();
        }else if (queues > 0){
            blockingQueue = new LinkedBlockingQueue<>(queues);
        }else{
            throw new IllegalStateException();
        }
        return new ThreadPoolExecutor(cores,threads,alive, TimeUnit.MILLISECONDS,blockingQueue,r -> new Thread(r,"gs-rpc-" + name + "-" + r.hashCode()),new ThreadPoolExecutor.AbortPolicy());
    }
}
