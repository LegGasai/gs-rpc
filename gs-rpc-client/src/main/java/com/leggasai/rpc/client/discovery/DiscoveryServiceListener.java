package com.leggasai.rpc.client.discovery;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-03-12:14
 * @Description:
 */
public class DiscoveryServiceListener implements CuratorCacheListener {
    private final DiscoveryCenter discoveryCenter;
    private final String serviceKey;
    private final String path;

    public DiscoveryServiceListener(DiscoveryCenter discoveryCenter, String serviceKey, String path) {
        this.discoveryCenter = discoveryCenter;
        this.serviceKey = serviceKey;
        this.path = path;
    }

    @Override
    public void event(Type type, ChildData oldData, ChildData data) {
        switch (type) {
            case NODE_CREATED:{
                String providerKey = getProviderKey(data.getPath());
                if (!StringUtils.isEmpty(providerKey)) {
                    discoveryCenter.addInvoker(serviceKey, providerKey,new String(data.getData()));
                }
                break;
            }
            case NODE_CHANGED:{
                String providerKey = getProviderKey(data.getPath());
                if (!StringUtils.isEmpty(providerKey)) {
                    discoveryCenter.updateInvoker(serviceKey, providerKey,new String(data.getData()));
                }
                break;
            }

            case NODE_DELETED:{
                String providerKey = getProviderKey(oldData.getPath());
                if (!StringUtils.isEmpty(providerKey)) {
                    discoveryCenter.removeInvoker(serviceKey, providerKey);
                }
                break;
            }

        }
        System.out.println("--------------------------------");
        discoveryCenter.getService();
    }


    private String getProviderKey(String childPath){
        if(childPath.endsWith(path)){
            return null;
        }
        return childPath.substring(childPath.indexOf(path) + path.length() + 1);
    }
}
