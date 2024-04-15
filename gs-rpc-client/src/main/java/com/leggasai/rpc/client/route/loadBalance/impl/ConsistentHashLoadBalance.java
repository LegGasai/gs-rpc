package com.leggasai.rpc.client.route.loadBalance.impl;

import com.google.common.hash.Hashing;
import com.leggasai.rpc.client.invoke.Invocation;
import com.leggasai.rpc.client.invoke.Invoker;
import com.leggasai.rpc.client.route.loadBalance.LoadBalance;
import com.leggasai.rpc.client.route.loadBalance.LoadBalanceFactory;
import com.leggasai.rpc.client.route.loadBalance.LoadBalanceType;
import com.leggasai.rpc.constants.Separator;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-14-16:54
 * @Description:
 */
public class ConsistentHashLoadBalance implements LoadBalance {
    private final int REPLICAS = 160;
    /**
     * Service#Method -> ConcurrentHashRing
     */
    private final static ConcurrentHashMap<String, ConsistentHashRing> serivces2HashRing = new ConcurrentHashMap<>();
    @Override
    public Invoker select(List<Invoker> invokers, Invocation invocation) {
        if (CollectionUtils.isEmpty(invokers)){
            return null;
        }
        // 如果invocation的参数为空，则调用随机负载均衡算法来选择Invoker
        if (invocation.getRequest().getParameters() == null){
            return LoadBalanceFactory.getLoadBalance(LoadBalanceType.RANDOM).select(invokers, invocation);
        }
        return doSelect(invokers,invocation);
    }

    private Invoker doSelect(List<Invoker> invokers,Invocation invocation) {
        String service = invocation.getRequest().getService();
        String method = invocation.getRequest().getMethod();
        String key = service + Separator.SERVICE_SPLIT + method;
        ConsistentHashRing hashRing = serivces2HashRing.get(key);
        int invokersHashcode = Objects.hash(invokers);
        // there is no corresponding concurrentHashRing or its invokers has changed
        if (hashRing == null || hashRing.identity != invokersHashcode) {
            ConsistentHashRing consistentHashRing = new ConsistentHashRing(invokers, REPLICAS, invokersHashcode);
            serivces2HashRing.put(key, consistentHashRing);
            hashRing = consistentHashRing;
        }
        return hashRing.select(invocation);
    }



    private static class ConsistentHashRing{
        private final TreeMap<Long, Invoker> virtualInvokers;
        private final int replicas;
        private final int identity;

        public ConsistentHashRing(List<Invoker> invokers, int replicas, int identity) {
            this.virtualInvokers = new TreeMap<>();
            this.replicas = replicas;
            this.identity = identity;
            buildRing(invokers);
        }

        private void buildRing(List<Invoker> invokers){
            for (Invoker invoker : invokers) {
                String providerKey = invoker.getAddress();
                for (int i = 0; i < replicas / 4; i++) {
                    String identityKey = providerKey + i;
                    byte[] digest = Hashing.md5().hashBytes(identityKey.getBytes()).asBytes();
                    for (int j = 0; j < 4; j++) {
                        long hashLong = hash(digest, j);
                        virtualInvokers.put(hashLong, invoker);
                    }
                }
            }
        }


        private long hash(byte[] digest, int number) {
            return (((long) (digest[3 + number * 4] & 0xFF) << 24)
                    | ((long) (digest[2 + number * 4] & 0xFF) << 16)
                    | ((long) (digest[1 + number * 4] & 0xFF) << 8)
                    | (digest[number * 4] & 0xFF))
                    & 0xFFFFFFFFL;
        }

        public Invoker select(Invocation invocation){
            Object[] parameters = invocation.getRequest().getParameters();
            StringBuilder sb = new StringBuilder();
            for (Object parameter : parameters) {
                if (parameter != null){
                    sb.append(parameter);
                }
            }
            byte[] digest = Hashing.md5().hashBytes(sb.toString().getBytes()).asBytes();
            return selectByKey(hash(digest, 0));
        }

        private Invoker selectByKey(long hash){
            Map.Entry<Long, Invoker> ceilingEntry = virtualInvokers.ceilingEntry(hash);
            if(ceilingEntry == null){
                return virtualInvokers.firstEntry().getValue();
            }
            return ceilingEntry.getValue();
        }
    }
}
