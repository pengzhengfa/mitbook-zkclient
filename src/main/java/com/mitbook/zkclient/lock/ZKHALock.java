package com.mitbook.zkclient.lock;

import com.mitbook.zkclient.ZKClient;
import com.mitbook.zkclient.exception.ZKInterruptedException;
import com.mitbook.zkclient.exception.ZKNoNodeException;
import com.mitbook.zkclient.listener.ZKChildCountListener;
import com.mitbook.zkclient.listener.ZKStateListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * 主从服务锁,主服务一直持有锁,断开连接,从服务获得锁
 * 非线程安全,每个线程请单独创建实例
 *
 * @author pengzhengfa
 */
public class ZKHALock implements ZKLock {
    private final static Logger logger = LoggerFactory.getLogger(ZKHALock.class);

    private final ZKChildCountListener countListener;
    private final ZKStateListener stateListener;
    private final ZKClient client;
    private final String lockPath;
    private String currentSeq;
    private Semaphore semaphore;

    private ZKHALock(final ZKClient client, final String lockPach) {
        this.client = client;
        this.lockPath = lockPach;

        countListener = new ZKChildCountListener() {
            @Override
            public void handleSessionExpired(String path, List<String> children) throws Exception {
                //ignore
            }

            @Override
            public void handleChildCountChanged(String path, List<String> children) throws Exception {
                if (check(currentSeq, children)) {
                    semaphore.release();
                }
            }
        };

        stateListener = new ZKStateListener() {
            @Override
            public void handleStateChanged(KeeperState state) throws Exception {
                if (state == KeeperState.SyncConnected) {//如果重新连接
                    //如果重连后之前的节点已删除,并且lock处于等待状态,则重新创建节点,等待获得lock
                    if (!client.exists(lockPach + "/" + currentSeq) || semaphore.availablePermits() == 0) {
                        String newPath = client.create(lockPath + "/1", null, CreateMode.EPHEMERAL_SEQUENTIAL);
                        String[] paths = newPath.split("/");
                        currentSeq = paths[paths.length - 1];
                    }
                }
            }

            @Override
            public void handleSessionError(Throwable error) throws Exception {
                //ignore
            }

            @Override
            public void handleNewSession() throws Exception {
                //ignore
            }
        };
    }

    /**
     * 创建ZKHALock实例的工厂方法
     *
     * @param client
     * @param lockPach
     * @return ZKHALock
     */
    public static ZKHALock newInstance(final ZKClient client, final String lockPach) {
        if (!client.exists(lockPach)) {
            throw new ZKNoNodeException("The lockPath is not exists!,please create the node.[path:" + lockPach + "]");
        }

        ZKHALock zkhaLock = new ZKHALock(client, lockPach);
        //对lockPath进行子节点数量的监听
        client.listenChildCountChanges(lockPach, zkhaLock.countListener);
        //对客户端连接状态进行监听
        client.listenStateChanges(zkhaLock.stateListener);
        return zkhaLock;
    }

    @Override
    public boolean lock() {
        //信号量为0,线程就会一直等待直到数据变成正数
        semaphore = new Semaphore(0);
        String newPath = client.create(lockPath + "/1", null, CreateMode.EPHEMERAL_SEQUENTIAL);
        String[] paths = newPath.split("/");
        currentSeq = paths[paths.length - 1];
        boolean getLock = false;
        try {
            semaphore.acquire();
            getLock = true;
        } catch (InterruptedException e) {
            throw new ZKInterruptedException(e);
        }
        if (getLock) {
            logger.debug("get halock successful.");
        } else {
            logger.debug("failed to get halock.");
        }
        return getLock;
    }

    /**
     * 释放锁
     *
     * @return boolean
     * 如果释放锁成功返回true,否则返回false
     * 释放锁失败只有一种情况,就是线程正好获得锁,在释放之前,
     * 与服务器断开连接,并且会话过期,这时候服务器会自动删除EPHEMERAL_SEQUENTIAL节点.
     * 在会话过期之后再删除节点就会删除失败,因为路径已经不存在了.
     */
    @Override
    public boolean unlock() {
        return client.delete(lockPath + "/" + currentSeq);
    }


    /**
     * 判断路径是否可以获得锁,如果checkPath 对应的序列是所有子节点中最小的,则可以获得锁.
     *
     * @param checkPath 需要判断的路径
     * @param children  所有的子路径
     * @return boolean 如果可以获得锁返回true,否则,返回false;
     */
    private boolean check(String checkPath, List<String> children) {
        if (children == null || !children.contains(checkPath)) {
            return false;
        }
        //判断checkPath 是否是children中的最小值,如果是返回true,不是返回false
        Long chePathSeq = Long.parseLong(checkPath);
        boolean isLock = true;
        for (String path : children) {
            Long pathSeq = Long.parseLong(path);
            if (chePathSeq > pathSeq) {
                isLock = false;
                break;
            }
        }
        return isLock;
    }

}
