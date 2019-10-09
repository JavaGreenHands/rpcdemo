package com.learning.rpc;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * RPC 代理（用于创建 RPC 服务代理）
 *
 * @author baijie
 * @date 2019-10-08
 */
public class RpcProxy {

    private String serverAddress;
    private ServiceDiscovery serviceDiscovery;

    public RpcProxy(String serverAddress){
        this.serverAddress = serverAddress;

    }

    public RpcProxy(ServiceDiscovery serviceDiscovery){
        this.serviceDiscovery = serviceDiscovery;
    }

    public <T> T create(Class<?> intefaceClass){
        return (T) Proxy.newProxyInstance(intefaceClass.getClassLoader(), new Class[]{intefaceClass}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                RpcRequest request = new RpcRequest();
                request.setRequestId(UUID.randomUUID().toString());
                request.setClassName(method.getDeclaringClass().getName());
                System.out.println(method.getDeclaringClass().getName());
                request.setMethodName(method.getName());
                request.setParameterTypes(method.getParameterTypes());
                request.setParameters(args);
                //查找服务
                if(serviceDiscovery != null){
                    serverAddress = serviceDiscovery.discover();
                }

                if(serverAddress == null || "".equals(serverAddress)){
                    throw new RuntimeException("serverAddress is empty ,please check config");
                }

                String[] array = serverAddress.split(":");
                String host = array[0];
                int port = Integer.parseInt(array[1]);
                RpcClient client = new RpcClient(host, port);
                RpcResponse rpcResponse  = client.send(request);
                if(rpcResponse.isError()){
                    throw rpcResponse.getError();
                }else {
                    return rpcResponse.getResult();
                }
            }
        });
    }


}
