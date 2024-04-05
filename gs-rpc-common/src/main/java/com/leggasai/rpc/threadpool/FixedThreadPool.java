package com.leggasai.rpc.threadpool;

import java.util.concurrent.*;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-01-20:55
 * @Description: Fixed线程池
 */
public class FixedThreadPool{

    public static Executor getExecutor(String name,int threads,int queueSize){
        BlockingQueue<Runnable> blockQueue = null;
        if (queueSize > 0){
            blockQueue = new LinkedBlockingQueue<Runnable>(queueSize);
        }else{
            blockQueue = new LinkedBlockingQueue<Runnable>();
        }
        return new ThreadPoolExecutor(threads,threads,0L, TimeUnit.MILLISECONDS,blockQueue, r -> new Thread(r,"gs-rpc-" + name + "-" + r.hashCode()),new ThreadPoolExecutor.AbortPolicy());
    }
}
