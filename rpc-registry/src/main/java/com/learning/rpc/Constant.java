package com.learning.rpc;

/**
 * 常量类
 *
 * @author baijie
 * @date 2019-10-08
 */
public class Constant {

    //zk超时时间
    public static final int ZK_SESSION_TIMEOUT = 50000;

    //注册节点
    public static final String ZK_REGISTRY_PATH = "/registry";
    //节点
    public static final String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";

}
