package com.learning.rpc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * RPC解码器
 *
 * @author baijie
 * @date 2019-10-08
 */
public class RpcDecoder extends ByteToMessageDecoder {


    private Class<?> genericClass;

    public RpcDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        if(in.readableBytes() < 4){
            return;
        }
        in.markReaderIndex();
        int dataLength = in.readInt();

        if(dataLength < 0 ){
            ctx.close();
        }
        if(in.readableBytes() < dataLength){
            in.resetReaderIndex();
        }

        byte[] data = new byte[dataLength];
        in.readBytes(data);
        Object object = SerializationUtil.deserialize(data, genericClass);
        out.add(object);


    }
}
