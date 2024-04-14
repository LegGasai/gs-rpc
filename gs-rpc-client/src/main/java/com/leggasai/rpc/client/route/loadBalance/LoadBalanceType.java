package com.leggasai.rpc.client.route.loadBalance;

public enum LoadBalanceType {
    CONSISTENT_HASH("consistenthash"),
    ROUND_ROBIN("roundrobin"),
    RANDOM("random"),
    LEAST_ACTIVE("leastactive"),
    SHORTEST_RESPONSE("shortestresponse"),

    ;


    private String type;

    private LoadBalanceType(String type) {
        this.type = type;
    }

    public static LoadBalanceType getByType(String type) {
        for (LoadBalanceType value : values()) {
            if (value.type.equalsIgnoreCase(type)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid LoadBalance : " + type);

    }
}
