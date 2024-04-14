package com.leggasai.rpc.client.route.loadBalance.impl;

import com.leggasai.rpc.client.invoke.Invoker;
import com.leggasai.rpc.client.route.loadBalance.LoadBalance;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-14-16:54
 * @Description:
 */
public class RoundRobinLoadBalance implements LoadBalance {
    private static final AtomicInteger roundRobin = new AtomicInteger(0);
    private static ConcurrentHashMap<Invoker,Integer> lastWeightMap = new ConcurrentHashMap<>();

    @Override
    public Invoker select(List<Invoker> invokers) {
        if (CollectionUtils.isEmpty(invokers)){
            return null;
        }
        return doSelect(invokers);
    }

    private Invoker doSelect(List<Invoker> invokers) {
        int totalWeight = invokers.stream().mapToInt(Invoker::getWeight).sum();
        int index = 0;
        int maxWeight = Integer.MIN_VALUE;
        for (int i = 0; i < invokers.size(); i++) {
            Invoker invoker = invokers.get(i);
            int currentWeight = invoker.getWeight() + lastWeightMap.computeIfAbsent(invoker,k->0);
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
