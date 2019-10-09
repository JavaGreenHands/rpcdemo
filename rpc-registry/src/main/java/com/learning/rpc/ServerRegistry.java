package com.learning.rpc;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * 服务注册 ，ZK 在该架构中扮演了“服务注册表”的角色，用于注册所有服务器的地址与端口，并对客户端提供服务发现的功能
 *
 * @author baijie
 * @date 2019-10-08
 */
public class ServerRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ServerRegistry.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private String registryAddress;

    public ServerRegistry(String registryAddress){

        this.registryAddress = registryAddress;
    }

    /**
     * 创建zookeeper 链接
     * @param data
     */
    public void registry(String data){

        if(data != null){

            ZooKeeper zk = connectServer();
            if(zk != null){
                createNode(zk,data);
            }
        }

    }


    private ZooKeeper connectServer(){
        ZooKeeper zk = null;

        try {

            zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if(watchedEvent.getState() == Event.KeeperState.SyncConnected){
                        latch.countDown();
                    }
                }
            });

            latch.await();
        }catch (Exception e){
            logger.error("connect zk error",e);
        }
        return zk;
    }

    /**
     * 创建节点
     * @param zk
     * @param data
     */
     private void createNode(ZooKeeper zk ,String data){

         try {
             byte[] bytes = data.getBytes();
             //如果该路径不存在创建
             if(zk.exists(Constant.ZK_REGISTRY_PATH,null) == null){
                 zk.create(Constant.ZK_REGISTRY_PATH,null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
             }

             String path = "" ;
             if(zk.exists(Constant.ZK_DATA_PATH,null) == null){
                 path =  zk.create(Constant.ZK_DATA_PATH,bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
                 logger.debug("create zookeeper node ({} => {})", path, data);
             }

        logger.info("创建节点完成,path="+ path);
         }catch (Exception e){
             logger.error("创建节点失败",e);
         }

     }

}
