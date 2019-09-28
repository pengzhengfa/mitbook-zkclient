package com.mitbook.zkclient.serializer;

/**
 * @author pengzhengfa
 */
public interface ZKSerializer {

    public byte[] serialize(Object data);

    public Object deserialize(byte[] bytes);
}
