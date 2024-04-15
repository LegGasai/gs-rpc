package com.leggasai.rpc.client.route.loadBalance.impl;

import com.leggasai.rpc.client.invoke.Invocation;
import com.leggasai.rpc.client.invoke.Invoker;
import com.leggasai.rpc.client.route.loadBalance.LoadBalance;
import org.checkerframework.checker.units.qual.N;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Random;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-14-16:54
 * @Description:
 */
public class WeightedRandomLoadBalance implements LoadBalance {
    private static final Random random = new Random();
    @Override
    public Invoker select(List<Invoker> invokers, Invocation invocation) {
        if (CollectionUtils.isEmpty(invokers)){
            return null;
        }
        return doSelect(invokers);
    }

    private Invoker doSelect(List<Invoker> invokers){
        int totalWeight = invokers.stream().mapToInt(Invoker::getWeight).sum();
        int randomWeight = random.nextInt(totalWeight);
        int currentWeight = 0;
        for (Invoker invoker : invokers) {
            currentWeight += invoker.getWeight();
            if (currentWeight > randomWeight) {
                return invoker;
            }
        }
        return invokers.get(0);
    }

}
