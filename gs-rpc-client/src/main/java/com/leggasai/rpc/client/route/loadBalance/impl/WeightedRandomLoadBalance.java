package com.leggasai.rpc.client.route.loadBalance.impl;

import com.leggasai.rpc.client.invoke.Invoker;
import com.leggasai.rpc.client.route.loadBalance.LoadBalance;

import java.util.List;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-14-16:54
 * @Description:
 */
public class WeightedRandomLoadBalance implements LoadBalance {
    @Override
    public Invoker select(List<Invoker> invokers) {

        return invokers.get(0);
    }


}
