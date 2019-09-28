package com.mitbook.zkclient.event;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ZooKeeper事件锁
 */
public class ZKEventLock extends ReentrantLock {

    private static final long serialVersionUID = 1L;

    private Condition nodeOrChildChangedCondition = newCondition();
    private Condition stateChangedCondition = newCondition();
    private Condition nodeEventCondition = newCondition();


    /**
     * 条件在节点改变(节点新增,修改,删除)或者子节点数量改变时被标记.
     * @return Condition
     */
    public Condition getNodeOrChildChangedCondition() {
        return nodeOrChildChangedCondition;
    }


    /**
     * 条件在ZooKeeper状态发生改变时被标记,包括,连接成功,断开连接,会话失效等.
     *
     * @return Condition
     */
    public Condition getStateChangedCondition() {
        return stateChangedCondition;
    }

    /**
     * 该条件在节点发生变化时会被标记
     *
     * @return Condition
     */
    public Condition getNodeEventCondition() {
        return nodeEventCondition;
    }
}