package com.learning.rpc;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 框架的Rpc 客户端 用于发Rpc请求
 *
 * @author baijie
 * @date 2019-10-08
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {


    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private String host;

    private int port;

    private RpcResponse response;

    private final Object obj = new Object();

    public RpcClient(String host,int port){

        this.host = host;
        this.port = port;
    }

    /**
     * 链接服务端,发送消息
     * @param request
     * @throws Exception
     */
    public RpcResponse send(RpcRequest request) throws Exception{

        System.out.println("发送消息:requestId"+request.getRequestId());
        EventLoopGroup group = new NioEventLoopGroup();
        try {

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {

                            // 向pipeline中添加编码、解码、业务处理的handler
                            socketChannel.pipeline()
                                    .addLast(new RpcEncoder(RpcRequest.class))
                                    .addLast(new RpcDecoder(RpcResponse.class))
                                    .addLast(RpcClient.this);

                        }
                    }).option(ChannelOption.SO_KEEPALIVE,true);

            //链接服务器
            ChannelFuture future = bootstrap.connect(host, port).sync();

            future.channel().writeAndFlush(request).sync();
            synchronized (obj){
                obj.wait();
            }
            if(response != null){
                future.channel().closeFuture().sync();
            }
            return response;
        }finally {
            group.shutdownGracefully();
        }

    }

    //读取服务端返回的结果
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {

        this.response = rpcResponse;

        synchronized (obj){
            obj.notifyAll();
        }
    }

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        logger.error("client caught exception", cause);
        ctx.close();
    }
}
