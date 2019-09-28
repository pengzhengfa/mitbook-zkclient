package com.mitbook.zkclient.serializer;

/**
 * 对Byte数组序列化,只是简单的原样返回
 *
 * @author pengzhengfa
 */
public class BytesSerializer implements ZKSerializer {

    @Override
    public Object deserialize(byte[] bytes) {
        return bytes;
    }

    @Override
    public byte[] serialize(Object bytes) {
        return (byte[]) bytes;
    }

}
