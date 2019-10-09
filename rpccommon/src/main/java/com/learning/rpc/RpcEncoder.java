package com.learning.rpc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


/**
 * RPC编码器
 *
 * @author baijie
 * @date 2019-10-08
 */
public class RpcEncoder extends MessageToByteEncoder {

    private Class<?> genericClass;

    public RpcEncoder(Class<?> genericClass){
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {

        if(genericClass.isInstance(o)){

            byte[] serialize = SerializationUtil.serialize(o);
            byteBuf.writeInt(serialize.length);
            byteBuf.writeBytes(serialize);
        }

    }
}
