package com.mitbook.zkclient.listener;

import org.apache.zookeeper.Watcher.Event.EventType;

/**
 * 节点变化监听,监听节点的创建,数据的修改,以及节点的删除
 *
 * @author pengzhengfa
 */
public abstract class ZKNodeListener implements ZKListener {

    @Override
    public void handle(String path, EventType eventType, Object data) throws Exception {

        if (eventType == EventType.NodeCreated) {
            handleDataCreated(path, data);
        }

        if (eventType == EventType.NodeDataChanged) {
            handleDataChanged(path, data);
        }

        if (eventType == EventType.NodeDeleted) {
            handleDataDeleted(path);
        }

        if (eventType == eventType.None) {
            handleSessionExpired(path);
        }
    }

    /**
     * 节点创建后回调此方法
     *
     * @param path
     * @param data
     * @return void
     * @throws Exception
     */
    public abstract void handleDataCreated(String path, Object data) throws Exception;

    /**
     * 节点内容变化的回调函数
     *
     * @param path
     * @param data
     * @return void
     * @throws Exception
     */
    public abstract void handleDataChanged(String path, Object data) throws Exception;

    /**
     * 节点删除的回调函数
     *
     * @param path
     * @return void
     * @throws Exception
     */
    public abstract void handleDataDeleted(String path) throws Exception;

    /**
     * 会话失效并重新连接后会回调此方法.
     * 因为在会话失效时,服务端会注销Wather监听,
     * 所以在会话失效后到连接成功这段时间内,数据可能发生变化,会触发此方法
     *
     * @param path
     * @return void
     * @throws Exception
     */
    public abstract void handleSessionExpired(String path) throws Exception;

}
