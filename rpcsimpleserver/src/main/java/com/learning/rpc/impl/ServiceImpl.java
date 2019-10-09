package com.learning.rpc.impl;

import com.learning.rpc.MyService;
import com.learning.rpc.Person;
import com.learning.rpc.RpcService;

/**
 * 具体服务实现
 *
 * @author baijie
 * @date 2019-10-08
 */
@RpcService(MyService.class)
public class ServiceImpl implements MyService {


    public String hello(String name) {
        System.out.println("已经调用服务端接口实现，业务处理结果为：");
        System.out.println("Hello! " + name);
        return "Hello! " + name;

    }

    public String hello(Person person) {
        System.out.println("已经调用服务端接口实现，业务处理为：");
        System.out.println("Hello! "+person.toString());
        return "Hello! "+person.toString();
    }
}
