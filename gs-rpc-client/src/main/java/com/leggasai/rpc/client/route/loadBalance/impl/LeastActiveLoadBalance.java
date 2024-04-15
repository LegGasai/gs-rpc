package com.leggasai.rpc.client.route.loadBalance.impl;

import com.leggasai.rpc.client.invoke.Invocation;
import com.leggasai.rpc.client.invoke.Invoker;
import com.leggasai.rpc.client.route.loadBalance.LoadBalance;

import java.util.List;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-14-16:54
 * @Description: 负载均衡：最少活跃有优先测量
 */
public class LeastActiveLoadBalance implements LoadBalance {
    @Override
    public Invoker select(List<Invoker> invokers, Invocation invocation) {
        // todo
        return invokers.get(0);
    }
}
