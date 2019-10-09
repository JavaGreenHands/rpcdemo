package com.learning.rpc;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * 本类用于client发现server节点的变化 ，实现负载均衡
 *
 * @author baijie
 * @date 2019-10-08
 */
public class ServiceDiscovery {


    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscovery.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private volatile List<String> dataList =  new ArrayList<>();

    private String registryAddress;

    public ServiceDiscovery(String registryAddress){
        this.registryAddress = registryAddress;

        ZooKeeper zk = connectServer();
        if(zk != null){
            watchNode(zk);
        }

    }

    /**
     * 链接
     *
     * @return
     */
    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT,
                    new Watcher() {
                        @Override
                        public void process(WatchedEvent event) {
                            if (event.getState() == Event.KeeperState.SyncConnected) {
                                latch.countDown();
                            }
                        }
                    });
            latch.await();
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return zk;
    }

    /**
     * 监听节点
     * @param zk
     */
    private void watchNode(final ZooKeeper zk){
        try {
            //获取所有节点
            List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    //节点发生改变
                    if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                        watchNode(zk);
                    }
                }
            });
            List<String> dataList = new ArrayList<>();
            //循环子节点
            for (String node : nodeList) {
                //获取节点中服务器的地址
                byte[] data = zk.getData(Constant.ZK_REGISTRY_PATH + "/" + node, false, null);
                dataList.add(new String(data));

            }
            LOGGER.debug("node data: {}", dataList);
            this.dataList = dataList;
        }catch (Exception e){
            LOGGER.error("监听节点错误...",e);
        }

    }

    /**
     * 获取服务地址
     * @return
     */
    public String discover(){
        if(this.dataList != null && this.dataList.size() >0){

            int i = new Random().nextInt(dataList.size());

           return dataList.get(i);
        }

        return null;
    }

}
