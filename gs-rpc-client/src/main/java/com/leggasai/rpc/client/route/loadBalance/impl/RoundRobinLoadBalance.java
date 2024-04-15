package com.leggasai.rpc.client.route.loadBalance.impl;

import com.leggasai.rpc.client.invoke.Invocation;
import com.leggasai.rpc.client.invoke.Invoker;
import com.leggasai.rpc.client.route.loadBalance.LoadBalance;
import com.leggasai.rpc.constants.Separator;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-14-16:54
 * @Description: 基于权重的平滑轮询算法
 */
public class RoundRobinLoadBalance implements LoadBalance {
    /**
     * Service#Method -> lastWeightMap
     */
    private final static ConcurrentHashMap<String, ConcurrentHashMap<Invoker,Integer>> serivces2Weight = new ConcurrentHashMap<>();
    @Override
    public Invoker select(List<Invoker> invokers, Invocation invocation) {
        if (CollectionUtils.isEmpty(invokers)){
            return null;
        }
        return doSelect(invokers, invocation);
    }

    private Invoker doSelect(List<Invoker> invokers, Invocation invocation) {
        String service = invocation.getRequest().getService();
        String method = invocation.getRequest().getMethod();
        String key = service + Separator.SERVICE_SPLIT + method;
        ConcurrentHashMap<Invoker, Integer> lastWeightMap = serivces2Weight.computeIfAbsent(key, k -> new ConcurrentHashMap<>());

        int totalWeight = invokers.stream().mapToInt(Invoker::getWeight).sum();
        int index = 0;
        int maxWeight = Integer.MIN_VALUE;
        for (int i = 0; i < invokers.size(); i++) {
            Invoker invoker = invokers.get(i);
            int currentWeight = invoker.getWeight() + lastWeightMap.computeIfAbsent(invoker,k->0);
            lastWeightMap.put(invoker,currentWeight);
            if (currentWeight > maxWeight){
                maxWeight = currentWeight;
                index = i;
            }
        }
        Invoker selected = invokers.get(index);
        lastWeightMap.put(selected,lastWeightMap.get(selected) - totalWeight);
        return selected;
    }


}
