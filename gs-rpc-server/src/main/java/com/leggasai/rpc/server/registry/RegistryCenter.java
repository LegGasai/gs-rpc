package com.leggasai.rpc.server.registry;

import com.leggasai.rpc.common.beans.RpcURL;
import com.leggasai.rpc.common.beans.ServiceMeta;
import com.leggasai.rpc.config.ProviderProperties;
import com.leggasai.rpc.constants.Separator;
import com.leggasai.rpc.server.service.ServiceManager;
import com.leggasai.rpc.utils.PathUtil;
import com.leggasai.rpc.zookeeper.CuratorClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Set;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-01-14:53
 * @Description: 注册中心
 */
@Component
public class RegistryCenter {

    private static final Logger logger = LoggerFactory.getLogger(RegistryCenter.class);
    private static final String SPLIT_TOKEN = Separator.PATH_SPLIT;
    private static final String PROVIDER_PREFIX = "providers";
    private static final String SERVICE_PREFIX = "services";

    private RpcURL rpcURL;

    public void setRpcURL(RpcURL rpcURL) {
        this.rpcURL = rpcURL;
    }

    private CuratorClient curatorClient;

    @Autowired
    private ProviderProperties providerProperties;
    @Autowired
    private ServiceManager serviceManager;


    @PostConstruct
    public void init() {
        String host = providerProperties.getRegistryHost();
        Integer port = providerProperties.getRegistryPort();
        Integer timeout = providerProperties.getRegistryTimeout();
        Integer session = providerProperties.getRegistrySession();
        this.curatorClient = new CuratorClient(host,port,timeout,session);
        if (this.curatorClient.isConnected()){
            this.curatorClient.watchState(new RegistryConnectionStateListener(this,timeout,session));
            logger.info("RegistryCenter has been connected to remote registry center {}:{}",host,port);
        }
    }

    /**
     * Registry all service from the service manager to the remoting registry center,e.g.Zookeeper(127.0.0.1:2181)
     */
    public void register(){
        Set<ServiceMeta> serviceMetaSet = serviceManager.getAllServices();
        doRegister(serviceMetaSet,true);
    }

    /**
     * Unregistry all registried service from the remoting registry center,e.g.Zookeeper(127.0.0.1:2181)
     */
    public void unregister(){
        logger.info("RegistryCenter is now unregistering all services");
        // 删除/providers/{providerKey}
        String providerPath = PathUtil.buildPath(SPLIT_TOKEN,PROVIDER_PREFIX,rpcURL.getAddress());
        curatorClient.deletePath(providerPath);
        // 删除/services/{servicesKey}/{providerKey}
        Set<ServiceMeta> serviceMetaSet = serviceManager.getAllServices();
        for (ServiceMeta serviceMeta : serviceMetaSet) {
            String serviceKey = serviceMeta.getServiceKey();
            String servicePath = PathUtil.buildPath(SPLIT_TOKEN,SERVICE_PREFIX,serviceKey,rpcURL.getAddress());
            curatorClient.deletePath(servicePath);
        }
        logger.info("RegistryCenter has unregistered all services");
    }

    public void close(){
        unregister();
        if (curatorClient != null){
            curatorClient.close();
            logger.info("RegistryCenter has been closed and disconnected from remote registry center {}:{}",providerProperties.getRegistryHost(),providerProperties.getRegistryHost());
        }
    }

    public void reRegisterAllServices(){
        // 清除缓存，否则zookeeper的延迟删除机制会显示节点已存在（上个会话创建的）
        unregister();
        Set<ServiceMeta> serviceMetaSet = serviceManager.getAllServices();
        doRegister(serviceMetaSet,false);
    }

    private void doRegister(Set<ServiceMeta> serviceMetaSet, Boolean first){
        if (serviceMetaSet.isEmpty()){
            logger.warn("No services have been provided by the current service provider");
            return;
        }
        if (!curatorClient.isConnected()){
            logger.error("Registry services fails, cannot connect to the registry center {}:{}",providerProperties.getRegistryHost(),providerProperties.getRegistryHost());
            return;
        }

        String providerParentPath = PathUtil.buildPath(SPLIT_TOKEN,PROVIDER_PREFIX,rpcURL.getAddress());

        curatorClient.createOrUpdatePersistent(providerParentPath,rpcURL.toString(),false);
        for (ServiceMeta serviceMeta : serviceMetaSet) {
            String serviceKey = serviceMeta.getServiceKey();
            String servicePath = PathUtil.buildPath(SPLIT_TOKEN,SERVICE_PREFIX,serviceKey,rpcURL.getAddress());
            String providerPath = PathUtil.buildPath(SPLIT_TOKEN,PROVIDER_PREFIX,rpcURL.getAddress(),serviceKey);
            curatorClient.createEphemeral(servicePath,rpcURL.toString(),!first);
            curatorClient.createEphemeral(providerPath,!first);
            logger.info("Service {} from {} has been registered to the registry center",serviceKey,rpcURL.getAddress());
        }
    }


}
