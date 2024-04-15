package com.leggasai.rpc.client.route.loadBalance;

import com.leggasai.rpc.client.invoke.Invocation;
import com.leggasai.rpc.client.invoke.Invoker;

import java.util.List;

public interface LoadBalance {
    Invoker select(List<Invoker> invokers, Invocation invocation);
}
