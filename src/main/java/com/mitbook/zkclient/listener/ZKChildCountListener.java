package com.mitbook.zkclient.listener;

import org.apache.zookeeper.Watcher.Event.EventType;

import java.util.List;

/**
 * 监听子节点数量变化,不监听子节点内容的变化
 *
 * @author pengzhengfa
 */
public abstract class ZKChildCountListener implements ZKListener {

    @Override
    public void handle(String path, EventType eventType, Object data) throws Exception {
        List<String> children = null;
        if (data != null) {
            children = (List<String>) data;
        }
        if (eventType == eventType.None) {
            handleSessionExpired(path, children);
        } else {

            handleChildCountChanged(path, children);
        }

    }

    /**
     * 子节点数量变化的回调函数
     *
     * @param path
     * @param children
     * @return void
     * @throws Exception
     */
    public abstract void handleChildCountChanged(String path, List<String> children) throws Exception;

    /**
     * 会话失效并重新连接后会回调此方法.
     * 因为在会话失效时,服务端会注销Wather监听,
     * 所以在会话失效后到连接成功这段时间内,数据可能发生变化,会触发此方法
     *
     * @param path
     * @return void
     * @throws Exception
     */
    public abstract void handleSessionExpired(String path, List<String> children) throws Exception;

}
