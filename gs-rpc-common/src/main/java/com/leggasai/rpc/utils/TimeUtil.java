package com.leggasai.rpc.utils;

public class TimeUtil {

    private static final long MOD = 100000000;
    public static Long getNanoTime(){
        return System.nanoTime() % MOD;
    }


    public static void printCostTime(String taskName, long startTime){
        System.out.println(String.format("[%s] cost:{%d} at:{%d} in thread:{%s}",taskName, getNanoTime() - startTime, getNanoTime(), Thread.currentThread().getName()));
    }
}
