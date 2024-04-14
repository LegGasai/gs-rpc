package com.leggasai.rpc.client.route.loadBalance;

import com.leggasai.rpc.client.route.loadBalance.impl.*;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-14-16:53
 * @Description:
 */
public class LoadBalanceFactory {
    private static final LoadBalance CONSISTENT_HASH_INSTANCE = new ConsistentHashLoadBalance();
    private static final LoadBalance LEAST_ACTIVE_INSTANCE = new LeastActiveLoadBalance();
    private static final LoadBalance ROUND_ROBIN_INSTANCE = new RoundRobinLoadBalance();
    private static final LoadBalance SHORTEST_RESPONSE_INSTANCE = new ShortestResponseLoadBalance();
    private static final LoadBalance WEIGHTED_RANDOM_INSTANCE = new WeightedRandomLoadBalance();


    public static LoadBalance getLoadBalance(LoadBalanceType loadBalanceType){
        switch (loadBalanceType){
            case CONSISTENT_HASH:
                return CONSISTENT_HASH_INSTANCE;
            case LEAST_ACTIVE:
                return LEAST_ACTIVE_INSTANCE;
            case ROUND_ROBIN:
                return ROUND_ROBIN_INSTANCE;
            case SHORTEST_RESPONSE:
                return SHORTEST_RESPONSE_INSTANCE;
            case RANDOM:
                return WEIGHTED_RANDOM_INSTANCE;
            default:
                return ROUND_ROBIN_INSTANCE;
        }
    }
}
