package com.leggasai.rpc.server.registry;

import com.leggasai.rpc.common.beans.RpcURL;
import com.leggasai.rpc.common.beans.ServiceMeta;
import com.leggasai.rpc.config.ProviderProperties;
import com.leggasai.rpc.config.RegistryProperties;
import com.leggasai.rpc.server.service.ServiceManager;
import com.leggasai.rpc.utils.PathUtil;
import com.leggasai.rpc.zookeeper.CuratorClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.common.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-01-14:53
 * @Description: 注册中心
 */
@Component
public class RegistryCenter{

    private static final Logger logger = LoggerFactory.getLogger(RegistryCenter.class);
    private static final String SPLIT_TOKEN = "/";
    private static final String PROVIDER_PREFIX = "providers";
    private static final String SERVICE_PREFIX = "services";

    private RpcURL rpcURL;

    public void setRpcURL(RpcURL rpcURL) {
        this.rpcURL = rpcURL;
    }

    private CuratorClient curatorClient;

    @Autowired
    private RegistryProperties registryProperties;

    @Autowired
    private ServiceManager serviceManager;


    @PostConstruct
    public void init() {
        String host = registryProperties.getHost();
        Integer port = registryProperties.getPort();
        Integer timeout = registryProperties.getTimeout();
        Integer session = registryProperties.getSession();
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
        logger.info("RegistryCenter has been closed and disconnected from remote registry center {}:{}",registryProperties.getHost(),registryProperties.getPort());
        unregister();
        curatorClient.close();
    }

    public void reRegisterAllServices(){
        // 清除缓存，否则zookeeper的延迟删除机制会显示节点已存在（上个会话创建的）
        unregister();
        Set<ServiceMeta> serviceMetaSet = serviceManager.getAllServices();
        doRegister(serviceMetaSet,false);
    }

    public void doRegister(Set<ServiceMeta> serviceMetaSet, Boolean first){
        if (!curatorClient.isConnected()){
            logger.error("Registry services fails, cannot connect to the registry center {}:{}",registryProperties.getHost(),registryProperties.getPort());
            return;
        }
        if (serviceMetaSet.isEmpty()){
            logger.warn("No services have been provided by the current service provider");
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
