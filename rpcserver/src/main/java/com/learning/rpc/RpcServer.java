package com.learning.rpc;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * 框架的RPC 服务器（用于将用户系统的业务类发布为 RPC 服务）
 * 使用时可由用户通过spring-bean的方式注入到用户的业务系统中
 * 由于本类实现了ApplicationContextAware InitializingBean
 * spring构造本对象时会调用setApplicationContext()方法，从而可以在方法中通过自定义注解获得用户的业务接口和实现
 * 还会调用afterPropertiesSet()方法，在方法中启动netty服务器
 * @author baijie
 * @date 2019-10-08
 */
public class RpcServer  implements ApplicationContextAware, InitializingBean {

    private static  final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    private String serverAddress;

    private ServerRegistry serviceRegistry;

    private Map<String,Object> handlerMap = new HashMap<String, Object>();

    public RpcServer (String serverAddress){
        this.serverAddress = serverAddress;
    }

    public RpcServer(String serverAddress,ServerRegistry serviceRegistry){
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
    }


    /**
     * 在此启动netty服务，绑定handle流水线：
     * 1、接收请求数据进行反序列化得到request对象
     * 2、根据request中的参数，让RpcHandler从handlerMap中找到对应的业务impl，调用指定方法，获取返回结果
     * 3、将业务调用结果封装到response并序列化后发往客户端
     *
     */
    public void afterPropertiesSet() throws Exception {

        NioEventLoopGroup bossGroup = new NioEventLoopGroup();

        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap
                    .group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel
                                .pipeline()
                                .addLast(new RpcDecoder(RpcRequest.class))
                                .addLast(new RpcEncoder(RpcResponse.class))
                                .addLast(new RpcHandler(handlerMap));

                        }
                    }).option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true);

            String[] array = serverAddress.split(":");
            String host = array[0];
            int port = Integer.parseInt(array[1]);
            ChannelFuture future = bootstrap.bind(host, port).sync();
            LOGGER.debug("server started on port {}", port);
            if(serviceRegistry != null){
                serviceRegistry.registry(serverAddress);
            }
            future.channel().closeFuture().sync();

        }catch (Exception e){
            LOGGER.error("",e);
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }


    }

    /**
     * 通过注解 获取标注了rpc服务注解的业务类的----接口及impl对象，将它放到handlerMap中
     * @param applicationContext
     * @throws BeansException
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);

        if(MapUtils.isNotEmpty(serviceBeanMap)){
            for (Object value : serviceBeanMap.values()) {
//                从业务实现类上的自定义注解中获取到value，从来获取到业务接口的全名
                String interfaceName = value.getClass().getAnnotation(RpcService.class).value().getName();

                handlerMap.put(interfaceName,value);

            }
        }
    }
}
