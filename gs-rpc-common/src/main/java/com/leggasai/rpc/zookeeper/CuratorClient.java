package com.leggasai.rpc.zookeeper;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.curator.framework.api.SetDataBuilder;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-03-19-21:48
 * @Description: Zookeeper客户端
 */
public class CuratorClient {
    private final Logger logger = LoggerFactory.getLogger(CuratorClient.class);
    private final CuratorFramework client;
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final String ZK_NAMESPACE = "gs-rpc";
    private static final int ZK_SESSION_TIMEOUT_MS = 60 * 1000;
    private static final int ZK_CONNECTION_TIMEOUT_MS = 5 * 1000;

    public CuratorClient(String host,int port){
        this(host,port,ZK_CONNECTION_TIMEOUT_MS,ZK_SESSION_TIMEOUT_MS);
    }
    public CuratorClient(String host,int port,int timeout,int session){
        try {
            String url = host + ":" + port;
            CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                    .connectString(url)
                    .namespace(ZK_NAMESPACE)
                    .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                    .connectionTimeoutMs(timeout)
                    .sessionTimeoutMs(session);
            client = builder.build();
            client.start();
            logger.info("Curator client正在启动，连接Zookeeper：{}",url);
            boolean connected = client.blockUntilConnected(timeout, TimeUnit.MILLISECONDS);
            if (connected){
                logger.info("Curator client启动成功，连接Zookeeper：{}",url);
            }else{
                logger.error("Curator client连接Zookeeper失败,url:{}",url);
            }
        }catch (Exception e){
            throw new IllegalStateException(e.getMessage(), e);
        }
    }


    public void createPersistent(String path, boolean allowExisted){
        createNode(path,null,CreateMode.PERSISTENT,true, allowExisted);
    }
    public void createPersistent(String path, String data, boolean allowExisted){
        createNode(path,data,CreateMode.PERSISTENT,true, allowExisted);
    }

    public void createEphemeral(String path, boolean allowExisted){
        createNode(path,null,CreateMode.EPHEMERAL,true, allowExisted);
    }

    public void createEphemeral(String path, String data, boolean allowExisted){
        createNode(path,data,CreateMode.EPHEMERAL,true, allowExisted);
    }



    private void createNode(String path, String data, CreateMode mode,boolean createParent,boolean allowExisted){
        try {
            CreateBuilder builder = client.create();
            if (createParent){
                builder.creatingParentsIfNeeded();
            }
            builder.withMode(mode);
            if (StringUtils.isEmpty(data)){
                builder.forPath(path);
            }else{
                builder.forPath(path,data.getBytes(CHARSET));
            }
        }catch (Exception e){
            if (!allowExisted){
                logger.error("CuratorClient fails to create ZNode for {}, it may already exist",path);
                throw new IllegalStateException(e.getMessage(), e);
            }else{
                logger.warn("CuratorClient fails to create ZNode for {}, it may already exist",path);
            }
        }
    }

    public void createOrUpdatePersistent(String path, String data, boolean allowExisted){
        createOrUpdate(path,data,CreateMode.PERSISTENT,true,allowExisted);
    }

    public void createOrUpdateEphemeral(String path, String data, boolean allowExisted){
        createOrUpdate(path,data,CreateMode.EPHEMERAL,true,allowExisted);
    }
    private void createOrUpdate(String path, String data,CreateMode mode, boolean createParent, boolean allowExisted){
        if (checkExits(path)){
            update(path,data);
        }else{
            createNode(path,data,mode,createParent,allowExisted);
        }
    }

    public void deletePath(String path){
        try {
            client.delete().deletingChildrenIfNeeded().forPath(path);
        }catch (NoNodeException ignoreException){}
        catch (Exception e){
            logger.error("CuratorClient fails to delete path for {} ",path);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void update(String path,String data,int version){
        updatePath(path,data,true,version);
    }

    public void update(String path,String data){
        updatePath(path,data,false,-1);
    }

    private void updatePath(String path,String data,Boolean withVersion,int version){
        byte[] bytes = data.getBytes(CHARSET);
        try {
            SetDataBuilder builder = client.setData();
            if (withVersion){
                builder.withVersion(version);
            }
            builder.forPath(path,bytes);
        }catch (Exception e){
            logger.error("CuratorClient fails to update path for {} ",path);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public List<String> getChildren(String path){
        try {
            return client.getChildren().forPath(path);
        }catch (NoNodeException e) {
            return null;
        } catch (Exception e) {
            logger.error("CuratorClient fails to get the children of path for {} ",path);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public String getData(String path) {
        try {
            byte[] dataBytes = client.getData().forPath(path);
            return (dataBytes == null || dataBytes.length == 0) ? null : new String(dataBytes, CHARSET);
        } catch (NoNodeException ignoreException) {
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 返回监听器，用于服务调用者订阅服务
     * @param path
     * @throws Exception
     */
    public CuratorCache watchPath(String path,CuratorCacheListener listener){
        CuratorCache cache = CuratorCache.build(client, path);
        cache.listenable().addListener(listener);
        cache.start();
        return cache;
    }


    public void watchState(ConnectionStateListener listener){
        client.getConnectionStateListenable().addListener(listener);
    }


    /**
     * 检查节点是否存在
     * @param path
     * @return
     */
    public boolean checkExits(String path){
        try {
            if (client.checkExists().forPath(path) != null){
                return true;
            }
        } catch (Exception e) {}
        return false;
    }

    /**
     * 判断是否已连接
     * @return
     */
    public boolean isConnected(){
        return client.getZookeeperClient().isConnected();
    }

    /**
     * 关闭连接
     * 用于服务提供者下线时，关闭连接
     */
    public void close(){
        client.close();
    }

}
