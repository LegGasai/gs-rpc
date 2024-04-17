package com.leggasai.rpc.gsrpcspringboot.api.dto;

import com.leggasai.rpc.enums.ResponseStatus;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-17-11:09
 * @Description: rpc复杂对象传输测试
 */

@Data
public class Order implements Serializable {
    private Long orderId;

    private List<String> productsList;

    private Map<String,Integer> productsCountMap;

    private Set<String> productsSet;

    private Date createTime;

    private ResponseStatus status;
}
