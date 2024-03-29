package com.learning.rpc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author baijie
 * @date 2019-10-08
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring.xml")
public class HelloServiceTest {

    @Autowired
    private RpcProxy rpcProxy;

    @Test
    public void helloTest1() {
        // 调用代理的create方法，代理HelloService接口
        MyService helloService = rpcProxy.create(MyService.class);

        // 调用代理的方法，执行invoke
        String result = helloService.hello("World");
        System.out.println("服务端返回结果：");
        System.out.println(result);
    }

    @Test
    public void helloTest2() {
        MyService helloService = rpcProxy.create(MyService.class);
        String result = helloService.hello(new Person("Yong", 12));
        System.out.println("服务端返回结果：");
        System.out.println(result);
    }
}
