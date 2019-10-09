package com.learning.rpc;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * * 处理具体的业务调用
 * * 通过构造时传入的“业务接口及实现”handlerMap，来调用客户端所请求的业务方法
 * * 并将业务方法返回值封装成response对象写入下一个handler（即编码handler——RpcEncoder）
 * @author baijie
 * @date 2019-10-08
 */
public class RpcHandler  extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcHandler.class);

    private final Map<String,Object> handlerMap;

    public RpcHandler(Map<String,Object> handlerMap){
        this.handlerMap = handlerMap;
    }

    /**
     * 接受消息,处理消息,返回结果
     * @param channelHandlerContext
     * @param rpcRequest
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {

        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(rpcRequest.getRequestId());
        System.out.println("收到消息:requestId"+rpcRequest.getRequestId());

        try {
            Object result = handle(rpcRequest);

            rpcResponse.setResult(result);

        }catch (Throwable t){
            rpcResponse.setError(t);
        }
        //写入 outbundle（即RpcEncoder）进行下一步处理（即编码）后发送到channel中给客户端
        channelHandlerContext.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE);
    }




    private Object handle(RpcRequest request) throws Throwable{
        String className = request.getClassName();

        //拿到实现类对象
        Object serviceBean = handlerMap.get(className);

        //拿到要执行的方法名
        String methodName = request.getMethodName();
        //参数类型
        Class<?>[] parameterTypes = request.getParameterTypes();
        //参数值
        Object[] parameters = request.getParameters();
        //拿到接口类
        Class<?> forName = Class.forName(className);

        Method method = forName.getMethod(methodName, parameterTypes);
        return method.invoke(serviceBean,parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("server caught exception", cause);
        ctx.close();
    }
}
