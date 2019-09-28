package com.mitbook.zkclient.listener;

import org.apache.zookeeper.Watcher.Event.KeeperState;

/**
 * ZooKeeper状态监听类
 *
 * @author pengzhengfa
 */
public interface ZKStateListener {

    /**
     * 状态改变的回调函数
     *
     * @param state
     * @return void
     * @throws Exception
     */
    public void handleStateChanged(KeeperState state) throws Exception;

    /**
     * 会话创建的回调函数
     *
     * @return void
     * @throws Exception
     */
    public void handleNewSession() throws Exception;

    /**
     * 会话出错的回调函数
     *
     * @param error
     * @return void
     * @throws Exception
     */
    public void handleSessionError(final Throwable error) throws Exception;

}
