package com.learning.rpc;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

/**
 * 基于protostuff实现的序列化工具
 *
 * @author baijie
 * @date 2019-10-08
 */
public class SerializationUtil {

    private static Map<Class<?> , Schema<?>> cachedSchema = new ConcurrentHashMap<Class<?>,Schema<?>>();

    private static Objenesis objenesis = new ObjenesisStd(true);

    private SerializationUtil(){

    }

    /**
     * 获取类的schema信息
     * @param clazz
     * @param <T>
     * @return
     */
    private static <T> Schema<T> getSchema(Class<T> clazz){
        //从缓存中获取schema
        Schema<T> schema = (Schema<T>) cachedSchema.get(clazz);
        if (schema == null) {

           schema = RuntimeSchema.createFrom(clazz);

           if(schema != null){
               cachedSchema.put(clazz,schema);
           }
        }

        return schema;
    }

    /**
     * 序列化,将对象序列化成字节数组
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> byte[] serialize(T obj){

        Class<T> clazz = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

        try {
            Schema<T> schema = getSchema(clazz);
            return ProtobufIOUtil.toByteArray(obj,schema,buffer);
        }catch (Exception e){
            throw new IllegalStateException(e.getMessage(),e);
        }finally {
            buffer.clear();
        }

    }

    /**
     * 反序列化
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T deserialize(byte[] data,Class<T> clazz){
        try {

            T message = (T) objenesis.newInstance(clazz);
            Schema<T> schema = getSchema(clazz);
            ProtobufIOUtil.mergeFrom(data,message,schema);

            return message;
        }catch (Exception e){
            throw new IllegalStateException(e.getMessage(),e);
        }
    }
}
