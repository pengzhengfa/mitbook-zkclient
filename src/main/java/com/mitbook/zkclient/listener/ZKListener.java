package com.mitbook.zkclient.listener;

import org.apache.zookeeper.Watcher.Event.EventType;

/**
 * 监听基类
 *
 * @author pengzhengfa
 */
public interface ZKListener {
    void handle(String path, EventType eventType, Object data) throws Exception;
}
