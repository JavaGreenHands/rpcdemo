package com.learning.rpc;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author baijie
 * @date 2019-10-09
 */
public class TestRpcClient {

    public static void main(String[] args) throws InterruptedException {

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
        RpcProxy rpcProxy = (RpcProxy) context.getBean("rpcProxy");

        // 调用代理的create方法，代理HelloService接口
        MyService helloService = rpcProxy.create(MyService.class);

        try {

            // 调用代理的方法，执行invoke
            String result = helloService.hello("World");
            System.out.println("服务端返回结果：");
            System.out.println(result);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread.sleep(Integer.MAX_VALUE);
    }
}
