package com.leggasai.rpc.threadpool;

import java.util.concurrent.Executor;

public interface ThreadPool {

    Executor getExecutor(String name, int cores,int threads,int queueCapacity,int alive);
}
