package com.mitbook.zkclient.lock;

/**
 * @author pengzhengfa
 */
public interface ZKLock {
    /**
     * 获得锁
     *
     * @return boolean 成功获得锁返回true,否则返回false
     */
    boolean lock();

    /**
     * 释放锁
     *
     * @return boolean
     * 如果释放锁成功返回true,否则返回false
     */
    boolean unlock();
}
