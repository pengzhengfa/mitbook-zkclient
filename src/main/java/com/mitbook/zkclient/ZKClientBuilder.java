package com.mitbook.zkclient;

import com.mitbook.zkclient.exception.ZKException;
import com.mitbook.zkclient.serializer.SerializableSerializer;
import com.mitbook.zkclient.serializer.ZKSerializer;

/**
 * ZKClient辅助创建类
 *
 * @author pengzhengfa
 */
public class ZKClientBuilder {
    private int connectionTimeout = Integer.MAX_VALUE;
    private ZKSerializer zkSerializer = new SerializableSerializer();
    private int eventThreadPoolSize = 1;
    private String servers;
    private int sessionTimeout = 30000;
    private int retryTimeout = -1;

    /**
     * 创建ZClient
     *
     * @return ZKClientBuilder
     */
    public static ZKClientBuilder newZKClient() {
        ZKClientBuilder builder = new ZKClientBuilder();
        return builder;
    }

    /**
     * 创建ZClient
     *
     * @param servers
     * @return ZKClientBuilder
     */
    public static ZKClientBuilder newZKClient(String servers) {
        ZKClientBuilder builder = new ZKClientBuilder();
        builder.servers(servers);
        return builder;
    }

    /**
     * 组件并初始化ZKClient
     *
     * @return ZKClient
     */
    public ZKClient build() {
        if (servers == null || servers.trim().equals("")) {
            throw new ZKException("Servers can not be empty !");
        }
        ZKClient zkClient = new ZKClient(servers, sessionTimeout, retryTimeout, zkSerializer, connectionTimeout, eventThreadPoolSize);
        return zkClient;
    }

    /**
     * 设置服务器地址
     *
     * @param servers
     * @return ZKClientBuilder
     */
    public ZKClientBuilder servers(String servers) {
        this.servers = servers;
        return this;
    }

    /**
     * 设置序列化类,可选.
     * (默认实现:{@link SerializableSerializer})
     *
     * @param zkSerializer
     * @return ZKClientBuilder
     */
    public ZKClientBuilder serializer(ZKSerializer zkSerializer) {
        this.zkSerializer = zkSerializer;
        return this;
    }

    /**
     * 设置会话失效时间,可选
     * (默认值:30000,实际大小ZooKeeper会重新计算大概在2 * tickTime ~ 20 * tickTime)
     *
     * @param sessionTimeout
     * @return ZKClientBuilder
     */
    public ZKClientBuilder sessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
        return this;
    }

    /**
     * 连接超时时间,可选.
     * 默认值Integer.MAX_VALUE
     *
     * @param connectionTimeout
     * @return ZKClientBuilder
     */
    public ZKClientBuilder connectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    /**
     * 重试超时时间,可选,主要用于ZooKeeper与服务器断开后的重连.
     * 默认值-1,也就是没有超时限制
     *
     * @param retryTimeout
     * @return ZKClientBuilder
     */
    public ZKClientBuilder retryTimeout(int retryTimeout) {
        this.retryTimeout = retryTimeout;
        return this;
    }

    /**
     * 处理事件的线程数,可选,默认值为1
     *
     * @param eventThreadPoolSize
     * @return ZKClientBuilder
     */
    public ZKClientBuilder eventThreadPoolSize(int eventThreadPoolSize) {
        this.eventThreadPoolSize = eventThreadPoolSize;
        return this;
    }

}
