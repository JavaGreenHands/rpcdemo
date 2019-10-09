package com.learning.rpc;

/**
 * 服务提供接口
 *
 * @author baijie
 * @date 2019-10-08
 */

public interface MyService {

    String hello(String name);

    String hello(Person person);


}
